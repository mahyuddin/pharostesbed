package pharoslabut.navigate;

import pharoslabut.sensors.CompassDataBuffer;
import pharoslabut.sensors.GPSDataBuffer;
import pharoslabut.tasks.MotionTask;
import pharoslabut.tasks.Priority;
//import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.exceptions.NoNewDataException;
import playerclient3.structures.gps.PlayerGpsData;

/**
 * Navigates a car to a specified destination.  It calculates which direction
 * the car needs to move and submits MotionTasks to the MotionArbiter to
 * move the vehicle to the destination.
 * 
 * <p>This class depends on the compass and GPS sensors.  If either of these sensors fail to provide 
 * current data, it stops the robot.</p>
 * 
 * @author Chien-Liang Fok
 */
public class NavigateCompassGPS extends Navigate {
	
	public static final double ERROR_HEADING = Double.MIN_VALUE;
	// Define the maximum turn angle in radians.  This is for the Traxxas mobility plane.
	// TODO: Generalize the MAX_TURN_ANGLE to work with any mobility plane
	public static final double MAX_TURN_ANGLE = 0.35; 
	
	public static final int COMPASS_MEDIAN_FILTER_LENGTH = 3;
	
	//public static final int GPS_BUFFER_SIZE = 10;
	//public static final int GPS_SENSE_PERIOD = 1000; // The period at which the GPS is read in ms
	
	//public static final double GPS_TARGET_RADIUS_METERS = 5.5;
	//public static final double GPS_TARGET_RADIUS_METERS = 2.5;
	public static final double GPS_TARGET_RADIUS_METERS = 1.5;
	//public static final double GPS_TARGET_RADIUS_METERS = 2;
	//public static final double GPS_TARGET_RADIUS_METERS = 3;
	//public static final double GPS_TARGET_RADIUS_METERS = 3.5;
	
	//public static final double NAV_SLOW_TURN_ANGLE = 0.2; // turn at an angle greater than this, then slow down
	//public static final double NAV_SLOW_TURN_VELOCITY = 0.4; // the slower speed at which to turn
	
	private GPSDataBuffer gpsDataBuffer;
	private MotionArbiter motionArbiter;
	private CompassDataBuffer compassDataBuffer;
	
	private MotionTask prevTask = null;
	
	/**
	 * Whether we are done navigating to a particular location.
	 */
	private boolean done;
	
	
	private double distanceToDestination;
	
	
	private double instantaneousSpeed;
	
	/**
	 * A constructor.
	 * 
	 * @param motionArbiter The motion arbitration component that accepts motion tasks generated
	 * by this component and decides whether to execute them.
	 * @param compassDataBuffer The compass data source (buffered).
	 * @param gpsDataBuffer The GPS data source (buffered).
	 */
	public NavigateCompassGPS(MotionArbiter motionArbiter, CompassDataBuffer compassDataBuffer, 
			GPSDataBuffer gpsDataBuffer) 
	{
		this.motionArbiter = motionArbiter;
		this.compassDataBuffer = compassDataBuffer;
		this.gpsDataBuffer = gpsDataBuffer;
	}
	
	/**
	 * Sends a stop command to the robot.  Note that the navigation component may still cause the 
	 * robot to continue to move.  To stop the navigation process, call stop().
	 */
	public void stopRobot() {
		MotionTask mt = new MotionTask(Priority.SECOND, 0 /* velocity */, 0 /* heading */);
		motionArbiter.submitTask(mt);
	}
	
