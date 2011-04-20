package pharoslabut.demo.autoIntersection.msgs;

import java.net.InetAddress;


/**
 * This message is sent by the robot to the intersection server when it
 * exits the intersection.
 * 
 * @author Michael Hanna
 */
public class ExitingMsg extends AutoIntersectionMsg {
	
	private static final long serialVersionUID = -5699029124573209933L;

	public ExitingMsg(InetAddress robotIP, int robotPort) {
		super(robotIP, robotPort);
	}
	
	public String toString() {
		return "ExitingMsg";
	}

}
