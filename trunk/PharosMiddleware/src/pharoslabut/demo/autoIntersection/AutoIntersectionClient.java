package pharoslabut.demo.autoIntersection;

//import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.LineFollower;
//import pharoslabut.navigate.LineFollowerEventListener;
import pharoslabut.sensors.*;
import playerclient3.structures.blobfinder.PlayerBlobfinderData;

/**
 * The top-level class of the autonomous intersection
 * demo client.
 * 
 * @author Chien-Liang Fok
 */
public class AutoIntersectionClient implements BlobDataConsumer  {

	/**
	 * Defines the possible states that the client manager can be in.
	 */
	public static enum ClientManagerState {IDLE, FOLLOW_LINE, REMOTE_TRAVERSAL, LOCAL_TRAVERSAL};
    
	private ClientManagerState currState = ClientManagerState.IDLE;
//	private FileLogger flogger;
	private LineFollower lf;
	private RemoteIntersectionManager rim;
	private LocalIntersectionManager lim;
	private LaneIdentifier li;
	
	public AutoIntersectionClient(String serverIP, int port, String playerIP, int playerPort) {
		
//		this.flogger = flogger;
		
		lf = new LineFollower(playerIP, playerPort);
		rim = new RemoteIntersectionManager(lf, serverIP, port, this);
		lim = new LocalIntersectionManager(lf, this);
		li = new LaneIdentifier("/dev/ttyS1");
		
		// This class is a listener for line follower events.
		lf.addBlobDataConsumer(this);
		
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
			Logger.log("Success!  Returning to FOLLOW_LINE state...");
			currState = ClientManagerState.FOLLOW_LINE;
		} else {
			Logger.log("Fail!");
			if (currState == ClientManagerState.REMOTE_TRAVERSAL) {
				Logger.log("Switching to the LocalIntersectionManager...");
				currState = ClientManagerState.LOCAL_TRAVERSAL;
				lim.start(getLaneSpecs());
			} else {
				Logger.logErr("Unexpected state: " + currState + ", aborting demo...");
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
			Logger.log("The LocalIntersectionManager succeeded, returning to FOLLOW_LINE state...");
			currState = ClientManagerState.FOLLOW_LINE;
		} else {
			// At this point in time, we have no recourse but to abort the demo...
			Logger.logErr("The LocalIntersectionManager failed...");
			lf.stop();
			currState = ClientManagerState.IDLE;
		}
	}
	
	/**
	 * @return The specifications of the lane that the robot is traversing.
	 */
	private LaneSpecs getLaneSpecs() {
		return li.getCurrentLane();
	}
	
//	/**
//	 * This implements the LineFollowerEventListener interface.
//	 * It is called whenever the LineFollower generates an event.
//	 */
//	public void newLineFollowerEvent(LineFollowerEvent lfe, LineFollower follower) {
//		
//		// If the LineFollower fails, abort!
//		if (lfe.getType() == LineFollowerEvent.LineFollowerEventType.ERROR) {
//			Logger.log("Received error from the LineFollower, aborting demo...");
//			currState = ClientManagerState.IDLE;
//			lf.stop(); // There was an error, stop!
//		}
//		
//		// The only time the ClientManager is interested in a LineFollowerEvent
//		// is if it is in the FOLLOW_LINE state.
//		else if (currState == ClientManagerState.FOLLOW_LINE) {
//			if (lfe.getType() == LineFollowerEvent.LineFollowerEventType.APPROACHING) {
//				Logger.log("Robot is approaching intersection, activating RemoteIntersectionManager...");
//				currState = ClientManagerState.REMOTE_TRAVERSAL;
//				rim.start(getLaneSpecs());
//			} else
//				Logger.log("Discarding unexpected event from LineFollower: " + lfe);
//		} else
//			Logger.log("Ignoring LineFollowerEvent " + lfe + " because not in FOLLOW_LINE state");
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
//		String result = "ClientManager: " + msg;
//		if (!isDebugMsg || System.getProperty ("PharosMiddleware.debug") != null)
//			System.out.println(result);
//		if (flogger != null)
//			flogger.log(result);
//	}
	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(msg);
	}
	
	private static void usage() {
		System.setProperty ("PharosMiddleware.debug", "true");
		print("Usage: " + AutoIntersectionClient.class.getName() + " <options>\n");
		print("Where <options> include:");
		print("-type <intersection management type>: The type of intersection management to evaluate (centralized, adhoc, auto, default auto)");
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
					Logger.setFileLogger(new pharoslabut.logger.FileLogger(args[++i]));
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
		
		new AutoIntersectionClient(serverIP, serverPort, playerServerIP, playerServerPort);
	}

	@Override
	public void newBlobData(PlayerBlobfinderData blobData) {
		// TODO Auto-generated method stub
		
	}
}
