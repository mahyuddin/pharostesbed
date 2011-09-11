package pharoslabut.demo.mrpatrol.logAnalyzer;

import java.io.File;
import java.util.Collections;
import java.util.Vector;

import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.Location;
import pharoslabut.util.AverageStatistic;

/**
 * Computes statistics about the patrol route.
 * 
 * @author Chien-Liang Fok
 */
public class AnalyzePatrolRoute {

	private MRPatrolExpData expData;
	
	/**
	 * The constructor.
	 * 
	 * @param expDir The directory containing the experiment log files.
	 * @param saveToFileName The name of the file in which to save data (may be null).
//	 * @param verbose Whether to print the details of each visit.
	 */
	public AnalyzePatrolRoute(String expDir, String saveToFileName) {
		
		Logger.log("Performing analysis...");
		
		// First get all of the experiment data.
		expData = new MRPatrolExpData(expDir);
		
		// Create the file logger is user requested that the results be saved to a file.
		FileLogger flogger = null;
		if (saveToFileName != null)
			flogger = new FileLogger(saveToFileName, false);
		
		// Get the waypoints of the patrol route.
		Vector<Location> waypoints = expData.getWayPoints();
		
		// Compute the total distance of the patrol route
		double totalDist = 0;
		Vector<Double> distances = new Vector<Double>();
		for (int i=0; i < waypoints.size(); i++) {
			Location currWaypoint = waypoints.get(i);
			Location prevWaypoint;
			if (i > 0)
				prevWaypoint = waypoints.get(i-1);
			else
				prevWaypoint = waypoints.get(waypoints.size()-1);
			double currDist = currWaypoint.distanceTo(prevWaypoint);
			distances.add(currDist);
			totalDist += currDist;		
		}
		
		log("\nNumber of waypoints: " + waypoints.size(), flogger);
		log("Total distance of single round (m): " + totalDist, flogger);
		log("Number of rounds in experiment: " + expData.getNumRounds(), flogger);
		log("\nWaypoint\tLocation\tDistance from Previous Waypoint (m)", flogger);
			
		for (int i=0; i < waypoints.size(); i++) {
			Location currWaypoint = waypoints.get(i);
			double currDist = distances.get(i);
			log(i + "\t" + currWaypoint + "\t" + currDist, flogger);
		}
		
		// Generate a table that can be submitted to http://www.gpsvisualizer.com/map_input
		log("\nTo view waypoints, submit the following to GPSVisualizer (http://www.gpsvisualizer.com/map_input):", flogger);
		log("name,desc,latitude,longitude", flogger);
		for (int i=0; i < waypoints.size(); i++) {
			Location currWaypoint = waypoints.get(i);
			log("Waypoint " + i + ", Waypoint " + i + ", " + currWaypoint.latitude() + ", " + currWaypoint.longitude(), flogger);
		}
		
		new PatrolWaypointVisualizer(expData.getExpDirName(), waypoints);
	}
	
	private void log(String msg, FileLogger flogger) {
		System.out.println(msg);
		if (flogger != null)
			flogger.log(msg);
	}
	
	private static void usage() {
		System.out.println("Usage: " + AnalyzePatrolRoute.class.getName()  + " <options>\n");
		System.out.println("Where <options> include:");
		System.out.println("\t-expDir <dir name>: The directory containing the multi-robot patrol experiment log files. (required)");
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
		
		System.out.println("Exp dir: " + new File(expDir).getAbsolutePath());
		if (saveToFileName != null) 
			System.out.println("Saving results to " + saveToFileName);
		else
			System.out.println("Not saving results to file.");
		System.out.println("Debug: " + (System.getProperty ("PharosMiddleware.debug") != null));
		
		try {
			new AnalyzePatrolRoute(expDir, saveToFileName);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
