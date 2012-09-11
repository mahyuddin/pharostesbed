package pharoslabut.demo.mrpatrol.logAnalyzer;

//import java.util.Collections;
import java.util.Vector;

import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
//import pharoslabut.navigate.Location;
import pharoslabut.util.AverageStatistic;

/**
 * Analyzes the time it took for each node to complete the multi-robot patrol experiment.
 * 
 * @author Chien-Liang Fok
 */
public class AnalyzeNodeRunTimes {

	private MRPatrolExpData expData;
	
	/**
	 * The constructor.
	 * 
	 * @param expDir The directory containing the experiment log files.
	 * @param saveToFileName The name of the file in which to save data (may be null).
//	 * @param verbose Whether to print the details of each visit.
	 */
	public AnalyzeNodeRunTimes(String expDir, String saveToFileName) {
		
		// First get all of the experiment data.
		expData = new MRPatrolExpData(expDir);
		
		Logger.log("Analyzing log files in " + expData.getExpDirName());
		
		// Next, get the waypoints
		Vector<RobotMRPatrolExpData> robots = expData.getRobots();
		
		// Print the results
		FileLogger flogger = null;
		if (saveToFileName != null)
			flogger = new FileLogger(saveToFileName, false);
		
		Vector<Double> completionTimes = new Vector<Double>();
		
		log("NodeID\tStarting Waypoint\tCompletion Time (ms)\tCompletion Time (s)", flogger);
		for (int i=0; i < robots.size(); i++) {
			RobotMRPatrolExpData currRobot = robots.get(i);
			long runTime = currRobot.getTotalRunTime();
			completionTimes.add((double)runTime);
			log(currRobot.getRobotName() + "(" + currRobot.getRobotID() + ")\t" + currRobot.getFirstWaypoint() + "\t" +  runTime + "\t" + (runTime/1000), flogger);
		}
		AverageStatistic stat = new AverageStatistic(completionTimes);
		log("Average (ms):\t" + stat, flogger);
		log("Average (s):\t" + stat.toSecondsString(), flogger);
	}

	private void log(String msg, FileLogger flogger) {
		System.out.println(msg);
		if (flogger != null)
			flogger.log(msg);
	}
	
	private static void usage() {
		System.out.println("Usage: " + AnalyzeNodeRunTimes.class.getName()  + " <options>\n");
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
			new AnalyzeNodeRunTimes(expDir, saveToFileName);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
