package pharoslabut.demo.autoIntersection;

import java.net.UnknownHostException;
import java.net.InetAddress;
import java.util.Timer;

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
	 * The ID of the robot.  This is unique to each robot, and should be the last octal of the
	 * ad hoc IP address.
	 */
	private InetAddress robotIP;
	
	/**
	 * This is the IP address of the intersection server.
	 */
	private InetAddress serverAddr;
	
	/**
	 * This is the port on which the intersection server is listening.
	 */
	private int serverPort;
	
	/**
	 * This component is responsible for ensuring the robot follows the line.
	 */
	private LineFollower lf;
	
	/**
	 * For logging debug messages.
	 */
	private FileLogger flogger;
	
	/**
	 * Set if server has given robot priority access through the intersection.
	 */
	private boolean accessGranted;
	
	/**
	 * The time at which the server has allows the robot to enter the intersection.
	 */
	private long accessTime;
	
	/**
	 * Distance from "Approaching" line to "Entering Intersection" line. 
	 * In cm.
	 */
	public static final int distToIntersection_cm = 90;
	
	/**
	 * The connection to the server.
	 */
	private NetworkInterface networkInterface;
	
	private Timer exitTimer;
	
	/**
	 * The constructor.
	 * 
	 * @param lf The LineFollower
	 * @param serverIP The IP address of the server.
	 * @param serverPort The port on which the server is listening.  
	 * This is also the port on which the robot is listening
	 * @param flogger The FileLogger for recording debug statements.
	 */
	public RemoteIntersectionManager(LineFollower lf, String serverIP, int serverPort, FileLogger flogger) {
		this.lf = lf;
		try {
			this.serverAddr = InetAddress.getByName(serverIP);
		} catch (UnknownHostException e) {
			log("ERROR: Unable to get server address...");
			e.printStackTrace();
			System.exit(1);
		}
		this.serverPort = serverPort;
		this.flogger = flogger;
		
		try {
			robotIP = InetAddress.getByName(pharoslabut.beacon.WiFiBeaconBroadcaster.getPharosIP());
		} catch (UnknownHostException e) {
			log("ERROR: Unable to get robot's IP address.");
			e.printStackTrace();
			System.exit(1); // fatal error.
		}
		
		lf.addListener(this);
	
		networkInterface = new UDPNetworkInterface(serverPort+1); // robot listens on same port as server
		networkInterface.registerMsgListener(this);
	}

	/**
	 * Called when the robot is approaching the intersection.
	 * It asks the server for permission to cross intersection.
	 */
	private void doApproaching() {
		log("Robot is approaching intersection!");
		
		// Create the RequestAccessMsg...
		long eta = ((long)(((distToIntersection_cm / LineFollower.MAX_SPEED) * 1000) + System.currentTimeMillis()));
		RequestAccessMsg ram = new RequestAccessMsg(robotIP, serverPort, eta);
		
		// Send the RequestAccessMsg...
		if (!networkInterface.sendMessage(serverAddr, serverPort, ram)) {
			log("WARNING: failed to send RequestAccessMsg...");
		} else
			log("Sent request to enter intersection...");
	}
	
	/**
	 * Called when the robot is entering the intersection.
	 */
	private void doEntering() {
		log("Robot is entering intersection!");
		//TODO Implement this... if approval has not been obtained, pause and wait 
		// for the approval to arrive (may need to query server again).
		
		while(!accessGranted) {
			log("No access granted, stopping robot...");
			try {
				// Stop the robot
				lf.stop();
				
				// Pause
				this.wait(100);
				
				// Request access from server...
				
				// Create the RequestAccessMsg...
				long eta = System.currentTimeMillis(); // robot is already at intersection
				RequestAccessMsg ram = new RequestAccessMsg(robotIP, serverPort, eta);
				
				// Send the RequestAccessMsg...
				if (!networkInterface.sendMessage(serverAddr, serverPort, ram)) {
					log("WARNING: failed to send RequestAccessMsg...");
				} else {
					log("Sent request access message again to server...");
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
		}
		// else go through intersection if the current time is at the accessTime
		
		if (accessGranted && System.currentTimeMillis() >= accessTime) {
			// start robot back up once priority access received
			lf.start();
		}
	}
	
	/**
	 * Called when the robot is exiting the intersection.
	 * It notifies the server that this robot is leaving the intersection
	 */
	private void doExiting() {
		log("Robot is exiting intersection!");
		
		// Create an ExitingMsg
		ExitingMsg em = new ExitingMsg(robotIP, serverPort);
		
		// Send the ExitingMsg
		if (!networkInterface.sendMessage(serverAddr, serverPort, em)) {
			log("WARNING: failed to send ExitingMsg...");
		}
		
		//TODO: start timer, if ack not within 2 seconds then send again
		exitTimer = new Timer();
		exitTimer.schedule(new java.util.TimerTask() {
			public void run() {
				doExiting();
			}
		}, 2000);
	}
	
	@Override
	public void newLineFollowerEvent(LineFollowerEvent lfe, LineFollower follower) {
		switch(lfe.getType()) {
		case APPROACHING:
			doApproaching();
			break;
		case ENTERING:
			doEntering();
			break;
		case EXITING:
			doExiting();
			break;
		case ERROR:
			log("Received error from line follower!  Aborting demo.");
			lf.stop(); // There was an error, stop!
			break;
		}
	}
	
	private void handleReservationTimeMsg(ReservationTimeMsg msg) {
		log("Received permission to enter intersection at time " + msg.getETA());
		// verify the message was specified for my robot's id
		// if()
		this.accessGranted = true;
		this.accessTime = msg.getETA();
		// else
		//   doEntering();
	}
	
	private void handleExitingAcknowledgedMsg(ExitingAcknowledgedMsg msg) {
		exitTimer.cancel();
		this.accessGranted = false; // robot is through and no longer needs access
		this.accessTime = Long.MAX_VALUE; // set this super high so robot would have to wait at intersection until new access time comes in
	}
	
	@Override
	public void newMessage(Message msg) {
		if (msg instanceof ReservationTimeMsg)
    		handleReservationTimeMsg( (ReservationTimeMsg) msg );
    	//else if (msg instanceof ExitingMsg)
    		//handleExitingMsg( (ExitingMsg) msg );
    	else if (msg instanceof ExitingAcknowledgedMsg)
    		handleExitingAcknowledgedMsg( (ExitingAcknowledgedMsg) msg );
    	else
    		System.out.println("RECEIVER: Unknown message " + msg);
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
		String result = "RemoteIntersectionMar: " + msg;
		if (!isDebugMsg || System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
}
