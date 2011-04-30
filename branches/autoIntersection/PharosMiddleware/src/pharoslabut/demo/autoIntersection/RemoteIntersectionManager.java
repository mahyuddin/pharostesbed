package pharoslabut.demo.autoIntersection;

import java.net.UnknownHostException;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

import pharoslabut.navigate.*;
import pharoslabut.io.*;
import pharoslabut.demo.autoIntersection.msgs.*;
import pharoslabut.logger.*;

/**
 * Communicates with the intersection server to safely navigate the 
 * intersection.
 * 
 * @author Chien-Liang Fok
 * @author Seth Gee
 */
public class RemoteIntersectionManager implements LineFollowerEventListener, MessageReceiver {
	
	/**
	 * Maximum amount of time in milliseconds between entering the intersection and
	 * exiting it.
	 */
	public static long MAXIMUM_INTERSECTION_DURATION = 10000;
	
	/**
	 * The maximum amount of times a RequestAccessMsg will be sent to the server.
	 */
	public static int MAXIMUM_REQUESTS = 5;
	
	/**
	 * The ID of the robot.  This is unique to each robot and is the wireless ad hoc IP address
	 * of the robot.  It is of the form "10.11.12.x".
	 */
	private InetAddress robotIP;
	
	/**
	 * This is the IP address of the intersection server.
	 */
	private InetAddress serverIP;
	
	/**
	 * This is the port on which the intersection server is listening.
	 */
	private int serverPort;
	
	/**
	 * The client manager is the main component of the robot-side of the application.
	 * It coordinates the line follower, remote intersection manager, and 
	 * local intersection manager.
	 */
	private ClientManager clientMgr;
	
	/**
	 * This component is responsible for making the robot follow the line,
	 * which represents a lane.
	 */
	private LineFollower lf;
	
	/**
	 * For logging debug messages.
	 */
	private FileLogger flogger;
	
	/**
	 * Keeps track of whether the RemoteIntersectionManager is running.
	 */
	private boolean isRunning = false;
	
	/**
	 * Set if server has given robot priority access through the intersection.
	 */
	private boolean accessGranted;
	
	/**
	 * The specification of the entry/exit lanes that the robot wants to
	 * travel through.  This class is responsible for ensuring the robot
	 * can travel through the intersection safely.
	 */
	private LaneSpecs laneSpecs;
	
	/**
	 * The time at which the server has allows the robot to enter the intersection.
	 */
	private long accessTime;
	
	/**
	 * Distance from "Approaching" line to "Entering Intersection" line. 
	 * In cm.
	 */
	public static final double distToIntersection_m = .90; // 90 cm distance to enter = .9 m
	
	/**
	 * The connection to the server.
	 */
	private NetworkInterface networkInterface;
	
	/**
	 * A timer for detecting if EXIT event never arrives.
	 */
	private Timer timer;
	
	/**
	 * The constructor.
	 * 
	 * @param lf The LineFollower
	 * @param serverIP The IP address of the intersection manager server.
	 * @param serverPort The port on which the intersection manager server is listening.
	 * This is also the port on which the robot is listening
	 * @param clientmgr The client manager that should be notified should this
	 * class fail to navigate the intersection.
	 * @param flogger The FileLogger for recording debug statements.
	 */
	public RemoteIntersectionManager(LineFollower lf, String serverIP, int serverPort, 
			ClientManager clientMgr, FileLogger flogger) 
	{
		this.lf = lf;
		try {
			this.serverIP = InetAddress.getByName(serverIP);
		} catch (UnknownHostException e) {
			log("ERROR: Unable to get server address...");
			e.printStackTrace();
			System.exit(1); // fatal error
		}
		this.serverPort = serverPort;
		this.clientMgr = clientMgr;
		this.flogger = flogger;
		
		try {
			robotIP = InetAddress.getByName(pharoslabut.beacon.WiFiBeaconBroadcaster.getPharosIP());
		} catch (UnknownHostException e) {
			log("ERROR: Unable to get robot's IP address.");
			e.printStackTrace();
			System.exit(1); // fatal error
		}
		
		lf.addListener(this);
	
		networkInterface = new TCPNetworkInterface(); //new UDPNetworkInterface(); // robot listens on any available port
		networkInterface.registerMsgListener(this);
	}
	
