package pharoslabut.demo.autoIntersection.server;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;

import pharoslabut.demo.autoIntersection.msgs.*;
import pharoslabut.io.*;

/**
 * The intersection manager defines the strategy for AIM
 * 
 * @author Michael Hanna
 * @author Chien-Liang Fok
 */
public class IntersectionManager extends Thread implements MessageReceiver {

    private long nextAvailableETC;
    public static LinkedList<Robot> robotsCompleted;
//    private UDPSender server;
	private int serverPort;
	private NetworkInterface networkInterface;

    /**
     * default constructor
     * sets nextAvailableETC to a dummy low value to make the intersection available at initialization
     */
    public IntersectionManager(int serverPort)
    {
        nextAvailableETC = -1;
        robotsCompleted = new LinkedList<Robot>();
        this.serverPort = serverPort;
//        this.server = new UDPSender(this.serverPort);
        
        // Create the network interface and register this object as a listener for
        // incoming messages.
        networkInterface = new UDPNetworkInterface(serverPort);
        networkInterface.registerMsgListener(this);
    }

    /**
     * This method decides whether a robot is allowed to go through the intersection or not
     * @param r Robot
     * @return true if the intersection access is granted, false otherwise
     */
    public boolean isAllowedAccess(Robot r)
    {
        if( nextAvailableETC <= r.getETA() )
            return true;
        return false;
    }

    public static LinkedList<Robot> getRobotsCompletedCopy()
    {
        LinkedList<Robot> tempList = new LinkedList<Robot>();
        Iterator<Robot> iterator = robotsCompleted.iterator();
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
     * - empty the queue
     * - start the server connection
     * - loop indefinitely and keep reading the queue
     * - set up the strategy for the intersection
     * - granting/canceling reservations by dealing with the queue and setting the robots' times of arrival
     * </pre>
     * @return robot - an object robot to be sent back to the client
     *
    public static Robot manageIntersection()
    {
        PriorityQueue<Robot> queue = RobotsPriorityQueue.getQueueCopy();
        if(! queue.isEmpty())
        {
            Robot robot = queue.peek();
            if(isAllowedAccess(robot) )
            {
                nextAvailableETC = robot.getETC();              // don't modify the robot ETA, keep it as is
                queue.remove();
            }
            else
            {
                long timeDifference = robot.getETC() - robot.getETA();
                robot.setETA(nextAvailableETC);
                nextAvailableETC = nextAvailableETC + timeDifference;
                robot.setETC(nextAvailableETC);
                queue.remove();
            }
            return robot;
        }
        return null;
    }
     * */

    /**
     * <pre>
     * Starting a new thread
     * - empty the queue
     * - start the server connection
     * - loop indefinitely and keep reading the queue
     * - set up the strategy for the intersection
     * - granting/canceling reservations by dealing with the queue and setting the robots' times of arrival
     * </pre>
     * @return robot - an object robot to be sent back to the client
     */
    @Override
    public void run()
    {
        PriorityQueue<Robot> queue = RobotsPriorityQueue.getQueue();        // this is the static original Queue, not a copy of it
        while(true)
        {
            try
            {
                if(! queue.isEmpty())
                {
                    Robot robot = queue.peek();
                    if(isAllowedAccess(robot) )
                    {
                        this.nextAvailableETC = robot.getETC();              // don't modify the robot ETA, keep it as is
                        queue.remove();
                        robotsCompleted.add(robot);
                        
                        // Create a ReservationTimeMsg...
                        ReservationTimeMsg rtm = new ReservationTimeMsg(robot.getIP(), robot.getPort(), robot.getETA());
                        networkInterface.sendMessage(robot.getIP(), robot.getPort(), rtm);
                        
//                        server.send(robot);
                    }
                    else
                    {
                        System.out.println("robot " + robot.getIP() + ":" + robot.getPort() + " is not allowed, nextavailableETC = " + nextAvailableETC + ". The robot's ETA: " + robot.getETA());
                        long timeDifference = robot.getETC() - robot.getETA();
                        robot.setETA(nextAvailableETC);
                //        nextAvailableETC = nextAvailableETC + timeDifference;
                        robot.setETC(nextAvailableETC + timeDifference);
                  //      queue.remove();
                    }
                }

                // wait 100ms to receive acknowledgment from the client
      /*          Thread.sleep(100);
                
                Iterator<Robot> iterator = robotsCompleted.iterator();
                while(iterator.hasNext())
                {
                    Robot robot = (Robot) iterator.next();
                    if(! robot.isAcknowledged() )
                    {
                        server.send(robot);
                    }
                }
*/
     //           Thread.sleep(3000);
            }
            catch(Exception e)
            { e.printStackTrace(); }
        }
    }

    /**
     * Handles incoming messages.
     */
	@Override
	public void newMessage(Message msg) {
		if (msg instanceof RequestAccessMsg)
    		handleRequestAccessMsg( (RequestAccessMsg) msg );
    	else if (msg instanceof ReservationTimeAcknowledgedMsg)
    		handleReservationTimeAcknowledgedMsg( (ReservationTimeAcknowledgedMsg) msg );
    	else if (msg instanceof ExitingMsg)
    		handleExitingMsg( (ExitingMsg) msg );
//    	else if (msg instanceof ExitingAcknowledgedMsg)
//    		handleExitingAcknowledgedMsg( (ExitingAcknowledgedMsg) msg );
    	else
    		System.out.println("RECEIVER: Unknown message " + msg);
	}
	
    private static void handleRequestAccessMsg(RequestAccessMsg msg) {
		if(msg != null)
		{
			Robot robot = new Robot(msg.getRobotIP(), msg.getRobotPort(), msg.getLaneSpecs(), msg.getETA(), msg.getETC());
			if(! robot.isEnqueued() )
            {
            	robot.setEnqueued(true);
                RobotsPriorityQueue.enqueue(robot);
            }
		}
	}

	private static void handleReservationTimeAcknowledgedMsg(ReservationTimeAcknowledgedMsg msg) {
		if(msg != null)
		{
			//Iterator<Robot> iterator = IntersectionManager.robotsGrantedAccess.iterator();
	        //while(iterator.hasNext())
	        {
	            //Robot robot = iterator.next();
	            //if( robot.getID() == msg.getRobotID() )
	            {
	            	//IntersectionManager.robotsGrantedAccess.remove(robot);
	            	//break;
	            }
	        }	
		}
	}

	private static void handleExitingMsg(ExitingMsg msg) {
		if(msg != null)
		{
			Iterator<Robot> iterator = IntersectionManager.robotsCompleted.iterator();
	        while(iterator.hasNext())
	        {
	            Robot robot = iterator.next();
	            if(robot.getIP().equals(msg.getRobotIP()) && robot.getPort() == msg.getRobotPort())
	            {
	            	IntersectionManager.robotsCompleted.remove(robot);
	            	break;
	            }
	        }	
		}
	}
}
