package pharoslabut.demo.mrpatrol.logAnalyzer;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.Vector;

import pharoslabut.logger.Logger;
//import pharoslabut.logger.analyzer.RobotExpData;
import pharoslabut.navigate.Location;

/**
 * This contains all of the data from a multi-robot patrol experiment.
 * 
 * @author Chien-Liang Fok
 *
 */
public class MRPatrolExpData {
	Vector<RobotMRPatrolExpData> robots = new Vector<RobotMRPatrolExpData>();
	String expDir;
	
	/**
	 * The constructor.
	 * 
	 * @param expDir The directory containing the experiment data.
	 */
	public MRPatrolExpData(String expDir) {
	
		this.expDir = new File(expDir).getAbsolutePath();
		
		// Get the robot logs from the experiment.
		FilenameFilter filter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return !name.startsWith(".") && name.contains("-MRPatrol") && name.contains(".log");
		    }
		};
		
		File dir = new File(expDir);

		String[] logFiles = dir.list(filter);
		if (logFiles == null) {
		    System.err.println("No files found.");
		    System.exit(1);
		} else {
		    for (int i=0; i < logFiles.length; i++) {
		    	String robotFileName = expDir + "/" + logFiles[i];
		    	Logger.logDbg("Reading robot log " + robotFileName);
		    	
		    	// Create a RobotMRPatrolExpData for each robot in experiment, and store it in the robots vector.
		        robots.add(new RobotMRPatrolExpData(robotFileName));	
		    }
		}
	}
	
	public String getExpDirName() {
		return expDir;
	}
	
	/**
	 * 
	 * @return The total number of coordination messages received throughout the experiment.
	 */
	public int getNumMsgRx() {
		int result = 0;
		for (int i=0; i < robots.size(); i++) {
			result += robots.get(i).getMsgRx().size();
		}
		return result;
	}
	
	/**
	 * 
	 * @return The total number of coordination messages transmitted throughout the experiment.
	 */
	public int getNumMsgTx() {
		int result = 0;
		for (int i=0; i < robots.size(); i++) {
			result += robots.get(i).getMsgTx().size();
		}
		return result;
	}
	
	/**
	 * 
	 * @return The waypoints in the experiment.  The assumption is that in a single loop, a robot does not
	 * visit the same waypoint twice.  Thus, by looking through the list of BehGotoGPSCoord behaviors, as soon
	 * as we see that we re-visit a waypoint, we know that we've visited all of the waypoints.
	 */
	public Vector<Location> getWayPoints() {
		if (robots.size() == 0) {
			Logger.logErr("Unable to get waypoints.  No robot data!");
			System.exit(1);
			return null; // never will get here
		} else
			return robots.get(0).getWayPoints();
	}
	
	/**
	 * Extracts the times a specific waypoint is visited.
	 * 
	 * @param waypoint The waypoint.
	 * @return the times when the waypoint was visited.
	 */
	public Vector<VisitationState> getVisitationTimes(Location waypoint) {
		Vector<VisitationState> visitationTimes = new Vector<VisitationState>();
		
		for (int i=0; i < robots.size(); i++) {
			RobotMRPatrolExpData currRobot = robots.get(i);
			Vector<VisitationState> robotVisitTimes = currRobot.getVisitationTimes(waypoint);
			visitationTimes.addAll(robotVisitTimes);
		}
		
		return visitationTimes;
	}
	
	/**
	 * @return The number of rounds in the experiment.
	 */
	public int getNumRounds() {
		if (robots.size() == 0) {
			Logger.logErr("Unable to get num rounds.  No robot data!");
			System.exit(1);
			return -1;
		} else 
			return robots.get(0).getNumRounds();
	}
	
	/**
	 * 
	 * @return The robots that participated in the experiment, ordered by starting waypoint.
	 */
	public Vector<RobotMRPatrolExpData> getRobots() {
		Collections.sort(robots);
		return robots;
	}
}