package aim;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Each robot is a client
 * @author ut
 */
public class Client extends Thread
{
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Socket socket;
    private Robot robot;
    /**
     * <pre>
     *  - The client opens a new socket on port 6665
     *  - Then connect to the server ("localhost")
     *  - Receives a Robot object over the Socket
     *  - starts a thread
     * </pew>
     */
    public Client()
    {
        oos = null;
        ois = null;
        socket = null;
        robot = null;

        try {
            // open a socket connection
            socket = new Socket("localhost", 6665);
            // open I/O streams for objects
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            // read an object from the server
            robot = (Robot) ois.readObject();
            System.out.println(robot);
            oos.close();
            ois.close();
        }
        catch(Exception e) {
            System.out.println(e);
            return;
        }
        this.start();
//        Thread robotThread = robot;
//        robotThread.start();
    }

    /**
     * This thread sends the robot data to the queue when approaching the intersection
     */
    public void run()
    {
        while(true)
        {
            if(! robot.isEnqueued() )
            {
                if( robot.getETA() < 5000000 )
                {
                    Queue.enqueue(robot);
                    robot.setEnqueued(true);
                }
            }
            try {
                Thread.sleep(1000);                 // wait 1 sec and resend
            } catch (InterruptedException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void main(String [] args)
    {
        new Client();
    }
}
