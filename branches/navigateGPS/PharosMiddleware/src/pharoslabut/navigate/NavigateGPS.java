package pharoslabut.navigate;

import java.util.ArrayList;

import pharoslabut.sensors.GPSDataBuffer;
import pharoslabut.tasks.MotionTask;
import pharoslabut.tasks.Priority;
import pharoslabut.logger.FileLogger;
import pharoslabut.exceptions.NoNewDataException;

/**
 * Navigates a car to a specified destination.  It calculates which direction
 * the car needs to move and submits MotionTasks to the MotionArbiter to
 * move the vehicle to the destination.
 * 
 * <p>This class depends on the  GPS sensors.  If the sensor fails to provide 
 * current data, it stops the robot.</p>
 * 
 * @author Chien-Liang Fok
 */
public class NavigateGPS extends Navigate {
	// Define the maximum turn angle in radians.  This is for the Traxxas mobility plane.
	// TODO: Generalize the MAX_TURN_ANGLE to work with any mobility plane
	public static final double MAX_TURN_ANGLE = 0.35; 
	
	public static final int GPS_BUFFER_SIZE = 10;
	public static final int GPS_SENSE_PERIOD = 1000; // The period at which the GPS is read in ms
	
	public static final double GPS_TARGET_RADIUS_METERS = 3.5; // Too big?
	
	private GPSDataBuffer gpsDataBuffer;
	private MotionArbiter motionArbiter;
	
	private MotionTask prevTask = null;
	
	/**
	 * A constructor.
	 * 
	 * @param motionArbiter The motion arbitration component that accepts motion tasks generated
	 * by this component and decides whether to execute them.
	 * @param gpsDataBuffer The GPS data source (buffered).
	 */
	public NavigateGPS(MotionArbiter motionArbiter, GPSDataBuffer gpsDataBuffer,
			FileLogger flogger) {
		super(flogger);
		this.motionArbiter = motionArbiter;
		this.gpsDataBuffer = gpsDataBuffer;
	}
	
	private void stopRobot() {
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
		
		if (targetDirection.getDistance() < GPS_TARGET_RADIUS_METERS) {
			log("Destination reached!");
			done = true;

			// Submit a stop motion task
			if (prevTask != null) {
				motionArbiter.revokeTask(prevTask);
				prevTask = null;
			}
			MotionTask mt = new MotionTask(Priority.SECOND, MotionTask.STOP_SPEED, MotionTask.STOP_HEADING);
			motionArbiter.submitTask(mt);
			prevTask = mt;
			
			log("Arrived at destination " + dest + "!");
		} else if (targetDirection.getDistance() > 2000) {
				log("Invalid distance: Greater than 2km (" + targetDirection.getDistance() + "), stopping robot...");
				stopRobot();
		} else {
			double currVel = calcControlledVelocity(targetDirection.getDistance(), velocity, targetDirection.getHeadingError());
			double robotHeadingInstr = calcControlledHeading(currVel, targetDirection.getHeadingError()); 
			
			MotionTask mt = new MotionTask(Priority.SECOND, currVel, robotHeadingInstr);
			motionArbiter.submitTask(mt);
			prevTask = mt;
			
			log("Current Instruction:\n\tVelocity: + " + mt.getVelocity() + "\n\tHeading: " + mt.getHeading());
		}
		return done;
	}
	
