package aim;

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
        new UDPServer();


        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new aim.GUI.MainWindow().setVisible(true);
            }
        });


 //       Thread IM = new IntersectionManager();
 //       IM.start();
    }

}
