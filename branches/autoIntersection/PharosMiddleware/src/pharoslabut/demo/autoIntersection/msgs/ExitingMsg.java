package pharoslabut.demo.autoIntersection.msgs;


/**
 * This message is sent by the robot to the intersection server when it
 * exits the intersection.
 * 
 * @author Michael Hanna
 */
public class ExitingMsg extends AutoIntersectionMsg {
	
	private static final long serialVersionUID = -5699029124573209933L;

	public ExitingMsg(int robotID) {
		super(robotID);
	}
	
	public String toString() {
		return "ExitingMsg";
	}

}
