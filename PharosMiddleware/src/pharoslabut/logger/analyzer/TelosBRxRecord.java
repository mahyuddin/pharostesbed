package pharoslabut.logger.analyzer;

import pharoslabut.RobotIPAssignments;

/**
 * Contains all of the information collected when a TelosB 802.15.4 wireless
 * broadcast is received.  
 * 
 * @author  Chien-Liang Fok
 */
public class TelosBRxRecord {
	long timestamp;
	int sndrID;
	int rcvrID;
	int seqno;
	int rssi;
	int lqi;
	int moteTimestamp;
	
	public TelosBRxRecord (long timestamp, int rcvrID, int sndrID, int seqno, 
			int rssi, int lqi, int moteTimestamp) 
	{
		this.timestamp = timestamp;
		this.sndrID = sndrID;
		this.rcvrID = rcvrID;
		this.seqno = seqno;
		this.rssi = rssi;
		this.lqi = lqi;
		this.moteTimestamp = moteTimestamp;
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
		return sndrID;
	}
	
	public int getReceiverID() {
		return rcvrID;
	}
	
	public int getSeqNo() {
		return seqno;
	}
	
	public int getLQI() {
		return lqi;
	}
	
	public int getRSSI() {
		return rssi;
	}
	
	public int getMoteTimestamp() {
		return moteTimestamp;
	}
	
	public String toString() {
		return timestamp + "\t" 
			+ sndrID + " (" + RobotIPAssignments.getRobotName(sndrID) + ")\t" 
			+ rcvrID + " (" + RobotIPAssignments.getRobotName(rcvrID) + ")\t" 
			+ seqno + "\t" + rssi + "\t" + lqi + "\t" + moteTimestamp;
	}
	
}
