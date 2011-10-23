package pharoslabut.demo.autoIntersection.clientDaemons.V2VSerial;

import java.net.InetAddress;
import java.net.UnknownHostException;

import pharoslabut.RobotIPAssignments;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.*;
import pharoslabut.sensors.Position2DBuffer;
import pharoslabut.beacon.BeaconBroadcaster;
import pharoslabut.beacon.WiFiBeacon;
import pharoslabut.beacon.WiFiBeaconBroadcaster;
import pharoslabut.beacon.WiFiBeaconEvent;
import pharoslabut.beacon.WiFiBeaconListener;
import pharoslabut.beacon.WiFiBeaconReceiver;
import pharoslabut.demo.autoIntersection.clientDaemons.ClientDaemon;
import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionDetector;
import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionEvent;
import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionEventListener;
import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionEventType;
import pharoslabut.demo.autoIntersection.msgs.*;
import pharoslabut.exceptions.PharosException;

/**
 * Implements an ad hoc form of intersection management where each robot decides
 * independently whether it is safe to traverse the intersection.  In this daemon,
 * only one robot may cross the intersection at a time.
 * 
 * @author Chien-Liang Fok
 */
public class V2VSerialClientDaemon extends ClientDaemon implements IntersectionEventListener, 
	Runnable, WiFiBeaconListener 
{
	
	/**
	 * The minimum beacon period in milliseconds.
	 */
	public static final int MIN_BEACON_PERIOD = 100;
	
	/**
	 * The maximum beacon period in milliseconds.
	 */
	public static final int MAX_BEACON_PERIOD = 1000;
	
	/**
	 * The maximum number of consecutive beacons that can be lost before concluding that a
	 * node is disconnected.
	 */
	public static final int MAX_CONSECUTIVE_LOST_BEACONS = 5;
	
	/**
	 * The minimum amount of time that a node must think it's safe to cross the intersection
	 * before actually granting itself access to the intersection.
	 */
	public static final int MIN_SAFE_DURATION = 2100;
	
	/**
	 * Whether the this daemon is running.
	 */
	private boolean isRunning = false;
	
	/**
	 * Whether access to the intersection was granted.
	 */
	private boolean accessGranted = false;
	
	/**
	 * Whether it is safe for the local node to cross the intersection.
	 */
	private boolean isSafeToCross = false;
	
	/**
	 * The time since we concluded that it is safe to cross the intersection.
	 */
	private long safeTimestamp = -1;
	
	/**
	 * The beacon broadcaster.
	 */
	private BeaconBroadcaster beaconBroadcaster;
	
	/**
	 * The WiFi multicast group address.
	 */
    protected String mCastAddress = "230.1.2.3"; // TODO: Make this adjustable.
    
    /**
	 * The WiFi multicast port.
	 */
    protected int mCastPort = 6000;  // TODO: Make this adjustable.
    
    /**
     * The beacon that this node is periodically broadcasting.
     */
    protected V2VSerialBeacon beacon;
    
    /**
     * The beacon receiver.
     */
    private WiFiBeaconReceiver beaconReceiver;
    
    /**
     * The neighbor list.
     */
    protected NeighborList nbrList;
    
    /**
	 * The constructor.
	 * 
//	 * @param port The local port on which this client should listen.
	 * @param lineFollower The line follower.
	 * @param intersectionDetector The intersection detector.
	 * @param entryPointID The entry point ID.
	 * @param exitPointID The exit point ID.
	 */
	public V2VSerialClientDaemon(//int port,
			LineFollower lineFollower, IntersectionDetector intersectionDetector, Position2DBuffer pos2DBuffer,
			String entryPointID, String exitPointID) 
	{
		super(lineFollower, intersectionDetector, pos2DBuffer, entryPointID, exitPointID);
//		this.port = port;
		
		// Obtain the multicast address		
		InetAddress mCastGroupAddress = null;
		try {
			mCastGroupAddress = InetAddress.getByName(mCastAddress);
		} catch (UnknownHostException uhe) {
			Logger.logErr("Problems getting multicast address");
			uhe.printStackTrace();
			System.exit(1);
		}
		
		// Obtain the IP address.
		String pharosIP = null;
		try {
			pharosIP = RobotIPAssignments.getAdHocIP();
		} catch (PharosException e) {
			Logger.logErr("Unable to get ad hoc IP address: " + e.toString());
			e.printStackTrace();
			System.exit(1);
		}
		
		// Obtain the network interface.
		String pharosNI = null;
		try {
			pharosNI = RobotIPAssignments.getAdHocNetworkInterface();
		} catch (PharosException e) {
			Logger.logErr("Unable to get ad hoc network interface: " + e.toString());
			e.printStackTrace();
			System.exit(1);
		}
		
		createBeacon(pharosIP);
		
		createNeighborList();
		
		Logger.log("Creating the beacon broadcaster.");
		beaconBroadcaster = new WiFiBeaconBroadcaster(mCastGroupAddress, pharosIP, 
				mCastPort, beacon);
		
		Logger.log("Creating the beacon receiver.");
		beaconReceiver = new WiFiBeaconReceiver(mCastAddress, mCastPort, pharosNI);
		beaconReceiver.addBeaconListener(this);
	}

	protected void createBeacon(String pharosIP) {
		Logger.log("Creating the beacon.");
		try {
			beacon = new V2VSerialBeacon(InetAddress.getByName(pharosIP), mCastPort);
		} catch (UnknownHostException e) {
			Logger.logErr("Unable to create the beacon: " + e.toString());
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	protected void createNeighborList() {
		 nbrList = new NeighborList();
	}
	
	/**
	 * This is called by AutoIntersectionClient when the start experiment message is received.
	 */
	@Override
	public synchronized void start() {
		if (!isRunning) {
			isRunning = true; 
			
			Logger.log("Registering self as listener to intersection events.");
			intersectionDetector.addIntersectionEventListener(this);
			
			Logger.log("Starting beacon receiver.");
			beaconReceiver.start();
			
			Logger.log("Starting beacon broadcaster with min period " + MIN_BEACON_PERIOD + " and max period " + MAX_BEACON_PERIOD);
			beaconBroadcaster.start(MIN_BEACON_PERIOD, MAX_BEACON_PERIOD);
			
			Logger.log("Starting a thread for this daemon.");
			new Thread(this).start();
		} else {
			Logger.logErr("Trying to start twice!");
		}
	}

	/**
	 * This is called by the AutoIntersectionClient whenever a singlecast message 
	 * is received.  It is not used in the ad hoc scheme that's implemented in this
	 * daemon since all coordination is done through beacons.
	 */
	@Override
	public void messageReceived(AutoIntersectionMsg msg) {
		Logger.logErr("Unexpected message: " + msg);
	}
	
	/**
	 * This is called when a new intersection event occurs.
	 */
	@Override
	public void newIntersectionEvent(IntersectionEvent lfe) {
		if (isRunning) {
			
			switch(lfe.getType()) {
			
			case APPROACHING:
				Logger.log("Vehicle is approaching intersection");
				currState = IntersectionEventType.APPROACHING;
				
				beacon.setVehicleStatus(VehicleStatus.REQUESTING);
				break;
			
			case ENTERING:
				if (!accessGranted) {
					Logger.log("Vehicle is entering intersection but access not granted.  Stopping robot.");
					lineFollower.pause();
				} else {
					Logger.log("Vehicle is entering intersection (access was granted).");
					beacon.setVehicleStatus(VehicleStatus.CROSSING);
				}
				currState = IntersectionEventType.ENTERING;
				break;
			
			case EXITING:
				Logger.log("Vehicle is exiting intersection.");
				currState = IntersectionEventType.EXITING;
				beacon.setVehicleStatus(VehicleStatus.EXITING);
				
				Logger.log("Moving one more second to pass the exiting marker.");
				
				synchronized(this) {
					try {
						this.wait(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				Logger.log("Pausing the line follower.");
				lineFollower.pause();
				
				// Keep the client running to ensure the final exit message is sent to the server.
//				isRunning = false;
				break;
			case ERROR:
				Logger.logErr("Received error from line follower!  Aborting demo.");
				lineFollower.stop(); // There was an error, stop!
			default:
				Logger.log("Discarding unexpected intersection event: " + lfe);
			}
		} else
			Logger.log("Ignoring event because not running: " + lfe);
	}

	/**
	 * This is called whenever a beacon is received.
	 */
	@Override
	public void beaconReceived(WiFiBeaconEvent be) {
		Logger.log("Received beacon: " + be);
		WiFiBeacon beacon = be.getBeacon();
		if (beacon instanceof V2VSerialBeacon) {
			nbrList.update((V2VSerialBeacon)beacon);
		} else
			Logger.logErr("Received an unexpected beacon: " + beacon);
	}

	
	@Override
	public void run() {
		Logger.log("Thread starting...");
		
		Logger.log("Starting the line follower.");
		lineFollower.start();
		
		while(isRunning) {
			
			nbrList.flushOldEntries(MAX_BEACON_PERIOD * MAX_CONSECUTIVE_LOST_BEACONS);
			
			if (currState == IntersectionEventType.APPROACHING || 
					currState == IntersectionEventType.ENTERING)
			{
				
				if (!accessGranted) {

					// We want to cross the intersection but we are not sure if it is safe.
					long currTime = System.currentTimeMillis();
					
					
					SafeState isSafeNow = nbrList.isSafeToCross();
					
					if (isSafeNow.isSafe()) {
						
						if (System.currentTimeMillis() > isSafeNow.getSafeTime()) {
							Logger.log("It is immediately safe to cross!  Granting self permission to cross intersection!");
							accessGranted = true;
							beacon.setVehicleStatus(VehicleStatus.CROSSING);
							lineFollower.unpause();
						} else {
							if (!isSafeToCross) {
								Logger.log("It might be safe to cross, currTime = " + currTime);
								isSafeToCross = true;
								safeTimestamp = currTime;
							} else {
								// The robot previously concluded that it was safe to cross the intersection.
								// See if enough time has passed to really be sure it is safe.
								long safeDuration = currTime - safeTimestamp;
								if (safeDuration > MIN_SAFE_DURATION) {
									Logger.log("Granting self permission to cross intersection! safeDuration = " 
											+ safeDuration + ", Min. safe duration = " + MIN_SAFE_DURATION);
									accessGranted = true;
									beacon.setVehicleStatus(VehicleStatus.CROSSING);
									lineFollower.unpause();
								}
							}
						}
					} else {
						Logger.log("Not safe to cross.");
						isSafeToCross = false;
						safeTimestamp = -1;
					}
				}
			}
			
			synchronized(this) {
				try {
					this.wait(CYCLE_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		Logger.log("Thread terminating...");
		System.exit(0);
		
	}
}
