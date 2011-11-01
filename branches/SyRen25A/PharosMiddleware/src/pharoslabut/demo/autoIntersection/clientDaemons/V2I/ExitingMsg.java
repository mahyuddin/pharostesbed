package pharoslabut.demo.autoIntersection.clientDaemons.V2I;

import java.net.InetAddress;

import pharoslabut.demo.autoIntersection.msgs.AutoIntersectionMsg;


/**
 * This message is sent by the robot to the intersection server when it
 * exits the intersection.
 * 
 * @author Chien-Liang Fok
 * @author Michael Hanna
 */
public class ExitingMsg extends AutoIntersectionMsg {
	
	private static final long serialVersionUID = -5699029124573209933L;

	/**
	 * The constructor.
	 * 
	 * @param robotIP The IP address of the robot.
	 * @param robotPort The port that the robot is listening to.
	 */
	public ExitingMsg(InetAddress robotIP, int robotPort) {
		super(robotIP, robotPort);
	}
	
	public String toString() {
		return getClass().getName() + ": " + super.toString();
	}

}
