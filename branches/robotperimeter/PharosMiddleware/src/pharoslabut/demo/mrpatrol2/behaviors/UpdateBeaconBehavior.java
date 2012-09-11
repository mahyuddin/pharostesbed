package pharoslabut.demo.mrpatrol2.behaviors;

/**
 * Defines a shared interface that all behaviors that transmit beacons must
 * implement.
 * 
 * @author Chien-Liang Fok
 */
public interface UpdateBeaconBehavior {

	/**
	 * Sets the number of waypoints traversed within the beacon.
	 * 
	 * @param numWaypointsTraversed the number of waypoints traversed.
	 */
	public void setWaypointsTraversed(int numWaypointsTraversed);
}
