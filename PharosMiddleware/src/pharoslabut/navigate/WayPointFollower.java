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
	
	public WayPointFollower(NavigateCompassGPS navigator, GPSMotionScript script, FileLogger flogger) {
		this.navigator = navigator;
		this.script = script;
		this.flogger = flogger;
	}
	
	public void start() {
		if (!running) {
			running = true;
			new Thread(this).start();
		}
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public void stop() {
		running = false;
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
