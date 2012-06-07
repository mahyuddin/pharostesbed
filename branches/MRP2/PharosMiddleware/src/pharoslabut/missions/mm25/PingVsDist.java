package pharoslabut.missions.mm25;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import pharoslabut.logger.Logger;
import pharoslabut.navigate.Location;
import pharoslabut.missions.m45.exp03.RobotExpData;

public class PingVsDist {
	public static final long PING_TIMESTAMP_OFFSET = 1338422400000L; // May 31, 2012 GMT
	//public static final long PING_TIMESTAMP_OFFSET = 1338336000000L; // May 30, 2012 GMT
	private static final Location STATIC_NODE_LOCATION = new Location(30.38569, -97.72521);

	private Vector<PingState> pingStates = new Vector<PingState>();
	private RobotExpData robotData;
	
	public PingVsDist() {
		//robotData = new RobotExpData("MM25-Exp1-ZIEGEN-SimpleMule_20120530183808.log");
		//robotData = new RobotExpData("MM25-Exp2-ZIEGEN-SimpleMule_20120530184802.log");
		//robotData = new RobotExpData("MM25-Exp3-ZIEGEN-SimpleMule_20120530185623.log");
		robotData = new RobotExpData("MM25-Exp4-ZIEGEN-SimpleMule_20120530190355.log");
		readPingFile();
		computePingDistCDF();
	}
	
	/**
	 * Generates CDF of ping distance.  It generates a table:
	 * 
	 *   | Distance  | Percentage of received pings when separated less than the distance |
	 */
	public void computePingDistCDF() {
		Vector<Double> distances = new Vector<Double>();
		
		// compute the distance between the robots for each received ping
		Enumeration<PingState> e = pingStates.elements();
		while (e.hasMoreElements()) {
			PingState currPing = e.nextElement();
			if (currPing.timestamp >= robotData.getExpStartTime() 
					&& currPing.timestamp <= robotData.getExpStopTime())
			{
				Location loc = robotData.getLocation(currPing.timestamp);
				double dist = loc.distanceTo(STATIC_NODE_LOCATION);
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
	
	private void readPingFile() {		
		String fileName = "MM25-Exp4-ping.txt";
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
							long timestamp = Long.valueOf(elem[2]) + PING_TIMESTAMP_OFFSET;
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
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.setProperty ("PharosMiddleware.debug", "true");
		new PingVsDist();
	}

}
