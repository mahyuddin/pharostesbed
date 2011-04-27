package pharoslabut.demo.autoIntersection.server;

import java.util.Iterator;
import java.util.LinkedList;

import pharoslabut.demo.autoIntersection.msgs.*;
import pharoslabut.io.*;
import pharoslabut.logger.FileLogger;

/**
 * The intersection manager defines the strategy for AIM
 * 
 * @author Michael Hanna
 * @author Chien-Liang Fok
 */
public class ServerIntersectionManager extends Thread implements MessageReceiver {

    private long nextAvailableETC;
    public LinkedList<Robot> robotsGrantedAccess;
	private NetworkInterface networkInterface;

	/**
	 * For logging debug messages.
	 */
	private FileLogger flogger;
	
	//for testing of robot's ability to wait for 10 secs every other traversal
	private boolean testingFlag = false;
	
    /**
     * default constructor
     * sets nextAvailableETC to a dummy low value to make the intersection available at initialization
     */
    public ServerIntersectionManager(int serverPort, FileLogger flogger)
    {
    	this.flogger = flogger;
    	log("Starting intersection manager on port " + serverPort + "...");
        nextAvailableETC = -1;
        robotsGrantedAccess = new LinkedList<Robot>();
   
        // Create the network interface and register this object as a listener for
        // incoming messages.
        networkInterface = new TCPNetworkInterface(serverPort); //UDPNetworkInterface(serverPort);
        networkInterface.registerMsgListener(this);
    }

    /**
     * This method decides whether a robot is allowed to go through the intersection or not
     * @param robot Robot
     * @return true if the intersection access is granted, false otherwise
     */
    public boolean isAllowedAccess(Robot robot)
    {
        if( nextAvailableETC <= robot.getETA() )
            return true;
        return false;
    }

    public LinkedList<Robot> getRobotsGrantedAccessCopy()
    {
        LinkedList<Robot> tempList = new LinkedList<Robot>();
        Iterator<Robot> iterator = robotsGrantedAccess.iterator();
        while(iterator.hasNext())
        {
            Robot robot = iterator.next();
            tempList.add(robot);
        }
        return tempList;
    }
    
    
    /**
     * <pre>
     * Starting a new thread
     * - loop indefinitely and keep reading the queue
     * - set up the strategy for the intersection
     * - granting/canceling reservations by dealing with the queue and setting the robots' times of arrival
     * </pre>
     */
    @Override
    public void run()
    {
        while(true)
        {
            try
            {
                if(! RobotsPriorityQueue.isEmpty())
                {
                    Robot robot = RobotsPriorityQueue.top();
                    log("This robot is on top of the queue: \n" + robot);
                    if(isAllowedAccess(robot) )
                    {
                    	log("This robot is allowed to go through the intersection:\n" + robot);
                    	robotsGrantedAccess.add(robot);
                        this.nextAvailableETC = robot.getETC(); 	// don't modify the robot ETA, keep it as is
                        RobotsPriorityQueue.dequeue(robot);
                        
                        // Create a ReservationTimeMsg...
                        ReservationTimeMsg rtm = new ReservationTimeMsg(robot.getIP(), robot.getPort(), robot.getETA());
                        log("RUN: Sending the ReservationTimeMsg to client: " + robot.getIP() + ":" + robot.getPort());
                        
                        if (! networkInterface.sendMessage(robot.getIP(), robot.getPort(), rtm)) {
                			log("WARNING: failed to send ReservationTimeMsg to client: " + robot.getIP() + ":" + robot.getPort());
                		} else {
                			log("RUN: Sent ReservationTimeMsg to client: " + robot.getIP() + ":" + robot.getPort());
                		}                    
                    }
                    /**
                     * else, if the robot is not allowed to go through the intersection:
                     * delay its ETA and ETC,
                     * and keep it inside the queue for the next iteration
                     */
                    else
                    {
                        log("This robot is not allowed to go through the intersection: \n" + robot);
                        long timeDifference = robot.getETC() - robot.getETA();
                        robot.setETA(nextAvailableETC);
                        robot.setETC(nextAvailableETC + timeDifference);
                    }
                }
                else {
                	Thread.sleep(50);
                }
            }
            catch(Exception e) {
            	e.printStackTrace(); }
        }
    }

