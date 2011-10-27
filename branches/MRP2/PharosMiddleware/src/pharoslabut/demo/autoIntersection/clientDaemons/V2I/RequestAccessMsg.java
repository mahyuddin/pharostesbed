package pharoslabut.demo.autoIntersection.clientDaemons.V2I;

import java.net.InetAddress;

import pharoslabut.demo.autoIntersection.msgs.AutoIntersectionMsg;

/**
 * This message is sent from the vehicle to the intersection server when
 * it approaches the intersection.  Its purpose is to request permission 
 * to cross the intersection.
 * 
 * @author Chien-Liang Fok
 * @author Michael Hanna
 */
public class RequestAccessMsg extends AutoIntersectionMsg  {
	
	private static final long serialVersionUID = 1877893336642970534L;
	
	/**
	 * The entry point.
	 */
	private String entryPointID;
	
	/**
	 * The exit point.
	 */
	private String exitPointID;
	
	/**
	 * The constructor.
	 * 
	 * @param robotIP The IP address of the robot.
	 * @param robotPort The port that the robot is listening to.
	 * @param entryPointID The entry point ID.
	 * @param exitPointID The exit point ID.
	 */
	public RequestAccessMsg(InetAddress robotIP, int robotPort, String entryPointID, 
			String exitPointID) 
	{
		super(robotIP, robotPort);
		this.entryPointID = entryPointID;
		this.exitPointID = exitPointID;
	}
	
	/**
	 * 
	 * @return The entry point ID.
	 */
	public String getEntryPoint() {
		return entryPointID;
	}
	
	/**
	 * 
	 * @return The exit point ID.
	 */
	public String getExitPoint() {
		return exitPointID;
	}
	
	public String toString() {
		return getClass().getName() + ", " + super.toString() 
			+ ", entryPointID=" + entryPointID + ", exitPointID=" + exitPointID;
	}
}
