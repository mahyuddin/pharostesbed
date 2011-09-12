package pharoslabut.demo.mrpatrol.logAnalyzer;

import pharoslabut.logger.analyzer.TimeCalibrator;

/**
 * Records when a coordination message is received.
 * 
 * @author Chien-Liang Fok
 */
public class CoordMsgRxEvent {

	private long timestamp;
	private String behName;
	private int behID;
	private int robotID;
	
	public CoordMsgRxEvent(long timestamp, String behName, int behID, int robotID) {
		this.timestamp = timestamp;
		this.behID = behID;
		this.robotID = robotID;
	}
	
	public void calibrateTime(TimeCalibrator calibrator) {
		this.timestamp = calibrator.getCalibratedTime(timestamp);
	}
	
	public String toString() {
		return timestamp + "\t" + behName + "\t" + behID + "\t" + robotID;
	}
}
