package pharoslabut.logger.analyzer;

/**
 * Records the reception of a WiFi beacon.
 * 
 * @author Chien-Liang Fok
 */
public class WiFiBeaconRx {
	long timestamp;
	int port;
	String IPaddress;
	int seqno;
	
	public WiFiBeaconRx(String IPaddress, int port, int seqno, long timestamp) {
		this.timestamp = timestamp;
		this.port = port;
		this.seqno = seqno;
		this.IPaddress = IPaddress;
	}
	
	public String getIPAddress() {
		return IPaddress;
	}
	
	public long getTimestamp() {
		return timestamp;
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
