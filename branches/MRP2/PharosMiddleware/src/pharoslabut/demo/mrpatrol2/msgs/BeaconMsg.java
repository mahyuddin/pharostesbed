package pharoslabut.demo.mrpatrol2.msgs;

import java.net.InetAddress;
import pharoslabut.beacon.WiFiBeacon;

/**
 * This is the beacon periodically broadcasted during a multi-robot patrol 2 (MRP2) experiment.
 *
 * @author Chien-Liang Fok
 */
public class BeaconMsg extends WiFiBeacon {
	
	private static final long serialVersionUID = 2947617778419351120L;
	
	/**
	 * The number of waypoints this robot has traversed.
	 */
	private int numWaypointsTraversed;
	
	/**
	 * The constructor.
	 * 
	 * @param address The address of this host.
	 * @param port The single-cast port number being used.
	 */
	public BeaconMsg(InetAddress address, int port) {
		super(address, port);
	}
	
	/**
	 * Sets the number of markers traversed.
	 * 
	 * @param numMarkersTraversed The number of markers traversed.
	 */
	public void setWaypointsTraversed(int numWaypointsTraversed) {
		this.numWaypointsTraversed = numWaypointsTraversed;
	}
	
	/**
	 * @return the number of markers traversed.
	 */
	public int getNumWaypointsTraversed() {
		return numWaypointsTraversed;
	}
	
	/**
	 * Returns a String representation of this class.
	 *
	 * @return a String representation of this class.
	 */
	public String toString() {
		StringBuffer sBuff = new StringBuffer("(");
		sBuff.append(getAddress().getHostAddress() + ":" + getPort()); 
		sBuff.append(", seqno = " + getSeqNum());
		sBuff.append(", timestamp = " + getTimestamp()); 
		sBuff.append(", numWaypointsTraversed = " + numWaypointsTraversed + ")");
		return sBuff.toString();
	}	
}