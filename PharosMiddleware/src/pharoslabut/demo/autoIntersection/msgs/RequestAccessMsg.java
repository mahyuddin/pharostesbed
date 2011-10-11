package pharoslabut.demo.autoIntersection.msgs;

import java.net.InetAddress;

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
	
//	/**
//	 * The estimated time of entry into the intersection.
//	 */
//	private long entraceTime;
//	
//	/**
//	 * The estimated time of exit from the intersection.
//	 */
//	private long exitTime;
	
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
	
//	/**
//     *  @return ETA
//     */
//    public long getETA() {
//        return this.ETA;
//    }
//    
//    /**
//     *  @return ETC
//     */
//    public long getETC() {
//        return this.ETC;
//    }
    
//    /**
//     *  @return laneSpecs
//     */
//    public LaneSpecs getLaneSpecs() {
//        return laneSpecs;
//    }

//    /**
//     *  this method sets a new ETA for the robot
//     * @param eta - The estimated time of arrival
//     */
//    public void setETA(long eta) {
//        this.ETA = eta;
////        this.ETA = ETA + new Date().getTime() - Main.startTime;
//    }
//
//    /**
//     *  this method sets a new ETC for the robot
//     * @param etc The estimated time of clearance
//     */
//    public void setETC(long etc) {
//        this.ETC = etc;
////        this.ETC = etc + new Date().getTime() - Main.startTime;
//    }
    
//    public void setLaneSpecs(LaneSpecs specs) {
//        this.laneSpecs = specs;
//    }
	
	public String toString() {
		return getClass().getName() + ", " + super.toString() 
			+ ", entryPointID=" + entryPointID + ", exitPointID=" + exitPointID;
	}
}
