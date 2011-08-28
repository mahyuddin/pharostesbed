package pharoslabut.logger.analyzer;

/**
 * Records the transmission of a WiFi beacon.
 * 
 * @author Chien-Liang Fok
 */
public class WiFiBeaconTx {

	private long timestamp;
	private String IPaddress;
	private int port;
	private int seqno;
	
	public WiFiBeaconTx(String IPaddress, int port, int seqno, long timestamp) {
		this.timestamp = timestamp;
		this.port = port;
		this.seqno = seqno;
		this.timestamp = timestamp;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public String getIPAddress() {
		return IPaddress;
	}
	
	public int getPort() {
		return port;
	}
	
	public int getSeqno() {
		return seqno;
	}
	
	public String toString() {
		return getClass().getName() + ": " + IPaddress + ", port = " + port + ", seqno = " + seqno + ", timestamp = " + timestamp;
	}
}
