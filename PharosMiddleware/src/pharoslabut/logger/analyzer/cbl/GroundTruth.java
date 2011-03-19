package pharoslabut.logger.analyzer.cbl;

import java.util.*;

import pharoslabut.logger.analyzer.ExpData;
import pharoslabut.logger.analyzer.RobotExpData;
import pharoslabut.navigate.Location;

/**
 * Extracts the true locations of the robots at specified points in time
 * and formats them in a manner that can be used in the connectivity-based-localization code.
 * 
 * @author Chien-Liang Fok
 */
public class GroundTruth {
	
	private String expDir;
	private long timeStepSize;
	private int numTimeSteps;
	private Hashtable<Integer, RobotTrueLocData> robotLocs = new Hashtable<Integer, RobotTrueLocData>();
	
	/**
	 * Extracts the true locations of the robots and formats them in a manner that can
	 * be used in the connectivity-based-localization code.
	 * 
	 * @param expDir The directory containing the experiment data.
	 * @param timeStepSize The time step in milliseconds.
	 * @param numTimeSteps The number of time steps.
	 * @return The actual locations of the robots based on their GPS measurements.
	 */
	public GroundTruth(String expDir, long timeStepSize, int numTimeSteps) {	
		this.expDir = expDir;
		this.timeStepSize = timeStepSize;
		this.numTimeSteps = numTimeSteps;
			
		// Get the experiment data...
		ExpData expData = new ExpData(expDir);
		
		// For each robot in the experiment...
		Enumeration<RobotExpData> robotEnum = expData.getRobotEnum();
		while (robotEnum.hasMoreElements()) {
			RobotExpData currRobot = robotEnum.nextElement();
			
			// Add it to the result...
			RobotTrueLocData currRobotData = new RobotTrueLocData(currRobot.getRobotID());
			robotLocs.put(currRobot.getRobotID(), currRobotData);
			
			long startTime = currRobot.getRobotStartTime();
			
			// Get the actual locations of the robot at each time step and add it
			// to the result.
			for (long i = 0; i < numTimeSteps; i++) {
				long currTime = startTime + i * timeStepSize;
				Location currLoc = currRobot.getLocation(currTime);
				robotLocs.get(currRobot.getRobotID()).addLocation(currLoc);
			}
		}
	}
	
	public String getExpDir() {
		return expDir;
	}
	
	public long getTimeStepSize() {
		return timeStepSize;
	}
	
	public int getNumTimeSteps() {
		return numTimeSteps;
	}
	
	public int getNumRobots() {
		return robotLocs.size();
	}
	
	public byte[] getRobotType() {
		byte[] result = new byte[getNumRobots()];
		for (int i=0; i < getNumRobots(); i++) {
			result[i] = 1;  // mobile and unknown location
		}
		return result;
	}
	
	public int[] getRobotIDs() {
		int[] result = new int[getNumRobots()];
		int indx = 0;
		Enumeration<Integer> robotIDs = robotLocs.keys();
		while (robotIDs.hasMoreElements()) {
			result[indx++] = robotIDs.nextElement();
		}
		return result;
	}
	
	/**
	 * Returns the latitude locations of each robot in a 1-dimensional array.
	 * The array is organized as follows: The latitudes of the first robot
	 * are saved followed by the second, etc.
	 * 
	 * @return the latitude ground truths.
	 */
	public double[] getLatitudeTruth() {
		double[] result = new double[numTimeSteps * getNumRobots()];
		int[] robotIDs = getRobotIDs();
		
		// For each robot...
		for (int robotIndx = 0; robotIndx < getNumRobots(); robotIndx++) {
			RobotTrueLocData currRobot = robotLocs.get(robotIDs[robotIndx]);
			
			// Save its latitude positions in the result...
			for (int locIndx = 0; locIndx < numTimeSteps; locIndx++) {
				result[robotIndx * numTimeSteps + locIndx] = currRobot.getLocation(locIndx).latitude();
			}
		}
		
		return result;
	}
	
	public double[] getLongitudeTruth() {
		double[] result = new double[numTimeSteps * getNumRobots()];
		int[] robotIDs = getRobotIDs();
		
		// For each robot...
		for (int robotIndx = 0; robotIndx < getNumRobots(); robotIndx++) {
			RobotTrueLocData currRobot = robotLocs.get(robotIDs[robotIndx]);
			
			// Save its latitude positions in the result...
			for (int locIndx = 0; locIndx < numTimeSteps; locIndx++) {
				result[robotIndx * numTimeSteps + locIndx] = currRobot.getLocation(locIndx).longitude();
			}
		}
		
		return result;
	}
	
	public double[] getElevationTruth() {
		double[] result = new double[numTimeSteps * getNumRobots()];
		for (int i=0; i < result.length; i++) {
			result[i] = 0;
		}
		return result;
	}
	
	/**
	 * Maintains the true locations of a single robot.
	 */
	class RobotTrueLocData {
		private int robotID;
		private Vector<Location> locations = new Vector<Location>();
		
		public RobotTrueLocData(int robotID) {
			this.robotID = robotID;
		}
		
		public String getRobotName() {
			return pharoslabut.RobotIPAssignments.getRobotName(robotID);
		}
		
		public int getRobotID() {
			return robotID;
		}
		
		public void addLocation(Location loc) {
			locations.add(loc);
		}
		
		public int numLocations() {
			return locations.size();
		}
		
		public Location getLocation(int indx) {
			return locations.get(indx);
		}
	}
}
