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
	 * The constructor.
	 * 
	 * @param fileName The name of the experiment log file.
	 */
	public RobotExpData(String fileName) {
		this.fileName = fileName;
		try {
			readFile();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
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
				
//				int i = 0;
//				while (tokens.hasMoreElements()) {
//					System.out.println(i++ + ": " + tokens.nextToken());
//				}
				
			}
			else if (line.contains("GPSDataBuffer: New GPS Data:")) {
				
				String[] tokens = line.split("[:=(), ]");
//				for (int i=0; i < tokens.length; i++) {
//					System.out.println(i + ": " + tokens[i]);
//				}
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
			else if (line.contains("Arrived at destination")) {
				String[] tokens = line.split("[:= ]");
				long timeStamp = Long.valueOf(tokens[0].substring(1, tokens[0].length()-1));
				currEdge.setEndTime(timeStamp);
				pathEdges.add(currEdge);
				currEdge = null;
			}
		}
	}
	
	/**
	 * Gets the path edge history.
	 * 
	 * @return the path edge history.
	 */
	public Vector<PathEdge> getpathEdges() {
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
	 * into a single vector.
	 * 
	 * @return The vector containing the entire path history of the robot.
	 */
	public Vector<GPSLocationState> getGPSHistory() {
		Enumeration<PathEdge> e = pathEdges.elements();
		Vector<GPSLocationState> result = new Vector<GPSLocationState>();
		
		while (e.hasMoreElements()) {
			PathEdge pe = e.nextElement();
			for (int i=0; i < pe.numLocations(); i++) {
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
}
