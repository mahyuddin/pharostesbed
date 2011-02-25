package pharoslabut.logger.analyzer;

import java.util.*;
import pharoslabut.navigate.Location;

/**
 * Contains the data collected as the robot travels along a single edge of a motion script.
 * 
 * @author Chien-Liang Fok
 */
public class PathEdge {
	private Location dest;
	private Location startLoc = null;
	
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
	 * The locations of the robot as it traversed this edge.
	 */
	private Vector <GPSLocationState> locations = new Vector<GPSLocationState>();
	
	/**
	 * The constructor.
	 * 
	 * @param dest the destination location
	 * @param startTime The time of starting to traverse the edge.
	 * @param speed The speed at which the robot was supposed to move.
	 */
	public PathEdge(Location dest, long startTime, double speed) {
		this.dest = dest;
		this.startTime = startTime;
		this.speed = speed;
	}
	
	/**
	 * Recalibrates the time based on the GPS timestamps.
	 * 
	 * @param timeOffset The offset between the system time and GPS time,
	 * accurate to within a second.
	 */
	public void calibrateTime(double timeOffset) {
		startTime = RobotExpData.getCalibratedTime(startTime, timeOffset);
		endTime = RobotExpData.getCalibratedTime(endTime, timeOffset);
		for (int i=0; i < locations.size(); i++) {
			GPSLocationState currLoc = locations.get(i);
			currLoc.calibrateTime(timeOffset);
		}
	}
	
	/**
	 * Returns the distance away from the destination that the robot was 
	 * at the end of traversing the path edge.
	 * 
	 * @return The distance in meters.
	 */
	public double finalDistFromDest() {
		return getLocation(getNumLocations()-1).getLocation().distanceTo(dest);
	}
	
	/**
	 * Calculates the lateness of the robot arriving at the destination
	 * 
	 * @return The lateness of the robot arriving at the destination.
	 */
	public double getLateness() {
		double averageSpeed = getAvgSpeed();
		
		
		
		double actualDistance = startLoc.distanceTo(dest);
		
		
		double expectedTime = actualDistance / averageSpeed;
		double actualTime = (endTime - startTime) / 1000.0;
		
		System.out.println("PathEdge.getLateness(): average speed: " + averageSpeed);
		System.out.println("PathEdge.getLateness(): actual distance: " + actualDistance);
		System.out.println("PathEdge.getLateness(): expected time: " + expectedTime);
		System.out.println("PathEdge.getLateness(): actualTime: " + actualTime);
		System.out.println("PathEdge.getLateness(): finalDistFromDest: " + finalDistFromDest());
		
		return actualTime - expectedTime;
	}
	
	/**
	 * Returns the average speed of the robot as it traveled along this path edge.
	 * This is calculated by taking the total distance traveled and dividing it by the total
	 * time it took to reach the destination waypoint.
	 * 
	 * @return The average speed of the robot in m/s.
	 */
	public double getAvgSpeed() {
//		double dist = startLoc.distanceTo(dest); // the distance in meters
//		long deltaTime = endTime - startTime; // the length of time in milliseconds
//		return dist / (deltaTime / 1000.0);
		
		double sum = 0; // the total distance traveled
		for (int i = 1; i < getNumLocations(); i++) {
			GPSLocationState startLoc = getLocation(i-1);
			GPSLocationState endLoc = getLocation(i);
			sum += startLoc.getLocation().distanceTo(endLoc.getLocation());
		}
		
		double deltaTime = ((endTime - startTime) / 1000.0);
		double result = sum / deltaTime;
		
		System.out.println("PathEdge.getAvgSpeed(): robot travel distance: " + sum);
		System.out.println("PathEdge.getAvgSpeed(): robot travel time: " + deltaTime);
		System.out.println("PathEdge.getAvgSpeed(): robot speed: " + result);
		
		return result;
	}
	
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
	 * @return true if a start edge was set.
	 */
	public boolean hasStartLoc() {
		return startLoc != null;
	}
	
