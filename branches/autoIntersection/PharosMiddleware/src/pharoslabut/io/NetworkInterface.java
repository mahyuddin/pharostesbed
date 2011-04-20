package pharoslabut.io;

import java.net.InetAddress;
import java.util.*;

import pharoslabut.logger.FileLogger;

/**
 * A top-level class for the network interface.
 * 
 * @author Chien-Liang Fok
 */
public abstract class NetworkInterface {

	/**
	 * These are the registered message receivers.
	 */
	private Vector<MessageReceiver> rcvrs = new Vector<MessageReceiver>();
	
	/**
	 * The file logger for recording debug data.
	 */
	protected FileLogger flogger = null;
	
	/**
	 * Sends a message.
	 * 
	 * @param address The IP address of the destination.
	 * @param port The port on which the receiver is listening.
	 * @param m The message to send.
	 * @return true if the send was successful.
	 */
	public abstract boolean sendMessage(InetAddress address, int port, Message m);
	
	/**
	 * Stops the network interfaces.  Closes all sockets.
	 */
	public abstract void stop();
	
	/**
	 * Sets the file logger.
	 * 
	 * @param flogger the file logger.
	 */
	public void setLogger(FileLogger flogger) {
		this.flogger = flogger;
	}
	
	/**
	 * Registers a message receiver.
	 * 
	 * @param mr The message receiver.
	 */
	public void registerMsgListener(MessageReceiver mr) {
		rcvrs.add(mr);
	}
	
	/**
	 * Deregisters a message receiver.
	 * 
	 * @param mr The message receiver.
	 */
	public void deregisterMsgListener(MessageReceiver mr) {
		rcvrs.remove(mr);
	}
	
	/**
	 * Passes the new message to each registered message receiver.
	 * 
	 * @param m The new message.
	 */
	protected void newMessage(Message m) {
		Enumeration<MessageReceiver> e = rcvrs.elements();
		while (e.hasMoreElements()) {
			e.nextElement().newMessage(m);
		}
	}
}