	// wait, gather GPS data, move, wait, gather GPS data, find heading and determine if accurate
	public boolean go(Location target, double moveVel)
	{
		boolean arrived = false; // has the robot arrived at target?
		boolean dataReceived = false;
		
		ArrayList<Location> lastLoc = new ArrayList<Location>();
		ArrayList<Location> currLoc = new ArrayList<Location>();
		
		
		
		while(!arrived) {
			// get GPS data
			while(!dataReceived) {
				try {
					log("go: Populating lastLoc buffer...");
					locBuffer.add(new Location(gpsDataBuffer.getCurrLoc()) ); // insert new Location at index 1, push the rest down
					if(locBuffer.get(11) != null) {
						locBuffer.remove(11); // only keep 10 locations(1-10) + average(0)
					}
					
					lastLoc = locBuffer.get(0); // old time-adjusted average location of robot
					locBuffer = averageLocBuffer(locBuffer, currVel); // time-adjusts all points and averages them into index 0
					currLoc = locBuffer.get(0); // new time-adjusted average location of robot
					
					heading = getHeading(lastLoc, currLoc, target); // gets the deviation heading to the target
					
					waitNavPeriod(); // make sure the GPS is ready
				} catch(NoNewDataException nnde) {
					logErr("go: Unable to get GPS data, GPS is offline...");
					gpsOnline = false;
				}
			}
			// move
			
			// get GPS data
			
			// average the two buffers
			double lastLocAvg;
			double currLocAvg;
			
			// get a heading
			
		}
		
	}
	
	/**
	 * Navigates to the target location. The system is tiered so that if
	 * one system fails it should still be able to make it to the target
	 * based on previous information. The highest priority is the accelerometer
	 * aided by the GPS, then the accelerometer or GPS, then movement based on 
	 * prior data. This priority system makes the robot able to function
	 * properly outdoors and indoors, under tree cover, and when one or both
	 * systems fail.
	 * 
	 * @param origin Where did you start
	 * @param target Where you want to go
	 * @param moveVel How fast you want to move when not close to target
	 * @return true If the robot arrives at the destination
	 */
	/*
	public boolean go(Location start, Location target, double moveVel)
	{	
		// If robot is at start
		// Fill locBuffer
		// Average locBuffer and put in index 0
		
		// Repeat:
		// Move 1 GPS period
		// Get robot to destination deviation heading
		// Adjust according to heading tolerance
		// Get next location 
		// Average locBuffer and put in index 0
		
		// Arrive at destination
		// Empty locBuffer
		// Ready for next go()
		
		boolean arrived = false; // has the robot arrived at the destination?
		boolean gpsOnline = true; // is the robot using GPS?
		boolean accelOnline = false; // is the robot using accelerometers?, not yet implemented
		//boolean motorNav = true; // only navigation is done by extrapolating postion based on speed and heading
		
		double currVel = 0; // stopped
		double heading = 0; // first move is straight forward, this is a deviation heading
		double dist; // how far away the target is
		double robotHeadingInstr; // tells the robot what to do
		
		Location lastLoc; // last location robot was at
		Location currLoc; // current location robot is at
		
		ArrayList<Location> locBuffer = new ArrayList<Location>(); // buffer containing last 10 locations and their average
		// ****how far do you have to move before the pre and post gps are different enough to get a valid heading
		locBuffer.add(0, start); // sets start as the first location the robot is at, assumes this is accurate, if you try to get GPS data instead the loop might run into a null pointer	
		start.setTime(System.currentTimeMillis()); // reset the start time to be right when the robot starts moving
		
		for(int x = 1; x <= 10; x++) { locBuffer.add(x, null); }
		
		// movement loop
		while(!arrived) {
			// Determine if the robot is at the target, not at the target, or too far away to reach the target and move accordingly
			dist = distToTarget(locBuffer, target); // get the distance between locBuffer(0) and target
			if ( dist < GPS_TARGET_RADIUS_METERS) { // the robot has arrived at the target
				log("Destination reached!");
				arrived = true;
	
				// Submit a stop motion task
				if (prevTask != null) {
					motionArbiter.revokeTask(prevTask);
					prevTask = null;
				}
				MotionTask mt = new MotionTask(Priority.SECOND, MotionTask.STOP_SPEED, MotionTask.STOP_HEADING);
				motionArbiter.submitTask(mt);
				prevTask = mt;
				
				log("Arrived at destination " + target + "!");
			} else if ( dist > 2000) { // robot cannot reach the target because it is too far
					log("Invalid distance: Greater than 2km (" + distToTarget(locBuffer, target) + "), stopping robot...");
					stopRobot();
			} else { // robot is en route, keep moving
				currVel = calcControlledVelocity(dist, moveVel, heading); // if the robot is close to target slow down, if it is turning slow down
				robotHeadingInstr = calcControlledHeading(currVel, heading); // adjusts currVel for the Traxxas 
				
				// send motion task
				MotionTask mt = new MotionTask(Priority.SECOND, currVel, robotHeadingInstr);
				motionArbiter.submitTask(mt);
				prevTask = mt;
				
				log("Current Instruction:\n\tVelocity: + " + mt.getVelocity() + "\n\tHeading: " + mt.getHeading());
			}
			
			// now that the robot has moved, evaluate its new heading
			if(gpsOnline && accelOnline) { // 1st tier
				log("NavigateGPS: Using tier 1 (GPS & Accelerometer)");
				waitNavPeriod(); // make sure the GPS is ready
				
			} else if(gpsOnline && !accelOnline) { // 2nd tier
				try {
					log("NavigateGPS: Using tier 2 (GPS)");
					locBuffer.add( 1, new Location(gpsDataBuffer.getCurrLoc()) ); // insert new Location at index 1, push the rest down
					if(locBuffer.get(11) != null) {
						locBuffer.remove(11); // only keep 10 locations(1-10) + average(0)
					}
					
					lastLoc = locBuffer.get(0); // old time-adjusted average location of robot
					locBuffer = averageLocBuffer(locBuffer, currVel); // time-adjusts all points and averages them into index 0
					currLoc = locBuffer.get(0); // new time-adjusted average location of robot
					
					heading = getHeading(lastLoc, currLoc, target); // gets the deviation heading to the target
					
					waitNavPeriod(); // make sure the GPS is ready
				} catch(NoNewDataException nnde) {
					logErr("go: Unable to get GPS data, GPS is offline...");
					gpsOnline = false;
				}
			} else if(!gpsOnline && accelOnline) { // 3rd tier
				log("NavigateGPS: Using tier 3 (Accelerometer)");
				
			} else if(!gpsOnline && !accelOnline && motorNav) { // 4th tier
				// rely on motor controls, this assumes that you have already started moving, avoid this but it lets you continue when there is nothing else
				log("NavigateGPS: Using tier 1 (Blind motor control)");
				
				if(locBuffer.get(1) != null) { // cannot move if there are no locations to go on
					// calculate next location, based on previous calculations
					
					
					lastLoc = locBuffer.get(0); // old time-adjusted average location of robot
					locBuffer = averageLocBuffer(locBuffer, currVel); // time-adjusts all points and averages them into index 0
					currLoc = locBuffer.get(0); // new time-adjusted average location of robot
					
					// get heading
					heading = getHeading(lastLoc, currLoc, target); // gets the deviation heading to the target
				}
				
			} else {
				log("NavigateGPS: Running blind, halting robot...");
				stopRobot();
			}
			
			// test to see if the sensors are working
			try { // test GPS
				gpsDataBuffer.getCurrLoc();
				gpsOnline = true;
				log("NavigatGPS: GPS online");
			} catch(NoNewDataException nnde) {
				gpsOnline = false;
			}
		}
		
		return arrived;
	}*/
	
