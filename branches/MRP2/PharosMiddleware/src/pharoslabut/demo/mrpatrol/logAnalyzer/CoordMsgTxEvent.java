package pharoslabut.demo.mrpatrol.logAnalyzer;

import pharoslabut.logger.analyzer.TimeCalibrator;

/**
 * Records when a coordination message is transmitted.
 * 
 * @author Chien-Liang Fok
 */
public class CoordMsgTxEvent {

	private long timestamp;
	
	private String destIP;
	
	private int destPort;
	
	/**
	 * 
	 * @param timestamp
	 * @param destIP
	 * @param destPort
	 */
	public CoordMsgTxEvent(long timestamp, String destIP, int destPort) {
		this.timestamp = timestamp;
		this.destIP = destIP;
		this.destPort = destPort;
	}
	
	public void calibrateTime(TimeCalibrator calibrator) {
		this.timestamp = calibrator.getCalibratedTime(timestamp);
	}
	
	public String toString() {
		return timestamp + "\t" + destIP + "\t" + destPort;
	}
}
