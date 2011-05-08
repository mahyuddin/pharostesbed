package playerclient2;

import playerclient2.structures.gps.PlayerGpsData;

/**
 * Defines the interface that all GPSListeners must implement.
 * 
 * @author Chien-Liang Fok
 */
public interface GPSListener {

	/**
	 * This is called by GPSInterface whenever a new GPS data arrives.
	 * 
	 * @param gpsData The GPS data that was received.
	 */
	public void newGPSData(PlayerGpsData gpsData);
}
