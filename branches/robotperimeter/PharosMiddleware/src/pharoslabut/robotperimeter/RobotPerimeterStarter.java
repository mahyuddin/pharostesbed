package pharoslabut.robotperimeter;
import java.net.InetAddress;
import java.net.UnknownHostException;

import pharoslabut.RobotIPAssignments;
import pharoslabut.beacon.WiFiBeacon;
import pharoslabut.beacon.WiFiBeaconBroadcaster;
import pharoslabut.beacon.WiFiBeaconReceiver;
import pharoslabut.exceptions.NoNewDataException;
import pharoslabut.exceptions.PharosException;
import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.MotionArbiter;
import pharoslabut.navigate.MotionScriptFollower;
import pharoslabut.navigate.NavigateCompassGPS;
import pharoslabut.navigate.NavigateRelative;
import pharoslabut.navigate.RelativeMotionScript;
import pharoslabut.navigate.Scooter;
import pharoslabut.navigate.motionscript.MotionScript;
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

public class RobotPerimeterStarter implements ProteusOpaqueListener {
	private String robotName;
	
	private String playerServerIP;
	private int playerServerPort;
	
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
	
	private MotionScript gpsMotionScript;
	private RelativeMotionScript relMotionScript;
	
	/**
	 * This is the file logger that is used for debugging purposes.  It is used when debug mode is enabled
	 * and there is no experiments running.
	 */
	private FileLogger debugFileLogger = null;

	private double fakeLatitude;
	private double fakeLongitude;

	private long beaconMinPeriod;
	private long beaconMaxPeriod;
	private short beaconTxPower;

