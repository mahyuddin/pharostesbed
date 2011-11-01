package pharoslabut.cps;

import java.net.InetAddress;

import pharoslabut.io.AckableMessage;

/**
 * Sent by the DemoClient to the DemoServer to move the robot.
 * 
 * @author Chien-Liang Fok
 */
public class RobotTurnMsg implements AckableMessage {

	private static final long serialVersionUID = -4516838753357794200L;

	/**
	 * The turn angle.
	 */
	private double angle;
	
	private InetAddress address;
	private int port;
	
	/**
	 * The constructor.
	 * 
	 * @param angle The angle to turn in degrees.  Negative is right, positive is left.
	 */
	public RobotTurnMsg(double angle) {
		this.angle = angle;
	}
	
	/**
	 * Returns the desired pan angle.
	 * 
	 * @return the pan angle.
	 */
	public double getAngle() {
		return angle;
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
		return "RobotTurnMsg, angle=" + angle;
	}
}
