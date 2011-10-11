package pharoslabut.demo.autoIntersection;

import java.net.InetAddress;
import java.net.UnknownHostException;

import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionDetector;
import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionDetectorIR;
import pharoslabut.demo.autoIntersection.msgs.AutoIntersectionMsg;
import pharoslabut.demo.autoIntersection.msgs.LoadExpSettingsMsg;
import pharoslabut.demo.autoIntersection.msgs.StartAdHocExpMsg;
import pharoslabut.demo.autoIntersection.msgs.StartCentralizedExpMsg;
import pharoslabut.exceptions.PharosException;
import pharoslabut.io.Message;
import pharoslabut.io.MessageReceiver;
import pharoslabut.io.SetTimeMsg;
import pharoslabut.io.TCPMessageReceiver;
import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.LineFollower;
import pharoslabut.navigate.MotionArbiter;
import pharoslabut.sensors.PathLocalizerOverheadMarkers;
import pharoslabut.sensors.Position2DBuffer;
import pharoslabut.sensors.ProteusOpaqueData;
import pharoslabut.sensors.ProteusOpaqueInterface;
import pharoslabut.sensors.ProteusOpaqueListener;
import pharoslabut.sensors.RangerDataBuffer;
import playerclient3.PlayerClient;
import playerclient3.PlayerException;
import playerclient3.Position2DInterface;
import playerclient3.RangerInterface;
import playerclient3.structures.PlayerConstants;

/**
 * The top-level class of the autonomous intersection demo client.
 * 
 * @author Chien-Liang Fok
 */
public class AutoIntersectionClient implements MessageReceiver, ProteusOpaqueListener {
	
	/**
	 * The name of the local robot.
	 */
	private String robotName;
	
	/**
	 * The IP address of the player server.
	 */
	private String playerIP;
	
	/**
	 * The TCP port on which the player server listens.
	 */
	private int playerPort;
	
    /**
     * The player client that connects to the player server.
     */
	private PlayerClient playerClient = null;
	
	/**
	 * This TCP port on which this server listens.
	 */
	private int port;
	
	/**
	 * The experiment settings.
	 */
	private LoadExpSettingsMsg settings;
	
	/**
	 * This is the component that allows the robot to follow a line.
	 */
	private LineFollower lineFollower;
	
	/**
	 * This localizes the robot by detecting overhead markers
	 * and using odometry.
	 */
	private PathLocalizerOverheadMarkers pathLocalizer;
	
	/**
	 * The lane identifier.
	 */
	private LaneIdentifier laneID;
	
	/**
	 * This detects the intersection.
	 */
	private IntersectionDetector intersectionDetector;
	
	/**
	 * The data buffer for storing ranger data.
	 */
	private RangerDataBuffer rangerBuffer;
	
	/**
	 * This is the file logger that is used for debugging purposes.  It is used when debug mode is enabled
	 * and there is no experiments running.
	 */
	private FileLogger debugFileLogger = null;
	
	/**
	 * The daemon that actually carries out the traversal of the intersection.
	 */
	private ClientDaemon daemon;
	
