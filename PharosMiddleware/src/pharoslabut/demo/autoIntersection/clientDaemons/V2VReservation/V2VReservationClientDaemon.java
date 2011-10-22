package pharoslabut.demo.autoIntersection.clientDaemons.V2VReservation;

import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionDetector;
import pharoslabut.navigate.LineFollower;

/**
 * Implements a V2V form of intersection management where each robot decides
 * independently whether it is safe to traverse the intersection.  In this daemon,
 * multiple robots may cross the intersection simultaneously.  The vehicles
 * form reservations of times in the future when they can cross.
 * 
 * @author Chien-Liang Fok
 */
public class V2VReservationClientDaemon extends pharoslabut.demo.autoIntersection.clientDaemons.V2VParallel.V2VParallelClientDaemon {

	public V2VReservationClientDaemon(LineFollower lineFollower,
			IntersectionDetector intersectionDetector, String entryPointID,
			String exitPointID) 
	{
		super(lineFollower, intersectionDetector, entryPointID, exitPointID);
		
	}

}
