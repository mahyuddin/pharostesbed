package pharoslabut.demo.mrpatrol.logAnalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import pharoslabut.behavior.BehGotoGPSCoordState;
import pharoslabut.behavior.BehaviorState;
import pharoslabut.logger.Logger;
import pharoslabut.logger.analyzer.RobotExpData;
import pharoslabut.logger.analyzer.TimeCalibrator;
import pharoslabut.navigate.Location;

/**
 * Reads the log files of Multi-Robot patrol experiments and extracts
 * relevant data from it.
 * 
 * @author Chien-Liang Fok
 *
 */
public class RobotMRPatrolExpData extends RobotExpData {

	private Vector<BehGotoGPSCoordState> behaviors = new Vector<BehGotoGPSCoordState>();
	
	public RobotMRPatrolExpData(String fileName) {
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
	 * Reads and organizes the data contained in the robot's experiment log file.
	 * 
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	@Override
	protected void readFile() throws NumberFormatException, IOException {
		super.readFile();
		File file = new File(fileName);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
		
		while ((line = br.readLine()) != null) {
			
			// Get the destination location of each BehGotoGPSCoord behavior
			if (line.contains("pharoslabut.behavior.BehGotoGPSCoord:") && line.contains("Constructor behavior;")) {
				
				String keyStr = "Constructor behavior;";
				String behaviorCreationLine = line.substring(line.indexOf(keyStr) + keyStr.length());
				
				String[] tokens = behaviorCreationLine.split("[=\\sl]");
				
				double latitude = Double.valueOf(tokens[5]);
				double longitude = Double.valueOf(tokens[9]);
				Location dest = new Location(latitude, longitude);
				
				behaviors.add(new BehGotoGPSCoordState(dest));
			}
			
			// Get the start time of each behavior
			else if (line.contains("pharoslabut.behavior.management.Manager: run:") && line.contains("running behavior")) {
				long timestamp = Long.valueOf(line.substring(1,line.indexOf(']')));
				
				//String keyStr = "pharoslabut.behavior.management.Manager: run: running behavior";
				String behaviorName = BehGotoGPSCoordState.getBehaviorName();
				
				int behaviorNumber = Integer.valueOf(line.substring(line.indexOf(behaviorName) + behaviorName.length()));
				
				if (behaviorNumber == 60) {
					System.out.println("start: Behavior number is 60");
				}
				
				BehaviorState behavior = behaviors.get(behaviorNumber);
				behavior.setBehaviorStartTime(timestamp);
			}
			
			// Get the stop time of each behavior
			else if (line.contains("pharoslabut.behavior.management.Manager: run:") && line.contains("End behavior")) {
				long timestamp = Long.valueOf(line.substring(1,line.indexOf(']')));
				
				String keyStr = "End behavior";
				String endBehaviorLine = line.substring(line.indexOf(keyStr) + keyStr.length());
				String[] tokens = endBehaviorLine.split("[\\s,]");
				
				int behaviorNumber = Integer.valueOf(tokens[2]);
				
				if (behaviorNumber == 60) {
					System.out.println("start: Behavior number is 60");
				}
				
				BehaviorState behavior = behaviors.get(behaviorNumber);
				behavior.setBehaviorStopTime(timestamp);
				
			}
			
		}
		
		// Remove the last behavior because it's the one that makes the robot go home
		if (behaviors.size() > 0)
			behaviors.remove(behaviors.size()-1);
		else
			Logger.logErr("Could not remove last \"go to home\" behavior!");
		
		// Set the behavior numbers
		for (int i=0; i < behaviors.size(); i++) {
			behaviors.get(i).setBehaviorNumber(i);
		}
		
	}
	
	/**
	 * 
	 * @return The waypoints in the experiment.  The assumption is that in a single loop, a robot does not
	 * visit the same waypoint twice.  Thus, by looking through the list of BehGotoGPSCoord behaviors, as soon
	 * as we see that we re-visit a waypoint, we know that we've visited all of the waypoints.
	 */
	public Vector<Location> getWayPoints() {
		Vector<Location> result = new Vector<Location>();
		for (int i=0; i < behaviors.size(); i++) {
			BehGotoGPSCoordState currBehavior = behaviors.get(i);
			Location dest = currBehavior.getDest();
			if (!result.contains(dest))
				result.add(dest);
		}
		return result;
	}
	
	/**
	 * Extracts the times a specific waypoint is visited.
	 * 
	 * @param waypoint The waypoint.
	 * @return the times when the waypoint was visited.
	 */
	public Vector<Long> getVisitationTimes(Location waypoint) {
		Vector<Long> result = new Vector<Long>();
		
		for (int i=0; i < behaviors.size(); i++) {
			BehGotoGPSCoordState currBehavior = behaviors.get(i);
			if (currBehavior.getDest().equals(waypoint)) {
				result.add(currBehavior.getStopTime());
			}
		}
		
		return result;
	}
	
	/**
	 * Compares the GPS timestamps with the log timestamps to determine the 
	 * offset needed to calibrate the log timestamps to match the GPS timestamps.
	 * The GPS timestamps are assumed to be accurate to within 1 second.
	 * 
	 * @return The time calibrator.  This can be used by subclasses.
	 */
	protected TimeCalibrator calibrateTime() {
		TimeCalibrator calibrator = super.calibrateTime();
		
		for (int i=0; i < behaviors.size(); i++) {
			behaviors.get(i).calibrateTime(calibrator);
		}
		
		return calibrator;
	}
	
	public String toString() {
		String result = super.toString();
		result += "Behaviors:\n";
		
		Enumeration<BehGotoGPSCoordState> e = behaviors.elements();
		while (e.hasMoreElements()) {
			result += "\t" + e.nextElement() + "\n";
		}
		
		return result;
	}
	
	public static void main(String[] args) {
		String fileName = "BehaveMission29-EXP1-LONESTAR-MRPatrol-20110830070525.log";
		RobotMRPatrolExpData robotExpData = new RobotMRPatrolExpData(fileName);
		System.out.println(robotExpData.toString());
		
		System.out.println("Waypoints: ");
		Vector<Location> waypoints = robotExpData.getWayPoints();
		for (int i=0; i < waypoints.size(); i++) {
			Location l = waypoints.get(i);
			System.out.println(i + "\t" + l);
		}
	}

}
