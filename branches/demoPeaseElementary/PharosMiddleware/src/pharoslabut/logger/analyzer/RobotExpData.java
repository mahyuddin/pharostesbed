package pharoslabut.logger.analyzer;

import java.io.*;
import java.util.*;

import pharoslabut.navigate.Location;
import playerclient.structures.gps.PlayerGpsData;

/**
 * Encapsulates the data recorded by a robot during an experiment.
 * 
 * @author Chien-Liang Fok
 */
public class RobotExpData {
	/**
	 * Details of a single edge in the path the robot traveled.
	 */
	private Vector<PathEdge> pathEdges = new Vector<PathEdge>();
	
	/**
	 *  The start time of the experiment. It is when the PharosServer
	 *  receives the start experiment message from the PharosClient.
	 */
	private long expStartTime;
	
	/**
	 * The name of the experiment log file.
	 */
	private String fileName;
	
	/**
	 * The offset in ms between the local timestamps and the GPS timestamps.
	 * To calibrate a local time stamp:
	 * 
	 * True time = Local timestamp - calibratedTimeOffset.
	 */
	private double calibratedTimeOffset;
	
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
	 * Reads in and organizes the data in the log file.
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
				
				if (currEdge != null) {
					currEdge.addLocation(new GPSLocationState(timeStamp, currLoc));
					if (!currEdge.hasStartLoc())
						currEdge.setStartLoc(new Location(currLoc));
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
						Integer.valueOf(tokens[5]), // sndrID
						Integer.valueOf(tokens[6]), // rcvrID
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
	 * Compares the GPS timestamps with the local time stamps to determine the 
	 * offset needed to calibrate the local time stamps.  This assumes that the 
	 * GPS timestamps are accurate to within 1 second.
	 */
	private void calibrateTime() {
		// First gather all of the location data...
		Vector<GPSLocationState> locs = new Vector<GPSLocationState>();
		for (int i=0; i < pathEdges.size(); i++) {
			locs.addAll(pathEdges.get(i).getLocations());
		}
		
//		pharoslabut.logger.FileLogger flogger = new pharoslabut.logger.FileLogger("CalibrateTime", false);
		
		double diffSum = 0; // the sum of all the diffs 
		
		// Calculate the difference between the local time stamp
		// and the GPS time stamp
		for (int i=0; i < locs.size(); i++) {
			
			// The following is the difference, measured in milliseconds, 
			// between the current time and midnight, January 1, 1970 UTC.
			long localTimestamp = locs.get(i).getTimeStamp();
			
			// This is the GPS Timestamp.  The code that generates it is available here:
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
		calibratedTimeOffset = diffSum / locs.size() * 1000;
		
//		String str = "CalibratedTimeOffset: " + calibratedTimeOffset;
//		System.out.println(str);
//		flogger.log(str);
		
	}
	
	public long getCalibratedTimeStamp(long timestamp) {
		return Math.round(timestamp - calibratedTimeOffset);
	}
	
	public Vector<TelosBRxRecord> getTelosBRxHist() {
		return telosBRxHist;
	}
	
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
	
	/**
	 * Returns a specific edge within this experiment.
	 * 
	 * @param indx The index of the path edge, must be between zero and numEdges().
	 * @return The edge within the experiment.
	 */
	public PathEdge getEdge(int indx) {
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
	 * Goes through each of the path edges and gathers all of the GPS data
	 * into a single vector.  Returns this vector.
	 * 
	 * @return The vector containing the entire path history of the robot.
	 */
	public Vector<GPSLocationState> getGPSHistory() {
		Enumeration<PathEdge> e = pathEdges.elements();
		Vector<GPSLocationState> result = new Vector<GPSLocationState>();
		
		while (e.hasMoreElements()) {
			PathEdge pe = e.nextElement();
			for (int i=0; i < pe.getNumLocations(); i++) {
				result.add(pe.getLocation(i));
			}
		}
		
		return result;
	}
	
	/**
	 * Gets the experiment start time
	 * 
	 * @return the experiment start time
	 */
	public long expStartTime() {
		return expStartTime;
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
	 * Calculates the lateness of the robot arriving at the specified waypoint.
	 * 
	 * @param wayPoint  The destination waypoint.
	 * @return The lateness of the robot arriving at the waypoint.
	 */
	public double getLatenessTo(int wayPoint) {
		return pathEdges.get(wayPoint).getLateness();
	}
	
	public static void main(String[] args) {
		new RobotExpData(args[0]);
	}
}
