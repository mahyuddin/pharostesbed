package pharoslabut.demo.mrpatrol2.daemons;

import pharoslabut.demo.mrpatrol2.Waypoint;
import pharoslabut.demo.mrpatrol2.behaviors.Behavior;
import pharoslabut.demo.mrpatrol2.behaviors.BehaviorBeacon;
import pharoslabut.demo.mrpatrol2.behaviors.BehaviorGoToLocation;
import pharoslabut.demo.mrpatrol2.behaviors.BehaviorWaitTime;
import pharoslabut.demo.mrpatrol2.config.ExpConfig;
import pharoslabut.demo.mrpatrol2.config.RobotExpSettings;
import pharoslabut.io.Message;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.Location;
import pharoslabut.navigate.MotionArbiter;

/**
 * Handles the execution of an uncoordinated multi-robot patrol 2 (MRP2) experiment.
 * 
 * @author Chien-Liang Fok
 */
public class LooselyCoordinatedOutdoorPatrolDaemon extends OutdoorPatrolDaemon {
	
	/**
	 * The constructor.
	 * 
	 * @param expConfig The experiment settings.
	 * @param mobilityPlane The type of mobility plane being used.
	 * @param playerServerIP The IP address of the player server.
	 * @param playerServerPort The TCP port of the player server.
	 * @param mCastAddress The multi-cast address for transmitting beacons.
	 * @param mCastPort The multi-cast port for transmitting beacons.
	 */
	public LooselyCoordinatedOutdoorPatrolDaemon(ExpConfig expConfig, MotionArbiter.MotionType mobilityPlane, 
			String playerServerIP, int playerServerPort,
			int serverPort,
			String mCastAddress, int mCastPort) 
	{
		super(expConfig, mobilityPlane, playerServerIP, playerServerPort, serverPort, mCastAddress, mCastPort);
		createBehaviors();
	}
	
	/**
	 * Handle incoming messages.  There are two types of messages that can come in:
	 * 
	 */
	@Override
	public void newMessage(Message msg) {
		
	}
	
	/**
	 * Generates all of the behaviors used in this experiment.
	 */
	private void createBehaviors() {
		
		Logger.logDbg("Creating the behaviors used in the experiment.");
		
		RobotExpSettings mySettings = expConfig.getMySettings();
		
		if (mySettings == null) {
			Logger.logErr("Unable to get robot settings.");
			System.exit(1);
		}
		
		String firstWaypointName = mySettings.getFirstWaypoint();
		int firstWaypointIndx = expConfig.getWaypointIndex(firstWaypointName);
		
		// Create a behavior that moves the robot to the starting location.
		// Note that this behavior has no prerequisites so it should start immediately when the experiment begins.
		Location firstWaypoint = expConfig.getWaypoint(firstWaypointName);
		if (firstWaypoint == null) {
			Logger.logErr("Unable to get first waypoint.");
			System.exit(1);
		}
		Behavior b0 = new BehaviorGoToLocation("GoToLoc_0_" + firstWaypointName, navigatorCompassGPS, firstWaypoint, SPEED_TO_FIRST_WAYPOINT);
		addBehavior(b0);
		Logger.logDbg("Creating behavior " + b0);
			
		// Create a behavior that waits till it's time to start.  
		// Note that this behavior has no prerequisites so it should start immediately when the experiment begins.
		Behavior waitBehavior = new BehaviorWaitTime("Wait_at_First_Waypoint", expConfig.getStartDelay() * 1000);
		addBehavior(waitBehavior);
		Logger.logDbg("Creating behavior " + waitBehavior);
		
		// Create a behavior that starts the beaconing.
		// Note that this behavior has no prerequisites so it should start immediately when the experiment begins.
		Behavior beaconBehavior = new BehaviorBeacon("Beacon", mCastAddress, mCastPort, serverPort);
		addBehavior(beaconBehavior);
		
		// By initializing the prevBehavior to be waitBehavior, we make 
		// the robot wait at the first waypoint until the waitBehavior is done. 
		// This enables the robots to reach their first waypoints prior to 
		// embarking on the multi-robot patrol.
		Behavior prevBehavior = waitBehavior;
		
		// For each round...
		int wpCount = 1;  // A counter for the total number of waypoints visited.  Starts at 1 b/c robot is already at first waypoint.
		for (int round = 0; round < expConfig.getNumRounds(); round++) {
			
			int wpIndx = (firstWaypointIndx + 1) % expConfig.getNumWaypoints();
			
			// Visit each waypoint in the patrol route...
			for (int wpCnt = 0; wpCnt < expConfig.getNumWaypoints(); wpCnt++) {
				
				Waypoint wp = expConfig.getWaypoint(wpIndx);

				Behavior currBehavior = new BehaviorGoToLocation("GoToLoc_" + wpCount + "_" + wp.getName(), navigatorCompassGPS, wp.getLoc(), wp.getSpeed());
				currBehavior.addPrerequisite(prevBehavior);

				addBehavior(currBehavior);
				prevBehavior = currBehavior;

				Logger.logDbg("Creating behavior " + currBehavior);

				wpIndx++; wpCount++;
				wpIndx %= expConfig.getNumWaypoints();
			}
			
		}
		
		// Make the beacon behavior stop after the robot arrives at the last waypoint in patrol.
		beaconBehavior.addDependency(prevBehavior);
		
		// Get the home starting location
		Location homeLocation = getLocation();
		Behavior bHome = new BehaviorGoToLocation("GoToLoc_" + wpCount + "_home", navigatorCompassGPS, homeLocation, SPEED_TO_HOME);
		bHome.addPrerequisite(prevBehavior);
		addBehavior(bHome);
		
		Logger.logDbg("Creating behavior " + bHome);
	}
}