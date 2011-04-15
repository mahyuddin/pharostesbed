package pharoslabut.demo.autoIntersection.AIM.src.aim;

import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 *
 * @author Michael Hanna
 */
public class RobotsPriorityQueue
{
    private static final int DEFAULT_CAPACITY = 15;
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
        Iterator iterator = queue.iterator();
        while(iterator. hasNext())
        {
            Robot robot = (Robot) iterator.next();
            tempQueue.add(robot);
        }
        return tempQueue;
    }


    public static void dequeue()
    {
        queue.remove();
    }

    public static void enqueue(Robot r)
    {
        queue.add(r);
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
        Iterator iterator = queue.iterator();
        while(iterator.hasNext())
        {
            Robot robot = (Robot) iterator.next();
            output += robot.getID();
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

        long ETA = new Date().getTime() - Main.startTime;
        enqueue( new Robot(0, "laneSpecs", ETA, ETA+offset ) );

        ETA += 2;
        enqueue( new Robot(1, "laneSpecs", ETA, ETA+offset ) );

        ETA += 10;
        enqueue( new Robot(2, "laneSpecs", ETA, ETA+offset ) );

        ETA += 4;
        enqueue( new Robot(3, "laneSpecs", ETA, ETA+offset ) );

        ETA += 5;
        enqueue( new Robot(4, "laneSpecs", ETA, ETA+offset ) );

        System.out.println(queue);
        
 //       enqueue( new Robot(5, "laneSpecs", (long)12, (long)1.3, (float)1.4) );
 //       enqueue( new Robot(6, "laneSpecs", (long)7, (long)1.3, (float)1.4) );
 //       enqueue( new Robot(7, "laneSpecs", (long)22, (long)1.3, (float)1.4) );
 //       enqueue( new Robot(8, "laneSpecs", (long)6, (long)1.3, (float)1.4) );


        /*
        while(queue.size() != 0 )
        {
            System.out.println(queue.remove() );
        }
         *
         */
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