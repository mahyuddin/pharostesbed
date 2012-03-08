package pharoslabut.demo.mrpatrol2.daemons;

import java.util.Enumeration;
import java.util.Vector;

import pharoslabut.demo.mrpatrol2.Waypoint;
import pharoslabut.demo.mrpatrol2.behaviors.Behavior;
import pharoslabut.demo.mrpatrol2.behaviors.BehaviorAnticipatedUpdateBeacon;
import pharoslabut.demo.mrpatrol2.behaviors.BehaviorBeacon;
import pharoslabut.demo.mrpatrol2.behaviors.BehaviorCoordination;
import pharoslabut.demo.mrpatrol2.behaviors.BehaviorGoToLocation;
import pharoslabut.demo.mrpatrol2.behaviors.BehaviorTCPBeacon;
import pharoslabut.demo.mrpatrol2.behaviors.BehaviorUpdateBeacon;
import pharoslabut.demo.mrpatrol2.behaviors.BehaviorWaitTime;
import pharoslabut.demo.mrpatrol2.config.CoordinationStrength;
import pharoslabut.demo.mrpatrol2.config.CoordinationType;
import pharoslabut.demo.mrpatrol2.config.ExpConfig;
import pharoslabut.demo.mrpatrol2.config.RobotExpSettings;
import pharoslabut.demo.mrpatrol2.context.WorldModel;
import pharoslabut.io.Message;
import pharoslabut.io.MessageReceiver;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.Location;
import pharoslabut.navigate.MotionArbiter;

/**
 * Handles the execution of a coordinated multi-robot patrol 2 (MRP2) experiment.
 * 
 * @author Chien-Liang Fok
 */
public class CoordinatedOutdoorPatrolDaemon extends OutdoorPatrolDaemon {
	
	/**
	 * The constructor.
	 * 
	 * @param expConfig The experiment settings.
	 * @param mobilityPlane The type of mobility plane being used.
	 * @param playerServerIP The IP address of the player server.
	 * @param playerServerPort The TCP port of the player server.
	 * @param mCastAddress The multi-cast address for transmitting beacons.
	 * @param mCastPort The multi-cast port for transmitting beacons.
	 * @param coordStrength The type of coordination to use.
	 */
	public CoordinatedOutdoorPatrolDaemon(ExpConfig expConfig, MotionArbiter.MotionType mobilityPlane, 
			String playerServerIP, int playerServerPort,
			int serverPort,
			String mCastAddress, int mCastPort,
			CoordinationStrength coordStrength) 
	{
		super(expConfig, mobilityPlane, playerServerIP, playerServerPort, serverPort, mCastAddress, mCastPort);
		createBehaviors(coordStrength);
	}
	
