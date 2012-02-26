package pharoslabut.demo.mrpatrol2.daemons;

import pharoslabut.RobotIPAssignments;
//import pharoslabut.beacon.WiFiBeaconBroadcaster;
import pharoslabut.beacon.WiFiBeaconEvent;
import pharoslabut.beacon.WiFiBeaconListener;
//import pharoslabut.beacon.WiFiBeaconReceiver;
import pharoslabut.demo.mrpatrol2.Waypoint;
import pharoslabut.demo.mrpatrol2.behaviors.Behavior;
import pharoslabut.demo.mrpatrol2.behaviors.BehaviorGoToLocation;
import pharoslabut.demo.mrpatrol2.behaviors.BehaviorWaitTime;
import pharoslabut.demo.mrpatrol2.config.ExpConfig;
import pharoslabut.demo.mrpatrol2.config.RobotExpSettings;
import pharoslabut.demo.mrpatrol2.msgs.BeaconMsg;
import pharoslabut.exceptions.PharosException;
import pharoslabut.io.Message;
import pharoslabut.logger.Logger;
//import pharoslabut.navigate.LineFollower;
import pharoslabut.navigate.Location;
import pharoslabut.navigate.MotionArbiter;
//import pharoslabut.navigate.NavigateCompassGPS;
//import pharoslabut.sensors.PathLocalizerOverheadMarkers;

/**
 * Handles the execution of an uncoordinated multi-robot patrol 2 (MRP2) experiment.
 * 
 * @author Chien-Liang Fok
 */
public class UncoordinatedOutdoorPatrolDaemon extends OutdoorPatrolDaemon implements WiFiBeaconListener {
	
	/**
	 * The constructor.
	 * 
	 * @param expConfig The experiment settings.
	 * 
	 * @param lineFollower The line follower.
	 * @param pathLocalizer The path localizer.
	 * @param numRounds The number of rounds to patrol.
	 * @param wifiBeaconBroadcaster The beacon broadcaster.
	 * @param wifiBeaconReceiver The beacon receiver.
	 */
	public UncoordinatedOutdoorPatrolDaemon(ExpConfig expConfig, MotionArbiter.MotionType mobilityPlane, String playerServerIP, int playerServerPort,
			String mCastAddress, int mCastPort) 
	{
		super(expConfig, mobilityPlane, playerServerIP, playerServerPort, mCastAddress, mCastPort);
		
		// Use beacons just so we can easily synchronize the clocks of everyone.
		Logger.logDbg("Adding self as beacon listener.");
		wifiBeaconReceiver.addBeaconListener(this);
		
		Logger.logDbg("Starting the beaconing.");
		RobotExpSettings settings = expConfig.getMySettings();
		
		if (settings != null) {
			wifiBeaconBroadcaster.setBeacon(new BeaconMsg(settings.getIP(), settings.getPort()));
			wifiBeaconBroadcaster.start(minPeriod, maxPeriod, txPower);
			createBehaviors();
		} else {
			Logger.logErr("Unable to get robot settings.");
			System.exit(1);
		}
	}
	
	@Override
	public void newMessage(Message msg) {
		// Since this is an uncoordinated patrol, we expect no messages to be exchanged and thus
		// do nothing here.
	}
	
	/**
	 * Generates all of the behaviors used in this experiment.
	 */
	public void createBehaviors() {
		
		RobotExpSettings mySettings = expConfig.getMySettings();
		String firstWaypointName = mySettings.getFirstWaypoint();
		int firstWaypointIndx = expConfig.getWaypointIndex(firstWaypointName);
		
		// Create behavior that moves the robot to the starting location.
		Location firstWaypoint = expConfig.getWaypoint(firstWaypointName);
		if (firstWaypoint == null) {
			Logger.logErr("Unable to go to first waypoint.");
			System.exit(1);
		}

//		Logger.logDbg("Creating a behavior that moves the robot to the first waypoint at " + firstWaypoint);
		Behavior b0 = new BehaviorGoToLocation("GoToLoc 0 (" + firstWaypointName + ")", navigatorCompassGPS, firstWaypoint, SPEED_TO_FIRST_WAYPOINT);
		addBehavior(b0);
		Logger.logDbg("Creating behavior " + b0);
			
		// Create a behavior that waits till it's time to start.
//		Logger.logDbg("Creating a behavior that forces the robot to wait at the first waypoint until the wait delay has elapsed.");
		Behavior waitBehavior = new BehaviorWaitTime("Wait at First Waypoint", b0, expConfig.getStartDelay() * 1000);
		addBehavior(waitBehavior);
		Logger.logDbg("Creating behavior " + waitBehavior);
		
		// For each round...
		Behavior prevBehavior = waitBehavior;
		int wpCount = 1;  // A counter for the total number of waypoints visited.  Starts at 1 b/c robot is already at first waypoint.
		for (int round = 0; round < expConfig.getNumRounds(); round++) {
			
			int wpIndx = (firstWaypointIndx + 1) % expConfig.getNumWaypoints();
			
			// Visit each waypoint in the patrol route...
			for (int wpCnt = 0; wpCnt < expConfig.getNumWaypoints(); wpCnt++) {
				Waypoint wp = expConfig.getWaypoint(wpIndx);
				
//				Logger.logDbg("Creating a behavior that goes to waypoint " + wpIndx + ", name = " + wp.getName() + ", loc = " + wp.getLoc() + ", speed = " + wp.getSpeed() + ", num waypoints visited = " + wpCount);
				
				Behavior currBehavior = new BehaviorGoToLocation("GoToLoc " + wpCount + " (" + wp.getName() + ")", navigatorCompassGPS, wp.getLoc(), wp.getSpeed());
				currBehavior.addPrerequisite(prevBehavior);
				addBehavior(currBehavior);
				prevBehavior = currBehavior;
				
				Logger.logDbg("Creating behavior " + currBehavior);
				
				wpIndx++; wpCount++;
				wpIndx %= expConfig.getNumWaypoints();
			}
			
		}
		
		// Get the home starting location
		Location homeLocation = getLocation();
		Behavior bHome = new BehaviorGoToLocation("GoToLoc " + wpCount + " (home)", navigatorCompassGPS, homeLocation, SPEED_TO_HOME);
		bHome.addPrerequisite(prevBehavior);
		addBehavior(bHome);
		
		Logger.logDbg("Creating behavior " + bHome);
	}

	/**
	 * This is only used for off-line time synchronization.  I.e., when analyzing the
	 * log files of multiple robots, the transmission time can be compared with the
	 * reception time to determine how far off the various clocks are.
	 */
	@Override
	public void beaconReceived(WiFiBeaconEvent be) {
		BeaconMsg beacon = (BeaconMsg)be.getBeacon();
		try {
			if (beacon.getSenderID() == RobotIPAssignments.getID()) {
				Logger.log("Ignoring my own beacon.");
			} else {
				String robotName = RobotIPAssignments.getName(beacon.getAddress());
				Logger.logDbg("Received beacon from " + robotName);
			}
		} catch (PharosException e) {
			Logger.logErr("While processing beacon, unable to determine robot's name based on its IP address (" 
					+ beacon.getAddress() + ")");
			e.printStackTrace();
		}
	}
}