	/**
	 * Calculates the proper velocity given the distance to the target and the desired velocity.
	 * As the distance decreases, the maximum velocity also decreases.
	 * 
	 * @param distance The distance to the target in meters
	 * @param desiredVelocity The desired velocity in meters per second.
	 * @return The proper velocity
	 */
	private double calcControlledVelocity(double distance, double desiredVelocity, double desiredHeading) {
		
		// The robot wants to make a very sharp turn; slow down
		if (Math.abs(desiredHeading) > MAX_TURN_ANGLE) {
			// The following was used by M8-Exp1-8
			//return 0.5;
			
			// The following was used by M8-Exp9
			return 0.6;
		}
		
		// These numbers are tuned for the Traxxas Mobility Plane.
		// TODO: Generalize the controlled-velocity-generating-algorithm so it works with any mobility plane
		
		// This was used for M8Exp1-10
//		if (distance > 15)
//			return desiredVelocity;
//		else if (distance > 10)
//			return (desiredVelocity > 1.5) ? 1.5 : desiredVelocity;
//		else if (distance > 7)
//			return (desiredVelocity > 1.0) ? 1.0 : desiredVelocity;
//		else if (distance > 5)
//			return (desiredVelocity > 0.7) ? 0.7 : desiredVelocity;
//		else
//			return (desiredVelocity > 0.5) ? 0.5 : desiredVelocity;
		
		// This was used for M8Exp11
//		if (distance > 10)
//			return desiredVelocity;
//		else if (distance > 6)
//			return (desiredVelocity > 1.5) ? 1.5 : desiredVelocity;
//		else if (distance > 4)
//			return (desiredVelocity > 1.0) ? 1.0 : desiredVelocity;
//		else if (distance > 3)
//			return (desiredVelocity > 0.7) ? 0.7 : desiredVelocity;
//		else
//			return (desiredVelocity > 0.5) ? 0.5 : desiredVelocity;
		
		// This was used for M8Exp12
		if (distance > 6)
			return desiredVelocity;
		else if (distance > 5)
			return (desiredVelocity > 1.5) ? 1.5 : desiredVelocity;
		else if (distance > 4)
			return (desiredVelocity > 1.0) ? 1.0 : desiredVelocity;
		else if (distance > 3)
			return (desiredVelocity > 0.7) ? 0.7 : desiredVelocity;
		else
			return (desiredVelocity > 0.5) ? 0.5 : desiredVelocity;
	}
	
	/**
	 * Calculates the proper heading of the robot.  As the robot's velocity increases,
	 * the heading should be throttled more to prevent the robot from weaving side-to-side.
	 * 
	 * @param velocity The velocity of the robot in m/s
	 * @param desiredHeading The desired heading in radians.  0 radians is straight ahead, 
	 * negative is turn left, positive is turn right.
	 * @return The proper heading that the robot should head in taking into considering
	 * the speed at which the robot is moving.
	 */
	private double calcControlledHeading(double velocity, double desiredHeading) {
		
		// Make sure the heading falls within the acceptable range for the Traxxas mobility plane 
		double clippedHeading = (Math.abs(desiredHeading) > MAX_TURN_ANGLE) ? MAX_TURN_ANGLE : Math.abs(desiredHeading);
		if (desiredHeading < 0)
			clippedHeading *= -1;
		
		// These numbers are tuned for the Traxxas Mobility Plane.
		// TODO: Generalize the controlled-heading-generating-algorithm so it works with any mobility plane
		
		// the following values were used for M8 Exp1-8
//		if (velocity < 0.8)
//			return clippedHeading;
//		else if (velocity < 1.1)
//			return 0.8 * clippedHeading;
//		else if (velocity < 1.6)
//			return 0.4 * clippedHeading;
//		else if (velocity < 2.1)
//			return 0.3 * clippedHeading;
//		else
//			return 0.2 * clippedHeading;
		
		// the following was use by M8 Exp9, the turn angle was under-dampened
//		if (velocity < 0.8)
//			return clippedHeading;
//		else if (velocity < 1.1)
//			return 0.8 * clippedHeading;
//		else if (velocity < 1.6)
//			return 0.45 * clippedHeading;
//		else if (velocity < 2.1)
//			return 0.4 * clippedHeading;
//		else
//			return 0.35 * clippedHeading;
	
		// the following was use by M8 Exp9,
//		if (velocity < 0.8)
//			return clippedHeading;
//		else if (velocity < 1.1)
//			return 0.8 * clippedHeading;
//		else if (velocity < 1.6)
//			return 0.42 * clippedHeading;
//		else if (velocity < 2.1)
//			return 0.35 * clippedHeading;
//		else
//			return 0.25 * clippedHeading;

//		if (velocity < 0.4)
//			return clippedHeading;
//		else if (velocity < 0.6) 
//			return 0.9 * clippedHeading;
//		else if (velocity < 0.8)
//			return 0.8 * clippedHeading;
//		else if (velocity < 1.0)
//			return 0.7 * clippedHeading;
//		else if (velocity < 1.2)
//			return 0.6 * clippedHeading;
//		else if (velocity < 1.4)
//			return 0.42 * clippedHeading;
//		else if (velocity < 1.6)
//			return 0.35 * clippedHeading;
//		else
//			return 0.25 * clippedHeading;

		if (velocity < 0.4)
			return clippedHeading;
		else if (velocity < 0.6) 
			return 0.8 * clippedHeading;
		else if (velocity < 0.8)
			return 0.7 * clippedHeading;
		else if (velocity < 1.0)
			return 0.6 * clippedHeading;
		else if (velocity < 1.2)
			return 0.5 * clippedHeading;
		else if (velocity < 1.4)
			return 0.35 * clippedHeading;
		else if (velocity < 1.6)
			return 0.25 * clippedHeading;
		else
			return 0.15 * clippedHeading;

	}
	
