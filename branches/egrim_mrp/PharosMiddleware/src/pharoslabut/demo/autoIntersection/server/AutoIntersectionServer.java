package pharoslabut.demo.autoIntersection.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;

import pharoslabut.demo.autoIntersection.*;
import pharoslabut.demo.autoIntersection.msgs.*;
import pharoslabut.demo.autoIntersection.server.GUI.IntersectionPanel;
import pharoslabut.demo.autoIntersection.server.GUI.LogPanel;
import pharoslabut.io.*;
import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;

/**
 * The intersection manager defines the strategy for AIM
 * 
 * @author Michael Hanna
 * @author Chien-Liang Fok
 */
public class AutoIntersectionServer extends Thread implements MessageReceiver {

    private long nextAvailableETC;
    public LinkedList<Robot> robotsGrantedAccess;
	private NetworkInterface networkInterface;

//	/**
//	 * For logging debug messages.
//	 */
//	private FileLogger flogger;
	
	private static int nWays;
	private static int nLanes;
	private static int intersectionWidth;
		
	
	//for testing of robot's ability to wait for 10 secs every other traversal
	private boolean testingFlag = false;
	
	IntersectionDetectorDisplay display = new IntersectionDetectorDisplay();
	
    /**
     * default constructor
     * sets nextAvailableETC to a dummy low value to make the intersection available at initialization
     */
    public AutoIntersectionServer(int serverPort)
    {
    	
    	Logger.log("Starting intersection manager on port " + serverPort + "...");
        nextAvailableETC = -1;
        robotsGrantedAccess = new LinkedList<Robot>();
   
        // Create the network interface and register this object as a listener for
        // incoming messages.
        networkInterface = new TCPNetworkInterface(serverPort); //UDPNetworkInterface(serverPort);
        networkInterface.registerMsgListener(this);
    }
    
    public static int getNWays() {
    	return nWays;
    }
    
    public static int getNLanes() {
    	return nLanes;
    }
    
