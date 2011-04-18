package pharoslabut.demo.autoIntersection.server;

/**
 * Creates a new Object Robot
 * @author Michael Hanna
 */
public class Robot implements java.io.Serializable {
    private int id;
    private String laneSpecs;
    private long ETA;
    private long ETC;
    private boolean enqueued;
    private boolean acknowledged;
    private boolean exited;

    /**
     * Class constructor
     * @param id Robot's ID (IP address)
     * @param laneSpecs Robot's lane specification (direction, path, lane Number)
     * @param ETA Robot's estimated time of arrival (at the intersection)
     * @param ETC Robot's estimated time of clearance (after crossing the intersection)
     */
    public Robot(int id, String laneSpecs, long ETA /*, long ETC*/) {
        this.id = id;
        this.laneSpecs = laneSpecs;
        this.ETA = ETA;
        //this.ETC = ETC;
        this.enqueued = false;
        this.acknowledged = false;
        this.exited = false;
    }

    /**
     * Class constructor
     * @param id Robot's ID (IP address)
     * Class constructed to handle robot exiting intersection.
     */
	public Robot(int id) {
		// TODO Auto-generated constructor stub
		this.id = id;
        this.laneSpecs = "exit";
        this.ETA = 0;
		this.enqueued = false;
        this.acknowledged = false;
        this.exited = true;
	}


	/**
     *  @return id
     */
    public int getID() {
        return this.id;
    }

    /**
     *  @return laneSpecs
     */
    public String getLaneSpecs() {
        return this.laneSpecs;
    }

    /**
     *  @return ETA
     */
    public long getETA() {
        return this.ETA;
    }

    /**
     *  this method sets a new ETA for the robot
     * @param eta The estimated time of arrival
     */
    public void setETA(long eta) {
        this.ETA = eta;
//        this.ETA = eta + new Date().getTime() - Main.startTime;
    }

    /**
     *  @return ETC
     */
    public long getETC() {
        return this.ETC;
    }

    /**
     *  this method sets a new ETC for the robot
     * @param etc The estimated time of clearance
     */
    public void setETC(long etc) {
        this.ETC = etc;
//        this.ETC = etc + new Date().getTime() - Main.startTime;
    }

    /**
     * This method checks if the robot request is on the queue
     *  @return true if the robot request is on the queue, false otherwise
     */
    public boolean isEnqueued() {
        return this.enqueued;
    }

    /**
     *  This method sets the boolean variable enqueued to b
     * @param b
     */
    public void setEnqueued(boolean b) {
        this.enqueued = b;
    }


    /**
     * This method checks if the robot has exited the intersection
     * It is being used as a confirmation that everything is running as expected
     *  @return true if the robot has exited the intersection, false otherwise
     */
    public boolean isExited() {
        return this.exited;
    }

    /**
     *  This method sets the boolean variable exited to b
     * @param b
     */
    public void setExited(boolean b) {
        this.exited = b;
    }

    /**
     * This method checks if the client(Seth) has received the object robot after setting its ETA and ETC
     * It is being used as an acknowledgment to make sure there is no packet loss
     * If no acknowledgment is received from the client side, I will keep sending the object robot
     *  @return true if an acknowledgment is received from the client saying that it received the message, false otherwise
     */
    public boolean isAcknowledged() {
        return this.acknowledged;
    }

    /**
     *  This method sets the boolean variable exited to b
     * @param b
     */
    public void setAcknowledged(boolean b) {
        this.acknowledged = b;
    }


    /**
     * This method overrides the toString() method
     * @return String with all the robot attributes
     */
    @Override
    public String toString()
    {
        String output = "id : " + id + "\n";
        output += "laneSpecs : " + laneSpecs + "\n";
        output += "ETA : " + ETA + "\n";
        output += "ETC : " + ETC + "\n";
        output += "enqueued : " + enqueued + "\n";
        output += "exited : " + exited + "\n";
        output += "acknowledged : " + acknowledged + "\n";
        return output;
    }

}
