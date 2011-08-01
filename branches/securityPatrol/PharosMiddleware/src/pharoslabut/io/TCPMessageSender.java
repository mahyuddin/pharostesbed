package pharoslabut.io;

import java.io.*;
import java.net.*;
import pharoslabut.logger.FileLogger;
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
    public static final int MAX_RETRIES = 5;
    
    /**
     * The amount of time to wait before retransmitting a message.
     */
    public static final long RETRY_DELAY_MS = 1000;
    
    /**
     * The file logger for debugging purposes.
     */
	private FileLogger flogger = null;
	
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
     * Sets the file logger.
     */
    public void setFileLogger(FileLogger flogger) {
    	this.flogger = flogger;
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
     * @param msg the message to be sent.
     * @throws PharosException whenever an error occurs.
     */
    public void sendMessage(InetAddress address, int port, Message msg) throws PharosException {
    	int numTries = 0;
    	boolean success = false;
    	String errMsg = null;
    	
    	while (!success && numTries++ < MAX_RETRIES) {
    		log("sendMessage: Sending message, attempt " + numTries + " of " + MAX_RETRIES + "..." 
    				+ "\tDestination: " + address  + ":" + port
    				+ "\tMessage to send: " + msg);
    		    		
    		Socket socket = null;
    		
    		try {
    			log("sendMessage: Opening connection to " + address + ":" + port);
    			socket = new Socket(address, port);
    			socket.setTcpNoDelay(true);
    			log("sendMessage: Connection established...");
    		} catch(IOException ioe) {
    			ioe.printStackTrace();
    			errMsg = ioe.getMessage();
    			logErr("sendMessage: IOException when creating connection to " + address + ":" + port + ", error message: " + errMsg);
    			continue;
    		}
    		
    		if (socket != null) {
    			try {
    				OutputStream os = socket.getOutputStream();
    				ObjectOutputStream oos = new ObjectOutputStream(os);
    				
    				log("sendMessage: Sending " + msg + "...");
    				oos.writeObject(msg);
    				oos.flush();
    				os.flush();
    			} catch(IOException ioe) {
    				ioe.printStackTrace();
        			errMsg = ioe.getMessage();
        			logErr("sendMessage: IOException when sending, error message: " + errMsg);
        			try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace(); // Just ignore this error since a fault has already occurred.
					}
        			continue;
    			}
    			
    			if (msg instanceof AckedMsg) {
    				log("sendMessage: Message is an AckedMsg, waiting for ack...");
    				InputStream is;
					try {
						is = socket.getInputStream();
						ObjectInputStream ois = new ObjectInputStream(is);
						log("sendMessage: Waiting for ack...");
	    				Object ack = ois.readObject();
	    				if (ack instanceof PharosAckMsg) {
	    					log("sendMessage: ack received!");
	    					success = true;
	    				}
					} catch (IOException e) {
						errMsg = e.getMessage();
						logErr("sendMessage: Got IOException while waiting for ack.");
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						errMsg = e.getMessage();
						logErr("sendMessage: Got ClassNotFoundException while waiting for ack.");
						e.printStackTrace();
					}
    			} else {
    				log("sendMessage: Message was not an AckedMsg, assuming transmission was successful.");
    			}

    			try {
    				log("sendMessage: Closing the socket to the destination host...");
					socket.close();
				} catch (IOException e) {
					logErr("sendMessage: Got IOException while closing socket.");
					e.printStackTrace();
				}
    		}
    		
    		if (!success && numTries < MAX_RETRIES) {
    			log("sendMessage: Transmission unsuccessful but max retries not reached, pausing for " + RETRY_DELAY_MS + " then retrying.");
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
    		logErr("sendMessage: Send Failed!");
    		throw new PharosException(errMsg);
    	} else {
    		log("sendMessage: Send success!");
    	}
    }
	
    private void logErr(String msg) {
    	String result = "TCPMessageSender: ERROR: " + msg;
    	System.err.println(result);
    	if (flogger != null)
    		flogger.log(result);
    }

	private void log(String msg) {
		String result = "TCPMessageSender: " + msg;
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
}