	/**
	 * Returns the starting location of the robot as it traverses this edge.
	 * 
	 * @return The starting location.
	 */
	public Location getStartLoc() {
		return startLoc;
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
	 * Returns the end time relative to the start of the experiment.
	 * The units is milliseconds.
	 */
	//public long getEndTime() {
	//	return endTime - expStartTime;
	//}
	
	/**
	 * Returns the absolute end time of this edge.
	 * The units is milliseconds.
	 */
	public long getEndTime() {
		return endTime;
	}
	
	/**
	 * Returns the end time relative to the start of the edge traversal.
	 * The units is milliseconds.
	 */
//	public long getRelativeEndTime() {
//		return endTime - startTime;
//	}
	
	/**
	 * Returns the end time relative to the start of edge traversal and
	 * normalized based on the last GPS reading received before the final
	 * destination was reached.
	 */
	public long getDuration() {
		GPSLocationState lastLoc = locations.get(locations.size()-1);
		return lastLoc.getTimestamp() - startTime;
	}
	
	/**
	 * Returns the final location of the robot when it finishes traversing this edge.
	 * 
	 * @return  the final location of the robot when it finishes traversing this edge.
	 */
	public Location getFinalLocation() {
		return locations.get(locations.size()-1).getLocation();
	}
	
	/**
	 * Returns the location of the robot at the specified time.
	 * Uses a linear interpolation of the robot's location when necessary.
	 * 
	 * @param timestamp The time of interest. 
	 * @return The location of the robot at the specified time.
	 */
	public Location getLocation(long timestamp) {
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
		
		if (startTime > timestamp) {
			log("WARNING: getLocation(timestamp): timestamp prior to beginning of edge. (" + startTime + " > " + timestamp + ")");
			return startLoc;
		}
		
		if (endTime < timestamp) {
			log("WARNING: getLocation(timestamp): timestamp after finishing edge. (" + endTime + " < " + timestamp + ")");
			return getFinalLocation();
		}
		
		// calculate the percent edge traversal...
		//double pctTraversed = ((double)(timestamp - startTime)) / ((double)(endTime - startTime)) * 100.0;
		//log("Path Edge pct traveled: " + pctTraversed);
		
		int beforeIndx = 0; // the index within locations vector that is immediately before the timestamp
		int afterIndx = 0; // the index within locations vector that is immediately after the timestamp
		
		boolean afterIndxFound = false;
		
		for (int i=0; i < locations.size(); i++) {
			GPSLocationState currLocation = locations.get(i);
			if (currLocation.getTimestamp() <= timestamp)
				beforeIndx = i;
			if (!afterIndxFound && currLocation.getTimestamp() >= timestamp) {
				afterIndxFound = true;
				afterIndx = i;
			}
		}
		
		log("Timestamp = " + timestamp + ", BeforeIndx = " + beforeIndx + ", AfterIndx = " + afterIndx);
		
		if (beforeIndx == afterIndx)
			return new Location(locations.get(beforeIndx).getLoc());
		else {
			GPSLocationState bLoc = locations.get(beforeIndx);
			GPSLocationState aLoc = locations.get(afterIndx);
			
			// Now we need to interpolate, create two lines both with time as the x axis.
			// One line has the longitude as the Y-axis while the other has the latitude.
			Location latBeforeLoc = new Location(bLoc.getLocation().latitude(), bLoc.getTimestamp());
			Location latAfterLoc = new Location(aLoc.getLocation().latitude(), aLoc.getTimestamp());
			Line latLine = new Line(latBeforeLoc, latAfterLoc);
			double interpLat = latLine.getLatitude(timestamp);
			
			Location lonBeforeLoc = new Location(bLoc.getTimestamp(), bLoc.getLocation().longitude());
			Location lonAfterLoc = new Location(aLoc.getTimestamp(), aLoc.getLocation().longitude());
			Line lonLine = new Line(lonBeforeLoc, lonAfterLoc);
			double interpLon = lonLine.getLongitude(timestamp);
			
			Location result = new Location(interpLat, interpLon);
			
			log("PathEdge.getLocation(timestamp):");
			log("\tBefore Location @" + bLoc.getTimestamp() + ": " + bLoc.getLocation());
			log("\tAfter Location @" + aLoc.getTimestamp() + ": " + aLoc.getLocation());
			log("\tInterpolated Location @" + timestamp + ": " + result);
			return result;
		}
	}
	
	/**
	 * Returns the location of the robot at the specified
	 * percentage of edge traversed.  This location is usually interpolated
	 * since it's highly unlikely that a GPS measurement arrived at the 
	 * precise time desired.
	 * 
	 * @param pct The percentage of the path that has been traversed.
	 * @return The location of the robot at that percentage in time.
	 */
	public Location getLocationPct(double pct) {
		
		// Determine the time at the percentage completion...
		long timestamp = (long)((endTime - startTime) * (pct / 100.0)) + startTime;
		if (timestamp > endTime)
			timestamp = endTime;
		
		return getLocation(timestamp);
	}
	
	public void addLocation(GPSLocationState ls) {
		locations.add(ls);
	}
	
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
	public Enumeration<GPSLocationState> getLocationsEnum() {
		return locations.elements();
	}
	
	/**
	 * The GPS sensor reports location information approximately at 1Hz.
	 * This method returns the number of GPS locations recorded while the robot
	 * was traversing this edge.
	 * 
	 * @return the number of GPS locations recorded during this edge.
	 */
	public int getNumLocations() {
		return locations.size();
	}
	
	/**
	 * The GPS sensor reports location information approximately at 1Hz.
	 * This method returns a specific GPS location measurement that was recorded 
	 * while the robot was traversing this edge.
	 * 
	 * @param indx The index of the GPS location measurement, between 0 and 
	 * the value returned by getNumLocations.
	 * @return the number of GPS locations recorded during this edge.
	 */
	public GPSLocationState getLocation(int indx) {
		return locations.get(indx);
	}
	
	public Location getDest() {
		return dest;
	}
	
	/**
	 * The start time of the experiment.
	 * 
	 * @return The start time of the experiment.
	 */
	public long getStartTime() {
		return startTime;
	}
	
	public double getSpeed() {
		return speed;
	}
	
	public double getDistanceToDest(int indx) {
		return dest.distanceTo(new Location(getLocation(indx).getLoc()));
	}
	
	private void log(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println("PathEdge: " + msg);
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Start Location: " + getStartLoc() + ", Destination: " + getDest() + "\n");
		sb.append("Start time: " + getStartTime() + ", End Time: " + getEndTime() + "\n");
		sb.append("Speed of travel: " + getSpeed() + "\n");
		sb.append("Number of GPS records: " + getNumLocations()  + "\n");
		sb.append("GPS History: " + "\n");
		for (int i = 0; i < getNumLocations(); i++) {
			GPSLocationState loc = getLocation(i);
			sb.append("\t" + (i+1) + ":\n\t\tTime:" + (loc.getTimestamp() - startTime + "\n") 
					+ "\t\tLocation:" + loc.getLoc() + "\n");
			sb.append("\t\tDistance to Destination (m): " + getDistanceToDest(i)+ "\n");
		}
		return sb.toString();
	}
}