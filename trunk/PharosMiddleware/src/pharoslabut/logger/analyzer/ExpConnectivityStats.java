package pharoslabut.logger.analyzer;

//import java.awt.Color;
//import java.io.File;
//import java.io.FilenameFilter;
//import java.net.InetAddress;
//import java.util.Enumeration;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Vector;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RefineryUtilities;

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
public class ExpConnectivityStats {

	private String expDirName;
	
	/**
	 * Contains the experiment data.
	 */
	private ExpData expData;
	
	/**
	 * Contains the experiment connection statistics.
	 */
	private Vector<ExpConnStat> expConnStats = new Vector<ExpConnStat>();
	
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
	 * @param expDirName The directory containing the experiment log files.
	 * @param samplingInterval  The interval at which to calculate the size of the neighbor list in milliseconds.
	 * @param disconnectionInterval The number of milliseconds that a node can remain within the neighbor list
	 * without receiving a beacon.
	 * @param saveToFile whether to save the error calculations to a file.
	 */
	public ExpConnectivityStats(String expDirName, long samplingInterval, long disconnectionInterval, boolean saveToFile) {
		this.expDirName = expDirName;
		this.samplingInterval = samplingInterval;
		this.disconnectionInterval = disconnectionInterval;
		
		// Read in the experiment log files
		expData = new ExpData(expDirName);
//		File expDir = new File(expDirName);
//		FilenameFilter filter = new FilenameFilter() {
//		    public boolean accept(File dir, String name) {
//		        return !name.startsWith(".") && name.endsWith(".log");
//		    }
//		};
//		
//		String[] logFileNames = expDir.list(filter);
//		
//		for (int i=0; i < logFileNames.length; i++) {
//			String logFileName = logFileNames[i];
//			Logger.logDbg("Reading node log file " + logFileName);
//			robotDatas.add(new RobotExpData(logFileName));
//		}
		
		calcStats();
		printResults(saveToFile);
		showChart();
	}
	
	/**
	 * Calculates the experiment statistics.
	 */
	private void calcStats() {
		
		// For each time interval.
		for (long currTime = expData.getExpStartTime(); currTime < expData.getExpStopTime(); 
			currTime += samplingInterval) 
		{
			Logger.logDbg("Computing stats for time " + currTime);
			
			// Get the node state for each node at currTime
			Vector<NodeState> nodeStates = new Vector<NodeState>();
			
			Enumeration<RobotExpData> e = expData.getRobotEnum();
			while (e.hasMoreElements()) {
				RobotExpData robotData = e.nextElement();
				NbrList nbrList = NbrList.getNbrList(robotData, currTime, disconnectionInterval);
				NodeState currNodeState = new NodeState(robotData.getRobotID(), nbrList, 
						robotData.getSpeed(currTime), robotData.getHeading(currTime));
				nodeStates.add(currNodeState);
			}
			
			// This holds the results for this timestamp.
			ExpConnStat expConnStat = new ExpConnStat(currTime); 
			
			// Compute the partitions by going through each node state and
			// grabbing the maximum closure of connected nodes (this requires a 
			// recursive function).  For each node, if it is already part of an 
			// existing partition, ignore it.  Otherwise, create a new partition
			// containing the node and its maximum closeure of connected nodes.
			for (int i=0; i < nodeStates.size(); i++) {
				NodeState currNode = nodeStates.get(i);
				if (!expConnStat.containsNode(currNode)) {
					
					// Get the transitive closure of connected nodes.
					// This forms a partition that will be added to the ExpConnStat.
					Partition p = getTransitiveConnectionClosure(currNode, nodeStates);
					expConnStat.addPartition(p);
				}
			}
			
			expConnStats.add(expConnStat);
			
//			Logger.log("Number of partitions: " + expConnStat.numPartitions());
//			Logger.log("Partition size: " + expConnStat.avgPartitionSize());
//			Logger.log("Number disconnected: " + expConnStat.numDisconnected());
		}
	}
	
	/**
	 * Searches for all nodes that are connected to the current node.
	 * 
	 * @param currNode The current node.
	 * @param nodeStates The nodes that are transitively connected to it.
	 * @return The collection of nodes that are connected to the currNode.
	 */
	private Partition getTransitiveConnectionClosure(NodeState currNode, Vector<NodeState> nodeStates) {
		Partition p = new Partition();
		p.addNode(currNode);
		getTransitiveConnectionClosureHelper(currNode, nodeStates, p);
		return p;
	}
	
