package pharoslabut.demo.autoIntersection2;

import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionDetector;
import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionDetectorIR;
import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionEvent;
import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionEventListener;
import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionEventType;
import pharoslabut.exceptions.PharosException;
import pharoslabut.io.Message;
import pharoslabut.io.MessageReceiver;
import pharoslabut.io.SetTimeMsg;
import pharoslabut.io.TCPMessageReceiver;
import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.LineFollower2;
import pharoslabut.navigate.MotionArbiter;
import pharoslabut.sensors.BlobDataProvider;
import pharoslabut.sensors.PathLocalizerOverheadMarkers;
import pharoslabut.sensors.Position2DBuffer;
import pharoslabut.sensors.ProteusOpaqueData;
import pharoslabut.sensors.ProteusOpaqueInterface;
import pharoslabut.sensors.ProteusOpaqueListener;
import pharoslabut.sensors.RangerDataBuffer;
import playerclient3.BlobfinderInterface;
import playerclient3.PlayerClient;
import playerclient3.PlayerException;
import playerclient3.Position2DInterface;
import playerclient3.PtzInterface;
import playerclient3.RangerInterface;
import playerclient3.structures.PlayerConstants;

/**
 * The top-level class of the autonomous intersection demo client.
 * 
 * @author Chien-Liang Fok
 */
public class AutoIntersectionClient implements MessageReceiver, ProteusOpaqueListener, IntersectionEventListener {
	enum ExpType {NORMAL, ACCIDENT, EVADE};
	
	/**
	 * The name of the local vehicle.
	 */
	private String vehicleName;
	
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
	 * This is the component that allows the robot to follow a line.
	 */
	private LineFollower2 lineFollower;
	
	/**
	 * This localizes the robot by detecting overhead markers
	 * and using odometry.
	 */
	private PathLocalizerOverheadMarkers pathLocalizer;
	
	/**
	 * The lane identifier.
	 */
//	private LaneIdentifier laneID;
	
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
	 * A buffer for incoming position2d informatino.
	 */
	private Position2DBuffer pos2DBuffer;
	
