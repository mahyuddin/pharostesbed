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
		
		// The incoming rssi as recorded by the log files is an 8-bit two's-complement integer.
		// Thus, convert this into a standard Java integer.
		// See: CC2420 datasheet: http://pharos.ece.utexas.edu/wiki/images/6/65/Cc2420-datasheet.pdf
		if ((rssi & 0x80) > 0) {
			// negative value, flip all the bits then add one
			int bitFlipped = rssi ^ 0xff;
			int addOne = bitFlipped + 1;
			this.rssi = -1 * addOne;
			
			// Debug output
//			System.out.println("TelosBRxRecord: raw RSSI = 0x" + Integer.toHexString(rssi) + "(" + rssi + ")"
//					+ ", bitFlipped = 0x" + Integer.toHexString(bitFlipped) + "(" + bitFlipped + ")"
//					+ ", addOne = 0x" + Integer.toHexString(addOne) + "(" + addOne + ")"
//					+ ", rssi = 0x" + Integer.toHexString(this.rssi) + "(" + this.rssi + ")");
		} else {
			// positive value, just leave as is
			this.rssi = rssi;
//			System.out.println("TelosBRxRecord: RSSI = 0x" + Integer.toHexString(rssi));
		}
		
		this.rssi = this.rssi - 45; // convert to dBm units.
//		System.out.println("TelosBRxRecord: RSSI = " + this.rssi + "dBm");
		this.lqi = lqi;
		this.moteTimestamp = moteTimestamp;
	}
	
//	public boolean hasValidRSSI() {
//		if (rssi >= 0)
//			return false;
//		else
//			return true;
//	}
	
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
