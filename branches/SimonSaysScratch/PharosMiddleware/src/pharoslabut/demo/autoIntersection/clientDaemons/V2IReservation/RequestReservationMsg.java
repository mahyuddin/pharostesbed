package pharoslabut.demo.autoIntersection.clientDaemons.V2IReservation;

import java.net.InetAddress;

import pharoslabut.demo.autoIntersection.clientDaemons.V2I.RequestAccessMsg;

/**
 * This message is sent from the vehicle to the intersection server when
 * it approaches the intersection.  Its purpose is to request permission 
 * to cross the intersection.
 * 
 * @author Chien-Liang Fok
 */
public class RequestReservationMsg extends RequestAccessMsg {

	private static final long serialVersionUID = -5572596353050076559L;

	/**
	 * The amount of time in milliseconds it will take the sending vehicle
	 * to cross the intersection. 
	 */
	long timeToCross;
	
	/**
	 * The constructor.
	 * 
	 * @param robotIP The IP address of the robot.
	 * @param robotPort The port that the robot is listening to.
	 * @param entryPointID The entry point ID.
	 * @param exitPointID The exit point ID.
	 * @param timeToCross The amount of time in milliseconds it will take the sending vehicle
	 * to cross the intersection. 
	 */
	public RequestReservationMsg(InetAddress robotIP, int robotPort,
			String entryPointID, String exitPointID, long timeToCross) 
	{
		super(robotIP, robotPort, entryPointID, exitPointID);
		this.timeToCross = timeToCross;
	}
	
	public long getTimeToCross() {
		return timeToCross;
	}
	
	public String toString() {
		return super.toString() + ", timeToCross = " + timeToCross;
	}
}
