package pharoslabut.demo.mrpatrol.logAnalyzer;

import java.util.Vector;

import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
//import pharoslabut.util.AverageStatistic;

/**
 * Analyzes the number of messages transmitted, the number of messages received, and 
 * computes the percentage of the messages that were lost in a multi-robot patrol experiment.
 * 
 * @author Chien-Liang Fok
 */
public class AnalyzeMessageStats {

	private MRPatrolExpData expData;
	
	/**
	 * The constructor.
	 * 
	 * @param expDir The directory containing the experiment log files.
	 * @param saveToFileName The name of the file in which to save data (may be null).
//	 * @param verbose Whether to print the details of each visit.
	 */
	public AnalyzeMessageStats(String expDir, String saveToFileName) {
		
		// First get all of the experiment data.
		expData = new MRPatrolExpData(expDir);
		
		Logger.log("Analyzing log files in " + expData.getExpDirName());
		
		// Print the results
		FileLogger flogger = null;
		if (saveToFileName != null)
			flogger = new FileLogger(saveToFileName, false);
		
		int numTx = expData.getNumMsgTx();
		int numRx = expData.getNumMsgRx();
		log("Number of messages transmitted: " + numTx, flogger);
		log("Number of messages received: " + numRx, flogger);
		if (numTx == 0)
			log("Percent lost: N/A ", flogger);
		else
			log("Percent lost: " + (numTx - numRx) / (double)numTx * 100, flogger);
		
	}

	private void log(String msg, FileLogger flogger) {
		System.out.println(msg);
		if (flogger != null)
			flogger.log(msg);
	}
	
	private static void usage() {
		System.out.println("Usage: " + AnalyzeMessageStats.class.getName()  + " <options>\n");
		System.out.println("Where <options> include:");
		System.out.println("\t-expDir <dir name>: The directory containing the multi-robot patrol experiment log files. (required)");
//		System.out.println("\t-verbose: Print the details of each waypoint visit.");
		System.out.println("\t-save <file name>: Save the idle times into a text file. (optional)");
		System.out.println("\t-debug or -d: Enable debug mode.");
	}
	
	public static void main(String[] args) {
		String expDir = null;
		String saveToFileName = null;
//		boolean verbose = false;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-h") || args[i].equals("-help")) {
					usage();
					System.exit(0);
				}
				else if (args[i].equals("-save")) {
					saveToFileName = args[++i];
				}
				else if (args[i].equals("-expDir")) {
					expDir = args[++i];
				}
//				else if (args[i].equals("-verbose")) {
//					verbose = true;
//				}
				else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				}
				else {
					System.err.println("Unknown option: " + args[i]);
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
			System.err.println("Must specify directory containing experiment log files.");
			usage();
			System.exit(1);
		}
		
		System.out.println("Exp dir: " + expDir);
		if (saveToFileName != null) 
			System.out.println("Saving results to " + saveToFileName);
		else
			System.out.println("Not saving results to file.");
		System.out.println("Debug: " + (System.getProperty ("PharosMiddleware.debug") != null));
		
		try {
			new AnalyzeMessageStats(expDir, saveToFileName);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
