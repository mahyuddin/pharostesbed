package pharoslabut.demo.indoorMRPatrol;

import java.net.InetAddress;

import pharoslabut.navigate.Location;

/**
 * Contains a robot's ID and its current state.
 * 
 * @author Chien-Liang Fok
 */
public class RobotState {

	
	private String robotName;
	
	private InetAddress robotIP;
	
	private double startingLoc;
	
	/**
	 * The constructor.
	 * 
	 * @param robotName The name of the robot.
	 * @param robotIP The IP address of the robot.
	 * @param staringLoc The starting location of the robot.  Location is defined as the distance from the
	 * beginning of the patrol route.
	 */
	public RobotState(String robotName, InetAddress robotIP, double startingLoc) {
		this.robotName = robotName;
		this.robotIP = robotIP;
		this.startingLoc = startingLoc;
	}
	
	public String getRobotName() {
		return robotName;
	}
	
	public InetAddress getRobotIP() {
		return robotIP;
	}
	
	public double getStartingLoc() {
		return startingLoc;
	}
	
	public String toString() {
		return "PatrolRobotID: " + robotName + ", " + robotIP + ", " + startingLoc;
	}
}
