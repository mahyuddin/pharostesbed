package pharoslabut.demo.mrpatrol2.daemons;


import pharoslabut.logger.Logger;
import pharoslabut.demo.mrpatrol2.config.ExpConfig;

/**
 * The top-level class of all PatrolDaemons used in the multi-robot patrol 2 (MRP2)
 * experiments.
 * 
 * @author Chien-Liang Fok
 */
public abstract class PatrolDaemon {
	/**
	 * The experiment configuration.
	 */
	protected ExpConfig expConfig;
	
	/**
	 * The number of waypoints visited.
	 */
	protected volatile int numWaypointsVisited = 0;
	
	/**
	 * Whether the experiment is done.
	 */
	protected boolean done = false;
	
	/**
	 * The minimum beacon period.
	 */
	protected long minPeriod = 1000;
	
	/**
	 * The maximum beacon period.
	 */
	protected long maxPeriod = 1500;
	
	/**
	 * The beacon transmission power.
	 */
	protected short txPower = (short)31;
	
	/**
	 * The constructor.
	 * 
	 * @param expConfig The experiment settings.
	 */
	public PatrolDaemon(ExpConfig expConfig) {
		this.expConfig = expConfig;
	}
	
//	/**
//	 * This is called whenever a marker is detected.
//	 */
//	@Override
//	public void markerEvent(int numMarkers, double distance) {
//		Logger.log("MARKER DETECTED: Total = " + numMarkers + ", At marker " + (startingMarkerID + numMarkers) % numMarkersPerRound);
//		synchronized(this) {
//			this.numMarkersSeen = numMarkers;
//			this.numMarkersSeenUpdated = true;
//			this.notifyAll();
//		}
//	}

	/**
	 * Checks whether the experiment is done.  The experiment is done when
	 * the number of markers seen is equal to the number of markers per round
	 * times the number of rounds in the experiment.
	 * 
	 * @return True if the experiment is done.
	 */
	protected boolean checkDone() {
		int numWaypointsVisitedWhenDone = expConfig.getNumWaypoints() * expConfig.getNumRounds();
		Logger.logDbg("Waypoints visited = " + numWaypointsVisited + ", total when done = " + numWaypointsVisitedWhenDone);
		return numWaypointsVisited >= numWaypointsVisitedWhenDone;
	}
}