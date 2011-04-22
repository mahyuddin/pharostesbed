package pharoslabut.demo.autoIntersection.msgs;

import java.net.InetAddress;

/**
 * What is this message used for?
 * 
 * @author Michael Hanna
 */
public class ExitingAcknowledgedMsg extends AutoIntersectionMsg {

	private static final long serialVersionUID = -5317453527956302211L;

	public ExitingAcknowledgedMsg(InetAddress robotIP, int robotPort) {
		super(robotIP, robotPort);
	}
	
	public String toString() {
		return "ExitingAcknowledgedMsg- " + "robotIP:" + this.getRobotIP().getHostAddress() + " - robotPort:" + this.getRobotPort();
	}

}
