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

	private String name;
	
	private InetAddress ip;
	
	private int port;
	
	private String firstWaypoint;
	
	public RobotExpSettings(String name, InetAddress ip, int port, String firstWaypoint) {
		this.name = name;
		this.ip = ip;
		this.port = port;
		this.firstWaypoint = firstWaypoint;
	}
	
	public String getName() {
		return name;
	}
	
	public InetAddress getIP() {
		return ip;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getFirstWaypoint() {
		return firstWaypoint;
	}
	
	public String toString() {
		return name + ", " + ip + ", " + port + ", " + firstWaypoint;
	}
	
}
