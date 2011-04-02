package pharoslabut.missions;

import java.util.Vector;

import pharoslabut.logger.analyzer.*;
import pharoslabut.logger.*;
import pharoslabut.navigate.Location;

/**
 * Reads a log file and generates a GPS trace file that can be used to plot the movements of a robot
 * using GPSVisualizer (http://www.gpsvisualizer.com/).
 * 
 * @author Chien-Liang Fok
 *
 */
public class GPSTraceGenerator {

	public GPSTraceGenerator() throws Exception {
		Vector<String> logFileNames = new Vector<String>();
		Vector<String> captionName = new Vector<String>();
		Vector<String> traceColor = new Vector<String>();
		
		/*
		 * Analyze the log files of robot Lonestar
		 */
//		String robotName = "Lonestar";
//		
//		logFileNames.add("M9/M9-Results/M9-Exp9/M9-Exp9-Lonestar_20101203100206.log");
//		captionName.add("Run 1");
//		traceColor.add("red");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp10/M9-Exp10-Lonestar_20101203102922.log");
//		captionName.add("Run 2");
//		traceColor.add("blue");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp11/M9-Exp11-Lonestar_20101203111154.log");
//		captionName.add("Run 3");
//		traceColor.add("purple");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp12/M9-Exp12-Lonestar_20101203114724.log");
//		captionName.add("Run 4");
//		traceColor.add("orange");
//		
//		logFileNames.add("M11/M11-Results/M11-Exp1/M11-Exp1-Lonestar_20101206054444.log");
//		captionName.add("Run 5");
//		traceColor.add("maroon");
//		
//		logFileNames.add("M11/M11-Results/M11-Exp3/M11-Exp3-Lonestar_20101206064809.log");		
//		captionName.add("Run 6");
//		traceColor.add("turqoise");
//		
//		logFileNames.add("M11/M11-Results/M11-Exp4/M11-Exp4-Lonestar_20101206071859.log");
//		captionName.add("Run 7");
//		traceColor.add("green");
		
		
//		This generates a file containing *all* runs of *all three* robots.
//		
//		String robotName = "Lonestar-Shiner-Wynkoop";
//		
//		logFileNames.add("M9/M9-Results/M9-Exp9/M9-Exp9-Lonestar_20101203100206.log");
//		captionName.add("Lonestar-1");
//		traceColor.add("red");
//		logFileNames.add("M9/M9-Results/M9-Exp10/M9-Exp10-Lonestar_20101203102922.log");
//		captionName.add("Lonestar-2");
//		traceColor.add("red");
//		logFileNames.add("M9/M9-Results/M9-Exp11/M9-Exp11-Lonestar_20101203111154.log");
//		captionName.add("Lonestar-3");
//		traceColor.add("red");
//		logFileNames.add("M9/M9-Results/M9-Exp12/M9-Exp12-Lonestar_20101203114724.log");
//		captionName.add("Lonestar-4");
//		traceColor.add("red");
//		logFileNames.add("M11/M11-Results/M11-Exp1/M11-Exp1-Lonestar_20101206054444.log");
//		captionName.add("Lonestar-5");
//		traceColor.add("red");
//		logFileNames.add("M11/M11-Results/M11-Exp3/M11-Exp3-Lonestar_20101206064809.log");		
//		captionName.add("Lonestar-6");
//		traceColor.add("red");
//		logFileNames.add("M11/M11-Results/M11-Exp4/M11-Exp4-Lonestar_20101206071859.log");
//		captionName.add("Lonestar-7");
//		traceColor.add("red");
//
//		logFileNames.add("M9/M9-Results/M9-Exp1/M9-Exp1-Shiner_20101203123303.log");
//		captionName.add("Shiner-1");
//		traceColor.add("orange");
//		logFileNames.add("M9/M9-Results/M9-Exp2/M9-Exp2-Shiner_20101203130807.log");
//		captionName.add("Shiner-2");
//		traceColor.add("orange");
//		logFileNames.add("M9/M9-Results/M9-Exp3/M9-Exp3-Shiner_20101203132921.log");
//		captionName.add("Shiner-3");
//		traceColor.add("orange");
//		logFileNames.add("M9/M9-Results/M9-Exp5/M9-Exp5-Shiner_20101203135855.log");
//		captionName.add("Shiner-4");
//		traceColor.add("orange");
//		logFileNames.add("M9/M9-Results/M9-Exp6/M9-Exp6-Shiner_20101203142245.log");
//		captionName.add("Shiner-5");
//		traceColor.add("orange");
//		logFileNames.add("M9/M9-Results/M9-Exp7/M9-Exp7-Shiner_20101203143934.log");
//		captionName.add("Shiner-6");
//		traceColor.add("orange");
//		logFileNames.add("M9/M9-Results/M9-Exp9/M9-Exp9-Shiner_20101203160528.log");
//		captionName.add("Shiner-7");
//		traceColor.add("orange");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp9/M9-Exp9-Wynkoop_20101203100336.log");
//		captionName.add("Wynkoop-1");
//		traceColor.add("blue");
//		logFileNames.add("M9/M9-Results/M9-Exp10/M9-Exp10-Wynkoop_20101203103051.log");
//		captionName.add("Wynkoop-2");
//		traceColor.add("blue");
//		logFileNames.add("M9/M9-Results/M9-Exp11/M9-Exp11-Wynkoop_20101203111323.log");
//		captionName.add("Wynkoop-3");
//		traceColor.add("blue");
//		logFileNames.add("M9/M9-Results/M9-Exp12/M9-Exp12-Wynkoop_20101203114853.log");
//		captionName.add("Wynkoop-4");
//		traceColor.add("blue");
//		logFileNames.add("M11/M11-Results/M11-Exp3/M11-Exp3-Wynkoop_20101206064822.log");
//		captionName.add("Wynkoop-5");
//		traceColor.add("blue");
//		logFileNames.add("M11/M11-Results/M11-Exp4/M11-Exp4-Wynkoop_20101206071912.log");
//		captionName.add("Wynkoop-6");
//		traceColor.add("blue");
//		logFileNames.add("M11/M11-Results/M11-Exp5/M11-Exp5-Wynkoop_20101206075054.log");
//		captionName.add("Wynkoop-7");
//		traceColor.add("blue");
		
//		String robotName = "Lonestar-MD";
//		
//		logFileNames.add("M9/M9-Results/M9-Exp9/M9-Exp9-Lonestar_20101203100206.log");
//		captionName.add("Lonestar-1");
//		traceColor.add("red");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp10/M9-Exp10-Lonestar_20101203102922.log");
//		captionName.add("Lonestar-2");
//		traceColor.add("blue");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp11/M9-Exp11-Lonestar_20101203111154.log");
//		captionName.add("Lonestar-3");
//		traceColor.add("purple");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp12/M9-Exp12-Lonestar_20101203114724.log");
//		captionName.add("Lonestar-4");
//		traceColor.add("orange");
//		
//		logFileNames.add("M11/M11-Results/M11-Exp1/M11-Exp1-Lonestar_20101206054444.log");
//		captionName.add("Lonestar-5");
//		traceColor.add("maroon");
//		
//		logFileNames.add("M11/M11-Results/M11-Exp3/M11-Exp3-Lonestar_20101206064809.log");		
//		captionName.add("Lonestar-6");
//		traceColor.add("turquoise");
//		
//		logFileNames.add("M11/M11-Results/M11-Exp4/M11-Exp4-Lonestar_20101206071859.log");
//		captionName.add("Lonestar-7");
//		traceColor.add("green");
		
//		String robotName = "Shiner-MD";
//		
//		logFileNames.add("M9/M9-Results/M9-Exp1/M9-Exp1-Shiner_20101203123303.log");
//		captionName.add("Shiner-1");
//		traceColor.add("red");
//		logFileNames.add("M9/M9-Results/M9-Exp2/M9-Exp2-Shiner_20101203130807.log");
//		captionName.add("Shiner-2");
//		traceColor.add("blue");
//		logFileNames.add("M9/M9-Results/M9-Exp3/M9-Exp3-Shiner_20101203132921.log");
//		captionName.add("Shiner-3");
//		traceColor.add("purple");
//		logFileNames.add("M9/M9-Results/M9-Exp5/M9-Exp5-Shiner_20101203135855.log");
//		captionName.add("Shiner-4");
//		traceColor.add("orange");
//		logFileNames.add("M9/M9-Results/M9-Exp6/M9-Exp6-Shiner_20101203142245.log");
//		captionName.add("Shiner-5");
//		traceColor.add("maroon");
//		logFileNames.add("M9/M9-Results/M9-Exp7/M9-Exp7-Shiner_20101203143934.log");
//		captionName.add("Shiner-6");
//		traceColor.add("turquoise");
//		logFileNames.add("M9/M9-Results/M9-Exp9/M9-Exp9-Shiner_20101203160528.log");
//		captionName.add("Shiner-7");
//		traceColor.add("green");
		
		String robotName = "Wynkoop-MD";
		
		logFileNames.add("M9/M9-Results/M9-Exp9/M9-Exp9-Wynkoop_20101203100336.log");
		captionName.add("Wynkoop-1");
		traceColor.add("red");
		logFileNames.add("M9/M9-Results/M9-Exp10/M9-Exp10-Wynkoop_20101203103051.log");
		captionName.add("Wynkoop-2");
		traceColor.add("blue");
		logFileNames.add("M9/M9-Results/M9-Exp11/M9-Exp11-Wynkoop_20101203111323.log");
		captionName.add("Wynkoop-3");
		traceColor.add("purple");
		logFileNames.add("M9/M9-Results/M9-Exp12/M9-Exp12-Wynkoop_20101203114853.log");
		captionName.add("Wynkoop-4");
		traceColor.add("orange");
		logFileNames.add("M11/M11-Results/M11-Exp3/M11-Exp3-Wynkoop_20101206064822.log");
		captionName.add("Wynkoop-5");
		traceColor.add("maroon");
		logFileNames.add("M11/M11-Results/M11-Exp4/M11-Exp4-Wynkoop_20101206071912.log");
		captionName.add("Wynkoop-6");
		traceColor.add("turquoise");
		logFileNames.add("M11/M11-Results/M11-Exp5/M11-Exp5-Wynkoop_20101206075054.log");
		captionName.add("Wynkoop-7");
		traceColor.add("green");
		
		FileLogger flogger = new FileLogger("GPSRouteTrace-" + robotName + ".csv", false /* print time stamp */);
		
		for (int i=0; i < logFileNames.size(); i++) {
			log("Processing " + logFileNames.get(i));
			flogger.log("type,latitude,longitude,name,color"); 
			
			RobotExpData ed = new RobotExpData(logFileNames.get(i));
			Vector<GPSLocationState> locations = ed.getGPSHistory();
			for (int j=0; j < locations.size(); j++) {
				Location currLoc = new Location(locations.get(j).getLoc());
				String line = "T," + currLoc.latitude() + "," + currLoc.longitude();
				if (j == 0) {
					line += ", " + captionName.get(i) + ", " + traceColor.get(i);
				}
				flogger.log(line);
			}
		}
		
		
		flogger.log("type,latitude,longitude,name,desc");
		flogger.log("W,30.52626,-97.6324133,Waypoints 1 and 11,Waypoints 1 and 11");
		flogger.log("W,30.5264833,-97.6325133,Waypoints 2 and 10,Waypoints 2 and 10");
		flogger.log("W,30.5266383,-97.6321067,Waypoints 3 and 9,Waypoints 3 and 9");
		flogger.log("W,30.52707,-97.6321167,Waypoints 4 and 8,Waypoints 4 and 8");
		flogger.log("W,30.5270783,-97.6324933,Waypoint 5");
		flogger.log("W,30.5274533,-97.632525,Waypoint 6");
		flogger.log("W,30.5274983,-97.6321333,Waypoint 7");
	}
	
	private void log(String msg) {
		System.out.println(msg);
	}
	
	public static void main(String[] args) {
		try {
			new GPSTraceGenerator();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
