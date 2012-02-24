package pharoslabut.demo.mrpatrol2;

import pharoslabut.navigate.Location;

/**
 * Defines a waypoint in a multi-robot patrol 2 (MRP2) experiment.
 * 
 * @author Chien-Liang Fok
 */
public class Waypoint implements java.io.Serializable {
	
	private static final long serialVersionUID = -8956251667548652022L;

	private String name;
	
	private Location loc;
	
	public Waypoint(String name, Location loc) {
		this.name = name;
		this.loc = loc;
	}
	
	public String getName() {
		return name;
	}
	
	public Location getLoc() {
		return loc;
	}
	
	public String toString() {
		return name + ", " + loc;
	}
}
