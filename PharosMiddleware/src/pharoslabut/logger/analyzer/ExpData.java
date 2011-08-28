package pharoslabut.logger.analyzer;

import java.util.*;
import java.io.*;

import pharoslabut.logger.Logger;

/**
 * Organizes the data recorded during an experiment.  An experiment
 * consists of one or more robots following motion scripts.
 * 
 * @author Chien-Liang Fok
 */
public class ExpData {
	
	private String expName = null;
	private Vector<RobotExpData> robots = new Vector<RobotExpData>();
	
	/**
	 * The constructor.
	 * 
	 * @param expDir The directory containing the experiment log files.
	 */
	public ExpData(String expDir) {
	
		// Parse out the name of the experiment.  The name of the experiment should follow
		// the following format: "M##-Exp##".
		String absExpDir = new File(expDir).getAbsolutePath();
		String[] tokens = absExpDir.split("/");
		for (int i=0; i < tokens.length; i++) {
			//log("Analyzing token " + tokens[i]);
			if (tokens[i].matches("M\\d+-Exp\\d+")) {
				expName = tokens[i];
				Logger.logDbg("Found experiment name \"" + expName + "\", expDir = " + expDir);
			}
		}
		
		if (expName == null) {
			Logger.logErr("Unable to determine experiment name, expDir = " + expDir);
			System.exit(1);
		}
		
		// Get all of the robot logs from the experiment.
		FilenameFilter filter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return !name.startsWith(".") && name.contains("-Pharos_") && name.contains(".log");
		    }
		};
		
		File dir = new File(expDir);

		String[] logFiles = dir.list(filter);
		if (logFiles == null) {
		    System.err.println("No files found.");
		    System.exit(1);
		} else {
		    for (int i=0; i<logFiles.length; i++) {
		    	String robotFileName = expDir + "/" + logFiles[i];
		    	Logger.logDbg("Reading robot log " + robotFileName);
		        robots.add(new RobotExpData(robotFileName));
		    }
		}
	}
	
	/**
	 * @return the name of the experiment.
	 */
	public String getExpName() {
//		return expName;
		if (getNumRobots() > 0) {
			RobotExpData robotData = getRobot(0);
			return robotData.getExpName();
		} else {
			Logger.logErr("Could not determine experiment name because no robots present.");
			System.exit(1);// should never get there
			return null;
		}
	}

	/**
	 * @return the name of the mission.
	 */
	public String getMissionName() {
		if (getNumRobots() > 0) {
			RobotExpData robotData = getRobot(0);
			return robotData.getMissionName();
		} else {
			Logger.logErr("Could not determine mission name because no robots present.");
			System.exit(1);// should never get there
			return null;
		}
	}
	
	/**
	 * Returns the experiment start time.  This is the time when the first
	 * robot that ran the experiment started.
	 * 
	 * @return The experiment start time.
	 */
	public long getExpStartTime() {
		long result = -1;
		Enumeration<RobotExpData> e = robots.elements();
		while (e.hasMoreElements()) {
			RobotExpData currRobot = e.nextElement();
			if (currRobot.ranExperiment()) {
				long currStartTime = currRobot.getStartTime();
				if (result == -1 || currStartTime < result)
					result = currStartTime;
			}
		}
		return result;
	}
	
	/**
	 * Returns the experiment stop time.  This is the latest stop time
	 * among all the robots in the experiment that ran the experiment.
	 * 
	 * @return The experiment stop time.
	 */
	public long getExpStopTime() {
		long result = -1;
		Enumeration<RobotExpData> e = robots.elements();
		while (e.hasMoreElements()) {
			RobotExpData currRobot = e.nextElement();
			if (currRobot.ranExperiment()) {
				long currStopTime = currRobot.getStopTime();
				if (result == -1 || currStopTime > result)
					result = currStopTime;
			}
		}
		return result;
	}
	
	/**
	 * Returns all of the robots that received the specified Telosb transmission.
	 * 
	 * @param robotID The transmitting robot's ID (last octal of IP address)
	 * @param seqno The sequence number within the transmission.
	 * @return
	 */
	public Vector<RobotExpData> getTelosBRxNodes(int robotID, int seqno) {
		Vector<RobotExpData> result = new Vector<RobotExpData>();
		for (int i=0; i < robots.size(); i++) {
			RobotExpData currRobot = robots.get(i);
			
			if (currRobot.rcvdTelosBTx(robotID, seqno)) {
				result.add(currRobot);
			}
		}
		return result;
	}
	
	/**
	 * The number of robots in the experiment.
	 * 
	 * @return The total number of robots.
	 */
	public int numRobots() {
		return robots.size();
	}
	
	/**
	 * Returns the robot with the specified ID.  A robot's ID is the last
	 * octal of its wireless IP address.
	 * 
	 * @param robotID The last octal of the robot's IP address.
	 * @return The experiment data for the robot with the specified IP address.
	 */
	public RobotExpData getRobotByID(int robotID) {
		for (int i=0; i < robots.size(); i++) {
			if (robots.get(i).getRobotID() == robotID) 
				return robots.get(i);
		}
		Logger.logErr("Unable to find robot with ID " + robotID);
		return null;
	}
	
	/**
	 * Returns the data associated with a specific robot.
	 * 
	 * @param indx Which robot data to get, between 0 and numRobots().
	 * @return The robot data.
	 */
	public RobotExpData getRobot(int indx) {
		return robots.get(indx);
	}
	
	/**
	 * Returns an enumeration of the robots in this experiment.
	 * 
	 * @return an enumeration of the robots in this experiment.
	 */
	public Enumeration<RobotExpData> getRobotEnum() {
		return robots.elements();
	}
	
	/**
	 * 
	 * @return The number of robots in the experiment.
	 */
	public int getNumRobots() {
		return robots.size();
	}
	
