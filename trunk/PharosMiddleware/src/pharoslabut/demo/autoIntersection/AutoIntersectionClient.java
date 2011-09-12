package pharoslabut.demo.autoIntersection;

//import pharoslabut.logger.FileLogger;
import java.util.Vector;

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
public class AutoIntersectionClient implements IntersectionEventListener, Runnable {
	
	/**
	 * Defines the types of intersection management.
	 */
	public static enum IntersectionManagementType {CENTRALIZED, ADHOC, AUTO};
	
	/**
	 * Defines the possible states that the client manager can be in.
	 */
	public static enum ClientManagerState {IDLE, FOLLOW_LINE, REMOTE_TRAVERSAL, LOCAL_TRAVERSAL};
    
	private IntersectionManagementType mgrType;
	private ClientManagerState currState = ClientManagerState.IDLE;
	private LineFollower lf;
	private RemoteIntersectionManager rim;
	private LocalIntersectionManager lim;
	private LaneIdentifier li;
//	private IntersectionEvent ie = null;
	
	private Vector<IntersectionEvent> ieBuffer = new Vector<IntersectionEvent>();
	/**
	 * Whether this client should continue to run.
	 */
	private boolean done = false;
	
	/**
	 * This detects the intersection.
	 */
	private IntersectionDetector detector;
	
	/**
	 * The constructor.
	 * 
	 * @param mgrType The type of intersection management beign tested.
	 */
	public AutoIntersectionClient(IntersectionManagementType mgrType) {
		this.mgrType = mgrType;
		new Thread(this).start();
	}
	
	/**
	 * The constructor.
	 * 
	 * @param mgrType The type of intersection management beign tested.
	 * @param serverIP The intersection server's IP address.
	 * @param port The intersection server's IP port.
	 * @param playerIP The Player Server's IP address.
	 * @param playerPort The Player Server's port.
	 */
	public AutoIntersectionClient(IntersectionManagementType mgrType, String serverIP, int port, String playerIP, int playerPort) {
		this(mgrType);
		
		// Create the line follower
		Logger.log("Creating the line follower.");
		lf = new LineFollower(playerIP, playerPort);
		
		// Create the lane identifier
		Logger.log("Creating the lane identifier.");
		li = new LaneIdentifier("/dev/ttyS1");
		
		// Create the intersection detector.
		Logger.log("Creating the intersection detector.");
		detector = new IntersectionDetectorIR(lf.getOpaqueInterface());
		detector.addIntersectionEventListener(this);
		
		// Creating the managers
		switch(mgrType) {
		case CENTRALIZED:
			rim = new RemoteIntersectionManager(lf, detector, serverIP, port, this);
			break;
		case ADHOC:
			lim = new LocalIntersectionManager(lf, detector, this);
			break;
		case AUTO:
			// Auto switches between centralized and ad hoc management
			rim = new RemoteIntersectionManager(lf, detector, serverIP, port, this);
			lim = new LocalIntersectionManager(lf, detector, this);
		}
		
		// Start the line follower.  This starts the robot following the line.
		Logger.log("Starting the line follower.");
		currState = ClientManagerState.FOLLOW_LINE;
		lf.start();
	}
	
	@Override
	public void newIntersectionEvent(IntersectionEvent ie) {
		Logger.log("INTERSECTION EVENT: " + ie);
		ieBuffer.add(ie);
		
		Logger.logDbg("Calling notify on this.");
		synchronized(this) {
			this.notifyAll();
		}
	}
	
	/**
	 * This should be called whenever the RemoteIntersectionManager is done.
	 * 
	 * @param success Whether the RemoteIntersectionManager successfully traversed
	 * the intersection.
	 */
	public void remoteIntersectionMgrDone(boolean success) {
		if (success) {
			Logger.log("Success! Returning to FOLLOW_LINE state...");
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
	
	public void run() {
		Logger.log("Thread starting.");
		
		while(!done) {
			if (ieBuffer.isEmpty()) {
				Logger.log("Waiting for next intersection event to occur.");
				try {
					synchronized(this) {
						wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			if (!ieBuffer.isEmpty()) {
				IntersectionEvent ie = ieBuffer.remove(0);
				Logger.log("Detected IntersectionEvent " + ie + ", sending it to the server.");
				IntersectionEvent currIE = ie;
				rim.sendToServer(currIE);
			} else {
				Logger.log("Thread awoken but Intersection Event was null!");
			}
			try {
				wait(250);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @return The specifications of the lane that the robot is traversing.
	 */
	private LaneSpecs getLaneSpecs() {
		return li.getCurrentLane();
	}
	
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
		IntersectionManagementType mgrType = IntersectionManagementType.AUTO;
		
		String serverIP = null;
		int serverPort = -1;
		
		String playerServerIP = "localhost";
		int playerServerPort = 6665;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-type")) {
					String type = args[++i];
					if (type.equals("auto"))
						mgrType = IntersectionManagementType.AUTO;
					else if (type.equals("adhoc"))
						mgrType = IntersectionManagementType.ADHOC;
					else if (type.equals("centralized"))
						mgrType = IntersectionManagementType.CENTRALIZED;
				} else if (args[i].equals("-server")) {
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
		
		new AutoIntersectionClient(mgrType, serverIP, serverPort, playerServerIP, playerServerPort);
	}
}
