package pharoslabut.logger.analyzer;

import java.io.*;
import java.util.*;

import pharoslabut.logger.*;
import pharoslabut.navigate.Location;

/**
 * Reads log files created by robots as they follow a motion script, 
 * and generates a CSV text file that can be used by GPSVisualizer to
 * plot the traces on a map.  
 * 
 * @author Chien-Liang Fok
 *
 */
public class GPSVisualize {
	// These colors were taken from: http://www.angelfire.com/wa/rogerswhome/colorchart.html
	public static final String[] COLORS = {"red", "blue", "purple", "orange",  "turquoise", "green", "gold", 
			"hotpink", "olive", "purple", "tomato", "indianred", "lavender", "deeppink", 
			"lemonchiffon", "lightsalmon", "lightsteelblue",  "lightblue", "lightslategray", 
			"maroon", "peru", "saddlebrown"};
	
	/**
	 * The constructor.
	 * 
	 * @param fileName The name of the file containing the specifications on
	 * which log files to analyze.
	 * @throws Exception If any error occurs.
	 */
	public GPSVisualize(String fileName) throws Exception {

		BufferedReader input = null;
		try {
			input =  new BufferedReader(new FileReader(fileName));
		} catch (IOException ex){
			ex.printStackTrace();
			System.err.println("Unable to open " + fileName);
			System.exit(1);
		}
		
		String outputFileName = null;
		Vector<String> logFileNames = new Vector<String>();
		Vector<String> captionNames = new Vector<String>();
		Vector<String> traceColors = new Vector<String>();
		Vector<WayPoint> waypoints = new Vector<WayPoint>();
		
		try {
			String line = null;
			int lineno = 1;
			while (( line = input.readLine()) != null) {
				if (!line.equals("")) {
					if (line.contains("OUTPUT_FILE")) {
						String[] elem = line.split("[\\s]+");
						try {
							outputFileName = elem[1];
						} catch(Exception e) {
							e.printStackTrace();
							System.err.println("Warning: Syntax error on line " + lineno + " of config file " + fileName + ":\n" + line);
							System.exit(1);
						}
					}
					else if (line.contains("LOG_FILE")) {
						String[] elem = line.split("[\\s]+");
					
						logFileNames.add(elem[1]);
						if (elem.length > 2)
							captionNames.add(elem[2]);  // user specified the caption name
						else {
							String robotName = extractRobotName(elem[1]); // use the robot name as the caption
							captionNames.add(robotName);
						}
						
						if (elem.length > 3)
							traceColors.add(elem[3]); // user specified the color
						else {
							String color = getColor(traceColors); // select a unique color automatically
							traceColors.add(color);
						}
					}
					else if (line.contains("WAYPOINT")) {
						String[] elem = line.split("[\\s]+");
						
						// If specification file includes name of waypoint, use it.
						// Otherwise, give it a sequential name.
						String name;
						if (elem.length == 4)
							name = line.substring(line.indexOf(elem[3]));
						else
							name = "Waypoint " + (waypoints.size()+1);
						
						double latlong1 = Double.valueOf(elem[1]);
						double latlong2 = Double.valueOf(elem[2]);
						if (latlong1 > 0) // assume the latitude is always > zero
							waypoints.add(new WayPoint(latlong1, latlong2, name));
						else
							waypoints.add(new WayPoint(latlong2, latlong1, name));
					}
				}
				lineno++;
			}
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		createGPSVisualizerFile(outputFileName, logFileNames, captionNames, traceColors, waypoints);
	}
	
	/**
	 * Extract the robot's name from it's log file.  
	 * Assumes the log file is of form "M##-Exp##-RobotName-Pharos_##.log"
	 * 
	 * @param logFileName The name of the log file
	 * @return The name of the robot.
	 */
	private String extractRobotName(String logFileName) {
		String robotName = null;
		
		String[] pathTokens = logFileName.split("/");
		for (int i=0; i < pathTokens.length; i++) {
			String currToken = pathTokens[i];
			if (currToken.endsWith(".log")) {
				String[] fileNameTokens = currToken.split("-");
				if (fileNameTokens.length > 3 && fileNameTokens[0].matches("M\\d+") && 
						fileNameTokens[1].matches("Exp\\d+")) 
				{
					robotName = fileNameTokens[2];
				}
			}
		}
		
		if (robotName == null) {
			System.err.println("ERROR: Unable to determine robot name: " + logFileName);
			new Exception().printStackTrace();
			System.exit(1);
		}
		
		return robotName;
	}
	
	/**
	 * Returns a unique color that is not already in the traceColors vector.
	 * 
	 * @param traceColors A vector of already-used colors
	 * @return A new color that is not already used.
	 */
	private String getColor(Vector<String> traceColors) {
		String result = null;
		int colorIndx = 0;
		
		while (result == null && colorIndx < COLORS.length) {
			// check if COLORS[colorIndx] is used
			boolean containsColor = false;
			Enumeration<String> e = traceColors.elements();
			while (e.hasMoreElements()) {
				if (e.nextElement().equals(COLORS[colorIndx]))
					containsColor = true;
			}
			
			if (!containsColor)
				result = COLORS[colorIndx];
			else
				colorIndx++;
		}
		
		if (result == null) {
			System.err.println("ERROR: Could not find a unique color!");
			new Exception().printStackTrace();
			System.exit(1);
		}
		return result;
	}
	
	/**
	 * A constructor for visualizing a single log file.
	 * 
	 * @param outputFileName The name of the file to be submitted to GPSVisualizer.
	 * @param logFileName The log file containing the robot's data.
	 * @param caption The caption for the trace generated by GPSVisualizer.
	 * @param color The color of the trace.
	 */
	public GPSVisualize(String outputFileName, String logFileName, String caption, String color) {
		
		Vector<String> logFileNames = new Vector<String>();
		Vector<String> captionNames = new Vector<String>();
		Vector<String> traceColors = new Vector<String>();
		Vector<WayPoint> waypoints = new Vector<WayPoint>();
		
		logFileNames.add(logFileName);
		captionNames.add(caption);
		traceColors.add(color);
		
		createGPSVisualizerFile(outputFileName, logFileNames, captionNames, traceColors, waypoints);
	}
	
	private void createGPSVisualizerFile(String outputFileName, Vector<String> logFileNames, 
			Vector<String> captionNames, Vector<String> traceColors, Vector<WayPoint> waypoints) 
	{
		FileLogger flogger = new FileLogger(outputFileName + ".csv", false /* print time stamp */);
		
		for (int i=0; i < logFileNames.size(); i++) {
			log("Processing " + logFileNames.get(i));
			flogger.log("type,latitude,longitude,name,color"); 
			
			RobotExpData ed = new RobotExpData(logFileNames.get(i));
			Vector<GPSLocationState> locations = ed.getGPSHistory();
			for (int j=0; j < locations.size(); j++) {
				Location currLoc = new Location(locations.get(j).getLoc());
				String line = "T," + currLoc.latitude() + "," + currLoc.longitude();
				if (j == 0)
					line += ", " + captionNames.get(i) + ", " + traceColors.get(i);
				flogger.log(line);
			}
		}
		
		
		if (waypoints.size() > 0) {
			log("Adding waypoints...");
			flogger.log("type,latitude,longitude,name");
			for (int i=0; i < waypoints.size(); i++) {
				WayPoint wp = waypoints.get(i);
				flogger.log("W," + wp.getLat() + "," + wp.getLon() + "," + wp.getName());
			}
		}
		log("done!");
	}
	
	private void log(String msg) {
		System.out.println(msg);
	}
	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(msg);
	}
	
