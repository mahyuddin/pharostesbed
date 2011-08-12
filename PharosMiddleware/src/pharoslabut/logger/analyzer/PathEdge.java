package pharoslabut.logger.analyzer;

import pharoslabut.navigate.Location;

/**
 * Contains the data about a single edge in a motion script.
 * 
 * @author Chien-Liang Fok
 */
public class PathEdge {
	
	/**
	 * The sequence number of the path edge.
	 */
	private int seqno;
	
	private Location endLoc;
	private Location idealStartLoc;
	private Location startLoc;
	
	/**
	 * The time at which the robot started to traverse this edge.
	 */
	private long startTime;
	
	/**
	 * The time at which the robot finished traversing this edge.
	 */
	private long endTime; // milliseconds
	
	/**
	 * This is the absolute time at which the experiment started.
	 */
	//private long expStartTime; // milliseconds
	
	/**
	 * The speed at which the robot was supposed to move.
	 */
	private double speed;
	
	/**
	 * The initial heading of the robot when it starts to traverse
	 * this path edge.
	 */
	private double startHeading = Double.MIN_VALUE;
	
	/**
	 * The constructor.
	 *
	 * @param endLoc The ideal destination location as specified by the motion script.
	 * @param startTime The time at which the robot started to traverse the edge.
	 * @param speed The ideal speed at which the robot was supposed to move,
	 * as specified by the motion script.
	 */
	public PathEdge(Location endLoc, long startTime, double speed) {
		this.endLoc = endLoc;
		this.startTime = startTime;
		this.speed = speed;
	}
	
	/**
	 * Sets the ideal start location.  This is the location as specified 
	 * in the motion script.
	 * 
	 * @param idealStartLoc The ideal starting location as specified by the motion script.  This is the 
	 * previous way point if one exists.
	 */
	public void setIdealStartLoc(Location idealStartLoc) {
		this.idealStartLoc = idealStartLoc;
	}
	
	/**
	 * Sets the sequence number.
	 * 
	 * @param seqno  the sequence number.
	 */
	public void setSeqNo(int seqno) {
		this.seqno = seqno;
	}
	
	/**
	 * @return The sequence number.
	 */
	public int getSeqNo() {
		return seqno;
	}
	
	/**
	 * Calibrates the time based on the GPS timestamps.
	 * 
	 * @param calibrator The time calibrator.
	 */
	public void calibrateTime(TimeCalibrator calibrator) {
		startTime = calibrator.getCalibratedTime(startTime);
		endTime = calibrator.getCalibratedTime(endTime);
	}
	
	/**
	 * Returns the distance away from the destination that the robot was 
	 * at the end of traversing the path edge.
	 * 
	 * @return The distance in meters.
	 */
//	public double finalDistFromDest() {
//		return getLocation(getNumLocations()-1).getLocation().distanceTo(dest);
//	}
	
	/**
	 * Calculates the lateness of the robot arriving at the destination
	 * 
	 * @return The lateness of the robot arriving at the destination.  This 
	 * is in milliseconds.
	 */
	public long getLateness() {
		// First calculate the distance traveled
		double dist = startLoc.distanceTo(endLoc);
		
		// Calculate the ideal duration in milliseconds...
		long idealDuration = (long) (dist / speed * 1000);
		
		// Calculate the actual duration in milliseconds...
		long actualDuration = endTime - startTime;
		
		// Calculate the difference
		long lateness = actualDuration - idealDuration;
		
		log("getLateness: dist: " + dist);
		log("getLateness: ideal duration: " + idealDuration);
		log("getLateness: actualDuration: " + actualDuration);
		log("getLateness: lateness: " + lateness);
		
		return lateness;
	}
	
