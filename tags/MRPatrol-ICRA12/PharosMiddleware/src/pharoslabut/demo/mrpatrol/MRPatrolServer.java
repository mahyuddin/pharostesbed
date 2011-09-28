package pharoslabut.demo.mrpatrol;

//import java.net.InetAddress;
//import java.net.UnknownHostException;
//
//import pharoslabut.RobotIPAssignments;
//import pharoslabut.beacon.WiFiBeacon;
//import pharoslabut.beacon.WiFiBeaconBroadcaster;
import pharoslabut.beacon.WiFiBeaconEvent;
import pharoslabut.beacon.WiFiBeaconListener;
//import pharoslabut.beacon.WiFiBeaconReceiver;
import pharoslabut.exceptions.PharosException;
import pharoslabut.experiment.ExpType;
//import pharoslabut.experiment.PharosExpServer;
import pharoslabut.io.*;
import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.MotionArbiter;
//import pharoslabut.navigate.MotionScriptFollower;
import pharoslabut.navigate.MotionScriptFollowerDoneListener;
import pharoslabut.navigate.NavigateCompassGPS;
//import pharoslabut.navigate.NavigateRelative;
//import pharoslabut.navigate.RelativeMotionScript;
//import pharoslabut.navigate.Scooter;
//import pharoslabut.navigate.motionscript.MotionScript;
//import pharoslabut.radioMeter.cc2420.TelosBeaconBroadcaster;
//import pharoslabut.radioMeter.cc2420.TelosBeaconException;
import pharoslabut.sensors.CompassDataBuffer;
import pharoslabut.sensors.GPSDataBuffer;
import pharoslabut.sensors.ProteusOpaqueData;
import pharoslabut.sensors.ProteusOpaqueInterface;
import pharoslabut.sensors.ProteusOpaqueListener;
import playerclient3.GPSInterface;
import playerclient3.PlayerClient;
import playerclient3.PlayerException;
import playerclient3.Position2DInterface;
import playerclient3.structures.PlayerConstants;
import pharoslabut.behavior.MultiRobotBehaveMsg;
import pharoslabut.behavior.MultiRobotTableMsg;
import pharoslabut.behavior.management.*;

// use pharoslabut.experiment.PharosExpServer as an example.
public class MRPatrolServer implements MessageReceiver, WiFiBeaconListener, ProteusOpaqueListener, MotionScriptFollowerDoneListener {
	private String robotName;
	
	private String playerServerIP;
	private int playerServerPort;
	private int pharosServerPort;
	
    /**
	 * The WiFi multicast group address.
	 */
//    private String mCastAddress;
    
    /**
	 * The WiFi multicast port.
	 */
//    private int mCastPort;
	
	private PlayerClient client = null;
	private CompassDataBuffer compassDataBuffer;
	private GPSDataBuffer gpsDataBuffer;
	private MotionArbiter motionArbiter;
	private Manager manageMRP;

	
	// Components for sending and receiving WiFi beacons
//	private WiFiBeaconBroadcaster wifiBeaconBroadcaster;
//	private WiFiBeaconReceiver wifiBeaconReceiver;
	
	// Components for sending and receiving TelosB beacons
//	private TelosBeaconBroadcaster telosRadioSignalMeter;
	
//	private MotionScript gpsMotionScript;
//	private RelativeMotionScript relMotionScript;
	private MRPConfData mrpConfdata;
	
	private FileLogger debugFileLogger = null;
	
	/**
	 * For communicating with other MRPatrolServers...
	 */
	private TCPMessageSender msgSender = TCPMessageSender.getSender();

