package pharoslabut.demo.autoIntersection.server;

import pharoslabut.demo.autoIntersection.intersectionSpecs.IntersectionSpecs;

/**
 * Implements a reservation-based intersection management server.
 * When a vehicle request access to the intersection, it computes the earliest the
 * the robot can enter the intersection and reserves this time for the vehicle.
 * It informs the vehicle of this time and expects that the vehicle not enter the intersection
 * until the time is reached.
 * 
 * @author Chien-Liang Fok
 */
public class ReservationDaemon extends ParallelDaemon {

	/**
	 * The constructor.
	 * 
	 * @param intersectionSpecs The intersection specifications.
	 * @param serverPort The port on which to listen.
	 */
	public ReservationDaemon(IntersectionSpecs intersectionSpecs, int serverPort) {
		super(intersectionSpecs, serverPort);
		
	}

}