	/**
	 * Calculates the distance to the target from its current location.
	 * 
	 * @param locBuffer Buffer containing past 10 locations and their time-adjusted average
	 * @param target The location to end at
	 * @return The distance to the target
	 */
	private double distToTarget(ArrayList<Location> locBuffer, Location target)
	{
		double dist;
		Location currLoc = locBuffer.get(0);
		dist = currLoc.distanceTo(target);
		log("NavigateGPS: Distance to target: " + dist);
		return dist;
	}
	
	/**
	 * Moves a robot to a particular location at a certain speed.  If either the GPS location or heading
	 * information is unavailable, halt the robot.
	 * 
	 * @param dest The destination location.
	 * @param velocity The speed in meters per second that the robot should travel at.
	 * @return true if the robot successfully reached the destination
	 */
	/*
	public boolean go(Location dest, double velocity) {
		if (flogger != null) {
			ArrayList<Double> buffer = new ArrayList<Double>();
			buffer.add(0.001);
			flogger.log("***TEST***: Index 0 = " + buffer.get(0) );
		}
		return true;
		
		boolean done = false;
		boolean success = false;
		Location currLoc = null;
		
		Location gpsAvg = null; 
		
		ArrayList<Location> gpsPoints = new ArrayList<Location>(10); // list of past gps points
		
		gpsPoints.add(0, null);
		
		try {
			// get 10 gps data points and average them together, index 1 is latest location
			gpsPoints.add(1, currLoc = new Location(gpsDataBuffer.getCurrLoc()) );
			waitNavPeriod();
			gpsPoints.add(2, currLoc = new Location(gpsDataBuffer.getCurrLoc()) );
			waitNavPeriod();
			gpsPoints.add(3, currLoc = new Location(gpsDataBuffer.getCurrLoc()) );
			waitNavPeriod();
			gpsPoints.add(4, currLoc = new Location(gpsDataBuffer.getCurrLoc()) );
			waitNavPeriod();
			gpsPoints.add(5, currLoc = new Location(gpsDataBuffer.getCurrLoc()) );
			waitNavPeriod();
			gpsPoints.add(6, currLoc = new Location(gpsDataBuffer.getCurrLoc()) );
			waitNavPeriod();
			gpsPoints.add(7, currLoc = new Location(gpsDataBuffer.getCurrLoc()) );
			waitNavPeriod();
			gpsPoints.add(8, currLoc = new Location(gpsDataBuffer.getCurrLoc()) );
			waitNavPeriod();
			gpsPoints.add(9, currLoc = new Location(gpsDataBuffer.getCurrLoc()) );
			waitNavPeriod();
			gpsPoints.add(10, currLoc = new Location(gpsDataBuffer.getCurrLoc()) );
			waitNavPeriod();
			
			gpsPoints = averageGPSPoints(gpsPoints, 0);
			gpsAvg = gpsPoints.get(0);
			
		} catch(NoNewDataException nnde) {
			if (currLoc == null || gpsAvg == null) {
				logErr("go: Unable to get the current location, halting robot...");
			} else
				logErr("go: Unable to get the current heading, halting robot...");
			stopRobot();
		}
		
		
		
		currLoc = null;
		gpsAvg = null;
		double time = 0;
		while (!done || time > 20) {
			try {
				gpsPoints.add(1, currLoc = new Location(gpsDataBuffer.getCurrLoc()) );
				gpsPoints = averageGPSPoints(gpsPoints, velocity);
				gpsAvg = gpsPoints.get(0);
				if (GPSDataBuffer.isValid(currLoc)) {
					double currHeading = getGPSHeading(gpsPoints);
					done = doNextMotionTask(gpsAvg, currHeading, dest, velocity);
					if (done) success = true;
				} else {
					logErr("go: Invalid current location " + currLoc + ", halting robot...");
					stopRobot();
				}
			} catch(NoNewDataException nnde) {
				if (currLoc == null || gpsAvg == null) {
					logErr("go: Unable to get the current location, halting robot...");
				} else
					logErr("go: Unable to get the current heading, halting robot...");
				stopRobot();
			}
			if (!done) {
				waitNavPeriod();
			}
			currLoc = null;
			gpsAvg = null;
			time += 1;
		}
		log("go: Done going to " + dest + ", success=" + success);
		return success;
	}*/
	
