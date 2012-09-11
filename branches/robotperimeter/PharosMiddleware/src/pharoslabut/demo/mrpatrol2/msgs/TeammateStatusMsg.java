package pharoslabut.demo.mrpatrol2.msgs;

import pharoslabut.io.Message;

/**
 * This message contains the status of a single robot.
 * 
 * @author Chien-Liang Fok
 */
public class TeammateStatusMsg implements Message {
	
	private static final long serialVersionUID = 8908355977401548135L;
	private String name;
	private int numWaypointsVisited;
	
	/**
	 * The constructor.
	 * 
	 * @param name The name of the robot that transmitted this message.
	 * @param numWaypointsVisited The number of waypoints the robot has visited.
	 */
	public TeammateStatusMsg(String name, int numWaypointsVisited) {
		this.name = name;
		this.numWaypointsVisited = numWaypointsVisited;
	}
	
	/**
	 * 
	 * @return The name of the robot that transmitted this message.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 
	 * @return The number of waypoints the robot has visited.
	 */
	public int getNumWaypointsVisited() {
		return numWaypointsVisited;
	}
	
	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
	}
	
	public String toString() {
		return getClass().getName() + ", name = " + name + ", numWaypointsVisited = " + numWaypointsVisited;
	}

}
