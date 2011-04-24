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
public class IntersectionManager extends Thread implements MessageReceiver {

    private long nextAvailableETC;
    public LinkedList<Robot> robotsGrantedAccess;
    public LinkedList<Robot> robotsExiting;
	private NetworkInterface networkInterface;

	/**
	 * For logging debug messages.
	 */
	private FileLogger flogger;
	
    /**
     * default constructor
     * sets nextAvailableETC to a dummy low value to make the intersection available at initialization
     */
    public IntersectionManager(int serverPort)
    {
    	System.out.println("Starting intersection manager on port " + serverPort + "...");
        nextAvailableETC = -1;
        robotsGrantedAccess = new LinkedList<Robot>();
        robotsExiting = new LinkedList<Robot>();
        flogger = new FileLogger("IntersectionManager.log");
   
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
     * Handles incoming messages.
     */
	@Override
	public void newMessage(Message msg) {
		log("RECEIVED MESSAGE: " + msg);
		if (msg instanceof RequestAccessMsg)
    		handleRequestAccessMsg( (RequestAccessMsg) msg );
    	else if (msg instanceof ReservationTimeAcknowledgedMsg)
    		handleReservationTimeAcknowledgedMsg( (ReservationTimeAcknowledgedMsg) msg );
    	else if (msg instanceof ExitingMsg)
    		handleExitingMsg( (ExitingMsg) msg );
    	else
    		log("RECEIVER: Unknown message " + msg);
	}


	private void handleRequestAccessMsg(RequestAccessMsg msg) {
		if(msg != null)
		{
			Robot robot = new Robot(msg.getRobotIP(), msg.getRobotPort(), msg.getLaneSpecs(), msg.getETA(), msg.getETC());
			if( (! robotsGrantedAccess.contains(robot))  && (! RobotsPriorityQueue.contains(robot)) ) {
				log("enqueueing the robot: \n" + robot);
				RobotsPriorityQueue.enqueue(robot);
				log("RobotsPriorityQueue: " + RobotsPriorityQueue.print() );
//				this.start();
			} else {
//				log("This robot was already granted access: \n" + robot);
			}
		}
		else
		{
			log("The received message is null");
		}
	}

	private void handleReservationTimeAcknowledgedMsg(ReservationTimeAcknowledgedMsg msg) {
		if(msg != null)
		{
			Robot robot = new Robot(msg.getRobotIP(), msg.getRobotPort());
			robotsGrantedAccess.remove(robot);
			robotsExiting.add(robot);
//			Iterator<Robot> iterator = robotsGrantedAccess.iterator();
//	        while(iterator.hasNext())
//	        {
//	            Robot robot = iterator.next();
//	            if( robot.getIP().equals(msg.getRobotIP()) && robot.getPort() == msg.getRobotPort() )
//	            {	            	
//	            }
//	        }	
		}
	}

	private void handleExitingMsg(ExitingMsg msg) {
		if(msg != null)
		{
            log("Run: Sending EXITING ACKNOWLEDGED MESSAGE " + msg.getRobotIP() + ":" + msg.getRobotPort());
            
            networkInterface.sendMessage(msg.getRobotIP(), msg.getRobotPort(), msg);   
            Robot robot = new Robot(msg.getRobotIP(), msg.getRobotPort());
            robotsExiting.remove(robot);
//			Iterator<Robot> iterator = robotsExiting.iterator();
//	        while(iterator.hasNext())
	//        {
//	            Robot robot = iterator.next();
//	            if(robot.getIP().equals(msg.getRobotIP()) && robot.getPort() == msg.getRobotPort())
//	            {
//	            	robotsExiting.remove(robot);
//	            }
//	        }	
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
//            PriorityQueue<Robot> queue = RobotsPriorityQueue.getQueue();        // this is the static original Queue, not a copy of it
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
                    
          //              networkInterface.sendMessage(robot.getIP(), robot.getPort(), rtm);                        
                    }
                    else
                    {
                        log("This robot is not granted access: \n" + robot);
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
}
