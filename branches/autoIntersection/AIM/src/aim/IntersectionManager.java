package aim;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;


/**
 * The intersection manager defines the strategy for AIM
 * @author Michael Hanna
 */

public class IntersectionManager extends Thread {

    private long nextAvailableETC;
    private static LinkedList<Robot> robotsCompleted;
    private static LinkedList<Robot> robotsPending;

    /**
     * default constructor
     * sets nextAvailableETC to a dummy low value to make the intersection available at initialization
     */
    public IntersectionManager()
    {
        nextAvailableETC = -1;
        robotsCompleted = new LinkedList<Robot>();
        robotsPending = new LinkedList<Robot>();
    }

    /**
     * This method decides whether a robot is allowed to go through the intersection or not
     * @param r Robot
     * @return true if the intersection access is granted, false otherwise
     */
    public boolean isAllowedAccess(Robot r)
    {
        if( nextAvailableETC < r.getETA() )
            return true;
        return false;
    }

    public static LinkedList<Robot> getRobotsCompleted()
    {
        LinkedList<Robot> tempList = new LinkedList<Robot>();
        Iterator iterator = robotsCompleted.iterator();
        while(iterator.hasNext())
        {
            Robot robot = (Robot) iterator.next();
            tempList.add(robot);
        }
        return tempList;
    }

    public static LinkedList<Robot> getRobotsPending()
    {
        LinkedList<Robot> tempList = new LinkedList<Robot>();
        Iterator iterator = robotsPending.iterator();
        while(iterator.hasNext())
        {
            Robot robot = (Robot) iterator.next();
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
                        robot.setAllowed(true);
                        queue.remove();
                        robotsCompleted.add(robot);
                        if( robotsPending.contains(robot) )
                            robotsPending.remove(robot);
                    }
                    else
                    {
                        long ETA_before = robot.getETA();
                        long timeDifference = robot.getETC() - robot.getETA();
                        robot.setETA(nextAvailableETC);
                        nextAvailableETC = nextAvailableETC + timeDifference;
                        robot.setETC(nextAvailableETC);
                        robot.setAllowed(false);
                        robot.setStoppageTime(robot.getETA() - ETA_before);
                  //      queue.remove();
                        robotsPending.add(robot);
                    }
                }
                Thread.sleep(2000);
            }
            catch(Exception e)
            { e.printStackTrace(); }
        }
    }



}
