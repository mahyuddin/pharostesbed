package pharoslabut.io;

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
	 * @param msg the message to be sent.
	 * @return Whether the send was successful.
	 */
	public boolean sendMessage(Message msg);
	
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