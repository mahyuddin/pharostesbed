package pharoslabut.logger.analyzer;

import java.util.Vector;

import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.Location;

/**
 * Generates a CSV file that can be submitted to GPSVisualizer.com to see
 * the routes taken by the robots and where the GPS and compass failed.
 * 
 * @author Chien-Liang Fok
 *
 */
public class VisualizeFaults {

	
	/**
	 * The constructor.
	 * 
	 * @param logFileName The name of the log file.
	 * @param robotColor The color of the robot.
	 */
	public VisualizeFaults(String logFileName, String robotColor) {
		RobotExpData robotData = new RobotExpData(logFileName);
		
		String outputFileName;
		if (logFileName.contains(".")) 
			outputFileName = logFileName.substring(0, logFileName.lastIndexOf('.')) + "-faults.csv";
		else
			outputFileName = logFileName + "-faults.csv";
		
		FileLogger flogger = new FileLogger(outputFileName, false);
		
		// First add a trace of the actual route taken by the robot
		flogger.log("type,latitude,longitude,name,color"); 
		Vector<GPSLocationState> locations = robotData.getGPSHistory();
		for (int j=0; j < locations.size(); j++) {
			Location currLoc = new Location(locations.get(j).getLoc());
			String line = "T," + currLoc.latitude() + "," + currLoc.longitude();
			if (j == 0)
				line += ", " + robotData.getMissionName() + "-" + robotData.getExpName() + "-" 
				+ robotData.getRobotName()  + ", " + robotColor;
			flogger.log(line);
		}
		
		// Add the actual waypoints of the motion script
		Vector<Location> wayPoints = robotData.getWayPoints();
		flogger.log("type,latitude,longitude,name,color");
		for (int i=0; i < wayPoints.size(); i++) {
			Location wpLoc = wayPoints.get(i);
			flogger.log("W," + wpLoc.latitude() + "," + wpLoc.longitude() + ", Way Point " + i + ",green");
		}
		
		// Add waypoints indicating the locations of GPS sensor Failure
		Vector<Long> gpsErrors = robotData.getGPSErrors();
		Logger.logDbg("Including GPS errors: " + gpsErrors.size());
		if (gpsErrors.size() > 0) {
			flogger.log("type,latitude,longitude,name,color");
			for (int i=0; i < gpsErrors.size(); i++) {
				long timestamp = gpsErrors.get(i);
				Location errorLoc = robotData.getLocation(timestamp);
				flogger.log("W," + errorLoc.latitude() + "," + errorLoc.longitude() + ", GPS Error " + i + ",red");
			}
		}
		
		// Add waypoints indicating the locations of heading sensor Failure
		Vector<Long> headingErrors = robotData.getHeadingErrors();
		if (headingErrors.size() > 0) {
			flogger.log("type,latitude,longitude,name,color");
			for (int i=0; i < headingErrors.size(); i++) {
				long timestamp = headingErrors.get(i);
				Location errorLoc = robotData.getLocation(timestamp);
				flogger.log("W," + errorLoc.latitude() + "," + errorLoc.longitude() + ", Heading Error " + i + ", yellow");
			}
		}
		
		System.out.println("Number of GPS Faults: " + gpsErrors.size());
		System.out.println("Number of Heading Faults: " + headingErrors.size());
	}
	
	private static void print(String msg) {
		System.out.println(msg);
	}
	
	private static void printErr(String msg) {
		System.err.println(msg);
	}
	
	private static void usage() {
		print("Usage: " + VisualizeFaults.class.getName() + " <options>\n");
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
			printErr("Must specify log file and output file.");
			usage();
			System.exit(1);
		}
		
		print("log file: " + logFileName);
		//print("Debug: " + (System.getProperty ("PharosMiddleware.debug") != null));
		
		try {
			new VisualizeFaults(logFileName, robotColor);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
