package pharoslabut.demo.autoIntersection.server;

import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.PriorityQueue;

import pharoslabut.demo.autoIntersection.*;

/**
 *
 * @author Michael Hanna
 */
public class RobotsPriorityQueue
{
    private static final int DEFAULT_CAPACITY = 10;
    private static Comparator<Robot> comparator = new RobotsComparator();
    private static PriorityQueue<Robot> queue = new PriorityQueue<Robot>(DEFAULT_CAPACITY, comparator);


    public static PriorityQueue<Robot> getQueue()
    {
        return queue;
    }

    public static int getDefaultCapacity()
    {
        return DEFAULT_CAPACITY;
    }


    public static PriorityQueue<Robot> getQueueCopy()
    {
        PriorityQueue<Robot> tempQueue = new PriorityQueue<Robot>(DEFAULT_CAPACITY, comparator);
        Iterator<Robot> iterator = queue.iterator();
        while(iterator. hasNext())
        {
            Robot robot = iterator.next();
            tempQueue.add(robot);
        }
        return tempQueue;
    }


    public static void dequeue()
    {
        queue.remove();
    }
    
    public static void dequeue(Robot robot)
    {
        queue.remove(robot);
    }

    public static void enqueue(Robot r)
    {
        queue.add(r);
    }
    
    public static boolean isEmpty()
    {
    	return queue.isEmpty();
    }
    
    public static Robot top()
    {
    	return queue.peek();
    }
    
    public static boolean contains(Robot robot) {
    	return queue.contains(robot);
    }


    /**
     * Used to print the elements inside the RobotsPriorityQueue in order
     * NOTE: DON't use the default toString() method.. it doesn't print the queue in order
     * @return String to be printed
     */
    public static String print()
    {
  //      update();
        String output = "";
        Iterator<Robot> iterator = queue.iterator();
        while(iterator.hasNext())
        {
            Robot robot = iterator.next();
            output += robot.getIP() + ":" + robot.getPort();
            output += " - ";
        }
        return output;
    }

    /**
     * test
     * @throws InterruptedException
     */
    public static void test() throws InterruptedException
    {
        long offset = 5;
        //etc = eta + offset
        
        try {
        	long ETA = new Date().getTime();
        	Robot robot = new Robot(java.net.InetAddress.getByName("10.11.12.0"), 8888, new LaneSpecs(), ETA, ETA);
        	enqueue( robot );

        	ETA += 2;
        	robot = new Robot(java.net.InetAddress.getByName("10.11.12.1"), 8888, new LaneSpecs(), ETA, ETA);
        	enqueue( robot );

        	ETA += 10;
        	robot = new Robot(java.net.InetAddress.getByName("10.11.12.2"), 8888, new LaneSpecs(), ETA, ETA );
        	enqueue( robot );

        	ETA += 4;
        	robot = new Robot(java.net.InetAddress.getByName("10.11.12.3"), 8888, new LaneSpecs(), ETA, ETA );
        	enqueue( robot );

        	ETA += 5;
        	robot = new Robot(java.net.InetAddress.getByName("10.11.12.4"), 8888, new LaneSpecs(), ETA, ETA );
        	enqueue( robot );
        } catch(java.net.UnknownHostException e) {
        	e.printStackTrace();
        }

        System.out.println(queue);
    }

}



class RobotsComparator implements Comparator<Robot>
{
    @Override
    public int compare(Robot x, Robot y)
    {
        // Assume neither getETA() is null
        if (x.getETA() < y.getETA())
        {
            return -1;
        }
        if (x.getETA() > y.getETA())
        {
            return 1;
        }
        return 0;
    }
}