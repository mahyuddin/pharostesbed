package pharoslabut.logger.analyzer;

import java.io.*;
import java.util.*;

import pharoslabut.exceptions.PharosException;
import pharoslabut.logger.FileLogger;
import pharoslabut.RobotIPAssignments;
import pharoslabut.navigate.Location;
import playerclient3.structures.gps.PlayerGpsData;

/**
 * Encapsulates the data recorded by a robot during an experiment.
 * 
 * @author Chien-Liang Fok
 */
public class RobotExpData {
	/**
	 * The name of the robot's experiment log file.
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
	
	private FileLogger flogger = null;
	
	/**
	 * The constructor.
	 * 
	 * @param fileName The name of the robot's experiment log file.
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
	 * Sets the file logger for saving debug messages into a file.
	 * 
	 * @param flogger The file logger to use to save debug messages.
	 */
	public void setFileLogger(FileLogger flogger) {
		this.flogger = flogger;
	}
	
	/**
	 * Returns all of the beacons received from the specified robot during
	 * the window of time surrounding the specified time stamp.  That is, the period
	 * of time considered is [timestamp - windowSize/2, timestamp + windowSize/2].
	 * 
	 * @param robotID The ID of the robot that transmitted the beacons of interest.
	 * @param timestamp The end time of consideration.
	 * @param windowSize The period of time before the time stamp to consider.
	 * @return The beacons that were received during the specified time window, 
	 * sorted by the time they were received.
	 */
	private Vector<TelosBRxRecord> getBeaconsFrom(int robotID, long timestamp, long windowSize) {
		Vector<TelosBRxRecord> result = new Vector<TelosBRxRecord>();
		
		long startTime = timestamp - windowSize/2;
		if (startTime < getRobotStartTime())
			startTime = getRobotStartTime();
		
		long stopTime = timestamp + windowSize/2;
		if (stopTime > getRobotStopTime())
			stopTime = getRobotStopTime();
		
		Enumeration<TelosBRxRecord> e = telosBRxHist.elements();
		while (e.hasMoreElements()) {
			TelosBRxRecord currRecord = e.nextElement();
			
			// Only add the RSSI to the vector if the beacon was sent over
			// the relevant time windows and it was sent from the specified robot
			if (currRecord.getTimeStamp() >= startTime && currRecord.getTimeStamp() <= stopTime 
					&& currRecord.getSenderID() == robotID) 
			{
				result.add(currRecord);
			}
		}
		Collections.sort(result);
		return result;
	}
	/**
	 * Returns the average RSSI of beacons received from the specified
	 * robot at the specified timestamp and over the specified preceding window size.
	 * 
	 * @param robotID The ID of the robot whose beacons we want to examine.
	 * @param timestamp The time at which the neighbor list should be determined.
	 * @param windowSize The number of prior milliseconds over which to determine
	 * RSSI.  That is, a node is a neighbor if its beacon was received during
	 * the interval [timestamp - windowSize, timestamp].
	 * @return the average RSSI of the beacons received, or -1 if no beacons
	 * were received during that period of time, meaning the two robots were
	 * likely disconnected.
	 */
//	public double getAvgRSSIto(int robotID, long timestamp, long windowSize) {
//		Vector<TelosBRxRecord> result = getBeaconsFrom(robotID, timestamp, windowSize);
//		
//		if (result.size() == 0)
//			return -1;
//		else {
//			// Return the average RSSI of the beacons received from the specified robot
//			// during the time window.
//			double total = 0;
//			for (int i=0; i < result.size(); i++) {
//				total += result.get(i).getRSSI();
//			}
//			return total / result.size();
//		}
//	}
	