//	private void logErr(String msg) {
//		System.err.println("ExpData: " + msg);
//	}
//	
//	private void log(String msg) {
//		if (System.getProperty ("PharosMiddleware.debug") != null)
//			System.out.println("ExpData: " + msg);
//	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Exp name: " + getExpName() + "\n");
		sb.append("Exp start: " + getExpStartTime() + ", Exp stop: " + getExpStopTime() + " (Total time: " + (getExpStopTime() - getExpStartTime())/1000 + "s)\n");
		sb.append("Number of robots: " + numRobots() + "\n");
		
		for (int i=0; i < numRobots(); i++) {
			RobotExpData currRobot = getRobot(i);
			sb.append("Robot " + (i+1) + " started " + (currRobot.getStartTime() - getExpStartTime()) + "ms after experiment start time\n");
			sb.append("\t" + currRobot.toString().replaceAll("\n", "\n\t") + "\n");
		}
		
		return sb.toString();
	}
	
	private static void print(String msg) {
		System.out.println(msg);
	}
	
	/**
	 * Performs basic tests on ExpData.  First it reads in all of the experiment data.
	 * This will automatically calibrate the log file's timestamps to match the GPS timestamps, 
	 * which has one second accuracy.  It then creates a table showing the calibrated start 
	 * times of each robot.
	 * 
	 * @param args The command line arguments, which is the directory containing experiment data.
	 */
	public static final void main(String[] args) {
		if (args.length < 1) {
			print("Usage: pharoslabut.logger.analyzer.Expdata [path to exp data]");
			System.exit(0);
		}
		
		// The following tests the timestamp calibration.
		ExpData expData = new ExpData(args[0]);
		
		if (expData.numRobots() == 0) {
			print("No robots in experiment.");
			System.exit(0);
		}
		
		RobotExpData baseline = expData.getRobot(0); // for comparison to other robots.
		
		// See if the start times match...
		print("Index\tRobotName\tStart Time\tStart Time Delta\tEnd Time\tEnd Time Delta\tFirst Edge Start Time\tFirst Edge Start Time Delta");
		for (int i=0; i < expData.numRobots(); i++) {
			RobotExpData robotData = expData.getRobot(i);
			print(i + "\t" + robotData.getRobotName() + "\t" 
					+ robotData.getStartTime() + "\t"
					+ (robotData.getStartTime() - baseline.getStartTime()) + "\t"
					+ robotData.getStopTime() + "\t"
					+ (robotData.getStopTime() - baseline.getStopTime()) + "\t"
					+ robotData.getPathEdge(0).getStartTime() + "\t"
					+ (robotData.getPathEdge(0).getStartTime() - baseline.getPathEdge(0).getStartTime()));
		}
		
		// For each robot, see how the log timestamp of the GPS data matches the GPS timestamps.
		boolean timestampsCalibrated = true;
		Enumeration<RobotExpData> e = expData.getRobotEnum();
		while (e.hasMoreElements()) {
			RobotExpData red = e.nextElement();
			Enumeration<GPSLocationState> loce = red.getGPSHistory().elements();
			while (loce.hasMoreElements()) {
				GPSLocationState currLoc = loce.nextElement();
				long timeDiff = currLoc.getTimestamp() - currLoc.getLoc().getTime_sec() * 1000L;
				if (Math.abs(timeDiff) > 2000) { 
					print("ERROR: Time calibration did not work in " + red.getFileName() + " (timeDiff = " + timeDiff + "): " + currLoc);
					timestampsCalibrated = false;
				}
			}
		}
		if (timestampsCalibrated) {
			print("Timestamps of GPS meausrements correctly calibrated.");
		}
		
	}
}
