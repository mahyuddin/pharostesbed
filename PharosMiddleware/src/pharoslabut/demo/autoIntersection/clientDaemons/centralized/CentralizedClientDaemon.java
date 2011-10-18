package pharoslabut.demo.autoIntersection.clientDaemons.centralized;

import java.net.InetAddress;
import java.net.UnknownHostException;

import pharoslabut.RobotIPAssignments;
import pharoslabut.demo.autoIntersection.clientDaemons.ClientDaemon;
import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionDetector;
import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionEvent;
import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionEventListener;
import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionEventType;
import pharoslabut.demo.autoIntersection.msgs.AutoIntersectionMsg;
import pharoslabut.exceptions.PharosException;
import pharoslabut.io.TCPMessageSender;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.LineFollower;

/**
 * Navigates across an intersection by communicating with a central server.
 * 
 * @author Chien-Liang Fok
 * @author Seth Gee
 */
public class CentralizedClientDaemon extends ClientDaemon implements IntersectionEventListener, Runnable {
	
	/**
	 * The number of milliseconds before this daemon assumes the previous
	 * request to gain access to the intersection was rejected.
	 */
	public static final long REQUEST_TIMEOUT = 2000;
	
	/**
	 * The IP address of the intersection management server.
	 */
	protected InetAddress serverIP;
	
	/**
	 * The port of the intersection management server.
	 */
	protected int serverPort;
	
	/**
	 * The local IP address.
	 */
	protected InetAddress ip;
	
	/**
	 * The local port on which this client should listen.
	 */
	protected int port;
	
	/**
	 * Whether the this daemon is running.
	 */
	protected boolean isRunning = false;
	
	/**
	 * The time since the robot last requested access to the intersection.
	 */
	protected long lastRequestTime = -1;
	
	/**
	 * Whether access to the intersection was granted.
	 */
	protected boolean accessGranted = false;
	
	/**
	 * The message sender.
	 */
	protected TCPMessageSender msgSender = TCPMessageSender.getSender();
	
	/**
	 * The constructor.
	 * 
	 * @param serverIP The IP address of the intersection management server.
	 * @param serverPort The port of the intersection management server.
	 * @param port The local port on which this client should listen.
	 * @param lineFollower The line follower.
	 * @param intersectionDetector The intersection detector.
	 * @param entryPointID The ID of the entry point.
	 * @param exitPointID The ID of the exit point.
	 */
	public CentralizedClientDaemon(InetAddress serverIP, int serverPort, int port,
			LineFollower lineFollower, IntersectionDetector intersectionDetector,
			String entryPointID, String exitPointID) 
	{
		super(lineFollower, intersectionDetector, entryPointID, exitPointID);
		this.serverIP = serverIP;
		this.serverPort = serverPort;
		this.port = port;
		this.lineFollower = lineFollower;
		this.intersectionDetector = intersectionDetector;
		
		// Get the local IP address.
		try {
			ip = InetAddress.getByName(RobotIPAssignments.getAdHocIP());
		} catch (UnknownHostException e) {
			Logger.logErr(e.toString());
			e.printStackTrace();
			System.exit(1);
		} catch (PharosException e) {
			Logger.logErr(e.toString());
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Starts this daemon running.
	 */
	public synchronized void start() {
		if (!isRunning) {
			isRunning = true; 
			
			Logger.log("Registering self as listener to intersection events.");
			intersectionDetector.addIntersectionEventListener(this);
			
			Logger.log("Starting a thread for this daemon.");
			new Thread(this).start();
		} else {
			Logger.logErr("Trying to start twice!");
		}
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
				break;
			
			case ENTERING:
				if (!accessGranted) {
					Logger.log("Vehicle is entering intersection but access not granted.  Stopping robot.");
					lineFollower.pause();
				} else {
					Logger.log("Vehicle is entering intersection (access was granted).");
				}
				currState = IntersectionEventType.ENTERING;
				break;
			
			case EXITING:
				Logger.log("Vehicle is exiting intersection.");
				currState = IntersectionEventType.EXITING;
				
				// Send a message to the server telling it that this robot has exited the
				// intersection
				Logger.log("Sending ExitingMsg to server.");
				ExitingMsg emsg = new ExitingMsg(ip, port);
				try {
					msgSender.sendMessage(serverIP, serverPort, emsg);
				} catch (PharosException e) {
					Logger.logErr("Problems while sending exiting message: " + e.toString());
					e.printStackTrace();
				}
				
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
				
//			This message is now handled by the ClientManager
			case ERROR:
				Logger.logErr("Received error from line follower!  Aborting demo.");
				lineFollower.stop(); // There was an error, stop!
			default:
				Logger.log("Discarding unexpected intersection event: " + lfe);
			}
		} else
			Logger.log("Ignoring event because not running: " + lfe);
	}
	
	@Override
	public void messageReceived(AutoIntersectionMsg msg) {
		if (msg instanceof GrantAccessMsg) {
//			GrantAccessMsg grantMsg = (GrantAccessMsg)msg;
			
			accessGranted = true;
			
			if (currState == IntersectionEventType.APPROACHING) {
				Logger.log("Received grant message.");	
			}
			else if (currState == IntersectionEventType.ENTERING) {
				Logger.log("Received grant message, resuming robot movement.");
				lineFollower.unpause();
			}
			else {
				Logger.logErr("Received unexpected grant message, currState = " + currState);
			}
		}
		
	}
	
	public void run() {
		Logger.log("Thread starting...");
		
		Logger.log("Starting the line follower.");
		lineFollower.start();
		
		while(isRunning) {
			
			if (currState == IntersectionEventType.APPROACHING || 
					currState == IntersectionEventType.ENTERING)
			{
				
				if (!accessGranted) {

					long currTime = System.currentTimeMillis();
					long timeSinceLastReq = currTime - lastRequestTime;
					if (timeSinceLastReq > REQUEST_TIMEOUT) {
						Logger.log("Sending request to server.");
						RequestAccessMsg requestMsg = new RequestAccessMsg(ip, port, 
								entryPointID, exitPointID);

						Logger.log("Sending request access message to server.");
						try {
							msgSender.sendMessage(serverIP, serverPort, requestMsg);
						} catch (PharosException e) {
							Logger.logErr(e.toString());
							e.printStackTrace();
						}
						
						lastRequestTime = currTime;
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
