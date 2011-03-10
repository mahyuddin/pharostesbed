package aim;

import java.io.*;
import java.net.*;


/**
 *
 * @author Michael Hanna
 */
public class UDPServer extends Thread {

    private final int PORT = 6665;
    private DatagramSocket socket = null;
    private ByteArrayOutputStream bos = null;
    private ObjectOutputStream oos = null;

    /*
    public static void main(String [] args)
    {
        new UDPServer();
    }
     *
     */

    public UDPServer() {
        System.out.println("Starting the UDP connection.");

        try {
            socket = new DatagramSocket(PORT);
            new Thread(this).start(); 
        }
        catch (SocketException e) {
            System.err.print("Whoops! It didn't work!!!\n");
            e.printStackTrace();
            System.out.println("ERROR: Unable to create datagram socket, message: " + e.getMessage());
        }
    }

    public void run()
    {
        while(true)
        {
            try {
                /** receive request
                 * The receive method waits forever until a packet is received.
                 * If no packet is received, the server makes no further progress and just waits.
                 */
                System.out.println("Receiving a request from the client...");
                byte [] sendByte = new byte[256];
                DatagramPacket sendPacket = new DatagramPacket( sendByte, sendByte.length);
                socket.receive(sendPacket);
                System.out.println("A request is received from the client at address:" + sendPacket.getAddress() + " and port:" + sendPacket.getPort());


                /** send the response to the client at "address" and "port"
                 * if the queue was not empty, send robot to the client with same or new ETA/ETC
                 */
                bos = new ByteArrayOutputStream();
                oos = new ObjectOutputStream(bos);
//                Robot robot = new Robot(0, "laneSpecs", (long)1.2, (long)1.3, (float)1.4);
                Robot robot = IntersectionManager.manageIntersection();
                if( robot != null )
                {
                    oos.writeObject(robot);
                    oos.flush();
                    oos.close();
                    bos.close();
                    sendByte = bos.toByteArray();

                    InetAddress address = sendPacket.getAddress();
                    int port = sendPacket.getPort();
                    sendPacket = new DatagramPacket(sendByte, sendByte.length, address, port);
                    System.out.println("Sending the following packet to address:" + address + " and port " + port + ":\n" + robot);
                    socket.send(sendPacket);
                    System.out.println("Packet data sent!");
                }
            }
            catch (UnknownHostException e)
            {
                System.err.print("Whoops! It didn't work!!!\n");
                e.printStackTrace();
            }
            catch (IOException e) {
                System.err.print("Whoops! It didn't work!!!\n");
                e.printStackTrace();
            }
            catch (Exception e) {
                System.err.print("Whoops! It didn't work!!!\n");
                e.printStackTrace();
            }
        }
//        socket.close();
    }

}
