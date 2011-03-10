package aim;

import java.util.PriorityQueue;


/**
 * The intersection manager defines the strategy for AIM
 * @author Michael Hanna
 */

public class IntersectionManager {

    private static long nextAvailableETC = -1;

    /**
     * default constructor
     * sets nextAvailableETC to a dummy low value to make the intersection available at initialization
     
    public IntersectionManager()
    {
        nextAvailableETC = -1;
    }
    */

    /**
     * This method decides whether a robot is allowed to go through the intersection or not
     * @param r Robot
     * @return true if the intersection access is granted, false otherwise
     */
    public static boolean isAllowedAccess(Robot r)
    {
        if( nextAvailableETC < r.getETA() )
            return true;
        return false;
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
     */
    public static Robot manageIntersection()
    {
        PriorityQueue<Robot> queue = RobotsPriorityQueue.getQueue();
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

}
