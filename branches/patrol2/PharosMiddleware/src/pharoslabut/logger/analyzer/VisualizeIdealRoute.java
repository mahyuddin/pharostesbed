package pharoslabut.logger.analyzer;

import java.util.Vector;

import pharoslabut.logger.FileLogger;
import pharoslabut.navigate.Location;

/**
 * Generates a CSV file that can be submitted to GPSVisualizer.com to see
 * the ideal route that should have been taken by the robot.
 * 
 * @author Chien-Liang Fok
 */
public class VisualizeIdealRoute {

	
	/**
	 * The constructor.
	 * 
	 * @param logFileName The name of the log file.
	 * @param robotColor The color of the robot.
	 */
	public VisualizeIdealRoute(String logFileName, String robotColor) {
		RobotExpData robotData = new RobotExpData(logFileName);
		
		String outputFileName;
		if (logFileName.contains(".")) 
			outputFileName = logFileName.substring(0, logFileName.lastIndexOf('.')) + "-idealRoute.csv";
		else
			outputFileName = logFileName + "-idealRoute.csv";
		
		FileLogger flogger = new FileLogger(outputFileName, false);
		
		Vector<Location> wayPoints = robotData.getWayPoints();
		
		// First add a trace of the ideal route to be taken by the robot
		flogger.log("type,latitude,longitude,name,color"); 
		for (int i=0; i < wayPoints.size(); i++) {
			Location wpLoc = wayPoints.get(i);
			String line = "T," + wpLoc.latitude() + "," + wpLoc.longitude();
			if (i == 0)
				line += ", " + robotData.getMissionName() + "-" + robotData.getExpName() + "-" 
				+ robotData.getRobotName()  + ", " + robotColor;
			flogger.log(line);
		}
		
		// Add the actual waypoints of the motion script
		
		flogger.log("type,latitude,longitude,name,color");
		for (int i=0; i < wayPoints.size(); i++) {
			Location wpLoc = wayPoints.get(i);
			flogger.log("W," + wpLoc.latitude() + "," + wpLoc.longitude() + ", Way Point " + i + ",green");
		}
		
		System.out.println("CSV file saved to " + outputFileName);
	}
	
	private static void print(String msg) {
		System.out.println(msg);
	}
	
	private static void printErr(String msg) {
		System.err.println(msg);
	}
	
	private static void usage() {
		print("Usage: " + VisualizeIdealRoute.class.getName() + " <options>\n");
		print("Where <options> include:");
		print("\t-log <log file>: The log file to analyze (required)");
		print("\t-color <color>: The color of the robot's line (optional, default blue)");
		print("\t-d: enable debug mode");
	}
	
	public static void main(String[] args) {
		String logFileName = null;
		String robotColor = "blue";
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-h") || args[i].equals("-help")) {
					usage();
					System.exit(0);
				}
				
				if (args[i].equals("-log")) {
					logFileName = args[++i];
				}
				else if (args[i].equals("-color")) {
					robotColor = args[++i];
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
		
		print("log file: " + logFileName);
		//print("Debug: " + (System.getProperty ("PharosMiddleware.debug") != null));
		
		try {
			new VisualizeIdealRoute(logFileName, robotColor);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
