package pharoslabut.logger.analyzer;

import pharoslabut.RobotIPAssignments;
import pharoslabut.navigate.Location;
import pharoslabut.logger.FileLogger;
//import java.util.*;

/**
 * Retrieves the RSSI values received by one robot from another robot.
 * The RSSI values are stored as inverse values (multiplied by -1).
 * 
 * @author Chien-Liang Fok
 */
public class GetRSSIvsTime {

	
	public GetRSSIvsTime(int robot1, int robot2, String expDir, long timeStepSize, FileLogger flogger) {
		ExpData expData = new ExpData(expDir);
		RobotExpData robot1Data = expData.getRobotByID(robot1);
		RobotExpData robot2Data = expData.getRobotByID(robot2);
		
		log("Timestamp (ms)\t Delta Time (s) \t -1*RSSI of " 
				+ RobotIPAssignments.getRobotName(robot2) 
				+ " on " + RobotIPAssignments.getRobotName(robot1) 
				+ "\t -1*RSSI of " 
				+ RobotIPAssignments.getRobotName(robot1) 
				+ " on " + RobotIPAssignments.getRobotName(robot2) 
				+ " \t Dist", flogger);
		
		for (long timestamp = expData.getExpStartTime(); timestamp < expData.getExpStopTime(); timestamp += timeStepSize) {
			double rssi1 = robot1Data.getTelosBRSSIto(robot2, timestamp, timeStepSize);
			
			double rssi2 = robot2Data.getTelosBRSSIto(robot1, timestamp, timeStepSize);
			
			Location robot1loc = robot1Data.getLocation(timestamp);
			Location robot2loc = robot2Data.getLocation(timestamp);
			
			rssi1 *= -1;
			rssi2 *= -1;
			
			long deltaTimeS = (timestamp - expData.getExpStartTime()) / 1000;
			log(timestamp + "\t" + deltaTimeS + "\t" + (rssi1 != 1 ? rssi1 :"") 
					+ "\t" + (rssi2 != 1 ? rssi2 : "") + "\t" + robot1loc.distanceTo(robot2loc),
					flogger);
		}
	}
	
	private void log(String msg, FileLogger flogger) {
		System.out.println(msg);
		if (flogger != null)
			flogger.log(msg);
	}
	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(msg);
	}
	
	private static void usage() {
		System.setProperty ("PharosMiddleware.debug", "true");
		print("Usage: pharoslabut.logger.analyzer.GetRSSIVsTime <options>\n");
		print("Where <options> include:");
		print("\t-expDir <experiment data directory>: The directory containing experiment data (required)");
		print("\t-robot1 <robot1 ID>: The ID of robot 1 (required)");
		print("\t-robot2 <robot1 ID>: The ID of robot 2 (required)");
		print("\t-timeStepSize <time step size>: The time step in milliseconds (default: 5000)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String expDir = null;
		int robot1ID = -1;
		int robot2ID = -1;
		long timeStepSize = 5000;
		
		// Process the command line arguments...
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-expDir"))
					expDir = args[++i];
				else if (args[i].equals("-robot1"))
					robot1ID = Integer.valueOf(args[++i]);
				else if (args[i].equals("-robot2"))
					robot2ID = Integer.valueOf(args[++i]);
				else if (args[i].equals("-timeStepSize"))
					timeStepSize = Long.valueOf(args[++i]);
				else if (args[i].equals("-debug") || args[i].equals("-d"))
					System.setProperty ("PharosMiddleware.debug", "true");
				else {
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			usage();
			System.exit(1);
		}
		
		if (expDir == null || robot1ID == -1 || robot2ID == -1) {
			usage();
			System.exit(1);
		}
		
		FileLogger flogger = new FileLogger("RSSIvsTime-" + RobotIPAssignments.getRobotName(robot1ID) 
				+ "-" + RobotIPAssignments.getRobotName(robot2ID) + ".txt", false);
		new GetRSSIvsTime(robot1ID, robot2ID, expDir, timeStepSize, flogger);
	}
	
}
