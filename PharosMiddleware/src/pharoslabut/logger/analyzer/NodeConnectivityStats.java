package pharoslabut.logger.analyzer;

//import java.awt.Color;
import java.net.InetAddress;
//import java.util.Enumeration;
import java.util.Vector;

//import org.jfree.chart.ChartFactory;
//import org.jfree.chart.ChartPanel;
//import org.jfree.chart.JFreeChart;
//import org.jfree.chart.plot.PlotOrientation;
//import org.jfree.chart.plot.XYPlot;
//import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
//import org.jfree.chart.title.LegendTitle;
//import org.jfree.data.xy.XYSeries;
//import org.jfree.data.xy.XYSeriesCollection;
//import org.jfree.ui.ApplicationFrame;
//import org.jfree.ui.RectangleEdge;
//import org.jfree.ui.RefineryUtilities;
//
//import pharoslabut.beacon.WiFiBeacon;
import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;

/**
 * Analyzes a node's experiment log file and computes connectivity statistics.
 * The following connectivity statistics are computing:
 * 
 * <ul>
 * <li>Total Connections: The number of times any node is added to the neighbor list.</li>
 * <li>Average Duration: The average duration a node remains in the neighbor list after it is added.</li> 
 * <li>Average Neighbors: The average size of the neighbor list.</li>
 * <li>Unique Neighbors: The total number of unique nodes ever added to the neighbor list.</li>
 * </ul>
 * 
 * @author Chien-Liang Fok
 */
public class NodeConnectivityStats {

	private String logFileName;
	
	private RobotExpData robotData;
	
	/**
	 * The total number of times a node is added to the neighbor list.
	 */
	private int totalConnections;
	
	/**
	 * The total number of times a node is removed from the neighbor list.
	 */
	private int totalDisconnections;
	
	/**
	 * The average time a node remains in the neighbor list.
	 */
	private double averageConnectionDuration;
	
	/**
	 * The average size of the neighbor list.
	 * This is the average of the instantaneous size of the neighbor list
	 * at every sampling interval.
	 */
	private double averageNeighbors;
	
	/**
	 * The total number of unique nodes ever added to the neighbor list.
	 */
	private int numUniqueNeighbors;
	
	/**
	 * The number of milliseconds that a node can remain within the neighbor list
	 * without receiving a beacon.
	 */
	private long disconnectionInterval;
	
	/**
	 * The interval at which to calculate the size of the neighbor list in milliseconds.
	 */
	private long samplingInterval;
	
	/**
	 * The constructor.
	 * 
	 * @param logFileName The experiment log file to analyze.
	 * @param samplingInterval  The interval at which to calculate the size of the neighbor list in milliseconds.
	 * @param disconnectionInterval The number of milliseconds that a node can remain within the neighbor list
	 * without receiving a beacon.
	 * @param saveToFile whether to save the error calculations to a file.
	 */
	public NodeConnectivityStats(String logFileName, long samplingInterval, long disconnectionInterval, boolean saveToFile) {
		this.logFileName = logFileName;
		this.samplingInterval = samplingInterval;
		this.disconnectionInterval = disconnectionInterval;
		robotData = new RobotExpData(logFileName);
		
		calcStats();
		printResults(saveToFile);
	}
	
	private void calcStats() {
		Vector<WiFiBeaconRx> rxEvents = robotData.getWiFiBeaconRxs();
		
		/*
		 * Initialize the number of total connections and disconnection to be zero.
		 * We will increment these each time a neighbor is added to or removed from the
		 * neighbor list.
		 */
		totalConnections = totalDisconnections = 0;
		
		/*
		 * Maintain a list of connection durations.  This will be used to calculate the average connection duration.
		 */
		Vector<Long> connectionDuration = new Vector<Long>();
		
		/*
		 * Maintain a running neighbor list so we can determine the state of the system
		 * at each time a beacon is received.
		 */
		NbrList nbrList = new NbrList(disconnectionInterval);
		
		// Go through the list of beacon receptions while
		// maintaining a neighbor list.  The neighbor list will 
		// only contain the nodes whos beacons were received
		// within the beacon timeout interval.  Keep a count of the number of
		// times a node is added to the neighbor list.
		for (int i=0; i < rxEvents.size(); i++) {
			
			WiFiBeaconRx currBeaconEvent = rxEvents.get(i);
			
			// First remove the disconnected nodes and update the list of connection durations.
			Vector<Long> removedDurations = nbrList.clearDisconnected(currBeaconEvent.getTimestamp());
			connectionDuration.addAll(removedDurations);
			totalDisconnections += removedDurations.size();
			
			// Next, if the beacon does not belong to the local robot,
			// add or update the beacon's sender in the list
			if (currBeaconEvent.getSenderID() != robotData.getRobotID()) {
				if (nbrList.update(currBeaconEvent))
					totalConnections++;
			}
		}
		
		// Determine the final state of the neighbor list at the end of the experiment.
		Logger.logDbg("Determining the final state of the neighbor list at the end of the experiment.");
		Vector<Long> removedDurations = nbrList.clearDisconnected(robotData.getStopTime());
		connectionDuration.addAll(removedDurations);
		totalDisconnections += removedDurations.size();
		
		// Calculate the average connection duration
		long totalConnDur = 0;
		for (int i=0; i < connectionDuration.size(); i++) {
			totalConnDur += connectionDuration.get(i);
		}
		averageConnectionDuration = totalConnDur / (double)connectionDuration.size();
		
		// Calculate the number of unique neighbors
		numUniqueNeighbors = nbrList.numUniqueNbrs();
		
		// Calculate the average size of the neighbor list.
		Logger.logDbg("Calculating the average size of the neighbor list.");
		long totalNbrListSize = 0;
		int countNbrListSize = 0;
		for (long currTime = robotData.getStartTime(); currTime < robotData.getStopTime(); currTime += samplingInterval) {
			totalNbrListSize += getNbrListSize(currTime);
			countNbrListSize++;
		}
		averageNeighbors = totalNbrListSize / (double)countNbrListSize;
	}
	