	private static void usage() {
		System.setProperty ("PharosMiddleware.debug", "true");
		print("Usage: pharoslabut.logger.analyzer.GPSVisualize <options>\n");
		print("Where <options> include:");
		print("\t-spec <spec file name>: The specification file. (required)");
		print("\t\tSyntax of specification file:");
		print("\t\t\tOUTPUT_FILE <name of output file>");
		print("\t\t\tLOG_FILE <name of log file> <caption> <color>");
		print("\t\t\t...");
		print("\t\tEach LOG_FILE listed in the specification file will have its own trace in the resulting GPSVisualizer script.");
		print("\t-log <log file name>: The name of the log file that was recorded by the robot as it carried out an experiment (default null)");
		print("\t-caption <caption name>: The caption for the trace (default null)");
		print("\t-color <color>: The color used to plot the trace (default red)");
		print("\t-output <output file name>: The name of the output file (default GPSVisualize)");
		print("\t\tNote: the \".csv\" extension is automatically apptended");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String specFileName = null;
		
		String logFileName = null;
		String caption = null;
		String color = "red";
		String outputFile = "GPSVisualize";
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-spec")) {
					specFileName = args[++i];
				}
				else if (args[i].equals("-log")) {
					logFileName = args[++i];
				}
				else if (args[i].equals("-caption")) {
					caption = args[++i];
				}
				else if (args[i].equals("-color")) {
					color = args[++i];
				}
				else if (args[i].equals("-output")) {
					outputFile = args[++i];
				}
				else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				}
				else {
					System.setProperty ("PharosMiddleware.debug", "true");
					print("Unknown option: " + args[i]);
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			usage();
			System.exit(1);
		}
		
		if (specFileName == null && logFileName == null) {
			System.setProperty ("PharosMiddleware.debug", "true");
			print("Must set either specify specification file or log file.");
			usage();
			System.exit(1);
		}
		
		if (specFileName != null) {
			// Generate GPS visualization based on a specification file (this may plot multiple robots)
			print("SpecFile: " + specFileName);
			print("Debug: " + (System.getProperty ("PharosMiddleware.debug") != null));
			try {
				new GPSVisualize(specFileName);
			} catch(Exception e) {
				e.printStackTrace();
			}
		} else {
			// Generate a GPS visualization for a single robot.
			if (caption == null) {
				System.setProperty ("PharosMiddleware.debug", "true");
				print("ERROR: Unknown caption.");
				usage();
				System.exit(1);
			}
			
			new GPSVisualize(outputFile, logFileName, caption, color);
		}
	}
}
