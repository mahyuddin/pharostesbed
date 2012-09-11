package pharoslabut.logger.analyzer;

import java.io.*;
import java.util.*;

import pharoslabut.beacon.WiFiBeacon;
import pharoslabut.exceptions.PharosException;
import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.RobotIPAssignments;
import pharoslabut.navigate.Location;
import playerclient3.structures.gps.PlayerGpsData;

/**
 * Holds the data recorded by a robot during an experiment.
 * 
 * @author Chien-Liang Fok
 */
public class RobotExpData {
	public static final boolean ENABLE_DEBUG_STATEMENTS = true;
	/**
	 * The name of the robot's experiment log file.
	 */
	protected String fileName;
	
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
	 * The locations of the robot as it traversed this edge.
	 */
	private Vector<GPSLocationState> locations = new Vector<GPSLocationState>();
	
	/**
	 * The heading of the robot as it executes the experiment.
	 */
	private Vector<HeadingState> headings = new Vector<HeadingState>();
	
	/**
	 * Records when the TelosB receives a broadcasts.
	 */
	private Vector<TelosBRxRecord> telosBRxHist = new Vector<TelosBRxRecord>();
	
	/**
	 * Records when the TelosB sends a broadcast.
	 */
	private Vector<TelosBTxRecord> telosBTxHist = new Vector<TelosBTxRecord>();
	
	/**
	 * For logging debug messages.
	 */
//	private FileLogger flogger = null;
	
	/**
	 * Contains the timestamps of when GPS errors occur.
	 */
	private Vector<Long> gpsErrors = new Vector<Long>();
	
	/**
	 * Contains the timestamps of when compass errors occur.
	 */
	private Vector<Long> headingErrors = new Vector<Long>();
	
	/**
	 * Holds the logical motion commands issued by the MotionArbiter to the
	 * robot.
	 */
	private Vector<MotionCmd> motionCmds = new Vector<MotionCmd>();
	
	/**
	 * Holds the times when a WiFi beacon was transmitted.
	 */
	private Vector<WiFiBeaconTx> wifiBeaconTxs = new Vector<WiFiBeaconTx>();
	
	/**
	 * Holds the times when a WiFi beacon was received.
	 */
	private Vector<WiFiBeaconRx> wifiBeaconRxs = new Vector<WiFiBeaconRx>();
	
	/**
	 * This is a protected constructor that does not take any parameters. It is used
	 * by subclasses.
	 */
	protected RobotExpData() {
	}
	
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
		
		// Debugging code...
//		log("Time\tRel Time (s)\tSpeed Command (m/s)\tHeading Command (radians)");
//		for (int i=0; i < motionCmds.size(); i++) {
//			//if (motionCmds.get(i).speedCmd == 0)
//				log(motionCmds.get(i).toString());
//		}
//		log("End of MotionCmd table.");
	}
	