	private ExpType expType;
	
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
			MotionArbiter.MotionType mobilityPlane, ExpType expType) 
	{
		
		this.playerIP = playerIP;
		this.playerPort = playerPort;
		this.port = port;
		this.expType = expType;
		
		/*
		 * If we're running in debug mode, start logging debug statements even before the experiment begins.
		 */
		if (System.getProperty ("PharosMiddleware.debug") != null) {
			debugFileLogger = new FileLogger("AutoIntersectionClient-" + FileLogger.getUniqueNameExtension() + ".log");
			Logger.setFileLogger(debugFileLogger);
			Logger.log("Creating a " + getClass().getName() + "...");
		}
		
		// Get the vehicle's name...		
		try {
			vehicleName = pharoslabut.RobotIPAssignments.getName();
			Logger.log("Vehicle name: " + vehicleName);
		} catch (PharosException e1) {
			Logger.logErr("Unable to get vehicle's name, using 'JohnDoe'");
			vehicleName = "JohnDoe";
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
//		Logger.log("Creating the lane identifier...");
//		laneID = new LaneIdentifier("/dev/ttyS1");
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
		pos2DBuffer = new Position2DBuffer(p2di);
		pos2DBuffer.start();
		Logger.logDbg("Subscribed to Position2d proxy.");
		
		BlobfinderInterface bfi = null;
		try {
			bfi = playerClient.requestInterfaceBlobfinder(0, PlayerConstants.PLAYER_OPEN_MODE);
		} catch (PlayerException e) { Logger.logErr("Could not connect to blob finder proxy."); System.exit(1);}
		Logger.log("Subscribed to BlobFinder.");
		
		PtzInterface ptz = null;
		try {
			ptz = playerClient.requestInterfacePtz(0, PlayerConstants.PLAYER_OPEN_MODE);
		} catch (PlayerException e) { Logger.logErr("Could not connect to PTZ proxy."); System.exit(1);}
		Logger.logDbg("Subscribed to camera PTZ.");
		
		// Start the PathLocalizerOverheadMarkers
		pathLocalizer = new PathLocalizerOverheadMarkers(rangerBuffer, pos2DBuffer);
		Logger.log("Created the PathLocalizerOverheadMarkers.");
		
		intersectionDetector = new IntersectionDetectorIR(pathLocalizer);
		intersectionDetector.addIntersectionEventListener(this);
		
		BlobDataProvider bdp = new BlobDataProvider(bfi);
		
		// Start the robot following the line
		lineFollower = new LineFollower2(bdp, p2di, ptz);
		lineFollower.setMaxSpeed(0.5);
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
		case RESET:
			reset();
			break;
		case STARTEXP:
			Logger.log("Starting experiment...");
			startExp((StartExpMsg)msg);
			break;
		case STOPEXP:
			Logger.log("Stopping experiment...");
			stopExp();
			break;
		case CUSTOM:
			break;
		default:
			Logger.log("Unknown Message: " + msg);
			System.exit(1);
		}
	}
	
	@Override
	public void newIntersectionEvent(IntersectionEvent ie) {
		Logger.log("new event: " + ie);
		if (ie.getType() == IntersectionEventType.EXITING) 
			lineFollower.stop();
		else if (ie.getType() == IntersectionEventType.ENTERING) {
			if (expType == ExpType.ACCIDENT && vehicleName.equals("CZECHVAR")) {
				
				// The following pause ensures the robot goes into the intersection before 
				// the mechanical failure.
				Logger.log("Pausing 2 seconds to allow robot to enter intersection.");
				synchronized(this) {
					try {
						this.wait(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				Logger.log("Pausing line follower to simulate accident.");
				lineFollower.pause();
			}
			else if (expType == ExpType.EVADE) {
				if (vehicleName.equals("CZECHVAR")) {
					// The following pause ensures the robot goes into the intersection before 
					// the mechanical failure.
					Logger.log("Pausing 2 seconds to allow robot to enter intersection.");
					synchronized(this) {
						try {
							this.wait(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					Logger.log("Pausing line follower to simulate accident.");
					lineFollower.pause();
				} else {
					// Make Ziegen swerve to the left to avoid colliding with Czechvar.
					lineFollower.override(20, 0.5);
					synchronized(this) {
						try {
							this.wait(1750);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					lineFollower.override(-20, 0.5);
					synchronized(this) {
						try {
							this.wait(3500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					lineFollower.override(20, 0.5);
					synchronized(this) {
						try {
							this.wait(1750);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					Logger.log("Pausing line follower to simulate accident.");
					lineFollower.pause();
				}
			}
		}
	}
	
	/**
	 * Starts the experiment.
	 * 
	 * @param startExpMsg The start experiment message.
	 */
	private void startExp(StartExpMsg startExpMsg) {
		
		// Start the file logger and set it in the Logger.
		String fileName = expType + "-" + vehicleName 
			+ "-AutoIntersectionClient_" + FileLogger.getUniqueNameExtension() + ".log"; 
		FileLogger expFlogger = new FileLogger(fileName);
		Logger.setFileLogger(expFlogger);
		
		Logger.log("Starting experiment at time " + System.currentTimeMillis() + "...");
		
		pathLocalizer.start();

        Logger.log("Experiment type: " + expType);
        
        switch (expType) {
        case NORMAL:
        case ACCIDENT:
        case EVADE:    
        	// In the all three experiments, start the line follower.
        	Logger.log("Starting the line follower.");
        	lineFollower.start();
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
		print("\t-mobilityPlane <traxxas|segway|create>: The type of mobility plane being used (default traxxas)");
		print("\t-expType <exp type>: The type of experiment.  Valid values include normal, accident, evade (default normal)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		int port = 7776;
		String playerServerIP = "localhost";
		int playerServerPort = 6665;
		MotionArbiter.MotionType mobilityPlane = MotionArbiter.MotionType.MOTION_TRAXXAS;
		ExpType expType = ExpType.NORMAL;
		
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
				} else if (args[i].equals("-expType")) {
					String et = args[++i].toLowerCase();
					if (et.equals("normal"))
						expType = ExpType.NORMAL;
					else if (et.equals("accident"))
						expType = ExpType.ACCIDENT;
					else if (et.equals("evade"))
						expType = ExpType.EVADE;
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
		
		new AutoIntersectionClient(port, playerServerIP, playerServerPort, mobilityPlane, expType);
	}
}
