package pharoslabut.demo.simonsays.io;

import java.net.InetAddress;

import pharoslabut.io.AckableMessage;

/**
 * Sent by the DemoClient to the DemoServer to move the robot.
 * 
 * @author Chien-Liang Fok
 *
 */
public class RobotMoveMsg implements AckableMessage {
	private static final long serialVersionUID = 4462473889617403180L;

	/**
	 * The distance to move in meters.
	 */
	private double dist;
	
	private InetAddress address;
	private int port;
	
	/**
	 * The constructor.
	 * 
	 * @param dist The distance in meters. Positive is forward, negative is backwards.
	 */
	public RobotMoveMsg(double dist) {
		this.dist = dist;
	}
	
	/**
	 * Returns the desired pan angle.
	 * 
	 * @return the pan angle.
	 */
	public double getDist() {
		return dist;
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
		return "RobotMoveMsg, dist=" + dist;
	}
}
