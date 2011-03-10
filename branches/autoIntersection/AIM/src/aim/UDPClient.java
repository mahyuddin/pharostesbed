package aim;

import java.io.*;
import java.net.*;
import java.util.logging.*;

/**
 *
 * @author Michael Hanna
 *
 */
public class UDPClient extends Thread
{
    private final int PORT = 6665;
    private ObjectInputStream ois = null;
    private Robot robot = null;
    /**
     * <pre>
     *  - The client opens a new socket on port 6665
     *  - Then connect to the server ("localhost")
     *  - Receives a Robot object over the Socket
     *  - starts a thread
     * </pre>
     */
    public UDPClient()
    {
        try {
            // get a datagram socket                                  
            DatagramSocket socket = new DatagramSocket();

            // send request
            byte[] receiveByte = new byte[256];
            InetAddress address = InetAddress.getByName("localhost");
            DatagramPacket receivePacket = new DatagramPacket(receiveByte, receiveByte.length, address, PORT);
            socket.send(receivePacket);
            System.out.println("A send request is sent to the server at address:" + address + " and port:" + receivePacket.getPort());

            // get response
            receivePacket = new DatagramPacket(receiveByte, receiveByte.length);
            socket.receive(receivePacket);
            System.out.println("A receive request is received from the server at address:" + address + " and port:" + receivePacket.getPort());

            //extract data from received packet and print it
            ois = new ObjectInputStream(new ByteArrayInputStream(receivePacket.getData()));
            Object object = ois.readObject();
            robot = null;
            if( object instanceof Robot )
            {
                robot = (Robot) object;
            }
            System.out.println(robot);
//            new Thread(this).start();
            socket.close();
        }
        catch(IOException e) {
            System.err.print("Whoops! It didn't work!!!\n");
            e.printStackTrace();
        }
        catch(Exception e) {
            System.err.print("Whoops! It didn't work!!!\n");
            e.printStackTrace();
        }
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
                    oldQueue.enqueue(robot);
                    robot.setEnqueued(true);
                }
            }
            try {
                Thread.sleep(1000);                 // wait 1 sec and resend
            } catch (InterruptedException ex) {
                Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void main(String [] args) throws IOException
    {
        new UDPClient();
    }

}
