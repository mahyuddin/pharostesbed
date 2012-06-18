package pharoslabut.logger.analyzer.pingTest;
import java.io.*;
import java.util.*;

import pharoslabut.logger.Logger;
import pharoslabut.logger.analyzer.GPSLocationState;
import pharoslabut.logger.analyzer.HeadingState;
import pharoslabut.logger.analyzer.Line;
import pharoslabut.logger.analyzer.TimeCalibrator;
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
	 * The start time of the actual patrol.  This is when the robots stop 
	 * waiting at their first waypoints.
	 */
	private long patrolStartTime;
	
	/**
	 * The locations of the robot as it traversed this edge.
	 */
	private Vector<GPSLocationState> locations = new Vector<GPSLocationState>();
	
	/**
	 * Contains the timestamps of when GPS errors occur.
	 */
	private Vector<Long> gpsErrors = new Vector<Long>();
	
	/**
	 * The heading of the robot as it executes the experiment.
	 */
	private Vector<HeadingState> headings = new Vector<HeadingState>();
	
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
	 * The constructor.
	 * 
	 * @param fileName The name of the robot's experiment log file.
	 */
	public RobotExpData(String fileName) {
		this.fileName = fileName;
		try {
			readFile();
			//calibrateTime();
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
	protected void readFile() throws NumberFormatException, IOException {
		File file = new File(fileName);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
		PlayerGpsData currLoc = null;
		boolean expStartTimeSet = false;
		
		while ((line = br.readLine()) != null) {
			
			// Save the experiment start time
			if (line.contains("Starting experiment at time ")) {
				String keyStr = "Starting experiment at time ";
				String startingLine = line.substring(line.indexOf(keyStr) + keyStr.length());
				
				String[] tokens = startingLine.split("[.\\s]");
				expStartTime = Long.valueOf(tokens[0]);
				//Logger.logDbg("expStartTime = " + expStartTime);
				expStartTimeSet = true;
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
				} else
					Logger.log("Rejecting invalid location: " + currLoc);
			}
			
			// Extract the heading measurements of the robot
			else if (line.contains("New heading=")) {
				String keyStr = "New heading=";
				String headingLine = line.substring(line.indexOf(keyStr) + keyStr.length());
				
				String[] tokens = headingLine.split("[, ]");
				
				long timeStamp = Long.valueOf(line.substring(1,line.indexOf(']')));
				double heading = Double.valueOf(tokens[0]);
				
				// Only add the heading measurement if it is valid.
				if (pharoslabut.sensors.CompassDataBuffer.isValid(heading)) {
					headings.add(new HeadingState(timeStamp, heading));
				} else
					Logger.log("WARNING: Rejecting invalid heading: " + heading);
			}
			
//			// Extract when the current waypoint was reached.
//			else if (line.contains("NavigateCompassGPS:") && line.contains("Arrived at destination")) {
//				// Save the end time of the experiment
//				long timestamp = Long.valueOf(line.substring(1,line.indexOf(']')));
//				if (patrolStarted) {
//					if (currEdge == null) {
//						Logger.logWarn("Arrived at destination but currEdge is null!");
//						//System.exit(1);
//					} else {
//						currEdge.setEndTime(timestamp);
//						pathEdges.add(currEdge);
//						currEdge = null;
//					}
//				}
//			}
			
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
			
		} // end while...
		
		// Do some sanity checks...
		if (!expStartTimeSet) {
			Logger.logErr("experiment start time not set!");
			System.exit(1);
		}
		
		if (locations.size() == 0) {
			Logger.logErr("robot has no known locations...");
			System.exit(1);
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
		patrolStartTime = calibrator.getCalibratedTime(patrolStartTime);
		
		for (int i=0; i < locations.size(); i++) {
			GPSLocationState currLoc = locations.get(i);
			currLoc.calibrateTime(calibrator);
		}
		
		for (int i=0; i < headings.size(); i++) {
			HeadingState currHeading = headings.get(i);
			currHeading.calibrateTime(calibrator);
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
		
		return calibrator;
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
	
	/**
	 * Returns the GPS data collected by the robot as it performed this experiment.
	 * 
	 * @return The vector containing the entire GPS history of the robot.
	 */
	public Vector<GPSLocationState> getGPSHistory() {
		return locations;
	}
	
	/**
	 * 
	 * @return The time when the patrol started.
	 */
	public long getPatrolStartTime() {
		return patrolStartTime;
	}
	
	/**
	 * Gets the time at which the robot started.  This is the time at which the
	 * robot received the StartExpMsg.
	 * 
	 * @return The robot's start time.
	 */
	public long getExpStartTime() {
		return expStartTime;
	}
	
	/**
	 * Gets the robot's end time.  This is the later of the end time of the last edge traversed,
	 * or the last GPS measurement recorded.
	 * 
	 * @return The experiment end time.
	 */
	public long getExpStopTime() {
		long result = -1;

		if (locations.size() > 0) {
			long locEndTime = locations.get(locations.size()-1).getTimestamp();
			result = locEndTime;
		} 
		
		if (result == -1) {
			Logger.logErr("Cannot get experiment end time because no locations recorded! Log File: " + fileName);
			new Exception().printStackTrace(); // get a stack trace for debugging...
			System.exit(1);
		}
		
		return result;
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
		if (time < getExpStartTime()) {
			Logger.logDbg("WARNING: timestamp prior to start time (" + time + " < " + getExpStartTime() + "), assuming speed is 0.");
			return 0;
		}
		
		if (time > getExpStopTime()) {
			Logger.logDbg("WARNING: timestamp after end time. (" + getExpStopTime() + " < " + time + "), assuming speed is 0");
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
		
		//Logger.logDbg("timestamp = " + time + ", beforeIndx = " + beforeIndx + ", afterIndx = " + afterIndx);
		
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
		
		//Logger.logDbg("preLoc=" + preLoc + ", postLoc=" + postLoc + ", dist=" + dist 
		//		+ ", deltaTime=" + deltaTime + ", speed=" + speed);
		
		return speed;
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
				Logger.logDbg("Found relevant motion command at index " + indx);
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
		if (timestamp < getExpStartTime()) {
			Logger.logDbg("WARNING: timestamp prior to start time. (" + timestamp + " < " + getExpStartTime() + ")");
			return getStartHeading();
		}
		
		if (timestamp > getExpStopTime()) {
			Logger.logDbg("WARNING: timestamp after end time. (" + getExpStopTime() + " < " + timestamp + ")");
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
		if (timestamp < getExpStartTime()) {
			Logger.logDbg("WARNING: timestamp prior to start time. (" + timestamp + " < " + getExpStartTime() + ")");
			return getStartLocation();
		}
		
		if (timestamp > getExpStopTime()) {
			Logger.logDbg("WARNING: timestamp after end time. (" + getExpStopTime() + " < " + timestamp + ")");
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
		
		public String toString() {
			return time + "\t" + (time - getExpStartTime())/1000.0 + "\t" + speedCmd + "\t" + headingCmd;
		}
	}
	
	public String toString() {
		String result = 
			"File Name: " + getFileName()
			+ "\nRobot Start Time: " + getExpStartTime();
		return result;
	}
}
