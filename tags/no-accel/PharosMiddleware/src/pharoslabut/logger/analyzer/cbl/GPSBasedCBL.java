package pharoslabut.logger.analyzer.cbl;

import pharoslabut.logger.FileLogger;

/**
 * Performs connectivity-based localization based only on GPS data.
 * 
 * @author Chien-Liang Fok
 */
public class GPSBasedCBL {
	//private GroundTruth groundTruth;
	//private FileLogger flogger = null;
	
	/**
	 * The constructor.
	 */
	public GPSBasedCBL(GroundTruth groundTruth, double range, FileLogger outFlogger, FileLogger flogger) {
		//this.groundTruth = groundTruth;
		//this.flogger = flogger;
		CBL cbl = new CBL(groundTruth);
		cbl.doCBL(range, outFlogger);
	}
	
//	private void log(String msg) {
//		boolean isDebugStmt = false;
//		GPSBasedCBL.log(msg, this.flogger, isDebugStmt);
//	}
	
//	private static void log(String msg, FileLogger flogger, boolean isDebugStmt) {
//		String result = "ExtractTrueLocation: " + msg;
//		if (!isDebugStmt || System.getProperty ("PharosMiddleware.debug") != null) 
//			System.out.println(result);
//		if (flogger != null)
//			flogger.log(result);
//	}
	
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
	
	private static void usage(String msg) {
		System.setProperty ("PharosMiddleware.debug", "true");
		print(msg);
		usage();
	}

	private static void usage() {
		System.setProperty ("PharosMiddleware.debug", "true");
		print("Usage: pharoslabut.logger.analyzer.cbl.GPSBasedCBL <options>\n");
		print("Where <options> include:");
		print("\t-expDir <experiment data directory>: The directory containing experiment data (required)");
		print("\t-output <output file>: The file in which to save the results (required)");
		print("\t-timeStepSize <time step size>: The length of the time step in ms (default 10000)");
		print("\t-numTimeSteps <number of time steps>: The number of time steps (default 50)");
		print("\t-range <range>: The maximum detectable inter-robot distance in meters (default infinity)");
		print("\t-log <log file name>: The file in which to log debug statements (default null)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String expDir = null;
		String outFile = null;
		FileLogger flogger = null; // for saving debug output
		long timeStepSize = 10000;
		int numTimeSteps = 50;
		double range = -1; // infinite
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
				else if (args[i].equals("-numTimeSteps"))
					numTimeSteps = Integer.valueOf(args[++i]);
				else if (args[i].equals("-range"))
					range = Double.valueOf(args[++i]);
				else if (args[i].equals("-debug") || args[i].equals("-d"))
					System.setProperty ("PharosMiddleware.debug", "true");
				else {
					usage("Unknown argument " + args[i]);
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
		
		GroundTruth groundTruth = new GroundTruth(expDir, timeStepSize, numTimeSteps);
		new GPSBasedCBL(groundTruth, range, outFlogger, flogger);
	}
}
