package pharoslabut.demo.autoIntersection.server;

import pharoslabut.demo.autoIntersection.*;
import java.net.*;

/**
 * Encapsulates the data for a robot.
 * 
 * @author Michael Hanna
 * @author Chien-Liang Fok
 */
public class Robot implements java.io.Serializable {
	private static final long serialVersionUID = 1590099958920681215L;
	private InetAddress ipAddress;
    private int port;
    
    private LaneSpecs laneSpecs;
    private long ETA;
    private long ETC;
    private boolean enqueued;
    private boolean acknowledged;
    private boolean exited;

    /**
     * Class constructor.
     * 
     * @param idAddress Robot's ID (IP address)
     * @param port The port of the robot
     * @param laneSpecs Robot's lane specification (direction, path, lane Number)
     * @param ETA Robot's estimated time of arrival (at the intersection)
     * @param ETC Robot's estimated time of clearance (after crossing the intersection)
     */
    public Robot(InetAddress ipAddress, int port, LaneSpecs laneSpecs, long ETA, long ETC) {
        this.ipAddress = ipAddress;
        this.port = port;
        
        this.laneSpecs = laneSpecs;
        this.ETA = ETA;
        this.ETC = ETC;
        this.enqueued = false;
        this.acknowledged = false;
        this.exited = false;
    }

    /**
     * Class constructor
     * 
     * @param idAddress Robot's ID (IP address)
     * @param port The port of the robot
     * @param ETA Robot's estimated time of arrival (at the intersection)
     */
	public Robot(InetAddress ipAddress, int port) {
		this(ipAddress, port, new LaneSpecs(), -1, -1);
	}


	/**
     *  @return id
     */
    public InetAddress getIP() {
        return ipAddress;
    }
    
    public int getPort() {
    	return port;
    }

    /**
     *  @return laneSpecs
     */
    public LaneSpecs getLaneSpecs() {
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
        String output = "\t Address : " + ipAddress + "\t" + "Port: " + port + "\n";
        output += "\t laneSpecs : " + laneSpecs + "\n";
        output += "\t ETA : " + ETA + "\n";
        output += "\t ETC : " + ETC + "\n";
        output += "\t enqueued : " + enqueued + "\n";
        output += "\t exited : " + exited + "\n";
        output += "\t acknowledged : " + acknowledged + "\n";
        return output;
    }
    
    /**
     * This overrides the equals() method
     * we only compare the robotIP and the robotPort
     */
    @Override
    public boolean equals(Object o)
    {
    	if(o == null)
    		return false;
    	if(! (o instanceof Robot) )
    		return false;
    	
    	Robot robot = (Robot) o;
    	return this.getIP().equals(robot.getIP())  &&  this.getPort()==robot.getPort();
    }

}
