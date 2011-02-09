package pharoslabut.missions;

import java.util.Vector;

import pharoslabut.logger.FileLogger;
import pharoslabut.logger.analyzer.ExpData;
import pharoslabut.logger.analyzer.LogFileReader;

/**
 * Calculates the "lateness" of a robot reaching each waypoint.  The lateness is the difference in the 
 * expected the time robot arrives at a waypoint and the time at which it actually arrived at the way point.
 * The expected time it should have arrived is the distance to the waypoint divided by the average speed
 * the robot traveled along the the edge to the waypoint.
 * 
 * @author Chien-Liang Fok
 *
 */
public class LatenessAnalyzer {

	/**
	 * The constructor.
	 */
	public LatenessAnalyzer() throws Exception {
		Vector<String> logFileNames = new Vector<String>();
		Vector<String> filePrefixes = new Vector<String>();
		
		/*
		 * Analyze the log files of robot Lonestar
		 */
		String robotName = "Lonestar";
		
		logFileNames.add("M9/M9-Results/M9-Exp9/M9-Exp9-Lonestar_20101203100206.log");
		filePrefixes.add("M9-Exp9-Lonestar");
		
		logFileNames.add("M9/M9-Results/M9-Exp10/M9-Exp10-Lonestar_20101203102922.log");
		filePrefixes.add("M9-Exp10-Lonestar");
		
		logFileNames.add("M9/M9-Results/M9-Exp11/M9-Exp11-Lonestar_20101203111154.log");
		filePrefixes.add("M9-Exp11-Lonestar");
		
		logFileNames.add("M9/M9-Results/M9-Exp12/M9-Exp12-Lonestar_20101203114724.log");
		filePrefixes.add("M9-Exp12-Lonestar");
		
		logFileNames.add("M11/M11-Results/M11-Exp1/M11-Exp1-Lonestar_20101206054444.log");
		filePrefixes.add("M11-Exp1-Lonestar");
		
		logFileNames.add("M11/M11-Results/M11-Exp3/M11-Exp3-Lonestar_20101206064809.log");
		filePrefixes.add("M11-Exp3-Lonestar");
		
		logFileNames.add("M11/M11-Results/M11-Exp4/M11-Exp4-Lonestar_20101206071859.log");
		filePrefixes.add("M11-Exp4-Lonestar");
		
		
//		String robotName = "Shiner";
//		
//		logFileNames.add("M9/M9-Results/M9-Exp1/M9-Exp1-Shiner_20101203123303.log");
//		filePrefixes.add("M9-Exp1-Shiner");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp2/M9-Exp2-Shiner_20101203130807.log");
//		filePrefixes.add("M9-Exp2-Shiner");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp3/M9-Exp3-Shiner_20101203132921.log");
//		filePrefixes.add("M9-Exp3-Shiner");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp5/M9-Exp5-Shiner_20101203135855.log");
//		filePrefixes.add("M9-Exp5-Shiner");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp6/M9-Exp6-Shiner_20101203142245.log");
//		filePrefixes.add("M9-Exp6-Shiner");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp7/M9-Exp7-Shiner_20101203143934.log");
//		filePrefixes.add("M9-Exp7-Shiner");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp9/M9-Exp9-Shiner_20101203160528.log");
//		filePrefixes.add("M9-Exp9-Shiner");
				
//		String robotName = "Wynkoop";
//		
//		logFileNames.add("M9/M9-Results/M9-Exp9/M9-Exp9-Wynkoop_20101203100336.log");
//		filePrefixes.add("M9-Exp9-Wynkoop");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp10/M9-Exp10-Wynkoop_20101203103051.log");
//		filePrefixes.add("M9-Exp10-Wynkoop");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp11/M9-Exp11-Wynkoop_20101203111323.log");
//		filePrefixes.add("M9-Exp11-Wynkoop");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp12/M9-Exp12-Wynkoop_20101203114853.log");
//		filePrefixes.add("M9-Exp12-Wynkoop");
//		
//		logFileNames.add("M11/M11-Results/M11-Exp3/M11-Exp3-Wynkoop_20101206064822.log");
//		filePrefixes.add("M11-Exp3-Wynkoop");
//		
//		logFileNames.add("M11/M11-Results/M11-Exp4/M11-Exp4-Wynkoop_20101206071912.log");
//		filePrefixes.add("M11-Exp4-Wynkoop");
//		
//		logFileNames.add("M11/M11-Results/M11-Exp5/M11-Exp5-Wynkoop_20101206075054.log");
//		filePrefixes.add("M11-Exp5-Wynkoop");
		
		/*
		 * Read the log files, store the information contained within them in vector expData.
		 */
		Vector<ExpData> expData = new Vector<ExpData>();
		for (int i = 0; i < logFileNames.size(); i++) {
			ExpData ed = LogFileReader.readLogFile(logFileNames.get(i));
			expData.add(ed);
		}
		
		// Initialize the FileLogger that will save the results into a file
		FileLogger flogger = new FileLogger(robotName + "-Lateness.txt", false /* print time stamp */);
		
		// Create the table header
		StringBuffer tableHeader = new StringBuffer();
		tableHeader.append("To Waypoint");
		for (int i=0; i < filePrefixes.size(); i++) {
			tableHeader.append("\t" + filePrefixes.get(i));
		}
		log(flogger, tableHeader.toString());
		
		int numWayPoints = expData.get(0).numEdges();
		
		for (int wayPoint = 0; wayPoint < numWayPoints; wayPoint++) {
			StringBuffer sb = new StringBuffer();
			sb.append(wayPoint+1);
				
			
			for (int i=0; i < expData.size(); i++) {
				sb.append("\t" + expData.get(i).getLatenessTo(wayPoint));
			}
			
			log(flogger, sb.toString());
		}
	}
	
	private void log(FileLogger flogger, String message) {
		System.out.println(message);
		flogger.log(message);
	}
	
	public static final void main(String[] args) {
		try {
			new LatenessAnalyzer();	
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	} 
}
