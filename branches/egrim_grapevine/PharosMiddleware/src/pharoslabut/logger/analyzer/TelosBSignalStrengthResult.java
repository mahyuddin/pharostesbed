package pharoslabut.logger.analyzer;


public class TelosBSignalStrengthResult {
	private TelosBRxRecord rxRec;
	private double dist;
	
	public TelosBSignalStrengthResult(TelosBRxRecord rxRec, double dist) {
		this.rxRec = rxRec;
		this.dist = dist;
	}
	
	public TelosBRxRecord getRxRecord() {
		return rxRec;
	}
	
	public double getDist() {
		return dist;
	}
	
	public String getTableHeader() {
		return "Timestamp\tSenderID\tReceiverID\tSeqno\tdistance\tLQI\tRSSI";
	}
	
	public String toString() {
		return rxRec.getTimeStamp() + "\t" + rxRec.getSenderID() + "\t" + rxRec.getReceiverID() + "\t" + rxRec.getSeqNo() + "\t" 
		 + dist + "\t" + rxRec.getLQI() + "\t" + rxRec.getRSSI();
	}
}