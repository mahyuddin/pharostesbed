package pharoslabut.behavior;

import pharoslabut.logger.analyzer.TimeCalibrator;

/**
 * The top-level class of all behavior states.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.demo.mrpatrol.logAnalyzer.RobotMRPatrolExpData
 */
public class BehaviorState {
	protected int behaviorNumber = -1; // initialize to an invalid value
	protected long startTime = -1;     // initialize to an invalid value
	protected long stopTime = -1;      // initialize to an invalid value
	
	/**
	 * A default constructor that does not take any parameters.
	 * This is used by subclasses.
	 */
	public BehaviorState() {}
	
	public void setBehaviorNumber(int behaviorNumber) {
		this.behaviorNumber = behaviorNumber;
	}
	
	public void setBehaviorStartTime(long timestamp) {
		this.startTime = timestamp;
	}
	
	public void setBehaviorStopTime(long timestamp) {
		this.stopTime = timestamp;
	}
	
	public int getBehaviorNumber() {
		return behaviorNumber;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public long getStopTime() {
		return stopTime;
	}
	
	public long getDuration() {
		return stopTime - startTime;
	}
	
	public void calibrateTime(TimeCalibrator calibrator) {
		
		// Only calibrate the time if its valid!
		if (startTime != -1)
			this.startTime = calibrator.getCalibratedTime(startTime);
		if (stopTime != -1)
			this.stopTime = calibrator.getCalibratedTime(stopTime);
	}
	
	public String toString() {
		return "behaviorNumber = " + behaviorNumber + ", startTime = " + startTime + ", stopTime = " + stopTime + ", duration = " + getDuration();
	}
}
