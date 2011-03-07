package pharoslabut.logger.analyzer;

import java.io.*;
import java.util.*;

import pharoslabut.logger.*;
import pharoslabut.RobotIPAssignments;
import pharoslabut.navigate.*;
import playerclient.structures.gps.PlayerGpsData;

/**
 * Encapsulates the data recorded by a robot during an experiment.
 * 
 * @author Chien-Liang Fok
 */
public class RobotExpData {
	/**
	 * The name of the experiment log file.
	 */
	private String fileName;
	
	/**
	 *  The start time of the experiment. It is when the PharosServer
	 *  receives the start experiment message from the PharosClient.
	 */
	private long expStartTime;
	
	/**
	 * Details of a single edge in the path the robot traveled.
	 */
	private Vector<PathEdge> pathEdges = new Vector<PathEdge>();
	
	/**
	 * The offset in ms between the local timestamps and the GPS timestamps.
	 * This is used to calibrate the timestamps recorded within the experiment
	 * data.  The equation for calibrating the time is:
	 * 
	 * True time = Local timestamp - timeOffset.
	 */
	//private double timeOffset;
	
	/**
	 * The locations of the robot as it traversed this edge.
	 */
	private Vector <GPSLocationState> locations = new Vector<GPSLocationState>();
	
	/**
	 * Records when the TelosB receives a broadcasts.
	 */
	private Vector<TelosBRxRecord> telosBRxHist = new Vector<TelosBRxRecord>();
	
	/**
	 * Records when the TelosB sends a broadcast.
	 */
	private Vector<TelosBTxRecord> telosBTxHist = new Vector<TelosBTxRecord>();
	
