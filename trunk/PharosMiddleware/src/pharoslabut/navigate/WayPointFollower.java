package pharoslabut.navigate;

import pharoslabut.logger.FileLogger;

/**
 * Follows a set of way points.  This is a simple implementation that
 * only uses a single speed throughout the entire process.
 * 
 * @author Chien-Liang Fok
 */
public class WayPointFollower implements Runnable {
	private NavigateCompassGPS navigator;
	private GPSMotionScript script;
	private boolean running = false;
	private FileLogger flogger;
	private WayPointFollowerDoneListener doneListener;
	
	public WayPointFollower(NavigateCompassGPS navigator, GPSMotionScript script, FileLogger flogger) {
		this.navigator = navigator;
		this.script = script;
		this.flogger = flogger;
	}
	
	/**
	 * Starts the robot following the waypoints specified in the motion script.
	 * This method should only be called when the WayPointFoller is stopped.  If it
	 * is called when the WayPointFollower is running, a false value will be returned.
	 * 
	 * @param doneListener The listener that should be notified when the WayPointFoller is done.
	 * @return true if the call was successful, false otherwise.
	 */
	public boolean start(WayPointFollowerDoneListener doneListener) {
		if (!running) {
			running = true;
			this.doneListener = doneListener;
			new Thread(this).start();
			return true;
		} else
			return false;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public void stop() {
		running = false;
		doneListener = null;
	}
	
	public void run() {
		int wayPointIndx = 0;
		while (running && wayPointIndx < script.numWayPoints()) {
			Location nextwp = script.getWayPoint(wayPointIndx);
			double speed = script.getSpeed(wayPointIndx);
			try {
				log("Going to " + nextwp + " at " + speed + "m/s");
				if (navigator.go(nextwp, speed)) { // this is a block operation, does not return until the robot is at the destination
					log("Destination Reached!");
				} else {
					log("Destination not reached!");
				}
				
				long pauseTime = script.getPauseTime(wayPointIndx);
				log("Pausing for " + pauseTime + "ms");
				synchronized(this) {
					wait(pauseTime);
				}
				wayPointIndx++;
				
			} catch (SensorException e) {
				log("Error accessing sensor: " + e.getMessage());
				e.printStackTrace();
				running = false;
			} catch (InterruptedException e) {
				log("Error while pausing: " + e.getMessage());
				e.printStackTrace();
				running = false;
			}
		}
		
		if (wayPointIndx == script.numWayPoints())
			log("Motion Script Completed!");
		else 
			log("Terminated prematurely.");
		
		if (doneListener != null)
			doneListener.wayPointFollowerDone(wayPointIndx == script.numWayPoints(), wayPointIndx);
		
		stop();
	}
	
	private void log(String msg) {
		String result = "WayPointFollower: " + msg;
		
		// only print log text to string if in debug mode
		if (System.getProperty ("PharosMiddleware.debug") != null) {
			System.out.println(result);
		}
		
		// always log text to file if a FileLogger is present
		if (flogger != null) {
			flogger.log(result);
		}
	}
}
