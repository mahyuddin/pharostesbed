package pharoslabut.logger.analyzer;

import java.util.*;
import java.io.*;

/**
 * Organizes the data recorded during an experiment.  An experiment
 * consists of one or more robots following motion scripts.
 * 
 * @author Chien-Liang Fok
 */
public class ExpData {
	
	private Vector<RobotExpData> robots = new Vector<RobotExpData>();
	
	/**
	 * The constructor.
	 * 
	 * @param expDir The directory containing the experiment log files.
	 */
	public ExpData(String expDir) {
	
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
		        robots.add(new RobotExpData(expDir + "/" + logFiles[i]));
		    }
		}
	}
	
	/**
	 * Returns the experiment start time.  This is the time when the last
	 * robot started.
	 * 
	 * @return The experiment start time.
	 */
	public long getExpStartTime() {
		long result = 0;
		Enumeration<RobotExpData> e = robots.elements();
		while (e.hasMoreElements()) {
			long currStartTime = e.nextElement().getRobotStartTime();
			if (currStartTime > result)
				result = currStartTime;
		}
		return result;
	}
	
	/**
	 * Returns the experiment stop time.  This is the earliest stop time
	 * among all the robots in the experiment.
	 * 
	 * @return The experiment stop time.
	 */
	public long getExpStopTime() {
		long result = -1;
		Enumeration<RobotExpData> e = robots.elements();
		while (e.hasMoreElements()) {
			long currStopTime = e.nextElement().getRobotStopTime();
			if (result == -1 || currStopTime < result)
				result = currStopTime;
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
		log("ERROR: Unable to find robot with ID " + robotID);
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
	
	private void log(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println("ExpData: " + msg);
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
					+ robotData.getRobotStartTime() + "\t"
					+ (robotData.getRobotStartTime() - baseline.getRobotStartTime()) + "\t"
					+ robotData.getRobotStopTime() + "\t"
					+ (robotData.getRobotStopTime() - baseline.getRobotStopTime()) + "\t"
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
