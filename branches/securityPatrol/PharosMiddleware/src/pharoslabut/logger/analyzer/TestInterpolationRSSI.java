package pharoslabut.logger.analyzer;

import java.util.*;
import pharoslabut.RobotIPAssignments;
import pharoslabut.exceptions.PharosException;
import pharoslabut.logger.FileLogger;

/**
 * Evaluates the accuracy of the RSSI interpolation.
 * Generates two tables: One time vs. actual RSSI of beacons received from specific sender,
 * another time vs. interpolated RSSI of beacons received from specific sender.
 * 
 * @author Chien-Liang Fok
 */
public class TestInterpolationRSSI {
	
	private FileLogger flogger;
	
	/**
	 * The constructor.
	 * 
	 * @param rcvrID
	 * @param sndrID
	 * @param expDir
	 * @param timeStepSize
	 * @param flogger
	 */
	public TestInterpolationRSSI(int rcvrID, int sndrID, String expDir, long timeStepSize) {
		ExpData expData = new ExpData(expDir);
		RobotExpData rcvrData = expData.getRobotByID(rcvrID);
		RobotExpData sndrData = expData.getRobotByID(sndrID);
		
		String sndrName = null;
		try {
			sndrName = RobotIPAssignments.getName(sndrID);
		} catch (PharosException e1) {
			logErr("Unable to get sender's name: " + sndrID);
			e1.printStackTrace();
		}
		
		String rcvrName = null;
		try {
			rcvrName = RobotIPAssignments.getName(rcvrID);
		} catch (PharosException e1) {
			logErr("Unable to get receiver's name: " + rcvrID);
			e1.printStackTrace();
		}
		
		this.flogger = new FileLogger("TestInterpolationRSSI-" + rcvrName + "-" + sndrName + ".txt", false);
		
		rcvrData.setFileLogger(flogger);
		
		log("Actual RSSI of beacons received by " + rcvrData.getRobotName() + " from " + sndrData.getRobotName() + ":");

		Vector<TelosBRxRecord> actualRx = rcvrData.getTelosBRxHist(sndrID);
		for (int i=0; i < actualRx.size(); i++) {
			TelosBRxRecord cr = actualRx.get(i);
			double deltaTimeS = (cr.getTimeStamp() - expData.getExpStartTime())/1000.0;
			log(cr.getTimeStamp() + "\t" + deltaTimeS + "\t" + cr.getRSSI());
		}
		
		log("\nInterpolated RSSI of beacons received by " + rcvrData.getRobotName() + " from " + sndrData.getRobotName() + ":");

		for (long timestamp = expData.getExpStartTime(); timestamp < expData.getExpStopTime(); timestamp += timeStepSize) {
			double rssi = rcvrData.getTelosBRSSIto(sndrID, timestamp, timeStepSize);
			
			double deltaTimeS = (timestamp - expData.getExpStartTime()) / 1000.0;
			log(timestamp + "\t" + deltaTimeS + "\t" + (rssi != -1 ? rssi : ""));
			
		}
	}
	
	private void logErr(String msg) {
		String result = "TestInterpolationRSSI: ERROR: " + msg;
		System.err.println(result);
		if (flogger != null)
			flogger.log(result);
	}
	
	private void log(String msg) {
		String result = "TestInterpolationRSSI: " + msg;
		System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(msg);
	}
	
	private static void usage() {
		System.setProperty ("PharosMiddleware.debug", "true");
		print("Usage: pharoslabut.logger.analyzer.TestInterpolationRSSI <options>\n");
		print("Where <options> include:");
		print("\t-expDir <experiment data directory>: The directory containing experiment data (required)");
		print("\t-rcvrID <robot1 ID>: The ID of robot 1 (required)");
		print("\t-sndrID <robot1 ID>: The ID of robot 2 (required)");
		print("\t-timeStepSize <time step size>: The time step in milliseconds (default: 5000)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String expDir = null;
		int rcvrID = -1;
		int sndrID = -1;
		long timeStepSize = 5000;
		
		// Process the command line arguments...
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-expDir"))
					expDir = args[++i];
				else if (args[i].equals("-rcvrID"))
					rcvrID = Integer.valueOf(args[++i]);
				else if (args[i].equals("-sndrID"))
					sndrID = Integer.valueOf(args[++i]);
				else if (args[i].equals("-timeStepSize"))
					timeStepSize = Long.valueOf(args[++i]);
				else if (args[i].equals("-debug") || args[i].equals("-d"))
					System.setProperty ("PharosMiddleware.debug", "true");
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
		
		if (expDir == null || rcvrID == -1 || sndrID == -1) {
			usage();
			System.exit(1);
		}
		
		new TestInterpolationRSSI(rcvrID, sndrID, expDir, timeStepSize);
	}
}
