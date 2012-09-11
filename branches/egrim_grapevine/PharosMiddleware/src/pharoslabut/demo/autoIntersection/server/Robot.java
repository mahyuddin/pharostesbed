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
     *  @return ETC
     */
    public long getETC() {
        return this.ETC;
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
     *  this method sets a new ETC for the robot
     * @param etc The estimated time of clearance
     */
    public void setETC(long etc) {
        this.ETC = etc;
//        this.ETC = etc + new Date().getTime() - Main.startTime;
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
        output += "\t ETC : " + ETC;
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
