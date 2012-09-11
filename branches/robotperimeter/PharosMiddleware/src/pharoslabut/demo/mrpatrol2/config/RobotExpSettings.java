package pharoslabut.demo.mrpatrol2.config;

import java.net.InetAddress;

/**
 * Contains the settings of a single robot in a multi-robot patrol 2 (MRP2) experiment.
 * 
 * @author Chien-Liang Fok
 * @see http://pharos.ece.utexas.edu/wiki/index.php/Multi-Robot_Patrol_2#Experiment_Configuration_File
 */
public class RobotExpSettings implements java.io.Serializable {
	
	private static final long serialVersionUID = 9016197697232793141L;

	/**
	 * The robot's name.
	 */
	private String name;
	
	/**
	 * The robot's IP address.
	 */
	private InetAddress ip;
	
	/**
	 * The robot's port.
	 */
	private int port;
	
	/**
	 * The name of the first waypoint that the robot should start at
	 * in the patrol route.
	 */
	private String firstWaypoint;
	
	/**
	 * The constructor.
	 * 
	 * @param name The robot's name.
	 * @param ip The robot's IP address.
	 * @param port The robot's port.
	 * @param firstWaypoint The name of the first waypoint that the robot should start at
	 * in the patrol route.
	 */
	public RobotExpSettings(String name, InetAddress ip, int port, String firstWaypoint) {
		this.name = name.toUpperCase();
		this.ip = ip;
		this.port = port;
		this.firstWaypoint = firstWaypoint;
	}
	
	/**
	 * 
	 * @return The robot's name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 
	 * @return The robot's IP address.
	 */
	public InetAddress getIP() {
		return ip;
	}
	
	/**
	 * 
	 * @return The robot's port.
	 */
	public int getPort() {
		return port;
	}
	
	/**
	 * 
	 * @return The name of the first waypoint that the robot should start at
	 * in the patrol route.
	 */
	public String getFirstWaypoint() {
		return firstWaypoint;
	}
	
	public String toString() {
		return name + ", " + ip + ", " + port + ", " + firstWaypoint;
	}
	
}
