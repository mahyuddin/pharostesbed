package pharoslabut.experiment;

import java.net.InetAddress;
import java.net.UnknownHostException;

import pharoslabut.RobotIPAssignments;
import pharoslabut.beacon.WiFiBeacon;
import pharoslabut.beacon.WiFiBeaconBroadcaster;
import pharoslabut.beacon.WiFiBeaconEvent;
import pharoslabut.beacon.WiFiBeaconListener;
import pharoslabut.beacon.WiFiBeaconReceiver;
import pharoslabut.context.ContextSubsystem;
import pharoslabut.exceptions.PharosException;
import pharoslabut.io.Message;
import pharoslabut.io.MessageReceiver;
import pharoslabut.io.MotionScriptMsg;
import pharoslabut.io.RelativeMotionScriptMsg;
import pharoslabut.io.SetTimeMsg;
import pharoslabut.io.StartExpMsg;
import pharoslabut.io.TCPMessageReceiver;
import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.MotionArbiter;
import pharoslabut.navigate.MotionScriptFollower;
import pharoslabut.navigate.MotionScriptFollowerDoneListener;
import pharoslabut.navigate.NavigateCompassGPS;
import pharoslabut.navigate.NavigateRelative;
import pharoslabut.navigate.RelativeMotionScript;
import pharoslabut.navigate.Scooter;
import pharoslabut.navigate.motionscript.MotionScript;
import pharoslabut.radioMeter.cc2420.TelosBeaconBroadcaster;
import pharoslabut.radioMeter.cc2420.TelosBeaconException;
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

/**
 * This should run on each robot.  It sits between the PlayerServer and PharosExpClient.
 * The PharosExpClient is used to coordinate the execution of experiments.
 * 
 * @see PharosExpClient
 * @author Chien-Liang Fok
 */
public class PharosExpServer implements MessageReceiver, WiFiBeaconListener, ProteusOpaqueListener, MotionScriptFollowerDoneListener {
	
	private String robotName;
	
	private String playerServerIP;
	private int playerServerPort;
	private int pharosServerPort;
	
    /**
	 * The WiFi multicast group address.
	 */
    private String mCastAddress;
    
    /**
	 * The WiFi multicast port.
	 */
    private int mCastPort;
	
	private PlayerClient client = null;
	private CompassDataBuffer compassDataBuffer;
	private GPSDataBuffer gpsDataBuffer;
	private MotionArbiter motionArbiter;
	
	// Components for sending and receiving WiFi beacons
	private WiFiBeaconBroadcaster wifiBeaconBroadcaster;
	private WiFiBeaconReceiver wifiBeaconReceiver;
	
	// Components for sending and receiving TelosB beacons
	private TelosBeaconBroadcaster telosRadioSignalMeter;
	
	private MotionScript gpsMotionScript;
	private RelativeMotionScript relMotionScript;
	
	/**
	 * This is the file logger that is used for debugging purposes.  It is used when debug mode is enabled
	 * and there is no experiments running.
	 */
	private FileLogger debugFileLogger = null;

	private ContextSubsystem contextSubsystem;
	
