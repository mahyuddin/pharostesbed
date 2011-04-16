package pharoslabut.navigate;

/**
 * This is implemented by any component that wants to know when the 
 * robot is done navigating the waypoints within a motion script.
 * 
 * @author Chien-Liang Fok
 */
public interface WayPointFollowerDoneListener {
	
	/**
	 * This is called when the WayPointFoller finishes execution.
	 * 
	 * @param success True if it successfully finished following the waypoints.
	 * @param finalWayPoint The final waypoint that the robot reached.  
	 * If successful, this should be equal to the number of waypoints in the motion script.
	 */
	public void wayPointFollowerDone(boolean success, int finalWayPoint);
}
