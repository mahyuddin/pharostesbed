package pharoslabut.logger.analyzer;

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
		return timestamp + "\t" + senderID + "\t" + seqno;
	}
	
}
