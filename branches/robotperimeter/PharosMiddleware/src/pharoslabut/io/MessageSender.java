package pharoslabut.io;

import java.net.*;
import pharoslabut.exceptions.*;

/**
 * Defines the interface of all MessageSenders.
 *
 * @author Chien-Liang Fok
 * @version 3/17/2003
 */
public interface MessageSender {
	
	public static enum ProtocolType {TCP, UDP};
	
	/**
	 * Sends a message.
	 *
	 * @param address The destination address.
	 * @param port The destination port.
	 * @param msg The message to be sent.
	 * @return Whether the send was successful.
	 * @throws PharosException Whenever a problem occurs during message transmission.
	 */
	public boolean sendMessage(InetAddress address, int port, Message msg) throws PharosException;
	
	/**
	 * Returns the singlecast protocol type of this MessageSender.
	 *
	 * @return the singlecast protocol type of this MessageSender.
	 */
	public ProtocolType getProtocolType();
	
	/**
	 * Forces the MessageSender to close all sockets and stop
	 * functioning.
	 */
	public void kill();
}
