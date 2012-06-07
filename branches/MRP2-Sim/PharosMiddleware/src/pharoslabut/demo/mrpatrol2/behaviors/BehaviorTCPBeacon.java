package pharoslabut.demo.mrpatrol2.behaviors;

import java.net.InetAddress;
import java.util.Iterator;

import pharoslabut.RobotIPAssignments;
import pharoslabut.demo.mrpatrol2.config.ExpConfig;
import pharoslabut.demo.mrpatrol2.config.RobotExpSettings;
import pharoslabut.demo.mrpatrol2.context.Teammate;
import pharoslabut.demo.mrpatrol2.context.WorldModel;
import pharoslabut.demo.mrpatrol2.msgs.BeaconMsg;
import pharoslabut.exceptions.PharosException;
import pharoslabut.io.Message;
import pharoslabut.io.MessageReceiver;
import pharoslabut.io.TCPMessageSender;
import pharoslabut.logger.Logger;

/**
 * Implements a behavior that periodically transmits state messages
 * to each teammate.
 * 
 * @author Chien-Liang Fok
 */
public class BehaviorTCPBeacon extends Behavior implements UpdateBeaconBehavior, MessageReceiver {

	public static final long MIN_BEACON_PERIOD = 1000;
	public static final long MAX_BEACON_PERIOD = 2000;
	
	/**
	 * Whether this behavior is done.
	 */
	private boolean isDone = false;
	
	/**
	 * The world model to update.  It is updated using information
	 * contained within incoming beacons.
	 */
	private WorldModel worldModel = null;
	
	/**
	 * The beacon to transmit.
	 */
	private BeaconMsg beacon;
	
	/**
	 * The experiment configuration.
	 */
	private ExpConfig expConfig;
	
	/**
	 * A reference to the message sender.
	 */
	private TCPMessageSender msgSender = TCPMessageSender.getSender();
	
	/**
	 * The constructor.  This is used by the coordinated outdoor patrol daemon.
	 * 
	 * @param name The name of the behavior.
     * @param serverPort The port number that the server is listening to on the local robot.  This information is included in the beacons.
     * @param worldModel The world model to update with information contained within the beacons.
     * @param expConfig The experiment configuration.
	 */
	public BehaviorTCPBeacon(String name, int serverPort, WorldModel worldModel, ExpConfig expConfig) {
		super(name);
		this.worldModel = worldModel;
		this.expConfig = expConfig;
		
		// Create the beacon.
		try {
			String pharosIP = RobotIPAssignments.getAdHocIP();
			beacon = new BeaconMsg(InetAddress.getByName(pharosIP), serverPort);
		} catch (Exception e) {
			Logger.logErr("Unable to get IP address: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	@Override
	public void newMessage(Message msg) {
		if (msg instanceof BeaconMsg) {
			BeaconMsg beacon = (BeaconMsg)msg;
			try {
				if (beacon.getSenderID() == RobotIPAssignments.getID()) {
					Logger.logDbg("Ignoring my own beacon.");
				} else {
					String robotName = RobotIPAssignments.getName(beacon.getAddress());
					long deltaTime = System.currentTimeMillis() - beacon.getTimestamp();
					Logger.log("Received beacon from " + robotName + ", latency = " + deltaTime + ", beacon = " + beacon);
					
					if (worldModel != null) {
						worldModel.updateTeammate(robotName, beacon.getNumWaypointsTraversed(), beacon.getTimestamp());
						Logger.logDbg("Updated world model: " + worldModel);
					}
				}
			} catch (PharosException e) {
				Logger.logErr("While processing beacon, unable to determine robot's name based on its IP address (" 
						+ beacon.getAddress() + ")");
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Sets the number of waypoints traversed within the beacon.
	 * 
	 * @param numWaypointsTraversed the number of waypoints traversed.
	 */
	@Override
	public void setWaypointsTraversed(int numWaypointsTraversed) {
		beacon.setWaypointsTraversed(numWaypointsTraversed);
	}

	@Override
	public void run() {
		while (!isDone) {
			
			StringBuffer sb = new StringBuffer("Transmitting beacon to each teammate individually:");
			
			// For each neighbor, transmit a state message to it.
			Iterator<Teammate> teammates = worldModel.getTeammates().iterator();
			while (teammates.hasNext()) {
				final Teammate teammate = teammates.next();
				
				sb.append("\n\t" + teammate.getName());
				
				// Spawn a new thread for each transmission to prevent a broken network
				// connection from halting the transmission to other teammates.
				new Thread(new Runnable() {
					public void run() {
						RobotExpSettings teammateSettings = expConfig.getTeamateSettings(teammate.getName());
						try {
							beacon.updateTimestamp();
							msgSender.sendMessage(teammateSettings.getIP(), teammateSettings.getPort(), beacon);
						} catch (PharosException e) {
							e.printStackTrace();
							Logger.logErr("Unable to send TCP beacon to " + teammate.getName());
						}
					}
				}).start();
			}
			
			Logger.logDbg(sb.toString());
			
			if (!dependenciesMet()) {
				Logger.logDbg("Dependencies not met! stopping...");
				stop();
			}
			
			if (!isDone()) {
				try {
					synchronized(this) {
						long delayPeriod = pharoslabut.util.Random.randPeriod(MIN_BEACON_PERIOD, MAX_BEACON_PERIOD);
						Logger.logDbg("Time till next transmission = " + delayPeriod);
						wait(delayPeriod);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public boolean isDone() {
		return isDone;
	}

	@Override
	public void stop() {
		isDone = true;
	}
}
