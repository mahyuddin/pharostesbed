package pharoslabut.demo.irobotcam;

import pharoslabut.io.Message;

/**
 * Sent by the DemoClient to the DemoServer to move the robot.
 * 
 * @author Chien-Liang Fok
 *
 */
public class RobotTurnMsg extends Message {

	private static final long serialVersionUID = -4516838753357794200L;

	/**
	 * The turn angle.
	 */
	double angle;
	
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
}