	/**
	 * The constructor.
	 * 
	 * @param port The TCP port on on which this client listens.  This is for receiving messages
	 * from the ExpMgr.
	 * @param playerIP The Player Server's IP address.
	 * @param playerPort The Player Server's port.
	 * @param mobilityPlane The type of mobility plane being used.
	 */
	public AutoIntersectionClient(int port, String playerIP, int playerPort, 
			MotionArbiter.MotionType mobilityPlane) 
	{
		
		this.playerIP = playerIP;
		this.playerPort = playerPort;
		this.port = port;
		
		/*
		 * If we're running in debug mode, start logging debug statements even before the experiment begins.
		 */
		if (System.getProperty ("PharosMiddleware.debug") != null) {
			debugFileLogger = new FileLogger("PharosExpServer-" + FileLogger.getUniqueNameExtension() + ".log");
			Logger.setFileLogger(debugFileLogger);
			Logger.log("Creating a " + getClass().getName() + "...");
		}
		
		// Get the robot's name...		
		try {
			robotName = pharoslabut.RobotIPAssignments.getName();
			Logger.log("Robot name: " + robotName);
		} catch (PharosException e1) {
			Logger.logErr("Unable to get robot's name, using 'JohnDoe'");
			robotName = "JohnDoe";
			e1.printStackTrace();
		}
		
		// Initialize the autonomous intersection client.
		if (!initClient()) {
			Logger.logErr("Failed to initialize the Pharos server!");
			System.exit(1);
		}
		
		// Create the player client.
		if (!createPlayerClient(mobilityPlane)) {
			Logger.logErr("Failed to connect to Player server!");
			System.exit(1);
		}
		
		// Create the lane identifier
		Logger.log("Creating the lane identifier...");
		laneID = new LaneIdentifier("/dev/ttyS1");
	}
	