	/**
	 * Recursively searches for all nodes that are connected to the current node.
	 * The termination condition is when all connected neighbors are already in the partition.
	 * 
	 * @param baseNode The base node that serves as a reference point.
	 * @param nodeStates The node states.
	 * @param partition The partition for holding the nodes connected to the curr node.
	 */
	private void getTransitiveConnectionClosureHelper(NodeState baseNode, Vector<NodeState> nodeStates,
			Partition partition)
	{
		// For each node in the experiment
		for (int i=0; i < nodeStates.size(); i++) {
			NodeState potentialNbr = nodeStates.get(i);
			
			// If a bi-directional link exists between the base node and the other node
			if (baseNode.getID() != potentialNbr.getID() && 
					baseNode.isConnectedTo(potentialNbr.getID()) && potentialNbr.isConnectedTo(baseNode.getID())) 
			{
				// If the other node is not in the partition, add it, then recurse.
				if (!partition.containsNode(potentialNbr)) {
					partition.addNode(potentialNbr);
					getTransitiveConnectionClosureHelper(potentialNbr, nodeStates, partition);  // Here is the recursive call
				}
			}
		}
	}
	
	private void printResults(boolean saveToFile) {
		FileLogger flogger = null;
		if (saveToFile) {
			String fileName = expData.getMissionName() + "-" + expData.getExpName() + "-ConnStats.txt";
//			if (expData.getNumRobots() > 0) {
//				RobotExpData robotData = expData.getRobot(0);
//				fileName = robotData.getMissionName() + "-" + robotData.getExpName() + "-ConnStats.txt";
//			} else {
//				Logger.logErr("Could not determine mission name or experiment number because no robots present.");
//				System.exit(1);// should never get there
//				fileName = "ConnStats.txt"; 
//			}
			Logger.log("Saving data to " + fileName);
			flogger = new FileLogger(fileName, false);
		}
		
		long expStartTime = expData.getExpStartTime();
		
		// Print a table that includes the statistics for each data sample
		log("Time (ms)\tExperiment Time (ms)\tNumber of Partitions\tAvg. Partition Size\tNumber Disconnected Nodes\tRelative Mobility (m/s)", flogger);
		for (int i=0; i < expConnStats.size(); i++) {
			ExpConnStat currStat = expConnStats.get(i);
			log(currStat.getTimestamp() + "\t" + (currStat.getTimestamp() - expStartTime) + "\t" + currStat.getNumPartitions() 
					+ "\t" + currStat.getAvgPartitionSize() + "\t" + currStat.getNumDisconnected() + "\t" + currStat.getRelativeMobility(), flogger);
		}
		
		// Print the overall experiment statistics
		log("Overall global statistics:", flogger);
		log("Average Number of Partitions: " + getAverageNumberOfParitions(), flogger);
		log("Average Parition Size: " + getAverageParitionSize(), flogger);
		log("Average Number of Disconnected Nodes: " + getAverageDisconnectedNodes(), flogger);
		log("Average Relative Mobility: " + getAverageRelativeMobility(), flogger);
	}
	