	/**
	 * Averages the past 10 points of the robot to get a time adjusted  
	 * GPS location. Assumes the robot heading is straight and that the
	 * indices are 1 second apart. Index 1 is the most recent data point.
	 * 
	 * @param gpsPoints
	 * @param robotSpeed
	 * @return time-averaged GPS location
	 */
	private ArrayList<Location> averageLocBuffer(ArrayList<Location> locBuffer, double currVel) {
		Location locAvg = locBuffer.get(0);
		Location tmp = null;
		int next;
		int latAdj;
		int lonAdj;
		double timeDif;
		
		// iterate through the locBuffer list and apply time adjust
		next = 2; // index 1 is already correct
		while(locBuffer.get(next) != null && next <= 10) {
			tmp = locBuffer.get(next); // store location
			locBuffer.remove(next); // remove location from buffer
			
			// if the robot is going in the negative x or y direction then subtract distance
			if( (locAvg.latitude()-tmp.latitude()) > 0 ) latAdj = 1;
			else latAdj = -1;
			if( (locAvg.longitude()-tmp.longitude()) > 0 ) lonAdj = 1;
			else lonAdj = -1;
			
			// add the time adjusted point into the buffer that the current location, = x0 + currVel * (tf - t0)
			timeDif = locBuffer.get(0).getTime()-tmp.getTime(); // delta time
			locBuffer.add(next, new Location( (tmp.latitude()+(latAdj*currVel*timeDif)) , (tmp.longitude()+(lonAdj*currVel*timeDif)) ) );
			log("Time-adjusted calculation: x0 + currVel * (tf - t0): " + locBuffer.get(next).latitude() + " + " + currVel + " * (" + locBuffer.get(0).getTime() + " - " +  locBuffer.get(next).getTime() + ")");
			next += 1; 
		}
		
		// average the locBuffer list
		next = 1;
		while(locBuffer.get(next) != null && next <= 10) {
			// add each location in the buffer to the average
			locAvg = new Location(locAvg.latitude()+locBuffer.get(next).latitude(), locAvg.longitude()+locBuffer.get(next).longitude()); 
			next += 1;
		}
		locAvg = new Location(locAvg.latitude()/(next-1), locAvg.longitude()/(next-1)); // divide by total number of points
		locBuffer.remove(0);
		locBuffer.add(locAvg); // update the locAvg, average location
		
		log("NavigateGPS: Computed time-averaged GPS location: " + locAvg.latitude() + " " + locAvg.longitude() );
		
		for(int x = 0; x <= 10; x++) {
			log("locBuffer(" + x +"): " + locBuffer.get(x));
		}
		return locBuffer;
	}
	