	/**
	 * Creates the player client and obtains the necessary interfaces from it.
	 * 
	 * @return true if successful.
	 */
	private boolean createPlayerClient(MotionArbiter.MotionType mobilityPlane) {
		
		// Connect to the player server.
		try {
			playerClient = new PlayerClient(playerIP, playerPort);
		} catch(PlayerException e) {
			Logger.logErr("Unable to connecting to Player: ");
			Logger.logErr("    [ " + e.toString() + " ]");
			return false;
		}
		Logger.log("Created player client.");
		
		// Subscribe to the ranger proxy.
		RangerInterface ri = playerClient.requestInterfaceRanger(0, PlayerConstants.PLAYER_OPEN_MODE);
		rangerBuffer = new RangerDataBuffer(ri);
		rangerBuffer.start();
		Logger.log("Subscribed to the ranger proxy.");
		
		Position2DInterface p2di = playerClient.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (p2di == null) {
			Logger.logErr("motors is null");
			return false;
		}
		Position2DBuffer pos2DBuffer = new Position2DBuffer(p2di);
		pos2DBuffer.start();
		Logger.logDbg("Subscribed to Position2d proxy.");
		
		// Start the PathLocalizerOverheadMarkers
		pathLocalizer = new PathLocalizerOverheadMarkers(rangerBuffer, pos2DBuffer);
		Logger.log("Created the PathLocalizerOverheadMarkers.");
		
		intersectionDetector = new IntersectionDetectorIR(pathLocalizer);
		
		// Start the robot following the line
		lineFollower = new LineFollower(playerClient);
		Logger.log("Created the line follower.");
		
		Logger.log("Subscribing to opaque interface.");
		ProteusOpaqueInterface oi = (ProteusOpaqueInterface)playerClient.requestInterfaceOpaque(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (oi == null) {
			Logger.logErr("opaque interface is null");
			return false;
		} else {
			oi.addOpaqueListener(this);
			Logger.log("Subscribed to opaque proxy.");
		}
		
		Logger.log("Changing Player server mode to PUSH...");
		playerClient.requestDataDeliveryMode(playerclient3.structures.PlayerConstants.PLAYER_DATAMODE_PUSH);
		
		Logger.log("Setting Player Client to run in continuous threaded mode...");
		playerClient.runThreaded(-1, -1);
		
		return true;
	}
	
	private boolean initClient() {
		Logger.log("Initializing on port " + port);
		new TCPMessageReceiver(this, port);
		return true;
	}
	
	/**
	 * This is called whenever a message is received.
	 */
	@Override
	public void newMessage(Message msg) {
		Logger.log("Received message: " + msg);
		switch(msg.getType()) {
		case SET_TIME:
			setSystemTime(((SetTimeMsg)msg).getTime());
			break;
		case LOAD_SETTINGS:
			// Simply save the message.  This message will be used
			// when the start experiment message arrives.
			this.settings = (LoadExpSettingsMsg)msg;  
			break;
		case RESET:
			reset();
			break;
		case STARTEXP:
			Logger.log("Starting experiment...");
			startExp((StartAdHocExpMsg)msg);
			break;
		case STOPEXP:
			Logger.log("Stopping experiment...");
			stopExp();
			break;
		case CUSTOM:
			if (msg instanceof AutoIntersectionMsg) {
				
				// pass the message to clients.
				daemon.messageReceived((AutoIntersectionMsg)msg);
			}
			break;
		default:
			Logger.log("Unknown Message: " + msg);
		}
	}
	
	/**
	 * Starts the experiment.
	 * 
	 */
	private void startExp(StartAdHocExpMsg startExpMsg) {
		
		// Start the file logger and set it in the Logger.
		String fileName = startExpMsg.getExpName() + "-" + robotName 
			+ "-IndoorMRPatrol_" + FileLogger.getUniqueNameExtension() + ".log"; 
		FileLogger expFlogger = new FileLogger(fileName);
		Logger.setFileLogger(expFlogger);
		
		Logger.log("Starting experiment at time " + System.currentTimeMillis() + "...");
		
		// Start the individual components
//		if (compassDataBuffer != null)			compassDataBuffer.start();
		
		pathLocalizer.start();
        
        ExpType expType = startExpMsg.getExpType();

        Logger.log("Patrol type: " + expType);
        
        switch (expType) {
        case CENTRALIZED:
        	StartCentralizedExpMsg msg = (StartCentralizedExpMsg)startExpMsg;

        	InetAddress serverIP = null;
        	try {
        		serverIP = InetAddress.getByName(msg.getServerIP());
        	} catch (UnknownHostException e) {
        		Logger.logErr("Unable to get serverIP : " + e);
        		e.printStackTrace();
        		System.exit(1);
        	}

        	daemon = new CentralizedClientDaemon(serverIP, msg.getServerPort(), port,
        			lineFollower, intersectionDetector, 
        			settings.getEntryID(), settings.getExitID());
        	break;
        case ADHOC:
        	daemon = new AdHocClientDaemon(lineFollower, intersectionDetector);
        	break;
        }
	}
	
	/**
	 * Stops the experiment.
	 */
	private void stopExp() {
		Logger.log("Stopping the experiment.");
		
		// Restore the debug file logger since the experiment has stopped.
		Logger.logDbg("Stopping experiment log file.");
		Logger.setFileLogger(debugFileLogger);
	}
	
	private void reset() {
		// TODO
	}
	
	@Override
	public void newOpaqueData(ProteusOpaqueData opaqueData) {
		if (opaqueData.getDataCount() > 0) {
			String s = new String(opaqueData.getData());
			Logger.log("MCU Message: " + s);
		}
	}
	
	/**
	 * Sets the local system time.
	 * 
	 * @param time The parameter to the 'date' command
	 */
	private void setSystemTime(String time) {
		try {
			Runtime rt = Runtime.getRuntime();
			String cmd = "sudo date " + time;
			Process pr = rt.exec(cmd);
			int exitVal = pr.waitFor();
			if (exitVal == 0)
				Logger.log("Successfully set system time to " + time);
			else
				Logger.logErr("Failed to set system time to " + time + ", error code " + exitVal);
		} catch(Exception e) {
			Logger.logErr("Unable to set system time, error: " + e.toString());
		}
	}
	

	
//	/**
//	 * This should be called whenever the RemoteIntersectionManager is done.
//	 * 
//	 * @param success Whether the RemoteIntersectionManager successfully traversed
//	 * the intersection.
//	 */
//	public void remoteIntersectionMgrDone(boolean success) {
//		if (success) {
//			Logger.log("Success! Returning to FOLLOW_LINE state...");
//			currState = ClientManagerState.FOLLOW_LINE;
//		} else {
//			Logger.log("Fail!");
//			if (currState == ClientManagerState.REMOTE_TRAVERSAL) {
//				Logger.log("Switching to the LocalIntersectionManager...");
//				currState = ClientManagerState.LOCAL_TRAVERSAL;
//				lim.start(getLaneSpecs());
//			} else {
//				Logger.logErr("Unexpected state: " + currState + ", aborting demo...");
//				lf.stop();
//				currState = ClientManagerState.IDLE;
//			}
//		}
//	}
//	
//	/**
//	 * This should be called whenever the LocalIntersectionManager is done.
//	 * 
//	 * @param success Whether the LocalIntersectionManager successfully traversed
//	 * the intersection.
//	 */
//	public void localIntersectionMgrDone(boolean success) {
//		if (success) {
//			Logger.log("The LocalIntersectionManager succeeded, returning to FOLLOW_LINE state...");
//			currState = ClientManagerState.FOLLOW_LINE;
//		} else {
//			// At this point in time, we have no recourse but to abort the demo...
//			Logger.logErr("The LocalIntersectionManager failed...");
//			lf.stop();
//			currState = ClientManagerState.IDLE;
//		}
//	}
//	
//	public void run() {
//		Logger.log("Thread starting.");
//		
//		while(!done) {
//			if (ie == null) {
//				Logger.log("Waiting for next intersection event to occur.");
//				try {
//					synchronized(this) {
//						wait();
//					}
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//			
//			if (ie != null) {
//				Logger.log("Detected IntersectionEvent " + ie + ", sending it to the server.");
//				IntersectionEvent currIE = ie;
//				rim.sendToServer(currIE);
//			} else {
//				Logger.log("Thread awoken but Intersection Event was null!");
//			}
//			try {
//				Thread.sleep(200);
//			} catch (InterruptedException e1) {
//				e1.printStackTrace();
//			}
//		}
//	}
//	
//	/**
//	 * @return The specifications of the lane that the robot is traversing.
//	 */
//	private LaneSpecs getLaneSpecs() {
//		return li.getCurrentLane();
//	}
	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(msg);
	}
	
