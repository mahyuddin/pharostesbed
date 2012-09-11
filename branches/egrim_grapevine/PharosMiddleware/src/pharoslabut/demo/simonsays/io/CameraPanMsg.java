package pharoslabut.demo.simonsays.io;

import java.net.InetAddress;

import pharoslabut.demo.simonsays.SimonSaysClient;
import pharoslabut.demo.simonsays.SimonSaysServer;
import pharoslabut.io.AckableMessage;

/**
 * This message is sent by the SimonSaysClient to the SimonSaysServer telling
 * it to pan the camera to a particular angle.
 * 
 * @author Chien-Liang Fok
 * @see SimonSaysClient
 * @see SimonSaysServer
 */
public class CameraPanMsg implements AckableMessage {
	private static final long serialVersionUID = -6339855504088553235L;
	private double panAngle;
	private InetAddress address;
	private int port;
	
	public CameraPanMsg(double panAngle) {
		this.panAngle = panAngle;
	}
	
	/**
	 * Returns the desired pan angle.
	 * 
	 * @return the pan angle in degrees.
	 */
	public double getPanAngle() {
		return panAngle;
	}
	
	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
	}

	@Override
	public void setReplyAddr(InetAddress address) {
		this.address = address;
	}

	@Override
	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public InetAddress getReplyAddr() {
		return address;
	}

	@Override
	public int getPort() {
		return port;
	}
	
	public String toString() {
		return "CameraPanMsg: panAngle=" + panAngle;
	}
}
