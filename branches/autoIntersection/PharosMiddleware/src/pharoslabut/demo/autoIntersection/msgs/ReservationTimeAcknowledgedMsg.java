package pharoslabut.demo.autoIntersection.msgs;

import java.net.InetAddress;

import pharoslabut.io.Message.MsgType;

/**
 * What is this for?
 * 
 * @author Michael Hanna
 */
public class ReservationTimeAcknowledgedMsg extends AutoIntersectionMsg {

	private static final long serialVersionUID = 3490049826053353698L;

	public ReservationTimeAcknowledgedMsg(InetAddress robotIP, int robotPort) {
		super(robotIP, robotPort);
	}
	
	public String toString() {
		return "ReservationTimeAcknowledgedMsg";
	}
	
}