	public PlayerGpsData getLocation(){
		try {
			return gpsDataBuffer.getCurrLoc();
		} catch (NoNewDataException e) {
			e.printStackTrace();
			Logger.logErr("Failed to get current location\n");
			return null;
		}
	}
	
	public boolean areWeThereYet(long aheadTime) {
		boolean result = (distanceToDestination / instantaneousSpeed) * 1000 < aheadTime;
		Logger.logDbg("distanceToDestination = " + distanceToDestination 
				+ ", instantaneousSpeed = " + instantaneousSpeed + ", aheadTime = " + aheadTime + ", result = " + result);
		return result;
	}

	public double getCompassHeading(){
		try {
			return compassDataBuffer.getMedian(COMPASS_MEDIAN_FILTER_LENGTH);
		} catch (NoNewDataException e) {
			e.printStackTrace();
			Logger.logErr("Unable to get compass heading\n");
			return ERROR_HEADING;
		}
	}
	
	public void SubmitMotionTask(TargetDirection targetDirection, double velocity){
		double currVel = calcControlledVelocity(targetDirection.getDistance(), velocity, targetDirection.getHeadingError());
		double robotHeadingInstr = calcControlledHeading(currVel, targetDirection.getHeadingError()); 
		
		// For bookkeeping purposes, used by method areWeThereYet()
		instantaneousSpeed = currVel;
		
		//headingError * MIN_VELOCITY/currVel;
		/*
		 * Positive heading error means the robot must turn left.
		 * Negative heading error means the robot must turn right.
		 */
		//double headingError = targetDirection.getHeadingError();

		// slow down if the turning rate is too fast
		//if (Math.abs(headingError) > NAV_SLOW_TURN_ANGLE) {
		//	currVel = NAV_SLOW_TURN_VELOCITY;
	    //}
		
		//double MAX_VELOCITY = 2.0; // m/s
		//double MIN_VELOCITY = 0.4;
		
		// If the heading error is greater than X and the speed is greater than X, 
		// proportionally decrease the change in heading sent to the robot
		//double robotHeadingInstr = headingError * MIN_VELOCITY/currVel;

		MotionTask mt = new MotionTask(Priority.SECOND, currVel, robotHeadingInstr);
		motionArbiter.submitTask(mt);
		prevTask = mt;
		
		Logger.log("Current Instruction:\n\tVelocity: + " + mt.getVelocity() + "\n\tHeading: " + mt.getHeading());
	}
	
	/**
	 * Calculates the next motion task that should be submitted to the MotionArbiter.
	 * The new motion tasks heading should ensure the robot continues to move towards the next way point.
	 * 
	 * @param currLoc The current location.
	 * @param currHeading The current heading.
	 * @param dest The destination location
	 * @param velocity The velocity at which to travel towards the destination
	 * @return Whether the destination has been reached.
	 */
	private boolean doNextMotionTask(Location currLoc, double currHeading, Location dest, double velocity) {
		TargetDirection targetDirection = locateTarget(currLoc, currHeading, dest);
		boolean done = false;
		
		// Save statistics in local variables.  This is used by
		// the areWeThereYet(...) method.
		distanceToDestination = targetDirection.getDistance();
		
		if (targetDirection.getDistance() < GPS_TARGET_RADIUS_METERS) {
			Logger.log("Destination reached!");
			done = true;

			// Submit a stop motion task
			if (prevTask != null) {
				motionArbiter.revokeTask(prevTask);
				prevTask = null;
			}
			MotionTask mt = new MotionTask(Priority.SECOND, MotionTask.STOP_SPEED, MotionTask.STOP_HEADING);
			motionArbiter.submitTask(mt);
			prevTask = mt;
			
			Logger.log("Arrived at destination " + dest + "!");
		} else if (targetDirection.getDistance() > 2000) {
			Logger.logErr("Invalid distance: Greater than 2km (" + targetDirection.getDistance() + "), stopping robot...");
				stopRobot();
		} else {
			SubmitMotionTask(targetDirection, velocity);
		}
		return done;
	}
	