	//private pharoslabut.wifi.UDPRxTx udpTest;
	
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
	public PharosExpServer(String playerServerIP, int playerServerPort, int pharosServerPort, 
			String mCastAddress, int mCastPort, MotionArbiter.MotionType mobilityPlane) 
	{
		this.playerServerIP = playerServerIP;
		this.playerServerPort = playerServerPort;
		this.pharosServerPort = pharosServerPort;
		
		this.mCastAddress = mCastAddress;
		this.mCastPort = mCastPort;
		
		/*
		 * If we're running in debug mode, start logging debug statements even before the experiment begins.
		 */
		if (System.getProperty ("PharosMiddleware.debug") != null) {
			debugFileLogger = new FileLogger("PharosExpServer-" + FileLogger.getUniqueNameExtension() + ".log");
			Logger.setFileLogger(debugFileLogger);
			Logger.log("Creating a PharosExpServer...");
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
		
		if (!initPharosServer()) {
			Logger.logErr("Failed to initialize the Pharos server!");
			System.exit(1);
		}
		
		if (!createPlayerClient(mobilityPlane)) {
			Logger.logErr("Failed to connect to Player server!");
			System.exit(1);
		}
		
		if (!initContextSubsystem()) {
			Logger.logErr("Failed to initialize context subsystem!");
			System.exit(1);
		}
		
		if (System.getProperty ("PharosMiddleware.disableWiFiBeacons") == null) {
			if (!initWiFiBeacons()) {
				Logger.logErr("Failed to initialize the WiFi beaconer!");
				System.exit(1);
			}
		} else
			Logger.log("WiFi beacons disabled.");
		
		if (System.getProperty ("PharosMiddleware.disableTelosBBeacons") == null) {
			if (!initTelosBeacons()) {
				Logger.logErr("Failed to initialize TelosB beaconer!");
				System.exit(1);
			}
		} else
			Logger.log("TelosB beacons disabled.");
	}
	
	/**
	 * Creates the player client and obtains the necessary interfaces from it.
	 * Creates the CompassDataBuffer, GPSDataBuffer, and MotionArbiter objects.
	 * 
	 * @return true if successful.
	 */
	private boolean createPlayerClient(MotionArbiter.MotionType mobilityPlane) {
		try {
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
		}
		
		// The Traxxas and Segway mobility planes' compasses are Position2D devices at index 1,
		// while the Segway RMP 50's compass is on index 2.
		Logger.log("Subscribing to compass.");
		Position2DInterface compass;
		if (mobilityPlane == MotionArbiter.MotionType.MOTION_IROBOT_CREATE ||
				mobilityPlane == MotionArbiter.MotionType.MOTION_TRAXXAS) {
			compass = client.requestInterfacePosition2D(1, PlayerConstants.PLAYER_OPEN_MODE);
		} else {
			compass = client.requestInterfacePosition2D(2, PlayerConstants.PLAYER_OPEN_MODE);
		}
		if (compass == null) {
			Logger.logErr("compass is null");
			return false;
		}
		
		Logger.log("Subscribing to GPS.");
		GPSInterface gps = client.requestInterfaceGPS(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (gps == null) {
			Logger.logErr("gps is null");
			return false;
		}
		
		Logger.log("Subscribing to opaque interface.");
		ProteusOpaqueInterface oi = (ProteusOpaqueInterface)client.requestInterfaceOpaque(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (oi == null) {
			Logger.logErr("opaque interface is null");
			return false;
		}
		
		compassDataBuffer = new CompassDataBuffer(compass);
		gpsDataBuffer = new GPSDataBuffer(gps);
		motionArbiter = new MotionArbiter(mobilityPlane, motors);
		oi.addOpaqueListener(this);
		
		Logger.log("Changing Player server mode to PUSH...");
		client.requestDataDeliveryMode(playerclient3.structures.PlayerConstants.PLAYER_DATAMODE_PUSH);
		
		Logger.log("Setting Player Client to run in continuous threaded mode...");
		client.runThreaded(-1, -1);
		
		return true;
	}
	
	private boolean initPharosServer() {
		new TCPMessageReceiver(this, pharosServerPort);
		return true;
	}
	
	private boolean initContextSubsystem() {
		try {
			contextSubsystem = new ContextSubsystem(gpsDataBuffer, compassDataBuffer);
			contextSubsystem.start();
			return true;
		} catch (Exception e) {
			Logger.logErr("Problem initializing context subsystem: " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	private boolean initWiFiBeacons() {
    	// Obtain the multicast address		
		InetAddress mCastGroupAddress = null;
		try {
			mCastGroupAddress = InetAddress.getByName(mCastAddress);
		} catch (UnknownHostException uhe) {
			Logger.logErr("Problems getting multicast address");
			uhe.printStackTrace();
			return false;
		}
		
		String pharosIP;
		try {
			pharosIP = RobotIPAssignments.getAdHocIP();
		} catch (PharosException e1) {
			Logger.logErr("Unable to get ad hoc IP address: " + e1.getMessage());
			e1.printStackTrace();
			return false;
		}
		
		String pharosNI;
		try {
			pharosNI = RobotIPAssignments.getAdHocNetworkInterface();
		} catch (PharosException e1) {
			Logger.logErr("Unable to get ad hoc network interface: " + e1.getMessage());
			e1.printStackTrace();
			return false;
		}
		
		if (pharosIP == null || pharosNI == null) {
			Logger.logErr("Unable to get pharos IP or pharos network interface...");
			return false;
		}
		
		try {
			WiFiBeacon beacon = new WiFiBeacon(InetAddress.getByName(pharosIP), pharosServerPort);
			wifiBeaconReceiver = new WiFiBeaconReceiver(mCastAddress, mCastPort, pharosNI);
			wifiBeaconBroadcaster = new WiFiBeaconBroadcaster(mCastGroupAddress, pharosIP, mCastPort, beacon);

			// Start receiving beacons
			wifiBeaconReceiver.start();
			return true;
		} catch (UnknownHostException e) {
			Logger.logErr("Problem initializing WiFi beacons: " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}
	
	private boolean initTelosBeacons() {
		try {
			telosRadioSignalMeter = new pharoslabut.radioMeter.cc2420.TelosBeaconBroadcaster();
		} catch (TelosBeaconException e) {
			Logger.logErr("Problem initializing TelosB beacons: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
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
		case LOAD_GPS_MOTION_SCRIPT:
			Logger.log("Loading GPS-based motion script...");
			MotionScriptMsg gpsMSMsg = (MotionScriptMsg)msg;
			gpsMotionScript = gpsMSMsg.getScript();
			break;
		case LOAD_RELATIVE_MOTION_SCRIPT:
			Logger.log("Loading relative motion script...");
			RelativeMotionScriptMsg relMMsg = (RelativeMotionScriptMsg)msg;
			relMotionScript = relMMsg.getScript();
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
		
		// Start the file logger and set it in the Logger.
		String fileName = expName + "-" + robotName + "-Pharos_" + FileLogger.getUniqueNameExtension() + ".log"; 
		FileLogger expFlogger = new FileLogger(fileName);
		Logger.setFileLogger(expFlogger);
		
		Logger.log("Starting experiment at time " + System.currentTimeMillis() + "...");
		
		// Start the individual components
		if (compassDataBuffer != null)			compassDataBuffer.start();
		
		// This is temporary code for mission 14...
		//flogger.log("PharosServer: Starting UDPRxTx:");
		//udpTest = new pharoslabut.wifi.UDPRxTx(expName, robotName, 55555, flogger);
		
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
			case FOLLOW_GPS_MOTION_SCRIPT:
				Logger.log("Starting GPS-based motion script...");
				NavigateCompassGPS navigatorGPS = new NavigateCompassGPS(motionArbiter, compassDataBuffer, 
						gpsDataBuffer);
				Scooter scooter = new Scooter(motionArbiter);
				MotionScriptFollower wpFollower = new MotionScriptFollower(navigatorGPS, scooter, 
						wifiBeaconBroadcaster, telosRadioSignalMeter, contextSubsystem);
				wpFollower.start(gpsMotionScript, this);
				break;
			case FOLLOW_RELATIVE_MOTION_SCRIPT:
				Logger.log("Starting relative motion script...");
				NavigateRelative navigatorRel = new NavigateRelative(motionArbiter, relMotionScript);
				navigatorRel.start();
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
		Logger.log("Stopping the experiment.");
//		
//		if (motionArbiter != null) {
//			motionArbiter.setFileLogger(null);
//		}
		
		Logger.log("PharosServer: Stopping the WiFi beacon broadcaster.");
		if (wifiBeaconBroadcaster != null) {
//			wifiBeaconBroadcaster.setFileLogger(null);
			wifiBeaconBroadcaster.stop();
		}
		
//		Logger.log("PharosServer: Stopping the WiFi beacon receiver.");
//		if (wifiBeaconReceiver != null) {
//			wifiBeaconReceiver.setFileLogger(null);
//		}
		
//		Logger.log("PharosServer: Stopping the GPS data buffer.");
//		if (gpsDataBuffer != null)	{
////			gpsDataBuffer.setFileLogger(null);
////			gpsDataBuffer.stop();
//		}
		
		Logger.log("PharosServer: Stopping the compass data buffer.");
		if (compassDataBuffer != null) {
//			compassDataBuffer.setFileLogger(null);
			compassDataBuffer.stop();
		}
		
		Logger.log("PharosServer: Stopping the TelosB signal meter.");
		if (telosRadioSignalMeter != null) {
//			telosRadioSignalMeter.setFileLogger(null);
			telosRadioSignalMeter.stop();
		}
		
		//flogger.log("PharosServer: Stopping the UDP tester.");
		//udpTest.stop();
		
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
	
	
//	private void logErr(String msg) {
//		String result = "PharosServer: ERROR: " + msg;
//		System.err.println(result);
//		if (flogger != null)
//			flogger.log(result);
//	}
//	
//	private void log(String msg) {
//		String result = "PharosServer: " + msg;
//		if (System.getProperty ("PharosMiddleware.debug") != null)
//			System.out.println(result);
//		if (flogger != null)
//			flogger.log(result);
//	}
	
	private static void print(String msg) {
		System.out.println(msg);
	}
	
	private static void usage() {
		print("Usage: " + PharosExpServer.class.getName() + " <options>\n");
		print("Where <options> include:");
		print("\t-playerServer <ip address>: The IP address of the Player Server (default localhost)");
		print("\t-playerPort <port number>: The Player Server's port number (default 6665)");
		print("\t-pharosPort <port number>: The Pharos Server's port number (default 7776)");
		print("\t-mobilityPlane <traxxas|segway|create>: The type of mobility plane being used (default traxxas)");
		print("\t-mCastAddress <ip address>: The Pharos Server's multicast group address (default 230.1.2.3)");
		print("\t-mCastPort <port number>: The Pharos Server's multicast port number (default 6000)");
		print("\t-noTelosBBeacons: Disable TelosB beacons (default enable TelosB beacons)");
		print("\t-noWiFiBeacons: Disable WiFi beacons (default enable WiFi beacons)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String playerIP = "localhost";
		int playerPort = 6665;
		int pharosPort = 7776;
		String mCastAddress = "230.1.2.3";
		int mCastPort = 6000;
		MotionArbiter.MotionType mobilityPlane = MotionArbiter.MotionType.MOTION_TRAXXAS;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-playerServer")) {
					playerIP = args[++i];
				} 
				else if (args[i].equals("-playerPort")) {
					playerPort = Integer.valueOf(args[++i]);
				}
				else if (args[i].equals("-mCastAddress")) {
					mCastAddress = args[++i];
				}
				else if (args[i].equals("-mCastPort")) {
					mCastPort = Integer.valueOf(args[++i]);
				}
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
				else if (args[i].equals("-noWiFiBeacons")) {
					System.setProperty ("PharosMiddleware.disableWiFiBeacons", "true");
				}
				else if (args[i].equals("-noTelosBBeacons")) {
					System.setProperty ("PharosMiddleware.disableTelosBBeacons", "true");
				}
				else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
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
		print("Multicast Address: " + mCastAddress);
		print("Multicast Port: " + mCastPort);
		print("Debug: " + ((System.getProperty ("PharosMiddleware.debug") != null) ? true : false));
		
		new PharosExpServer(playerIP, playerPort, pharosPort, mCastAddress, mCastPort, mobilityPlane);
	}
}
