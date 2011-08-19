package pharoslabut.logger.analyzer;

import java.util.Vector;
import java.util.Enumeration;
import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;

/**
 * Computes the number of neighbors each node has throughout the length of an experiment.
 * 
 * @author Chien-Liang Fok
 */
public class ConnectivityVsTime {
	/**
	 * The window of time over which the received TelosB beacons will be used to 
	 * determine connectivity.  
	 */
	public static final long TELOSB_CONNECTIVITY_WINDOW = 5000;
	
	private ExpData expData;
	
	/**
	 * The constructor.
	 * 
	 * @param expDir The directory containing the data from the experiment.
	 */
	public ConnectivityVsTime(String expDir) {
		expData = new ExpData(expDir);
	}
	
	public void analyzeTelosConnectivityDegreeVsTime() {
		
		Vector<String> fileNames = new Vector<String>();
		Vector<String> robotNames = new Vector<String>();
		
		// For each robot in the experiment
		Enumeration<RobotExpData> robotEnum = expData.getRobotEnum();
		while (robotEnum.hasMoreElements()) {
			RobotExpData currRobot = robotEnum.nextElement();
			
			if (currRobot.ranExperiment()) {
				String fileName = expData.getExpName() + "-ConnectivityVsTime-" + currRobot.getRobotName() + ".data";
				FileLogger resultsLogger = new FileLogger(fileName, false);
				fileNames.add(fileName);
				robotNames.add(currRobot.getRobotName());

				long expStartTime = expData.getExpStartTime();
				long expStopTime = expData.getExpStopTime();

				if (expStopTime < expStartTime) {
					Logger.logErr("Experiment stop time (" + expStopTime + ") earlier than start time (" + expStartTime + ")");
					Enumeration<RobotExpData> e = expData.getRobotEnum();
					while (e.hasMoreElements()) {
						currRobot = e.nextElement();	
						Logger.logErr(currRobot.getRobotName() + "\t" + currRobot.getStartTime() + "\t" + currRobot.getStopTime());
					}
					System.exit(1);
				}

				int dataCount = 0;

				// For each time window in the experiment
				for (long time = expStartTime + TELOSB_CONNECTIVITY_WINDOW; time < expStopTime; 
				time += TELOSB_CONNECTIVITY_WINDOW) 
				{
					// Get the connectivity of the robot at that time
					int numNbrs = currRobot.getTelosBConnectivity(time, TELOSB_CONNECTIVITY_WINDOW).size();

					// Save the data in a file
					resultsLogger.log(time + "\t" + (time - expStartTime)/1000 + "\t" + numNbrs);
					dataCount++;
				}

				if (dataCount == 0) {
					Logger.logErr("No data for robot " + currRobot.getRobotName() + " in experiment " + expData.getExpName() + ", startTime = " + expStartTime + ", stopTime = " + expData.getExpStopTime());
					System.exit(1);
				}
			}
		}
		
		// Generate the GNUPlot script
		FileLogger gnuPlotLogger = new FileLogger(expData.getExpName() + "-ConnectivityVsTime.gnuplot", false);
		gnuPlotLogger.log("set title \"" + expData.getExpName() + " cc2420 Connectivity Vs. Time (" + (TELOSB_CONNECTIVITY_WINDOW/1000) + "s window)");
		gnuPlotLogger.log("set xlabel \"Time (s)\"");
		gnuPlotLogger.log("set yrange [0:" + expData.numRobots() + "]");
		gnuPlotLogger.log("set ylabel \"Connectivity (Number of Neighbors)");
		gnuPlotLogger.log("set key outside below");
		gnuPlotLogger.log("plot \\");
		for (int i=0; i < fileNames.size(); i++) {
			String line = "     \"" + fileNames.get(i) + "\" using 2:3 with lines title \"" + robotNames.get(i) + "\"";
			if (i < fileNames.size() - 1)
				line += ", \\";
			gnuPlotLogger.log(line);
		}
		gnuPlotLogger.log("set terminal png");
		gnuPlotLogger.log("set output \"" + expData.getExpName() + "-ConnectivityVsTime.png\"");
		gnuPlotLogger.log("replot");
	}
	
//	private void logErr(String msg) {
//		String result = "ConnectivityVsTime: " + msg; 
//		System.err.println(result);
//		if (flogger != null)
//			flogger.log(result);
//	}
//	
//	private void log(String msg) {
//		ConnectivityVsTime.log(msg, this.flogger);
//	}
	
//	private static void log(String msg, FileLogger flogger) {
//		String result = "ConnectivityVsTime: " + msg;
//		if (System.getProperty ("PharosMiddleware.debug") != null) 
//			System.out.println(result);
//		if (flogger != null)
//			flogger.log(result);
//	}
//	
//	private static void print(String msg, FileLogger flogger) {
//		System.out.println(msg);
//		if (flogger != null) {
//			flogger.log(msg);
//		}
//	}
	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(msg);
	}
	
	private static void usage() {
		System.setProperty ("PharosMiddleware.debug", "true");
		print("Usage: " + ConnectivityVsTime.class.getName() + " <options>\n");
		print("Where <options> include:");
		print("\t-expDir <experiment data directory>: The directory containing experiment data (required)");
		print("\t-log <log file name>: The file in which to log debug statements (default null)");
		print("\t-telos: Analyze signal strength vs. distance of TelosB mote.");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String expDir = null;
		//String outputFile = null;
		boolean analyzeTelos = false;
		
		// Process the command line arguments...
		try {
			for (int i=0; i < args.length; i++) {
		
				if (args[i].equals("-log"))
					Logger.setFileLogger(new FileLogger(args[++i], false));
				else if (args[i].equals("-expDir"))
					expDir = args[++i];
				else if (args[i].equals("-debug") || args[i].equals("-d"))
					System.setProperty ("PharosMiddleware.debug", "true");
				else if (args[i].equals("-telos"))
					analyzeTelos = true;
				else {
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
			usage();
			System.exit(1);
		}
		
		ConnectivityVsTime analyzer = new ConnectivityVsTime(expDir);
		
		if (analyzeTelos) {
			// Perform the actual analysis...
			
			//FileLogger outputLogger = new FileLogger(outputFile, false);
			
			//log("Analyzing all TelosB connectivity vs. time in experiment " + expDir + "...", flogger);
			analyzer.analyzeTelosConnectivityDegreeVsTime();			
		}
	}
}