	public RobotPerimeterStarter(String playerServerIP, int playerServerPort, String mCastAddress, int mCastPort, MotionArbiter.MotionType mobilityPlane, long beaconMinPeriod, long beaconMaxPeriod, short beaconTxPower, double fakeLatitude, double fakeLongitude) throws InterruptedException, NoNewDataException 
	{
		this.playerServerIP = playerServerIP;
		this.playerServerPort = playerServerPort;
		
		this.mCastAddress = mCastAddress;
		this.mCastPort = mCastPort;
		
		this.fakeLatitude = fakeLatitude;
		this.fakeLongitude = fakeLongitude;
		
		this.beaconMinPeriod = beaconMinPeriod;
		this.beaconMaxPeriod = beaconMaxPeriod;
		this.beaconTxPower = beaconTxPower;
		
		/*
		 * If we're running in debug mode, start logging debug statements even before the experiment begins.
		 */
		if (System.getProperty ("PharosMiddleware.debug") != null) {
			debugFileLogger = new FileLogger("RobotPerimeterStarter-" + FileLogger.getUniqueNameExtension() + ".log");
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
		
		if (!createPlayerClient(mobilityPlane)) {
			Logger.logErr("Failed to connect to Player server!");
			System.exit(1);
		}
		
		if (System.getProperty ("PharosMiddleware.disableWiFiBeacons") == null) {
			if (!initWiFiBeacons()) {
				Logger.logErr("Failed to initialize the WiFi beaconer!");
				System.exit(1);
			}
		} else
			Logger.log("WiFi beacons disabled.");
		
		
		///// Old code 
		Logger.log("Starting experiment...");
		startExp();
	}
		
	private void startExp() throws InterruptedException {
		// Start the file logger and set it in the Logger.
		String fileName = robotName + "-RobotPerimeterStarter_" + FileLogger.getUniqueNameExtension() + ".log"; 
		FileLogger expFlogger = new FileLogger(fileName);
		Logger.setFileLogger(expFlogger);
		
		Logger.log("Starting experiment at time " + System.currentTimeMillis() + "...");
		
		// Start the individual components
		if (compassDataBuffer != null)			compassDataBuffer.start();

		Logger.log("Starting beacon broadcaster");
		wifiBeaconBroadcaster.start(beaconMinPeriod, beaconMaxPeriod, beaconTxPower);
		
		Mobility mobility = new Mobility (motionArbiter, compassDataBuffer, gpsDataBuffer/*, wifibeaconbroadcaster*/);
		
		Vision vision = new Vision(fakeLatitude, fakeLongitude);
		//TODO put "vision" in different thread, set to receive gps messages
		vision.VisionControl();
		mobility.controlMotionAndIntelligence();
	}
	
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
			WiFiBeacon beacon = new WiFiBeacon(InetAddress.getByName(pharosIP), playerServerPort);
			wifiBeaconReceiver = new WiFiBeaconReceiver(mCastAddress, mCastPort, pharosNI, true);
			wifiBeaconBroadcaster = new WiFiBeaconBroadcaster(mCastGroupAddress, pharosIP, mCastPort, beacon, true);

			// Start receiving beacons
			wifiBeaconReceiver.start();
			return true;
		} catch (UnknownHostException e) {
			Logger.logErr("Problem initializing WiFi beacons: " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public void newOpaqueData(ProteusOpaqueData opaqueData) {
		if (opaqueData.getDataCount() > 0) {
			String s = new String(opaqueData.getData());
			Logger.log("MCU Message: " + s);
		}
	}

	private static void usage() {
		System.err.println("Usage: pharoslabut.RobotMover <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
		System.err.println("\t-location <lat> <long>: The fake target location (default 30.286838, -97.73659)");
		System.err.println("\t-mobilityPlane <traxxas|segway|create>: The type of mobility plane being used (default traxxas)");
		System.err.println("\t-mCastAddress <ip address>: The Pharos Server's multicast group address (default 230.1.2.3)");
		System.err.println("\t-mCastPort <port number>: The Pharos Server's multicast port number (default 6000)");
		System.err.println("\t-beaconMinPeriod <time>: The minimum interbeacon transmit interval in ms (default: 280)");
		System.err.println("\t-beaconMaxPeriod <time>: The maximum interbeacon transmit interval in ms (default: 320)");
		System.err.println("\t-beaconTxPower <power>: The beacon transmit power [not sure if this does anything] (default: 31)");
			}

	public static void main(String[] args) throws InterruptedException, NoNewDataException {
		String serverIP = "localhost";
		int serverPort = 6665;
		String mCastAddress = "230.1.2.3";
		int mCastPort = 6000;
		MotionArbiter.MotionType mobilityPlane = MotionArbiter.MotionType.MOTION_TRAXXAS;
		
		double fakeLatitude = 30.286838;
		double fakeLongitude = -97.73659;
		
		long beaconMinPeriod = 280;
		long beaconMaxPeriod = 320;
		short beaconTxPower = 31;

		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-server"))
					serverIP = args[++i];
				else if (args[i].equals("-port"))
					serverPort = Integer.valueOf(args[++i]);
				else if (args[i].equals("-location"))
				{
					fakeLatitude = Double.valueOf(args[++i]);
					fakeLongitude = Double.valueOf(args[++i]);
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
				else if (args[i].equals("-beaconMinPeriod")) {
					beaconMinPeriod = Long.valueOf(args[++i]);
				}
				else if (args[i].equals("-beaconMaxPeriod")) {
					beaconMaxPeriod = Long.valueOf(args[++i]);
				}
				else if (args[i].equals("-beaconTxPower")) {
					beaconTxPower = Short.valueOf(args[++i]);
				}
				else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
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

		System.out.println("Server IP: " + serverIP);
		System.out.println("Server port: " + serverPort);
		System.out.println("Multicast address: " + mCastAddress);
		System.out.println("Multicast port: " + mCastPort);
		System.out.println("Beacon min period: " + beaconMinPeriod);
		System.out.println("Beacon max period: " + beaconMaxPeriod);
		System.out.println("Beacon TX power: " + beaconTxPower);
		System.out.println("Fake target location: " + fakeLatitude + ", " + fakeLongitude);
		
		new RobotPerimeterStarter(serverIP, serverPort, mCastAddress, mCastPort, mobilityPlane, beaconMinPeriod, beaconMaxPeriod, beaconTxPower, fakeLatitude, fakeLongitude);
	}
}