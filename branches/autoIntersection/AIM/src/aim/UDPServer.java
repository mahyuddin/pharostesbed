package aim;

import java.io.*;
import java.net.*;


/**
 *
 * @author Michael Hanna
 */
public class UDPServer extends Thread {

//    private int serverPort = 6665;
    private int serverPort;
    private DatagramSocket socket;
    private ByteArrayOutputStream bos;
    private ObjectOutputStream oos;

    /*
    public static void main(String [] args)
    {
        new UDPServer();
    }
     *
     */

    public UDPServer(int serverPort) {
        System.out.println("Starting the UDP connection.");
        this.socket = null;
        this.serverPort = serverPort;
        this.bos = null;
        this.oos = null;
        
        try {
            this.socket = new DatagramSocket(this.serverPort);
        }
        catch (SocketException e) {
            System.err.print("SERVER: Whoops! It didn't work!!!\n");
            e.printStackTrace();
            System.out.println("SERVER: ERROR: Unable to create datagram socket, message: " + e.getMessage());
        }       
    }
    
    public void send(Object message)
    {
        try {
            /** receive request
             * The receive method waits forever until a packet is received.
             * If no packet is received, the server makes no further progress and just waits.
             */
            System.out.println("SERVER: Receiving a request from the client...");
            byte [] sendByte = new byte[256];
            DatagramPacket sendPacket = new DatagramPacket( sendByte, sendByte.length);
            socket.receive(sendPacket);
            System.out.println("SERVER: A request is received from the client at address: " + sendPacket.getAddress() + " and port: " + sendPacket.getPort());


            /** send the response to the client at "address" and "port"
             * if the queue was not empty, send an object to the client with same or new ETA/ETC
             */
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
    //                Robot robot = new Robot(0, "laneSpecs", (long)1.2, (long)1.3, (float)1.4);
    //              Robot robot = IntersectionManager.getRobotsCompleted().peekFirst();
            if( message != null )
            {
                oos.writeObject(message);
                oos.flush();
                oos.close();
                bos.close();
                sendByte = bos.toByteArray();

                InetAddress address = sendPacket.getAddress();
                int clientPort = sendPacket.getPort();
                sendPacket = new DatagramPacket(sendByte, sendByte.length, address, clientPort);
                System.out.println("Sending the following packet to address:" + address + " and port " + clientPort + ":\n" + message);
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


}