	/**
	 * The constructor.
	 * 
	 * @param fileName The name of the experiment log file.
	 */
	public RobotExpData(String fileName) {
		this.fileName = fileName;
		try {
			readFile();
			calibrateTime();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Determines whether the robot actually ran in the experiment.
	 * It ran in the experiment if it traversed at least one edge.
	 * 
	 * @return true if the robot ran in the experiment.
	 */
	public boolean ranExperiment() {
		return numEdges() > 0;
	}
	
	/**
	 * Determines whether this robot received the specified beacon.
	 * 
	 * @param robotID The transmitter's ID.
	 * @param seqno The sequence number.
	 * @return true if the transmission was received.
	 */
	public boolean rcvdTelosBTx(int robotID, int seqno) {
		return getTelosBRx(robotID, seqno) != null;
	}

	/**
	 * Finds the record of the robot receiving the specified TelosB transmission.
	 * 
	 * @param robotID The transmitter's ID.
	 * @param seqno The sequence number.
	 * @return The reception record, or null if the robot did not receive the transmission.
	 */
	public TelosBRxRecord getTelosBRx(int robotID, int seqno) {
		for (int i=0; i < telosBRxHist.size(); i++) {
			TelosBRxRecord currRxRec = telosBRxHist.get(i);
			if (currRxRec.getSenderID() == robotID && currRxRec.getSeqNo() == seqno)
				return currRxRec;
		}
		return null;
	}
	
	/**
	 * An accessor for the TelosB transmission record with the specified
	 * sequence number.
	 * 
	 * @param seqno The sequence number.
	 * @return The TelosB transmission record, or null if none found.
	 */
	public TelosBTxRecord getTelosBTx(int seqno) {
		for (int i=0; i < telosBTxHist.size(); i++) {
			TelosBTxRecord txRec = telosBTxHist.get(i);
			if (txRec.getSeqNo() == seqno)
				return txRec;
		}
		return null;
	}
	
	/**
	 * Returns the robot's name.
	 * 
	 * @return the robot's name, or null if unknown.
	 */
	public String getRobotName() {
		String[] tokens = fileName.split("-");
		return tokens[3];
	}
	
	/**
	 * Returns the robot's ID, which is the last octal of the robot's IP address.
	 * 
	 * @return the last octal of the robot's IP address, or -1 if IP address is unknown.
	 */
	public int getRobotID() {
		int id = RobotIPAssignments.getRobotID(getRobotName());
		if (id == -1) System.exit(1); 
		return id;
	}
	
	/**
	 * Reads in and organizes the data in the robot's log file.
	 * 
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	private void readFile() throws NumberFormatException, IOException {
		File file = new File(fileName);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
		PathEdge currEdge = null;
		PlayerGpsData currLoc = null;
		
		while (( line = br.readLine()) != null){
			if (line.contains("Starting experiment at time:")) {
				String[] tokens = line.split("[: ]");
				this.expStartTime = Long.valueOf(tokens[8]);
			}
			else if (line.contains("WayPointFollower: Going to")) {
				StringTokenizer tokens = new StringTokenizer(line, ":()[], m/s");
				long timeStamp = Long.valueOf(tokens.nextToken());
				tokens.nextToken(); // WayPointFollower
				tokens.nextToken(); // going
				tokens.nextToken(); // to
				double lat = Double.valueOf(tokens.nextToken());
				double lon = Double.valueOf(tokens.nextToken());
				tokens.nextToken(); // 0.0
				tokens.nextToken(); // at
				double speed = Double.valueOf(tokens.nextToken()); 
				
				currEdge = new PathEdge(new Location(lat, lon), timeStamp, speed);
				
				// Set the start location of the path edge if we know where we are.
				if (currLoc != null) {
					currEdge.setStartLoc(new Location(currLoc));
				}
			}
			else if (line.contains("GPSDataBuffer: New GPS Data:")) {
				
				String[] tokens = line.split("[:=(), ]");
				long timeStamp = Long.valueOf(tokens[0].substring(1, tokens[0].length()-1));
				currLoc = new PlayerGpsData();
				currLoc.setAltitude(Integer.valueOf(tokens[18]));
				currLoc.setErr_horz(Double.valueOf(tokens[32]));
				currLoc.setErr_vert(Double.valueOf(tokens[34]));
				currLoc.setHdop(Integer.valueOf(tokens[28]));
				currLoc.setLatitude(Integer.valueOf(tokens[14]));
				currLoc.setLongitude(Integer.valueOf(tokens[16]));
				currLoc.setNum_sats(Integer.valueOf(tokens[26]));
				currLoc.setQuality(Integer.valueOf(tokens[24]));
				currLoc.setTime_sec(Integer.valueOf(tokens[10]));
				currLoc.setTime_usec(Integer.valueOf(tokens[12]));
				currLoc.setUtm_e(Double.valueOf(tokens[20]));
				currLoc.setUtm_n(Double.valueOf(tokens[22]));
				currLoc.setVdop(Integer.valueOf(tokens[30]));
				
				// Only add the GPSLocation if it is valid.  It is valid if the 
				// latitude is 
				Location l = new Location(currLoc);
				if (pharoslabut.navigate.GPSDataBuffer.isValid(l)) {
					locations.add(new GPSLocationState(timeStamp, currLoc));
					if (currEdge != null) {
						if (!currEdge.hasStartLoc())
							currEdge.setStartLoc(l);
					}
				} else {
					log("Rejecting invalid location: " + currLoc);
				}
			}
			else if (line.contains("RadioSignalMeter: SEND_BCAST")) {
				// The format of this line is:
				// [local time stamp] RadioSignalMeter: SEND_BCAST [node id] [seqno]
				String[] tokens = line.split("(\\s|\\[|\\])");
				TelosBTxRecord txRec = new TelosBTxRecord(
						Long.valueOf(tokens[1]), // timestamp
						Integer.valueOf(tokens[5]), // sender ID
						Integer.valueOf(tokens[6])); // seqno
				telosBTxHist.add(txRec);
				
			}
			else if (line.contains("RadioSignalMeter: RADIO_CC2420_RECEIVE")) {
				// The format of this line is:
				// [local time stamp] RadioSignalMeter: RADIO_CC2420_RECEIVE [receiver id] [sender id] [seqno] [RSSI] [LQI] [mote timestamp]
				String[] tokens = line.split("(\\s|\\[|\\])");
//				for (int i=0; i < tokens.length; i++) {
//					System.out.println(i + ": " + tokens[i]);
//				}
				TelosBRxRecord rxRec = new TelosBRxRecord(
						Long.valueOf(tokens[1]), // timestamp
						Integer.valueOf(tokens[5]), // rcvrID
						Integer.valueOf(tokens[6]), // sndrID
						Integer.valueOf(tokens[7]), // seqno
						Integer.valueOf(tokens[8]), // rssi
						Integer.valueOf(tokens[9]), // lqi
						Integer.valueOf(tokens[10])); // moteTimestamp
				telosBRxHist.add(rxRec);
			}

			else if (line.contains("Arrived at destination")) {
				// Save the end time of the experiment
				String[] tokens = line.split("[:= ]");
				long timeStamp = Long.valueOf(tokens[0].substring(1, tokens[0].length()-1));
				currEdge.setEndTime(timeStamp);
				pathEdges.add(currEdge);
				currEdge = null;
			}
		}
	}
	
	/**
	 * Compares the GPS timestamps with the log timestamps to determine the 
	 * offset needed to calibrate the log timestamps to match the GPS timestamps.
	 * The GPS timestamps are assumed to be accurate to within 1 second.
	 */
	private void calibrateTime() {
		// First gather all of the location data...
		Vector<GPSLocationState> locs = getGPSHistory();
		
//		pharoslabut.logger.FileLogger flogger = new pharoslabut.logger.FileLogger("CalibrateTime", false);
		
		double diffSum = 0; // the sum of all the diffs 
		
		// Calculate the difference between the log file and GPS timestamps
		for (int i=0; i < locs.size(); i++) {
			
			// The following is the difference, measured in milliseconds, 
			// between the current time and midnight, January 1, 1970 UTC.
			long localTimestamp = locs.get(i).getTimestamp();
			
			// This is the GPS timestamp.  The code that generates it is available here:
			// http://playerstage.svn.sourceforge.net/viewvc/playerstage/code/player/tags/release-2-1-3/server/drivers/gps/garminnmea.cc?revision=8139&view=markup
			// Note that the microsecond field is always zero, meaning it only provides
			// one second resolution.  The value is the number of seconds elapsed since 
			// 00:00 hours, Jan 1, 1970 UTC.
			int gpsSec = locs.get(i).getLoc().getTime_sec();
//			int gpsUSec = locs.get(i).getLoc().getTime_usec();
			
			// Calculate the difference between the two time stamps
			double diff = (localTimestamp / 1000.0) - gpsSec;
			
			diffSum += diff;
			
//			String str = localTimestamp + "\t" + gpsSec +"\t" + diff;
//			System.out.println(str);
//			flogger.log(str);
		}
		
		// Calculate the average offset and use it to calibrate all of the local timestamps.
		double timeOffset = diffSum / locs.size() * 1000;
		
//		String str = "CalibratedTimeOffset: " + calibratedTimeOffset;
//		System.out.println(str);
//		flogger.log(str);
		
		// Calibrate all of the timestamps...
		expStartTime = RobotExpData.getCalibratedTime(expStartTime, timeOffset);
		
		for (int i=0; i < locations.size(); i++) {
			GPSLocationState currLoc = locations.get(i);
			currLoc.calibrateTime(timeOffset);
		}
		
		for (int i=0; i < pathEdges.size(); i++) {
			PathEdge currEdge = pathEdges.get(i);
			currEdge.calibrateTime(timeOffset);
		}
		
		for (int i=0; i < telosBRxHist.size(); i++) {
			TelosBRxRecord rxRec = telosBRxHist.get(i);
			rxRec.calibrateTime(timeOffset);
		}
		
		for (int i=0; i < telosBTxHist.size(); i++) {
			TelosBTxRecord txRec = telosBTxHist.get(i);
			txRec.calibrateTime(timeOffset);
		}
	}
	
	/**
	 * Returns the calibrated timestamp to one second accuracy.
	 * 
	 * @param timestamp The recorded timestamp.  This may be inaccurate.
	 * @return The calibrated timestamp.  This is accurate to within one second.
	 */
	public static long getCalibratedTime(long timestamp, double offset) {
		return Math.round(timestamp - offset);
	}
	
	/**
	 * Returns the TelosB wireless packet reception history.
	 * 
	 * @return the TelosB wireless packet reception history.
	 */
	public Vector<TelosBRxRecord> getTelosBRxHist() {
		return telosBRxHist;
	}
	
	/**
	 * Returns the TelosB wireless packet transmission history.
	 * 
	 * @return the TelosB wireless packet transmission history.
	 */
	public Vector<TelosBTxRecord> getTelosBTxHist() {
		return telosBTxHist;
	}

	/**
	 * Gets the path edge history.
	 * 
	 * @return the path edge history.
	 */
	public Vector<PathEdge> getPathEdges() {
		return pathEdges;
	}
	
	public PathEdge getPathEdge(long timestamp) {
		
		// If the desired time is prior to starting the experiment, abort.
		if (timestamp < expStartTime) {
			logErr("ERROR: getPathEdge(timestamp): Specified timestamp (" + timestamp + ") prior to start of experiment (" + expStartTime + ")");
			System.exit(1);
		}
		
		// If the desired time is prior to starting the first edge traversal, return null
		if (timestamp < pathEdges.get(0).getStartTime()) {
			return null;
		}
		
		// If the desired time is after finishing the final edge, return null
		if (timestamp > pathEdges.get(pathEdges.size()-1).getEndTime()) {
			return null;
		}
		
		int edgeIndx = -1;
		for (int i=0; i < pathEdges.size(); i++) {
			if (timestamp >= pathEdges.get(i).getStartTime())
				edgeIndx = i;
		}
		
		// If no edge was found, abort.
		if (edgeIndx == -1) {
			logErr("ERROR: getPathEdge(timestamp): Unable to find edge containing timestamp " + timestamp);
			System.exit(1);
		}
		
		log("getPathEdge: indx = " + edgeIndx);
		
		return pathEdges.get(edgeIndx);
	}
	
	/**
	 * Returns a specific edge within this experiment.
	 * 
	 * @param indx The index of the path edge, must be between zero and numEdges().
	 * @return The edge within the experiment.
	 */
	public PathEdge getPathEdge(int indx) {
		return pathEdges.get(indx);
	}
	
	/**
	 * The number of edges in the experiment.  This is also the number of waypoints in 
	 * the motion script.
	 * 
	 * @return the number of edges in the experiment.
	 */
	public int numEdges() {
		return pathEdges.size();
	}
	
	/**
	 * Returns the GPS data collected by the robot as it performed this experiment.
	 * 
	 * @return The vector containing the entire GPS history of the robot.
	 */
	public Vector<GPSLocationState> getGPSHistory() {
//		Vector<GPSLocationState> result = new Vector<GPSLocationState>();
//		
//		Enumeration<PathEdge> e = pathEdges.elements();
//		
//		while (e.hasMoreElements()) {
//			PathEdge pe = e.nextElement();
//			
//			Enumeration<GPSLocationState> locEnum = pe.getLocationsEnum();
//			while (locEnum.hasMoreElements()) {
//				result.add(locEnum.nextElement());
//			}
//		}
//		
		return locations;
	}
	
	/**
	 * Gets the time at which the robot started.  This is the time at which the
	 * robot received the StartExpMsg.
	 * 
	 * @return The robot's start time.
	 */
	public long getRobotStartTime() {
		return expStartTime;
	}
	
	/**
	 * Gets the robot's end time.  This is the later of the end time of the last edge traversed,
	 * or the last GPS measurement recorded.
	 * 
	 * @return The experiment end time.
	 */
	public long getRobotStopTime() {
		long result = -1;
		
		// Find the time when the robot finished traversing the last edge.
		if (numEdges() > 0) {
			PathEdge lastEdge = getPathEdge(numEdges()-1);
			result = lastEdge.getEndTime();
		} 
		
		// If the last location is received *after* the end time of the last edge traversed,
		// consider it to be the end time of the robot.
		if (locations.size() > 0) {
			long locEndTime = locations.get(locations.size()-1).getTimestamp();
			if (locEndTime > result)
				result = locEndTime;
		} 
		
		if (result == -1) {
			logErr("getRobotEndTime: Cannot get experiment end time because numEdges and locations are both zero! Log File: " + fileName);
			new Exception().printStackTrace(); // get a stack trace for debugging...
			System.exit(1);
		}
		
		return result;
	}

	/**
	 * Gets the experiment log file name
	 * 
	 * @return the experiment log file name
	 */
	
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * Returns the initial location of the robot when the experiment began.
	 * 
	 * @return the initial location of the robot when the experiment began.
	 */
	public Location getBeginLocation() {
		return locations.get(0).getLocation();
	}
	
	/**
	 * Returns the final location of the robot after it finishes this experiment.
	 * 
	 * @return The final location of the robot.
	 */
	public Location getEndLocation() {
		return locations.get(locations.size()-1).getLocation();
	}
	
	public Enumeration<GPSLocationState> getLocationsEnum() {
		return locations.elements();
	}
	
	/**
	 * Returns the location of the robot at the specified time.
	 * Uses a linear interpolation of the robot's location when necessary.
	 * 
	 * @param timestamp The time of interest. 
	 * @return The location of the robot at the specified time.
	 */
	public Location getLocation(long timestamp) {
		if (timestamp < getRobotStartTime()) {
			log("WARNING: getLocation(timestamp): timestamp prior to beginning of experiment. (" + timestamp + " < " + getRobotStartTime() + ")");
			return getBeginLocation();
		}
		
		if (timestamp > getRobotStopTime()) {
			log("WARNING: getLocation(timestamp): timestamp after end of experiment. (" + getRobotStopTime() + " < " + timestamp + ")");
			return getEndLocation();
		}
		
		// calculate the percent edge traversal...
		//double pctTraversed = ((double)(timestamp - startTime)) / ((double)(endTime - startTime)) * 100.0;
		//log("Path Edge pct traveled: " + pctTraversed);
		
		// Find the index of the locations immediately before or after the
		// desired timestamp
		int beforeIndx = 0; 
		int afterIndx = 0;
		
		boolean afterIndxFound = false;
		
		for (int i=0; i < locations.size(); i++) {
			GPSLocationState currLocation = locations.get(i);
			if (currLocation.getTimestamp() <= timestamp)
				beforeIndx = i;
			if (!afterIndxFound && currLocation.getTimestamp() >= timestamp) {
				afterIndxFound = true;
				afterIndx = i;
			}
		}
		
		log("getLocation(timestamp): timestamp = " + timestamp + ", beforeIndx = " + beforeIndx + ", afterIndx = " + afterIndx);
		
		if (beforeIndx == afterIndx)
			return new Location(locations.get(beforeIndx).getLoc());
		else {
			GPSLocationState bLoc = locations.get(beforeIndx);
			GPSLocationState aLoc = locations.get(afterIndx);
			
			// Now we need to interpolate.  Create two lines both with time as the x axis.
			// One line has the longitude as the Y-axis while the other has the latitude.
			Location latBeforeLoc = new Location(bLoc.getLocation().latitude(), bLoc.getTimestamp());
			Location latAfterLoc = new Location(aLoc.getLocation().latitude(), aLoc.getTimestamp());
			Line latLine = new Line(latBeforeLoc, latAfterLoc);
			double interpLat = latLine.getLatitude(timestamp);
			
			Location lonBeforeLoc = new Location(bLoc.getTimestamp(), bLoc.getLocation().longitude());
			Location lonAfterLoc = new Location(aLoc.getTimestamp(), aLoc.getLocation().longitude());
			Line lonLine = new Line(lonBeforeLoc, lonAfterLoc);
			double interpLon = lonLine.getLongitude(timestamp);
			
			Location result = new Location(interpLat, interpLon);
			
			log("getLocation(timestamp):");
			log("\tBefore Location @" + bLoc.getTimestamp() + ": " + bLoc.getLocation());
			log("\tAfter Location @" + aLoc.getTimestamp() + ": " + aLoc.getLocation());
			log("\tInterpolated Location @" + timestamp + ": " + result);
			return result;
		}
		
//		PathEdge edge = getPathEdge(timestamp);
//		if (edge != null)
//			return edge.getLocation(timestamp);
//		else {
//			if (timestamp >= expStartTime) {
//				//log("getLocation(timestamp): timestamp is after experiment started but prior to robot starting on first edge.");
//				return pathEdges.get(0).getLocation(0).getLocation();
//			} else if (timestamp > getPathEdge(numEdges()-1).getEndTime()) {
//				PathEdge finalEdge = getPathEdge(numEdges()-1); 
//				return finalEdge.getEndLocation();
//			} else {
//				logErr("ERROR: getLocation(timestamp): No edge at time " + timestamp);
//				return null;
//			}
//		}
	}
	
	/**
	 * Returns the number of waypoints in the experiment.
	 * 
	 * @return the number of waypoints in the experiment.
	 */
	public double getNumWaypoints() {
		return pathEdges.size();
	}
	
	/**
	 * Calculates the lateness of the robot arriving at the specified waypoint.
	 * 
	 * @param wayPoint  The destination waypoint.
	 * @return The lateness of the robot arriving at the waypoint.
	 */
	public double getLatenessTo(int wayPoint) {
		if (wayPoint > getNumWaypoints()) {
			System.err.println("ERROR: RobotExpData.getLatenessTo: Specified waypoint greater than number of waypoints (" 
					+ wayPoint + " " + getNumWaypoints());
			System.exit(1); // fatal error
		}
		
		return pathEdges.get(wayPoint).getLateness();
	}
	
	private void logErr(String msg) {
		//if (System.getProperty ("PharosMiddleware.debug") != null)
		System.err.println("RobotExpData: " + msg);
	}
	
	private void log(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println("RobotExpData: " + msg);
	}
	
	public String toString() {
		String result = 
			"FileName: " + fileName
			+ "\nExpStartTime: " + expStartTime
			+ "\nNumber of path edges: " + pathEdges.size();
		for (int i=0; i < pathEdges.size(); i++) {
			PathEdge currEdge = pathEdges.get(i);
			result += "\n\tPath edge " + i + ": startTime = " + currEdge.getStartTime()
				+ ", stopTime = " + currEdge.getEndTime();
		}
		return result;
	}
	
	private static void print(String msg) {
		System.out.println(msg);
	}
	
	public static void main(String[] args) {
		if (args.length < 1) {
			print("Usage: pharoslabut.logger.analyzer.RobotExpdata [path to robot experiment data]");
			System.exit(0);
		}
		
		System.setProperty ("PharosMiddleware.debug", "true");
		
		RobotExpData red = new RobotExpData(args[0]);
		
		// Check whether timestamps were correctly calibrated.
		int numErrors = 0;
		Enumeration<GPSLocationState> locs = red.getGPSHistory().elements();
		while (locs.hasMoreElements()) {
			GPSLocationState currLoc = locs.nextElement();
			
			long logTimestamp = currLoc.getTimestamp();
			long gpsTimestamp = (currLoc.getLoc().getTime_sec() * 1000L);
			long timeDiff =  logTimestamp - gpsTimestamp;
			if (Math.abs(timeDiff) > 2000) { 
				print((++numErrors) + " ERROR: Bad time calibration in " + red.getFileName() + " (logTimestamp = " + logTimestamp + ", gpsTimestamp = " + gpsTimestamp + ", timeDiff = " + timeDiff + "): " + currLoc);
			}
		}
		if (numErrors == 0) {
			print("GPS timestamp calibration OK!");
		}
		
		// Check whether the getLocation method is OK
		print("Evaluating RobotExpData.getLocation(long timestamp)...");
		print("\tExperiment start time: " + red.getRobotStartTime());
		String testGetLocFileName = "RobotExpData-TestGetLocation.txt";
		FileLogger flogger = new FileLogger(testGetLocFileName, false);
		
		//int pathEdgeToCheck = 15;
		
		flogger.log("type,latitude,longitude,name,color");
		long currTime = red.getRobotStartTime();
		//long currTime = red.getPathEdge(pathEdgeToCheck).getStartTime();
		boolean firstLoc = true;
		while (currTime < red.getRobotStopTime()) {
		//while (currTime < red.getPathEdge(pathEdgeToCheck).getEndTime()) {
			Location currLoc = red.getLocation(currTime);
			String line = "T," + currLoc.latitude() + "," + currLoc.longitude();
			if (firstLoc) {
				line += ",getLocation,blue";
				firstLoc = false;
			}
			flogger.log(line);
			currTime += 1000;
		}
		
		flogger.log("type,latitude,longitude");
		locs = red.getGPSHistory().elements();
		//locs = red.getPathEdge(pathEdgeToCheck).getLocationsEnum();
		while (locs.hasMoreElements()) {
			GPSLocationState currLoc = locs.nextElement();
			flogger.log("W," + currLoc.getLocation().latitude() + "," + currLoc.getLocation().longitude());
		}
		
		
		print("\tEvaluation script saved to " + testGetLocFileName + ", upload it to GPSVisualizer to visualize...");
	}
}
