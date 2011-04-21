package pharoslabut.io;

import java.io.*;
import java.net.*;

/**
 * The UDP Network Interface uses UDP to transmit and receive messages.
 * 
 * @author Chien-Liang Fok
 */
public class UDPNetworkInterface extends NetworkInterface {

	private DatagramSocket socket;
	private int port;
    private UDPReceiver rcvr;
    
    /**
     * The constructor.
     * 
     * @param port The port on which to listen for incoming messages.
     */
	public UDPNetworkInterface(int port) {
		this.port = port;
		openSocket();
		rcvr = new UDPReceiver();
	}
	
	@Override
	public void stop() {
		rcvr.stop();
	}
	
	@Override
	public boolean sendMessage(InetAddress address, int port, Message m) {
		
        if(m == null) {
        	log("ERROR: Attempted to send null message...", false);
        	return false;
        }
        
        if (socket == null) {
        	log("ERROR: while trying to send, the socket is null...", false);
        	return false;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        try {
        	ObjectOutputStream oos = new ObjectOutputStream(bos);
        	oos.writeObject(m);
        	oos.flush();
        	oos.close();
        	
        	byte[] sendByte = bos.toByteArray();
        	bos.close();
        	
        	DatagramPacket pkt = new DatagramPacket(sendByte, sendByte.length, address, port);
        	socket.send(pkt);
        	
        	log("Sent message " + m + " to " + address + ":" + port);
        } catch(IOException ioe) {
        	log("ERROR: while sending datagram packet, error: " + ioe.getMessage(), false);
        	ioe.printStackTrace();
        	return false;
        }
        
        return true;
	}
	
	/**
	 * Opens the server socket.  If an error occurs while opening the socket,
	 * it waits two seconds before trying again.
	 */
	private void openSocket() {
		while (socket == null) {
			log("Opening server socket...");
			try {
				socket = new DatagramSocket(port);
			} catch (SocketException e) {
				log("ERROR: Failed to open server socket, error: " + e.getMessage(), false);

				try {
					synchronized(this) {
						this.wait(2000); // pause 2 seconds
					}
				} catch(InterruptedException ie) {
					ie.printStackTrace();
				}
			}
		}
	}
	
	
	/**
	 * Listens for incoming messages.  Executes using its own thread.
	 */
	private class UDPReceiver implements Runnable {
		boolean running = true;
		
		public UDPReceiver() {
			new Thread(this).start();
		}
		
		/**
		 * Stops this UDPReceiver.
		 */
		public void stop() {
			running = false;
		}
		
		public void run() {
			
			while(running) {
				if (socket == null) 
					openSocket();
				if (socket != null) {
					try {
						byte[] receiveByte = new byte[256];
						DatagramPacket rpkt = new DatagramPacket(receiveByte, receiveByte.length);
						socket.receive(rpkt);
						
						//extract data from received packet and print it
						ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(rpkt.getData()));
			            try {
							Message m = (Message)ois.readObject();
							newMessage(m); // notify all listeners of message
						} catch (ClassNotFoundException e) {
							log("ERROR: while extracting object from datagram packet: " + e.getMessage(), false);
							e.printStackTrace();
						}
			            
					} catch(IOException ioe) {
						log("ERROR: while receiving packet: " + ioe.getMessage(), false);
						ioe.printStackTrace();
						socket.close();
						socket = null;
					}
				}
			}
			
			socket.close();
		}
	}
	
	private void log(String msg) {
		log(msg, true);
	}
	
	private void log(String msg, boolean isDebugMsg) {
		String result = "UDPNetworkInterface: " + msg;
		if (System.getProperty ("PharosMiddleware.debug") != null || !isDebugMsg)
			System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
}
