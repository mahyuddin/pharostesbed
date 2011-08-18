package pharoslabut.logger.analyzer.motion;

/**
 * A temporal divergence measurement.
 *
 * @author Chien-Liang Fok
 */
public class TemporalDivergence {
	
	/**
	 * The waypoint sequence number, starting from zero.
	 */
	private int waypointNumber;
	
	/**
	 * The ideal time at which the robot should have arrived at the waypoint.
	 * This is based on when the robot first started to move.
	 */
	private long idealTOA;
	
	/**
	 * The ideal time that the robot should have reached the waypoint relative to
	 * when it left the previous waypoint.
	 */
	private long idealRelTOA;
	
	/**
	 * The actual time when the robot reached the waypoint.
	 */
	private long actualTOA;
	
	/**
	 * The constructor.
	 * 
	 * @param waypointNumber The waypoint sequence number, starting from zero.
	 * @param idealTOA The ideal time when the robot should arrive at the waypoint.
	 * This is based on when the robot first started to move.
	 * @param idealRelTOA The ideal time that the robot should have reached the waypoint relative to
	 * when it left the previous waypoint.
	 * @param actualTOA The actual time when the robot reached the waypoint.
	 */
	public TemporalDivergence(int waypointNumber, long idealTOA, long idealRelTOA, long actualTOA) {
		this.waypointNumber = waypointNumber;
		this.idealTOA = idealTOA;
		this.idealRelTOA = idealRelTOA;
		this.actualTOA = actualTOA;
	}
	
	public int getWaypointNumber() {
		return waypointNumber;
	}
	
	public long getIdealTOA() {
		return idealTOA;
	}
	
	public long getIdealRelTOA() {
		return idealRelTOA;
	}
	
	public long getActualTOA() {
		return actualTOA;
	}
	
	/**
	 * 
	 * @return The temporal divergence in milliseconds.
	 */
	public long getLateness(boolean absolute) {
		long result = actualTOA - idealTOA;
		if (absolute)
			return Math.abs(result);
		else
			return result;
	}
	
	/**
	 * 
	 * @return The relative divergence in milliseconds.
	 */
	public long getRelLateness(boolean absolute) {
		long result = actualTOA - idealRelTOA;
		if (absolute)
			return Math.abs(result);
		else
			return result;
	}
	
	public String toString() {
		return "TemporalDivergence: waypointNumber=" + waypointNumber + "idealTOA=" + idealTOA + ", idealRelTOA=" + idealRelTOA 
		+ ", actualTOA=" + actualTOA + ", lateness=" + getLateness(false)
		+ ", relative lateness=" + getRelLateness(false);
	}
}