	/**
	 * Plots the experiment connectivity statistics on charts for easy visualization.
	 */
	private void showChart() {
		// Create the various data series...
		XYSeries numPartitionSeries = new XYSeries("Number of partitions");
		XYIntervalSeries avgPartitionSizeSeries = new XYIntervalSeries("Average partition size");
		XYSeries numDisconnectedSeries = new XYSeries("Number of disconnected nodes");
		XYIntervalSeries relativeMobilitySeries = new XYIntervalSeries("Relative mobility");

		long expStartTime = expData.getExpStartTime();
		
		// Fill in the data series
		for (int i=0; i < expConnStats.size(); i++) {
			ExpConnStat currStat = expConnStats.get(i);
			double currTime = (currStat.getTimestamp() - expStartTime) / 1000.0; // in seconds
			
			numPartitionSeries.add(currTime, currStat.getNumPartitions());
			
			double avgPartitionSize = currStat.getAvgPartitionSize().getAverage();
			double avgPartitionSizeConf95 = currStat.getAvgPartitionSize().getConf95();
			avgPartitionSizeSeries.add(currTime, currTime, currTime, avgPartitionSize, avgPartitionSize-avgPartitionSizeConf95, avgPartitionSize+avgPartitionSizeConf95);
			
			numDisconnectedSeries.add(currTime, currStat.getNumDisconnected());
			
			double avgRelMobility = currStat.getRelativeMobility().getAverage();
			double avgRelMobilityConf95 = currStat.getRelativeMobility().getConf95();
			relativeMobilitySeries.add(currTime, currTime, currTime, avgRelMobility, avgRelMobility - avgRelMobilityConf95, avgRelMobility + avgRelMobilityConf95);
		}

		// Create data sets for each series
		XYSeriesCollection numPartitionDataSet = new XYSeriesCollection();
		numPartitionDataSet.addSeries(numPartitionSeries);
		
		XYIntervalSeriesCollection avgPartitionSizeDataSet = new XYIntervalSeriesCollection();
		avgPartitionSizeDataSet.addSeries(avgPartitionSizeSeries);
		
		XYSeriesCollection numDisconnectedDataSet = new XYSeriesCollection();
		numDisconnectedDataSet.addSeries(numDisconnectedSeries);
		
		XYIntervalSeriesCollection relativeMobilityDataSet = new XYIntervalSeriesCollection();
		relativeMobilityDataSet.addSeries(relativeMobilitySeries);
		
		
		// Create the charts
		JFreeChart numPartitionChart = ChartFactory.createXYLineChart(
				"Number of Partitions vs. Time",                       // chart title
				"Time (s)",                                            // x axis label
				"Num. Partitions",                                     // y axis label
				numPartitionDataSet,                                   // the heading data
				PlotOrientation.VERTICAL,                              // plot orientation (y axis is vertical)
				false,                                                 // include legend
				true,                                                  // tooltips
				false                                                  // urls
		);
		
		JFreeChart avgPartitionSizeChart = ChartFactory.createXYLineChart(
				"Avg. Partition Size vs. Time",                        // chart title
				"Time (s)",                                            // x axis label
				"Avg. Parition Size",                                  // y axis label
				avgPartitionSizeDataSet,                               // the heading data
				PlotOrientation.VERTICAL,                              // plot orientation (y axis is vertical)
				false,                                                 // include legend
				true,                                                  // tooltips
				false                                                  // urls
		);
		
		JFreeChart numDisconnectedChart = ChartFactory.createXYLineChart(
				"Number of Disconnected Nodes vs. Time",               // chart title
				"Time (s)",                                            // x axis label
				"Num. Disconnected Nodes",                             // y axis label
				numDisconnectedDataSet,                                // the heading data
				PlotOrientation.VERTICAL,                              // plot orientation (y axis is vertical)
				false,                                                 // include legend
				true,                                                  // tooltips
				false                                                  // urls
		);
		
		JFreeChart relativeMobilityChart = ChartFactory.createXYLineChart(
				"Relative Mobility vs. Time",                          // chart title
				"Time (s)",                                            // x axis label
				"Rel. Mobility (m/s)",                             // y axis label
				relativeMobilityDataSet,                               // the heading data
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
        
		numPartitionChart.setBackgroundPaint(Color.white);
		avgPartitionSizeChart.setBackgroundPaint(Color.white);
		numDisconnectedChart.setBackgroundPaint(Color.white);
		relativeMobilityChart.setBackgroundPaint(Color.white);
        
        // Configure when to display lines an when to display the shapes that indicate data points
        XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer();
        renderer1.setSeriesLinesVisible(0, true); // display the heading as a line
        renderer1.setSeriesShapesVisible(0, false);
//        renderer1.setSeriesPaint(0, Color.BLACK);
//        renderer1.setSeriesLinesVisible(1, true); // display the headingCmd as a line
//        renderer1.setSeriesShapesVisible(1, false);
//        renderer1.setSeriesPaint(1, Color.RED);
//        renderer1.setSeriesLinesVisible(2, false); // display the begin edge traversal points as blue dots
//        renderer1.setSeriesShapesVisible(2, true);
//        renderer1.setSeriesPaint(2, Color.BLUE);
//        renderer1.setSeriesShape(2, new java.awt.geom.Ellipse2D.Double(-3,-3,6,6));
//        renderer1.setSeriesLinesVisible(3, false); // display the begin edge traversal points as green dots
//        renderer1.setSeriesShapesVisible(3, true);
//        renderer1.setSeriesPaint(3, Color.GREEN.darker());
//        renderer1.setSeriesShape(3, new java.awt.geom.Ellipse2D.Double(-5,-5,10,10));
        
        XYErrorRenderer xyerrorrenderer = new XYErrorRenderer();
        xyerrorrenderer.setSeriesLinesVisible(0, true); // display the heading as a line
        xyerrorrenderer.setSeriesShapesVisible(0, false);
        xyerrorrenderer.setDrawXError(false);
        xyerrorrenderer.setDrawYError(true);
        
        final XYPlot numPartitionPlot = numPartitionChart.getXYPlot();
        final XYPlot avgPartitionSizePlot = avgPartitionSizeChart.getXYPlot();
        final XYPlot numDisconnectedPlot = numDisconnectedChart.getXYPlot();
		final XYPlot relativeMobilityPlot = relativeMobilityChart.getXYPlot();
        numPartitionPlot.setRenderer(0, renderer1);
        avgPartitionSizePlot.setRenderer(0, xyerrorrenderer);
        numDisconnectedPlot.setRenderer(0, renderer1);
        relativeMobilityPlot.setRenderer(0, xyerrorrenderer);
        
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
        ChartPanel numPartitionChartPanel = new ChartPanel(numPartitionChart);
        ChartPanel avgPartitionSizeChartPanel = new ChartPanel(avgPartitionSizeChart);
        ChartPanel numDisconnectedChartPanel = new ChartPanel(numDisconnectedChart);
        ChartPanel relativeMobilityChartPanel = new ChartPanel(relativeMobilityChart);
        
        numPartitionChartPanel.setPreferredSize(new java.awt.Dimension(1200, 200));
        avgPartitionSizeChartPanel.setPreferredSize(new java.awt.Dimension(1200, 200));
        numDisconnectedChartPanel.setPreferredSize(new java.awt.Dimension(1200, 200));
        relativeMobilityChartPanel.setPreferredSize(new java.awt.Dimension(1200, 200));
       
        // Place both chart panels within a single panel with two rows.
        javax.swing.JPanel chartsPanel = new javax.swing.JPanel(new java.awt.GridLayout(4,1));
        chartsPanel.add(numPartitionChartPanel);
        chartsPanel.add(avgPartitionSizeChartPanel);
        chartsPanel.add(numDisconnectedChartPanel);
        chartsPanel.add(relativeMobilityChartPanel);
       
        // Create a frame for the chart, then display it.
        ApplicationFrame appFrame = new ApplicationFrame("Network Partition and Node Mobility Statistics for " + expData.getMissionName() + " " + expData.getExpName());
        appFrame.setContentPane(chartsPanel);
        appFrame.pack();
		RefineryUtilities.centerFrameOnScreen(appFrame);
		appFrame.setVisible(true);
	}
	
	/**
	 * @return the average number of partitions and its 95% confidence interval.
	 */
	public AverageStatistic getAverageNumberOfParitions() {
		Vector<Double> partitionCount = new Vector<Double>();
		for (int i=0; i < expConnStats.size(); i++) {
			ExpConnStat currStat = expConnStats.get(i);
			double numPartitions = currStat.getNumPartitions();
			partitionCount.add(numPartitions);
		}
		return new AverageStatistic(partitionCount);
	}
	
	/**
	 * @return the average partition size and its 95% confidence interval.
	 */
	public AverageStatistic getAverageParitionSize() {
		Vector<Double> partitionSize = new Vector<Double>();
		for (int i=0; i < expConnStats.size(); i++) {
			ExpConnStat currStat = expConnStats.get(i);
			AverageStatistic numPartitions = currStat.getAvgPartitionSize();
			partitionSize.add(numPartitions.getAverage());
		}
		return new AverageStatistic(partitionSize);
	}
	
	/**
	 * @return the average number of disconnected nodes and its 95% confidence interval.
	 */
	public AverageStatistic getAverageDisconnectedNodes() {
		Vector<Double> disconnectedCount = new Vector<Double>();
		for (int i=0; i < expConnStats.size(); i++) {
			ExpConnStat currStat = expConnStats.get(i);
			double numPartitions = currStat.getNumDisconnected();
			disconnectedCount.add(numPartitions);
		}
		return new AverageStatistic(disconnectedCount);
	}
	
	/**
	 * @return the average relative mobility and its 95% confidence interval.
	 */
	public AverageStatistic getAverageRelativeMobility() {
		Vector<Double> relativeMobilities = new Vector<Double>();
		for (int i=0; i < expConnStats.size(); i++) {
			ExpConnStat currStat = expConnStats.get(i);
			AverageStatistic rm = currStat.getRelativeMobility();
			relativeMobilities.add(rm.getAverage());
		}
		return new AverageStatistic(relativeMobilities);
	}
	
	
	
	/**
	 * Contains the experiment connection statistics at a particular snapshot 
	 * in time.
	 */
	private class ExpConnStat {
		private long timestamp;
		private Vector<Partition> partitions = new Vector<Partition>();
		
		/**
		 * The constructor.
		 */
		public ExpConnStat(long timestamp) {
			this.timestamp = timestamp;
		}
		
		/**
		 * 
		 * @return The timestamp.
		 */
		public long getTimestamp() {
			return timestamp;
		}
		
		/**
		 * Adds a partition.
		 * 
		 * @param p The partition to add.
		 */
		public void addPartition(Partition p) {
			partitions.add(p);
		}
		
		/**
		 * Checks whether the node is already part of an existing partition.
		 * 
		 * @param node The node to check
		 * @return true if the node is part of an existing partition.
		 */
		public boolean containsNode(NodeState node) {
			for (int i=0; i < partitions.size(); i++) {
				Partition currPart = partitions.get(i);
				if (currPart.containsNode(node)) {
					return true;
				}
			}
			return false;
		}
		
		/**
		 * @return The number of partitions.
		 */
		public int getNumPartitions() {
			return partitions.size();
		}
		
		/**
		 * 
		 * @return The average partition size.
		 */
		public AverageStatistic getAvgPartitionSize() {
			Vector<Double> partitionSizes = new Vector<Double>();
			
			for (int i=0; i < partitions.size(); i++) {
				double partitionSize = partitions.get(i).size();
				partitionSizes.add(partitionSize);
			}
			
			return new AverageStatistic(partitionSizes);
		}
		
		/**
		 * 
		 * @return The number of disconnected nodes.  This is the number of partitions
		 * consisting of only on node.
		 */
		public int getNumDisconnected() {
			int result = 0;
			for (int i=0; i < partitions.size(); i++) {
				if (partitions.get(i).size() == 1)
					result++;
			}
			return result;
		}
		
		/**
		 * 
		 * @return The sum of all the relative velocities between every pair of nodes.
		 */
		public AverageStatistic getRelativeMobility() {
			Vector<Double> relativeMobilities = new Vector<Double>();
			
			Vector<NodeState> allNodes = getAllNodes();
			for (int i=0; i < allNodes.size() - 1; i++) {
				NodeState referenceNode = allNodes.get(i);
				
				for (int j=i+1; j < allNodes.size(); j++) {
					NodeState otherNode = allNodes.get(j);
					double relativeSpeed = referenceNode.getRelativeSpeed(otherNode);
					relativeMobilities.add(relativeSpeed);
				}
			}
			
			return new AverageStatistic(relativeMobilities);
		}
		
		/**
		 * @return  all nodes in the experiment
		 */
		private Vector<NodeState> getAllNodes() {
			Vector<NodeState> result = new Vector<NodeState>();
			for (int i=0; i < partitions.size(); i++) {
				result.addAll(partitions.get(i).getNodes());
			}
			return result;
		}
	}
	
	/**
	 * Represents a partition, which is a group of connected nodes.
	 */
	private class Partition {
		private Vector<NodeState> nodeState = new Vector<NodeState>();
		
		public Partition() {}
		
		/**
		 * Adds a node to this partition.  If the node is already part of
		 * this partition, ignore the request.
		 * 
		 * @param node The node to add.
		 */
		public void addNode(NodeState node) {
			if (containsNode(node)) {
				Logger.log("WARNING: Attempting to add a node to a partition twice!");
			} else {
				nodeState.add(node);
			}
		}
		
		/**
		 * Adds a collection of nodes to this partition.  If any of the nodes are already part of
		 * this partition, ignore the request.
		 * 
		 * @param nodes The nodes to add.
		 */
		public void addNodes(Vector<NodeState> nodes) {
			for (int i=0; i < nodes.size(); i++) {
				addNode(nodes.get(i));
			}
		}
		
		/**
		 * 
		 * @return The nodes in this partition.
		 */
		public Vector<NodeState> getNodes() {
			return nodeState;
		}
		
		/**
		 * Checks whether the node is already part of an existing partition.
		 * 
		 * @param node The node to check
		 * @return true if the node is part of an existing partition.
		 */
		public boolean containsNode(NodeState node) {
			for (int i=0; i < nodeState.size(); i++) {
				NodeState currNode = nodeState.get(i);
				if (currNode.getID() == node.getID())
					return true;
			}
			return false;
		}
		
		public int size() {
			return nodeState.size();
		}
	}
	
	/**
	 * Contains the state of a node, which includes the node's ID,
	 * Speed, and Heading.
	 */
	private class NodeState {
		private int nodeID;
		private NbrList nbrList;
		private double speed, heading;
		
		/**
		 * The constructor.
		 * 
		 * @param nodeID The ID of the node.
		 * @param speed The speed at which the node is moving in meters per second.
		 * @param heading The heading of the node in radians (-PI to PI with 0 degrees due north and -PI/2 due East)
		 */
		public NodeState(int nodeID, NbrList nbrList, double speed, double heading) {
			this.nodeID = nodeID;
			this.nbrList = nbrList;
			this.speed = speed;
			this.heading = heading;
		}
		
//		public NbrList getNbrList() {
//			return nbrList;
//		}
		
		/**
		 * Checks whether this node is connected to the specified node.
		 * 
		 * @param nodeID The ID of the node to check.
		 * @return true if a node with the specified ID is in this node's neighbor list.
		 */
		public boolean isConnectedTo(int nodeID) {
			return nbrList.contains(nodeID);
		}
		
		/**
		 * 
		 * @return The ID of the node.
		 */
		public int getID() {
			return nodeID;
		}
		
		/**
		 * Returns the change in latitude of the robot's motion.
		 * This is analogous to the "Y" component of a vector.
		 * 
		 * @return The change in latitude of the robot's velocity.
		 */
		public double getLatitudeComponent() {
			return speed * Math.cos(heading);
		}
		
		/**
		 * Returns the change in longitude of the robot's motion.
		 * This is analogous to the "X" component of a vector.
		 * 
		 * @return The change in longitude of the robot's velocity.
		 */
		public double getLongitudeComponent() {
			return speed * Math.sin(heading);
		}
		
		/**
		 * Returns the relative speed in m/s between this node and another node.
		 * 
		 * @param otherNode The other node with which to compare.
		 * @return The relative speed between this node and the other node in meters per second.
		 */
		public double getRelativeSpeed(NodeState otherNode) {
			double deltaLatitude = getLatitudeComponent() + otherNode.getLatitudeComponent();
			double deltaLongitude = getLongitudeComponent() + otherNode.getLongitudeComponent();
			double relativeSpeed = Math.sqrt(Math.pow(deltaLatitude, 2) + Math.pow(deltaLongitude, 2));
			return relativeSpeed;
		}
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
		print("Usage: " + ExpConnectivityStats.class.getName()  + " <options>\n");
		print("Where <options> include:");
		print("\t-expDir <log file name>: The log file generated during the experiment. (required)");
		print("\t-save: Save the experiment connectivity stats to a text file.  The name will be \"[exp name]-ConnStats.txt\" (optional)");
		print("\t-samplingInterval <sampling interval>: The interval in milliseconds at which to calculate the statistics. (optional, default 5000)");
		print("\t-disconnectionInterval <timeout interval>: The interval in milliseconds that a node may remain in a neighbor list without a beacon being received (optional, default 6000)");
		print("\t-debug or -d: Enable debug mode");
	}
	
	public static void main(String[] args) {
		String expDirName = null;
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
				else if (args[i].equals("-expDir")) {
					expDirName = args[++i];
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
		
		if (expDirName == null) {
			printErr("Must specify experiment directory.");
			usage();
			System.exit(1);
		}
		
		print("Experiment directory: " + expDirName);
		print("Debug: " + (System.getProperty ("PharosMiddleware.debug") != null));
		
		try {
			new ExpConnectivityStats(expDirName, samplingInterval, disconnectionInterval, saveToFile);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