	private static void usage() {
		System.setProperty ("PharosMiddleware.debug", "true");
		print("Usage: " + AutoIntersectionClient.class.getName() + " <options>\n");
		print("Where <options> include:");
		print("\t-port <port>: The port on which the this client listens (default 7776).");
		print("\t-playerIP <ip address>: The IP address of the Player Server (default localhost)");
		print("\t-playerPort <port number>: The Player Server's port number (default 6665)");
		print("\t-log <log file name>: The name of the file in which to save debug output (default null)");
		print("\t-mobilityPlane <traxxas|segway|create>: The type of mobility plane being used (default traxxas)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		int port = 7776;
		String playerServerIP = "localhost";
		int playerServerPort = 6665;
		MotionArbiter.MotionType mobilityPlane = MotionArbiter.MotionType.MOTION_TRAXXAS;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-port")) {
					port = Integer.valueOf(args[++i]);
				} else if (args[i].equals("-playerIP")) {
					playerServerIP = args[++i];
				} else if (args[i].equals("-playerPort")) {
					playerServerPort = Integer.valueOf(args[++i]);
				} else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				} else if (args[i].equals("-log")) {
					Logger.setFileLogger(new pharoslabut.logger.FileLogger(args[++i]));
				} else if (args[i].equals("-mobilityPlane")) {
					String mp = args[++i].toLowerCase();
					if (mp.equals("traxxas"))
						mobilityPlane = MotionArbiter.MotionType.MOTION_TRAXXAS;
					else if (mp.equals("segway"))
						mobilityPlane = MotionArbiter.MotionType.MOTION_SEGWAY_RMP50;
					else if (mp.equals("create"))
						mobilityPlane = MotionArbiter.MotionType.MOTION_IROBOT_CREATE;
					else {
						System.err.println("Unknown mobility plane " + mp);
						usage();
						System.exit(1);
					}
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
		
		new AutoIntersectionClient(port, playerServerIP, playerServerPort, mobilityPlane);
	}
}