	/**
	 * The constructor.  Starts the server running.
	 * 
	 * @param playerServerIP The player server's IP address.
	 * @param playerServerPort The player server's port.
	 * @param pharosServerPort This server's port.
	 * @param mCastAddress The multicast address over which to broadcast WiFi beacons.
	 * @param mCastPort the multicast port over which to broadcast WiFi beacons.
	 * @param mobilityPlane The type of mobility plane being used.
	 */
	public MRPatrolServer(String playerServerIP, int playerServerPort, int pharosServerPort, 
			/*String mCastAddress, int mCastPort,*/ MotionArbiter.MotionType mobilityPlane) 
	{
//		boolean SimulateAll = false;
		this.playerServerIP = playerServerIP;
		this.playerServerPort = playerServerPort;
		this.pharosServerPort = pharosServerPort;
		
		/*
		 * If we're running in debug mode, start logging debug statements even before the experiment begins.
		 */
		if (System.getProperty ("PharosMiddleware.debug") != null) {
			debugFileLogger = new FileLogger("MRPatrolServer-" + FileLogger.getUniqueNameExtension() + ".log");
			Logger.setFileLogger(debugFileLogger);
			Logger.log("Creating a PharosExpServer...");
		}
		
//		this.mCastAddress = mCastAddress;
//		this.mCastPort = mCastPort;
		
		// create the simulation environment
		//if(System.getProperty ("simulateBehave") != null)
		//	SimulateAll = true;
		
		// Get the robot's name...		
		try {
			robotName = pharoslabut.RobotIPAssignments.getName();
			Logger.log("Robot name: " + robotName);
		} catch (PharosException e1) {
			Logger.logErr("Unable to get robot's name, using 'JohnDoe'");
			robotName = "JohnDoe";
			e1.printStackTrace();
		}
		
		if (!initPharosServer()) {
			Logger.logErr("Failed to initialize the Pharos server!");
			System.exit(1);
		}
		
		if (System.getProperty ("simulateBehave") == null) {
			if (!createPlayerClient(mobilityPlane)) {
				Logger.logErr("Failed to connect to Player server!");
				System.exit(1);
			}
		} else
			Logger.log("Running in simulation mode, not connecting to player server.");
		
//		if ((System.getProperty ("PharosMiddleware.disableWiFiBeacons") == null) && !SimulateAll) {
//			if (!initWiFiBeacons()) {
//				logErr("Failed to initialize the WiFi beaconer!");
//				System.exit(1);
//			}
//		} else
//			log("WiFi beacons disabled.");
		
//		if ((System.getProperty ("PharosMiddleware.disableTelosBBeacons") == null) && !SimulateAll){
//			if (!initTelosBeacons()) {
//				logErr("Failed to initialize TelosB beaconer!");
//				System.exit(1);
//			}
//		} else
//			log("TelosB beacons disabled.");
	}
	
