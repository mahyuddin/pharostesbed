package pharoslabut.demo.autoIntersection.clientDaemons.V2IReservation;

import java.net.InetAddress;

import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionDetector;
import pharoslabut.navigate.LineFollower;

/**
 * Navigates across an intersection by communicating with a central server.  It implements the
 * simple reservation protocol where the server informs this vehicle of at what time it should enter 
 * the intersection.  This vehicle waits at the entrance to the intersection this time arrives,
 * and then crosses the intersection.
 * 
 * @author Chien-Liang Fok
 */
public class V2IReservationClientDaemon extends pharoslabut.demo.autoIntersection.clientDaemons.V2I.V2IClientDaemon {

	/**
	 * The constructor.
	 * 
	 * @param serverIP The IP address of the intersection management server.
	 * @param serverPort The port of the intersection management server.
	 * @param port The local port on which this client should listen.
	 * @param lineFollower The line follower.
	 * @param intersectionDetector The intersection detector.
	 * @param entryPointID The ID of the entry point.
	 * @param exitPointID The ID of the exit point.
	 */
	public V2IReservationClientDaemon(InetAddress serverIP, int serverPort,
			int port, LineFollower lineFollower,
			IntersectionDetector intersectionDetector, String entryPointID,
			String exitPointID) 
	{
		super(serverIP, serverPort, port, lineFollower, intersectionDetector,
				entryPointID, exitPointID);
	}
	
	
	

}
