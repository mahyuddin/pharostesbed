package pharoslabut.logger.analyzer.cbl;

import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import pharoslabut.experiment.RobotExpSettings;
import pharoslabut.logger.FileLogger;
import pharoslabut.logger.analyzer.GPSLocationState;
import pharoslabut.logger.analyzer.GPSVisualize;
import pharoslabut.logger.analyzer.RobotExpData;
import pharoslabut.navigate.*;

/**
 * Reads in the output of the connectivity-based localization search
 * algorithm, and reformats its for display on GPSVisualizer.
 * 
 * @author Chien-Liang Fok
 */
public class CBLOutputToGPSVisualizer {
	FileLogger flogger = null;
	FileLogger outputFlogger;
	private Hashtable<Integer, RobotLocInfo> dataTable = new Hashtable<Integer, RobotLocInfo>();
	
	public CBLOutputToGPSVisualizer(String inputFile, String outputFile, FileLogger flogger) {
		this.flogger = flogger;
		outputFlogger = new FileLogger(outputFile, false);
		
		doAnalysis(inputFile);
	}
	
	private class RobotLocEntry {
		int timestep;
		Location locTrue;
		Location locEst;
		
		public RobotLocEntry(int timestep, Location locTrue, Location locEst) {
			this.timestep = timestep;
			this.locTrue = locTrue;
			this.locEst = locEst;
		}
	}
	
	private class RobotLocInfo {
		int robotNumber;
		Vector<RobotLocEntry> locs = new Vector<RobotLocEntry>();
		
		public RobotLocInfo(int robotNumber) {
			this.robotNumber = robotNumber;
		}
		
		public void addEntry(RobotLocEntry entry) {
			locs.add(entry);
		}
	}
	
	/**
	 * Processes tokens of the form "Robot #0 (floor 0)"
	 * 
	 * @param token The token to process.
	 * @return The robot number as specified within the token.
	 */
	private int extractRobotNumber(String token) {
		String[] tokens = token.split("[\\s]+");
		for (int i=0; i < tokens.length; i++) {
			if (tokens[i].startsWith("#")) {
				return Integer.valueOf(tokens[i].substring(1));
			}
		}
		logErr("Unable to find robot number in token " + token);
		return -1; 
	}
	
	private int extractTime(String token) {
		String[] tokens = token.split("[\\s]+");
//		for (int i=0; i < tokens.length; i++) {
//			log("extractTime: " + i + ": " + tokens[i]);
//		}
		return Integer.valueOf(tokens[tokens.length-1]);
	}
	
	private Location extractLocation(String token) {
		String[] tokens = token.split("[\\s|=]+");
//		for (int i=0; i < tokens.length; i++) {
//			log("extractTime: " + i + ": " + tokens[i]);
//		}
		return new Location(Double.valueOf(tokens[4]), Double.valueOf(tokens[2]));
	}
	
	/**
	 * Reads in the file and creates a new GPS trace file that can be used by
	 * GPSVisualizer to see the paths taken by the robots and the estimated paths
	 * based on the connectivity-based localization algorithm. 
	 * 
	 * @param inputFile The input file containing the output of the connectivity-based localizer.
	 */
	private void doAnalysis(String inputFile) {
		BufferedReader input = null;
		try {
			input =  new BufferedReader(new FileReader(inputFile));
		} catch (IOException ex){
			ex.printStackTrace();
			System.err.println("Unable to open " + inputFile);
			System.exit(1);
		}
		
		try {
			String line = null;
			int lineno = 1;
			while (( line = input.readLine()) != null) {
				if (!line.equals("")) {
					if (line.startsWith("Robot")) {
						String[] elem = line.split("[\\|]");
//						for (int i=0; i < elem.length; i++) {
//							log(i + ": " + elem[i]);
//						}
						int robotNumber = extractRobotNumber(elem[0]);
						int timeStep = extractTime(elem[1]);
						Location locTrue = extractLocation(elem[2]);
						Location locEst = extractLocation(elem[3]);
						
						RobotLocEntry rle = new RobotLocEntry(timeStep, locTrue, locEst);
						
						if (!dataTable.containsKey(robotNumber)) {
							RobotLocInfo rli = new RobotLocInfo(robotNumber);
							rli.addEntry(rle);
							dataTable.put(robotNumber, rli);
						} else {
							RobotLocInfo rli = dataTable.get(robotNumber);
							rli.addEntry(rle);
						}
					}
				}
				lineno++;
			}
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		// By now all of the data is stored in the dataTable.
		// Generate the GPSVisualizer file.
		
		int colorIndx = 0;
		
		Enumeration<Integer> robotNumbers = dataTable.keys();
		while (robotNumbers.hasMoreElements()) {
			RobotLocInfo currRobot = dataTable.get(robotNumbers.nextElement());
			
			log("Processing robot " + currRobot.robotNumber);
			
			// Make a trace of the robot's actual location
			outputFlogger.log("type,latitude,longitude,name,color"); 
			for (int j=0; j < currRobot.locs.size(); j++) {
				RobotLocEntry rle = currRobot.locs.get(j);
				Location currLoc = rle.locTrue;
				String line = "T," + currLoc.latitude() + "," + currLoc.longitude();
				if (j == 0)
					line += ", Robot " + currRobot.robotNumber + " Actual Loc, " + GPSVisualize.COLORS[colorIndx++];
				outputFlogger.log(line);
			}
			
			// Make a trace of the robot's estimated location
			outputFlogger.log("type,latitude,longitude,name,color"); 
			for (int j=0; j < currRobot.locs.size(); j++) {
				RobotLocEntry rle = currRobot.locs.get(j);
				Location currLoc = rle.locEst;
				String line = "T," + currLoc.latitude() + "," + currLoc.longitude();
				if (j == 0)
					line += ", Robot " + currRobot.robotNumber + " Est. Loc, " + GPSVisualize.COLORS[colorIndx++];
				outputFlogger.log(line);
			}
		}
	}
	
	private void logErr(String msg) {
		System.err.println(msg);
		System.exit(1);
	}
	
	private void log(String msg) {
		boolean isDebugStmt = false;
		CBLOutputToGPSVisualizer.log(msg, this.flogger, isDebugStmt);
	}
	
	private static void log(String msg, FileLogger flogger, boolean isDebugStmt) {
		String result = "CBLOutputToGPSVisualizer: " + msg;
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
		print("Usage: pharoslabut.logger.analyzer.cbl.CBLOutputToGPSVisualizer <options>\n");
		print("Where <options> include:");
		print("\t-input <input file>: This file should contain the output of the CBL algorithm (required)");
		print("\t-output <output file>: The file in which to save the results (required)");
		print("\t-log <log file name>: The file in which to log debug statements (default null)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String inputFile = null;
		String outputFile = null;
		FileLogger flogger = null; // for saving debug output
		
		// Process the command line arguments...
		try {
			for (int i=0; i < args.length; i++) {
		
				if (args[i].equals("-log"))
					flogger = new FileLogger(args[++i], false);
				else if (args[i].equals("-input"))
					inputFile = args[++i];
				else if (args[i].equals("-output"))
					outputFile = args[++i];
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
		
		if (inputFile == null || outputFile == null) {
			usage();
			System.exit(1);
		}
		
		new CBLOutputToGPSVisualizer(inputFile, outputFile, flogger);
	}
}
