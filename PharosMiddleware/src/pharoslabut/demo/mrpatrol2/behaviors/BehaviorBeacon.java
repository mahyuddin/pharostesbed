package pharoslabut.demo.mrpatrol2.behaviors;

import java.net.InetAddress;
import java.net.UnknownHostException;

import pharoslabut.RobotIPAssignments;
import pharoslabut.beacon.WiFiBeaconBroadcaster;
import pharoslabut.beacon.WiFiBeaconEvent;
import pharoslabut.beacon.WiFiBeaconListener;
import pharoslabut.beacon.WiFiBeaconReceiver;
import pharoslabut.demo.mrpatrol2.msgs.BeaconMsg;
import pharoslabut.exceptions.PharosException;
import pharoslabut.logger.Logger;

/**
 * Implements a behavior that emits WiFi beacons.
 * 
 * @author Chien-Liang Fok
 */
public class BehaviorBeacon extends Behavior implements WiFiBeaconListener {
	
	public static final long MIN_BEACON_PERIOD = 1000;
	public static final long MAX_BEACON_PERIOD = 2000;
	
	// Components for sending and receiving WiFi beacons
	private WiFiBeaconBroadcaster wifiBeaconBroadcaster;
	
	private WiFiBeaconReceiver wifiBeaconReceiver;
	
	/**
	 * The beacon to transmit.
	 */
	private BeaconMsg beacon;
	
	/**
	 * Whether this behavior is done.
	 */
	private boolean isDone = false;
	
	/**
	 * The constructor.
	 * 
	 * @param name The name of the behavior.
	 * @param mcastGroupAddress the multicast address to listen in on.
     * @param mcastport the multicast port to listen in on.
     * @param serverPort The port number that the server is listening to on the local robot.  This information is included in the beacons.
	 */
	public BehaviorBeacon(String name, String mCastAddress, int mCastPort, int serverPort) {
		super(name);
		
		// Create the components that send and receive beacons.
		initWiFiBeacons(mCastAddress, mCastPort);
		
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
	
	/**
     * Initializes the components that transmit and receive beacons.  This does not
     * actually start transmitting beacons.  To start transmitting beacons, a beacon
     * must be set in the WiFiBeaconBroadcaster and WiFiBeaconBroadcaster.start() must be called.
     */
	private void initWiFiBeacons(String mCastAddress, int mCastPort) {
		// Obtain the multicast address		
		InetAddress mCastGroupAddress = null;
		try {
			mCastGroupAddress = InetAddress.getByName(mCastAddress);
		} catch (UnknownHostException uhe) {
			Logger.logErr("Problems getting multicast address");
			uhe.printStackTrace();
			System.exit(1);
		}
		
		String pharosIP = null;
		try {
			pharosIP = RobotIPAssignments.getAdHocIP();
		} catch (PharosException e1) {
			Logger.logErr("Unable to get ad hoc IP address: " + e1.getMessage());
			e1.printStackTrace();
			System.exit(1);
		}
		
		// Obtain the correct network interface (ad hoc wireless).
		String pharosNI = null;
		try {
			pharosNI = RobotIPAssignments.getAdHocNetworkInterface();
		} catch (PharosException e1) {
			Logger.logErr("Unable to get ad hoc network interface: " + e1.getMessage());
			e1.printStackTrace();
			System.exit(1);
		}
		
		if (pharosIP == null || pharosNI == null) {
			Logger.logErr("Unable to get pharos IP or pharos network interface...");
			System.exit(1);
		}
		
		wifiBeaconReceiver = new WiFiBeaconReceiver(mCastAddress, mCastPort, pharosNI);
        wifiBeaconBroadcaster = new WiFiBeaconBroadcaster(mCastGroupAddress, pharosIP, mCastPort);
	}

	@Override
	public void run() {
		
		// Start receiving beacons
		wifiBeaconReceiver.addBeaconListener(this);
		wifiBeaconReceiver.start();  
		
		// Start transmitting beacons
		wifiBeaconBroadcaster.setBeacon(beacon);
		wifiBeaconBroadcaster.start(MIN_BEACON_PERIOD, MAX_BEACON_PERIOD);
		
		while (!isDone) {
			if (!dependenciesMet()) {
				Logger.logDbg("Dependencies not met, behavior is done.");
				stop();
			}
		}
	}

	@Override
	public boolean isDone() {
		return isDone;
	}

	@Override
	public void stop() {
		wifiBeaconReceiver.stop();
		wifiBeaconBroadcaster.stop();
		isDone = true;
	}

	/**
	 * This is called whenever a beacon is received.
	 * 
	 * It can be used for off-line time synchronization, i.e., when analyzing the
	 * log files of multiple robots, the transmission time can be compared with the
	 * reception time to determine how far off the various clocks are.
	 */
	@Override
	public void beaconReceived(WiFiBeaconEvent be) {
		BeaconMsg beacon = (BeaconMsg)be.getBeacon();
		try {
			if (beacon.getSenderID() == RobotIPAssignments.getID()) {
				Logger.logDbg("Ignoring my own beacon.");
			} else {
				String robotName = RobotIPAssignments.getName(beacon.getAddress());
				long deltaTime = System.currentTimeMillis() - beacon.getTimestamp();
				Logger.log("Received beacon from " + robotName + ", latency = " + deltaTime + ", beacon = " + beacon);
			}
		} catch (PharosException e) {
			Logger.logErr("While processing beacon, unable to determine robot's name based on its IP address (" 
					+ beacon.getAddress() + ")");
			e.printStackTrace();
		}
	}

	/**
	 * @return A string representation of this class.
	 */
	@Override
	public String toString() {
		return "BehaviorBeacon " + super.toString() + ", beacon = " + beacon;
	}
}
