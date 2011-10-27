package pharoslabut.demo.autoIntersection;

import java.net.InetAddress;

/**
 * The settings of a specific robot in the experiment.
 * 
 * @author Chien-Liang Fok
 */
public class RobotExpSettings implements java.io.Serializable {

	private static final long serialVersionUID = -2593885153382922030L;

	/**
	 * The name of the robot.
	 */
	private String name;
	
	/**
	 * The robot's IP address.
	 */
	private InetAddress ip;
	
	/**
	 * The TCP port on which the robot is listening.
	 */
	private int port;
	
	/**
	 * The robot's entry point into the intersection.
	 */
	private String entryPointID;
	
	/**
	 * The robot's exit point from the intersection.
	 */
	private String exitPointID;
	
	/**
	 * The constructor.
	 * 
	 * @param name The name of the robot.
	 * @param ip The IP address of the robot.
	 * @param port The port on which the robot's AutoIntersectionClient is listening.
	 * @param entryPointID The robot's entry point into the intersection.
	 * @param exitPointID The robot's point of exit from the intersection.
	 */
	public RobotExpSettings(String name, InetAddress ip, int port, String entryPointID,
			String exitPointID) 
	{
		this.name = name;
		this.ip = ip;
		this.port = port;
		this.entryPointID = entryPointID;
		this.exitPointID = exitPointID;
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
	
	public String getEntryPointID() {
		return entryPointID;
	}
	
	public String getExitPointID() {
		return exitPointID;
	}
	
	public String toString() {
		return name + ", " + ip + ", " + port + ", " + entryPointID + ", " + exitPointID;
	}
	
}
