package pharoslabut.logger.analyzer.pingTest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import pharoslabut.logger.Logger;
import pharoslabut.navigate.Location;

/**
 * Computes the CDF of ping receptions versus distance.
 * 
 * @author Chien-Liang Fok
 */
public class PingVsDist {
	private Vector<PingState> pingStates = new Vector<PingState>();
	private RobotExpData robotData;
	
	public PingVsDist(long pingOffset, Location staticNodeLoc, String robotLog, String pingLog) {
		//robotData = new RobotExpData("MM25-Exp1-ZIEGEN-SimpleMule_20120530183808.log");
		//robotData = new RobotExpData("MM25-Exp2-ZIEGEN-SimpleMule_20120530184802.log");
		//robotData = new RobotExpData("MM25-Exp3-ZIEGEN-SimpleMule_20120530185623.log");
		robotData = new RobotExpData(robotLog);
		readPingFile(pingLog, pingOffset);
		computePingDistCDF(staticNodeLoc);
	}
	
	/**
	 * Generates CDF of ping distance.  It generates a table:
	 * 
	 *   | Distance  | Percentage of received pings when separated less than the distance |
	 */
	public void computePingDistCDF(Location staticNodeLoc) {
		Vector<Double> distances = new Vector<Double>();
		
		// compute the distance between the robots for each received ping
		Enumeration<PingState> e = pingStates.elements();
		while (e.hasMoreElements()) {
			PingState currPing = e.nextElement();
			if (currPing.timestamp >= robotData.getExpStartTime() 
					&& currPing.timestamp <= robotData.getExpStopTime())
			{
				Location loc = robotData.getLocation(currPing.timestamp);
				double dist = loc.distanceTo(staticNodeLoc);
				distances.add(dist);
			} else {
				Logger.logWarn("Rejecting ping b/c timestamp (" + currPing.timestamp + ") falls out of bounds [" 
						+ robotData.getExpStartTime() 
						+ ", " + robotData.getExpStopTime() + "]");
			}
		}
		
		if (distances.size() == 0) {
			System.err.println("ERROR: No distance measurements!");
			System.exit(1);
		}
		
		Collections.sort(distances);
		
//		int count = 0;
//		Enumeration<Double> e2 = distances.elements();
//		while (e2.hasMoreElements()) {
//			System.out.println(count + ": " + e2.nextElement());
//			count++;
//		}
		
		double maxDist = distances.get(distances.size()-1);
		for (int currDist = 0; currDist < maxDist+1; currDist++) {
			// compute the percentage of pings whose distances are less than currDist
			int pingCount = 0;
			Enumeration<Double> e2 = distances.elements();
			while (e2.hasMoreElements()) {
				double dist = e2.nextElement();
				if (dist < currDist)
					pingCount++;
			}
			double pctLess = pingCount / ((double)distances.size());
			System.out.println(currDist + ", " + pctLess);
		}
	}
	
	private void readPingFile(String pingLog, long pingOffset) {		
		String fileName = pingLog;
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
				if (!line.equals("") && !line.startsWith("//")) {
					if (line.contains("64 bytes")) {
						String[] elem = line.split("[\\s,=]+");
						try {
							double latency = Double.valueOf(elem[9]);
							int seqno = Integer.valueOf(elem[5]);
							line = input.readLine();
							lineno++;
							elem = line.split("[\\s]");
							long timestamp = Long.valueOf(elem[2]) + pingOffset;
//							Logger.log("Experiment name: " + expName);
							pingStates.add(new PingState(timestamp, seqno, latency));
						} catch(Exception e) {
							e.printStackTrace();
							System.err.println("Syntax error on line " + lineno + " of file " + fileName + ":\n" + line);
							System.exit(1);
						}
					} 
				}
				lineno++;
			}
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
//		Enumeration<PingState> e = pingStates.elements();
//		while (e.hasMoreElements()) {
//			System.out.println(e.nextElement().toString());
//		}
	}
	
	private class PingState {
		long timestamp;
		int seqno;
		double latency; // in milliseconds
		
		public PingState(long timestamp, int seqno, double latency) {
			this.timestamp = timestamp;
			this.seqno = seqno;
			this.latency = latency;
		}
		
		public String toString() {
			return timestamp + "\t" + seqno + "\t" + latency;
		}
	}
	
	private static void print(String msg) {
		System.out.println(msg);
	}
	
	private static void usage() {
		print("Usage: " + PingVsDist.class.getName() + " <options>\n");
		print("Where <options> include:");
		print("\t-pingOffset <ping offset>: The offset between the ping timestamp and the system timestamp (required)");
		print("\t\tSee: pharoslabut.logger.analyzer.pingTest.ComputePingTimestampOffset");
		print("\t-staticLoc <latitude>  <longitude>: The location of the static node (required)");
		print("\t-robotLog <log file>: The log file generated by the robot as it moved (required)");
		print("\t-pingLog <log file>: The log file generated by the ping program (required)");
		print("\t-debug: enable debug mode");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long pingOffset = -1;
		Location staticNodeLoc = null;
		String robotLog = null;
		String pingLog = null;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-pingOffset")) {
					pingOffset = Long.valueOf(args[++i]);
				} 
				else if (args[i].equals("-staticLoc")) {
					double lat = Double.valueOf(args[++i]);
					double lon = Double.valueOf(args[++i]);
					staticNodeLoc = new Location(lat, lon);
				}
				else if (args[i].equals("-robotLog")) {
					robotLog = args[++i];
				}
				else if (args[i].equals("-pingLog")) {
					pingLog = args[++i];
				}
				else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				}
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
		
		if (pingOffset == -1) {
			System.err.println("ERROR: ping offset not set.");
			System.exit(1);
		}
		
		if (staticNodeLoc == null) {
			System.err.println("ERROR: static node location not set.");
			System.exit(1);
		}
		
		if (robotLog == null) {
			System.err.println("ERROR: robot log file not set.");
			System.exit(1);
		}
		
		if (pingLog == null) {
			System.err.println("ERROR: ping log file not set.");
			System.exit(1);
		}
		
		new PingVsDist(pingOffset, staticNodeLoc, robotLog, pingLog);
	}

}
