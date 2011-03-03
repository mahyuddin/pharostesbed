package aim;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * The intersection manager defines the strategy for AIM
 * @author ut
 */

public class IntersectionManager extends Thread {

    private static long nextAvailableETC;

    /**
     * default constructor
     * sets nextAvailableETC to a dummy low value to make the intersection available at initialization
     */
    public IntersectionManager()
    {
        nextAvailableETC = -1;
    }

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
     */
    public void run()
    {
        Queue.makeEmpty();
        new Server();
        while(true)
        {
            if(! Queue.isEmpty())
            {
                Robot robot = Queue.getFront();
                if(isAllowedAccess(robot) )
                {
                    nextAvailableETC = robot.getETC();              // don't modify the robot ETA, keep it as is
                    Queue.dequeue();
                }
                else
                {
                    long timeDifference = robot.getETC() - robot.getETA();
                    robot.setETA(nextAvailableETC);
                    nextAvailableETC = nextAvailableETC + timeDifference;
                    robot.setETC(nextAvailableETC);
                    Queue.dequeue();
                }
            }
            else
            {
                try {
                    Thread.sleep(500);
                }
                catch (InterruptedException ex) {
                    Logger.getLogger(IntersectionManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
//        String hostname = "192.168.1.22";
//        String hostname = "localhost";
//        List allRobots = Collections.synchronizedList(new ArrayList());
    }

}
