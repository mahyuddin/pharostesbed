package pharoslabut.demo.simonsays;

import pharoslabut.io.Message;

/**
 * Sent by the DemoClient to the DemoServer to move the robot.
 * 
 * @author Chien-Liang Fok
 *
 */
public class RobotMoveMsg extends Message {
	private static final long serialVersionUID = 4462473889617403180L;

	/**
	 * The distance to move in meters.
	 */
	double dist;
	
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
	
	public String toString() {
		return "RobotMoveMsg, dist=" + dist;
	}
}