    public static int getIntersectionWidth() {
    	return intersectionWidth;
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
                    Logger.log("This robot is on top of the queue: " + "robotIP:" + robot.getIP().getHostAddress() + " robotPort:" + robot.getPort());
                    if(isAllowedAccess(robot) )
                    {
                    	Logger.log("This robot is allowed to go through the intersection:" + "robotIP:" + robot.getIP().getHostAddress() + " robotPort:" + robot.getPort());
                    	robotsGrantedAccess.add(robot);
                        this.nextAvailableETC = robot.getETC(); 	// don't modify the robot ETA, keep it as is
                        RobotsPriorityQueue.dequeue(robot);
                        
                        // Create a ReservationTimeMsg...
                        ReservationTimeMsg rtm = new ReservationTimeMsg(robot.getIP(), robot.getPort(), robot.getETA());
                        Logger.log("Sending the ReservationTimeMsg to client: " + "robotIP:" + robot.getIP().getHostAddress() + " robotPort:" + robot.getPort());
                        
                        if (! networkInterface.sendMessage(robot.getIP(), robot.getPort(), rtm)) {
                        	Logger.log("WARNING: failed to send ReservationTimeMsg to client: " + "robotIP:" + robot.getIP().getHostAddress() + " robotPort:" + robot.getPort());
                		} else {
                			Logger.log("Sent ReservationTimeMsg to client: " + "robotIP:" + robot.getIP().getHostAddress() + " robotPort:" + robot.getPort());
                		}                    
                    }
                    /**
                     * else, if the robot is not allowed to go through the intersection:
                     * delay its ETA and ETC,
                     * and keep it inside the queue for the next iteration
                     */
                    else
                    {
                    	Logger.log("This robot is not allowed to go through the intersection: " + "robotIP:" + robot.getIP().getHostAddress() + " robotPort:" + robot.getPort());
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
    
    private void handleAutoIntDebugMsg(AutoIntDebugMsg msg) {
    	Logger.log("Received AutoIntDebugMsg: " + msg);
    	display.updateText(msg.getIE().getType().toString());
    }

    /**
     * Handles incoming messages.
     */
	@Override
	public void newMessage(Message msg) {
		Logger.log("RECEIVED MESSAGE: " + msg);
		if (msg instanceof RequestAccessMsg)
    		handleRequestAccessMsg( (RequestAccessMsg) msg );
    	else if (msg instanceof ExitingMsg)
    		handleExitingMsg((ExitingMsg) msg );
    	else if (msg instanceof AutoIntDebugMsg) {
    		handleAutoIntDebugMsg((AutoIntDebugMsg)msg);
    	}
    	else
    		Logger.log("RECEIVER: Unknown message " + msg);
	}


	private void handleRequestAccessMsg(RequestAccessMsg msg) {
		if(msg != null)
		{
			Robot robot = new Robot(msg.getRobotIP(), msg.getRobotPort(), msg.getLaneSpecs(), msg.getETA(), msg.getETC());
			
			//testing to make robot wait 10 secs every other loop traversal
			/*if(testingFlag) {
				log("Testing flag UP!");
				nextAvailableETC = robot.getETA()+10000; // 10 seconds after intersection arrival
				testingFlag = false;
			}
			else {
				testingFlag = true;
				log("Testing flag DOWN!");
			}*/
			
			if( (! robotsGrantedAccess.contains(robot))  && (! RobotsPriorityQueue.contains(robot)) ) {
				Logger.log("enqueueing the robot: " + "robotIP:" + robot.getIP().getHostAddress() + " robotPort:" + robot.getPort());
				RobotsPriorityQueue.enqueue(robot);
				Logger.log("RobotsPriorityQueue: " + RobotsPriorityQueue.print() );
			} else {
				Logger.log("This robot was already granted access: " + "robotIP:" + robot.getIP().getHostAddress() + " robotPort:" + robot.getPort());
			}
			IntersectionPanel.approachingCar(msg.getRobotIP(), msg.getLaneSpecs());
		}
		else
		{
			Logger.log("The received message is null");
		}
	}


	private void handleExitingMsg(ExitingMsg msg) {
		if(msg != null)
		{
			Robot robot = new Robot(msg.getRobotIP(), msg.getRobotPort());
			robotsGrantedAccess.remove(robot);
			Logger.log("Robot exiting! Removing robot from the list.");
			
			IntersectionPanel.exitingCar(msg.getRobotIP());
			
			//uncomment line below if using UDP
            //networkInterface.sendMessage(msg.getRobotIP(), msg.getRobotPort(), msg);	
		}
	}
	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(msg);
	}
    
	private static void usage() {
		System.setProperty ("PharosMiddleware.debug", "true");
		print("Usage: " + AutoIntersectionServer.class.getName() + " <options>\n");
		print("Where <options> include:");
		print("\t-port <port number>: The port on which to listen (default 6665)");
		print("\t-ways <number of ways>: the number of ways in the intersection (default 4)");
		print("\t-lanes <number of lanes>: the number of lanes in each way of the the intersection (default 2)");
		print("\t-width <Intersection width>: the width of the intersection in cm (default 150cm)");
		print("\t-log <log file name>: The name of the file in which to save debug output (default AutoIntersectionServer.log)");
		print("\t-debug: enable debug mode");
	}
	
    /**
     * call the ServerIntersectionManager and start running the code
     * @param args the command line arguments
     * @throws InterruptedException
     */
    public static void main(String [] args) {
		int serverPort = 6665
				;
		String logFileName = "AutoIntersectionServer.log";
		nWays = 4;
		nLanes = 2;
		intersectionWidth = 150;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-port")) {
					serverPort = Integer.valueOf(args[++i]);
				} else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				} else if (args[i].equals("-log")) {
					logFileName = args[++i];
				} else if (args[i].equals("-ways")) {
					nWays =  Integer.valueOf(args[++i]);
				} else if (args[i].equals("-lanes")) {
					nLanes = Integer.valueOf(args[++i]);
				} else if (args[i].equals("-width")) {
					intersectionWidth = Integer.valueOf(args[++i]);
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
		
		Logger.setFileLogger(new FileLogger(logFileName));
		
		if( nWays == 4  &&  nLanes == 2 ) {
			TwoLaneFourWayIntersectionSpecs is = new TwoLaneFourWayIntersectionSpecs(intersectionWidth);
		}
		    	
        Thread sim = new AutoIntersectionServer(serverPort);
        sim.start();

        java.awt.EventQueue.invokeLater(new Runnable() {
            @SuppressWarnings("static-access")
			public void run() {
                new pharoslabut.demo.autoIntersection.server.GUI.IntersectionGUI().createAndShowGUI();
            }
        });   
    }
}
