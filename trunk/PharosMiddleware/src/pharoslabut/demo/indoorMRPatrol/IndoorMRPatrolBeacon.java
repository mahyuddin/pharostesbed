package pharoslabut.demo.indoorMRPatrol;

import java.net.InetAddress;
import pharoslabut.beacon.WiFiBeacon;

/**
 * This is the beacon periodically broadcasted during the indoor multi-robot
 * patrol application.
 *
 * @author Chien-Liang Fok
 */
public class IndoorMRPatrolBeacon extends WiFiBeacon {

	private static final long serialVersionUID = -906967090637308191L;

	/**
	 * The number of markers this robot has traversed.
	 */
	private int numMarkersTraversed;
	
	/**
	 * Default constructor.
	 */
	public IndoorMRPatrolBeacon() {	
	}
	
	/**
	 * The constructor.
	 * 
	 * @param address
	 * @param port
	 */
	public IndoorMRPatrolBeacon(InetAddress address, int port) {
		super(address, port);
	}
	
	/**
	 * Sets the number of markers traversed.
	 * 
	 * @param numMarkersTraversed the number of markers traversed.
	 */
	public void setNumMarkersTraversed(int numMarkersTraversed) {
		this.numMarkersTraversed = numMarkersTraversed;
	}
	
	/**
	 * @return the number of markers traversed.
	 */
	public int getNumMarkersTraversed() {
		return numMarkersTraversed;
	}
	
	/**
	 * Returns a String representation of this class.
	 *
	 * @return a String representation of this class.
	 */
	public String toString() {
		StringBuffer sBuff = new StringBuffer("(");
		sBuff.append(getAddress().getHostAddress() + ":" + getPort() 
				+ ", " + getSeqNum() + ", " + numMarkersTraversed + ")");
		return sBuff.toString();
	}	
}
