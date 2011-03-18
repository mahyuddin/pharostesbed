package pharoslabut.logger.analyzer.cbl;

import java.util.*;
import pharoslabut.navigate.*;
import pharoslabut.logger.FileLogger;
import pharoslabut.logger.analyzer.ExpData;
import pharoslabut.logger.analyzer.RobotExpData;

/**
 * Extracts the true locations of the robots so that they can be
 * used to generate the input to the connectivity-based-localization
 * algorithm.
 * 
 * @author Chien-Liang Fok
 */
public class ExtractTrueLocation {

	private FileLogger flogger = null;
	private ExpData expData;
	
	
	/**
	 * The constructor.
	 * 
	 * @param expDir The directory containing the data from the experiment.
	 * @param flogger The file logger in which to save log data. This may be null.
	 */
	public ExtractTrueLocation(String expDir, FileLogger flogger) {
		this.flogger = flogger;
		expData = new ExpData(expDir);
	}
	
	class RobotTrueLocData {
		private String robotName;
		private int robotID;
		private Vector<Location> locations = new Vector<Location>();
		
		public RobotTrueLocData(String robotName, int robotID) {
			this.robotName = robotName;
			this.robotID = robotID;
		}
		
		public String getRobotName() {
			return robotName;
		}
		
		public int getRobotID() {
			return robotID;
		}
		
		public void addLocation(Location loc) {
			locations.add(loc);
		}
		
		public int numLocations() {
			return locations.size();
		}
		
		public Location getLocation(int indx) {
			return locations.get(indx);
		}
	}
	
	/**
	 * Extracts the true locations of the robots and formats them in a manner that can
	 * be used in the connectivity-based-localization code.
	 * 
	 * @param timestepSize The length of the time step in milliseconds.
	 * @param numTimesteps The number of time steps.
	 * @param outputLogger The logger for storing the result.
	 */
	public void getTrueLocation(long timestepSize, int numTimesteps, FileLogger outputLogger) {
		
		Vector<RobotTrueLocData> robotLocs = new Vector<RobotTrueLocData>();
		
		// For each robot in the experiment
		Enumeration<RobotExpData> robotEnum = expData.getRobotEnum();
		while (robotEnum.hasMoreElements()) {
			RobotExpData currRobot = robotEnum.nextElement();
			
			RobotTrueLocData currRobotData = new RobotTrueLocData(currRobot.getRobotName(), currRobot.getRobotID());
			long startTime = currRobot.getRobotStartTime();
			
			// Get the actual locations of the robot at each time step
			for (long i = 0; i < numTimesteps; i++) {
				long currTime = startTime + i * timestepSize;
				Location currLoc = currRobot.getLocation(currTime);
				currRobotData.addLocation(currLoc);
			}
			robotLocs.add(currRobotData);
		}
		
		// Print the results in a format that is used by the 
		// connectivity-based-localization algorithm
		
//		static public double[] Xtrue = {
//	        0.0, 1.0, 1.0, 1.0, 1.0, 1.0, // Robot #1, X locations at time t=1..6
//	        3.0, 2.0, 2.0, 2.0, 2.0, 2.0, // Robot #2, X locations at time t=1..6
//	        0.0, 1.0, 1.0, 1.0, 1.0, 1.0, // Robot #3, X locations at time t=1..6
//	        3.0, 2.0, 2.0, 2.0, 2.0, 2.0, // Robot #4, X locations at time t=1..6
//	        0.0, 0.0, 0.0, 0.0, 0.0, 0.0 // Robot #5, X locations at time t=1..6 (stationary)
//	    };
//	    static public double[] Ytrue = {
//	        0.0, 0.0, 1.0, 2.0, 3.0, 4.0, // Robot #1, Y locations at time t=1..6
//	        0.0, 0.0, 1.0, 2.0, 3.0, 4.0, // Robot #2, Y locations at time t=1..6
//	        -1.0, -1.0, -2.0, -3.0, -4.0, -5.0, // Robot #3, Y locations at time t=1..6
//	        -1.0, -1.0, -2.0, -3.0, -4.0, -5.0, // Robot #4, Y locations at time t=1..6
//	        3.0, 3.0, 3.0, 3.0, 3.0, 3.0 // Robot #5, X locations at time t=1..6 (stationary)
//	    };
//	    static public double[] Ztrue = {
//	        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, // Robot #1, Z locations at time t=1..6 (floor 1)
//	        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, // Robot #2, Z locations at time t=1..6 (floor 1)
//	        1.0, 1.0, 1.0, 1.0, 1.0, 1.0, // Robot #3, Z locations at time t=1..6 (floor 2, height offset +1 unit)
//	        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, // Robot #4, Z locations at time t=1..6 (floor 1)
//	        1.0, 1.0, 1.0, 1.0, 1.0, 1.0 // Robot #5, Z-locations at time t=1..6 (floor 2, height offset +1 unit)
//	    };
//	    static public int nRobots = 5;
//	    static public int nTimesteps = 6;
//	    static public byte[] RobotType = {
//	        1, // Robot #1 is type-I node (mobile with unknown locations)
//	        1, // Robot #2 is type-I node (mobile with unknown locations)
//	        1, // Robot #3 is type-I node (mobile with unknown locations)
//	        1, // Robot #4 is type-I node (mobile with unknown locations)
//	        2, // Robot #5 is type-II node (stationary with unknown locations)
//	    };
		
		StringBuffer xTrueStr = new StringBuffer("static public double[] Xtrue = {\n");
		StringBuffer yTrueStr = new StringBuffer("static public double[] Ytrue = {\n");
		StringBuffer zTrueStr = new StringBuffer("static public double[] Ztrue = {\n");
		StringBuffer robotTypeStr = new StringBuffer("static public byte[] RobotType = {\n");
		
		// For each robot...
		for (int i=0; i < robotLocs.size(); i++) {
			RobotTrueLocData currRobot = robotLocs.get(i);
			
			// For each location of the robot...
			for (int j=0; j < currRobot.numLocations(); j++) {
				Location currLoc = currRobot.getLocation(j);
				
				if (j == 0) {
					xTrueStr.append("\t");
					yTrueStr.append("\t");
					zTrueStr.append("\t");
				}
				
				xTrueStr.append(currLoc.longitude());
				yTrueStr.append(currLoc.latitude());
				zTrueStr.append("0.0");  // assume all robots are at the same elevation.
				
				if (j < currRobot.numLocations() - 1) {
					xTrueStr.append(", ");
					yTrueStr.append(", ");
					zTrueStr.append(", ");
				}
			}
			
			if (i < robotLocs.size() - 1) {
				// Add a descriptive comment to the end of the row in each array
				xTrueStr.append(", // X locations of robot " + currRobot.getRobotName() + " (" + currRobot.getRobotID() + ")\n");
				yTrueStr.append(", // Y locations of robot " + currRobot.getRobotName() + " (" + currRobot.getRobotID() + ")\n");
				zTrueStr.append(", // Z locations of robot " + currRobot.getRobotName() + " (" + currRobot.getRobotID() + ")\n");
				robotTypeStr.append("\t 1, // robot " + currRobot.getRobotName() + " (" + currRobot.getRobotID() + ") is type I (mobile with unknown locations)\n");
			} else {
				// Add a descriptive comment to the end of the row in each array, then close the arrays
				xTrueStr.append(" // X locations of robot " + currRobot.getRobotName() + " (" + currRobot.getRobotID() + ")\n};");
				yTrueStr.append(" // Y locations of robot " + currRobot.getRobotName() + " (" + currRobot.getRobotID() + ")\n};");
				zTrueStr.append(" // Z locations of robot " + currRobot.getRobotName() + " (" + currRobot.getRobotID() + ")\n};");
				robotTypeStr.append("\t 1 // robot " + currRobot.getRobotName() + " (" + currRobot.getRobotID() + ") is type I (mobile with unknown locations)\n};");
			}
		}
		
		outputLogger.log(xTrueStr.toString());
		outputLogger.log(yTrueStr.toString());
		outputLogger.log(zTrueStr.toString());
		
		outputLogger.log("static public int nRobots = " + expData.numRobots() + ";");
		outputLogger.log("static public int nTimesteps = " + numTimesteps + ";");
		outputLogger.log(robotTypeStr.toString());
	}
	
//	private void logErr(String msg, FileLogger errLogger) {
//		String result = "SignalStrengthVsDist: " + msg;
//		//if (System.getProperty ("PharosMiddleware.debug") != null) 
//		System.err.println(result);
//		if (flogger != null)
//			flogger.log(result);
//		if (errLogger != null)
//			errLogger.log(result);
//	}
	
