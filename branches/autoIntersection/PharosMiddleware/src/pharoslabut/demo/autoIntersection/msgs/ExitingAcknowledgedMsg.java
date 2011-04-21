package pharoslabut.demo.autoIntersection.msgs;

import java.net.InetAddress;

import pharoslabut.io.Message.MsgType;

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
		return "ExitingAcknowledgedMsg";
	}

}
