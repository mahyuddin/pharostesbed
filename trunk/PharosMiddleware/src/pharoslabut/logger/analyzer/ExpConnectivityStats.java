package pharoslabut.logger.analyzer;

//import java.awt.Color;
//import java.io.File;
//import java.io.FilenameFilter;
//import java.net.InetAddress;
//import java.util.Enumeration;
import java.util.Enumeration;
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
			
			Logger.log("Number of partitions: " + expConnStat.numPartitions());
			Logger.log("Partition size: " + expConnStat.avgPartitionSize());
			Logger.log("Number disconnected: " + expConnStat.numDisconnected());
			
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
			String fileName;
			if (expData.getNumRobots() > 0) {
				RobotExpData robotData = expData.getRobot(0);
				fileName = robotData.getMissionName() + "-" + robotData.getExpName() + "-ConnStats.txt";
			} else {
				Logger.logErr("Could not determine mission name or experiment number because no robots present.");
				System.exit(1);// should never get there
				fileName = "ConnStats.txt"; 
			}
			Logger.log("Saving data to " + fileName);
			flogger = new FileLogger(fileName, false);
		}
		
//		log("Total Connections: " + totalConnections, flogger);
//		log("Total Disconnections: " + totalDisconnections, flogger);
//		log("Average Connection Duration: " + averageConnectionDuration, flogger);
//		log("Average Neighbor List Size: " + averageNeighbors, flogger);
//		log("Total Unique Neighbors: " + numUniqueNeighbors, flogger);
	}
	
	/**
	 * Contains the experiment connection statistics at a particular snapshot 
	 * in time.
	 */
	private class ExpConnStat {
		long timestamp;
		Vector<Partition> partitions = new Vector<Partition>();
		
		/**
		 * The constructor.
		 */
		public ExpConnStat(long timestamp) {
			this.timestamp = timestamp;
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
		public int numPartitions() {
			return partitions.size();
		}
		
		/**
		 * 
		 * @return The average partition size.
		 */
		public double avgPartitionSize() {
			double totalSize = 0;
			for (int i=0; i < partitions.size(); i++) {
				totalSize += partitions.get(i).size();
			}
			return totalSize / partitions.size();
		}
		
		/**
		 * 
		 * @return The number of disconnected nodes.  This is the number of partitions
		 * consisting of only on node.
		 */
		public int numDisconnected() {
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
		public double getRelativeMobility() {
			double result = 0;
			//TODO
			
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
		
		public int getID() {
			return nodeID;
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
