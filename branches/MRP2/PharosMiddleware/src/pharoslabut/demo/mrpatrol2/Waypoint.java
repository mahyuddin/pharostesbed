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
	
	/**
	 * The maximum speed in m/s at which the robot should travel 
	 * towards the waypoint. 
	 */
	private double speed;
	
	public Waypoint(String name, Location loc, double speed) {
		this.name = name;
		this.loc = loc;
		this.speed = speed;
	}
	
	public String getName() {
		return name;
	}
	
	public Location getLoc() {
		return loc;
	}
	
	public double getSpeed() {
		return speed;
	}
	
	public String toString() {
		return "Waypoint " + name + ", " + loc + ", " + speed + " m/s";
	}
}
