package pharoslabut;

import java.net.*;

import pharoslabut.beacon.*;
import pharoslabut.logger.FileLogger;
import pharoslabut.navigate.*;
import pharoslabut.radioMeter.cc2420.RadioSignalMeterException;
import pharoslabut.io.*;

import playerclient.*;
import playerclient.structures.PlayerConstants;
import playerclient.structures.opaque.PlayerOpaqueData;

/**
 * The PharosServer operates on each robot.  It acts as an adapter that sits between the PlayerServer and PharosClient.
 * The PharosClient is used by the application to perform application-specific tasks.
 * 
 * @see PharosClient
 * 
 * @author Chien-Liang Fok
 */
public class PharosServer implements MessageReceiver, BeaconListener, OpaqueListener, WayPointFollowerDoneListener {
	
	private String playerServerIP;
	private int playerServerPort;
	private int beaconMin, beaconMax;
	private int pharosServerPort;
	
    /**
	 * The multicast group address.  By default this is 230.1.2.3.
	 */
    private String mCastAddress = "230.1.2.3";
    
    /**
	 * The multicast port.  By default this is 6000.
	 */
    private int mCastPort = 6000;
	
	private PlayerClient client = null;
	private CompassDataBuffer compassDataBuffer;
	private GPSDataBuffer gpsDataBuffer;
	private MotionArbiter motionArbiter;
	
//	private TCPMessageReceiver msgRcvr;
	
	private BeaconBroadcaster beaconBroadcaster;
	private BeaconReceiver beaconReceiver;
	
	private pharoslabut.radioMeter.cc2420.RadioSignalMeter rsm;
	
	private GPSMotionScript gpsMotionScript;
	private RelativeMotionScript relMotionScript;
	
	private FileLogger flogger = null;
	
	private pharoslabut.wifi.UDPRxTx udpTest;
	
//	private String expName;
	
	/**
	 * The constructor.  Immediately starts the server running.
	 * 
	 * @param playerServerIP
	 * @param playerServerPort
	 * @param pharosServerPort
	 * @param beaconMin The minimum beaconing interval in ms
	 * @param beaconMax The maximum beaconing interval in ms
	 */
	public PharosServer(String playerServerIP, int playerServerPort, int pharosServerPort, String mCastAddress, 
			int mCastPort, int beaconMin, int beaconMax) {
		
		this.playerServerIP = playerServerIP;
		this.playerServerPort = playerServerPort;
		this.pharosServerPort = pharosServerPort;
		
		this.mCastAddress = mCastAddress;
		this.mCastPort = mCastPort;
		this.beaconMin = beaconMin;
		this.beaconMax = beaconMax;
		
		if (!createPlayerClient()) {
			log("ERROR: Failed to connect to player server!");
			System.exit(1); // fatal error
		}
		
		if (!initServer()) {
			log("ERROR: Failed to initialize the server!");
			System.exit(1);
		}
		
		if (!initWiFiBeacons()) {
			log("ERROR: Failed to initialize the beaconer!");
			System.exit(1);
		}
		
		if (!initTelosBeacons()) {
			log("ERROR: Failed to initialize Telos beaconer!");
		}
	}
	
	private boolean createPlayerClient() {
		try {
			client = new PlayerClient(playerServerIP, playerServerPort);
		} catch(PlayerException e) {
			log("ERROR: Unable to connecting to Player: ");
			log("    [ " + e.toString() + " ]");
			return false;
		}
		
		Position2DInterface motors = client.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		Position2DInterface compass = client.requestInterfacePosition2D(1, PlayerConstants.PLAYER_OPEN_MODE);
		GPSInterface gps = client.requestInterfaceGPS(0, PlayerConstants.PLAYER_OPEN_MODE);
		OpaqueInterface oi = client.requestInterfaceOpaque(0, PlayerConstants.PLAYER_OPEN_MODE);
		
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
		return true;
	}
	
