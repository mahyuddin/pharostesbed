package pharoslabut.missions;

/**
 * Maps a time stamp to a heading.
 * 
 * @author Chien-Liang Fok
 */
public class HeadingState {
	private long timeStamp;
	private double heading;
	
	public HeadingState(long timeStamp, double heading) {
		this.timeStamp = timeStamp;
		this.heading = heading;
	}
	
	public long getTimeStamp() {
		return timeStamp;
	}
	
	public double getHeading() {
		return heading;
	}
	
	public String toString() {
		return timeStamp + "\t" + heading;
	}
}