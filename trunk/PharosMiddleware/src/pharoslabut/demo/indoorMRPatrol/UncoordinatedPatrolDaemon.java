package pharoslabut.demo.indoorMRPatrol;

import pharoslabut.logger.Logger;
import pharoslabut.navigate.LineFollower;
import pharoslabut.sensors.PathLocalizerOverheadMarkers;
import pharoslabut.sensors.PathLocalizerOverheadMarkersListener;

/**
 * Handles the execution of an uncoordinated robot patrol experiment.
 * 
 * @author Chien-Liang Fok
 *
 */
public class UncoordinatedPatrolDaemon implements Runnable, PathLocalizerOverheadMarkersListener {

	/**
	 * The number of markers per round.
	 */
	private int numMarkersPerRound;
	
	/**
	 * The number of rounds to patrol.
	 */
	private int numRounds;
	
	/**
	 * The line follower.
	 */
	private LineFollower lineFollower;
	
	/**
	 * The number of markers seen.
	 */
	private int numMarkersSeen = 0;
	
	/**
	 * Whether the experiment is done.
	 */
	boolean done = false;
	
	/**
	 * The constructor.
	 * 
	 * @param settings The experiment settings.
	 * @param lineFollower The line follower.
	 * @param pathLocalizer The path localizer.
	 * @param numRounds The number of rounds to patrol.
	 */
	public UncoordinatedPatrolDaemon(LoadExpSettingsMsg settings, LineFollower lineFollower, 
			PathLocalizerOverheadMarkers pathLocalizer, int numRounds) 
	{
		this.numMarkersPerRound = settings.getNumMarkers();
		this.numRounds = numRounds;
		pathLocalizer.addListener(this);
		new Thread(this).start();
	}
	
	@Override
	public void markerEvent(int numMarkers) {
		Logger.log("Received marker event: num markers = " + numMarkers);
		synchronized(this) {
			this.numMarkersSeen = numMarkers;
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
	private boolean checkDone() {
		int numMarkersSeen;
		
		// grab a copy of the number of markers seen
		synchronized(this) {
			numMarkersSeen = this.numMarkersSeen;
		}
		
		int numMarkersWhenDone = numMarkersPerRound * numRounds;
		
		Logger.logDbg("numMarkersSeen = " + numMarkersSeen + ", numMarkersWhenDone = " + numMarkersWhenDone);
		
		return numMarkersSeen >= numMarkersWhenDone;
	}
	@Override
	public void run() {
		Logger.logDbg("Thread starting...");
		
		if (!checkDone()) {
			Logger.logDbg("Starting the line follower.");
			lineFollower.start();
			
			while (!checkDone()) {
				synchronized(this) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						Logger.logErr("Exception while waiting: [" + e.getMessage() + "]");
						e.printStackTrace();
					}
				}
			}
		} else {
			Logger.log("WARNING: The experiment was completed even before it started!");
			System.exit(0);
		}
	}
}
