package pharoslabut.demo.autoIntersection.server;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * Implements a priority queue of vehicles waiting to enter the intersection.
 * 
 * @author Michael Hanna
 */
public class VehiclePriorityQueue {
	
    private static final int DEFAULT_CAPACITY = 10;
    private static Comparator<Vehicle> comparator = new RobotsComparator();
    private static PriorityQueue<Vehicle> queue = new PriorityQueue<Vehicle>(DEFAULT_CAPACITY, comparator);


    public static PriorityQueue<Vehicle> getQueue()
    {
        return queue;
    }

    public static int getDefaultCapacity()
    {
        return DEFAULT_CAPACITY;
    }


    public static PriorityQueue<Vehicle> getQueueCopy()
    {
        PriorityQueue<Vehicle> tempQueue = new PriorityQueue<Vehicle>(DEFAULT_CAPACITY, comparator);
        Iterator<Vehicle> iterator = queue.iterator();
        while(iterator. hasNext())
        {
            Vehicle robot = iterator.next();
            tempQueue.add(robot);
        }
        return tempQueue;
    }


    public static void dequeue()
    {
        queue.remove();
    }
    
    public static void dequeue(Vehicle robot)
    {
        queue.remove(robot);
    }

    public static void enqueue(Vehicle r)
    {
        queue.add(r);
    }
    
    public static boolean isEmpty()
    {
    	return queue.isEmpty();
    }
    
    public static Vehicle top()
    {
    	return queue.peek();
    }
    
    public static boolean contains(Vehicle robot) {
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
        Iterator<Vehicle> iterator = queue.iterator();
        while(iterator.hasNext())
        {
            Vehicle robot = iterator.next();
            output += "robotIP:" + robot.getIP().getHostAddress() + " robotPort:" + robot.getPort();
            output += " - ";
        }
        return output;
    }

//    /**
//     * test
//     * @throws InterruptedException
//     */
//    public static void test() throws InterruptedException
//    {
//        long offset = 5;
//        //etc = eta + offset
//        
//        try {
//        	long ETA = new Date().getTime();
//        	Vehicle robot = new Vehicle(java.net.InetAddress.getByName("10.11.12.0"), 8888, new LaneSpecs(), ETA, ETA);
//        	enqueue( robot );
//
//        	ETA += 2;
//        	robot = new Vehicle(java.net.InetAddress.getByName("10.11.12.1"), 8888, new LaneSpecs(), ETA, ETA);
//        	enqueue( robot );
//
//        	ETA += 10;
//        	robot = new Vehicle(java.net.InetAddress.getByName("10.11.12.2"), 8888, new LaneSpecs(), ETA, ETA );
//        	enqueue( robot );
//
//        	ETA += 4;
//        	robot = new Vehicle(java.net.InetAddress.getByName("10.11.12.3"), 8888, new LaneSpecs(), ETA, ETA );
//        	enqueue( robot );
//
//        	ETA += 5;
//        	robot = new Vehicle(java.net.InetAddress.getByName("10.11.12.4"), 8888, new LaneSpecs(), ETA, ETA );
//        	enqueue( robot );
//        } catch(java.net.UnknownHostException e) {
//        	e.printStackTrace();
//        }
//
//        System.out.println(queue);
//    }

}



class RobotsComparator implements Comparator<Vehicle>
{
    @Override
    public int compare(Vehicle x, Vehicle y)
    {
//        // Assume neither getETA() is null
//        if (x.getETA() < y.getETA())
//        {
//            return -1;
//        }
//        if (x.getETA() > y.getETA())
//        {
//            return 1;
//        }
        return 0;
    }
}