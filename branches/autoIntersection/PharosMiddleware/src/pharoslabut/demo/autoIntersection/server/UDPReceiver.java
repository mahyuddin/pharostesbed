package pharoslabut.demo.autoIntersection.server;

import java.io.*;
import java.net.*;
import java.util.Iterator;

import pharoslabut.demo.autoIntersection.msgs.*;
import pharoslabut.io.*;

/**
 * NOTE: THIS SHOULD NO LONGER BE USED!!!
 * @author Michael Hanna
 *
 */
public class UDPReceiver extends Thread
{

    private int port;
    private ObjectInputStream ois;
    private String ipAdd;
 

    public UDPReceiver(int port, String add)
    {
        this.port = port;
        this.ois = null;
        this.ipAdd = add;
    }

	/**
     * <pre>
     *  - The client opens a new socket on port
     *  - Then connect to the server ("localhost")
     *  - Receives an object over the Socket
     *  - starts a thread
     * </pre>
     * @return Object - returns the received object
     */
    public void run()
    {
    	while(true)
    	{
	        try {
	            // get a datagram socket
	            DatagramSocket socket = new DatagramSocket();
	        	
	            // send request
	            byte[] receiveByte = new byte[256];
	            InetAddress address = InetAddress.getByName(ipAdd);//InetAddress.getByName("localhost");
	            DatagramPacket receivePacket = new DatagramPacket(receiveByte, receiveByte.length, address, port);
	            socket.send(receivePacket);
	            System.out.println("RECEIVER: A send request is sent to the server at address:" + address + " and port:" + receivePacket.getPort());
	
	            // get response
	            receivePacket = new DatagramPacket(receiveByte, receiveByte.length);
	            socket.receive(receivePacket);
	            System.out.println("RECEIVER: A receive request is received from the server at address:" + address + " and port:" + receivePacket.getPort());
	
	            //extract data from received packet and print it
	            ois = new ObjectInputStream(new ByteArrayInputStream(receivePacket.getData()));
	            Object object = ois.readObject();
	            Message msg = (Message) object;
//	            processMessage(msg);
	            
	            System.out.println("RECEIVER: The folowing Message is received: \n" + msg);
	            socket.close();
	        }
	        catch(IOException e) {
	            System.err.print("RECEIVER: Whoops! It didn't work!!!\n");
	            e.printStackTrace();
	        }
	        catch(Exception e) {
	            System.err.print("RECEIVER: Whoops! It didn't work!!!\n");
	            e.printStackTrace();
	        }
    	}
    }
    
//    
//    private static void processMessage(Message msg)
//    {
//    	if (msg instanceof RequestAccessMsg)
//    		handleRequestAccessMsg( (RequestAccessMsg) msg );
//    	else if (msg instanceof ReservationTimeMsg)
//    		handleReservationTimeMsg( (ReservationTimeMsg) msg );
//    	else if (msg instanceof ReservationTimeAcknowledgedMsg)
//    		handleReservationTimeAcknowledgedMsg( (ReservationTimeAcknowledgedMsg) msg );
//    	else if (msg instanceof ExitingMsg)
//    		handleExitingMsg( (ExitingMsg) msg );
//    	else if (msg instanceof ExitingAcknowledgedMsg)
//    		handleExitingAcknowledgedMsg( (ExitingAcknowledgedMsg) msg );
//    	else
//    		System.out.println("RECEIVER: Unknown message " + msg);
//    }
//    
//    

}
