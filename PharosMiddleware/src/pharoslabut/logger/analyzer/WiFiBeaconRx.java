package pharoslabut.logger.analyzer;

import pharoslabut.beacon.WiFiBeacon;

/**
 * Records the reception of a WiFi beacon.
 * 
 * @author Chien-Liang Fok
 */
public class WiFiBeaconRx extends WiFiBeaconTx {

	/**
	 * The constructor.
	 * 
	 * @param beacon The received beacon.
	 * @param timestamp The timestamp of the transmission.
	 */
	public WiFiBeaconRx(WiFiBeacon beacon, long timestamp) {
		super(beacon, timestamp);
	}
}
