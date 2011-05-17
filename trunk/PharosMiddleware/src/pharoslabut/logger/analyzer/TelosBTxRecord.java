package pharoslabut.logger.analyzer;

import pharoslabut.RobotIPAssignments;
import pharoslabut.exceptions.PharosException;

/**
 * This records each time the TelosB mote broadcasts a message.
 * 
 * @author Chien-Liang Fok
 */
public class TelosBTxRecord {

	long timestamp;
	int sndrID;
	int seqno;
	
	public TelosBTxRecord(long timestamp, int senderID, int seqno) {
		this.timestamp = timestamp;
		this.sndrID = senderID;
		this.seqno = seqno;
	}
	
	/**
	 * Recalibrates the time based on the GPS timestamps.
	 * 
	 * @param calibrator The time calibrator.
	 */
	public void calibrateTime(TimeCalibrator calibrator) {
		timestamp = calibrator.getCalibratedTime(timestamp);
	}
	
	public long getTimeStamp() {
		return timestamp;
	}
	
	public int getSenderID() {
		return sndrID;
	}
	
	public int getSeqNo() {
		return seqno;
	}
	
	public String toString() {
		String sndrName = null;
		try {
			sndrName = RobotIPAssignments.getRobotName(sndrID);
		} catch (PharosException e1) {
			logErr("Unable to get sender's name: " + sndrID);
			e1.printStackTrace();
		}
		
		return timestamp + "\t" + sndrID + " (" + sndrName + ")\t" + seqno;
	}
	
	private void logErr(String msg) {
		String result = "TelosBTxRecord: ERROR: " + msg;
		System.err.println(result);
//		if (flogger != null)
//			flogger.log(result);
	}
}
