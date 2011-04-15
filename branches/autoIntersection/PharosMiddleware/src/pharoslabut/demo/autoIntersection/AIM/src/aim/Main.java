package pharoslabut.demo.autoIntersection.AIM.src.aim;

import java.util.Date;

/**
 *
 * @author Michael Hanna
 * 
 */
public class Main {

    /**
     * startTime is a constant containing the time when the run started
     */
    public static final long startTime = new Date().getTime();

    /**
     * call the IntersectionManager and start running the code
     * @param args the command line arguments
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException  {
        RobotsPriorityQueue.test();


    //    UDPClient client = new UDPClient(6665);
    //    client.start();

        Thread IM = new IntersectionManager(6665);
        Receive receieve = new Receive((IntersectionManager) IM, 6665);

        receieve.start();
        IM.start();

   //     UDPServer server = new UDPServer(6665);
   //     server.start();


        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new pharoslabut.demo.autoIntersection.AIM.src.aim.GUI.MainWindow().setVisible(true);
            }
        });
        
    }

}
