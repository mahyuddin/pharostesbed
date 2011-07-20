package pharoslabut.io;

import java.io.*;
import java.net.*;
import pharoslabut.logger.FileLogger;
import pharoslabut.exceptions.*;

/**
 * Sends messages to using TCP.
 *
 * @author Chien-Liang Fok
 * @version 3/17/2003
 */
public class TCPMessageSender implements MessageSender {
    
	private FileLogger flogger = null;
	
	private static TCPMessageSender tcpMsgSndr = new TCPMessageSender();
	
    /**
     * Creates a TCPMessageSender.
     */
    private TCPMessageSender() {
    }
    
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
    	
    	try {
    		log("Opening TCP socket to " + address + ":" + port);
    		Socket socket = new Socket(address, port);
    		socket.setTcpNoDelay(true);

    		OutputStream os = socket.getOutputStream();
    		ObjectOutputStream oos = new ObjectOutputStream(os);
    		
    		log("Sending the object "+ msg.getType()+" to the destination.");
    		oos.writeObject(msg);
    		oos.flush();
    		os.flush();

    		log("Closing the socket to the destination host.");
    		socket.close();

    	} catch(Exception e) {
    		e.printStackTrace();
    		throw new PharosException(e.getMessage());
    	}
    }
	
	void log(String msg) {
		String result = "TCPMessageSender: " + msg;
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
}
