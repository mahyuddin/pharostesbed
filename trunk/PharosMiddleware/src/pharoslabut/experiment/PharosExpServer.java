package pharoslabut.experiment;

import java.net.*;

import pharoslabut.beacon.*;
import pharoslabut.exceptions.PharosException;
import pharoslabut.logger.FileLogger;
import pharoslabut.navigate.*;
import pharoslabut.navigate.motionscript.MotionScript;
import pharoslabut.radioMeter.cc2420.*;
import pharoslabut.sensors.CompassDataBuffer;
import pharoslabut.sensors.GPSDataBuffer;
import pharoslabut.sensors.ProteusOpaqueData;
import pharoslabut.sensors.ProteusOpaqueInterface;
import pharoslabut.sensors.ProteusOpaqueListener;
import pharoslabut.io.*;

import playerclient3.*;
import playerclient3.structures.PlayerConstants;

/**
 * The PharosServer runs on each robot.  It sits between the PlayerServer and PharosClient.
 * The PharosClient is used by the application to perform application-specific tasks.
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
	 * The WiFi multicast group address.  By default this is 230.1.2.3.
	 */
    private String mCastAddress = "230.1.2.3";
    
    /**
	 * The WiFi multicast port.  By default this is 6000.
	 */
    private int mCastPort = 6000;
	
	private PlayerClient client = null;
	private CompassDataBuffer compassDataBuffer;
	private GPSDataBuffer gpsDataBuffer;
	private MotionArbiter motionArbiter;
	
