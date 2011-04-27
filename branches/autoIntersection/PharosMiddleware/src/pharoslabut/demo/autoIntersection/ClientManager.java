package pharoslabut.demo.autoIntersection;

import pharoslabut.logger.FileLogger;
import pharoslabut.navigate.LineFollower;
import pharoslabut.navigate.LineFollowerEvent;
import pharoslabut.navigate.LineFollowerEventListener;

/**
 * The top-level class of the autonomous intersection
 * demo client.
 * 
 * @author Chien-Liang Fok
 */
public class ClientManager implements LineFollowerEventListener {

	/**
	 * Defines the possible states that the client manager can be in.
	 */
	public static enum ClientManagerState {IDLE, FOLLOW_LINE, REMOTE_TRAVERSAL, LOCAL_TRAVERSAL};
    
	private ClientManagerState currState = ClientManagerState.IDLE;
	private FileLogger flogger;
	private LineFollower lf;
	private RemoteIntersectionManager rim;
	private LocalIntersectionManager lim;
	
	public ClientManager(String serverIP, int port, String playerIP, int playerPort, FileLogger flogger) {
		
		this.flogger = flogger;
		
		lf = new LineFollower(playerIP, playerPort, flogger);
		rim = new RemoteIntersectionManager(lf, serverIP, port, this, flogger);
		lim = new LocalIntersectionManager(lf, this, flogger);
		
		// This class is a listener for line follower events.
		lf.addListener(this);
		
		// Start the line follower.  This starts the robot moving following the line.
		currState = ClientManagerState.FOLLOW_LINE;
		lf.start();
	}
	
	/**
	 * This should be called whenever the RemoteIntersectionManager is done.
	 * 
	 * @param success Whether the RemoteIntersectionManager successfully traversed
	 * the intersection.
	 */
	public void remoteIntersectionMgrDone(boolean success) {
		if (success) {
			log("remoteIntersectionMgrDone: The RemoteIntersectionManager succeeded, returning to FOLLOW_LINE state...");
			currState = ClientManagerState.FOLLOW_LINE;
		} else {
			log("remoteIntersectionMgrDone: The RemoteIntersectionManager failed...");
			if (currState == ClientManagerState.REMOTE_TRAVERSAL) {
				log("remoteIntersectionMgrDone: Switching to the LocalIntersectionManager...");
				currState = ClientManagerState.LOCAL_TRAVERSAL;
				lim.start(getLaneSpecs());
			} else {
				log("remoteIntersectionMgrDone: ERROR: Unexpected state: " + currState + ", aborting demo...", false);
				lf.stop();
				currState = ClientManagerState.IDLE;
			}
		}
	}
	
	/**
	 * This should be called whenever the LocalIntersectionManager is done.
	 * 
	 * @param success Whether the LocalIntersectionManager successfully traversed
	 * the intersection.
	 */
	public void localIntersectionMgrDone(boolean success) {
		if (success) {
			log("localIntersectionMgrDone: The LocalIntersectionManager succeeded, returning to FOLLOW_LINE state...");
			currState = ClientManagerState.FOLLOW_LINE;
		} else {
			// At this point in time, we have no recourse but to abort the demo...
			log("localIntersectionMgrDone: ERROR: The LocalIntersectionManager failed...", false);
			lf.stop();
			currState = ClientManagerState.IDLE;
		}
	}
	
	/**
	 * @return The specifications of the lane that the robot is traversing.
	 */
	private LaneSpecs getLaneSpecs() {
		// TODO
		return new LaneSpecs();
	}
	
	
	@Override
	public void newLineFollowerEvent(LineFollowerEvent lfe, LineFollower follower) {
		
		// If the LineFollower fails, abort!
		if (lfe.getType() == LineFollowerEvent.LineFollowerEventType.ERROR) {
			log("Received error from the LineFollower, aborting demo...");
			currState = ClientManagerState.IDLE;
			lf.stop(); // There was an error, stop!
		}
		
		// The only time the ClientManager is interested in a LineFollowerEvent
		// is if it is in the FOLLOW_LINE state.
		else if (currState == ClientManagerState.FOLLOW_LINE) {
			if (lfe.getType() == LineFollowerEvent.LineFollowerEventType.APPROACHING) {
				log("Robot is approaching intersection, activating RemoteIntersectionManager...");
				currState = ClientManagerState.REMOTE_TRAVERSAL;
				rim.start(getLaneSpecs());
			} else
				log("newLineFollowerEvent: Discarding unexpected event from LineFollower: " + lfe);
		} else
			log("newLineFollowerEvent: Ignoring LineFollowerEvent because not in FOLLOW_LINE state.");
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
		String result = "ClientManager: " + msg;
		if (!isDebugMsg || System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(msg);
	}
	
	private static void usage() {
		System.setProperty ("PharosMiddleware.debug", "true");
		print("Usage: pharoslabut.demo.autoIntersection.ClientManager <options>\n");
		print("Where <options> include:");
		print("\t-server <ip address>: The IP address of the intersection server (required)");
		print("\t-port <port number>: The port on which the intersection server is listening (required)");
		print("\t-playerServer <ip address>: The IP address of the Player Server (default localhost)");
		print("\t-playerPort <port number>: The Player Server's port number (default 6665)");
		print("\t-log <log file name>: The name of the file in which to save debug output (default null)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String serverIP = null;
		int serverPort = -1;
		
		String playerServerIP = "localhost";
		int playerServerPort = 6665;
		FileLogger flogger = null;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-server")) {
					serverIP = args[++i];
				} else if (args[i].equals("-port")) {
					serverPort = Integer.valueOf(args[++i]);
				} else if (args[i].equals("-playerServer")) {
					playerServerIP = args[++i];
				} else if (args[i].equals("-playerPort")) {
					playerServerPort = Integer.valueOf(args[++i]);
				} else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				} else if (args[i].equals("-log")) {
					flogger = new FileLogger(args[++i]);
				} else {
					print("Unknown argument " + args[i]);
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
 
		if (serverIP == null || serverPort == -1) {
			System.setProperty ("PharosMiddleware.debug", "true");
			print("Must specify intersection server's IP and port.");
			usage();
			System.exit(1);
		}
		
		print("Server IP: " + serverIP);
		print("Server port: " + serverPort);
		
		new ClientManager(serverIP, serverPort, playerServerIP, playerServerPort, flogger);
	}
}
