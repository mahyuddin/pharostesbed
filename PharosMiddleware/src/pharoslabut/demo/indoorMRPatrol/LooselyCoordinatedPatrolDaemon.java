package pharoslabut.demo.indoorMRPatrol;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import pharoslabut.RobotIPAssignments;
import pharoslabut.beacon.WiFiBeaconBroadcaster;
import pharoslabut.beacon.WiFiBeaconEvent;
import pharoslabut.beacon.WiFiBeaconListener;
import pharoslabut.beacon.WiFiBeaconReceiver;
import pharoslabut.exceptions.PharosException;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.LineFollower;
import pharoslabut.sensors.PathLocalizerOverheadMarkers;

/**
 * Handles the execution of an loosely coordinated robot patrol experiment.
 * 
 * @author Chien-Liang Fok
 */
public class LooselyCoordinatedPatrolDaemon extends PatrolDaemon implements Runnable, WiFiBeaconListener {
	
	/**
	 * The maximum time in milliseconds that can pass before a teammate is 
	 * considered disconnected.
	 */
	public static final long DISCONNECTION_THRESHOLD = 10000;
	
	/**
	 * Records the local robot's perspective of the team's state.
	 * The key is the robot name, while the value is the robot's state.
	 */
	private HashMap<String, RobotState> teamState = new HashMap<String, RobotState>();
	
	/**
	 * The beacon broadcaster.
	 */
	private WiFiBeaconBroadcaster wifiBeaconBroadcaster;
	
	/**
	 * The constructor.
	 * 
	 * @param settings The experiment settings.
	 * @param lineFollower The line follower.
	 * @param pathLocalizer The path localizer.
	 * @param numRounds The number of rounds to patrol.
	 * @param wifiBeaconBroadcaster The beacon broadcaster.
	 * @param wifiBeaconReceiver The beacon receiver.
	 */
	public LooselyCoordinatedPatrolDaemon(LoadExpSettingsMsg settings, LineFollower lineFollower, 
			PathLocalizerOverheadMarkers pathLocalizer, int numRounds,
			WiFiBeaconBroadcaster wifiBeaconBroadcaster, WiFiBeaconReceiver wifiBeaconReceiver) 
	{
		super(settings.getNumMarkers(), numRounds, lineFollower, pathLocalizer);
		this.wifiBeaconBroadcaster = wifiBeaconBroadcaster;
		wifiBeaconReceiver.addBeaconListener(this);
		
		String robotName = null;
		try {
			robotName = pharoslabut.RobotIPAssignments.getName();
			Logger.log("Robot name: " + robotName);
		} catch (PharosException e) {
			e.printStackTrace();
			Logger.logErr("Unable to get robot name.");
			System.exit(1);
		}
		
		Logger.log("Creating this robot's world view.");
		Vector<RobotExpSettings> team = settings.getTeam();
		for (int i=0; i < team.size(); i++) {
			RobotExpSettings currRobot = team.get(i);
			
			// Do not put self in world view.
			if (!currRobot.getName().equals(robotName)) {
				Logger.logDbg("Adding robot " + currRobot.getName() + " to world view.");
				teamState.put(currRobot.getName(), 
					new RobotState(currRobot.getName(), currRobot.getIP(), currRobot.getStartingLoc()));
			}
		}
		
		Logger.log("Starting the beaconing.");
		long minPeriod = 1000;
		long maxPeriod = 1500;
		short txPower = (short)31;
		wifiBeaconBroadcaster.start(minPeriod, maxPeriod, txPower);
		
		new Thread(this).start();
	}
	
