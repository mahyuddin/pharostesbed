package pharoslabut.demo.autoIntersection.msgs;

import java.net.InetAddress;
import pharoslabut.demo.autoIntersection.*;

/**
 * This message is sent from the robot to the intersection server when
 * it approaches the intersection.  It's purpose is to request permission 
 * to cross the intersection.
 * 
 * @author Chien-Liang Fok
 * @author Michael Hanna
 */
public class RequestAccessMsg extends AutoIntersectionMsg  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1877893336642970534L;
	
	private long ETA;
	private long ETC;
	private LaneSpecs laneSpecs;
	
	/**
	 * The constructor.
	 * This constructor contains the ETC, laneSpecs which are not currently used
	 * but will be used in the future
	 * 
	 * @param robotIP The IP address of the robot.
	 * @param robotPort The port that the robot is listening to.
	 * @param ETA The estimated time when the robot will enter the intersection.
	 * @param ETC The estimated time when the robot will exit the intersection.
	 * @param laneSpecs The lane specification, i.e., which lane the robot will be approaching from.
	 */
	public RequestAccessMsg(InetAddress robotIP, int robotPort, long ETA, long ETC, LaneSpecs laneSpecs) {
		super(robotIP, robotPort);
		this.ETC = ETC;
		this.ETA = ETA;
		this.laneSpecs = laneSpecs;
	}
	
	/**
     *  @return ETA
     */
    public long getETA() {
        return this.ETA;
    }
    
    /**
     *  @return ETC
     */
    public long getETC() {
        return this.ETC;
    }
    
    /**
     *  @return laneSpecs
     */
    public LaneSpecs getLaneSpecs() {
        return laneSpecs;
    }

    /**
     *  this method sets a new ETA for the robot
     * @param eta - The estimated time of arrival
     */
    public void setETA(long eta) {
        this.ETA = eta;
//        this.ETA = ETA + new Date().getTime() - Main.startTime;
    }

    /**
     *  this method sets a new ETC for the robot
     * @param etc The estimated time of clearance
     */
    public void setETC(long etc) {
        this.ETC = etc;
//        this.ETC = etc + new Date().getTime() - Main.startTime;
    }
    
    public void setLaneSpecs(LaneSpecs specs) {
        this.laneSpecs = specs;
    }
	
	public String toString() {
		return "RequestAccessMsg- " + "robotIP:" + getRobotIP().getHostAddress()
				+ " robotPort:" + getRobotPort() + " ETA:" + getETA()
				+ " ETC:" + getETC() + " laneSpecs:" + getLaneSpecs();
	}
}
