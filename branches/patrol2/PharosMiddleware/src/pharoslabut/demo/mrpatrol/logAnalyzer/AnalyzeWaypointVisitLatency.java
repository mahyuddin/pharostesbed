package pharoslabut.demo.mrpatrol.logAnalyzer;

import java.util.Collections;
import java.util.Vector;

import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.Location;
import pharoslabut.util.AverageStatistic;

/**
 * Analyzes the latencies of visiting each waypoint.
 * It generates a table containing times between each visit of each waypoint.
 * 
 * @author Chien-Liang Fok
 *
 */
public class AnalyzeWaypointVisitLatency {

	private MRPatrolExpData expData;
	
	/**
	 * The constructor.
	 * 
	 * @param expDir The directory containing the log files generated during a MRPatrol experiment.
	 * @param saveToFileName The name of the file in which to save data (may be null).
	 */
	public AnalyzeWaypointVisitLatency(String expDir, String saveToFileName) {
		
		// First get all of the experiment data.
		expData = new MRPatrolExpData(expDir);
		
		// Next, get the waypoints
		Vector<Location> waypoints = expData.getWayPoints();
		
//		Logger.logDbg("Number of waypoints: " + waypoints.size());
//		Logger.logDbg("Waypoints:");
//		for (int i=0; i < waypoints.size(); i++) {
//			Logger.logDbg("\t" + waypoints.get(i));
//		}
		
		Vector<WaypointState> waypointStates = new Vector<WaypointState>();
		
		// For each waypoint... 
		for (int i=0; i < waypoints.size(); i++) {
			Location currWaypoint = waypoints.get(i);
			
			// Get the times it was visited...
			Vector<Long> visitationTimes = expData.getVisitationTimes(currWaypoint);
			Collections.sort(visitationTimes); // sort it in ascending order
			
			// Compute the idle times between visits
			Vector<Long> idleTimes = new Vector<Long>();
			for (int j=0; j < visitationTimes.size()-1; j++) {
				long currVisitTime = visitationTimes.get(j);
				long nextVisitTime = visitationTimes.get(j+1);
				long idleTime = nextVisitTime - currVisitTime;
				idleTimes.add(idleTime);
			}
			
			// Save the data in waypointStates
			waypointStates.add(new WaypointState(currWaypoint, visitationTimes, idleTimes));
		}
		
		// print the results
		FileLogger flogger = null;
		if (saveToFileName != null)
			flogger = new FileLogger(saveToFileName, false);
		
		log("WaypointID\tWaypoint\tAverage Idle Time (ms)", flogger);
		for (int i=0; i < waypointStates.size(); i++) {
			WaypointState currWaypoint = waypointStates.get(i);
			log(i + "\t" + currWaypoint.waypoint + "\t" + currWaypoint.getAvgIdleTime(), flogger);
		}
	}
	
	private class WaypointState {
		Location waypoint;
		Vector<Long> visitationTimes;
		Vector<Long> idleTimes;
		
		public WaypointState(Location waypoint, Vector<Long> visitationTimes, Vector<Long> idleTimes) {
			this.waypoint = waypoint;
			this.visitationTimes = visitationTimes;
			this.idleTimes = idleTimes;
		}
		
		/**
		 * 
		 * @return The average idle time.
		 */
		public AverageStatistic getAvgIdleTime() {
			
			// Convert the idle times into a vector of doubles
			Vector<Double> temp = new Vector<Double>();
			for (int i=0; i < idleTimes.size(); i++) {
				temp.add((double)idleTimes.get(i));
			}
			
			// Compute the average and confidence intervals.
			return new AverageStatistic(temp);
		}
	}
	
	private void log(String msg, FileLogger flogger) {
		System.out.println(msg);
		if (flogger != null)
			flogger.log(msg);
	}
	
	private static void usage() {
		System.out.println("Usage: " + AnalyzeWaypointVisitLatency.class.getName()  + " <options>\n");
		System.out.println("Where <options> include:");
		System.out.println("\t-expDir <dir name>: The directory containing the log files generated during a MRPatrol experiment. (required)");
		System.out.println("\t-save <file name>: Save the idle times a text file. (optional)");
		System.out.println("\t-debug or -d: Enable debug mode");
	}
	
	public static void main(String[] args) {
		String expDir = null;
		String saveToFileName = null;
		
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
			System.err.println("Must specify log file.");
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
			new AnalyzeWaypointVisitLatency(expDir, saveToFileName);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
