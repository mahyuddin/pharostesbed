package pharoslabut.demo.autoIntersection.intersectionSpecs;

import pharoslabut.navigate.Location;

/**
 * Defines an entry point into an intersection.
 * 
 * @author Chien-Liang Fok
 */
public class EntryPoint {
	
	/**
	 * The ID of the entry point.
	 */
	private String id;
	
	/**
	 * The heading of the robot as it enters the intersection.
	 */
	private double heading;
	
	/**
	 * The location of the entry point.
	 */
	private Location location;
	
	/**
	 * The constructor.
	 * 
	 * @param id The ID of the entry point.  Typically, this is "E[x]" where [x] is an integer that
	 * enumerates the entry points.
	 * @param heading
	 * @param location
	 */
	public EntryPoint(String id, double heading, Location location) {
		this.id = id;
		this.heading = heading;
		this.location = location;
	}
	
	public String getID() {
		return id;
	}
	
	public double getHeading() {
		return heading;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public String toString() {
		return getClass().getName() + ", id=" + id + ", heading=" + heading + ", location=" + location;
	}
}