	/**
	 * Returns the average speed of the robot as it traveled along this path edge.
	 * This is calculated by taking the total distance traveled and dividing it by the total
	 * time it took to reach the destination waypoint.
	 * 
	 * @return The average speed of the robot in m/s.
	 */
//	public double getAvgSpeed() {
//		double dist = startLoc.distanceTo(dest); // the distance in meters
//		long deltaTime = endTime - startTime; // the length of time in milliseconds
//		return dist / (deltaTime / 1000.0);
//		
//		double sum = 0; // the total distance traveled
//		for (int i = 1; i < getNumLocations(); i++) {
//			GPSLocationState startLoc = getLocation(i-1);
//			GPSLocationState endLoc = getLocation(i);
//			sum += startLoc.getLocation().distanceTo(endLoc.getLocation());
//		}
//		
//		double deltaTime = ((endTime - startTime) / 1000.0);
//		double result = sum / deltaTime;
//		
//		System.out.println("PathEdge.getAvgSpeed(): robot travel distance: " + sum);
//		System.out.println("PathEdge.getAvgSpeed(): robot travel time: " + deltaTime);
//		System.out.println("PathEdge.getAvgSpeed(): robot speed: " + result);
//		
//		return result;
//	}
	
	/**
	 * Sets the start location of this edge.
	 * 
	 * @param startLoc The robot's starting location along this edge.
	 */
	public void setStartLoc(Location startLoc) {
		this.startLoc = startLoc;
	}
	
	/**
	 * Whether the start location was set for this edge.
	 * 
	 * @return true if a start location was set for this edge.
	 */
	public boolean hasStartLoc() {
		return startLoc != null;
	}
	
	/**
	 * Returns the starting location of the robot as it traverses this edge.
	 * This is the actual start location, not the ideal which is the previous edge's
	 * ideal end location.
	 * 
	 * @return The starting location.
	 */
	public Location getStartLoc() {
		return startLoc;
	}
	
	/**
	 * Sets the start heading of this edge.
	 * 
	 * @param startHeading The robot's starting heading along this edge.
	 */
	public void setStartHeading(double startHeading) {
		this.startHeading = startHeading;
	}
	
	/**
	 * Whether the start heading was set for this edge.
	 * 
	 * @return true if a start heading was set.
	 */
	public boolean hasStartHeading() {
		return startHeading != Double.MIN_VALUE;
	}
	
	/**
	 * Returns the starting heading of the robot as it traverses this edge.
	 * This is the actual start heading, not the ideal which is directly facing
	 * the next waypoint.
	 * 
	 * @return The starting heading.
	 */
	public double getStartHeading() {
		return startHeading;
	}
	
	/**
	 * Sets the time when the robot reaches the destination.  Its value is the number of
	 * milliseconds that have passed since the epoch.
	 * 
	 * @param endTime The time at which the robot arrives at the destination.
	 */
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	
	/**
	 * Returns the actual time when the robot arrives at the destination waypoint of
	 * this edge. The units is in milliseconds since the epoch.
	 * 
	 * @return The actual time when the robot reached the end waypoint of this edge.
	 */
	public long getEndTime() {
		return endTime;
	}
	
	/**
	 * Returns the duration of the edge traversal.  This is the difference
	 * between the start time and end time.
	 * 
	 * @return The duration of the edge in milliseconds.
	 */
	public long getDuration() {
		return getEndTime() - getStartTime();
	}
	
	
	/**
	 * Returns the location of the robot at the specified time.
	 * Uses a linear interpolation of the robot's location when necessary.
	 * 
	 * @param timestamp The time of interest. 
	 * @return The location of the robot at the specified time.
	 */
//	public Location getLocation(long timestamp) {
//		GPSLocationState beforeLoc = null;
//		GPSLocationState afterLoc = null;
//		
//		for (int i=0; i < locations.size(); i++) {
//			GPSLocationState currLoc = locations.get(i);
//			if (currLoc.getTimestamp() <= timestamp)
//				beforeLoc = currLoc;
//			if (afterLoc == null && currLoc.getTimestamp() >= timestamp)
//				afterLoc = currLoc;
//		}
//	}
	
	/**
	 * Returns the location of the robot at the specified
	 * percentage of edge traversed.  This location is usually interpolated
	 * since it's highly unlikely that a GPS measurement arrived at the 
	 * precise time desired.
	 * 
	 * @param pct The percentage of the path that has been traversed.
	 * @return The location of the robot at that percentage in time.
	 */
//	public Location getLocationPct(double pct) {
//		
//		// Determine the time at the percentage completion...
//		long timestamp = (long)((endTime - startTime) * (pct / 100.0)) + startTime;
//		if (timestamp > endTime)
//			timestamp = endTime;
//		
//		return getLocation(timestamp);
//	}
	
