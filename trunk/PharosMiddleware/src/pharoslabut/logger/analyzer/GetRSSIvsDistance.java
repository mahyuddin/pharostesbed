package pharoslabut.logger.analyzer;

import java.util.Enumeration;
import java.util.Vector;

import pharoslabut.RobotIPAssignments;
import pharoslabut.exceptions.PharosException;
import pharoslabut.logger.FileLogger;
import pharoslabut.navigate.Location;

public class GetRSSIvsDistance {

	private FileLogger flogger;
	
	public GetRSSIvsDistance(int rcvrID, int sndrID, String expDir, FileLogger flogger) {
		this.flogger = flogger;
		
		ExpData expData = new ExpData(expDir);
		RobotExpData rcvrData = expData.getRobotByID(rcvrID);
		RobotExpData sndrData = expData.getRobotByID(sndrID);
		
		String sndrName = null;
		try {
			sndrName = RobotIPAssignments.getRobotName(sndrID);
		} catch (PharosException e1) {
			logErr("Unable to get sender's name: " + sndrID);
			e1.printStackTrace();
		}
		
		String rcvrName = null;
		try {
			rcvrName = RobotIPAssignments.getRobotName(rcvrID);
		} catch (PharosException e1) {
			logErr("Unable to get receiver's name: " + rcvrID);
			e1.printStackTrace();
		}
		
		log("Timestamp (ms)\t Delta Time (s) \t Distance (m) \t RSSI of " + sndrName + " on " + rcvrName);
		
		// For each TelosB beacon received...
		Vector<TelosBRxRecord> rxHist = rcvrData.getTelosBRxHist();
		Enumeration<TelosBRxRecord> e = rxHist.elements();
		while (e.hasMoreElements()) {
			TelosBRxRecord currRxRecord = e.nextElement();
			long timestamp = currRxRecord.getTimeStamp();
			double deltaTimeS = (timestamp - expData.getExpStartTime()) / 1000.0;
			
			// Find the locations of the sender and receiver based on GPS...
			Location rcvrLoc = rcvrData.getLocation(timestamp);
			Location sndrLoc = sndrData.getLocation(timestamp);
			
			// Calculate the distance between the sender and receiver
			double dist = rcvrLoc.distanceTo(sndrLoc);
			
			// Save the results
			log(timestamp + "\t" + deltaTimeS + "\t" +  dist + "\t" + currRxRecord.getRSSI());
		}
		
		// add information about the way points
		log("Waypoint Info:");
		for (int i=0; i < rcvrData.getNumWaypoints(); i++) {
			PathEdge pe = rcvrData.getPathEdge(i);
			log("Time of arrival at waypoint " + i  + ": " + pe.getEndTime() + " (" + (pe.getEndTime() - expData.getExpStartTime()) + ")");
		}
	}
	
	private void logErr(String msg) {
		String result = "GetRSSIvsDistance: ERROR: " + msg;
		System.err.println(result);
		if (flogger != null)
			flogger.log(result);
	}
	
	private void log(String msg) {
		String result = "GetRSSIvsDistance: " + msg;
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
		print("Usage: pharoslabut.logger.analyzer.GetRSSIvsDistance <options>\n");
		print("Where <options> include:");
		print("\t-expDir <experiment data directory>: The directory containing experiment data (required)");
		print("\t-output <name of file to save results>: The name of the file in which to save results (required)");
		print("\t-rcvrID <robot ID>: The ID of rcvr robot (required)");
		print("\t-sndrID <robot ID>: The ID of sndr robot (required)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String expDir = null;
		String outFile = null;
		int rcvrID = -1;
		int sndrID = -1;
		
		// Process the command line arguments...
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-expDir"))
					expDir = args[++i];
				else if (args[i].equals("-output"))
					outFile = args[++i];
				else if (args[i].equals("-rcvrID"))
					rcvrID = Integer.valueOf(args[++i]);
				else if (args[i].equals("-sndrID"))
					sndrID = Integer.valueOf(args[++i]);
				else if (args[i].equals("-debug") || args[i].equals("-d"))
					System.setProperty ("PharosMiddleware.debug", "true");
				else {
					System.setProperty ("PharosMiddleware.debug", "true");
					print("Unknown option: " + args[i]);
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			usage();
			System.exit(1);
		}
		
		if (expDir == null || rcvrID == -1 || sndrID == -1 || outFile == null) {
			usage();
			System.exit(1);
		}
		
		FileLogger flogger = new FileLogger(outFile, false);
		new GetRSSIvsDistance(rcvrID, sndrID, expDir, flogger);
	}
}
