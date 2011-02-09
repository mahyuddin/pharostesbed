package pharoslabut.beacon;

import java.net.InetAddress;

/**
 * This is the beacon periodically broadcasted by the BeaconBroadcaster.
 * It consists of the IP address and the port number of the server 
 * emitting the beacon.
 *
 * @author Chien-Liang Fok
 * @version 11/09/2010
 */
public class Beacon implements java.io.Serializable {
	private static final long serialVersionUID = 5257691949384329827L;

	/**
	 * The address of this host.
	 */
    private InetAddress address;
    
    /**
	 * The single-cast port number being used.
	 */
    private int port;
    
    /**
     * A unique sequence number.
     */
    private long seqNum = 0;
	
	/**
	 * Default constructor for the beacon.
	 */
	public Beacon(InetAddress address, int port) {
		this.address = address;
		this.port = port;
	}
	
	/**
	 * Increments the sequence number.
	 */
	public void incSeqNum() {
		seqNum++;
	}
	
	/**
	 * Returns an Array of Profiles, or null if there are no
	 * profiles stored in this beacon.
	 */
	public InetAddress getAddress() {
		return address;
	}
	
	public int getPort() {
		return port;
	}
	
	public long getSeqNum() {
		return seqNum;
	}
	
	/**
	 * Returns a String representation of this class.
	 *
	 * @return a String representation of this class.
	 */
	public String toString() {
		StringBuffer sBuff = new StringBuffer("(");
		sBuff.append(address.getHostAddress() + ":" + port + "," + seqNum + ")");
		return sBuff.toString();
	}
}
