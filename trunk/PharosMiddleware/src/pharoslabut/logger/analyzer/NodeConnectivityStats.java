package pharoslabut.logger.analyzer;

//import java.awt.Color;
import java.awt.Color;
import java.io.File;
//import java.io.IOException;
//import java.net.InetAddress;
//import java.util.Enumeration;
import java.util.Enumeration;
import java.util.Vector;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
//import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
//import org.jfree.data.xy.XYIntervalSeries;
//import org.jfree.data.xy.XYIntervalSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

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
import pharoslabut.util.AverageStatistic;

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
	
	private ExpData expData;
	
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
	private AverageStatistic averageConnectionDuration;
	
	/**
	 * The average size of the neighbor list.
	 * This is the average of the instantaneous size of the neighbor list
	 * at every sampling interval.
	 */
	private AverageStatistic averageNeighbors;
	
	private Vector<NeighborListState> nbrListStates = new Vector<NeighborListState>();
	
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
		
		// Get the experiment info.  This is needed to get the start time of the experiment.
		File f = new File(logFileName);
		String path = f.getAbsolutePath();
		path = path.substring(0, path.lastIndexOf('/'));
		expData = new ExpData(path);
		
		calcStats();
		printResults(saveToFile);
		
		showChart();
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
		Vector<Double> tempVec = new Vector<Double>();
		Enumeration<Long> e = connectionDuration.elements(); // Convert the connection duration vector into a vector of doubles.
		while (e.hasMoreElements()) {
			tempVec.add((double)e.nextElement());
		}
		averageConnectionDuration = new AverageStatistic(tempVec);
		
		// Calculate the number of unique neighbors
		numUniqueNeighbors = nbrList.numUniqueNbrs();
		
		// Calculate the average size of the neighbor list.
		Logger.logDbg("Calculating the average size of the neighbor list.");
		tempVec = new Vector<Double>();
		for (long currTime = expData.getExpStartTime(); currTime < expData.getExpStopTime(); currTime += samplingInterval) {
			int nbrListSize = getNbrListSize(currTime);
			tempVec.add((double)nbrListSize);
			nbrListStates.add(new NeighborListState(currTime, nbrListSize));
		}
		averageNeighbors = new AverageStatistic(tempVec);
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
		
		// Get the start time of the experiment.
		long startTime = expData.getExpStartTime();
		
		log("Time (ms)\tExperiment Time (ms)\tNeighbor List Size", flogger);
		for (int i=0; i < nbrListStates.size(); i++) {
			NeighborListState nbrListState = nbrListStates.get(i);
			log(nbrListState.timestamp + "\t" + (nbrListState.timestamp - startTime) + "\t" + nbrListState.size, flogger);
		}
		
		log("Overall per-node statistics:", flogger);
		log("Total Connections: " + totalConnections, flogger);
		log("Total Disconnections: " + totalDisconnections, flogger);
		log("Average Connection Duration (ms): " + averageConnectionDuration, flogger);
		log("Average Neighbor List Size: " + averageNeighbors, flogger);
		log("Total Unique Neighbors: " + numUniqueNeighbors, flogger);
	}
	
	/**
	 * Plots the experiment connectivity statistics on charts for easy visualization.
	 */
	private void showChart() {
		// Create the various data series...
		XYSeries numNbrsSeries = new XYSeries("Number of Neighbors");

		long expStartTime = expData.getExpStartTime();
		
		// Fill in the data series
		for (int i=0; i < nbrListStates.size(); i++) {
			NeighborListState nbrListState = nbrListStates.get(i);
			numNbrsSeries.add((nbrListState.timestamp - expStartTime) / 1000.0, nbrListState.size);
			
		}

		// Create data sets for each series
		XYSeriesCollection numNbrsDataSet = new XYSeriesCollection();
		numNbrsDataSet.addSeries(numNbrsSeries);
		
		
		// Create the charts
		JFreeChart numNeighborsChart = ChartFactory.createXYLineChart(
				"Number of Neighbors vs. Time",                        // chart title
				"Time (s)",                                            // x axis label
				"Num. Neighbors",                                      // y axis label
				numNbrsDataSet,                                        // the number of neighbors data
				PlotOrientation.VERTICAL,                              // plot orientation (y axis is vertical)
				false,                                                 // include legend
				true,                                                  // tooltips
				false                                                  // urls
		);
		
		
       
        
        // Place the legend on top of the chart just below the title.
//        LegendTitle headingLegend = headingChart.getLegend();
//        headingLegend.setPosition(RectangleEdge.TOP);
//        LegendTitle speedLegend = speedChart.getLegend();
//        speedLegend.setPosition(RectangleEdge.TOP);
        
		numNeighborsChart.setBackgroundPaint(Color.white);
        
        // Configure when to display lines an when to display the shapes that indicate data points
        XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer();
        renderer1.setSeriesLinesVisible(0, true); // display the heading as a line
        renderer1.setSeriesShapesVisible(0, false);
        
//        XYErrorRenderer xyerrorrenderer = new XYErrorRenderer();
//        xyerrorrenderer.setSeriesLinesVisible(0, true); // display the heading as a line
//        xyerrorrenderer.setSeriesShapesVisible(0, false);
//        xyerrorrenderer.setDrawXError(false); // draw only Y error bars
//        xyerrorrenderer.setDrawYError(true);
//        
        final XYPlot numNeighborsPlot = numNeighborsChart.getXYPlot();
		
        numNeighborsPlot.setRenderer(0, renderer1);
        
//        XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer();
//        renderer2.setSeriesLinesVisible(0, true); // display the speed as a line
//        renderer2.setSeriesShapesVisible(0, false);
//        renderer2.setSeriesPaint(0, Color.BLACK);
//        renderer2.setSeriesLinesVisible(1, true); // display the speedCmd as a line
//        renderer2.setSeriesShapesVisible(1, false);
//        renderer2.setSeriesPaint(1, Color.RED);
//        renderer2.setSeriesLinesVisible(2, false); // display the waypoints as points
//        renderer2.setSeriesShapesVisible(2, true);
//        renderer2.setSeriesPaint(2, Color.BLUE);
//        renderer2.setSeriesShape(2, new java.awt.geom.Ellipse2D.Double(-5,-5,10,10));
//        renderer2.setSeriesLinesVisible(3, false); // display the begin edge traversal points as green dots
//        renderer2.setSeriesShapesVisible(3, true);
//        renderer2.setSeriesPaint(3, Color.GREEN);
//        renderer2.setSeriesShape(3, new java.awt.geom.Ellipse2D.Double(-5,-5,10,10));
//        
//        final XYPlot speedPlot = speedChart.getXYPlot();
//        speedPlot.setRenderer(0, renderer2);
        
        // Place the charts in their own panels.
        ChartPanel numNeighborsChartPanel = new ChartPanel(numNeighborsChart);
        
        numNeighborsChartPanel.setPreferredSize(new java.awt.Dimension(1200, 200));
        
        // Place both chart panels within a single panel with two rows.
        javax.swing.JPanel chartsPanel = new javax.swing.JPanel(new java.awt.GridLayout(1,1));
        chartsPanel.add(numNeighborsChartPanel);
       
        // Create a frame for the chart, then display it.
        ApplicationFrame appFrame = new ApplicationFrame("Per Node Statistics for " + expData.getMissionName() + " " + expData.getExpName() + " " + robotData.getRobotName());
        appFrame.setContentPane(chartsPanel);
        appFrame.pack();
		RefineryUtilities.centerFrameOnScreen(appFrame);
		appFrame.setVisible(true);
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
	
	private class NeighborListState {
		long timestamp;
		int size;
		
		public NeighborListState(long timestamp, int size) {
			this.timestamp = timestamp;
			this.size = size;
		}
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
