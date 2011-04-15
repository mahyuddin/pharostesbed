package pharoslabut.demo.autoIntersection.AIM.src.aim;

import java.io.*;
import java.net.*;

/**
 *
 * @author Michael Hanna
 *
 */
public class UDPClient extends Thread
{
//    private int port = 6665;
    private int port;
    private ObjectInputStream ois;

    public UDPClient(int port)
    {
        this.port = port;
        this.ois = null;
    }

    /**
     * <pre>
     *  - The client opens a new socket on port 6665
     *  - Then connect to the server ("localhost")
     *  - Receives an object over the Socket
     *  - starts a thread
     * </pre>
     * @return Object - returns the received object
     */
    public Object receive()
    {
        try {
            // get a datagram socket
            DatagramSocket socket = new DatagramSocket();

            // send request
            byte[] receiveByte = new byte[256];
            InetAddress address = InetAddress.getByName("localhost");
            DatagramPacket receivePacket = new DatagramPacket(receiveByte, receiveByte.length, address, port);
            socket.send(receivePacket);
            System.out.println("CLIENT: A send request is sent to the server at address:" + address + " and port:" + receivePacket.getPort());

            // get response
            receivePacket = new DatagramPacket(receiveByte, receiveByte.length);
            socket.receive(receivePacket);
            System.out.println("CLIENT: A receive request is received from the server at address:" + address + " and port:" + receivePacket.getPort());

            //extract data from received packet and print it
            ois = new ObjectInputStream(new ByteArrayInputStream(receivePacket.getData()));
            Object object = ois.readObject();
            socket.close();
            return object;
        }
        catch(IOException e) {
            System.err.print("CLIENT: Whoops! It didn't work!!!\n");
            e.printStackTrace();
        }
        catch(Exception e) {
            System.err.print("CLIENT: Whoops! It didn't work!!!\n");
            e.printStackTrace();
        }
        return null;
//        Thread robotThread = robot;
//        robotThread.start();
    }

}