//	private TCPMessageReceiver msgRcvr;
	
	// Components for sending and receiving WiFi beacons
	private WiFiBeaconBroadcaster beaconBroadcaster;
	private WiFiBeaconReceiver beaconReceiver;
	
	// Components for sending and receiving TelosB beacons
	private TelosBeaconBroadcaster telosRadioSignalMeter;
	
	private MotionScript gpsMotionScript;
	private RelativeMotionScript relMotionScript;
	
	private FileLogger flogger = null;
	
	//private pharoslabut.wifi.UDPRxTx udpTest;
	
	/**
	 * The constructor.  Immediately starts the server running.
	 * 
	 * @param playerServerIP The IP address of the player server with which to connect
	 * @param playerServerPort The port that the player server is listening on.
	 * @param pharosServerPort The port on which this Pharos Server should listen on.
	 */
	public PharosExpServer(String playerServerIP, int playerServerPort, int pharosServerPort, 
			String mCastAddress, int mCastPort) 
	{
		this.playerServerIP = playerServerIP;
		this.playerServerPort = playerServerPort;
		this.pharosServerPort = pharosServerPort;
		
		this.mCastAddress = mCastAddress;
		this.mCastPort = mCastPort;
		
		
		// Get the robot's name...		
		try {
			robotName = pharoslabut.RobotIPAssignments.getName();
		} catch (PharosException e1) {
			logErr("Unable to get robot's name, using 'JohnDoe'");
			robotName = "JohnDoe";
			e1.printStackTrace();
		}
		
		if (!initPharosServer()) {
			log("ERROR: Failed to initialize the Pharos server!");
			System.exit(1);
		}
		
		if (!createPlayerClient()) {
			log("ERROR: Failed to connect to Player server!");
			//System.exit(1); // fatal error
		}
		
		if (!initWiFiBeacons()) {
			log("ERROR: Failed to initialize the beaconer!");
			System.exit(1);
		}
		
		if (System.getProperty ("PharosMiddleware.disableTelosB") == null && !initTelosBeacons()) {
			log("ERROR: Failed to initialize Telos beaconer!");
		}
	}
	
	/**
	 * Creates the player client and obtains the necessary interfaces from it.
	 * Creates the CompassDataBuffer, GPSDataBuffer, and MotionArbiter objects.
	 * 
	 * @return true if successful.
	 */
	private boolean createPlayerClient() {
		try {
			client = new PlayerClient(playerServerIP, playerServerPort);
		} catch(PlayerException e) {
			log("ERROR: Unable to connecting to Player: ");
			log("    [ " + e.toString() + " ]");
			return false;
		}
		
		Position2DInterface motors = client.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		Position2DInterface compass = client.requestInterfacePosition2D(2, PlayerConstants.PLAYER_OPEN_MODE);
		GPSInterface gps = client.requestInterfaceGPS(0, PlayerConstants.PLAYER_OPEN_MODE);
		ProteusOpaqueInterface oi = (ProteusOpaqueInterface)client.requestInterfaceOpaque(0, PlayerConstants.PLAYER_OPEN_MODE);
		
		if (motors == null) {
			log("ERROR: motors is null");
			return false;
		}
		
		if (compass == null) {
			log("ERROR: compass is null");
			return false;
		}
		
		if (gps == null) {
			log("ERROR: gps is null");
			return false;
		}
		
		if (oi == null) {
			log("ERROR: opaque interface is null");
			return false;
		}
		
		compassDataBuffer = new CompassDataBuffer(compass);
		gpsDataBuffer = new GPSDataBuffer(gps);
		motionArbiter = new MotionArbiter(motors);
		oi.addOpaqueListener(this);
		
		log("Changing Player server mode to PUSH...");
		client.requestDataDeliveryMode(playerclient3.structures.PlayerConstants.PLAYER_DATAMODE_PUSH);
		
		log("Setting Player Client to run in continuous threaded mode...");
		client.runThreaded(-1, -1);
		
		return true;
	}
	
	private boolean initPharosServer() {
		//TCPMessageReceiver msgRcvr = 
		new TCPMessageReceiver(this, pharosServerPort);
		return true;
	}
	
	private boolean initWiFiBeacons() {
    	// Obtain the multicast address		
		InetAddress mCastGroupAddress = null;
		try {
			mCastGroupAddress = InetAddress.getByName(mCastAddress);
		} catch (UnknownHostException uhe) {
			log("Problems getting multicast address");
			uhe.printStackTrace();
			return false;
		}
		
		String pharosIP = WiFiBeaconBroadcaster.getPharosIP();
		String pharosNI = WiFiBeaconReceiver.getPharosNetworkInterface();
		
		if (pharosIP == null || pharosNI == null) {
			log("Unable to get pharos IP or pharos network interface...");
			return false;
		}
		
		try {
			WiFiBeacon beacon = new WiFiBeacon(InetAddress.getByName(WiFiBeaconBroadcaster.getPharosIP()), pharosServerPort);
			beaconReceiver = new WiFiBeaconReceiver(mCastAddress, mCastPort, pharosNI);
			beaconBroadcaster = new WiFiBeaconBroadcaster(mCastGroupAddress, pharosIP, mCastPort, beacon);

			// Start receiving beacons
			beaconReceiver.start();
			//beaconBroadcaster.start();
			return true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private boolean initTelosBeacons() {
		try {
			telosRadioSignalMeter = new pharoslabut.radioMeter.cc2420.TelosBeaconBroadcaster();
		} catch (TelosBeaconException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * This is called whenever a message is received.
	 */
	@Override
	public void newMessage(Message msg) {
		log("Received message: " + msg);
		switch(msg.getType()) {
		case LOAD_GPS_MOTION_SCRIPT:
			log("Loading GPS-based motion script...");
			MotionScriptMsg gpsMSMsg = (MotionScriptMsg)msg;
			gpsMotionScript = gpsMSMsg.getScript();
			break;
		case LOAD_RELATIVE_MOTION_SCRIPT:
			log("Loading relative motion script...");
			RelativeMotionScriptMsg relMMsg = (RelativeMotionScriptMsg)msg;
			relMotionScript = relMMsg.getScript();
			break;
		case RESET:
			reset();
			break;
		case STARTEXP:
			log("Starting experiment...");
			StartExpMsg sem = (StartExpMsg)msg;
			startExp(sem.getExpName(), sem.getExpType(), sem.getDelay());
			break;
		case STOPEXP:
			stopExp();
			break;
		default:
			log("Unknown Message: " + msg);
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
		String fileName = expName + "-" + robotName + "-Pharos_" + FileLogger.getUniqueNameExtension() + ".log"; 
		flogger = new FileLogger(fileName);
		
		log("startExp: Starting the file logger...");
		if (motionArbiter != null)				motionArbiter.setFileLogger(flogger);
		if (gpsDataBuffer != null)				gpsDataBuffer.setFileLogger(flogger);
		if (compassDataBuffer!= null) 			compassDataBuffer.setFileLogger(flogger);
		if (beaconBroadcaster != null)			beaconBroadcaster.setFileLogger(flogger);
		if (beaconReceiver != null) 			beaconReceiver.setFileLogger(flogger);
		if (telosRadioSignalMeter != null) 		telosRadioSignalMeter.setFileLogger(flogger);
		
		log("startExp: Starting experiment at time " + System.currentTimeMillis() + "...");
		
		// Start the individual components
		if (compassDataBuffer != null)			compassDataBuffer.start();
		if (gpsDataBuffer != null) 				gpsDataBuffer.start();
		
		// This is temporary code for mission 14...
		//flogger.log("PharosServer: Starting UDPRxTx:");
		//udpTest = new pharoslabut.wifi.UDPRxTx(expName, robotName, 55555, flogger);
		
		log("startExp: Pausing " + delay + "ms before starting motion script.");
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
				log("startExp: Starting GPS-based motion script...");
				NavigateCompassGPS navigatorGPS = new NavigateCompassGPS(motionArbiter, compassDataBuffer, 
						gpsDataBuffer, flogger);
				Scooter scooter = new Scooter(motionArbiter, flogger);
				MotionScriptFollower wpFollower = new MotionScriptFollower(navigatorGPS, scooter, 
						beaconBroadcaster, telosRadioSignalMeter, flogger);
				wpFollower.start(gpsMotionScript, this);
				break;
			case FOLLOW_RELATIVE_MOTION_SCRIPT:
				log("startExp: Starting relative motion script...");
				NavigateRelative navigatorRel = new NavigateRelative(motionArbiter, relMotionScript, flogger);
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
		flogger.log("PharosServer: Stopping the file logger.");
		
		if (motionArbiter != null) {
			motionArbiter.setFileLogger(null);
		}
		
		flogger.log("PharosServer: Stopping the WiFi beacon broadcaster.");
		if (beaconBroadcaster != null) {
			beaconBroadcaster.setFileLogger(null);
			beaconBroadcaster.stop();
		}
		
		flogger.log("PharosServer: Stopping the WiFi beacon receiver.");
		if (beaconReceiver != null) {
			beaconReceiver.setFileLogger(null);
		}
		
		flogger.log("PharosServer: Stopping the GPS data buffer.");
		if (gpsDataBuffer != null)	{
			gpsDataBuffer.setFileLogger(null);
			gpsDataBuffer.stop();
		}
		
		flogger.log("PharosServer: Stopping the compass data buffer.");
		if (compassDataBuffer != null) {
			compassDataBuffer.setFileLogger(null);
			compassDataBuffer.stop();
		}
		
		flogger.log("PharosServer: Stopping the TelosB signal meter.");
		if (telosRadioSignalMeter != null) {
			telosRadioSignalMeter.setFileLogger(null);
			telosRadioSignalMeter.stop();
		}
		
		//flogger.log("PharosServer: Stopping the UDP tester.");
		//udpTest.stop();
		
		flogger = null;
	}
	
	@Override
	public void newOpaqueData(ProteusOpaqueData opaqueData) {
		if (opaqueData.getDataCount() > 0) {
			String s = new String(opaqueData.getData());
			log("MCU Message: " + s);
		}
	}
	
	@Override
	public void beaconReceived(WiFiBeaconEvent be) {
		log("Received beacon: " + be);
	}
	
	
	private void logErr(String msg) {
		String result = "PharosServer: " + msg;
		System.err.println(result);
		if (flogger != null)
			flogger.log(result);
	}
	
	private void log(String msg) {
		String result = "PharosServer: " + msg;
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
	
	private static void print(String msg) {
		System.out.println(msg);
	}
	
	private static void usage() {
		print("Usage: pharoslabut.experiment.PharosExpServer <options>\n");
		print("Where <options> include:");
		print("\t-playerServer <ip address>: The IP address of the Player Server (default localhost)");
		print("\t-playerPort <port number>: The Player Server's port number (default 6665)");
		print("\t-pharosPort <port number>: The Pharos Server's port number (default 7776)");
		print("\t-mCastAddress <ip address>: The Pharos Server's multicast group address (default 230.1.2.3)");
		print("\t-mCastPort <port number>: The Pharos Server's multicast port number (default 6000)");
		print("\t-noTelosB: Disable support for TelosB (by default support TelosB)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String playerIP = "localhost";
		int playerPort = 6665;
		int pharosPort = 7776;
		String mCastAddress = "230.1.2.3";
		int mCastPort = 6000;
		
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
				else if (args[i].equals("-noTelosB")) {
					System.setProperty ("PharosMiddleware.disableTelosB", "true");
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
		print("Multicast Address: " + mCastAddress);
		print("Multicast Port: " + mCastPort);
		print("Debug: " + ((System.getProperty ("PharosMiddleware.debug") != null) ? true : false));
		
		new PharosExpServer(playerIP, playerPort, pharosPort, mCastAddress, mCastPort);
	}
}