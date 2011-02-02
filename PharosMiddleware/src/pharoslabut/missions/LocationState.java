package pharoslabut.missions;

import pharoslabut.navigate.*;

/**
 * Maps a time stamp to a location.
 * 
 * @author Chien-Liang Fok
 */
public class LocationState {
	private long timeStamp;
	private Location loc;
	
	public LocationState(long timeStamp, Location loc) {
		this.timeStamp = timeStamp;
		this.loc = loc;
	}
	
	public Location getLoc() {
		return loc;
	}
	
	public long getTimeStamp() {
		return timeStamp;
	}
	
	public String toString() {
		return timeStamp + "\t" + loc;
	}
}
