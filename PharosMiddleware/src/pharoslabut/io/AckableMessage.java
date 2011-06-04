package pharoslabut.io;

import java.net.*;

/**
 * A message that can be acknowledged.  This means it contains the reply address and
 * port.
 * 
 * @author Chien-Liang Fok
 *
 */
public interface AckableMessage extends Message {

	/**
	 * @return The reply address to which the acknowledgement should be sent.
	 */
	public InetAddress getReplyAddr();

	/**
	 * @return The port the acknowledgement should be sent to.
	 */
	public int getPort();
}