	/**
	 * Moves a robot to a particular location at a certain speed.  If either the GPS location or heading
	 * information is unavailable, halt the robot.
	 * 
	 * @param dest The destination location.
	 * @param velocity The speed in meters per second that the robot should travel at.
	 * @return true if the robot successfully reached the destination
	 */
	public boolean go(Location dest, double velocity) {
		done = false;
		boolean success = false;
		
		while (!done) {
			Location currLoc = null;
			
			try {
				currLoc = new Location(gpsDataBuffer.getCurrLoc());
				if (GPSDataBuffer.isValid(currLoc)) {
					double currHeading = compassDataBuffer.getMedian(COMPASS_MEDIAN_FILTER_LENGTH);
					done = doNextMotionTask(currLoc, currHeading, dest, velocity);
					if (done) success = true;
					//dprevLoc = currLoc;
				} else {
					Logger.logErr("Invalid current location " + currLoc + ", halting robot...");
					stopRobot();
				}
			} catch(NoNewDataException nnde) {
				if (currLoc == null) {
					Logger.logErr("Unable to get the current location, halting robot...");
				} else
					Logger.logErr("Unable to get the current heading, halting robot...");
				stopRobot();
			}
			
			if (!done) {
				try {
					synchronized(this) {
						wait(NAV_CYCLE_PERIOD); // pause for a moment before repeating
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		stopRobot();
		Logger.log("Done going to " + dest + ", success=" + success);
		return success;
	}
	
	/**
	 * Stops the navigation process.
	 */
	public void stop() {
		done = true;
	}
	
//	private void logErr(String msg) {
//		String result = "NavigateCompassGPS: ERROR: " + msg;
//		
//		System.err.println(result);
//		
//		// always log text to file if a FileLogger is present
//		if (flogger != null)
//			flogger.log(result);
//	}
//	
//	private void log(String msg) {
//		String result = "NavigateCompassGPS: " + msg;
//		
//		// only print log text to string if in debug mode
//		if (System.getProperty ("PharosMiddleware.debug") != null)
//			System.out.println(result);
//		
//		// always log text to file if a FileLogger is present
//		if (flogger != null)
//			flogger.log(result);
//	}
//	
//	public static final void main(String[] args) {
//		String serverIP = "10.11.12.20";
//		int serverPort = 6665;
//		
//		// Enable debug output
//		System.setProperty ("PharosMiddleware.debug", "true"); 
//		
//		PlayerClient client = null;
//		try {
//			client = new PlayerClient(serverIP, serverPort);
//		} catch(PlayerException e) {
//			System.err.println("Error connecting to Player: ");
//			System.err.println("    [ " + e.toString() + " ]");
//			System.exit (1);
//		}
//		
//		Position2DInterface motors = client.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
//		Position2DInterface compass = client.requestInterfacePosition2D(1, PlayerConstants.PLAYER_OPEN_MODE);
//		GPSInterface gps = client.requestInterfaceGPS(0, PlayerConstants.PLAYER_OPEN_MODE);
//		
//		if (motors == null) {
//			System.err.println("motors is null");
//			System.exit(1);
//		}
//		
//		if (compass == null) {
//			System.err.println("compass is null");
//			System.exit(1);
//		}
//		
//		if (gps == null) {
//			System.err.println("gps is null");
//			System.exit(1);
//		}
//		
//		CompassDataBuffer compassDataBuffer = new CompassDataBuffer(compass);
//		GPSDataBuffer gpsDataBuffer = new GPSDataBuffer(gps);
//		MotionArbiter motionArbiter = new MotionArbiter(motors);
//		
//		String fileName = "NavigateCompassGPS_" + FileLogger.getUniqueNameExtension() + ".log"; 
//		FileLogger flogger = new FileLogger(fileName);
//		
//		NavigateCompassGPS navigator = new NavigateCompassGPS(motionArbiter, compassDataBuffer, gpsDataBuffer, flogger);
//		
//		// TEST CODE:  See if the robot is able to move to a specific destination
//		//Location destLoc = new Location(30.2655183,	-97.7690083); // barton springs point A
//		//Location destLoc = new Location(30.2657367,	-97.7684767); // barton springs point B
//		Location destLoc = new Location(30.2657533,	-97.7680267); // barton springs point C  
//			
//		
//		//double velocity = 0.4; // go relatively slowly
//		double velocity = 1; // go relatively briskly
//		if (!navigator.go(destLoc, velocity)) {
//			flogger.log("Failed to reach to destination.");
//		}
//	}
}
