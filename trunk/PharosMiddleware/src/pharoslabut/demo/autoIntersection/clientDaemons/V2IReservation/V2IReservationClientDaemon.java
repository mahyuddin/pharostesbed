package pharoslabut.demo.autoIntersection.clientDaemons.V2IReservation;

import java.net.InetAddress;

import pharoslabut.demo.autoIntersection.clientDaemons.V2I.GrantAccessMsg;
import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionDetector;
import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionEventType;
import pharoslabut.exceptions.PharosException;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.LineFollower;
import pharoslabut.sensors.Position2DBuffer;
import pharoslabut.sensors.Position2DListener;
import playerclient3.structures.position2d.PlayerPosition2dData;

/**
 * Navigates across an intersection by communicating with a central server.  It implements the
 * simple reservation protocol where the server informs this vehicle of at what time it should enter 
 * the intersection.  This vehicle waits at the entrance to the intersection this time arrives,
 * and then crosses the intersection.
 * 
 * @author Chien-Liang Fok
 */
public class V2IReservationClientDaemon 
	extends pharoslabut.demo.autoIntersection.clientDaemons.V2I.V2IClientDaemon
	implements Position2DListener
{
	/**
	 * The distance in meters between the approach and entrance points of the intersection.
	 */
	public static final double DISTANCE_TO_ENTRANCE = 0.36; // To do, make this determined on-line at approaching marker.

	/**
	 * The amount of time in milliseconds it will take the local vehicle to
	 * cross the intersection.
	 */
	public static final long TIME_TO_CROSS_INTERSECTION = 4000;  // TODO Make the vehicle determine this on-line when it approaches the intersection.
	
	/**
	 * This is the time when the vehicle may enter the intersection.
	 */
	private long grantTime = -1;
	
	/**
	 * The distance to the entrance in meters.
	 */
	private double distToEntrance = DISTANCE_TO_ENTRANCE;
	
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
	public V2IReservationClientDaemon(InetAddress serverIP, int serverPort, int port, 
			LineFollower lineFollower, IntersectionDetector intersectionDetector, Position2DBuffer pos2DBuffer,
			String entryPointID, String exitPointID) 
	{
		super(serverIP, serverPort, port, lineFollower, intersectionDetector,pos2DBuffer,
				entryPointID, exitPointID);
	}
	
	/**
	 * Returns the number of milliseconds until the grant time.
	 * If the grant time has already past, return 0.  If the grant
	 * time was not set, return the maximum long value.
	 * 
	 * @return the number of milliseconds until the grant time.
	 */
	private long getTimeTillGrant() {
		if (grantTime == -1)
			return Long.MAX_VALUE;
		
		long currTime = System.currentTimeMillis();
		if (currTime >= grantTime)
			return 0;
		else
			return grantTime - currTime;
	}
	
	/**
	 * Implements the logic of how to handle a grant access message.
	 * 
	 * @param msg The grant access message.
	 */
	@Override
	protected void handleGrantMessage(GrantAccessMsg msg) {
		
		if (msg instanceof GrantAccessReservationMsg) {
			
			GrantAccessReservationMsg grantMsg = (GrantAccessReservationMsg)msg;
			
			// Check if this is a duplicate grant message.
			if (grantTime != -1) {
				Logger.log("WARNING: duplicate grant message.  old grant time = " + grantTime 
						+ ", new grant time = " + grantMsg.getReservationTime());
			}
			
			// Regardless if this is a duplicate grant message, update the grant time.
			grantTime = grantMsg.getReservationTime();
			Logger.log("Grant time is " + grantTime + ", which is " + getTimeTillGrant() + " ms from now.");
			
			if (getTimeTillGrant() == 0) {
				Logger.log("Received grant message and grant time has arrived, setting access granted to be true.");
				accessGranted = true;
			} else {
				Logger.log("Received grant message but grant time has not arrived.");
			}

			if (currState == IntersectionEventType.APPROACHING) {
				Logger.log("Received grant message while still approaching intersection.");	
			}
			else if (currState == IntersectionEventType.ENTERING) {
				Logger.log("Received grant message while at entrance to intersection, resuming robot movement.");
				lineFollower.unpause();
			}
			else
				Logger.logErr("Received unexpected grant message, currState = " + currState);
		} else {
			Logger.logErr("Received a GrantAccessMsg that was not of type GrantAccessReservationMsg!");
			System.exit(1);
		}
	}

	/**
	 * Sends a request to the server asking for permission to cross the intersection.
	 * It overrides the parent's method by sending a RequestReservationmsg rather than
	 * a RequestAccessMsg.  The only difference between these messages is the RequestReservationMsg
	 * includes the time it will take for the local vehicle to cross the intersection.
	 */
	@Override
	protected void sendRequest() {
		// Only send request messages if grant time was not already set.
		if (grantTime == -1) {
			long currTime = System.currentTimeMillis();
			long timeSinceLastReq = currTime - lastRequestTime;
			if (timeSinceLastReq > REQUEST_TIMEOUT) {
				Logger.log("Sending request to server.");
				RequestReservationMsg requestMsg = new RequestReservationMsg(ip, port, 
						entryPointID, exitPointID, TIME_TO_CROSS_INTERSECTION);

				Logger.log("Sending request access message to server.");
				try {
					msgSender.sendMessage(serverIP, serverPort, requestMsg);
				} catch (PharosException e) {
					Logger.logErr(e.toString());
					e.printStackTrace();
				}

				lastRequestTime = currTime;
			}
		}
	}
	
	/**
	 * Adjust the vehicle's speed based on the grant time and the estimated distance to the entrance.
	 */
	protected void adjustVehicleSpeed() {
		
		double newMaxSpeed;
		
		long timeTillGrant = getTimeTillGrant();
		if (timeTillGrant == 0) {
			lineFollower.setMaxSpeed(LineFollower.MAX_SPEED);
			newMaxSpeed = LineFollower.MAX_SPEED;
		} else {
			newMaxSpeed = distToEntrance / (timeTillGrant/1000.0);
			lineFollower.setMaxSpeed(newMaxSpeed);
		}
		
		Logger.log("Adjusting the vehicle's velocity so it arrives at the entrance JIT. distToEntrance = " + distToEntrance
				+ ", time till grant = " + timeTillGrant + ", new max speed = " + newMaxSpeed);
	}

	/**
	 * This is called whenever new position 2D data arrives.  It contains odometry information that 
	 * we use to estimate the distance to the entrance of the intersection.
	 */
	@Override
	public void newPlayerPosition2dData(PlayerPosition2dData data) {
		
		// Only compute the distance to the entrance if we have passed the approaching marker.
		if (currState == IntersectionEventType.APPROACHING) {
			double distTraveled = data.getPos().getPx() / 1000.0; 
			distToEntrance = Math.max(0, distToEntrance - distTraveled);
			Logger.log("Updating the estimated distance to the entrance.  distTraveled = " + distTraveled 
					+ ", distToEntrance = " + distToEntrance);
		}
	}
}
