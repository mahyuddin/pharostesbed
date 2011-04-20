package pharoslabut.demo.autoIntersection.msgs;

/**
 * What is this message used for?
 * 
 * @author Michael Hanna
 */
public class ExitingAcknowledgedMsg extends AutoIntersectionMsg {

	private static final long serialVersionUID = -5317453527956302211L;

	public ExitingAcknowledgedMsg(int robotID) {
		super(robotID);
	}
	
	public String toString() {
		return "ExitingAcknowledgedMsg";
	}

}
