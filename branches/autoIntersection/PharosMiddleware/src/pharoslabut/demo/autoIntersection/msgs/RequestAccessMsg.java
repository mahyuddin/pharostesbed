package pharoslabut.demo.autoIntersection.msgs;

/**
 * This message is sent from the robot to the intersection server when
 * it approaches the intersection.  It's purpose is to request permission 
 * to cross the intersection.
 * 
 * @author Chien-Liang Fok
 * @author Michael Hanna
 */
public class RequestAccessMsg extends AutoIntersectionMsg  {

	private static final long serialVersionUID = -3642519285338558989L;
	private long ETA;
	private long ETC;
	private String laneSpecs;
	
	/**
	 * The constructor.
	 * This constructor contains the ETC, laneSpecs which are not currently used
	 * but will be used in the future
	 */
	public RequestAccessMsg(int robotID, long ETA, long ETC, String laneSpecs) {
		super(robotID);
		this.ETC = ETC;
		this.ETA = ETA;
		this.laneSpecs = laneSpecs;
	}
	
	/*
	 * Another constructor
	 * without the ETC or the laneSpecs
	 */
	public RequestAccessMsg(int robotID, long ETA) {
		super(robotID);
		this.ETA = ETA;
		ETC = -1;
		laneSpecs = null;
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
    public String getLaneSpecs() {
        return this.laneSpecs;
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
    
    public void setLaneSpecs(String specs) {
        this.laneSpecs = specs;
//        this.ETA = eta + new Date().getTime() - Main.startTime;
    }
	
	public String toString() {
		return "RequestAccessMsg";
	}
}
