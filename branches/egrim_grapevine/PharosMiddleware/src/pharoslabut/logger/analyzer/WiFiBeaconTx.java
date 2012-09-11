package pharoslabut.logger.analyzer;

import pharoslabut.beacon.WiFiBeacon;

/**
 * Records the transmission of a WiFi beacon.
 * 
 * @author Chien-Liang Fok
 */
public class WiFiBeaconTx {

	private long timestamp;
	private WiFiBeacon beacon;
	
	/**
	 * The constructor.
	 * 
	 * @param IPaddress The IP address of the sender.
	 * @param port The multicast port through which the beacon was sent.
	 * @param seqno The sequence number of the beacon.
	 * @param timestamp The timestamp of the transmission.
	 */
	public WiFiBeaconTx(WiFiBeacon beacon, long timestamp) {
		this.beacon = beacon;
		this.timestamp = timestamp;
		
	}
	
	/**
	 * 
	 * @return the ID of the sender. 
	 */
	public int getSenderID() {
		return beacon.getSenderID();
	}
	
	/**
	 * 
	 * @return The timestamp of the transmission.
	 */
	public long getTimestamp() {
		return timestamp;
	}
	
	/**
	 * 
	 * @return The IP address of the sender.
	 */
	public WiFiBeacon getBeacon() {
		return beacon;
	}
	
	/**
	 * Recalibrates the time based on the GPS timestamps.
	 * 
	 * @param calibrator The time calibrator.
	 */
	public void calibrateTime(TimeCalibrator calibrator) {
		timestamp = calibrator.getCalibratedTime(timestamp);
	}
}
