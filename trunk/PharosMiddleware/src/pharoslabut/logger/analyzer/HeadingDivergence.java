package pharoslabut.logger.analyzer;

import java.util.*;

import pharoslabut.navigate.Location;

/**
 * Calculates the divergence between the robot's heading
 * and the ideal heading.  The ideal heading is facing
 * the next waypoint.
 * 
 * @author Chien-Liang Fok
 */
public class HeadingDivergence {

	/**
	 * The constructor.
	 * 
	 * @param logFileName The experiment log file to analyze.
	 */
	public HeadingDivergence(String logFileName) {
		RobotExpData expData = new RobotExpData(logFileName);
		
		// For each waypoint
		Vector<Location> wayPoints = expData.getWayPoints();
		for (int i=0; i < wayPoints.size(); i++) {
			log("Waypoint " + i + ": " + wayPoints.get(i));
			
			
		}
		
		
	}
	
	/**
	 * Logs a debug message.  This message is only printed if we're running
	 * in debug mode.
	 * 
	 * @param msg The message to log.
	 */
	private void logDbg(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			log(msg);
	}
	
	/**
	 * Logs a message.  This message is always printed regardless of
	 * whether we are running in debug mode.
	 * 
	 * @param msg The message to log.
	 */
	private void log(String msg) {
		String result = "HeadingDivergence: " + msg;
		System.out.println(result);
	}
	
	private void logErr(String msg) {
		String result = "HeadingDivergence: ERROR: " + msg;
		System.err.println(result);
	}
	
	private static void print(String msg) {
		System.out.println(msg);
	}
	
	private static void printErr(String msg) {
		System.err.println(msg);
	}
	
	private static void usage() {
		print("Usage: pharoslabut.logger.analyzer.HeadingDivergence <options>\n");
		print("Where <options> include:");
		print("\t-log <log file name>: The log file generated during the experiment. (required)");
		print("\t-debug or -d: Enable debug mode");
	}
	
	public static void main(String[] args) {
		String logFileName = null;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-h") || args[i].equals("-help")) {
					usage();
					System.exit(0);
				}
				
				if (args[i].equals("-log")) {
					logFileName = args[++i];
				}
				else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				}
				else {
					printErr("Unknown option: " + args[i]);
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			usage();
			System.exit(1);
		}
		
		if (logFileName == null) {
			printErr("Must specify log file.");
			usage();
			System.exit(1);
		}
		
		print("Log file: " + logFileName);
		print("Debug: " + (System.getProperty ("PharosMiddleware.debug") != null));
		
		try {
			new HeadingDivergence(logFileName);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