//	/**
//	 * Sets the file logger for saving debug messages into a file.
//	 * 
//	 * @param flogger The file logger to use to save debug messages.
//	 */
//	public void setFileLogger(FileLogger flogger) {
//		this.flogger = flogger;
//	}
	
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
		if (startTime < getStartTime())
			startTime = getStartTime();
		
		long stopTime = timestamp + windowSize/2;
		if (stopTime > getStopTime())
			stopTime = getStopTime();
		
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
	 * Tokenizes the file name based on the dashes.  Removes all 
	 * directories from the name.  The two types of file names supported include:
	 * 
	 *   [mission name]-[exp desc]-[exp name]-[robot name]-[...]
	 * 
	 * and
	 * 
	 *   [mission name]-[exp name]-[robot name]-[...]
	 *   
	 * @return The tokenized file name.
	 */
	private String[] tokenizeFileName() {
		String robotFileName = fileName;
		
		// If the file name contains directories, remove the directories from the name first.
		if (robotFileName.indexOf("/") != -1)
			robotFileName = robotFileName.substring(fileName.lastIndexOf("/")+1);
		String[] tokens = robotFileName.split("-");
		
//		log("Tokenizing robotFileName = " + robotFileName);
//		for (int i=0; i < tokens.length; i++) {
//			log(i + ": " + tokens[i]);
//		}
		
		return tokens;
	}
	
	/**
	 * Returns the robot's name.
	 * 
	 * @return the robot's name, or null if unknown.
	 */
	public String getRobotName() {
		String[] tokens = tokenizeFileName();
		
		if (fileName.contains("MRPatrol")) {
			return tokens[2];
		} else {
			if (tokens.length > 2) {
				if (tokens.length == 5)
					return tokens[3];
				else
					return tokens[2];
			} else
				return null;
		}
	}
	
	/**
	 * Returns the mission's name.
	 * 
	 * @return the mission's name, or null if unknown.
	 */
	public String getMissionName() {
		String[] tokens = tokenizeFileName();
		if (tokens.length > 0)
			return tokens[0];
		else
			return null;
	}
	
	/**
	 * Returns the experiment's name.
	 * 
	 * @return the experiment's name, or null if unknown.
	 */
	public String getExpName() {
		String[] tokens = tokenizeFileName();
		if (tokens.length > 1) {
			if (tokens.length == 5)
				return tokens[1] + "-" + tokens[2];
			else
				return tokens[1];
		} else
			return null;
	}
	
	/**
	 * Returns the robot's ID, which is the last octal of the robot's IP address.
	 * 
	 * @return the last octal of the robot's IP address, or -1 if IP address is unknown.
	 */
	public int getRobotID() {
		int id = -1;
		
		try {
			id = RobotIPAssignments.getID(getRobotName());
		} catch (PharosException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return id;
	}
	
	/**
	 * Reads and organizes the data contained in the robot's experiment log file.
	 * 
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	protected void readFile() throws NumberFormatException, IOException {
		File file = new File(fileName);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
		PathEdge currEdge = null;
		PlayerGpsData currLoc = null;
		boolean expStartTimeSet = false;
		
		while ((line = br.readLine()) != null){
			
			// Extract the experiment start times.
			if (line.contains("Starting experiment at time:")) {
				String[] tokens = line.split("[: ]");
				expStartTime = Long.valueOf(tokens[8]);
				Logger.logDbg("expStartTime = " + expStartTime);
				expStartTimeSet = true;
			}
			else if (line.contains("Starting experiment at time ")) {
				String keyStr = "Starting experiment at time ";
				String startingLine = line.substring(line.indexOf(keyStr) + keyStr.length());
				
				String[] tokens = startingLine.split("[.\\s]");
				expStartTime = Long.valueOf(tokens[0]);
				Logger.logDbg("expStartTime = " + expStartTime);
				expStartTimeSet = true;
			} 
			
			// Record the next waypoint.
			else if (line.contains("WayPointFollower: Going to") 
					|| (line.contains("MotionScriptFollower:") && line.contains("Going to "))) 
			{
				String keyStr = "Going to ";
				String goingToLine = line.substring(line.indexOf(keyStr) + keyStr.length());
				String[] tokens = goingToLine.split("[\\(\\),\\s,m]+");
				
				long timeStamp = Long.valueOf(line.substring(1,line.indexOf(']')));
				double lat = Double.valueOf(tokens[1]);
				double lon = Double.valueOf(tokens[2]);
				double speed = Double.valueOf(tokens[5]); 
				
				currEdge = new PathEdge(new Location(lat, lon), timeStamp, speed);
				
				// Set the start location of the path edge if we know where we are.
				if (currLoc != null)
					currEdge.setStartLoc(new Location(currLoc));
			}
			else if (line.contains("TestNavigateCompassGPS: Going to")) {
				String[] tokens = line.split("[\\[\\]:=(), ]");
//				for (int i=0; i < tokens.length; i++) {
//					System.out.println(i + ": " + tokens[i]);
//				}
				long timeStamp = Long.valueOf(tokens[1]);
				double lat = Double.valueOf(tokens[9]);
				double lon = Double.valueOf(tokens[11]);
				double speed = Double.valueOf(tokens[16]);
				
				currEdge = new PathEdge(new Location(lat, lon), timeStamp, speed);
				
				// Set the start location of the path edge if we know where we are.
				if (currLoc != null)
					currEdge.setStartLoc(new Location(currLoc));
			}
			
			// Extract the GPS location data.
			else if (line.contains("New GPS Data:")) {
				String keyStr = "New GPS Data:";
				String gpsLine = line.substring(line.indexOf(keyStr) + keyStr.length());
				
				String[] tokens = gpsLine.split("[:=(), ]");
				
				long timeStamp = Long.valueOf(line.substring(1,line.indexOf(']')));
				currLoc = new PlayerGpsData();
				currLoc.setAltitude(Integer.valueOf(tokens[12]));
				currLoc.setErr_horz(Double.valueOf(tokens[26]));
				currLoc.setErr_vert(Double.valueOf(tokens[28]));
				currLoc.setHdop(Integer.valueOf(tokens[22]));
				currLoc.setLatitude(Integer.valueOf(tokens[8]));
				currLoc.setLongitude(Integer.valueOf(tokens[10]));
				currLoc.setNum_sats(Integer.valueOf(tokens[20]));
				currLoc.setQuality(Integer.valueOf(tokens[18]));
				currLoc.setTime_sec(Integer.valueOf(tokens[4]));
				currLoc.setTime_usec(Integer.valueOf(tokens[6]));
				currLoc.setUtm_e(Double.valueOf(tokens[14]));
				currLoc.setUtm_n(Double.valueOf(tokens[16]));
				currLoc.setVdop(Integer.valueOf(tokens[24]));
				
				Location l = new Location(currLoc);
				if (pharoslabut.sensors.GPSDataBuffer.isValid(l)) {  // Only add the GPSLocation if it is valid.
					locations.add(new GPSLocationState(timeStamp, currLoc));
					if (currEdge != null) {
						if (!currEdge.hasStartLoc())
							currEdge.setStartLoc(l);
					}
				} else {
					Logger.log("Rejecting invalid location: " + currLoc);
				}
			}
			// This is for Proteus III log files.
			else if (line.contains("Received GPS message:")) {
				String keyStr = "Received GPS message:";
				String gpsLine = line.substring(line.indexOf(keyStr) + keyStr.length());
				
				String[] tokens = gpsLine.split("[,\\s)]");
				
				long timeStamp = Long.valueOf(line.substring(1,line.indexOf(']')));
				currLoc = new PlayerGpsData();
				currLoc.setAltitude((int)(Double.valueOf(tokens[20]) * 1000));  // convert from meters to mm
				//currLoc.setErr_horz(Double.valueOf(tokens[26]));
				//currLoc.setErr_vert(Double.valueOf(tokens[28]));
				currLoc.setHdop((int)(Double.valueOf(tokens[40]) * 10));
				currLoc.setLatitude((int)(Double.valueOf(tokens[12]) * 1e7));
				currLoc.setLongitude((int)(Double.valueOf(tokens[16]) * 1e7));
				currLoc.setNum_sats(Integer.valueOf(tokens[36]));
				currLoc.setQuality(Integer.valueOf(tokens[32]));
				currLoc.setTime_sec(Integer.valueOf(tokens[4]));
				currLoc.setTime_usec(Integer.valueOf(tokens[8]));
				currLoc.setUtm_e(Double.valueOf(tokens[24]));
				currLoc.setUtm_n(Double.valueOf(tokens[28]));
				currLoc.setVdop((int)(Double.valueOf(tokens[44]) * 10));
				
				Location l = new Location(currLoc);
				if (pharoslabut.sensors.GPSDataBuffer.isValid(l)) {  // Only add the GPSLocation if it is valid.
					locations.add(new GPSLocationState(timeStamp, currLoc));
					if (currEdge != null) {
						if (!currEdge.hasStartLoc())
							currEdge.setStartLoc(l);
					}
				} else {
					Logger.log("Rejecting invalid location: " + currLoc);
				}
			}
			
			
			// Extract the heading measurements of the robot
			// The following is printed by the CompassDataBuffer during Mission 15
			else if (line.contains("New heading:")) {
				String keyStr = "New heading:";
				String headingLine = line.substring(line.indexOf(keyStr) + keyStr.length());
				
				String[] tokens = headingLine.split("[, ]");
				
				long timeStamp = Long.valueOf(line.substring(1,line.indexOf(']')));
				double heading = Double.valueOf(tokens[1]);
				
				// Only add the heading measurement if it is valid.
				if (pharoslabut.sensors.CompassDataBuffer.isValid(heading)) {
					headings.add(new HeadingState(timeStamp, heading));
					if (currEdge != null) {
						if (!currEdge.hasStartHeading())
							currEdge.setStartHeading(heading);
					}
				} else {
					Logger.log("Rejecting invalid heading: " + heading);
				}
			}
			// The following is printed by the CompassDataBuffer during Mission 25
			else if (line.contains("New heading=")) {
				String keyStr = "New heading=";
				String headingLine = line.substring(line.indexOf(keyStr) + keyStr.length());
				
				String[] tokens = headingLine.split("[, ]");
				
				long timeStamp = Long.valueOf(line.substring(1,line.indexOf(']')));
				double heading = Double.valueOf(tokens[0]);
				
				// Only add the heading measurement if it is valid.
				if (pharoslabut.sensors.CompassDataBuffer.isValid(heading)) {
					headings.add(new HeadingState(timeStamp, heading));
					if (currEdge != null) {
						if (!currEdge.hasStartHeading())
							currEdge.setStartHeading(heading);
					}
				} else {
					Logger.log("WARNING: Rejecting invalid heading: " + heading);
				}
			}
			
			// Extract when the TelosB transmitted a beacon.
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
			
			// Extract when a TelosB beacon was received.
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
			
			// Extract when a WiFi beacon was broadcasted.
			else if (line.contains("WiFiBeaconBroadcaster: Broadcasting Beacon:") // valid for log files up to and including Mission 26
					|| (line.contains("pharoslabut.beacon.WiFiBeaconBroadcaster") && line.contains("Broadcasting Beacon:"))) // valid for missions 27 and above
			{
				String keyStr = "Broadcasting Beacon:";
				String broadcastLine = line.substring(line.indexOf(keyStr) + keyStr.length());
				
				long timestamp = Long.valueOf(line.substring(1,line.indexOf(']')));
				
				String[] tokens = broadcastLine.split("[\\(\\):, ]");
				String ipAddress = tokens[2];
				int port = Integer.valueOf(tokens[3]);
				long seqno = Long.valueOf(tokens[4]);
				
				WiFiBeaconTx beaconTx = new WiFiBeaconTx(new WiFiBeacon(ipAddress, port, seqno), timestamp);
				wifiBeaconTxs.add(beaconTx);
				
			}
			
			// Extract when a WiFi beacon was received.
			else if (line.contains("BeaconReciever: Received beacon:") // valid for log files up to and including Mission 26
					|| (line.contains("pharoslabut.beacon.WiFiBeaconReceiver") && line.contains("Received beacon:"))) // valid for missions 27 and above
			{
				String keyStr = "Received beacon:";
				String rcvLine = line.substring(line.indexOf(keyStr) + keyStr.length());
				
				long timestamp = Long.valueOf(line.substring(1,line.indexOf(']')));
				
				String[] tokens = rcvLine.split("[\\(\\):, ]");
				String ipAddress = tokens[2];
				int port = Integer.valueOf(tokens[3]);
				long seqno = Long.valueOf(tokens[4]);
				
				WiFiBeaconRx beaconRx = new WiFiBeaconRx(new WiFiBeacon(ipAddress, port, seqno), timestamp);
				wifiBeaconRxs.add(beaconRx);
			}

			// Extract when the current waypoint was reached.
			else if (line.contains("NavigateCompassGPS:") && line.contains("Arrived at destination")) {
				// Save the end time of the experiment
				long timestamp = Long.valueOf(line.substring(1,line.indexOf(']')));
				
				if (currEdge == null) {
					Logger.logWarn("Arrived at destination but currEdge is null!");
					//System.exit(1);
				} else {
				
					currEdge.setEndTime(timestamp);
					pathEdges.add(currEdge);
					currEdge = null;
				}
			}
			
			// Extract when the GPS sensor failed.
			else if (line.contains("go: ERROR: Unable to get the current location") 
					|| line.contains("ERROR: Failed to get current location")) 
			{
				
				long timeStamp = Long.valueOf(line.substring(1,line.indexOf(']')));
//				Logger.log("Found a GPS sensor fault at time " + timeStamp);
				gpsErrors.add(timeStamp);
			}
			
			// Extract when the compass sensor failed.
			else if (line.contains("go: ERROR: Unable to get the current heading")
					|| line.contains("ERROR: Unable to get compass heading")) 
			{
				long timeStamp = Long.valueOf(line.substring(1,line.indexOf(']')));
				headingErrors.add(timeStamp);
			}
			
			// Extract the motion commands being issued by the motion arbiter
			else if (line.contains("MotionArbiter:") && line.contains("Sending motion command")) {
				String keyStr = "Sending motion command";
				String headingLine = line.substring(line.indexOf(keyStr) + keyStr.length());
				
				String[] tokens = headingLine.split("[=,\\s ]+");
				
				long timeStamp = Long.valueOf(line.substring(1,line.indexOf(']')));
				
				double speed  = Double.valueOf(tokens[2]);
				double heading = Double.valueOf(tokens[4]);
				
				
				motionCmds.add(new MotionCmd(timeStamp, speed, heading));
			}
			
			// Extract the pause durations
			else if (line.contains("MotionScriptFollower:") && line.contains("Pausing for")) {
				String keyStr = "Pausing for";
				String pausingLine = line.substring(line.indexOf(keyStr) + keyStr.length());
				
				String[] tokens = pausingLine.split("[=,\\s m]+");
				
				//long timeStamp = Long.valueOf(line.substring(1,line.indexOf(']')));
				
				long pauseTime  = Long.valueOf(tokens[1]);
				
				if (currEdge != null)
					currEdge.addPauseTime(pauseTime);
				else if (pathEdges.size() > 0)
					pathEdges.get(pathEdges.size()-1).addPauseTime(pauseTime);
				else
					Logger.logWarn("Discarding pause time because currEdge not defined and there were not previous edges!");
			}
		} // end while...
		
		// Set the ideal start locations...
		for (int i=0; i < pathEdges.size(); i++) {
			if (i == 0) {
				// The first edge's ideal start location is where it actually
				// started.
				pathEdges.get(0).setIdealStartLoc(getStartLocation());
			} else {
				// For every path edge after the first one, the ideal start location
				// the ideal end location of the previous edge.
				PathEdge prevEdge = pathEdges.get(i-1);
				pathEdges.get(i).setIdealStartLoc(prevEdge.getEndLocation());
			}
		}
		
		// Do some sanity checks...
		if (!expStartTimeSet) {
			Logger.logWarn("Experiment start time not set!  Using timestamp of first line in the log file.");
			br = new BufferedReader(new FileReader(file));
			line = br.readLine();
			
			expStartTime = Long.valueOf(line.substring(1,line.indexOf(']')));
			expStartTimeSet = true;
			Logger.logDbg("expStartTime = " + expStartTime);
		}
		
		if (locations.size() == 0) {
			Logger.logErr("robot has no known locations...");
			System.exit(1);
		}
		
		// Add sequence number to the path edges...
		for (int i=0; i < pathEdges.size(); i++) {
			pathEdges.get(i).setSeqNo(i);
		}
	}
	
	/**
	 * Compares the GPS timestamps with the log timestamps to determine the 
	 * offset needed to calibrate the log timestamps to match the GPS timestamps.
	 * The GPS timestamps are assumed to be accurate to within 1 second.
	 * 
	 * @return The time calibrator.  This can be used by subclasses.
	 */
	protected TimeCalibrator calibrateTime() {
		// First gather all of the location data...
		Vector<GPSLocationState> locs = getGPSHistory();
		
//		pharoslabut.logger.FileLogger flogger = new pharoslabut.logger.FileLogger("CalibrateTime", false);
		
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
			
			// Calculate the difference between the two timestamps this is in second units
			double diff = (localTimestamp / 1000.0) - gpsSec;
			
			calibrator.addCalibrationPoint(localTimestamp, diff * 1000);
			
//			String str = localTimestamp + "\t" + gpsSec +"\t" + diff;
//			System.out.println(str);
//			flogger.log(str);
		}
		
		// Calibrate all of the timestamps...
		expStartTime = calibrator.getCalibratedTime(expStartTime);
		
		for (int i=0; i < locations.size(); i++) {
			GPSLocationState currLoc = locations.get(i);
			currLoc.calibrateTime(calibrator);
		}
		
		for (int i=0; i < headings.size(); i++) {
			HeadingState currHeading = headings.get(i);
			currHeading.calibrateTime(calibrator);
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
		
		for (int i=0; i < gpsErrors.size(); i++) {
			gpsErrors.set(i, calibrator.getCalibratedTime(gpsErrors.get(i)));
		}
		
		for (int i=0; i < headingErrors.size(); i++) {
			headingErrors.set(i, calibrator.getCalibratedTime(headingErrors.get(i)));
		}
		
		for (int i=0; i < motionCmds.size(); i++) {
			motionCmds.get(i).calibrateTime(calibrator);
		}
		
		for (int i=0; i < wifiBeaconRxs.size(); i++) {
			wifiBeaconRxs.get(i).calibrateTime(calibrator);
		}
		
		for (int i=0; i < wifiBeaconTxs.size(); i++) {
			wifiBeaconTxs.get(i).calibrateTime(calibrator);
		}
		
		return calibrator;
	}
	
	/**
	 * 
	 * @return The beacon reception events.
	 */
	public Vector<WiFiBeaconRx> getWiFiBeaconRxs() {
		return wifiBeaconRxs;
	}
	
	/**
	 * 
	 * @return The beacon transmission events.
	 */
	public Vector<WiFiBeaconTx> getWiFiBeaconTxs() {
		return wifiBeaconTxs;
	}
	
	/**
	 * 
	 * @return A vector containing the timestamps of when the GPS sensor failed.
	 */
	public Vector<Long> getGPSErrors() {
		return gpsErrors;
	}
	
	/**
	 * 
	 * @return A vector containing the timestamps of when the heading sensor failed.
	 */
	public Vector<Long> getHeadingErrors() {
		return headingErrors;
	}
//	/**
//	 * Returns the calibrated timestamp to one second accuracy.
//	 * 
//	 * @param timestamp The recorded timestamp.  This may be inaccurate.
//	 * @return The calibrated timestamp.  This is accurate to within one second.
//	 */
//	public static long getCalibratedTime(long timestamp, double offset) {
//		return Math.round(timestamp - offset);
//	}
	
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
	 * 
	 * @return The waypoints of the motion script.
	 */
	public Vector<Location> getWayPoints() {
		Vector<Location> results = new Vector<Location>();
		for (int i=0; i < pathEdges.size(); i++) {
			PathEdge pe = pathEdges.get(i);
			results.add(pe.getEndLocation());
		}
		return results;
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
			Logger.logErr("Specified timestamp (" + timestamp + ") prior to start of experiment (" + expStartTime + ")");
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
			Logger.logErr("Unable to find edge containing timestamp " + timestamp);
			System.exit(1);
		}
		
		Logger.log("getPathEdge: indx = " + edgeIndx);
		
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
		return locations;
	}
	
	/**
	 * Gets the time at which the robot started.  This is the time at which the
	 * robot received the StartExpMsg.
	 * 
	 * @return The robot's start time.
	 */
	public long getStartTime() {
		return expStartTime;
	}
	
	/**
	 * Gets the robot's end time.  This is the later of the end time of the last edge traversed,
	 * or the last GPS measurement recorded.
	 * 
	 * @return The experiment end time.
	 */
	public long getStopTime() {
		long result = -1;
		
		result = getFinalWaypointArrivalTime();
		
		// If the last location is received *after* the end time of the last edge traversed,
		// consider it to be the end time of the robot.
		if (locations.size() > 0) {
			long locEndTime = locations.get(locations.size()-1).getTimestamp();
			if (locEndTime > result)
				result = locEndTime;
		} 
		
		if (result == -1) {
			Logger.logErr("Cannot get experiment end time because numEdges and locations are both zero! Log File: " + fileName);
			new Exception().printStackTrace(); // get a stack trace for debugging...
			System.exit(1);
		}
		
		return result;
	}
	
	/**
	 * Gets the time at which the robot arrives as the final way point.
	 * 
	 * @return The time at which the last waypoint was reached.
	 */
	public long getFinalWaypointArrivalTime() {
		// Find the time when the robot finished traversing the last edge.
		if (numEdges() > 0) {
			PathEdge lastEdge = getPathEdge(numEdges()-1);
			return lastEdge.getEndTime();
		} else {
			Logger.logWarn("Robot did not traverse any edges!  Using last location reading");
			if (locations.size() > 0) {
				GPSLocationState finalLoc = locations.get(locations.size()-1);
				return finalLoc.getTimestamp();
			} else {
				Logger.logWarn("No final location!");
			}
		}
		
		return -1;
	}
	
	
	/**
	 * Returns the speed command that the Pharos Middleware is issuing to the robot.
	 * 
	 * @param time The time at which to determine the logical speed.
	 * @return The speed that the Pharos Middleware wants the robot to travel at in m/s.
	 */
	public double getSpeedCmd(long time) {
		int indx = getRelevantMotionCmdIndx(time);
		return motionCmds.get(indx).speedCmd;
	}
	
	/**
	 * Returns the robot's estimated speed based on consecutive GPS measurements.
	 * 
	 * @param time The time at which to determine the actual speed.
	 * @return The speed that the robot is actually traveling at based on GPS measurements.
	 */
	public double getSpeed(long time) {
		if (time < getStartTime()) {
			Logger.logDbg("WARNING: timestamp prior to start time (" + time + " < " + getStartTime() + "), assuming speed is 0.");
			return 0;
		}
		
		if (time > getStopTime()) {
			Logger.logDbg("WARNING: timestamp after end time. (" + getStopTime() + " < " + time + "), assuming speed is 0");
			return 0;
		}
		
		// Find the index of the GPS measurements immediately before or after the desired time
		int beforeIndx = 0; 
		int afterIndx = 0;
		
		boolean afterIndxFound = false;
		
		for (int i=0; i < locations.size(); i++) {
			GPSLocationState currLocation = locations.get(i);
			if (currLocation.getTimestamp() <= time)
				beforeIndx = i;
			if (!afterIndxFound && currLocation.getTimestamp() >= time) {
				afterIndxFound = true;
				afterIndx = i;
			}
		}
		
		Logger.logDbg("timestamp = " + time + ", beforeIndx = " + beforeIndx + ", afterIndx = " + afterIndx);
		
		GPSLocationState preLoc, postLoc;
		
		if (beforeIndx == afterIndx) {
			// A GPS measurement arrived at precise time we want to estimate the robot's speed.
			// Use this GPS measurement and the one just before it if it exists.  Otherwise,
			// use the one just after it if it exists.
			if (beforeIndx > 0) {
				preLoc = locations.get(beforeIndx - 1);
				postLoc = locations.get(beforeIndx);	
			} else if (beforeIndx + 1 < locations.size()){
				preLoc = locations.get(beforeIndx);
				postLoc = locations.get(beforeIndx + 1);
			} else {
				Logger.logErr("Unable to get two GPS measurements to estimate speed.");
				System.exit(1);
				preLoc = postLoc = null; // dummy code to prevent compiler from complaining 
			}
		} else {
			preLoc = locations.get(beforeIndx);
			postLoc = locations.get(afterIndx);
		}
		
		// Calculate the difference in time
		double deltaTime = (postLoc.getTimestamp() - preLoc.getTimestamp()) / 1000.0;
		
		// Calculate the distance between the two GPS measurements
		double dist = preLoc.getLocation().distanceTo(postLoc.getLocation());
		
		double speed = dist / deltaTime;
		
		Logger.logDbg("preLoc=" + preLoc + ", postLoc=" + postLoc + ", dist=" + dist 
				+ ", deltaTime=" + deltaTime + ", speed=" + speed);
		
		return speed;
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
		if (startTime < getStartTime())
			startTime = getStartTime();
		
		long stopTime = timestamp;
		if (stopTime > getStopTime())
			stopTime = getStopTime();
		
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
	public Location getStartLocation() {
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
	
	/**
	 * Returns the initial heading of the robot when the experiment began.
	 * 
	 * @return the initial heading of the robot when the experiment began.
	 */
	public double getStartHeading() {
		return headings.get(0).getHeading();
	}
	
	/**
	 * Returns the final location of the robot after it finishes this experiment.
	 * 
	 * @return The final location of the robot.
	 */
	public double getEndHeading() {
		return headings.get(headings.size()-1).getHeading();
	}
	
	/**
	 * 
	 * @return An enumeration of the actual headings.
	 */
	public Enumeration<HeadingState> getHeadingEnum() {
		return headings.elements();
	}
	
	/**
	 * @return An enumeration of the actual locations.
	 */
	public Enumeration<GPSLocationState> getLocationsEnum() {
		return locations.elements();
	}
	
	/**
	 * Determines the ideal heading at the specified time.
	 * 
	 * @param timestamp The specified time.
	 * @return The ideal heading.
	 */
	public double getIdealHeading(long timestamp) {
		
		
		PathEdge relevantEdge = getRelevantPathEdge(timestamp);
		if (relevantEdge != null) {
			Location endLoc = relevantEdge.getEndLocation();
			Location robotLoc = getLocation(timestamp);
			return pharoslabut.navigate.Navigate.angle(robotLoc, endLoc);
		} else {	
			Logger.logErr("Could not find path edge covering time " + timestamp 
				+ " (" + (timestamp - getStartTime()) + ")");
			printPathEdges(true);
			System.exit(1);
			return Double.MIN_VALUE; // This is just dummy code to prevent the compiler from complaining.
		}
	}
	
	/**
	 * Returns the path edge that encompasses the specified time.
	 * 
	 * @param timestamp The time during which the robot is traveling.
	 * @return The edge that robot is on at the specified time.
	 */
	public PathEdge getRelevantPathEdge(long timestamp) {
		//log("getRelevantPathEdge: timestamp = " + timestamp);
		//printPathEdges(false);
		
		for (int i = 0;  i < pathEdges.size(); i++) {
			
			PathEdge currEdge = pathEdges.get(i);
			
			// Check whether the requested time is prior to start of the first edge.
			if (currEdge.getStartTime() > timestamp)
				return currEdge;
			
			if (currEdge.getStartTime() <= timestamp && currEdge.getEndTime() >= timestamp)
				return currEdge;
			
			// Check whether requested time is after end of the last edge.
			if (i == pathEdges.size() - 1 && currEdge.getEndTime() < timestamp)
				return currEdge;
		}
		
		Logger.logErr("Could not find relevant path edge at time " + timestamp
				+ " (" + (timestamp - getStartTime()) + ")");
		printPathEdges(true);
		System.exit(1);
		return null;
	}
	
	/**
	 * Returns the location and time that a robot becomes oriented as it traverses the specified
	 * edge.
	 * 
	 * @param pathEdge The path edge along which the robot is traveling.
	 * @param orientedThreshold The maximum heading error in radians before a robot is considered oriented.
	 * @return the location and time at which the robot becomes oriented.
	 */
	public LocationState getOrientedLocation(PathEdge pathEdge, double orientedThreshold) {
		
		LocationState result = null;
		
		long currTime = pathEdge.getStartTime();
		
		while (result == null && currTime < pathEdge.getEndTime()) {
			double actualHeading = getHeading(currTime);
			double idealHeading = getIdealHeading(currTime);
			
			if (Math.abs(actualHeading - idealHeading) < orientedThreshold) {
				Logger.log("Found oriented location!");
				result = new LocationState(currTime, getLocation(currTime));
			}
			
			currTime += 100; // check every 0.1s
		}
		
		if (result == null) {
			Logger.logErr("Unable to find oriented location along edge " + pathEdge.getSeqNo() + ", threshold=" + orientedThreshold);
			System.exit(1);
		}
		
		return result;
	}
	
	private void printPathEdges(boolean isError) {
		Logger.log("Start Time\tDelta Start Time\tEnd Time\tDelta End Time\tStart Location\tEnd Location");
		for (int i=0; i < pathEdges.size(); i++) {
			PathEdge e = pathEdges.get(i);
			String result = e.getStartTime() 
				+ "\t" + (e.getStartTime() - getStartTime())
				+ "\t" + e.getEndTime()
				+ "\t" + (e.getEndTime() - getStartTime())  
				+ "\t" + e.getStartLoc() 
				+ "\t" + e.getEndLocation();

			if (isError)
				Logger.logErr(result);
			else
				Logger.log(result);
		}
	}
	
	/**
	 * Finds the index within the motionCmds vector that contains the command that is
	 * active at the specified time.
	 * 
	 * @param timestamp The specified time.
	 * @return The index within motionCmds that contains the relevant motion command.
	 */
	private int getRelevantMotionCmdIndx(long timestamp) {
		
		for (int indx = 0; indx < motionCmds.size() - 1; indx++) {
			
			MotionCmd currCmd = motionCmds.get(indx);
			MotionCmd nxtCmd = motionCmds.get(indx+1);
			
			// Check if the relevant time is prior to the first motion command.
			if (indx == 0 && timestamp <  currCmd.time) {
				Logger.log("WARNING: timestamp before that of first motion command (" 
						+ timestamp + " < " + currCmd.time + "), assuming first command is relevant...");
				return 0;
			}
			
			// Check if the timestamp falls between the current and next motion commands
			else if (currCmd.time <= timestamp && timestamp < nxtCmd.time) {
				Logger.log("Found relevant motion command at index " + indx);
				return indx;
			}
			
			// Check if timestamp falls after the last command.
			else if (indx + 1 == motionCmds.size() - 1 && nxtCmd.time <= timestamp) {
				return indx+1;
			}
			
		}
		
		if (motionCmds.size() == 0) {
			Logger.logErr("No motion commands!");
			System.exit(1);
		}
		
		// The following is debugging code...
		String firstCmdTime;
		String lastCmdTime;
		if (motionCmds.size() > 0) {
			firstCmdTime = "Timestamp of first motion command: " + motionCmds.get(0).time;
			lastCmdTime = "Timestamp of last motion command: " + motionCmds.get(motionCmds.size()-1).time;
		} else {
			firstCmdTime = "No first motion command!";
			lastCmdTime = "No last motion command!";
		}
		
		Logger.logErr("Failed to find relevant motion task, timestamp = " + timestamp
				+ "\n\t" + firstCmdTime
				+ "\n\t" + lastCmdTime);
		
		new Exception().printStackTrace();
		System.exit(1); // fatal error
		return -1; // should never get here
	}
	/**
	 * Returns the heading command issued by the MotionArbiter to the robot.
	 * This does not interpolate the value because it is continuously defined.
	 * 
	 * @param timestamp The time of interest.
	 * @return The heading command.
	 */
	public double getHeadingCmd(long timestamp) {
		int indx = getRelevantMotionCmdIndx(timestamp);
		return motionCmds.get(indx).headingCmd;
	}
	
	/**
	 * Returns the heading of the robot at the specified time.
	 * Uses a linear interpolation of the robot's heading when necessary.
	 * 
	 * @param timestamp The time of interest. 
	 * @return The heading of the robot at the specified time.
	 */
	public double getHeading(long timestamp) {
		if (timestamp < getStartTime()) {
			Logger.logDbg("WARNING: timestamp prior to start time. (" + timestamp + " < " + getStartTime() + ")");
			return getStartHeading();
		}
		
		if (timestamp > getStopTime()) {
			Logger.logDbg("WARNING: timestamp after end time. (" + getStopTime() + " < " + timestamp + ")");
			return getEndHeading();
		}
		
		// Find the index of the headings immediately before or after the
		// desired time
		int beforeIndx = 0; 
		int afterIndx = 0;
		
		boolean afterIndxFound = false;
		
		for (int i=0; i < headings.size(); i++) {
			HeadingState currHeading = headings.get(i);
			if (currHeading.getTimestamp() <= timestamp)
				beforeIndx = i;
			if (!afterIndxFound && currHeading.getTimestamp() >= timestamp) {
				afterIndxFound = true;
				afterIndx = i;
			}
		}
		
		Logger.logDbg("timestamp = " + timestamp + ", beforeIndx = " + beforeIndx + ", afterIndx = " + afterIndx);
		
		if (beforeIndx == afterIndx)
			return headings.get(beforeIndx).getHeading();
		else {
			HeadingState bHeading = headings.get(beforeIndx);
			HeadingState aHeading = headings.get(afterIndx);
			
			return getInterpolatedHeading(bHeading, aHeading, timestamp);
		}
	}
	
	/**
	 * Interpolates the heading of the robot at a particular point in time.
	 * 
	 * @param bHeading The heading prior to the time of interest.
	 * @param aHeading The heading after the time of interest.
	 * @param timestamp The time at which the interpolated heading 
	 * should be calculated.
	 * @param flogger The logger for recording debug statements.
	 * @return The interpolated heading.
	 */
	public static double getInterpolatedHeading(HeadingState bHeading, 
			HeadingState aHeading, long timestamp) 
	{
		Location preHeading = new Location(bHeading.getHeading(), bHeading.getTimestamp());
		Location postHeading = new Location(aHeading.getHeading(), aHeading.getTimestamp());
		Line headingLine = new Line(preHeading, postHeading);
		return headingLine.getLatitude(timestamp);
	}
	
	/**
	 * Returns the location of the robot at the specified time.
	 * Uses a linear interpolation of the robot's location when necessary.
	 * 
	 * @param timestamp The time of interest. 
	 * @return The location of the robot at the specified time.
	 */
	public Location getLocation(long timestamp) {
		if (timestamp < getStartTime()) {
			Logger.logDbg("WARNING: timestamp prior to start time. (" + timestamp + " < " + getStartTime() + ")");
			return getStartLocation();
		}
		
		if (timestamp > getStopTime()) {
			Logger.logDbg("WARNING: timestamp after end time. (" + getStopTime() + " < " + timestamp + ")");
			return getEndLocation();
		}
		
		// Find the index of the locations immediately before or after the
		// desired time
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
		
		Logger.logDbg("timestamp = " + timestamp + ", beforeIndx = " + beforeIndx + ", afterIndx = " + afterIndx);
		
		if (beforeIndx == afterIndx)
			return new Location(locations.get(beforeIndx).getLoc());
		else {
			GPSLocationState bLoc = locations.get(beforeIndx);
			GPSLocationState aLoc = locations.get(afterIndx);
			
			return getInterpolatedLoc(
					bLoc.getLocation().latitude(), bLoc.getLocation().longitude(), bLoc.getTimestamp(),
					aLoc.getLocation().latitude(), aLoc.getLocation().longitude(), aLoc.getTimestamp(),
					timestamp);
		}
	
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
//	 * @param flogger The file logger for recording debug statements.
	 * @return The location of the robot at the specified timestamp.
	 */
	public static Location getInterpolatedLoc(double lat1, double lon1, long time1,
		double lat2, double lon2, long time2, long timestamp) 
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
		
		Logger.logDbg("Interpolated location:\n"
			+ "\tBefore Location @ " + time1 + ": (" + lat1 + ", " + lon1 + ")\n"
			+ "\tAfter Location @ " + time2 + ": (" +  lat2 + ", " + lon2 + ")\n"
			+ "\tInterpolated Location @ " + timestamp + ": " + result);
		
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
	
	/**
	 * 
	 * @return The maximum latitude coordinate of the robot.
	 */
	public double getMaxLat() {
		if (locations.size() > 0) {
			double result = locations.get(0).getLocation().latitude();
			for (int i=1; i < locations.size(); i++) {
				double currLat = locations.get(i).getLocation().latitude(); 
				if (currLat > result)
					result = currLat;
			}
			return result;
		} else {
			Logger.logErr("No known locations!");
			System.exit(1);
			return -1; // will never get here
		}
	}
	
	/**
	 * 
	 * @return The minimum latitude coordinate of the robot.
	 */
	public double getMinLat() {
		if (locations.size() > 0) {
			double result = locations.get(0).getLocation().latitude();
			for (int i=1; i < locations.size(); i++) {
				double currLat = locations.get(i).getLocation().latitude(); 
				if (currLat < result)
					result = currLat;
			}
			return result;
		} else {
			Logger.logErr("No known locations!");
			System.exit(1);
			return -1; // will never get here
		}
	}
	
	/**
	 * 
	 * @return The maximum longitude coordinate of the robot.
	 */
	public double getMaxLon() {
		if (locations.size() > 0) {
			double result = locations.get(0).getLocation().longitude();
			for (int i=1; i < locations.size(); i++) {
				double currLon = locations.get(i).getLocation().longitude(); 
				if (currLon > result)
					result = currLon;
			}
			return result;
		} else {
			Logger.logErr("No known locations!");
			System.exit(1);
			return -1; // will never get here
		}
	}
	
	/**
	 * 
	 * @return The minimum longitude coordinate of the robot.
	 */
	public double getMinLon() {
		if (locations.size() > 0) {
			double result = locations.get(0).getLocation().longitude();
			for (int i=1; i < locations.size(); i++) {
				double currLon = locations.get(i).getLocation().longitude(); 
				if (currLon < result)
					result = currLon;
			}
			return result;
		} else {
			Logger.logErr("No known locations!");
			System.exit(1);
			return -1; // will never get here
		}
	}
	
	/**
	 * Holds a motion command and when it was issued.
	 */
	private class MotionCmd {
		long time;
		double speedCmd, headingCmd;
		
		public MotionCmd(long time, double speedCmd, double headingCmd) {
			this.time = time;
			this.speedCmd = speedCmd;
			this.headingCmd = headingCmd;
		}
		
		/**
		 * Recalibrates the time based on the GPS timestamps.
		 * 
		 * @param calibrator The time calibrator.
		 */
		public void calibrateTime(TimeCalibrator calibrator) {
			time = calibrator.getCalibratedTime(time);
		}
		
//		public String getTableHeader() {
//			return "Time\tSpeed Command (m/s)\tHeading Command (radians)";
//		}
		
		public String toString() {
			return time + "\t" + (time - getStartTime())/1000.0 + "\t" + speedCmd + "\t" + headingCmd;
		}
	}
	
//	private void logErr(String msg) {
//		// Add the name of the calling method to the beginning of the message.
//		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
//		String callingMethodName = stackTraceElements[2].getMethodName();
//		
//		String result = getClass().getName() + ": " + callingMethodName + ": ERROR: " + msg;
//		System.err.println(result);
//		System.err.flush();
//		if (flogger != null)
//			flogger.log(result);
//	}
//	
//	private static void log(String msg, FileLogger flogger) {
//		String result = RobotExpData.class.getName() + ": " + msg; 
//		if (System.getProperty ("PharosMiddleware.debug") != null)
//			System.out.println(result);
//		if (flogger != null)
//			flogger.log(result);
//	}
//	
//	private static void logDbg(String msg, FileLogger flogger) {
//		if (ENABLE_DEBUG_STATEMENTS)
//			log(msg, flogger);
//	}
//	
//	/**
//	 * This only logs statements when ENABLE_DEBUG_STATEMENTS is true.
//	 * 
//	 * @param msg the message to log.
//	 */
//	private void logDbg(String msg) {
//		if (ENABLE_DEBUG_STATEMENTS)
//			log(msg);
//	}
//	
//	/**
//	 * Logs debug statements.
//	 * 
//	 * @param msg the message to log.
//	 */
//	private void log(String msg) {
//		String result = getClass().getName() + ": " + msg;
//		if (System.getProperty ("PharosMiddleware.debug") != null)
//			System.out.println(result);
//		if (flogger != null)
//			flogger.log(result);
//	}
	
	/**
	 * Calculates the time since the robot start time.
	 * 
	 * @param time the absolute time stamp
	 * @return The relative time that has passed since the robot start time.
	 */
	private long getRelativeTime(long time) {
		return (time - getStartTime())/1000;
	}
	
	/**
	 * Prints a table listing the arrival times of the waypoints.
	 * 
	 * @param flogger The file logger to which to print the table.
	 */
	public void printWayPointArrivalTable(FileLogger flogger) {
		flogger.log("Waypoint\tLatitude\tLongitude\tArrival Time (ms)\tArrival Delta Time (ms)\tArrival Delta Time (s)\tY Coordinate");
		for (int i=0; i < pathEdges.size(); i++) {
			PathEdge currEdge = pathEdges.get(i);
			Location waypoint = currEdge.getEndLocation();
			long endTime = currEdge.getEndTime();
			flogger.log(i + "\t" + waypoint.latitude() + "\t" + waypoint.longitude() 
					+ "\t" + endTime + "\t" + (endTime - getStartTime()) 
					+ "\t" + (endTime - getStartTime())/1000 + "\t0");
		}
	}
	
	public String toString() {
		String result = 
			"File Name: " + getFileName()
			+ "\nRobot Start Time: " + getStartTime()
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
	
	/**
	 * A small test of this class.
	 * 
	 * @param args The command line arguments.
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			print("Usage: " + RobotExpData.class.getName() + " [path to robot experiment data]");
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
		
//		print("Robot start time: " + red.getStartTime());
//		
//		boolean testGetLocation = false;
//		if (testGetLocation) {
//			// Check whether the getLocation method is OK
//			print("Evaluating RobotExpData.getLocation(long timestamp)...");
//			print("\tExperiment start time: " + red.getStartTime());
//			String testGetLocFileName = "RobotExpData-TestGetLocation.txt";
//			FileLogger flogger = new FileLogger(testGetLocFileName, false);
//
//			//int pathEdgeToCheck = 15;
//
//			flogger.log("type,latitude,longitude,name,color");
//			long currTime = red.getStartTime();
//			//long currTime = red.getPathEdge(pathEdgeToCheck).getStartTime();
//			boolean firstLoc = true;
//			while (currTime < red.getStopTime()) {
//				//while (currTime < red.getPathEdge(pathEdgeToCheck).getEndTime()) {
//				Location currLoc = red.getLocation(currTime);
//				String line = "T," + currLoc.latitude() + "," + currLoc.longitude();
//				if (firstLoc) {
//					line += ",getLocation,blue";
//					firstLoc = false;
//				}
//				flogger.log(line);
//				currTime += 1000;
//			}
//
//			flogger.log("type,latitude,longitude");
//			locs = red.getGPSHistory().elements();
//			//locs = red.getPathEdge(pathEdgeToCheck).getLocationsEnum();
//			while (locs.hasMoreElements()) {
//				GPSLocationState currLoc = locs.nextElement();
//				flogger.log("W," + currLoc.getLocation().latitude() + "," + currLoc.getLocation().longitude());
//			}
//
//			print("\tEvaluation script saved to " + testGetLocFileName + ", upload it to GPSVisualizer to visualize...");
//		}
	}
}
