package pharoslabut.logger.analyzer;

/**
 * A pairing between a heading measurement and the 
 * time at which it was taken.
 * 
 * @author Chien-Liang Fok
 *
 */
public class HeadingState {
	private double heading;
	private long timestamp;
	
	/**
	 * The constructor.
	 * 
	 * @param timestamp The timestamp of the heading measurement.
	 * @param heading The heading measurement.
	 */
	public HeadingState(long timestamp, double heading) {
		this.timestamp = timestamp;
		this.heading = heading;
	}
	
	/**
	 * Recalibrates the time based on the GPS timestamps.
	 * 
	 * @param calibrator The time calibrator.
	 */
	public void calibrateTime(TimeCalibrator calibrator) {
		timestamp = calibrator.getCalibratedTime(timestamp);
	}
	
	/**
	 * 
	 * @return The heading in radians.
	 */
	public double getHeading() {
		return heading;
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
		return "HeadingState: " + timestamp + "\t" + heading;
	}
}
