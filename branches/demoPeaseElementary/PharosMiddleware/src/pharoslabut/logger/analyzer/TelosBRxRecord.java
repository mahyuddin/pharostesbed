package pharoslabut.logger.analyzer;

public class TelosBRxRecord {
	long timestamp;
	int sndrID;
	int rcvrID;
	int seqno;
	int rssi;
	int lqi;
	int moteTimestamp;
	
	public TelosBRxRecord (long timestamp, int sndrID, int rcvrID, int seqno, 
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
		return timestamp + "\t" + sndrID + "\t" + rcvrID + "\t" + seqno + "\t" + rssi + "\t" + lqi + "\t" + moteTimestamp;
	}
	
}
