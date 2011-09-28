package pharoslabut.demo.mrpatrol.logAnalyzer;

import java.util.Collections;
import java.util.Vector;

import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.Location;
import pharoslabut.util.AverageStatistic;

/**
 * Analyzes the wait time of each waypoint.  The wait time is the duration a robot 
 * stays at a waypoint waiting for its teammates to arrive at their waypoints.
 * 
 * @author Chien-Liang Fok
 */
public class AnalyzeWaypointWaitTime2 {

	/**
	 * The constructor.
	 * 
	 * @param expDir1 The directory containing the experiment log files.
	 * @param expDir2 The directory containing the experiment log files.
	 * @param saveToFileName The name of the file in which to save data (may be null).
	 * @param verbose Whether to print the details of each visit.
	 */
	public AnalyzeWaypointWaitTime2(String expDir1, String expDir2, String saveToFileName, boolean verbose) {
		
		// First get all of the experiment data.
		MRPatrolExpData expData1 = new MRPatrolExpData(expDir1);
		MRPatrolExpData expData2 = new MRPatrolExpData(expDir2);
		
		Logger.log("Analyzing log files in " + expData1.getExpDirName() + " and " + expData2.getExpDirName());
		
		AnalyzeWaypointWaitTime analyzer = new AnalyzeWaypointWaitTime();
		
//		Logger.logDbg("Number of waypoints: " + waypoints.size());
//		Logger.logDbg("Waypoints:");
//		for (int i=0; i < waypoints.size(); i++) {
//			Logger.logDbg("\t" + waypoints.get(i));
//		}
		
		Vector<WaypointWaitTimeState> waypointStates1 = analyzer.getWaitTimes(expData1);
		Vector<WaypointWaitTimeState> waypointStates2 = analyzer.getWaitTimes(expData2);
		
		// Print the results
		FileLogger flogger = null;
		if (saveToFileName != null)
			flogger = new FileLogger(saveToFileName, false);
		
		if (verbose) {
			log("Details of Experiment " + expData1.getExpDirName(), flogger);
			analyzer.printVerbose(waypointStates1, flogger);
			log("Details of Experiment " + expData2.getExpDirName(), flogger);
			analyzer.printVerbose(waypointStates2, flogger);
		}
		
		log("WaypointID\tWaypoint\tAvg Wait Time (ms)\tAvg Wait Time 95% Conf (ms)\tAvg Wait Time (s)\tAvg Wait Time 95% Conf (s)", flogger);
		for (int i=0; i < waypointStates1.size(); i++) {
			WaypointWaitTimeState currWaypoint1 = waypointStates1.get(i);
			WaypointWaitTimeState currWaypoint2 = waypointStates2.get(i);
			
			currWaypoint1.mergeWith(currWaypoint2);
			
			AverageStatistic stat = currWaypoint1.getAvgWaitTime();
			log(i + "\t" + currWaypoint1.waypoint + "\t" + stat + "\t" + stat.toSecondsString(), flogger);
		}
	}
	
	private void log(String msg, FileLogger flogger) {
		System.out.println(msg);
		if (flogger != null)
			flogger.log(msg);
	}
	
	private static void usage() {
		System.out.println("Usage: " + AnalyzeWaypointWaitTime.class.getName()  + " <options>\n");
		System.out.println("Where <options> include:");
		System.out.println("\t-expDir1 <dir name>: The directory containing the multi-robot patrol experiment log files. (required)");
		System.out.println("\t-expDir2 <dir name>: The directory containing the multi-robot patrol experiment log files. (required)");
		System.out.println("\t-verbose: Print the details of each waypoint visit.");
		System.out.println("\t-save <file name>: Save the idle times into a text file. (optional)");
		System.out.println("\t-debug or -d: Enable debug mode.");
	}
	
	public static void main(String[] args) {
		String expDir1 = null;
		String expDir2 = null;
		String saveToFileName = null;
		boolean verbose = false;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-h") || args[i].equals("-help")) {
					usage();
					System.exit(0);
				}
				else if (args[i].equals("-save")) {
					saveToFileName = args[++i];
				}
				else if (args[i].equals("-expDir1")) {
					expDir1 = args[++i];
				}
				else if (args[i].equals("-expDir2")) {
					expDir2 = args[++i];
				}
				else if (args[i].equals("-verbose")) {
					verbose = true;
				}
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
		
		if (expDir1 == null) {
			System.err.println("Must specify directory 1 containing experiment log files.");
			usage();
			System.exit(1);
		}
		
		if (expDir2 == null) {
			System.err.println("Must specify directory 2 containing experiment log files.");
			usage();
			System.exit(1);
		}
		
		System.out.println("Exp dir1: " + expDir1);
		System.out.println("Exp dir2: " + expDir2);
		if (saveToFileName != null) 
			System.out.println("Saving results to " + saveToFileName);
		else
			System.out.println("Not saving results to file.");
		System.out.println("Debug: " + (System.getProperty ("PharosMiddleware.debug") != null));
		
		try {
			new AnalyzeWaypointWaitTime2(expDir1, expDir2, saveToFileName, verbose);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
