package pharoslabut.logger.analyzer;

import pharoslabut.navigate.Location;

/**
 * A pairing between a Location object provided by Player and the 
 * time at which it was received.
 * 
 * @author Chien-Liang Fok
 *
 */
public class LocationState {
	private Location loc;
	private long timestamp;
	
	/**
	 * The constructor.
	 * 
	 * @param timestamp The time.
	 * @param loc The location.
	 */
	public LocationState(long timestamp, Location loc) {
		this.timestamp = timestamp;
		this.loc = loc;
	}
//	
//	/**
//	 * Recalibrates the time based on the GPS timestamps.
//	 * 
//	 * @param calibrator The time calibrator.
//	 */
//	public void calibrateTime(TimeCalibrator calibrator) {
//		timestamp = calibrator.getCalibratedTime(timestamp);
//	}
	
	/**
	 * @return the location.
	 */
	public Location getLoc() {
		return loc;
	}

	/**
	 * Returns the local time stamp.  This is the value of the local
	 * system clock and obtained using System.currentTimeMillis().
	 *  
	 * @return The local time stamp.
	 */
	public long getTimestamp() {
		return timestamp;
	}
	
	public String toString() {
		return "LocationState: " + timestamp + "\t" + loc;
	}
}