	/**
	 * Returns the time when the robot has finished traversing a specified
	 * percentage of the edge.
	 * 
	 * @return The time when the robot has finished traversing a specified
	 * percentage of the edge.
	 */
	public long getTimePctComplete(double pctComplete) {
		return (long) ((getEndTime() - getStartTime()) * pctComplete + getStartTime());
	}
	
//	public void addLocation(GPSLocationState ls) {
//		locations.add(ls);
//	}
	
	/**
	 * The GPS sensor reports location information approximately at 1Hz.
	 * This method returns all of the GPS locations recorded while the robot
	 * was traversing this edge.
	 * 
	 * @return the number of GPS locations recorded during this edge.
	 */
//	public Vector<GPSLocationState> getLocations() {
//		return locations;
//	}
	
	/**
	 * The GPS sensor reports location information approximately at 1Hz.
	 * This method returns an enumeration of the GPS locations recorded while the robot
	 * was traversing this edge.
	 * 
	 * @return the number of GPS locations recorded during this edge.
	 */
//	public Enumeration<GPSLocationState> getLocationsEnum() {
//		return locations.elements();
//	}
	
	/**
	 * The GPS sensor reports location information approximately at 1Hz.
	 * This method returns the number of GPS locations recorded while the robot
	 * was traversing this edge.
	 * 
	 * @return the number of GPS locations recorded during this edge.
	 */
//	public int getNumLocations() {
//		return locations.size();
//	}
	
	/**
	 * The GPS sensor reports location information approximately at 1Hz.
	 * This method returns a specific GPS location measurement that was recorded 
	 * while the robot was traversing this edge.
	 * 
	 * @param indx The index of the GPS location measurement, between 0 and 
	 * the value returned by getNumLocations.
	 * @return the number of GPS locations recorded during this edge.
	 */
//	public GPSLocationState getLocation(int indx) {
//		return locations.get(indx);
//	}
	
	/**
	 * Returns the ideal final location of the edge.
	 * This is the location as specified by the motion script.
	 */
	public Location getEndLocation() {
		return endLoc;
	}
	
	/**
	 * The start time of the experiment.
	 * 
	 * @return The start time of the experiment.
	 */
	public long getStartTime() {
		return startTime;
	}
	
	/**
	 * Returns the ideal starting location of the robot as it traverses this edge.  This is the location of the
	 * previous waypoint as specified in the motion script.  The actual starting location
	 * may differ based on where the robot stopped when it reached the previous waypoint.
	 * 
	 * @return The ideal starting location of the robot as it traverses this edge
	 */
	public Location getIdealStartLoc() {
		return idealStartLoc;
	}
	
	/**
	 * Returns the ideal speed of the robot.  This is the speed that the
	 * motion script specified.  The actual speed may differ based on
	 * the calibration of the encoder, misalignment of the front wheels, etc.
	 * 
	 * @return The ideal speed of the robot as specified by the motion script.
	 */
	public double getIdealSpeed() {
		return speed;
	}
	
//	public double getDistanceToDest(int indx) {
//		return dest.distanceTo(new Location(getLocation(indx).getLoc()));
//	}
	
	private void log(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println("PathEdge: " + msg);
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Ideal Start Location: " + getIdealStartLoc() + ", Actual Start Location: " + getStartLoc() + ", Destination: " + getEndLocation() + "\n");
		sb.append("Start time: " + getStartTime() + ", End Time: " + getEndTime() + "\n");
		sb.append("Speed of travel: " + getIdealSpeed() + "\n");
//		sb.append("Number of GPS records: " + getNumLocations()  + "\n");
//		sb.append("GPS History: " + "\n");
//		for (int i = 0; i < getNumLocations(); i++) {
//			GPSLocationState loc = getLocation(i);
//			sb.append("\t" + (i+1) + ":\n\t\tTime:" + (loc.getTimestamp() - startTime + "\n") 
//					+ "\t\tLocation:" + loc.getLoc() + "\n");
//			sb.append("\t\tDistance to Destination (m): " + getDistanceToDest(i)+ "\n");
//		}
		return sb.toString();
	}
}