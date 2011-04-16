package pharoslabut.demo.autoIntersection.server;

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


        Thread IM = new IntersectionManager(6665);
        Receive receive = new Receive(6665);

        receive.start();
        IM.start();


        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new pharoslabut.demo.autoIntersection.server.GUI.MainWindow().setVisible(true);
            }
        });    
    }

}
