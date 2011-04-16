package pharoslabut.logger.analyzer;

import pharoslabut.navigate.Location;

public class WayPoint {
	private Location loc;
	String name;
	
	public WayPoint(double lat, double lon, String name) {
		loc = new Location(lat, lon);
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
