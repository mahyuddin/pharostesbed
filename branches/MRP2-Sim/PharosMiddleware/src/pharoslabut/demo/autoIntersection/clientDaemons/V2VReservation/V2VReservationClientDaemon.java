package pharoslabut.demo.autoIntersection.clientDaemons.V2VReservation;

import java.net.InetAddress;
import java.net.UnknownHostException;

import pharoslabut.demo.autoIntersection.clientDaemons.V2VSerial.VehicleStatus;
import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionDetector;
import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionEventType;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.LineFollower;
import pharoslabut.sensors.Position2DBuffer;
import pharoslabut.sensors.Position2DListener;
import playerclient3.structures.position2d.PlayerPosition2dData;

/**
 * Implements a V2V form of intersection management where each robot decides
 * independently whether it is safe to traverse the intersection.  In this daemon,
 * multiple robots may cross the intersection simultaneously.  The vehicles
 * form reservations of times in the future when they can cross.
 * 
 * @author Chien-Liang Fok
 */
public class V2VReservationClientDaemon extends 
	pharoslabut.demo.autoIntersection.clientDaemons.V2VParallel.V2VParallelClientDaemon 
	implements Position2DListener
{
	/**
	 * The amount of time it'll take the vehicle to cross the intersection in milliseconds.
	 */
	public static final long TIME_TO_CROSS = 4000;  // TODO do not hard code this.
	
	/**
	 * The distance to the entrance in meters.
	 */
	private double distToEntrance = pharoslabut.demo.autoIntersection.clientDaemons.V2IReservation.V2IReservationClientDaemon.DISTANCE_TO_ENTRANCE;
	
	/**
	 * The time when this vehicle may enter the intersection.
	 */
	//private long entryTime = -1;
	
	/**
	 * The constructor.
	 * 
	 * @param lineFollower The line follower.
	 * @param intersectionDetector The intersection detector.
	 * @param entryPointID The entry point ID.
	 * @param exitPointID The exit point ID.
	 */
	public V2VReservationClientDaemon(LineFollower lineFollower,
			IntersectionDetector intersectionDetector, Position2DBuffer pos2DBuffer, String entryPointID,
			String exitPointID) 
	{
		super(lineFollower, intersectionDetector, pos2DBuffer, entryPointID, exitPointID);
		pos2DBuffer.addPos2DListener(this);
	}

	/**
	 * Creates the beacon.  In this policy, we use the V2VReservationBeacon.
	 */
	@Override
	protected void createBeacon(String pharosIP) {
		Logger.log("Creating the beacon.");
		try {
			beacon = new V2VReservationBeacon(InetAddress.getByName(pharosIP), mCastPort,
					entryPointID, exitPointID, TIME_TO_CROSS);
		} catch (UnknownHostException e) {
			Logger.logErr("Unable to create the beacon: " + e.toString());
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	@Override
	protected void createNeighborList() {
		 nbrList = new ReservationNeighborList(entryPointID, exitPointID);
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
	
	/**
	 * Returns the number of milliseconds until the entry time.
	 * If the entry time has past, return 0.  If the entry
	 * time was not set, return the maximum long value.
	 * 
	 * @return the number of milliseconds until the entry time.
	 */
	private long getTimeTillEntry() {
		if (safeTimestamp == -1)
			return Long.MAX_VALUE;
		
		long currTime = System.currentTimeMillis();
		if (currTime >= safeTimestamp)
			return 0;
		else
			return safeTimestamp - currTime;
	}
	
	/**
	 * Adjust the vehicle's speed based on the grant time and the estimated distance to the entrance.
	 */
	@Override
	protected void adjustVehicleSpeed() {
		if (currState == IntersectionEventType.APPROACHING) {
			double newMaxSpeed = LineFollower.MAX_SPEED;

			long timeTillEntry = getTimeTillEntry();
			if (timeTillEntry == Long.MAX_VALUE) {
				Logger.logErr("Invalid time till entry, not adjusting vehicle speed.");
			} else if (timeTillEntry == 0) {
				// keep max speed to be the actual max speed
			} else {
				newMaxSpeed = distToEntrance / (timeTillEntry/1000.0);	
			}
			
			lineFollower.setMaxSpeed(newMaxSpeed);

			Logger.log("Adjusting velocity so it arrives at the entrance JIT. distToEntrance = " + distToEntrance
					+ ", time till entry = " + (timeTillEntry == Long.MAX_VALUE ? "NULL":timeTillEntry) + ", new max speed = " + newMaxSpeed);
		}
		
		else if (currState == IntersectionEventType.ENTERING) {
			if (getTimeTillEntry() == 0) {
				Logger.log("Vehicle is in entering state and the time till entry is zero.  Setting max speed to be " + LineFollower.MAX_SPEED);
				lineFollower.setMaxSpeed(LineFollower.MAX_SPEED);
				lineFollower.unpause();
			} else {
				Logger.log("Vehicle is in entering state but time till entry is not zero.  Leaving vehicle speed alone.");
			}
		}
		
		else {
			Logger.log("Not changing vehicle speed because vehicle not in approaching or entering state, currState=" + currState);
		}
	}
	
	/**
	 * Grants the local vehicle permission to cross the intersection.
	 */
	@Override
	protected void grantSelfAccess() {
		accessGranted = true;
		
		long currTime = System.currentTimeMillis();
		
		Logger.log("Granting self access to the intersection.  Updating beacon to indicate I'm crossing with entry time of " + currTime);
		V2VReservationBeacon myBeacon = (V2VReservationBeacon)beacon;
		myBeacon.setVehicleStatus(VehicleStatus.CROSSING);
		myBeacon.setEntryTime(currTime);
	
		lineFollower.unpause();
	}
}