	private void log(String msg) {
		boolean isDebugStmt = false;
		ExtractTrueLocation.log(msg, this.flogger, isDebugStmt);
	}
	
	private static void log(String msg, FileLogger flogger, boolean isDebugStmt) {
		String result = "ExtractTrueLocation: " + msg;
		if (!isDebugStmt || System.getProperty ("PharosMiddleware.debug") != null) 
			System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
	
//	private static void print(String msg, FileLogger flogger) {
//		System.out.println(msg);
//		if (flogger != null) {
//			flogger.log(msg);
//		}
//	}
	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(msg);
	}
	
	private static void usage() {
		System.setProperty ("PharosMiddleware.debug", "true");
		print("Usage: pharoslabut.logger.analyzer.cbl.ExtractTrueLocation <options>\n");
		print("Where <options> include:");
		print("\t-expDir <experiment data directory>: The directory containing experiment data (required)");
		print("\t-output <output file>: The file in which to save the results (required)");
		print("\t-timeStepSize <time step size>: The length of the time step in ms (default 10000)");
		print("\t-numTimeSteps <number of time steps>: The number of time steps (default 50)");
		print("\t-log <log file name>: The file in which to log debug statements (default null)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String expDir = null;
		String outFile = null;
		FileLogger flogger = null; // for saving debug output
		long timeStepSize = 10000;
		int numTimeSteps = 50;
		// Process the command line arguments...
		try {
			for (int i=0; i < args.length; i++) {
		
				if (args[i].equals("-log"))
					flogger = new FileLogger(args[++i], false);
				else if (args[i].equals("-expDir"))
					expDir = args[++i];
				else if (args[i].equals("-output"))
					outFile = args[++i];
				else if (args[i].equals("-timeStepSize"))
					timeStepSize = Long.valueOf(args[++i]);
				else if (args[i].equals("-numTimeSteops"))
					numTimeSteps = Integer.valueOf(args[++i]);
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
		
		if (expDir == null || outFile == null) {
			usage();
			System.exit(1);
		}
		
		FileLogger outFlogger = new FileLogger(outFile, false);
		
		ExtractTrueLocation etl = new ExtractTrueLocation(expDir, flogger);
		etl.getTrueLocation(timeStepSize, numTimeSteps, outFlogger);
	}
}