	/**
	 * This is called whenever a beacon is received.
	 */
	@Override
	public void beaconReceived(WiFiBeaconEvent be) {
		IndoorMRPatrolBeacon beacon = (IndoorMRPatrolBeacon)be.getBeacon();
		try {
			String robotName = RobotIPAssignments.getName(beacon.getAddress());
			Logger.logDbg("Received beacon from " + robotName);
			
			RobotState robotState = teamState.get(robotName);
			robotState.setLastHeardTimeStamp();
			robotState.setNumMarkersTraversed(beacon.getNumMarkersTraversed());
			
			// Since the world view has changed, notify all threads waiting on this change
			Logger.logDbg("Notifying all threads waiting on team state changes.");
			synchronized(teamState) {
				teamState.notifyAll();
			}
		} catch (PharosException e) {
			Logger.logErr("While processing beacon, unable to determine robot's name based on its IP address (" 
					+ beacon.getAddress() + ")");
			e.printStackTrace();
		}
	}
	
	/**
	 * Determine whether the team is synchronized.  The team is synchronized when all of the
	 * team mates that are within range have traversed at least the same number of markers as
	 * the local node.
	 * 
	 * @return true if the team is synchronized with the local robot.
	 */
	private boolean isTeamSynced() {
		boolean result = true;
		
		StringBuffer sb = new StringBuffer("Checking whether the team is in sync, numMarkersSeen = " 
				+ numMarkersSeen + "...");
		Iterator<RobotState> itr = teamState.values().iterator();
		while (itr.hasNext()) {
			RobotState currState = itr.next();
			sb.append("\n\tChecking robot: " + currState + "...");
			
			// Ignore the teammate if he has disconnected wirelessly.
			if (currState.getAge() > DISCONNECTION_THRESHOLD) {
				sb.append("disconnected (ignoring)");
			} else {
				if (currState.getNumMarkersTraversed() < numMarkersSeen) {
					result = false;
					sb.append(" not synched!");
				} else {
					sb.append("synched!");
				}
			}
		}
		
		Logger.logDbg(sb.toString());
		return result;
	}
	
	/**
	 * Forces the calling thread to wait until the team is synched.
	 */
	private void waitTillLooselySynced() {
		
		// While the team is not in sync, wait for the team to become in sync.
		while (!isTeamSynced()) {
			synchronized(teamState) {
				try {
					teamState.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Updates the local robot's beacon to contain the most recent number of markers seen.
	 */
	private void updateBeacon() {
		Logger.log("Updating beacon");
		IndoorMRPatrolBeacon beacon = (IndoorMRPatrolBeacon)wifiBeaconBroadcaster.getBeacon();
		beacon.setNumMarkersTraversed(numMarkersSeen);
		Logger.log("New beacon: " + beacon);
	}
	
	@Override
	public void run() {
		Logger.logDbg("Thread starting...");
		
		if (!checkDone()) {
			Logger.logDbg("Starting the line follower.");
			lineFollower.start();
			
			while (!checkDone()) {
				boolean numMarkersSeenUpdated;
				
				synchronized(this) {
					numMarkersSeenUpdated = this.numMarkersSeenUpdated;
					this.numMarkersSeenUpdated = false;
				}
				
				if (numMarkersSeenUpdated) {
					
					Logger.log("Reached marker " + numMarkersSeen);
					updateBeacon();
					
					Logger.log("Checking if all teammates are loosely synced.");
					if (!isTeamSynced()) {
						Logger.log("Team not synced, waiting at this marker until team is synced.");
						lineFollower.stop();
						waitTillLooselySynced();
						lineFollower.start();
					} else {
						Logger.log("Team synched, continuing.");
					}
				}
				
				// Wait for the next overhead marker event.
				synchronized(this) {
					if (!this.numMarkersSeenUpdated) {
						try {
							this.wait();
						} catch (InterruptedException e) {
							Logger.logErr("Exception while waiting: [" + e.getMessage() + "]");
							e.printStackTrace();
						}
					}
				}
			}
			
			Logger.log("Experiment completed!");  // Shall we synchronize one last time?
			lineFollower.stop();
			System.exit(0);
			
		} else {
			Logger.log("WARNING: The experiment was completed even before it started!");
			System.exit(0);
		}
	}
}
