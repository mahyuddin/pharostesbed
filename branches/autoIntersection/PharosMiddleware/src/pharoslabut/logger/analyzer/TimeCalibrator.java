package pharoslabut.logger.analyzer;

import java.util.*;

/**
 * This maintains a list of local times and the amount of change needed to 
 * shift it to match the GPS timestamp.
 * 
 * @author Chien-Liang Fok
 *
 */
public class TimeCalibrator {
	Vector<Long> calibrationPoints = new Vector<Long>();
	Vector<Double> offsets = new Vector<Double>();
	
	/**
	 * The constructor.
	 */
	public TimeCalibrator() {
	}
	
	/**
	 * Adds a calibration point.
	 * 
	 * @param timestamp The timestamp as recorded by the Pharos Middleware.  This is in ms units.
	 * @param offset The necessary offset of the timestamp to make it match the GPS timestamp.
	 * This should be in ms units.
	 */
	public void addCalibrationPoint(long timestamp, double offset) {
		calibrationPoints.add(timestamp);
		offsets.add(offset);
	}
	
	/**
	 * Recursively finds the closest calibration timestamp to the specified timestamp.
	 * 
	 * @param timestamp The specified timestamp.
	 * @param leftIndx The left index.
	 * @param rightIndx The right index.
	 * @return The index within the localTimestamp vector that is closest to the specified
	 * timestamp.
	 */
	private int findClosestTimestamp(long timestamp, int leftIndx, int rightIndx) {
		
		System.out.println("findClosestTimestamp: " + timestamp + ", " + leftIndx + ", " + rightIndx);
		
		// base cases
		if (leftIndx == rightIndx)
			return leftIndx;
		
		if (Math.abs(leftIndx - rightIndx) == 1) {
			long leftDelta = Math.abs(calibrationPoints.get(leftIndx) - timestamp);
			long rightDelta = Math.abs(calibrationPoints.get(rightIndx) - timestamp);
			
			if (leftDelta < rightDelta)
				return leftIndx;
			else
				return rightIndx;
		}
		
		// Recursive search
		int midPoint = (rightIndx + leftIndx) / 2;
		if (calibrationPoints.get(midPoint) > timestamp)
			return findClosestTimestamp(timestamp, leftIndx, midPoint);
		else
			return findClosestTimestamp(timestamp, midPoint, rightIndx);
	}
	
	/**
	 * Gets the calibrated timestamp.
	 * 
	 * @param timestamp The timestamp to calibrate.
	 * @return The calibrated timestamp.
	 */
	public long getCalibratedTime(long timestamp) {
		if (calibrationPoints.size() == 0) {
			return timestamp;
		} else {
			// find the calibration point that is closest to the timestamp
			int closestIndx = findClosestTimestamp(timestamp, 0, calibrationPoints.size() - 1);
			double offset = offsets.get(closestIndx);
			return Math.round(timestamp - offset);
		}
	}
	
}
