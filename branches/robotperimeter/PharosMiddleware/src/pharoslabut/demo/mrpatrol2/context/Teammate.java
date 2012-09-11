package pharoslabut.demo.mrpatrol2.context;

import pharoslabut.logger.Logger;

/**
 * Stores information about a particular teammate.
 * 
 * @author Chien-Liang Fok
 */
public class Teammate implements java.io.Serializable {
	
	private static final long serialVersionUID = 8251338847203253858L;

	/**
	 * The name of the teammate.  This corresponds to the robot's name.
	 */
	private String name;
	
	/**
	 * The number of waypoints that the robot has visited.
	 */
	private int numWaypointsVisited = 0;
	
	/**
	 * The time when the teammate was last heard from.  Note that this is
	 * assigned by the teammate using the teammate's clock.  However, since we're
	 * assuming approximate clock synchronization, it should be comparable to the
	 * local clock.
	 */
	private long lastUpdateTime = -1;
	
	/**
	 * The constructor.
	 * 
	 * @param name The name of the teammate.
	 */
	public Teammate(String name) {
		this.name = name;
	}
	
	/**
	 * 
	 * @return The name of the teammate.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Update the number of waypoints visited.
	 * 
	 * @param numWaypointsVisited The number of waypoints visited.
	 * @param lastUpdateTime The last update time (this is recorded by the teammate's system clock).
	 */
	public void updateNumWaypointsVisited(int numWaypointsVisited, long lastUpdateTime) {
		if (this.lastUpdateTime < lastUpdateTime) {
			this.lastUpdateTime = lastUpdateTime;
			
			if (this.numWaypointsVisited <= numWaypointsVisited) {
				this.numWaypointsVisited = numWaypointsVisited;
			} else {
				Logger.logErr("Unexpected decrease in numWaypointsVisited!"
						+ "\n\tRecorded = " + this.numWaypointsVisited + " at " + this.lastUpdateTime
						+ "\n\tSubmitted = " + numWaypointsVisited + " at " + lastUpdateTime);
			}
		} else {
			Logger.logDbg("Did not update numWaypoints visited for robot " + name 
					+ " because recorded lastUpdateTime (" + this.lastUpdateTime 
					+ ") was after specified lastUpdateTime (" + lastUpdateTime + ")");
		}
	}
	
	/**
	 * 
	 * @return The last update time.
	 */
	public long getLastUpdateTime() {
		return lastUpdateTime;
	}
	
	/**
	 * 
	 * @return The number of waypoints visited.
	 */
	public int getNumWaypointsVisited() {
		return numWaypointsVisited;
	}
	
	/**
	 * Determines whether this teammate is still active.
	 * 
	 * @param maxTimeDelta The max difference between the current time and the last time the teammate
	 * was update. 
	 * @return true if the robot is still active, meaning the interval of time between 
	 * when the teammate was last updated and now is less than maxTimeDelta.
	 */
	public boolean isActive(long maxTimeDelta) {
		long currTime = System.currentTimeMillis();
		long timeDelta = currTime - lastUpdateTime;
		boolean isActive = timeDelta < maxTimeDelta;
		Logger.logDbg("currTime = " + currTime + ", timeDelta = " + timeDelta + ", isActive = " + isActive);
		return isActive;
	}
	
	/**
	 * @return A string representation of this class.
	 */
	public String toString() {
		return "Teammate " + name + ", numWaypointsVisited = " + numWaypointsVisited + ", lastUpdateTime = " + lastUpdateTime;
	}
}
