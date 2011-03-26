package pharoslabut.logger.analyzer;

import playerclient.structures.gps.PlayerGpsData;

import pharoslabut.navigate.*;

/**
 * A pairing between a PlayerGpsData provider by Player and the timestamp at which it was received.
 * 
 * @author Chien-Liang Fok
 *
 */
public class GPSLocationState {
	private PlayerGpsData gpsLoc;
	private long timestamp;
	
	public GPSLocationState(long timestamp, PlayerGpsData gpsLoc) {
		this.timestamp = timestamp;
		this.gpsLoc = gpsLoc;
	}
	
	/**
	 * Recalibrates the time based on the GPS timestamps.
	 * 
	 * @param calibrator The time calibrator.
	 */
	public void calibrateTime(TimeCalibrator calibrator) {
		timestamp = calibrator.getCalibratedTime(timestamp);
	}
	
	public PlayerGpsData getLoc() {
		return gpsLoc;
	}
	
	public Location getLocation() {
		return new Location(gpsLoc);
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
		return timestamp + "\t" + gpsLoc;
	}
}
