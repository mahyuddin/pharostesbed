package pharoslabut.logger.analyzer;

import java.io.*;
import java.util.Vector;

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
public class RobotLogToGPSVisualizer {

	private String outputFileName = null;
	private Vector<String> logFileNames = new Vector<String>();
	private Vector<String> captionNames = new Vector<String>();
	private Vector<String> traceColors = new Vector<String>();
	private Vector<WayPoint> waypoints = new Vector<WayPoint>();
	
	/**
	 * The constructor.
	 * 
	 * @param fileName The name of the file containing the specifications on
	 * which log files to analyze.
	 * @throws Exception If any error occurs.
	 */
	public RobotLogToGPSVisualizer(String fileName) throws Exception {

		BufferedReader input = null;
		try {
			input =  new BufferedReader(new FileReader(fileName));
		} catch (IOException ex){
			ex.printStackTrace();
			System.err.println("Unable to open " + fileName);
			System.exit(1);
		}
		
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
						captionNames.add(elem[2]);
						traceColors.add(elem[3]);
				
					}
					else if (line.contains("WAYPOINT")) {
						String[] elem = line.split("[\\s]+");
						String name = line.substring(line.indexOf(elem[3]));
						waypoints.add(new WayPoint(Double.valueOf(elem[1]), Double.valueOf(elem[2]), name));
					}
				}
				lineno++;
			}
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
		print("Usage: pharoslabut.logger.analyzer.RobotLogToGPSVisualizer <options>\n");
		print("Where <options> include:");
		print("\t-spec <spec file name>: The specification file. (required)");
		print("\t\tSyntax of specification file:");
		print("\t\t\tOUTPUT_FILE <name of output file>");
		print("\t\t\tLOG_FILE <name of log file> <caption> <color>");
		print("\t\t\t...");
		print("\t\tEach LOG_FILE listed in the specification file will have its own trace in the resulting GPSVisualizer script.");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String fileName = null;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-spec")) {
					fileName = args[++i];
				} 
				else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				}
				else {
					System.setProperty ("PharosMiddleware.debug", "true");
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			usage();
			System.exit(1);
		}
		
		if (fileName == null) {
			usage();
			System.exit(1);
		}
		
		print("File: " + fileName);
		print("Debug: " + (System.getProperty ("PharosMiddleware.debug") != null));
		
		try {
			new RobotLogToGPSVisualizer(fileName);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
