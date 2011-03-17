package pharoslabut.logger.analyzer;

import pharoslabut.logger.FileLogger;

/**
 * Generates a summary of the experiment.
 * 
 * @author Chien-Liang Fok
 */
public class SummarizeExperiment {

	private FileLogger flogger = null;
	private ExpData expData;
	
	
	/**
	 * The constructor.
	 * 
	 * @param expDir The directory containing the data from the experiment.
	 * @param flogger The file logger in which to save log data. This may be null.
	 */
	public SummarizeExperiment(String expDir, FileLogger flogger) {
		this.flogger = flogger;
		expData = new ExpData(expDir);
	}
	
	public void printSummary() {
		log("Summary of " + expData.getExpName() + ":");
		log(expData.toString());
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
		SummarizeExperiment.log(msg, this.flogger, isDebugStmt);
	}
	
	private static void log(String msg, FileLogger flogger, boolean isDebugStmt) {
		String result = "SignalStrengthVsDist: " + msg;
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
		print("Usage: pharoslabut.logger.analyzer.SummarizeExperiment <options>\n");
		print("Where <options> include:");
		print("\t-expDir <experiment data directory>: The directory containing experiment data (required)");
		print("\t-log <log file name>: The file in which to log debug statements (default null)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String expDir = null;
		FileLogger flogger = null; // for saving debug output
		
		// Process the command line arguments...
		try {
			for (int i=0; i < args.length; i++) {
		
				if (args[i].equals("-log"))
					flogger = new FileLogger(args[++i], false);
				else if (args[i].equals("-expDir"))
					expDir = args[++i];
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
		
		if (expDir == null) {
			usage();
			System.exit(1);
		}
		
		SummarizeExperiment se = new SummarizeExperiment(expDir, flogger);
		se.printSummary();
	}
}
