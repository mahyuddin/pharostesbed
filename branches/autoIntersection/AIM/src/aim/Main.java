package aim;

import java.util.Date;

/**
 *
 * @author Michael Hanna
 * 
 */
public class Main {

    public static final long startTime = new Date().getTime();

    /**
     * call the IntersectionManager and start running the code
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException  {
        RobotsPriorityQueue.test();
        new UDPServer();
 //       Thread IM = new IntersectionManager();
 //       IM.start();
    }

}
