package pharoslabut.demo.mrpatrol.logAnalyzer;

/**
 * Contains a waypoint visitation time and the ID of the robot that visited the waypoint.
 * 
 * @author Chien-Liang Fok
 */
public class VisitationState implements Comparable<VisitationState> {

	private String robotName;
	private int robotID;
	private long timestamp;
	
	public VisitationState(String robotName, int robotID, long timestamp) {
		this.robotName = robotName;
		this.robotID = robotID;
		this.timestamp = timestamp;
	}
	
	public String getRobotName() {
		return robotName;
	}
	
	public int getRobotID() {
		return robotID;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public int compareTo(VisitationState vs) {
		if (timestamp == vs.getTimestamp())
			return 0;
		else if (timestamp > vs.getTimestamp()) 
			return 1;
		else
			return -1;
	}
	
	public String toString() {
		return getClass().getName() + ": " + robotName + "\t" + robotID + "\t" + timestamp;
	}
}