	private double getHeading(Location lastLoc, Location currLoc, Location target) {
		double heading = 0;
		
		double robotAngle = lastLoc.angleTo(currLoc); // gets unit circle angle in radians of the direction the robot is facing
		double targetAngle = currLoc.angleTo(target); // gets unit circle angle in radians of the direction to the target
		
		heading = robotAngle - targetAngle; // deviation heading to target
		
		// get adjusted deviation heading, compensates for different quadrants. A negative heading is turn left, a positive heading is turn right
		if( robotAngle <= Math.PI && targetAngle >= Math.PI ) { // robot heading is 1st or 2nd quadrant, target is in 3rd or 4th quadrant
			if( Math.abs(heading) > Math.PI ) { // if absolute heading is greater than 180deg then adjust
				heading += (2*Math.PI);
			}
		} else if( robotAngle > Math.PI && targetAngle < Math.PI ) { // robot heading is 3rd or 4th quadrant, target is in 1st or 2nd quadrant
			if( Math.abs(heading) > Math.PI ) { // if absolute heading is greater than 180deg then adjust
				heading -= (2*Math.PI);
			}
		}
		
		log("NavigateGPS: Last Location: " + lastLoc + " Current Location: " + currLoc);
		log("NavigateGPS: Computed Deviation Heading: " + heading + " Robot Heading: " + robotAngle + " Target Heading: " + targetAngle);
	
		return heading;
	}
	
	private void waitNavPeriod() {
		try {
			synchronized(this) {
				wait(NAV_CYCLE_PERIOD); // pause for a moment before repeating
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void logErr(String msg) {
		String result = "NavigateGPS: ERROR: " + msg;
		
		System.err.println(result);
		
		// always log text to file if a FileLogger is present
		if (flogger != null)
			flogger.log(result);
	}
	
	private void log(String msg) {
		String result = "NavigateCompassGPS: " + msg;
		
		// only print log text to string if in debug mode
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
		
		// always log text to file if a FileLogger is present
		if (flogger != null)
			flogger.log(result);
	}
}
