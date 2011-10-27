package pharoslabut.demo.indoorMRPatrol;

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
 * Handles the execution of an uncoordinated robot patrol experiment.
 * 
 * @author Chien-Liang Fok
 *
 */
public class UncoordinatedPatrolDaemon extends PatrolDaemon implements Runnable, WiFiBeaconListener {
	
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
	public UncoordinatedPatrolDaemon(LoadExpSettingsMsg settings, LineFollower lineFollower, 
			PathLocalizerOverheadMarkers pathLocalizer, int numRounds,
			WiFiBeaconBroadcaster wifiBeaconBroadcaster, WiFiBeaconReceiver wifiBeaconReceiver) 
	{
		super(settings, numRounds, lineFollower, pathLocalizer);
		
		// Use beacons just so we can easily synchronize the clocks of everyone.
		Logger.logDbg("Adding self as beacon listener.");
		wifiBeaconReceiver.addBeaconListener(this);
		
		Logger.logDbg("Starting the beaconing.");
		long minPeriod = 1000;
		long maxPeriod = 1500;
		short txPower = (short)31;
		wifiBeaconBroadcaster.start(minPeriod, maxPeriod, txPower);
		
		new Thread(this).start();
	}
	
	@Override
	public void run() {
		Logger.logDbg("Thread starting...");
		
		if (!checkDone()) {
			Logger.logDbg("Starting the line follower.");
			lineFollower.start();
			
			while (!checkDone()) {
				synchronized(this) {
					if (!numMarkersSeenUpdated) {
						try {
							this.wait();
						} catch (InterruptedException e) {
							Logger.logErr("Exception while waiting: [" + e.getMessage() + "]");
							e.printStackTrace();
						}
					} else
						numMarkersSeenUpdated = false;
				}
			}
		} else {
			Logger.log("WARNING: The experiment was completed even before it started!");
			System.exit(0);
		}
		
		Logger.log("Experiment completed!");
		lineFollower.stop();
		Logger.log("Program exiting.");
		System.exit(0);
	}

	/**
	 * This is only used for time synchronization.
	 */
	@Override
	public void beaconReceived(WiFiBeaconEvent be) {
		IndoorMRPatrolBeacon beacon = (IndoorMRPatrolBeacon)be.getBeacon();
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
