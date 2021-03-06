package pharoslabut.beacon;

import java.net.InetAddress;
import java.net.UnknownHostException;

import pharoslabut.RobotIPAssignments;

/**
 * This is the beacon periodically broadcasted by the BeaconBroadcaster.
 * It consists of the IP address and the port number of the server 
 * transmitting the beacon.
 *
 * @author Chien-Liang Fok
 * @version 02/26/2012
 */
public class WiFiBeacon implements java.io.Serializable {
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
     * The timestamp as recorded by the transmitter's system clock.
     */
    private long timestamp;
	
    /**
     * A default constructor for allowing subclasses to be serializable.
     */
    public WiFiBeacon() {	
    }
    
	/**
	 * Default constructor for the beacon.
	 * 
	 * @param address The address of this host.
	 * @param port The single-cast port number being used.
	 */
	public WiFiBeacon(InetAddress address, int port) {
		this.address = address;
		this.port = port;
	}
	
	/**
	 * This constructor is used during the analysis of log files.
	 * 
	 * @param address The address of this host.
	 * @param port The single-cast port number being used.
	 * @param seqNum The beacon's sequence number.
	 */
	public WiFiBeacon(String ipAddress, int port, long seqNum) {
		try {
			this.address = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}
		this.port = port;
		this.seqNum = seqNum;
	}
	
	/**
	 * 
	 * @return The timestamp.
	 */
	public long getTimestamp() {
		return timestamp;
	}
	
	/**
	 * Updates this beacon's timestamp.
	 */
	public void updateTimestamp() {
		timestamp = System.currentTimeMillis();
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
	
	/**
	 * Returns the ID of the sender, which is the last octal of the sender's ad hoc IP address.
	 * 
	 * @return the ID of the sender.
	 */
	public int getSenderID() {
		return RobotIPAssignments.getID(address);
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
		sBuff.append(address.getHostAddress() + ":" + port + "," + seqNum + ", " + timestamp + ")");
		return sBuff.toString();
	}
}
