package pharoslabut.missions;

/**
 * Maps a time stamp to a heading.
 * 
 * @author Chien-Liang Fok
 */
public class MotionArbiterState {
	private long timeStamp;
	private double speedCmd;
	private double steeringCmd;
	
	public MotionArbiterState(long timeStamp, double speedCmd, double steeringCmd) {
		this.timeStamp = timeStamp;
		this.speedCmd = speedCmd;
		this.steeringCmd = steeringCmd;
	}
	
	public long getTimeStamp() {
		return timeStamp;
	}
	
	public double getSpeedCmd() {
		return speedCmd;
	}
	public double getSteeringCmd() {
		return steeringCmd;
	}
	
	public String toString() {
		return timeStamp + "\t" + speedCmd + "\t" + steeringCmd;
	}
}