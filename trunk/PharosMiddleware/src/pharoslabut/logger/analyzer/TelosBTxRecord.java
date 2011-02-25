package pharoslabut.logger.analyzer;

import pharoslabut.RobotIPAssignments;

/**
 * This records each time the TelosB mote broadcasts a message.
 * 
 * @author Chien-Liang Fok
 */
public class TelosBTxRecord {

	long timestamp;
	int senderID;
	int seqno;
	
	public TelosBTxRecord(long timestamp, int senderID, int seqno) {
		this.timestamp = timestamp;
		this.senderID = senderID;
		this.seqno = seqno;
	}
	
	/**
	 * Recalibrates the time based on the GPS timestamps.
	 * 
	 * @param timeOffset The offset between the system time and GPS time,
	 * accurate to within a second.
	 */
	public void calibrateTime(double timeOffset) {
		timestamp = RobotExpData.getCalibratedTime(timestamp, timeOffset);
	}
	
	public long getTimeStamp() {
		return timestamp;
	}
	
	public int getSenderID() {
		return senderID;
	}
	
	public int getSeqNo() {
		return seqno;
	}
	
	public String toString() {
		return timestamp + "\t" + senderID + " (" + RobotIPAssignments.getRobotName(senderID) + ")\t" + seqno;
	}
	
}
