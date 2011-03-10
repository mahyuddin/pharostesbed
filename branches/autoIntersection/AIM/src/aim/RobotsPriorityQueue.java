package aim;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 *
 * @author Michael Hanna
 */
public class RobotsPriorityQueue
{
    private static final int DEFAULT_CAPACITY = 25;
    private static Comparator<Robot> comparator = new RobotsComparator();
    private static PriorityQueue<Robot> queue = new PriorityQueue<Robot>(DEFAULT_CAPACITY, comparator);

    public static PriorityQueue<Robot> getQueue()
    {
        return queue;
    }

    /**
     * test
     */
    public static void test() throws InterruptedException
    {
        Thread.sleep(1000);
        queue.add( new Robot(0, "laneSpecs", (long)12, (long)1.3, (float)1.4) );
        queue.add( new Robot(0, "laneSpecs", (long)11, (long)1.3, (float)1.4) );
        queue.add( new Robot(0, "laneSpecs", (long)10, (long)1.3, (float)1.4) );
        queue.add( new Robot(0, "laneSpecs", (long)19, (long)1.3, (float)1.4) );
        queue.add( new Robot(0, "laneSpecs", (long)8, (long)1.3, (float)1.4) );
        queue.add( new Robot(0, "laneSpecs", (long)12, (long)1.3, (float)1.4) );
        queue.add( new Robot(0, "laneSpecs", (long)7, (long)1.3, (float)1.4) );
        queue.add( new Robot(0, "laneSpecs", (long)22, (long)1.3, (float)1.4) );
        queue.add( new Robot(0, "laneSpecs", (long)6, (long)1.3, (float)1.4) );

        while(queue.size() != 0 )
        {
            System.out.println(queue.remove() );
        }
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

