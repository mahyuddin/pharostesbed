package pharoslabut.demo.mrpatrol2.daemons;

import pharoslabut.RobotIPAssignments;
import pharoslabut.beacon.WiFiBeaconBroadcaster;
import pharoslabut.beacon.WiFiBeaconEvent;
import pharoslabut.beacon.WiFiBeaconListener;
import pharoslabut.beacon.WiFiBeaconReceiver;
import pharoslabut.demo.mrpatrol2.config.ExpConfig;
import pharoslabut.demo.mrpatrol2.config.RobotExpSettings;
import pharoslabut.demo.mrpatrol2.msgs.BeaconMsg;
import pharoslabut.exceptions.PharosException;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.LineFollower;
import pharoslabut.navigate.Location;
import pharoslabut.navigate.MotionArbiter;
import pharoslabut.sensors.PathLocalizerOverheadMarkers;

/**
 * Handles the execution of an uncoordinated multi-robot patrol 2 (MRP2) experiment.
 * 
 * @author Chien-Liang Fok
 */
public class UncoordinatedOutdoorPatrolDaemon extends OutdoorPatrolDaemon implements Runnable, WiFiBeaconListener {
	
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
			WiFiBeaconBroadcaster wifiBeaconBroadcaster, WiFiBeaconReceiver wifiBeaconReceiver) 
	{
		super(expConfig, mobilityPlane, playerServerIP, playerServerPort);
		
		// Use beacons just so we can easily synchronize the clocks of everyone.
		Logger.logDbg("Adding self as beacon listener.");
		wifiBeaconReceiver.addBeaconListener(this);
		
		Logger.logDbg("Starting the beaconing.");
		RobotExpSettings settings = expConfig.getMySettings();
		
		if (settings != null) {
			wifiBeaconBroadcaster.setBeacon(new BeaconMsg(settings.getIP(), settings.getPort()));
			wifiBeaconBroadcaster.start(minPeriod, maxPeriod, txPower);
		
			new Thread(this).start();
		} else {
			Logger.logErr("Unable to get robot settings.");
			System.exit(1);
		}
	}
	
	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		
		Logger.logDbg("Thread starting at time " + startTime + "...");
		
		// Get the home starting location
		getHomeLocation();
		
		// Go to the starting locations
		// TODO
		
		// Wait till it's time to start.
		long currTime = System.currentTimeMillis();
		long timeDiff = currTime - startTime;
		if (timeDiff < expConfig.getStartDelay() * 1000) {
			Logger.logDbg("Pausing for " + timeDiff + " milliseconds before starting the patrol.");
			synchronized(this) {
				try {
					Thread.sleep(timeDiff);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		// For each round...
		for (int round = 0; round < expConfig.getNumRounds(); round++) {
			// Go to each waypoint along the route
			
		}
		
		// Go back to the home location.
		
//		if (!checkDone()) {
//			while (!checkDone()) {
//				
				// Go to each waypoint in the route!
//				synchronized(this) {
//					if (!numMarkersSeenUpdated) {
//						try {
//							this.wait();
//						} catch (InterruptedException e) {
//							Logger.logErr("Exception while waiting: [" + e.getMessage() + "]");
//							e.printStackTrace();
//						}
//					} else
//						numMarkersSeenUpdated = false;
//				}
//			}
//		} else {
//			Logger.log("WARNING: The experiment was completed even before it started!");
//			System.exit(0);
//		}
		
		Logger.log("Experiment completed!");
		//lineFollower.stop();
		Logger.log("Program exiting.");
		System.exit(0);
	}

	/**
	 * This is only used for time synchronization.
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