	/**
	 * Computes the size of the neighbor list at a specific time.
	 * 
	 * @param currTime The time to consider.
	 * @return The size of the neighbor list at the specified time.
	 */
	private int getNbrListSize(long currTime) {
		NbrList nbrList = NbrList.getNbrList(robotData, currTime, disconnectionInterval);
//		
//		Vector<WiFiBeaconRx> rxEvents = robotData.getWiFiBeaconRxs();
//		NbrList list = new NbrList(disconnectionInterval);
//		
//		for (int i=0; i < rxEvents.size(); i++) {
//			WiFiBeaconRx currRxEvent = rxEvents.get(i);
//			if (currRxEvent.getTimestamp() < currTime)
//				list.update(currRxEvent);
//		}
//		
//		list.clearDisconnected(currTime);
		
		return nbrList.size();
	}
	
	private void printResults(boolean saveToFile) {
		FileLogger flogger = null;
		if (saveToFile) {
			String fileName;
			if (logFileName.contains("."))
				fileName = logFileName.substring(0, logFileName.lastIndexOf('.')) + "-ConnStats.txt";
			else
				fileName = logFileName + "-headingSpeed.txt";

			Logger.log("Saving data to " + fileName);
			flogger = new FileLogger(fileName, false);
		}
		
		log("Total Connections: " + totalConnections, flogger);
		log("Total Disconnections: " + totalDisconnections, flogger);
		log("Average Connection Duration: " + averageConnectionDuration, flogger);
		log("Average Neighbor List Size: " + averageNeighbors, flogger);
		log("Total Unique Neighbors: " + numUniqueNeighbors, flogger);
	}
	
	/**
	 * This is used internally to print text to the screen and to a file.
	 * 
	 * @param msg The message to print and save.
	 * @param flogger The file logger through which to save the message to a file.
	 */
	private void log(String msg, FileLogger flogger) {
		System.out.println(msg);
		if (flogger != null)
			flogger.log(msg);
	}
	
	private static void print(String msg) {
		System.out.println(msg);
	}
	
	private static void printErr(String msg) {
		System.err.println(msg);
	}
	
	private static void usage() {
		print("Usage: " + NodeConnectivityStats.class.getName()  + " <options>\n");
		print("Where <options> include:");
		print("\t-log <log file name>: The log file generated during the experiment. (required)");
		print("\t-save: Save the node connectivity stats to a text file.  The name will be the same as the log file but ending with \"-ConnStats.txt\" (optional)");
		print("\t-samplingInterval <sampling interval>: The interval in milliseconds at which to calculate the instantaneous size of the neighbor list. This is for calculating the average size of the neighbor list (optional, default 10000)");
		print("\t-disconnectionInterval <timeout interval>: The interval in milliseconds that a node may remain in a neighbor list without a beacon being received (optional, default 6000)");
		print("\t-debug or -d: Enable debug mode");
	}
	
	public static void main(String[] args) {
		String logFileName = null;
		long samplingInterval = 10000;
		long disconnectionInterval = 6000;
		boolean saveToFile = false;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-h") || args[i].equals("-help")) {
					usage();
					System.exit(0);
				}
				else if (args[i].equals("-save")) {
					saveToFile = true;
				}
				else if (args[i].equals("-samplingInterval")) {
					samplingInterval = Long.valueOf(args[++i]);
				}
				else if (args[i].equals("-disconnectionInterval")) {
					disconnectionInterval = Long.valueOf(args[++i]);
				}
				else if (args[i].equals("-log")) {
					logFileName = args[++i];
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
		
		print("Log file: " + logFileName);
		print("Debug: " + (System.getProperty ("PharosMiddleware.debug") != null));
		
		try {
			new NodeConnectivityStats(logFileName, samplingInterval, disconnectionInterval, saveToFile);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
