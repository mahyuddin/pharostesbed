package pharoslabut.demo.mrpatrol2.msgs;

import java.net.InetAddress;
import pharoslabut.beacon.WiFiBeacon;
import pharoslabut.io.Message;

/**
 * This is the beacon periodically broadcasted during a multi-robot patrol 2 (MRP2) experiment.
 *
 * @author Chien-Liang Fok
 */
public class BeaconMsg extends WiFiBeacon implements Message {
	
	private static final long serialVersionUID = 2947617778419351120L;
	
	/**
	 * The number of waypoints this robot has traversed.
	 */
	private int numWaypointsTraversed;
	
//	/**
//	 * This will be set to true when the transmitter is anticipating
//	 * arrival at the next waypoint.
//	 */
//	private boolean anticipated;
	
	/**
	 * The constructor.
	 * 
	 * @param address The address of this host.
	 * @param port The single-cast port number being used.
	 */
	public BeaconMsg(InetAddress address, int port) {
		super(address, port);
	}
	
//	public void setAnticipated() {
//		anticipated = true;
//	}
//	
//	public void unsetAnticipated() {
//		anticipated = false;
//	}
//	
//	public boolean getAnticipated() {
//		return anticipated;
//	}
	
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
	
	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
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
		sBuff.append(", numWaypointsTraversed = " + numWaypointsTraversed);
//		sBuff.append(", anticipated = " + anticipated + ")");
		return sBuff.toString();
	}
}