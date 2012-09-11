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
public class AnalyzeWaypointWaitTime {
	
	public AnalyzeWaypointWaitTime() {}
	
	/**
	 * The constructor.
	 * 
	 * @param expDir The directory containing the experiment log files.
	 * @param saveToFileName The name of the file in which to save data (may be null).
	 * @param verbose Whether to print the details of each visit.
	 */
	public AnalyzeWaypointWaitTime(String expDir, String saveToFileName, boolean verbose) {
		
		// First get all of the experiment data.
		MRPatrolExpData expData = new MRPatrolExpData(expDir);
		
		Logger.log("Analyzing log files in " + expData.getExpDirName());
		
//		Logger.logDbg("Number of waypoints: " + waypoints.size());
//		Logger.logDbg("Waypoints:");
//		for (int i=0; i < waypoints.size(); i++) {
//			Logger.logDbg("\t" + waypoints.get(i));
//		}
		
		Vector<WaypointWaitTimeState> waypointStates = getWaitTimes(expData);
		
		// Print the results
		FileLogger flogger = null;
		if (saveToFileName != null)
			flogger = new FileLogger(saveToFileName, false);
		
		if (verbose) {
			printVerbose(waypointStates, flogger);
		}
		
		log("WaypointID\tWaypoint\tAvg Wait Time (ms)\tAvg Wait Time 95% Conf (ms)\tAvg Wait Time (s)\tAvg Wait Time 95% Conf (s)", flogger);
		for (int i=0; i < waypointStates.size(); i++) {
			WaypointWaitTimeState currWaypoint = waypointStates.get(i);
			AverageStatistic stat = currWaypoint.getAvgWaitTime();
			log(i + "\t" + currWaypoint.waypoint + "\t" + stat + "\t" + stat.toSecondsString(), flogger);
		}
	}
	
	public void printVerbose(Vector<WaypointWaitTimeState> waypointStates, FileLogger flogger) {
		for (int i=0; i < waypointStates.size(); i++) {
			WaypointWaitTimeState currWaypoint = waypointStates.get(i);
			log("Details for Waypoint " + i + " at " + currWaypoint.waypoint + ":", flogger);

			log("Visit number\tArrival Time (ms)\tDeparture Time (ms)\tWait Time (ms)\tRobotID", flogger);
			for (int j=0; j < currWaypoint.visitationTimes.size(); j++) {
				VisitationState currVisit = currWaypoint.visitationTimes.get(j);
				log(j + "\t" + currVisit.getArrivalTime() 
						+ "\t" + currVisit.getArrivalTime() 
						+ "\t" + currWaypoint.waitTimes.get(j) 
						+ "\t" + currVisit.getRobotName() 
						+ " (" + currVisit.getRobotID() + ")", flogger);
			}
			log("", flogger); // add new line after each table.
		}
	}	
	
	/**
	 * 
	 * @param expData The experiment data.
	 * @return the wait times at each waypoint.
	 */
	public Vector<WaypointWaitTimeState> getWaitTimes(MRPatrolExpData expData) {
		Vector<Location> waypoints = expData.getWayPoints();
		
		Vector<WaypointWaitTimeState> waypointStates = new Vector<WaypointWaitTimeState>();
		
		// For each waypoint... 
		for (int i=0; i < waypoints.size(); i++) {
			Location currWaypoint = waypoints.get(i);
			
			// Get the wait times...
			Vector<VisitationState> visits = expData.getVisitationTimes(currWaypoint);
			Collections.sort(visits); // sort it in ascending order
			
			// Compute the wait times during each visit
			Vector<Long> waitTimes = new Vector<Long>();
			for (int j=0; j < visits.size(); j++) {
				waitTimes.add(visits.get(j).getWaitTime());
			}
			
			// Save the data in waypointStates
			waypointStates.add(new WaypointWaitTimeState(currWaypoint, visits, waitTimes));
		}
		
		return waypointStates;
	}
	
	
	private void log(String msg, FileLogger flogger) {
		System.out.println(msg);
		if (flogger != null)
			flogger.log(msg);
	}
	
	private static void usage() {
		System.out.println("Usage: " + AnalyzeWaypointWaitTime.class.getName()  + " <options>\n");
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
			new AnalyzeWaypointWaitTime(expDir, saveToFileName, verbose);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
