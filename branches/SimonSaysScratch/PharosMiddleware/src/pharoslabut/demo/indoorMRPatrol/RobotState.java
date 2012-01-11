package pharoslabut.demo.indoorMRPatrol;

import java.net.InetAddress;

/**
 * Contains a robot's ID and its current state.
 * 
 * @author Chien-Liang Fok
 */
public class RobotState {
	
	/**
	 * The name of the robot.
	 */
	private String robotName;
	
	/**
	 * The IP address of the robot.
	 */
	private InetAddress robotIP;
	
	/**
	 * The starting location of the robot.  Location is defined as the distance from the
	 * beginning of the patrol route.
	 */
	private double startingLoc;
	
	/**
	 * The number of markers this robot has traversed.
	 */
	private int numMarkersTraversed = 0;
	
	/**
	 * The time when the robot was heard from.
	 */
	private long lastHeardTimeStamp = 0;
	
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
	
	/**
	 * Sets the number of markers this robot has traversed.
	 * @param numMarkersTraversed
	 */
	public void setNumMarkersTraversed(int numMarkersTraversed) {
		this.numMarkersTraversed = numMarkersTraversed;
	}
	
	/**
	 * 
	 * @return The number of markers traversed.
	 */
	public int getNumMarkersTraversed() {
		return numMarkersTraversed;
	}
	
	/**
	 * Sets the last heard time stamp to be the current time.
	 */
	public void setLastHeardTimeStamp() {
		lastHeardTimeStamp = System.currentTimeMillis();
	}
	
	/**
	 * @return The time since the last time this robot was heard from.
	 */
	public long getAge() {
		return System.currentTimeMillis() - lastHeardTimeStamp;
	}
	
	public String toString() {
		return "PatrolRobotID: name = " + robotName + ", IP = " + robotIP 
			+ ", startingLoc = " + startingLoc + ", age = " + getAge();
	}
}
