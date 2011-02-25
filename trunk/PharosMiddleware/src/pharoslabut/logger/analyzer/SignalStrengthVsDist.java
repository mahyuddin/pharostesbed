package pharoslabut.logger.analyzer;

import java.util.Enumeration;
import java.util.Vector;
import pharoslabut.logger.*;
import pharoslabut.navigate.*;
/**
 * This analyzes the log files from and experiment and extracts the signal
 * strength and distance data.
 * 
 * It produces a log file with the following format
 * 
 * @author Chien-Liang Fok
 */
public class SignalStrengthVsDist {
	public static final int TELOSB_RXTX_MAX_TIME_DIFF = 3000;
	
	private FileLogger flogger = null;
	
	/**
	 * The constructor.
	 * 
	 * @param analyzeTelos Whether to analyze the TelosB wireless data.
	 * @param expDir The directory containing the data from the experiment.
	 * @param logFile The name of the log file, may be null.
	 */
	public SignalStrengthVsDist(boolean analyzeTelos, String expDir, String logFile) {
		if (logFile != null)
			flogger = new FileLogger(logFile, false);
		
		ExpData expData = new ExpData(expDir);
		
		if (analyzeTelos) {
			log("Analyzing TelosB signal vs. distance...");
			analyzeTelosBSignal(expData);
		}
	}
	
	/**
	 * Analyzes the TelosB signal vs. distance.
	 * 
	 * @param expData The experiment data.
	 */
	private void analyzeTelosBSignal(ExpData expData) {
		
		// This is for storing the results of the signal strength measurements.
		Vector<TelosBSignalStrengthResult> results = new Vector<TelosBSignalStrengthResult>();
		
		// For each robot in the experiment...
		Enumeration<RobotExpData> robotEnum = expData.getRobotEnum();
		while (robotEnum.hasMoreElements()) {
			RobotExpData currTxRobot = robotEnum.nextElement();
			Vector<TelosBTxRecord> txRecord = currTxRobot.getTelosBTxHist();
			
			log("Analyzing transmissions of robot " + currTxRobot.getRobotName() + " (tx count = " + txRecord.size() + ")");
			
			// For each Tx performed by the robot...
			Enumeration<TelosBTxRecord> txEnum = txRecord.elements();
			while(txEnum.hasMoreElements()) {
				TelosBTxRecord currTxRec = txEnum.nextElement();
				
				// Get the time of transmission and the location of the transmitter at that time...
				long txTimestamp = currTxRec.getTimeStamp();
				Location txLoc = currTxRobot.getLocation(txTimestamp);
				if (txLoc == null) {
					logErr("ERROR: Unable to locate transmitter at time " + txTimestamp + ", fileName: " 
							+ currTxRobot.getFileName() + ", robotExpData:\n" + currTxRobot);
					System.exit(1);
				}
				
				// Find every robot that received the transmission
				Vector<RobotExpData> rcvrs = expData.getTelosBRxNodes(currTxRec.getSenderID(), currTxRec.getSeqNo());
				log("Analyzing transmission [" + currTxRec + "], robot at " + txLoc + ", received by " + rcvrs.size() + " node(s)");
				
				// For each receiver...
				Enumeration<RobotExpData> rcvrEnum = rcvrs.elements();
				while (rcvrEnum.hasMoreElements()) {
					RobotExpData currRxRobot = rcvrEnum.nextElement();
					TelosBRxRecord currRxRec = currRxRobot.getTelosBRx(currTxRec.getSenderID(), currTxRec.getSeqNo());
					
					// Do a sanity check to make sure reception is correct.
					// The senderID and sequence numbers must match.
					if (currRxRec.getSenderID() != currTxRec.getSenderID() || currRxRec.getSeqNo() != currTxRec.getSeqNo()) {
						logErr("ERROR: sender ID or sequence number mismatch:\n\tcurrTxRec = [" + currTxRec + "]\n\tcurrRxRec = [" + currRxRec + "]");
						System.exit(1);
					}
					
					long rxTimestamp = currRxRec.getTimeStamp();
					Location rxLoc = currRxRobot.getLocation(rxTimestamp);
					log("\tReception [" + currRxRec + "], robot at " + rxLoc);
					
					// Verify that the timestamps are correctly calibrated...
					if (Math.abs(txTimestamp - rxTimestamp) > TELOSB_RXTX_MAX_TIME_DIFF) {
						logErr("WARNING: Tx and Rx timestamps differ (" + txTimestamp + " vs. " 
								+ rxTimestamp + ", diff = " + Math.abs(txTimestamp - rxTimestamp) / 1000L + " seconds), rxRecord = [" + currRxRec + "], txRecord = [" + currTxRec + "]");
					}
					
					// Calculate the distance between the sender and receiver...
					double distance = txLoc.distanceTo(rxLoc);
					
					// Save the results...
					TelosBSignalStrengthResult result = new TelosBSignalStrengthResult(currRxRec, distance);
					results.add(result);
				}
			}
		}
		
		// print the results
		log(results.get(0).getTableHeader());
		for (int i=0; i < results.size(); i++) {
			log(results.get(i).toString());
		}
	}
	
	private class TelosBSignalStrengthResult {
		TelosBRxRecord rxRec;
		double dist;
		
		public TelosBSignalStrengthResult(TelosBRxRecord rxRec, double dist) {
			this.rxRec = rxRec;
			this.dist = dist;
		}
		
		public String getTableHeader() {
			return "Timestamp\tSenderID\tReceiverID\tSeqno\tdistance\tLQI\tRSSI";
		}
		
		public String toString() {
			return rxRec.getTimeStamp() + "\t" + rxRec.getSenderID() + "\t" + rxRec.getReceiverID() + "\t" + rxRec.getSeqNo() + "\t" 
			 + dist + "\t" + rxRec.getLQI() + "\t" + rxRec.getRSSI();
		}
	}
	
	private void logErr(String msg) {
		String result = "SignalStrengthVsDist: " + msg;
		if (System.getProperty ("PharosMiddleware.debug") != null) 
			System.err.println(result);
		if (flogger != null)
			flogger.log(result);
	}
	
	private void log(String msg) {
		String result = "SignalStrengthVsDist: " + msg;
		if (System.getProperty ("PharosMiddleware.debug") != null) 
			System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println("PharosClient: " + msg);
	}
	
	private static void usage() {
		System.setProperty ("PharosMiddleware.debug", "true");
		print("Usage: pharoslabut.logger.analyzer.SignalStrengthVsDist <options>\n");
		print("Where <options> include:");
		print("\t-expDir <experiment data directory>: The directory containing experiment data (required)");
		print("\t-log <log file name>: The file in which to log results (default null)");
		print("\t-telos: Analyze signal strength vs. distance of TelosB mote.");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String expDir = null;
		String logFile = null;
		boolean analyzeTelos = false;
		
		try {
			for (int i=0; i < args.length; i++) {
		
				if (args[i].equals("-log"))
					logFile = args[++i];
				else if (args[i].equals("-expDir"))
					expDir = args[++i];
				else if (args[i].equals("-debug") || args[i].equals("-d"))
					System.setProperty ("PharosMiddleware.debug", "true");
				else if (args[i].equals("-telos"))
					analyzeTelos = true;
				else {
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			usage();
			System.exit(1);
		}
		
		if (expDir == null) {
			usage();
			System.exit(1);
		}
		
		new SignalStrengthVsDist(analyzeTelos, expDir, logFile);
	}
}
