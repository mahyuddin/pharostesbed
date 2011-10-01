package pharoslabut.demo.indoorMRPatrol;

import java.net.InetAddress;

public class RobotExpSettings implements java.io.Serializable {
	
	private static final long serialVersionUID = -6833303603496561692L;

	private String name;
	
	private InetAddress ip;
	
	private int port;
	
	private double startingLoc;
	
	public RobotExpSettings(String name, InetAddress ip, int port, double startingLoc) {
		this.name = name;
		this.ip = ip;
		this.port = port;
		this.startingLoc = startingLoc;
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
	
	public double getStartingLoc() {
		return startingLoc;
	}
	
	public String toString() {
		return name + ", " + ip + ", " + port + ", " + startingLoc;
	}
	
}
