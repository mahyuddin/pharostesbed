package aim;

/**
 * The reservation queue. It is a queue of robots
 * @author ut
 */
public class Queue {

    private static final int DEFAULT_CAPACITY = 25;
    private static Robot[] RobotsArray;
    private static int queueSize;
    private static int front;
    private static int back;


    /*
    public Queue() {
        RobotsArray = new Robot[DEFAULT_CAPACITY];
        makeEmpty();
    }
     *
     */

    /**
     * Test if the queue is empty.
     * @return true if empty, false otherwise.
     */
    public static boolean isEmpty() {
        return queueSize == 0;
    }

    /**
     * Make the queue empty.
     */
    public static void makeEmpty()
    {
        RobotsArray = new Robot[DEFAULT_CAPACITY];
        queueSize = 0;
        front = 0;
        back = -1;
    }

    /**
     * Return and remove the least recently inserted item
     * from the queue.
     * @return the least recently inserted item in the queue.
     * @throws UnderflowException if the queue is empty.
     */
    public static Robot dequeue( )
    {
        if( isEmpty( ) ) {
            System.out.println("Cannot dequeue.. the queue is empty");
//            throw UnderflowException( "ArrayQueue dequeue" );
        }
        queueSize--;
        Robot returnedRobot = RobotsArray[front];
        front = increment(front);
        return returnedRobot;
    }

    /**
     * Get the least recently inserted item in the queue.
     * Does not alter the queue.
     * @return the least recently inserted item in the queue.
     * @throws UnderflowException if the queue is empty.
     */
    public static Robot getFront( )
    {
        if( isEmpty( ) ) {
//            throw new UnderflowException( "ArrayQueue getFront" );
            System.out.println("Cannot getFront.. the queue is empty");
        }
        return RobotsArray[ front ];
    }

    /**
     * Insert a new item into the queue.
     * @param r the item to insert.
     */
    public static void enqueue( Robot r )
    {
        if( queueSize == RobotsArray.length ) {
            doubleQueue( );
        }
        back = increment(back);
        RobotsArray[back] = r;
        queueSize++;
    }

    /**
     * Internal method to increment with wraparound.
     * @param x any index in theArray's range.
     * @return x+1, or 0 if x is at the end of theArray.
     */
    private static int increment( int x )
    {
        if( ++x == RobotsArray.length )
            x = 0;
        return x;
    }

    /**
     * Internal method to expand theArray.
     */
    private static void doubleQueue( )
    {
        Robot [] newRobotsArray;
        newRobotsArray = new Robot[ RobotsArray.length + DEFAULT_CAPACITY ];

        // Copy elements that are logically in the queue
        for( int i = 0; i < queueSize; i++, front = increment(front) ) {
            newRobotsArray[i] = RobotsArray[front];
        }

        RobotsArray = newRobotsArray;
        front = 0;
        back = queueSize - 1;
    }


    /**
     * Exception class for access in empty containers
     * such as stacks, queues, and priority queues.
     */
    public class UnderflowException extends RuntimeException {
        /**
         * Construct this exception object.
         * @param message the error message.
         */
        public UnderflowException( String message ) {
            super( message );
        }
    }

}
