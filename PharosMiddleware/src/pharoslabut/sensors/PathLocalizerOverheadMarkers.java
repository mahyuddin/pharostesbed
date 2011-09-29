package pharoslabut.sensors;

import pharoslabut.logger.Logger;
import playerclient3.structures.ranger.PlayerRangerData;

/**
 * Uses overhead markers to localize a robot along a path.  The overhead
 * markers are detected using IR sensors.
 * 
 * @author Chien-Liang Fok
 *
 */
public class PathLocalizerOverheadMarkers implements RangerListener {

	/**
	 * Defines the possible states that the client manager can be in.
	 */
	public static enum IRPathLocalizerState {NO_MARKER, MARKER};
	
	/**
	 * The index of the relevant range data within the PlayerRangerData object.
	 */
	public static final int OVERHEAD_RANGER_INDEX = 5;
	
	/**
	 * The distance between overhead markers in meters.
	 */
	public static final double OVERHEAD_MARKER_SEPARATION_DISTANCE = 1;
	
	/**
	 * The threshold value above which the marker is assumed to not exist.
	 */
	public static final int THRESHOLD_NONEXIST_MARKER = 10000;  // it is usually 65535
	
	/**
	 * The threshold value below which the marker is assumed to exist.
	 */
	public static final int THRESHOLD_EXIST_MARKER = 1000;
	
	/**
	 * The number of consecutive no marker sensor readings before we conclude
	 * that the marker no longer exists.
	 */
	public static final int THRESHOLD_NO_MARKER = 5;
	
	/**
	 * The number of consecutive marker sensor readings before we conclude that the 
	 * marker exists.
	 */
	public static final int THRESHOLD_MARKER = 1;
	
	/**
	 * Whether the robot is currently under a marker.  It is initially assumed to not be under a marker.
	 */
	private IRPathLocalizerState currState = IRPathLocalizerState.NO_MARKER;
	
	/**
	 * Records the number of overhead markers seen by the robot.
	 */
	private int numOverheadMarkers = 0;
	
	/**
	 * The total distance traveled by the robot in meters.
	 */
	private double totalDistTraveled = 0;
	
	/**
	 * The number of consecutive sensor measurements in which the marker is not detected.
	 */
	private int countNoMarker = 0;
	
	/**
	 * The number of consecutive sensor measurements in which the marker
	 * is detected.
	 */
	private int countMarker = 0;
	
	/**
	 * The constructor.
	 * 
	 * @param rangerBuffer The source of range information.
	 */
	public PathLocalizerOverheadMarkers(RangerDataBuffer rangerBuffer) {
		rangerBuffer.addRangeListener(this);
	}

	/**
	 * This processes the raw range data from the IR sensor used to detect the overhead markers.
	 */
	private void processRangeData(double range) {
		Logger.log("current range measurement: " + range);
		
		if (range > THRESHOLD_NONEXIST_MARKER) {
			// We may no longer be under the overhead marker.
			
			countMarker = 0; // reset the "marker" counter

			if (currState != IRPathLocalizerState.NO_MARKER) {
				
				// See if we get THRESHOLD_NO_MARKER consecutive 
				// measurements greater than THRESHOLD_NONEXIST_MARKER
				if (++countNoMarker > THRESHOLD_NO_MARKER) {
					// Conclude that we are no longer under an overhead marker
					currState = IRPathLocalizerState.NO_MARKER;
					Logger.log("STATUS CHANGE: NO_MARKER");
				} else {
					// We got a distance measurement that might indicate the non-presence of the 
					// marker but we need to wait till we get THRESOLD_NO_MARKER consecutive readings
					// that are greater than THRESHOLD_NONEXIST_MARKER.
				}
			} else {
				// This is a duplicate "no marker" signal.  Ingnore it.
			}
		} else if (range < THRESHOLD_EXIST_MARKER) {
			// We may be under a marker
			
			countNoMarker = 0; // reset the "no marker" counter
			
			if (currState == IRPathLocalizerState.NO_MARKER) {
				
				if (++countMarker > THRESHOLD_MARKER) {
					// Conclude that we are under a mark
					currState = IRPathLocalizerState.MARKER;
					numOverheadMarkers++;
					Logger.log("STATUS CHANGE: MARKER Total markers seen: " + numOverheadMarkers);
				}
			} else {
				// duplicate event.  Ignore it.  Maybe print a debug statement to know we're here
			}
		} else {
			countMarker = 0;
			countNoMarker = 0;
		}
	}

	@Override
	public void newRangerData(PlayerRangerData rangeData) {
		int numSensors = rangeData.getRanges_count();
    	if (numSensors >= 6) {
    		double[] data = rangeData.getRanges();
    		double range = data[5];
    		
    		processRangeData(range);
    		
    	} else {
    		Logger.logErr("Expected at least 6 sensors, instead got " + numSensors);
    	}
		
	}
}
