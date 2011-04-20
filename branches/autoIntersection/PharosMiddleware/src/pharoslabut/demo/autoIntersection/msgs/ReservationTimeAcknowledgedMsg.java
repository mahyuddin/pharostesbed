package pharoslabut.demo.autoIntersection.msgs;

/**
 * What is this for?
 * 
 * @author Michael Hanna
 */
public class ReservationTimeAcknowledgedMsg extends AutoIntersectionMsg {

	private static final long serialVersionUID = 3490049826053353698L;

	public ReservationTimeAcknowledgedMsg(int robotID) {
		super(robotID);
	}
	
	public String toString() {
		return "ReservationTimeAcknowledgedMsg";
	}
	
}
