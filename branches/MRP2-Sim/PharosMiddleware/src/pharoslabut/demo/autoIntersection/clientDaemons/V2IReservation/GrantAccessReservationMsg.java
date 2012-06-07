package pharoslabut.demo.autoIntersection.clientDaemons.V2IReservation;

import java.net.InetAddress;

import pharoslabut.demo.autoIntersection.clientDaemons.V2I.GrantAccessMsg;

/**
 * Sent by the server to the robot assigning it a time in the future when it may
 * enter the intersection.
 * 
 * @author Chien-Liang Fok
 */
public class GrantAccessReservationMsg extends GrantAccessMsg {
	
	private static final long serialVersionUID = -8445310513843598319L;
	
	private long reservationTime;
	
	/**
	 * The constructor.
	 * 
	 * @param robotIP The IP address of the vehicle.
	 * @param robotPort The port of the vehicle.
	 * @param reservationTime The time in the future when the vehicle is allowed to enter the intersection.
	 */
	public GrantAccessReservationMsg(InetAddress robotIP, int robotPort, long reservationTime) {
		super(robotIP, robotPort);
		this.reservationTime = reservationTime;
	}
	
	public long getReservationTime() {
		return reservationTime;
	}
	
	public String toString() {
		return getClass().getName() + ", reservationTime = " + reservationTime;
	}
}