	private boolean initServer() {
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
		
		String pharosIP = BeaconBroadcaster.getPharosIP();
		String pharosNI = BeaconReceiver.getPharosNetworkInterface();
		
		if (pharosIP == null || pharosNI == null) {
			log("Unable to get pharos IP or pharos network interface...");
			return false;
		}
		
		try {
			Beacon beacon = new Beacon(InetAddress.getByName(BeaconBroadcaster.getPharosIP()), pharosServerPort);
			beaconReceiver = new BeaconReceiver(mCastAddress, mCastPort, pharosNI);
			beaconBroadcaster = new BeaconBroadcaster(mCastGroupAddress, pharosIP, mCastPort, 
					beacon, beaconMin, beaconMax);

			// Start broadcasting and receiving beacons
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
			rsm = new pharoslabut.radioMeter.cc2420.RadioSignalMeter();
		} catch (RadioSignalMeterException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	@Override
	public void newMessage(Message msg) {
		log("Received message: " + msg);
		switch(msg.getType()) {
		case LOAD_GPS_MOTION_SCRIPT:
			log("Loading GPS-based motion script...");
			GPSMotionScriptMsg gpsMSMsg = (GPSMotionScriptMsg)msg;
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
			startExp(sem.getExpName(), sem.getRobotName(), sem.getExpType(), sem.getDelay());
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
	 * @param robotName The robot's name.
	 * @param expType The type of experiment.
	 * @param delay The number of milliseconds before starting to follow the motion script.
	 */
	private void startExp(String expName, String robotName, ExpType expType, int delay) {
		
		// Start the file logger
		String fileName = expName + "-" + robotName + "-Pharos_" + FileLogger.getUniqueNameExtension() + ".log"; 
		flogger = new FileLogger(fileName);
		motionArbiter.setFileLogger(flogger);
		beaconBroadcaster.setFileLogger(flogger);
		beaconReceiver.setFileLogger(flogger);
		gpsDataBuffer.setFileLogger(flogger);
		compassDataBuffer.setFileLogger(flogger);
		rsm.setFileLogger(flogger);
		
		flogger.log("PharosServer: Starting experiment at time: " + System.currentTimeMillis());

		// This is temporary code for mission 14...
		flogger.log("PharosServer: Starting UDPRxTx:");
		udpTest = new pharoslabut.wifi.UDPRxTx(expName, robotName, 55555, flogger);
		
		// Start the beacons
		flogger.log("PharosServer: Starting the WiFi beacon broadcaster.");
		beaconBroadcaster.start();
		
		// Start the TelosB cc2420 radio signal meter
		flogger.log("PharosServer: Starting the TelosB beacon broadcaster.");
		rsm.startBroadcast(1000 /* period */, 1000 /* num broadcasts */);

		flogger.log("PharosServer: Pausing " + delay + "ms before starting motion script.");
		if (delay > 0) {
			synchronized(this) {
				try {
					wait(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		flogger.log("PharosServer: Starting motion script.");
		switch(expType) {
			case FOLLOW_GPS_MOTION_SCRIPT:
				log("Following GPS-based motion script...");
				NavigateCompassGPS navigatorGPS = new NavigateCompassGPS(motionArbiter, compassDataBuffer, 
						gpsDataBuffer, flogger);
				WayPointFollower wpFollower = new WayPointFollower(navigatorGPS, gpsMotionScript, flogger);
				wpFollower.start(this);
				break;
			case FOLLOW_RELATIVE_MOTION_SCRIPT:
				log("Following a relative motion script...");
				NavigateRelative navigatorRel = new NavigateRelative(motionArbiter, relMotionScript, flogger);
				navigatorRel.start();
				break;
		}
		
	}
	
	@Override
	public void wayPointFollowerDone(boolean success, int finalWayPoint) {
		// For now, assume that once the robot is done following a motion script,
		// the experiment is over.
		stopExp();
		
	}
	
	private void stopExp() {
		flogger.log("PharosServer: Stopping the file logger.");
		motionArbiter.setFileLogger(null);
		beaconBroadcaster.setFileLogger(null);
		beaconReceiver.setFileLogger(null);
		gpsDataBuffer.setFileLogger(null);
		compassDataBuffer.setFileLogger(null);
		rsm.setFileLogger(null);
		
		flogger.log("PharosServer: Stopping the WiFi beacon broadcaster.");
		beaconBroadcaster.stop();
		
		flogger.log("PharosServer: Stopping the TelosB broadcaster.");
		rsm.stopBroadcast();
		
		flogger.log("PharosServer: Stopping the UDP tester.");
		udpTest.stop();
		
		flogger = null;
	}
	
	@Override
	public void newOpaqueData(PlayerOpaqueData opaqueData) {
		if (opaqueData.getDataCount() > 0) {
			String s = new String(opaqueData.getData());
			log("MCU Message: " + s);
		}
	}
	
	@Override
	public void beaconReceived(BeaconEvent be) {
		log("Received beacon: " + be);
	}
	
	private void log(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println("PharosServer: " + msg);
		if (flogger != null)
			flogger.log("PharosServer: " + msg);
	}
	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(msg);
	}
	
	private static void usage() {
		System.setProperty ("PharosMiddleware.debug", "true");
		print("Usage: pharoslabut.PharosServer <options>\n");
		print("Where <options> include:");
		print("\t-playerServer <ip address>: The IP address of the Player Server (default localhost)");
		print("\t-playerPort <port number>: The Player Server's port number (default 6665)");
		print("\t-pharosPort <port number>: The Pharos Server's port number (default 7776)");
		print("\t-mCastAddress <ip address>: The Pharos Server's multicast group address (default 230.1.2.3)");
		print("\t-mCastPort <port number>: The Pharos Server's multicast port number (default 6000)");
		print("\t-bmin <period in ms>: minimum beacon period (default 500)");
		print("\t-bmax <period in ms>: maximum beacon period (default 2000)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		int beaconMin = 500;
		int beaconMax = 2000;
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
				else if (args[i].equals("-bmin")) {
					beaconMin = Integer.valueOf(args[++i]);
				} 
				else if (args[i].equals("-bmax")) {
					beaconMax = Integer.valueOf(args[++i]);
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
		print("Min beacon period: " + beaconMin + "ms");
		print("Max beacon period: " + beaconMax + "ms");
		print("Debug: " + ((System.getProperty ("PharosMiddleware.debug") != null) ? true : false));
		
		new PharosServer(playerIP, playerPort, pharosPort, mCastAddress, mCastPort, beaconMin, beaconMax);
	}
}