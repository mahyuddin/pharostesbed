package pharoslabut.demo.autoIntersection.server;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;


/**
 * The intersection manager defines the strategy for AIM
 * @author Michael Hanna
 */

public class IntersectionManager extends Thread {

    private long nextAvailableETC;
    public static LinkedList<Robot> robotsCompleted;
    private UDPSender server;
	private int serverPort;

    /**
     * default constructor
     * sets nextAvailableETC to a dummy low value to make the intersection available at initialization
     */
    public IntersectionManager(int serverPort)
    {
        nextAvailableETC = -1;
        robotsCompleted = new LinkedList<Robot>();
        this.serverPort = serverPort;
        this.server = new UDPSender(this.serverPort);
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
                        server.send(robot);
                    }
                    else
                    {
                        System.out.println("robot " + robot.getID() + " is not allowed, nextavailableETC = " + nextAvailableETC + ". The robot's ETA: " + robot.getETA());
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



}
