package pharoslabut.demo.mrpatrol.logAnalyzer;

import java.util.Collections;
import java.util.Vector;

import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.Location;
import pharoslabut.util.AverageStatistic;

/**
 * Analyzes the idle times of each waypoint.  The idle time is the time between robot visits.
 * It generates a table containing the idle timesof each waypoint.
 * 
 * @author Chien-Liang Fok
 */
public class AnalyzeWaypointIdleTime {
	
	/**
	 * The default constructor.
	 */
	public AnalyzeWaypointIdleTime() {
		
	}
	
	/**
	 * The constructor.
	 * 
	 * @param expDir The directory containing the experiment log files.
	 * @param saveToFileName The name of the file in which to save data (may be null).
	 * @param verbose Whether to print the details of each visit.
	 */
	public AnalyzeWaypointIdleTime(String expDir, String saveToFileName, boolean verbose) {
		
		// First get all of the experiment data.
		MRPatrolExpData expData = new MRPatrolExpData(expDir);
		Logger.log("Analyzing log files in " + expData.getExpDirName());
		
//		Logger.logDbg("Number of waypoints: " + waypoints.size());
//		Logger.logDbg("Waypoints:");
//		for (int i=0; i < waypoints.size(); i++) {
//			Logger.logDbg("\t" + waypoints.get(i));
//		}
		
		Vector<WaypointIdlenessState> waypointStates = getIdleTimes(expData);
		
		// Print the results
		FileLogger flogger = null;
		if (saveToFileName != null)
			flogger = new FileLogger(saveToFileName, false);
		
		if (verbose) {
			printVerbose(waypointStates, flogger);	
		}
		
		log("WaypointID\tWaypoint\tAvg Idle Time (ms)\tAvg Idle Time 95% Conf (ms)\tAvg Idle Time (s)\tAvg Idle Time 95% Conf (s)", flogger);
		for (int i=0; i < waypointStates.size(); i++) {
			WaypointIdlenessState currWaypoint = waypointStates.get(i);
			AverageStatistic stat = currWaypoint.getAvgIdleTime();
			log(i + "\t" + currWaypoint.waypoint + "\t" + stat + "\t" + stat.toSecondsString(), flogger);
		}
	}
	
	public void printVerbose(Vector<WaypointIdlenessState> waypointStates, FileLogger flogger) {
		for (int i=0; i < waypointStates.size(); i++) {
			WaypointIdlenessState currWaypoint = waypointStates.get(i);
			log("Details for Waypoint " + i + " at " + currWaypoint.waypoint + ":", flogger);
			
			log("Visit number\tTime (ms)\tTime since Last Visit (ms)\tRobotID", flogger);
			for (int j=0; j < currWaypoint.visitationTimes.size(); j++) {
				if (j > 0)
					log(j + "\t" + currWaypoint.visitationTimes.get(j).getArrivalTime() + "\t" + currWaypoint.idleTimes.get(j-1) 
							+ "\t" + currWaypoint.visitationTimes.get(j).getRobotName() 
							+ " (" + currWaypoint.visitationTimes.get(j).getRobotID() + ")", flogger);
				else
					log(j + "\t" + currWaypoint.visitationTimes.get(j).getArrivalTime()
							+ "\t\t" + currWaypoint.visitationTimes.get(j).getRobotName() 
							+ " (" + currWaypoint.visitationTimes.get(j).getRobotID() + ")", flogger);
			}
			log("", flogger); // add new line after each table.
		}
	}
	
	/**
	 * Returns the idleness of each waypoint in the experiment.
	 * 
	 * @param expData the experiment data.
	 * @return The idleness of each waypoint in the experiment.
	 */
	public Vector<WaypointIdlenessState> getIdleTimes(MRPatrolExpData expData) {
		Vector<WaypointIdlenessState> waypointStates = new Vector<WaypointIdlenessState>();
		Vector<Location> waypoints = expData.getWayPoints();
		
		// For each waypoint... 
		for (int i=0; i < waypoints.size(); i++) {
			Location currWaypoint = waypoints.get(i);
			
			// Get the times it was visited...
			Vector<VisitationState> visitationTimes = expData.getVisitationTimes(currWaypoint);
			Collections.sort(visitationTimes); // sort it in ascending order
			
			// Compute the idle times between visits
			Vector<Long> idleTimes = new Vector<Long>();
			for (int j=0; j < visitationTimes.size()-1; j++) {
				long currVisitTime = visitationTimes.get(j).getArrivalTime();
				long nextVisitTime = visitationTimes.get(j+1).getArrivalTime();
				long idleTime = nextVisitTime - currVisitTime;
				idleTimes.add(idleTime);
			}
			
			// Save the data in waypointStates
			waypointStates.add(new WaypointIdlenessState(currWaypoint, visitationTimes, idleTimes));
		}
		
		return waypointStates;
	}
	
	private void log(String msg, FileLogger flogger) {
		System.out.println(msg);
		if (flogger != null)
			flogger.log(msg);
	}
	
	private static void usage() {
		System.out.println("Usage: " + AnalyzeWaypointIdleTime.class.getName()  + " <options>\n");
		System.out.println("Where <options> include:");
		System.out.println("\t-expDir <dir name>: The directory containing the multi-robot patrol experiment log files. (required)");
		System.out.println("\t-verbose: Print the details of each waypoint visit.");
		System.out.println("\t-save <file name>: Save the idle times into a text file. (optional)");
		System.out.println("\t-debug or -d: Enable debug mode.");
	}
	
	public static void main(String[] args) {
		String expDir = null;
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
				else if (args[i].equals("-expDir")) {
					expDir = args[++i];
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
			new AnalyzeWaypointIdleTime(expDir, saveToFileName, verbose);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
