package pharoslabut.demo.mrpatrol.logAnalyzer;

/**
 * Contains a waypoint visitation time and the ID of the robot that visited the waypoint.
 * 
 * @author Chien-Liang Fok
 */
public class VisitationState implements Comparable<VisitationState> {

	private String robotName;
	private int robotID;
	private long arrivalTime;
	private long departTime;
	
	/**
	 * The constructor.
	 * 
	 * @param robotName The robot name.
	 * @param robotID The robot ID.
	 * @param arrivalTime The time when the robot arrives at this waypoint.
	 * @param departTime The time when the robot leaves the waypoint.
	 */
	public VisitationState(String robotName, int robotID, long arrivalTime, long departTime) {
		this.robotName = robotName;
		this.robotID = robotID;
		this.arrivalTime = arrivalTime;
		this.departTime = departTime;
	}
	
	public String getRobotName() {
		return robotName;
	}
	
	public int getRobotID() {
		return robotID;
	}
	
	public long getArrivalTime() {
		return arrivalTime;
	}
	
	public long getDepartureTime() {
		return departTime;
	}
	
	public long getWaitTime() {
		return getDepartureTime() - getArrivalTime();
	}
	
	public int compareTo(VisitationState vs) {
		if (arrivalTime == vs.getArrivalTime())
			return 0;
		else if (arrivalTime > vs.getArrivalTime()) 
			return 1;
		else
			return -1;
	}
	
	public String toString() {
		return getClass().getName() + ": " + robotName + "\t" + robotID + "\t" + arrivalTime + "\t" + departTime;
	}
}
