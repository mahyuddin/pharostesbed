package pharoslabut.logger.analyzer;

import pharoslabut.logger.*;

/**
 * Calculates the divergence between the robot's heading
 * and the ideal heading.  The ideal heading is the direction the robot 
 * must face to point to the next waypoint.
 * 
 * @author Chien-Liang Fok
 */
public class HeadingDivergence {

	/**
	 * The constructor.
	 * 
	 * @param logFileName The experiment log file to analyze.
	 * @param outputFileName The output file to save the results in.
	 */
	public HeadingDivergence(String logFileName, String outputFileName) {
		RobotExpData robotData = new RobotExpData(logFileName);
		FileLogger flogger = new FileLogger(outputFileName, false);
		
		long startTime = robotData.getStartTime();
		
		log("Time (ms)\tDelta Time (ms)\tActual Heading\tIdeal Heading\tHeading Error", flogger);
		
		// Calculate heading divergence every 1s
		for (long time = startTime; time < robotData.getStopTime(); time += 1000) {
			double currHeading = robotData.getHeading(time);
			double idealHeading = robotData.getIdealHeading(time);
			double headingError = pharoslabut.navigate.Navigate
				.headingError(currHeading, idealHeading);
			
			log(time + "\t" + (time - startTime) + "\t" + currHeading + "\t" + idealHeading 
					+ "\t" + headingError, flogger);
		}
		
	}
	
	/**
	 * Logs a debug message.  This message is only printed if we're running
	 * in debug mode.
	 * 
	 * @param msg The message to log.
	 */
//	private void logDbg(String msg) {
//		if (System.getProperty ("PharosMiddleware.debug") != null)
//			log(msg);
//	}
	
	/**
	 * Logs a message.  This message is always printed regardless of
	 * whether we are running in debug mode.
	 * 
	 * @param msg The message to log.
	 */
//	private void log(String msg) {
//		log(msg, null);
//	}
	
	private void log(String msg, FileLogger flogger) {
		String result = "HeadingDivergence: " + msg;
		System.out.println(result);
		if (flogger != null)
			flogger.log(msg);
	}
	
//	private void logErr(String msg) {
//		logErr(msg, null);
//	}
	
//	private void logErr(String msg, FileLogger flogger) {
//		String result = "HeadingDivergence: ERROR: " + msg;
//		System.err.println(result);
//		if (flogger != null)
//			flogger.log(msg);
//	}
	
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
		print("\t-output <output file>: The output file (required).");
		print("\t-debug or -d: Enable debug mode");
	}
	
	public static void main(String[] args) {
		String logFileName = null;
		String outputFileName = null;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-h") || args[i].equals("-help")) {
					usage();
					System.exit(0);
				}
				else if (args[i].equals("-log")) {
					logFileName = args[++i];
				}
				else if (args[i].equals("-output")) {
					outputFileName = args[++i];
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
		print("Output file: " + outputFileName);
		print("Debug: " + (System.getProperty ("PharosMiddleware.debug") != null));
		
		try {
			new HeadingDivergence(logFileName, outputFileName);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
