package pharoslabut.demo.autoIntersection;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;

import pharoslabut.RobotIPAssignments;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.*;
import pharoslabut.beacon.BeaconBroadcaster;
import pharoslabut.beacon.WiFiBeacon;
import pharoslabut.beacon.WiFiBeaconBroadcaster;
import pharoslabut.beacon.WiFiBeaconEvent;
import pharoslabut.beacon.WiFiBeaconListener;
import pharoslabut.beacon.WiFiBeaconReceiver;
import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionDetector;
import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionEvent;
import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionEventListener;
import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionEventType;
import pharoslabut.demo.autoIntersection.msgs.*;
import pharoslabut.exceptions.PharosException;

/**
 * Implements an ad hoc form of intersection management where each robot decides
 * independently whether it is safe to traverse the intersection.
 * 
 * @author Chien-Liang Fok
 */
public class AdHocClientDaemon extends ClientDaemon implements IntersectionEventListener, 
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
	public static final int MIN_SAFE_DURATION = 3000;
	
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
    private String mCastAddress = "230.1.2.3"; // TODO: Make this adjustable.
    
    /**
	 * The WiFi multicast port.
	 */
    private int mCastPort = 6000;  // TODO: Make this adjustable.
    
    /**
     * The beacon that this node is periodically broadcasting.
     */
    private AdHocAutoIntersectionBeacon beacon;
    
    /**
     * The beacon receiver.
     */
    private WiFiBeaconReceiver beaconReceiver;
    
    /**
     * The neighbor list.
     */
    private NeighborList nbrList = new NeighborList();
    
    /**
     * A neighbor list that records the status of each neighbor.
     * The key is a string that consists of the neighbor's IP address,
     * and the value is a neighbor state object that records the neighbor's
     * state.
     */
    private Hashtable<InetAddress, NeighborList> neighborList = new Hashtable<InetAddress, NeighborList>();
    
    /**
	 * The constructor.
	 * 
//	 * @param port The local port on which this client should listen.
	 * @param lineFollower The line follower.
	 * @param intersectionDetector The intersection detector.
	 * @param entryPointID The entry point ID.
	 * @param exitPointID The exit point ID.
	 */
	public AdHocClientDaemon(//int port,
			LineFollower lineFollower, IntersectionDetector intersectionDetector,
			String entryPointID, String exitPointID) 
	{
		super(lineFollower, intersectionDetector, entryPointID, exitPointID);
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
		
		Logger.log("Creating the beacon.");
		try {
			beacon = new AdHocAutoIntersectionBeacon(InetAddress.getByName(pharosIP), mCastPort);
		} catch (UnknownHostException e) {
			Logger.logErr("Unable to create the beacon: " + e.toString());
			e.printStackTrace();
			System.exit(1);
		}
		
		Logger.log("Creating the beacon broadcaster.");
		beaconBroadcaster = new WiFiBeaconBroadcaster(mCastGroupAddress, pharosIP, 
				mCastPort, beacon);
		
		Logger.log("Creating the beacon receiver.");
		beaconReceiver = new WiFiBeaconReceiver(mCastAddress, mCastPort, pharosNI);
		beaconReceiver.addBeaconListener(this);
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
		WiFiBeacon beacon = be.getBeacon();
		if (beacon instanceof AdHocAutoIntersectionBeacon) {
			nbrList.update((AdHocAutoIntersectionBeacon)beacon);
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
					boolean isSafeNow = nbrList.isSafeToCross();
					if (isSafeNow) {
						if (!isSafeToCross) {
							Logger.log("It might be safe to cross, currTime = currTime");
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
	
//	/**
//	 * Maximum amount of time in milliseconds between entering the intersection and
//	 * exiting it.
//	 */
//	public static long MAXIMUM_INTERSECTION_DURATION = 10000;
//	
//	/**
//	 * The maximum amount of times a RequestAccessMsg will be sent to the server.
//	 */
//	public static int MAXIMUM_REQUESTS = 5;
//	
//	/**
//	 * This detects the intersection.
//	 */
//	private IntersectionDetector detector;
//	
//	/**
//	 * The ID of the robot.  This is unique to each robot and is the wireless ad hoc IP address
//	 * of the robot.  It is of the form "10.11.12.x".
//	 */
//	private InetAddress robotIP;
//	
//	/**
//	 * This is the IP address of the intersection server.
//	 */
//	private InetAddress serverIP;
//	
//	/**
//	 * This is the port on which the intersection server is listening.
//	 */
//	private int serverPort;
//	
////	/**
////	 * The client manager is the main component of the robot-side of the application.
////	 * It coordinates the line follower, remote intersection manager, and 
////	 * local intersection manager.
////	 */
////	private AutoIntersectionClient clientMgr;
//	
//	/**
//	 * This component is responsible for making the robot follow the line,
//	 * which represents a lane.
//	 */
//	private LineFollower lf;
//	
//	/**
//	 * Keeps track of whether the RemoteIntersectionManager is running.
//	 */
//	private boolean isRunning = false;
//	
//	/**
//	 * Set if server has given robot priority access through the intersection.
//	 */
//	private boolean accessGranted;
//	
//	/**
//	 * The specification of the entry/exit lanes that the robot wants to
//	 * travel through.  This class is responsible for ensuring the robot
//	 * can travel through the intersection safely.
//	 */
//	private LaneSpecs laneSpecs;
//	
//	/**
//	 * The time at which the server has allows the robot to enter the intersection.
//	 */
//	private long accessTime;
//	
//	/**
//	 * Distance from "Approaching" line to "Entering Intersection" line. 
//	 * In cm.
//	 */
//	public static final double distToIntersection_m = .90; // 90 cm distance to enter = .9 m
//	
//	/**
//	 * The connection to the server.
//	 */
//	private NetworkInterface networkInterface;
//	
//	/**
//	 * A timer for detecting if EXIT event never arrives.
//	 */
//	private Timer timer;
//	
//	
//	/**
//	 * The constructor.
//	 * 
//	 * @param settings The experiment settings.
//	 * @param lf The LineFollower
//	 * @param clientmgr The client manager that should be notified should this
//	 * class fail to navigate the intersection.
//	 */
//	public AdHocClientDaemon(LoadExpSettingsMsg settings, LineFollower lf, 
//			IntersectionDetector detector) 
//	{	
//		this.lf = lf;
//		try {
//			this.serverIP = InetAddress.getByName(settings.getServerIP());
//		} catch (UnknownHostException e) {
//			Logger.logErr("ERROR: Unable to get server address...");
//			e.printStackTrace();
//			System.exit(1); // fatal error
//		}
//		this.serverPort = serverPort;
//		
//		try {
//			robotIP = InetAddress.getByName(pharoslabut.RobotIPAssignments.getAdHocIP());
//		} catch (Exception e) {
//			Logger.logErr("Unable to get robot's IP address.");
//			e.printStackTrace();
//			System.exit(1); // fatal error
//		}
//		
////		lf.addListener(this);
//	
//		networkInterface = new TCPNetworkInterface(); //new UDPNetworkInterface(); // robot listens on any available port
//		networkInterface.registerMsgListener(this);
//	}
//	
//	/**
//	 * Sends an IntersectionEvent to the server.
//	 * 
//	 * @param currIE The intersection event to send.
//	 */
//	public void sendToServer(IntersectionEvent currIE) {
//		AutoIntDebugMsg msg = new AutoIntDebugMsg(robotIP, networkInterface.getLocalPort(), currIE);
//		networkInterface.sendMessage(serverIP, serverPort, msg);
//	}
//	
//	/**
//	 * Starts the RemoteIntersectionManager running.  This should be
//	 * called when the LineFollower approaches the intersection.
//	 * 
//	 * @param laneSpecs The lane specifications.  This specifies which line the robot is
//	 * approaching from and which lane it would like to exit the intersection from.
//	 */
//	public void start(LaneSpecs laneSpecs) {
//		this.laneSpecs = laneSpecs;
//		this.isRunning = true;
//		doApproaching();
//	}
//
//	/**
//	 * Called when the robot is approaching the intersection.
//	 * It asks the server for permission to cross intersection.
//	 */
//	private void doApproaching() {
//		Logger.log("Robot is approaching intersection " + this.laneSpecs.getEntryID() + "!");
//		
//		// Create a RequestAccessMsg.
//		long eta = ((long)(((distToIntersection_m / LineFollower.MAX_SPEED) * 1000) + System.currentTimeMillis()));
//		RequestAccessMsg ram = new RequestAccessMsg(robotIP, networkInterface.getLocalPort(), 
//				eta, eta+MAXIMUM_INTERSECTION_DURATION, laneSpecs);
//		
//		// Send the RequestAccessMsg to the intersection server...
//		if (!networkInterface.sendMessage(serverIP, serverPort, ram)) {
//			
//			// For now, ignore this problem because when the robot enters the intersection,
//			// it will notice that it has not been granted access and will re-request
//			// permission to enter.
//			Logger.log("WARNING: failed to send RequestAccessMsg...");
//		} else
//			Logger.log("Sent request to enter intersection...");
//	}
//	
//	/**
//	 * Called when the robot is entering the intersection.
//	 */
//	private void doEntering() {
//		Logger.log("Logger.log(Robot is entering intersection!");
//		
//		// If approval has not been obtained, pause and wait for the approval to arrive.
//		// Repeatedly ask up to MAXIMUM_REQUESTS before aborting.
//		if (!accessGranted) {
//			Logger.log("Access NOT granted, stopping robot...");
//			lf.stop();
//			
//			int tryCount = 0;
//			
//			// Repeatedly send RequestAccessMsg to server...
//			while(!accessGranted && tryCount < MAXIMUM_REQUESTS) {
//				// Create the RequestAccessMsg...
//				long eta = System.currentTimeMillis(); // robot is already at intersection
//				RequestAccessMsg ram = new RequestAccessMsg(robotIP, networkInterface.getLocalPort(), 
//						eta, eta+MAXIMUM_INTERSECTION_DURATION, laneSpecs);
//				
//				tryCount++;
//				
//				// Send the RequestAccessMsg...
//				if (!networkInterface.sendMessage(serverIP, serverPort, ram))
//					Logger.log("WARNING: failed to send RequestAccessMsg + " + tryCount + "...");
//				else
//					Logger.log("Sent RequestAccessMsg " + tryCount + " to server...");
//				
//				try {
//					synchronized (this){
//						wait(1000);
//					}
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		
//		// If access could not be obtained, abort and notify the client manager.
//		if (!accessGranted) {
//			Logger.logErr("Unable to gain access to intersection, aborting...");
//			doHalt(false);
//		}
//		
//		
//		// Access was granted, wait appropriate amount of time before continuing.
//		assert accessGranted : "Expected access granted"; // Will throw error if access not granted.
//		long currTime;
//
//		// Wait until the granted access time has been reached.
//		while ((currTime = System.currentTimeMillis()) < accessTime) {
//			Logger.log("Access granted but access time in the future (curr time=" 
//					+ currTime + ", accessTime=" + accessTime + ", diff=" + (accessTime - currTime) + ")");
//			lf.stop();
//			try {
//				synchronized (this){
//					wait(1000);
//				}
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//		
//		// start robot moving once priority access received
//		Logger.log("Starting the robot moving...");
//		lf.start();
//		
//		Logger.log("Starting timer to detect if the EXIT event does not arrive...");
//		timer = new Timer();
//		timer.schedule(new TimerTask() {
//			public void run() {
//				
//				// Note from Liang:  I'm not sure this is the smart thing to do (assume that the robot has
//				// crossed the intersection).  But I don't want to have the demo halted because
//				// the CMUCam failed to detect the exit strip on the ground.
//				Logger.log("Timer: WARNING: Robot spent more than " +  MAXIMUM_INTERSECTION_DURATION 
//						+ "ms in intersection!  Assuming it has safely crossed the intersection.");
//				doHalt(true);
//			}
//		}, MAXIMUM_INTERSECTION_DURATION);
//	}
//	
//	/**
//	 * Called when the robot is exiting the intersection.
//	 * It notifies the server that this robot is leaving the intersection
//	 */
//	private void doExiting() {
//		Logger.log("Robot is exiting intersection!");
//		
//		// Stop the timer since we received a ReservationTimeMsg
//		if (timer != null) {
//			Logger.log("Stopping timer...");
//			timer.cancel();
//			timer = null;
//		} else
//			Logger.logErr("Timer was null, could not stop it!");
//		
//		// Create an ExitingMsg
//		ExitingMsg em = new ExitingMsg(robotIP, networkInterface.getLocalPort());
//		
//		// Send the ExitingMsg to intersection server.
//		if (!networkInterface.sendMessage(serverIP, serverPort, em)) {
//			// TODO Handle this situation!  Try to retransmit the message a certain number
//			// of times before giving up.
//			Logger.log("WARNING: failed to send ExitingMsg...");
//		} else
//			Logger.log("sent ExitingMsg to server...");
//		
//		doHalt(true);
//	}
//	
//	/**
//	 * Called to terminate the RemoteIntersectionManager.  Notifies the 
//	 * ClientManager that this is done executing.
//	 * 
//	 * @param success Whether it successfully navigated across the intersection.
//	 */
//	private void doHalt(boolean success) {
//		accessGranted = false; // be sure to reset this, otherwise it will go forever
//		isRunning = false; // The remote intersection manager is done running.
//		
//		if (!success) {
//			Logger.log("Did not succeed, halting the robot...");
//			lf.stop();
//		}
//		
//		// Notify the client manager that the RemoteIntersectionManager is done
//		Logger.log("Notifying ClientManager that RemoteIntersectionmanager is done (success = " + success + ")");
////		clientMgr.remoteIntersectionMgrDone(success);
//	}
//	
////	@Override
////	public void newLineFollowerEvent(LineFollowerEvent lfe, LineFollower follower) {
////		if (isRunning) {
////			switch(lfe.getType()) {
////			// APPROACHING and ERROR events are handled by the ClientManager
////			case ENTERING:
////				doEntering();
////				break;
////			case EXITING:
////				doExiting();
////				break;
////			default:
////				Logger.logErr("Unexpected LineFollower event (" + lfe + "), aborting...");
////				doHalt(false);
////			}
////		} else
////			Logger.log("Ignoring event because not running: " + lfe);
////	}
//	
//	/**
//	 * Handles ReservationTimeMsg messages from the server.
//	 * 
//	 * @param msg The ReservationTimeMsg.
//	 */
//	private void handleReservationTimeMsg(ReservationTimeMsg msg) {
//		Logger.log("handleReservationTimeMsg: Received permission to enter intersection at time " + msg.getETA());
//		this.accessGranted = true;
//		this.accessTime = msg.getETA();
//	}
//	
//	/*
//	 * unused for now because of TCP, need method for UDP
//	private void handleExitingAcknowledgedMsg(ExitingAcknowledgedMsg msg) {
//		log("handleExitingAcknowledgedMsg: Canceling Exit Timer, ACK received.");
//		exitTimer.cancel();
//		this.accessGranted = false; // robot is through and no longer needs access
//		this.accessTime = Long.MAX_VALUE; // set this super high so robot would have to wait at intersection until new access time comes in
//	}
//	*/
//	
//	/**
//	 * Handles messages sent from the intersection server.
//	 */
//	@Override
//	public void newMessage(Message msg) {
//		Logger.log("newMessage: Received new message from server: " + msg);
//		if (isRunning) {
//			if (msg instanceof ReservationTimeMsg)
//				handleReservationTimeMsg( (ReservationTimeMsg) msg );
//			//Uncomment if using UDP, keep commented out for TCP
//			//else if (msg instanceof ExitingAcknowledgedMsg)
//			//handleExitingAcknowledgedMsg( (ExitingAcknowledgedMsg) msg );
//			else
//				Logger.logErr("Unknown message from server: " + msg);
//		} else {
//			Logger.log("Received a message but was not running, discarding it...");
//		}
//	}

//	/**
//	 * Logs a debug message.  This message is only printed when debug mode is enabled.
//	 * 
//	 * @param msg The message to log.
//	 */
//	private void log(String msg) {
//		log(msg, true);
//	}
//	
//	/**
//	 * Logs a message.
//	 * 
//	 * @param msg  The message to log.
//	 * @param isDebugMsg Whether the message is a debug message.
//	 */
//	private void log(String msg, boolean isDebugMsg) {
//		String result = "RemoteIntersectionManager: " + msg;
//		if (!isDebugMsg || System.getProperty ("PharosMiddleware.debug") != null)
//			System.out.println(result);
//		if (flogger != null)
//			flogger.log(result);
//	}
}