	/**
	 * Creates the player client and obtains the necessary interfaces from it.
	 * Creates the CompassDataBuffer, GPSDataBuffer, and MotionArbiter objects.
	 * 
	 * @return true if successful.
	 */
	private boolean createPlayerClient(MotionArbiter.MotionType mobilityPlane) {
		Logger.log("Creating Player Client");
		try {
			Logger.log("Creating player client, conneting to " + playerServerIP + ":" + playerServerPort);
			client = new PlayerClient(playerServerIP, playerServerPort);
		} catch(PlayerException e) {
			Logger.logErr("Unable to connecting to Player: ");
			Logger.logErr("    [ " + e.toString() + " ]");
			return false;
		}
		
		Logger.log("Subscribing to motors.");
		Position2DInterface motors = client.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (motors == null) {
			Logger.logErr("motors is null");
			return false;
		} else
			Logger.log("Successfully subscribed to motors.");
		
		// The Traxxas and Segway mobility planes' compasses are Position2D devices at index 1,
		// while the Segway RMP 50's compass is on index 2.
		Logger.log("Subscribing to compass.");
		Position2DInterface compass;
		if (mobilityPlane == MotionArbiter.MotionType.MOTION_IROBOT_CREATE ||
				mobilityPlane == MotionArbiter.MotionType.MOTION_TRAXXAS) {
			compass = client.requestInterfacePosition2D(1, PlayerConstants.PLAYER_OPEN_MODE);
		} else
			compass = client.requestInterfacePosition2D(2, PlayerConstants.PLAYER_OPEN_MODE);
		if (compass == null) {
			Logger.logErr("compass is null");
			return false;
		} else
			Logger.log("Successfully subscribed to compass.");
		
		Logger.log("Subscribing to GPS.");
		GPSInterface gps = client.requestInterfaceGPS(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (gps == null) {
			Logger.logErr("gps is null");
			return false;
		} else
			Logger.log("Successfully subscribed to GPS.");
		
		Logger.log("Subscribing to opaque interface.");
		ProteusOpaqueInterface oi = (ProteusOpaqueInterface)client.requestInterfaceOpaque(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (oi == null) {
			Logger.logErr("opaque interface is null");
			return false;
		} else
			Logger.log("Successfully subscribed to opaque interface.");
		
		compassDataBuffer = new CompassDataBuffer(compass);
		
		gpsDataBuffer = new GPSDataBuffer(gps);
		if(gpsDataBuffer == null){
			Logger.logErr("failed to create GPS Data Buffer.");
			System.exit(1);
		} else
			Logger.log("Successfully created GPS Data Buffer");
		
		motionArbiter = new MotionArbiter(mobilityPlane, motors);
		oi.addOpaqueListener(this);
		
		Logger.log("Changing Player server mode to PUSH...");
		client.requestDataDeliveryMode(playerclient3.structures.PlayerConstants.PLAYER_DATAMODE_PUSH);
		
		Logger.log("Setting Player Client to run in continuous threaded mode...");
		client.runThreaded(-1, -1);
		
		Logger.log("Done Creating Player Client");
		return true;
	}
	
	/**
	 * Initializes the Pharos server.  This consists of starting the TCPMessageReceiver.
	 * 
	 * @return true if successful.
	 */
	private boolean initPharosServer() {
		new TCPMessageReceiver(this, pharosServerPort);
		return true;
	}
	
//	private boolean initWiFiBeacons() {
//    	// Obtain the multicast address		
//		InetAddress mCastGroupAddress = null;
//		try {
//			mCastGroupAddress = InetAddress.getByName(mCastAddress);
//		} catch (UnknownHostException uhe) {
//			logErr("initWiFiBeacons: Problems getting multicast address");
//			uhe.printStackTrace();
//			return false;
//		}
//		
//		String pharosIP;
//		try {
//			pharosIP = RobotIPAssignments.getAdHocIP();
//		} catch (PharosException e1) {
//			logErr("initWiFiBeacons: Unable to get ad hoc IP address: " + e1.getMessage());
//			e1.printStackTrace();
//			return false;
//		}
//		
//		String pharosNI;
//		try {
//			pharosNI = RobotIPAssignments.getAdHocNetworkInterface();
//		} catch (PharosException e1) {
//			logErr("initWiFiBeacons: Unable to get ad hoc network interface: " + e1.getMessage());
//			e1.printStackTrace();
//			return false;
//		}
//		
//		if (pharosIP == null || pharosNI == null) {
//			logErr("initWiFiBeacons: Unable to get pharos IP or pharos network interface...");
//			return false;
//		}
//		
//		try {
//			WiFiBeacon beacon = new WiFiBeacon(InetAddress.getByName(pharosIP), pharosServerPort);
//			wifiBeaconReceiver = new WiFiBeaconReceiver(mCastAddress, mCastPort, pharosNI);
//			wifiBeaconBroadcaster = new WiFiBeaconBroadcaster(mCastGroupAddress, pharosIP, mCastPort, beacon);
//
//			// Start receiving beacons
//			wifiBeaconReceiver.start();
//			return true;
//		} catch (UnknownHostException e) {
//			logErr("initWiFiBeacons: Problem initializing WiFi beacons: " + e.getMessage());
//			e.printStackTrace();
//		}
//		return false;
//	}
//	
//	private boolean initTelosBeacons() {
//		try {
//			telosRadioSignalMeter = new pharoslabut.radioMeter.cc2420.TelosBeaconBroadcaster();
//		} catch (TelosBeaconException e) {
//			logErr("initTelosBeacons: Problem initializing TelosB beacons: " + e.getMessage());
//			e.printStackTrace();
//			return false;
//		}
//		return true;
//	}
	
	/**
	 * Sets the local system time.
	 * 
	 * @param time The parameter to the 'date' command
	 */
//	private void setSystemTime(String time) {
//		try {
//			Runtime rt = Runtime.getRuntime();
//			String cmd = "sudo date " + time;
//			Process pr = rt.exec(cmd);
//			int exitVal = pr.waitFor();
//			if (exitVal == 0)
//				log("setSystemTime: Successfully set system time to " + time);
//			else
//				logErr("setSystemTime: Failed to set system time to " + time + ", error code " + exitVal);
//		} catch(Exception e) {
//			logErr("setSystemTime: Unable to set system time, error: " + e.toString());
//		}
//	}
	
	/**
	 * This is called whenever a message is received.
	 */
	@Override
	public void newMessage(Message msg) {
		Logger.log("Received message: " + msg.getType());
		switch(msg.getType()) {
//		case SET_TIME:
//			setSystemTime(((SetTimeMsg)msg).getTime());
//			break;
//		case LOAD_GPS_MOTION_SCRIPT:
//			log("newMessage: Loading GPS-based motion script...");
//			MotionScriptMsg gpsMSMsg = (MotionScriptMsg)msg;
//			gpsMotionScript = gpsMSMsg.getScript();
//			break;
//		case LOAD_RELATIVE_MOTION_SCRIPT:
//			log("newMessage: Loading relative motion script...");
//			RelativeMotionScriptMsg relMMsg = (RelativeMotionScriptMsg)msg;
//			relMotionScript = relMMsg.getScript();
//			break;
		case LOAD_BEHAVIORCONFIG_FILE:
			Logger.log("Loading behavior based configuation file...");
			MRPConfigMsg behMsg = (MRPConfigMsg)msg; 
			Logger.log("Received MRPConfigMsg: " + behMsg);
			
			try {
				mrpConfdata = new MRPConfData(behMsg);
			} catch(Exception e) {
				Logger.logErr("problem creating MRPConfData out of MRPConfigMsg");
				e.printStackTrace();
				System.exit(1);
			}
			
			break;
		case CUSTOM:
			Logger.log("Received custom message: " + msg);
			break;
		case RESET:
			reset();
			break;
		case STARTEXP:
			Logger.log("Starting experiment...");
			StartExpMsg sem = (StartExpMsg)msg;
			startExp(sem.getExpName(), sem.getExpType(), sem.getDelay());
			break;
		case STOPEXP:
			Logger.log("Stopping experiment...");
			stopExp();
			break;
		case UPDATE_BEH_MSG:
			Logger.log("updated behavior message: " + msg);
			MultiRobotBehaveMsg mRmsg = (MultiRobotBehaveMsg)msg;
			manageMRP.updateTeammates(mRmsg);
			break;
		case UPDATE_BEH_TABLE_MSG:
			Logger.log("updating behavior message: " + msg);
			MultiRobotTableMsg mRTmsg = (MultiRobotTableMsg)msg;
			manageMRP.updateTeammatesTable(mRTmsg);
			break;
		default:
			Logger.log("Unknown Message: " + msg);
		}
	}
	
	private void reset() {
		// TODO
	}
	
	/**
	 * Starts an experiment.
	 * 
	 * @param expName The experiment name.
	 * @param expType The type of experiment.
	 * @param delay The number of milliseconds before starting to follow the motion script.
	 */
	private void startExp(String expName, ExpType expType, int delay) {
		
		// Start the file logger
		String fileName = expName + "-" + robotName + "-MRPatrol-" + FileLogger.getUniqueNameExtension() + ".log"; 
		FileLogger expFlogger = new FileLogger(fileName);
		Logger.setFileLogger(expFlogger);
		
		Logger.log("Starting experiment at time " + System.currentTimeMillis() + "...");
		
		if(gpsDataBuffer == null) {
			Logger.logErr("gpsDataBuffer is null!");
			System.exit(1);
		}
		
		// Start the individual components
		if (compassDataBuffer != null)
			compassDataBuffer.start();
		if(gpsDataBuffer == null) {
			System.err.print("gpsDataBuffer is null after setting logger\n");
			System.exit(1);
		}

		Logger.log("Pausing " + delay + "ms before starting motion script.");
		if (delay > 0) {
			synchronized(this) {
				try {
					wait(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		switch(expType) {
//			case FOLLOW_GPS_MOTION_SCRIPT:
//				log("startExp: Starting GPS-based motion script...");
//				NavigateCompassGPS navigatorGPS = new NavigateCompassGPS(motionArbiter, compassDataBuffer, 
//						gpsDataBuffer, flogger);
//				Scooter scooter = new Scooter(motionArbiter, flogger);
//				MotionScriptFollower wpFollower = new MotionScriptFollower(navigatorGPS, scooter, 
//						wifiBeaconBroadcaster, telosRadioSignalMeter, flogger);
//				wpFollower.start(gpsMotionScript, this);
//				break;
//			case FOLLOW_RELATIVE_MOTION_SCRIPT:
//				log("startExp: Starting relative motion script...");
//				NavigateRelative navigatorRel = new NavigateRelative(motionArbiter, relMotionScript, flogger);
//				navigatorRel.start();
//				break;
			case RUN_BEHAVIOR_GPS:
				Logger.log("Starting Behavior-based GPS following experiment...");
				if (gpsDataBuffer == null) {
					Logger.logErr("About to Create Manager: GPS Data buffer is NULL!");
					System.exit(1);
				}
				NavigateCompassGPS mynavigatorGPS = new NavigateCompassGPS(motionArbiter, compassDataBuffer, 
						gpsDataBuffer);
				
				manageMRP = new Manager(mrpConfdata, mynavigatorGPS, msgSender);
				manageMRP.start();
				Logger.log(" manager created.");
				break;
		}
		
	}
	
	@Override
	public void motionScriptDone(boolean success, int finalInstrIndx, boolean continueRunning) {
		if (!continueRunning)
			stopExp();
	}
	
	/**
	 * Stops the experiment.
	 */
	private void stopExp() {
		Logger.log("PharosServer: Stopping the file logger.");
		
		
		Logger.log("Stopping the compass data buffer.");
		if (compassDataBuffer != null) {
			compassDataBuffer.stop();
		}
		
		// Restore the debug file logger since the experiment has stopped.
		Logger.logDbg("Stopping experiment log file.");
		Logger.setFileLogger(debugFileLogger);
	}
	
	@Override
	public void newOpaqueData(ProteusOpaqueData opaqueData) {
		if (opaqueData.getDataCount() > 0) {
			String s = new String(opaqueData.getData());
			Logger.log("MCU Message: " + s);
		}
	}
	
	@Override
	public void beaconReceived(WiFiBeaconEvent be) {
		Logger.log("Received beacon: " + be);
	}
	
	private static void print(String msg) {
		System.out.println(msg);
	}
	
	private static void usage() {
		print("Usage: " + MRPatrolServer.class.getName() + " <options>\n");
		print("Where <options> include:");
		print("\t-playerServer <ip address>: The IP address of the Player Server (default localhost)");
		print("\t-playerPort <port number>: The Player Server's port number (default 6665)");
		print("\t-pharosPort <port number>: The Pharos Server's port number (default 7776)");
		print("\t-mobilityPlane <traxxas|segway|create>: The type of mobility plane being used (default traxxas)");
//		print("\t-mCastAddress <ip address>: The Pharos Server's multicast group address (default 230.1.2.3)");
//		print("\t-mCastPort <port number>: The Pharos Server's multicast port number (default 6000)");
		print("\t-simulate: Localhost simulation (no connection to player server)");
//		print("\t-noTelosBBeacons: Disable TelosB beacons (default enable TelosB beacons)");
//		print("\t-noWiFiBeacons: Disable WiFi beacons (default enable WiFi beacons)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String playerIP = "localhost";
		int playerPort = 6665;
		int pharosPort = 7776;
//		String mCastAddress = "230.1.2.3";
//		int mCastPort = 6000;
		MotionArbiter.MotionType mobilityPlane = MotionArbiter.MotionType.MOTION_TRAXXAS;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-playerServer")) {
					playerIP = args[++i];
				} 
				else if (args[i].equals("-playerPort")) {
					playerPort = Integer.valueOf(args[++i]);
				}
//				else if (args[i].equals("-mCastAddress")) {
//					mCastAddress = args[++i];
//				}
//				else if (args[i].equals("-mCastPort")) {
//					mCastPort = Integer.valueOf(args[++i]);
//				}
				else if (args[i].equals("-mobilityPlane")) {
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
				}
//				else if (args[i].equals("-noWiFiBeacons")) {
//					System.setProperty ("PharosMiddleware.disableWiFiBeacons", "true");
//				}
//				else if (args[i].equals("-noTelosBBeacons")) {
//					System.setProperty ("PharosMiddleware.disableTelosBBeacons", "true");
//				}
				else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				}
				else if (args[i].equals("-simulate")) {
					System.setProperty ("simulateBehave", "true");
				}
				else if (args[i].equals("-pharosPort")) {
					pharosPort = Integer.valueOf(args[++i]);
				}
				else {
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			usage();
			System.exit(1);
		}
		
		print("Player Server IP: " + playerIP);
		print("Player Server port: " + playerPort);
		print("Pharos Server Port: " + pharosPort);
		print("Mobility plane: " + mobilityPlane);
//		print("Multicast Address: " + mCastAddress);
//		print("Multicast Port: " + mCastPort);
		print("Debug: " + ((System.getProperty ("PharosMiddleware.debug") != null) ? true : false));
		print("Simulate: "+((System.getProperty ("simulateBehave") != null) ? true : false));
		
		new MRPatrolServer(playerIP, playerPort, pharosPort, /*mCastAddress, mCastPort,*/ mobilityPlane);
	}
}