    /**
     * Handles incoming messages.
     */
	@Override
	public void newMessage(Message msg) {
		log("RECEIVED MESSAGE: " + msg);
		if (msg instanceof RequestAccessMsg)
    		handleRequestAccessMsg( (RequestAccessMsg) msg );
    	else if (msg instanceof ExitingMsg)
    		handleExitingMsg((ExitingMsg) msg );
    	else
    		log("RECEIVER: Unknown message " + msg);
	}


	private void handleRequestAccessMsg(RequestAccessMsg msg) {
		if(msg != null)
		{
			Robot robot = new Robot(msg.getRobotIP(), msg.getRobotPort(), msg.getLaneSpecs(), msg.getETA(), msg.getETC());
			
			//testing to make robot wait 10 secs every other loop traversal
			if(testingFlag) {
				log("Testing flag UP!");
				nextAvailableETC = robot.getETA()+10000; // 10 seconds after intersection arrival
				testingFlag = false;
			}
			else {
				testingFlag = true;
				log("Testing flag DOWN!");
			}
			
			if( (! robotsGrantedAccess.contains(robot))  && (! RobotsPriorityQueue.contains(robot)) ) {
				log("enqueueing the robot: \n" + robot);
				RobotsPriorityQueue.enqueue(robot);
				log("RobotsPriorityQueue: " + RobotsPriorityQueue.print() );
				//this.start();
			} else {
				log("This robot was already granted access: \n" + robot);
			}
		}
		else
		{
			log("The received message is null");
		}
	}


	private void handleExitingMsg(ExitingMsg msg) {
		if(msg != null)
		{
			Robot robot = new Robot(msg.getRobotIP(), msg.getRobotPort());
			
			robotsGrantedAccess.remove(robot);
            
			log("Robot exiting! Removing robot from the list.");
			
			//uncomment line below if using UDP
            //networkInterface.sendMessage(msg.getRobotIP(), msg.getRobotPort(), msg);	
		}
	}
	
	/**
	 * Logs a debug message.  This message is only printed when debug mode is enabled.
	 * 
	 * @param msg The message to log.
	 */
	private void log(String msg) {
		log(msg, true);
	}
	
	/**
	 * Logs a message.
	 * 
	 * @param msg  The message to log.
	 * @param isDebugMsg Whether the message is a debug message.
	 */
	private void log(String msg, boolean isDebugMsg) {
		String result = "IntersectionManager: " + msg;
		if (!isDebugMsg || System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
    
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(msg);
	}
    
	private static void usage() {
		System.setProperty ("PharosMiddleware.debug", "true");
		print("Usage: pharoslabut.demo.autoIntersection.server.ServerIntersectionManager <options>\n");
		print("Where <options> include:");
		print("\t-port <port number>: The port on which to listen (default 6665)");
		print("\t-log <log file name>: The name of the file in which to save debug output (default ServerIntersectionManager.log)");
		print("\t-debug: enable debug mode");
	}
	
    /**
     * call the IntersectionManager and start running the code
     * @param args the command line arguments
     * @throws InterruptedException
     */
    public static void main(String [] args) {
		int serverPort = 6665;
		String logFileName = "ServerIntersectionManager.log";
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-port")) {
					serverPort = Integer.valueOf(args[++i]);
				} else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				} else if (args[i].equals("-log")) {
					logFileName = args[++i];
				} else {
					print("Unknown argument " + args[i]);
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		FileLogger flogger = new FileLogger(logFileName);
		
//       RobotsPriorityQueue.test();
    	
        Thread sim = new ServerIntersectionManager(serverPort, flogger);
        
        sim.start();

/*
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new pharoslabut.demo.autoIntersection.server.GUI.MainWindow().setVisible(true);
            }
        });   
        */ 
    }
}
