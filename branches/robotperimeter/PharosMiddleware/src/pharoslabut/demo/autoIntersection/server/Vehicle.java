package pharoslabut.demo.autoIntersection.server;

import java.net.*;

/**
 * Encapsulates the data for a specific vehicle that wants to
 * cross the intersection.
 * 
 * @author Michael Hanna
 * @author Chien-Liang Fok
 */
public class Vehicle implements java.io.Serializable {
	private static final long serialVersionUID = 1590099958920681215L;
	
	/**
	 * The IP address of the vehicle.
	 */
	private InetAddress ipAddress;
	
	/**
	 * The port on which the vehicle is listening.
	 */
    private int port;
    
    /**
     * The entry point of the vehicle.
     */
    private String entryPointID;
    
    /**
     * The exit point of the vehicle.
     */
    private String exitPointID;
    
    /**
     * The constructor.
     * 
     * @param ipAddress The IP address of the vehicle.
     * @param port The port on which the vehicle is listening.
     */
    public Vehicle(InetAddress ipAddress, int port) {
    	this.ipAddress = ipAddress;
        this.port = port;
    }
    		
    /**
     * The constructor.
     * 
     * @param ipAddress The IP address of the vehicle.
     * @param port The port on which the vehicle is listening.
     * @param entryPointID The entry point of the vehicle.
     * @param exitPointID The exit point of the vehicle.
     */
    public Vehicle(InetAddress ipAddress, int port, String entryPointID, String exitPointID) {
        this(ipAddress, port);
        this.entryPointID = entryPointID;
	    this.exitPointID = exitPointID;
    }

//    /**
//     * Class constructor
//     * 
//     * @param idAddress Robot's ID (IP address)
//     * @param port The port of the robot
//     * @param ETA Robot's estimated time of arrival (at the intersection)
//     */
//	public Vehicle(InetAddress ipAddress, int port) {
//		this(ipAddress, port, new LaneSpecs(), -1, -1);
//	}


	/**
     *  @return The IP address of the vehicle.
     */
    public InetAddress getIP() {
        return ipAddress;
    }
    
    /**
     * 
     * @return The port on which the vehicle is listening.
     */
    public int getPort() {
    	return port;
    }

    /**
     * 
     * @return The entry point of the vehicle.
     */
    public String getEntryPointID() {
    	return entryPointID;
    }
    
    /**
     * 
     * @return The exit point of the vehicle.
     */
    public String getExitPointID() {
    	return exitPointID;
    }
    
    /**
     * This overrides the equals() method
     * we only compare the robotIP and the robotPort
     */
    @Override
    public boolean equals(Object o) {
    	if(o == null)
    		return false;
    	if(!(o instanceof Vehicle))
    		return false;
    	
    	Vehicle robot = (Vehicle) o;
    	return getIP().equals(robot.getIP())  &&  getPort() == robot.getPort();
    }
    
    /**
     * This method overrides the toString() method
     * @return String with all the robot attributes
     */
    @Override
    public String toString() {
    	return getClass().getName() + ": ip=" + ipAddress + ", port=" + port 
    		+ ", entryPointID=" + entryPointID + ", exitPointID=" + exitPointID;
    }
}
