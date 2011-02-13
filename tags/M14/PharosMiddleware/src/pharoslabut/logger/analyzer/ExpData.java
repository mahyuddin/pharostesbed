package pharoslabut.logger.analyzer;

import java.util.*;

/**
 * Contains the data recorded from a single experiment.  An experiment
 * consists of a robot following a motion script.
 * 
 * @author Chien-Liang Fok
 */
public class ExpData {
	
	/**
	 * Details of a single edge in the path the robot traveled.
	 */
	private Vector<PathEdge> pathHistory = new Vector<PathEdge>();
	
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
	public ExpData(String fileName) {
		this.fileName = fileName;
		
	}
	
	/**
	 * Sets the start time of the experiment.
	 * 
	 * @param expStartTime the start time of the experiment.
	 */
	public void setExpStartTime(long expStartTime) {
		this.expStartTime = expStartTime;
	}
	
	/**
	 * Adds a path edge.
	 * 
	 * @param pe The path edge to be added.
	 */
	public void addPathEdge(PathEdge pe) {
		pathHistory.add(pe);
	}
	
	/**
	 * Gets the path edge history.
	 * 
	 * @return the path edge history.
	 */
	public Vector<PathEdge> getPathHistory() {
		return pathHistory;
	}
	
	/**
	 * Returns a specific edge within this experiment.
	 * 
	 * @param indx The index of the path edge, must be between zero and numEdges().
	 * @return The edge within the experiment.
	 */
	public PathEdge getEdge(int indx) {
		return pathHistory.get(indx);
	}
	
	/**
	 * The number of edges in the experiment.  This is also the number of waypoints in 
	 * the motion script.
	 * 
	 * @return the number of edges in the experiment.
	 */
	public int numEdges() {
		return pathHistory.size();
	}
	
	/**
	 * Goes through each of the path edges and gathers all of the GPS data
	 * into a single vector.
	 * 
	 * @return The vector containing the entire path history of the robot.
	 */
	public Vector<GPSLocationState> getGPSHistory() {
		Enumeration<PathEdge> e = pathHistory.elements();
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
		return pathHistory.get(wayPoint).getLateness();
	}
}