	/**
	 * Starts the RemoteIntersectionManager running.  This should be
	 * called when the LineFollower approaches the intersection.
	 * 
	 * @param laneSpecs The lane specifications.  This specifies which line the robot is
	 * approaching from and which lane it would like to exit the intersection from.
	 */
	public void start(LaneSpecs laneSpecs) {
		this.laneSpecs = laneSpecs;
		this.isRunning = true;
		doApproaching();
	}

	/**
	 * Called when the robot is approaching the intersection.
	 * It asks the server for permission to cross intersection.
	 */
	private void doApproaching() {
		log("Robot is approaching intersection!");
		
		// Create a RequestAccessMsg.
		long eta = ((long)(((distToIntersection_m / LineFollower.MAX_SPEED) * 1000) + System.currentTimeMillis()));
		RequestAccessMsg ram = new RequestAccessMsg(robotIP, networkInterface.getLocalPort(), 
				eta, eta+MAXIMUM_INTERSECTION_DURATION, laneSpecs);
		
		// Send the RequestAccessMsg to the intersection server...
		if (!networkInterface.sendMessage(serverIP, serverPort, ram)) {
			
			// For now, ignore this problem because when the robot enters the intersection,
			// it will notice that it has not been granted access and will re-request
			// permission to enter.
			log("doApproaching: WARNING: failed to send RequestAccessMsg...");
		} else
			log("doApproaching: Sent request to enter intersection...");
	}
	
