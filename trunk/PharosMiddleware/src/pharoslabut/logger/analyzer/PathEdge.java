package pharoslabut.logger.analyzer;

import java.util.Vector;
import pharoslabut.navigate.Location;

/**
 * Contains the data collected as the robot travels along a single edge of a motion script.
 * 
 * @author Chien-Liang Fok
 */
public class PathEdge {
	private Location dest;
	private Location startLoc = null;
	private long startTime, endTime; // milliseconds
	private long expStartTime; // milliseconds
	private double speed;
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
	 * Returns the distance away from the destination that the robot was 
	 * at the end of traversing the path edge.
	 * 
	 * @return The distance in meters.
	 */
	public double finalDistFromDest() {
		return getLocation(numLocations()-1).getLocation().distanceTo(dest);
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
		for (int i = 1; i < numLocations(); i++) {
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
	public long getEndTime() {
		return endTime - expStartTime;
	}
	
	/**
	 * Returns the end time relative to the start of the epoch.
	 * The units is milliseconds.
	 */
	public long getAbsoluteEndTime() {
		return endTime;
	}
	
	/**
	 * Returns the end time relative to the start of the edge traversal.
	 * The units is milliseconds.
	 */
	public long getRelativeEndTime() {
		return endTime - startTime;
	}
	
	/**
	 * Returns the end time relative to the start of edge traversal and
	 * normalized based on the last GPS reading received before the final
	 * destination was reached.
	 */
	public long getNormalizedEndTime() {
		return locations.get(locations.size()-1).getTimeStamp() - startTime;
	}
	
	/**
	 * Returns the interpolated location of the robot at the specified
	 * percentage of edge traversal completion.
	 * 
	 * @param pct The percentage of the path that has been traversed.
	 * @return The location of the robot at that percentage in time.
	 */
	public Location getLocationPct(double pct) {
		
		// First determine what absolute time this percentage corresponds do
		long timeAtPctComplete = (long)((endTime - startTime) * (pct / 100.0)) + startTime;
		if (timeAtPctComplete > endTime) {
			timeAtPctComplete = endTime;
		}
		
		int beforeIndx = 0; // the index within locations vector that is immediately before pcntDone
		int afterIndx = 0; // the index within locations vector that is immediately after pctntDone
		
		boolean afterIndxFound = false;
		
		for (int i=0; i < locations.size(); i++) {
			GPSLocationState currLocation = locations.get(i);
			if (currLocation.getTimeStamp() <= timeAtPctComplete)
				beforeIndx = i;
			if (!afterIndxFound && currLocation.getTimeStamp() >= timeAtPctComplete) {
				afterIndxFound = true;
				afterIndx = i;
			}
		}
		
		if (beforeIndx == afterIndx)
			return new Location(locations.get(beforeIndx).getLoc());
		else {
			GPSLocationState bLoc = locations.get(beforeIndx);
			GPSLocationState aLoc = locations.get(afterIndx);
			
			// Now we need to interpolate, create two lines both with time as the x axis.
			// One line has the longitude as the Y-axis while the other has the latitude.
			Location latBeforeLoc = new Location(bLoc.getLocation().latitude(), bLoc.getTimeStamp());
			Location latAfterLoc = new Location(aLoc.getLocation().latitude(), aLoc.getTimeStamp());
			Line latLine = new Line(latBeforeLoc, latAfterLoc);
			double interpLat = latLine.getLatitude(timeAtPctComplete);
			
			Location lonBeforeLoc = new Location(bLoc.getTimeStamp(), bLoc.getLocation().longitude());
			Location lonAfterLoc = new Location(aLoc.getTimeStamp(), aLoc.getLocation().longitude());
			Line lonLine = new Line(lonBeforeLoc, lonAfterLoc);
			double interpLon = lonLine.getLongitude(timeAtPctComplete);
			
			return new Location(interpLat, interpLon);
		}
		
	}
	
	public void addLocation(GPSLocationState ls) {
		locations.add(ls);
	}
	
	public int numLocations() {
		return locations.size();
	}
	
	public GPSLocationState getLocation(int indx) {
		return locations.get(indx);
	}
	
	public Location getDest() {
		return dest;
	}
	
	public long getStartTime() {
		return startTime - expStartTime;
	}
	
	public long getAbsoluteStartTime() {
		return startTime;
	}
	
	public double getSpeed() {
		return speed;
	}
	
	public double getDistanceToDest(int indx) {
		return dest.distanceTo(new Location(getLocation(indx).getLoc()));
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Start Location: " + getStartLoc() + ", Destination: " + getDest() + "\n");
		sb.append("Start time: " + getStartTime() + ", End Time: " + getEndTime() + "\n");
		sb.append("Absolute Start time: " + getAbsoluteStartTime() 
				+ ", Absolute End Time: " + getAbsoluteEndTime() + "\n");
		sb.append("Speed of travel: " + getSpeed() + "\n");
		sb.append("Number of GPS records: " + numLocations()  + "\n");
		sb.append("GPS History: " + "\n");
		for (int i = 0; i < numLocations(); i++) {
			GPSLocationState loc = getLocation(i);
			sb.append("\t" + (i+1) + ":\n\t\tTime:" + (loc.getTimeStamp() - startTime + "\n") 
					+ "\t\tLocation:" + loc.getLoc() + "\n");
			sb.append("\t\tDistance to Destination (m): " + getDistanceToDest(i)+ "\n");
		}
		return sb.toString();
	}
}