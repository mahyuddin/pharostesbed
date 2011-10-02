package pharoslabut.demo.indoorMRPatrol;

import pharoslabut.logger.Logger;
import pharoslabut.navigate.LineFollower;
import pharoslabut.sensors.PathLocalizerOverheadMarkers;

/**
 * Handles the execution of an uncoordinated robot patrol experiment.
 * 
 * @author Chien-Liang Fok
 *
 */
public class UncoordinatedPatrolDaemon extends PatrolDaemon implements Runnable {
	
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
		super(settings, numRounds, lineFollower, pathLocalizer);
		new Thread(this).start();
	}
	
	@Override
	public void run() {
		Logger.logDbg("Thread starting...");
		
		if (!checkDone()) {
			Logger.logDbg("Starting the line follower.");
			lineFollower.start();
			
			while (!checkDone()) {
				synchronized(this) {
					if (!numMarkersSeenUpdated) {
						try {
							this.wait();
						} catch (InterruptedException e) {
							Logger.logErr("Exception while waiting: [" + e.getMessage() + "]");
							e.printStackTrace();
						}
					} else
						numMarkersSeenUpdated = false;
				}
			}
		} else {
			Logger.log("WARNING: The experiment was completed even before it started!");
			System.exit(0);
		}
		
		Logger.log("Experiment completed!");
		lineFollower.stop();
		Logger.log("Program exiting.");
		System.exit(0);
	}
}
