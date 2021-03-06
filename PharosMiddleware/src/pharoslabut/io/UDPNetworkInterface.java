package pharoslabut.io;

import java.io.*;
import java.net.*;

import pharoslabut.logger.Logger;

/**
 * The UDP Network Interface uses UDP to transmit and receive messages.
 * 
 * @author Chien-Liang Fok
 */
public class UDPNetworkInterface extends NetworkInterface {

	private DatagramSocket socket;
    private UDPReceiver rcvr;
    
    /**
     * The port on which to listen for incoming message.  If -1, then select a random port that is available.
     */
    private int port = -1;
    
    /**
     * Creates a UDPNetworkInterface that listens on a random port.
     */
	public UDPNetworkInterface() {
		this(-1);
	}
	
	/**
     * Creates a UDPNetworkInterface that listens on a specific port.
     * 
     * @param port The port on which to listen.
     */
	public UDPNetworkInterface(int port) {
		this.port = port;
		openSocket();
		rcvr = new UDPReceiver();
	}
	
	@Override
	public int getLocalPort() {
		if (socket == null)
			openSocket();
		
		return socket.getLocalPort();
	}
	@Override
	public void stop() {
		rcvr.stop();
	}
	
	@Override
	public boolean sendMessage(InetAddress address, int port, Message m) {
		
        if(m == null) {
        	Logger.log("ERROR: message is null");
        	return false;
        }
        
        if (socket == null) {
        	Logger.log("ERROR: socket is null...");
        	return false;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        try {
        	ObjectOutputStream oos = new ObjectOutputStream(bos);
        	oos.writeObject(m);
        	oos.flush();
        	bos.flush();
        	oos.close();
        	bos.close();
        	
        	byte[] sendByte = bos.toByteArray();
        	
        	Logger.log("packet size= " + sendByte.length);
        	
        	DatagramPacket pkt = new DatagramPacket(sendByte, sendByte.length, address, port);
        	socket.send(pkt);
        	
        	Logger.log("Sent message " + m + " to " + address + ":" + port);
        } catch(IOException ioe) {
        	Logger.logErr("Problem while sending datagram packet, error: " + ioe.getMessage());
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
			Logger.log("Opening server socket...");
			try {
				if (port == -1)
					socket = new DatagramSocket();
				else
					socket = new DatagramSocket(port);
				Logger.log("Server socket listening on port " + getLocalPort() + ", sendBufferSize=" + socket.getSendBufferSize());
			} catch (SocketException e) {
				Logger.logErr("Failed to open server socket, error: " + e.getMessage());

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
							Logger.logErr("Problem while extracting object from datagram packet: " + e.getMessage());
							e.printStackTrace();
						}
			            
					} catch(IOException ioe) {
						Logger.logErr("Problem while receiving packet: " + ioe.getMessage());
						ioe.printStackTrace();
						socket.close();
						socket = null;
					}
				}
			}
			
			socket.close();
		}
	}
	
//	private void log(String msg) {
//		log(msg, true);
//	}
//	
//	private void log(String msg, boolean isDebugMsg) {
//		String result = "UDPNetworkInterface: " + msg;
//		if (System.getProperty ("PharosMiddleware.debug") != null || !isDebugMsg) {
//			System.out.println(result);
//			System.out.flush();
//		}
//		if (flogger != null)
//			flogger.log(result);
//	}
}
