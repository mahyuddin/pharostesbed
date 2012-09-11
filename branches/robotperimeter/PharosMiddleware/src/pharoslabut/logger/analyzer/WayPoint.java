package pharoslabut.logger.analyzer;

import pharoslabut.navigate.Location;

/**
 * A waypoint is a target location that the robot should reach.  
 * 
 * @author Chien-Liang
 *
 */
public class WayPoint {
	private Location loc;
	String name;
	
	public WayPoint(double lat, double lon, String name) {
		loc = new Location(lat, lon);
		this.name = name;
	}
	
	public WayPoint(Location loc, String name) {
		this.loc = loc;
		this.name = name;
	}
	
	public double getLat() {
		return loc.latitude();
	}
	
	public double getLon() {
		return loc.longitude();
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		return loc.latitude() + "\t" + loc.longitude() + "\t" + name;
	}
}