	/**
	 * Returns the estimated RSSI of beacons received from the specified
	 * robot at the specified time stamp.  This is done by finding the
	 * closest RSSI reading to the desired time stamp within a certain
	 * window of time surrounding the desired time.  <!-- performing a linear
	 * interpolation of RSSI readings that are received within the specified
	 * time window before and after the desired time.-->
	 * 
	 * @param robotID The ID of the robot whose beacons we want to examine.
	 * @param timestamp The time at which the neighbor list should be determined.
	 * @param windowSize The window of interest surrounding the timestamp in 
	 * milliseconds over which to determine RSSI.  That is, a node is a neighbor 
	 * if its beacon was received during the interval 
	 * [timestamp - windowSize/2, timestamp + windowSize/2].
	 * @return the average RSSI of the beacons received, or -1 if no beacons
	 * were received during that period of time, meaning the two robots were
	 * likely disconnected.
	 */
	public double getTelosBRSSIto(int robotID, long timestamp, long windowSize) {
		Vector<TelosBRxRecord> result = getBeaconsFrom(robotID, timestamp, windowSize);
		
		if (result.size() == 0) {
			// No beacons were received during this time interval
			return -1;
		} else if (result.size() == 1) {
			// If there was only one beacon received in the window, use its RSSI value directly
			TelosBRxRecord r = result.get(0);
			return r.getRSSI();
		} else {
			// If we actually received a beacon at the specified timestamp, use its RSSI value directly...
			for (int i = 0; i < result.size()-1; i++) {
				if (result.get(i).getTimeStamp() == timestamp)
					return result.get(i).getRSSI();
			}
			
			// Find the <time, RSSI> closest to the desired timestamp and use it.
			// This is guaranteed to be within half a window size of the desired time.
			int closestIndx = 0;
			long closestDelta = Math.abs(result.get(0).getTimeStamp() - timestamp);
			for (int i = 1; i < result.size(); i++) {
				long currDelta = Math.abs(result.get(i).getTimeStamp() - timestamp);
				if (currDelta < closestDelta) {
					closestIndx = i;
					closestDelta = currDelta;
				}
			}
			
			return result.get(closestIndx).getRSSI();
//			
//			// Find the <time, RSSI> pairs immediately before and after the specified
//			// timestamp.  Use linear interpolation to estimate RSSI at timestamp.
//			int beforeIndx = 0;
//			for (int i = 0; i < result.size()-1; i++) {
//				if (result.get(i).getTimeStamp() < timestamp)
//					beforeIndx = i;
//			}
//			
//			int afterIndx = result.size()-1;
//			for (int i = afterIndx; i > 0; i--) {
//				if (result.get(i).getTimeStamp() > timestamp)
//					afterIndx = i;
//			}
//			
//			TelosBRxRecord beforeRecord = result.get(beforeIndx);
//			TelosBRxRecord afterRecord = result.get(afterIndx);
//			
//			// If both anchor points are to the left or to the right of desired time stamp,
//			// just return the closest one...
//			if (beforeRecord.getTimeStamp() < timestamp && afterRecord.getTimeStamp() < timestamp) {
//				return afterRecord.getRSSI();
//			} else if (beforeRecord.getTimeStamp() > timestamp && afterRecord.getTimeStamp() > timestamp) {
//				return beforeRecord.getRSSI();
//			}
//			
//			// perform linear interpolation to guess RSSI value at the specified time step
//			Line l = new Line(new Location(beforeRecord.getRSSI(), beforeRecord.getTimeStamp()),
//					new Location(afterRecord.getRSSI(), afterRecord.getTimeStamp()));
//			double rssi = l.getLatitude(timestamp);
//
//			log("Desired time: " + timestamp);
//			for (int i=0; i < result.size(); i++) {
//				TelosBRxRecord currRecord = result.get(i);
//				log(i + "\t" + (currRecord.getTimeStamp() - timestamp) + "\t" + currRecord.getRSSI());
//			}
//			log("beforeIndx = " + beforeIndx + ", afterIndx = " + afterIndx);
//			log("Before record: " + beforeRecord);
//			log("After record: " + afterRecord);
//			log("Estimated RSSI at " + timestamp + " = " + rssi);
//			
//			return rssi;
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
		String robotFileName = fileName;
		
		// If the file name contains directories, remove the directories
		// from the name first.
		if (robotFileName.indexOf("/") != -1)
			robotFileName = robotFileName.substring(fileName.lastIndexOf("/")+1);
		String[] tokens = robotFileName.split("-");
		
//		log("Getting robot name: robotFileName = " + robotFileName);
//		for (int i=0; i < tokens.length; i++) {
//			log(i + ": " + tokens[i]);
//		}
		return tokens[2];
	}
	
	/**
	 * Returns the robot's ID, which is the last octal of the robot's IP address.
	 * 
	 * @return the last octal of the robot's IP address, or -1 if IP address is unknown.
	 */
	public int getRobotID() {
		int id = -1;
		
		try {
			id = RobotIPAssignments.getRobotID(getRobotName());
		} catch (PharosException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
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
			else if (line.contains("WayPointFollower: Going to") 
					|| line.contains("MotionScriptFollower: Going to")) 
			{
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
				if (pharoslabut.sensors.GPSDataBuffer.isValid(l)) {
					locations.add(new GPSLocationState(timeStamp, currLoc));
					if (currEdge != null) {
						if (!currEdge.hasStartLoc())
							currEdge.setStartLoc(l);
					}
				} else {
					log("Rejecting invalid location: " + currLoc);
				}
			}
			else if (line.contains("RadioSignalMeter: SEND_BCAST") 
					|| line.contains("TelosBeaconBroadcaster: SEND_TELSOB_BCAST")) {
				// The format of this line is:
				// [local time stamp] RadioSignalMeter: SEND_BCAST [node id] [seqno]
				String[] tokens = line.split("(\\s|\\[|\\])");
				TelosBTxRecord txRec = new TelosBTxRecord(
						Long.valueOf(tokens[1]), // timestamp
						Integer.valueOf(tokens[5]), // sender ID
						Integer.valueOf(tokens[6])); // seqno
				telosBTxHist.add(txRec);
				
			}
			else if (line.contains("RadioSignalMeter: RADIO_CC2420_RECEIVE")
					|| line.contains("TelosBeaconReceiver: RADIO_CC2420_RECEIVE")) {
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
		
		//double diffSum = 0; // the sum of all the diffs
		TimeCalibrator calibrator = new TimeCalibrator();
		
		// Calculate the difference between the log file and GPS timestamps
		for (int i=0; i < locs.size(); i++) {
			
			// The following is the number of milliseconds that have passed since January 1, 1970 UTC.
			long localTimestamp = locs.get(i).getTimestamp();
			
			// This is the GPS timestamp.  The code that generates it is available here:
			// http://playerstage.svn.sourceforge.net/viewvc/playerstage/code/player/tags/release-2-1-3/server/drivers/gps/garminnmea.cc?revision=8139&view=markup
			// Note that the microsecond field is always zero, meaning it only provides
			// one second resolution.  The value is the number of seconds elapsed since 
			// 00:00 hours, Jan 1, 1970 UTC.
			int gpsSec = locs.get(i).getLoc().getTime_sec();
//			int gpsUSec = locs.get(i).getLoc().getTime_usec();
			
			// Calculate the difference between the two time stamps this is in second units
			double diff = (localTimestamp / 1000.0) - gpsSec;
			
			//diffSum += diff;
			calibrator.addCalibrationPoint(localTimestamp, diff * 1000);
			
//			String str = localTimestamp + "\t" + gpsSec +"\t" + diff;
//			System.out.println(str);
//			flogger.log(str);
		}
		
		// Calculate the average offset in milliseconds and use it to calibrate all of the local timestamps.
		//double timeOffset = diffSum / locs.size() * 1000;
		
//		String str = "timeOffset: " + timeOffset;
//		System.out.println(str);
//		flogger.log(str);
		
		// Calibrate all of the timestamps...
		//expStartTime = RobotExpData.getCalibratedTime(expStartTime, timeOffset);
		expStartTime = calibrator.getCalibratedTime(expStartTime);
		
		for (int i=0; i < locations.size(); i++) {
			GPSLocationState currLoc = locations.get(i);
			currLoc.calibrateTime(calibrator);
		}
		
		for (int i=0; i < pathEdges.size(); i++) {
			PathEdge currEdge = pathEdges.get(i);
			currEdge.calibrateTime(calibrator);
		}
		
		for (int i=0; i < telosBRxHist.size(); i++) {
			TelosBRxRecord rxRec = telosBRxHist.get(i);
			rxRec.calibrateTime(calibrator);
		}
		
		for (int i=0; i < telosBTxHist.size(); i++) {
			TelosBTxRecord txRec = telosBTxHist.get(i);
			txRec.calibrateTime(calibrator);
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
	 * Returns the TelosB wireless packet reception history from
	 * a specific sender.
	 * 
	 * @param sndrID The ID of the sender.
	 * @return the TelosB wireless packet reception history.
	 */
	public Vector<TelosBRxRecord> getTelosBRxHist(int sndrID) {
		Vector<TelosBRxRecord> result = new Vector<TelosBRxRecord>();
		Enumeration<TelosBRxRecord> e = telosBRxHist.elements();
		while (e.hasMoreElements()) {
			TelosBRxRecord ce = e.nextElement();
			if (ce.getSenderID() == sndrID)
				result.add(ce);
		}
		return result;
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
	 * Returns the neighbor list of this robot.
	 * 
	 * @param timestamp The time at which the neighbor list should be determined.
	 * @param windowSize The number of prior milliseconds over which to determine
	 * neighbors.  That is, a node is a neighbor if its beacon was received during
	 * the interval [timestamp - windowSize, timestamp].
	 * @return The list of neighbors at the specified time.
	 */
	public Vector<Integer> getTelosBConnectivity(long timestamp, long windowSize) {
		Vector<Integer> result = new Vector<Integer>();
		
		long startTime = timestamp - windowSize;
		if (startTime < getRobotStartTime())
			startTime = getRobotStartTime();
		
		long stopTime = timestamp;
		if (stopTime > getRobotStopTime())
			stopTime = getRobotStopTime();
		
		Enumeration<TelosBRxRecord> e = telosBRxHist.elements();
		while (e.hasMoreElements()) {
			TelosBRxRecord currRecord = e.nextElement();
			if (currRecord.getTimeStamp() >= startTime && currRecord.getTimeStamp() <= stopTime) {
				
				// only add it to the vector if it does not already contain the value.
				if (!result.contains(currRecord.getSenderID()))
					result.add(currRecord.getSenderID());
			}
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
			log("WARNING: getLocation(timestamp): timestamp prior to robot start time. (" + timestamp + " < " + getRobotStartTime() + ")");
			return getBeginLocation();
		}
		
		if (timestamp > getRobotStopTime()) {
			log("WARNING: getLocation(timestamp): timestamp after robot end time. (" + getRobotStopTime() + " < " + timestamp + ")");
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
			
			return getInterpolatedLoc(
					bLoc.getLocation().latitude(), bLoc.getLocation().longitude(), bLoc.getTimestamp(),
					aLoc.getLocation().latitude(), aLoc.getLocation().longitude(), aLoc.getTimestamp(),
					timestamp, flogger);
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
	 * Interpolates the location of a robot at a certain time based on two locations that
	 * occur before and after the desired time.  The first three parameters specify
	 * the location of the robot prior to the desired time.  The second three parameters
	 * specify a known location of the robot after the desired time.  The last parameter
	 * specifies the time at which we want to estimate the robot's location.
	 * 
	 * @param lat1 The latitude of the robot before the desired time.
	 * @param lon1 The longitude of the robot before the desired time.
	 * @param time1 The timestamp of the location of the robot before the desired time.
	 * @param lat2  The latitude of the robot after the desired time.
	 * @param lon2 The longitude of the robot after the desired time.
	 * @param time2 The timestamp of the location of the robot after the desired time.
	 * @param timestamp The time at which to interpolate the robot's location.
	 * @param flogger The file logger for recording debug statements.
	 * @return The location of the robot at the specified timestamp.
	 */
	public static Location getInterpolatedLoc(double lat1, double lon1, long time1,
		double lat2, double lon2, long time2, long timestamp, FileLogger flogger) 
	{
		// Now we need to interpolate.  Create two lines both with time as the x axis.
		// One line has the longitude as the Y-axis while the other has the latitude.
		Location latBeforeLoc = new Location(lat1, time1);
		Location latAfterLoc = new Location(lat2, time2);
		Line latLine = new Line(latBeforeLoc, latAfterLoc);
		double interpLat = latLine.getLatitude(timestamp);
		
		Location lonBeforeLoc = new Location(time1, lon1);
		Location lonAfterLoc = new Location(time2, lon2);
		Line lonLine = new Line(lonBeforeLoc, lonAfterLoc);
		double interpLon = lonLine.getLongitude(timestamp);
		
		Location result = new Location(interpLat, interpLon);
		
		log("RobotExpData.getInterpolatedLoc:", flogger);
		log("\tBefore Location @" + time1 + ": (" + lat1 + ", " + lon1 + ")", flogger);
		log("\tAfter Location @" + time2 + ": (" +  lat2 + ", " + lon2 + ")", flogger);
		log("\tInterpolated Location @" + timestamp + ": " + result, flogger);
		return result;
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
	
	private static void log(String msg, FileLogger flogger) {
		String result = "RobotExpData: " + msg; 
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
	
	private void log(String msg) {
		String result = "RobotExpData: " + msg; 
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
	
	/**
	 * Calculates the time since the robot start time.
	 * 
	 * @param time the absolute time stamp
	 * @return The relative time that has passed since the robot start time.
	 */
	private long getRelativeTime(long time) {
		return (time - getRobotStartTime())/1000;
	}
	
	public String toString() {
		String result = 
			"File Name: " + getFileName()
			+ "\nRobot Start Time: " + getRobotStartTime()
			+ "\nNumber of path edges: " + pathEdges.size();
		for (int i=0; i < pathEdges.size(); i++) {
			PathEdge currEdge = pathEdges.get(i);
			result += "\n\tPath edge " + i + ": startTime = " + currEdge.getStartTime() + " (" + getRelativeTime(currEdge.getStartTime()) + "s)"
				+ ", stopTime = " + currEdge.getEndTime() + " (" + getRelativeTime(currEdge.getEndTime()) + "s)";
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
			if (Math.abs(timeDiff) > 1000) { 
				print((++numErrors) + "/" + red.getGPSHistory().size() + " ERROR: Bad time calibration in " + red.getFileName() 
						+ " (logTimestamp = " + logTimestamp + ", gpsTimestamp = " + gpsTimestamp 
						+ ", timeDiff = " + timeDiff + "): " + currLoc);
			}
		}
		
		if (numErrors == 0) {
			print("GPS timestamp calibration OK!");
		}
		
		print("Robot start time: " + red.getRobotStartTime());
		
		boolean testGetLocation = false;
		if (testGetLocation) {
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
}
