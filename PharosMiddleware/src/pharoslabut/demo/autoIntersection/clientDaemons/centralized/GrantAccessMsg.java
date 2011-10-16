package pharoslabut.demo.autoIntersection.clientDaemons.centralized;

import java.net.InetAddress;

import pharoslabut.demo.autoIntersection.msgs.AutoIntersectionMsg;

/**
 * Sent by the server to the robot assigning it the time at which it may
 * enter the intersection.
 * 
 * @author Chien-Liang Fok
 * @author Michael Hanna
 */
public class GrantAccessMsg extends AutoIntersectionMsg {
	
	private static final long serialVersionUID = 7593577761036988454L;
	
	/**
	 * The constructor.
	 * 
	 * @param ip The IP address of the vehicle.
	 * @param port The port on which the vehicle is listening.
	 */
	public GrantAccessMsg(InetAddress robotIP, int robotPort) {
		super(robotIP, robotPort);
	}
	
	public String toString() {
		return getClass().getName() + ": " + super.toString();
	}
}
