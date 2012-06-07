package pharoslabut.io;

import java.io.*;
import java.net.*;
import pharoslabut.logger.Logger;
import pharoslabut.exceptions.*;

/**
 * Sends messages to using TCP.
 *
 * @author Chien-Liang Fok
 * @version 08/01/2011
 */
public class TCPMessageSender implements MessageSender {
	
	/**
	 * This is the maximum number of times this class will try to retransmit a message
	 * before giving up.
	 */
    public static final int MAX_RETRIES = 0;
    
    /**
     * The amount of time to wait before retransmitting a message.
     */
    public static final long RETRY_DELAY_MS = 1000;
	
	/**
	 * This is a singleton class and this is the reference to the single instance
	 * of this class.
	 */
	private static TCPMessageSender tcpMsgSndr = new TCPMessageSender();
	
    /**
     * Creates a TCPMessageSender.
     */
    private TCPMessageSender() {}
    
    /**
     * Provides access to the TCPMessageSender singleton.
     * 
     * @return The TCPMessageSender.
     */
    public static TCPMessageSender getSender() {
    	return tcpMsgSndr;
    }
    
    /**
     * Forces the TCPMessageSender to close all sockets and stop
     * functioning.
     */
    public void kill() {
    }

    @Override
    public ProtocolType getProtocolType() {
        return ProtocolType.TCP;
    }
    
    /**
     * Sends a message via a TCP socket.
     *
	 * @param address The destination address.
	 * @param port The destination port.
	 * @param msg The message to be sent.
	 * @return Whether the send was successful.
	 * @throws PharosException Whenever a problem occurs during message transmission.
     */
    public boolean sendMessage(InetAddress address, int port, Message msg) throws PharosException {
    	MsgSender sender = new MsgSender(address, port, msg);
    	return sender.doSend();
    }
	
	private class MsgSender {
		InetAddress address;
		int port;
		Message msg;
		
		public MsgSender(InetAddress address, int port, Message msg) {
			this.address = address;
			this.port = port;
			this.msg = msg;
		}
		
		public boolean doSend() {
		  	int numTries = 0;
	    	boolean success = false;
	    	String errMsg = null;
	    	
	    	while (!success && numTries++ <= MAX_RETRIES) {
	    		Logger.log("Sending message, attempt " + numTries + " of " + MAX_RETRIES + "..." 
	    				+ "\n\tDestination: " + address  + ":" + port
	    				+ "\n\tMessage to send: " + msg);
	    		    		
	    		Socket socket = null;
	    		
	    		try {
	    			Logger.log("Opening connection to " + address + ":" + port);
	    			socket = new Socket(address, port);
	    			socket.setTcpNoDelay(true);
	    			Logger.log("Connection established...");
	    		} catch(IOException ioe) {
	    			ioe.printStackTrace();
	    			errMsg = ioe.getMessage();
	    			Logger.logErr("IOException when creating connection to " + address + ":" + port + ", error message: " + errMsg);
	    			continue;
	    		}
	    		
	    		if (socket != null) {
	    			try {
	    				OutputStream os = socket.getOutputStream();
	    				ObjectOutputStream oos = new ObjectOutputStream(os);
	    				
	    				Logger.log("Sending " + msg + "...");
	    				oos.writeObject(msg);
	    				oos.flush();
	    				os.flush();
	    				Logger.log("Message was sent.");
	    			} catch(IOException ioe) {
	    				ioe.printStackTrace();
	        			errMsg = ioe.getMessage();
	        			Logger.logErr("IOException when sending, error message: " + errMsg);
	        			try {
							socket.close();
						} catch (IOException e) {
							e.printStackTrace(); // Just ignore this error since a fault has already occurred.
						}
	        			continue;
	    			}
	    			
	    			if (msg instanceof AckedMsg) {
	    				Logger.log("Message is an AckedMsg, waiting for ack...");
	    				InputStream is;
						try {
							is = socket.getInputStream();
							ObjectInputStream ois = new ObjectInputStream(is);
							Logger.log("Waiting for ack...");
		    				Object ack = ois.readObject();
		    				if (ack instanceof PharosAckMsg) {
		    					Logger.log("ack received!");
		    					success = true;
		    				} else
		    					Logger.logErr("Received object not a PharosAckMsg");
						} catch (IOException e) {
							errMsg = e.getMessage();
							Logger.logErr("Got IOException while waiting for ack.");
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							errMsg = e.getMessage();
							Logger.logErr("Got ClassNotFoundException while waiting for ack.");
							e.printStackTrace();
						}
	    			} else {
	    				Logger.log("Message was not an AckedMsg, assuming transmission was successful.");
	    				success = true;
	    			}

	    			try {
	    				Logger.log("Closing the socket to the destination host...");
	    				socket.shutdownOutput();
	    				socket.shutdownInput();
						socket.close();
					} catch (IOException e) {
						Logger.logErr("Got IOException while closing socket.");
						e.printStackTrace();
					}
	    		}
	    		
	    		if (!success && numTries < MAX_RETRIES) {
	    			Logger.log("Transmission unsuccessful but max retries not reached, pausing for " + RETRY_DELAY_MS + " then retrying.");
	    			synchronized(this) {
	    				try {
							this.wait(RETRY_DELAY_MS);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
	    			}
	    		}
	    	}
	    	
	    	if (!success) {
	    		Logger.logErr("Send Failed!");
	    		return false;
	    	} else {
	    		Logger.log("Send success!");
	    		return success;
	    	}
		}
	}
}
