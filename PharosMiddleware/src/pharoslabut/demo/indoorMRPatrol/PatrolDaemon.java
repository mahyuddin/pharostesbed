package pharoslabut.demo.indoorMRPatrol;

import pharoslabut.logger.Logger;
import pharoslabut.navigate.LineFollower;
import pharoslabut.sensors.PathLocalizerOverheadMarkers;
import pharoslabut.sensors.PathLocalizerOverheadMarkersListener;

/**
 * The top-level class of all PatrolDaemons.
 * 
 * @author Chien-Liang Fok
 */
public abstract class PatrolDaemon implements PathLocalizerOverheadMarkersListener{
	/**
	 * The number of markers per round.
	 */
	protected int numMarkersPerRound;
	
	/**
	 * The number of rounds to patrol.
	 */
	protected int numRounds;
	
	/**
	 * The line follower.
	 */
	protected LineFollower lineFollower;
	
	/**
	 * The number of markers seen.
	 */
	protected volatile int numMarkersSeen = 0;
	
	/**
	 * Whether the number of markers seen was updated.
	 */
	protected volatile boolean numMarkersSeenUpdated = false;
	
	/**
	 * Whether the experiment is done.
	 */
	protected boolean done = false;
	
	/**
	 * The path localizer.
	 */
	protected PathLocalizerOverheadMarkers pathLocalizer;
	
	/**
	 * The starting marker.
	 */
	int startingMarkerID;
	
	/**
	 * The constructor.
	 * 
	 * @param settings The experiment settings.
	 * @param numRounds The number of rounds to patrol.
	 * @param lineFollower The line follower.
	 * @param pathLocalizer The path localizer.
	 */
	public PatrolDaemon(LoadExpSettingsMsg settings, int numRounds, LineFollower lineFollower,
			PathLocalizerOverheadMarkers pathLocalizer) 
	{
		this.numMarkersPerRound = settings.getNumMarkers();
		this.numRounds = numRounds;
		this.lineFollower = lineFollower;
		this.pathLocalizer = pathLocalizer;
		this.startingMarkerID = (int)(settings.getMySettings().getStartingLoc() / settings.getMarkerDist());
		pathLocalizer.addListener(this);
	}
	
	/**
	 * This is called whenever a marker is detected.
	 */
	@Override
	public void markerEvent(int numMarkers, double distance) {
		Logger.log("MARKER DETECTED: Total = " + numMarkers + ", At marker " + (startingMarkerID + numMarkers) % numMarkersPerRound);
		synchronized(this) {
			this.numMarkersSeen = numMarkers;
			this.numMarkersSeenUpdated = true;
			this.notifyAll();
		}
	}

	/**
	 * Checks whether the experiment is done.  The experiment is done when
	 * the number of markers seen is equal to the number of markers per round
	 * times the number of rounds in the experiment.
	 * 
	 * @return True if the experiment is done.
	 */
	protected boolean checkDone() {
//		int numMarkersSeen;
//		
//		// grab a copy of the number of markers seen
//		synchronized(this) {
//			numMarkersSeen = this.numMarkersSeen;
//		}
		int numMarkersWhenDone = numMarkersPerRound * numRounds;
		Logger.logDbg("numMarkersSeen = " + numMarkersSeen + ", numMarkersWhenDone = " + numMarkersWhenDone);
		return numMarkersSeen >= numMarkersWhenDone;
	}
}