	/**
	 * Generates all of the behaviors used in this patrol experiment.
	 * 
	 * Three behaviors start immediately:
	 *   1. A GoToLocation behavior that moves the robot to the first waypoint.
	 *   2. A WaitTime behavior that makes the robot wait at the first waypoint until 
	 *      a certain amount of time has passed since the experiment began.  This allows
	 *      all of the robots to reach their first waypoints prior to the beginning of
	 *      the experiment.
	 *   3. A Beacon behavior that starts the beaconing process.
	 *   
	 * After the initial GoToLocation and WaitTime behaviors complete, a sequence of behaviors are run
	 * that move the robot to each waypoint along the patrol route.  Upon arriving at each waypoint,
	 * a coordination behavior is run that ensures the robots remain synchronized.
	 * The entire route is patrolled a certain number of times.
	 * 
	 * Once the patrol is done, a final behavior moves the robot back to the home location.
	 * 
	 * @param coordType The type of coordination to use.
	 */
	private void createBehaviors(CoordinationStrength coordStrength) {
		
		Logger.logDbg("Creating the behaviors used in the experiment.");
		
		RobotExpSettings mySettings = expConfig.getMySettings();
		
		if (mySettings == null) {
			Logger.logErr("Unable to get robot settings.");
			System.exit(1);
		}
		
		WorldModel worldModel = new WorldModel(expConfig);
		
		String firstWaypointName = mySettings.getFirstWaypoint();
		int firstWaypointIndx = expConfig.getWaypointIndex(firstWaypointName);
		
		// Create a behavior that moves the robot to the starting location.
		// Note that this behavior has no prerequisites so it should start immediately when the experiment begins.
		Location firstWaypoint = expConfig.getWaypoint(firstWaypointName);
		if (firstWaypoint == null) {
			Logger.logErr("Unable to get first waypoint.");
			System.exit(1);
		}
		BehaviorGoToLocation b0 = new BehaviorGoToLocation("GoToLoc_0_" + firstWaypointName, navigatorCompassGPS, firstWaypoint, SPEED_TO_FIRST_WAYPOINT);
		addBehavior(b0);
		Logger.logDbg("Creating behavior " + b0);
			
		// Create a behavior that waits till it's time to start.  
		// Note that this behavior has no prerequisites so it should start immediately when the experiment begins.
		Behavior waitBehavior = new BehaviorWaitTime("Wait_at_First_Waypoint", expConfig.getStartDelay() * 1000);
		addBehavior(waitBehavior);
		Logger.logDbg("Creating behavior " + waitBehavior);
		
		// Create a behavior that starts the beaconing.
		// Note that this behavior has no prerequisites so it should start immediately when the experiment begins.
		BehaviorBeacon beaconBehavior = new BehaviorBeacon("BeaconUDP", mCastAddress, mCastPort, serverPort, worldModel);
		addBehavior(beaconBehavior);
		
		// Create a behavior that transmits beacons using TCP
		BehaviorTCPBeacon beaconTCPBehavior = new BehaviorTCPBeacon("BeaconTCP", serverPort, worldModel, expConfig);
		addMsgRcvr(beaconTCPBehavior);  // allow this behavior to receive TCP messages.
		addBehavior(beaconTCPBehavior);
		
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

				// Add a GoToLocation behavior to go to the next waypoint.
				BehaviorGoToLocation behaviorGoToLoc = new BehaviorGoToLocation("GoToLoc_" + wpCount + "_" + wp.getName(), navigatorCompassGPS, wp.getLoc(), wp.getSpeed());
				behaviorGoToLoc.addPrerequisite(prevBehavior);
				addBehavior(behaviorGoToLoc);
				Logger.logDbg("Creating behavior " + behaviorGoToLoc);
				
				// Add coordination behavior after reaching each waypoint.
				BehaviorCoordination behaviorCoordination = new BehaviorCoordination("Coordination_" + wpCount, worldModel, wpCount, coordStrength);
				behaviorCoordination.addPrerequisite(behaviorGoToLoc); // the prerequisite is to reach the waypoint
				addBehavior(behaviorCoordination);
				Logger.logDbg("Creating behavior " + behaviorCoordination);
				
				// Add a behavior that updates the number of waypoints traversed in the beacon
				if (expConfig.getCoordinationType() == CoordinationType.PASSIVE) {
					
					
					BehaviorUpdateBeacon updateBeaconBehavior = new BehaviorUpdateBeacon("UpdateBeacon_" + wpCount, wpCount);
					updateBeaconBehavior.addBehaviorToUpdate(beaconBehavior);
					updateBeaconBehavior.addBehaviorToUpdate(beaconTCPBehavior);
					updateBeaconBehavior.addPrerequisite(behaviorGoToLoc); // the prerequisite is to reach the waypoint
					addBehavior(updateBeaconBehavior);
					Logger.logDbg("Creating behavior " + updateBeaconBehavior);
				} else if (expConfig.getCoordinationType() == CoordinationType.ANTICIPATED_FIXED) {
					
					BehaviorAnticipatedUpdateBeacon updateBeaconBehavior = new BehaviorAnticipatedUpdateBeacon("UpdateBeacon_" + wpCount, wpCount, 
							behaviorGoToLoc, expConfig.getAheadTime());
					updateBeaconBehavior.addBehaviorToUpdate(beaconBehavior);
					updateBeaconBehavior.addBehaviorToUpdate(beaconTCPBehavior);
					updateBeaconBehavior.addPrerequisite(prevBehavior); // the prerequisite is to reach the waypoint
//					updateBeaconBehavior.addCurrentlyRunningBehavior(behaviorGoToLoc);
					addBehavior(updateBeaconBehavior);
					Logger.logDbg("Creating behavior " + updateBeaconBehavior);
				}
				
				prevBehavior = behaviorCoordination;  // The next behavior should only begin after the coordination behavior is complete.
				
				wpIndx++; wpCount++;
				wpIndx %= expConfig.getNumWaypoints();
			}
			
		}
		
		// Get the home starting location
		Location homeLocation = getLocation();
		Behavior bHome = new BehaviorGoToLocation("GoToLoc_" + wpCount + "_home", navigatorCompassGPS, homeLocation, SPEED_TO_HOME);
		bHome.addPrerequisite(prevBehavior);
		addBehavior(bHome);
		
		// Make the beacon behavior stop after the robot arrives back home.
		beaconBehavior.addDependency(bHome);
		beaconTCPBehavior.addDependency(bHome);
		
		Logger.logDbg("Creating behavior " + bHome);
	}
}