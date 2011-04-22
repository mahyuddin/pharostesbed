package pharoslabut.demo.autoIntersection.msgs;

import java.net.InetAddress;

import pharoslabut.io.Message.MsgType;

/**
 * Sent by the server to the robot assigning it the time at which it may
 * enter the intersection.
 * 
 * @author Michael Hanna
 */
public class ReservationTimeMsg extends AutoIntersectionMsg {
	
	private static final long serialVersionUID = 7593577761036988454L;
	private long ETA;
	
	public ReservationTimeMsg(InetAddress robotIP, int robotPort, long ETA) {
		super(robotIP, robotPort);
		this.ETA = ETA;
	}
	
	/**
     *  @return ETA
     */
    public long getETA() {
        return this.ETA;
    }
	
	/**
     *  this method sets a new ETA for the robot
     * @param eta - The estimated time of arrival
     */
    public void setETA(long eta) {
        this.ETA = eta;
//        this.ETA = ETA + new Date().getTime() - Main.startTime;
    }
	
	public String toString() {
		return "ReservationTimeMsg- " + "robotIP:" + this.getRobotIP().getHostAddress() + " robotPort:" + this.getRobotPort()
				+ " ETA:" + this.getETA();
	}

}