	/**
	 * Called when the robot is entering the intersection.
	 */
	private void doEntering() {
		log("doEntering: Robot is entering intersection!");
		
		// If approval has not been obtained, pause and wait for the approval to arrive.
		// Repeatedly ask up to MAXIMUM_REQUESTS before aborting.
		if (!accessGranted) {
			log("doEntering: No access granted, stopping robot...");
			lf.stop();
			
			int tryCount = 0;
			
			// Repeatedly send RequestAccessMsg to server...
			while(!accessGranted && tryCount < MAXIMUM_REQUESTS) {
				// Create the RequestAccessMsg...
				long eta = System.currentTimeMillis(); // robot is already at intersection
				RequestAccessMsg ram = new RequestAccessMsg(robotIP, networkInterface.getLocalPort(), 
						eta, eta+MAXIMUM_INTERSECTION_DURATION, laneSpecs);
				
				tryCount++;
				
				// Send the RequestAccessMsg...
				if (!networkInterface.sendMessage(serverIP, serverPort, ram))
					log("doEntering: WARNING: failed to send RequestAccessMsg + " + tryCount + "...");
				else
					log("doEntering: Sent RequestAccessMsg " + tryCount + " to server...");
				
				try {
					synchronized (this){
						wait(1000);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		// If access could not be obtained, abort and notify the client manager.
		if (!accessGranted) {
			log("doEntering: ERROR: Unable to gain access to intersection, aborting...");
			doHalt(false);
		}
		
		
		// Access was granted, wait appropriate amount of time before continuing.
		assert accessGranted : "Expected access granted"; // Will throw error if access not granted.
		long currTime;

		// Wait until the granted access time has been reached.
		while ((currTime = System.currentTimeMillis()) < accessTime) {
			log("doEntering: Access granted but access time in the future (curr time=" + currTime + ", accessTime=" + accessTime);
			lf.stop();
			try {
				synchronized (this){
					wait(1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		// start robot moving once priority access received
		log("doEntering: Starting the robot moving...");
		lf.start();
		
		log("doEntering: Starting timer to detect when the EXIT event does not arrive...");
		timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				
				// Note from Liang:  I'm not sure this is the smart thing to do (assume that the robot has
				// crossed the intersection).  But I don't want to have the demo halted because
				// the CMUCam failed to detect the exit strip on the ground.
				log("Timer: WARNING: Robot spent more than " +  MAXIMUM_INTERSECTION_DURATION 
						+ "ms in intersection!  Assuming it has safely crossed the intersection.");
				doHalt(true);
			}
		}, MAXIMUM_INTERSECTION_DURATION);
	}
	
	/**
	 * Called when the robot is exiting the intersection.
	 * It notifies the server that this robot is leaving the intersection
	 */
	private void doExiting() {
		log("doExiting: Robot is exiting intersection!");
		
		// Stop the timer since we received a ReservationTimeMsg
		if (timer != null) {
			log("doExiting: Stopping timer...");
			timer.cancel();
			timer = null;
		} else
			log("doExiting: ERROR: timer was null, could not stop it!");
		
		// Create an ExitingMsg
		ExitingMsg em = new ExitingMsg(robotIP, networkInterface.getLocalPort());
		
		// Send the ExitingMsg to intersection server.
		if (!networkInterface.sendMessage(serverIP, serverPort, em)) {
			// TODO Handle this situation!  Try to retransmit the message a certain number
			// of times before giving up.
			log("doExiting: WARNING: failed to send ExitingMsg...");
		} else
			log("doExiting: sent ExitingMsg to server...");
		
		doHalt(true);
	}
	
	/**
	 * Called to terminate the RemoteIntersectionManager.  Notifies the 
	 * ClientManager that this is done executing.
	 * 
	 * @param success Whether it successfully navigated across the intersection.
	 */
	private void doHalt(boolean success) {
		accessGranted = false; // be sure to reset this, otherwise it will go forever
		isRunning = false; // The remote intersection manager is done running.
		
		if (!success) {
			log("doHalt: Did not succeed, halting the robot...");
			lf.stop();
		}
		
		// Notify the client manager that the RemoteIntersectionManager is done
		log("doHalt: notifying ClientManager that RemoteIntersectionmanager is done (success = " + success + ")");
		clientMgr.remoteIntersectionMgrDone(success);
	}
	
	@Override
	public void newLineFollowerEvent(LineFollowerEvent lfe, LineFollower follower) {
		if (isRunning) {
			switch(lfe.getType()) {
			// APPROACHING and ERROR events are handled by the ClientManager
			case ENTERING:
				doEntering();
				break;
			case EXITING:
				doExiting();
				break;
			default:
				log("newLineFollowerEvent: ERROR: Unexpected LineFollower event (" + lfe + "), aborting...", false);
				doHalt(false);
			}
		} else
			log("newLineFollowerEvent: Ignoring event because not running: " + lfe);
	}
	
	/**
	 * Handles ReservationTimeMsg messages from the server.
	 * 
	 * @param msg The ReservationTimeMsg.
	 */
	private void handleReservationTimeMsg(ReservationTimeMsg msg) {
		log("handleReservationTimeMsg: Received permission to enter intersection at time " + msg.getETA());
		this.accessGranted = true;
		this.accessTime = msg.getETA();
	}
	
	/*
	 * unused for now because of TCP, need method for UDP
	private void handleExitingAcknowledgedMsg(ExitingAcknowledgedMsg msg) {
		log("handleExitingAcknowledgedMsg: Canceling Exit Timer, ACK received.");
		exitTimer.cancel();
		this.accessGranted = false; // robot is through and no longer needs access
		this.accessTime = Long.MAX_VALUE; // set this super high so robot would have to wait at intersection until new access time comes in
	}
	*/
	
	/**
	 * Handles messages sent from the intersection server.
	 */
	@Override
	public void newMessage(Message msg) {
		log("newMessage: Received new message from server: " + msg);
		if (isRunning) {
			if (msg instanceof ReservationTimeMsg)
				handleReservationTimeMsg( (ReservationTimeMsg) msg );
			//Uncomment if using UDP, keep commented out for TCP
			//else if (msg instanceof ExitingAcknowledgedMsg)
			//handleExitingAcknowledgedMsg( (ExitingAcknowledgedMsg) msg );
			else
				log("newMessage: ERROR: Unknown message from server: " + msg, false);
		} else {
			log("newMessage: Received a message but was not running, discarding it...");
		}
	}

	/**
	 * Logs a debug message.  This message is only printed when debug mode is enabled.
	 * 
	 * @param msg The message to log.
	 */
	private void log(String msg) {
		log(msg, true);
	}
	
	/**
	 * Logs a message.
	 * 
	 * @param msg  The message to log.
	 * @param isDebugMsg Whether the message is a debug message.
	 */
	private void log(String msg, boolean isDebugMsg) {
		String result = "RemoteIntersectionManager: " + msg;
		if (!isDebugMsg || System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
}
