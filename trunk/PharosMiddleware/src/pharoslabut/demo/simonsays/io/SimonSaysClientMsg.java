package pharoslabut.demo.simonsays.io;

import java.net.InetAddress;

import pharoslabut.io.AckedMsg;

/**
 * The top-level class of SimonSays Messages that are sent from the 
 * client to the server.
 * 
 * @author Chien-Liang Fok
 */
public abstract class SimonSaysClientMsg implements AckedMsg {

	private static final long serialVersionUID = 5793902362164319521L;

	private InetAddress replyAddress;
	private int replyPort;
	
	/**
	 * The constructor.
	 * 
	 * @param replyAddress The IP address of the client.
	 * @param replyPort The port of the client.
	 */
	public SimonSaysClientMsg(InetAddress replyAddress, int replyPort) {
		this.replyAddress = replyAddress;
		this.replyPort = replyPort;
	}
	
	public InetAddress getReplyAddr() {
		return replyAddress;
	}

	public int getReplyPort() {
		return replyPort;
	}
	
	public String toString() {
		return replyAddress + ":" + replyPort;
	}
}
