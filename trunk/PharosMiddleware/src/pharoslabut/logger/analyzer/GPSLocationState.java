package pharoslabut.logger.analyzer;

import playerclient.structures.gps.PlayerGpsData;

import pharoslabut.navigate.*;

public class GPSLocationState {
	private PlayerGpsData gpsLoc;
	private long timeStamp;
	
	public GPSLocationState(long timeStamp, PlayerGpsData gpsLoc) {
		this.timeStamp = timeStamp;
		this.gpsLoc = gpsLoc;
	}
	
	public PlayerGpsData getLoc() {
		return gpsLoc;
	}
	
	public Location getLocation() {
		return new Location(gpsLoc);
	}

	/**
	 * Returns the local time stamp.  This is the value of the local
	 * system clock and obtained using System.currentTimeMillis().
	 *  
	 * @return The local time stamp.
	 */
	public long getTimeStamp() {
		return timeStamp;
	}
	
	public String toString() {
		return timeStamp + "\t" + gpsLoc;
	}
}
