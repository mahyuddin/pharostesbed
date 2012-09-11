package pharoslabut.robotperimeter;
import java.net.InetAddress;
import java.net.UnknownHostException;

import pharoslabut.RobotIPAssignments;
import pharoslabut.navigate.MotionArbiter;
import pharoslabut.sensors.CompassDataBuffer;
import pharoslabut.sensors.GPSDataBuffer;
import pharoslabut.tasks.Priority;
import pharoslabut.tasks.MotionTask;
import pharoslabut.beacon.WiFiBeacon;
import pharoslabut.beacon.WiFiBeaconBroadcaster;
import pharoslabut.beacon.WiFiBeaconReceiver;
import pharoslabut.exceptions.NoNewDataException;
import pharoslabut.exceptions.PharosException;
import pharoslabut.logger.*;
import playerclient3.GPSInterface;
import playerclient3.PlayerClient;
import playerclient3.PlayerException;
import playerclient3.Position2DInterface;
import playerclient3.structures.PlayerConstants;

public class RobotPerimeterStarter {
	private PlayerClient client = null;
	private FileLogger flogger = null;
	WiFiBeaconReceiver wifiBeaconReceiver;
	WiFiBeaconBroadcaster wifiBeaconBroadcaster;

	public RobotPerimeterStarter(String serverIP, int serverPort,String fileName, double fakeLatitude, double fakeLongitude) throws InterruptedException, NoNewDataException 
	{
		
		// Enable logging...
		if (fileName != null) {
			flogger = new FileLogger(fileName);
			Logger.setFileLogger(flogger);
			//                        motionArbiter.setFileLogger(flogger);
		}		
		// Connect to the player server...
		try {
			client = new PlayerClient(serverIP, serverPort);
		} catch(PlayerException e) {
			log("Error connecting to Player: ");
			log("    [ " + e.toString() + " ]");
			System.exit (1);
		}

		// Subscribe to robot motors...
		Position2DInterface motors = client.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (motors == null) {
			log("motors is null");
			System.exit(1);
		}

		// Create a motion arbiter...
		MotionArbiter motionArbiter = null;
		motionArbiter = new MotionArbiter(MotionArbiter.MotionType.MOTION_TRAXXAS, motors);
		
	
		Position2DInterface compass = client.requestInterfacePosition2D(1, PlayerConstants.PLAYER_OPEN_MODE);
		if (compass == null)
		{
			log ("compass is null");
			System.exit(1);
		}
		Logger.log("Subscribing to GPS.");
		GPSInterface gps = client.requestInterfaceGPS(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (gps == null) {
			Logger.logErr("gps is null");
			System.exit(1);
		}
		
		Logger.logDbg("Setting Player Client to run in continuous threaded mode...");
		client.runThreaded(-1, -1);
		
		Logger.logDbg("Changing Player server mode to PUSH...");
		client.requestDataDeliveryMode(playerclient3.structures.PlayerConstants.PLAYER_DATAMODE_PUSH);
		
		
		CompassDataBuffer compassDataBuffer = new CompassDataBuffer(compass);
		GPSDataBuffer gpsDataBuffer = new GPSDataBuffer(gps);
		
		initWiFiBeacons();
		
		Logger.log("gps Interface:" + gps + "gpsDataBuffer: " + gpsDataBuffer.toString());

		Mobility mobility = new Mobility (motionArbiter, compassDataBuffer, gpsDataBuffer/*, wifibeaconbroadcaster*/);
		wifiBeaconBroadcaster.run();
		
		Vision vision = new Vision(fakeLatitude, fakeLongitude);
		//TODO put "vision" in different thread, set to receive gps messages
		vision.VisionControl();
		mobility.controlMotionAndIntelligence();
	}
	
	private boolean initWiFiBeacons() {
		
		String mCastAddress = "230.1.2.3";
		int mCastPort = 6000;
		int pharosServerPort = 7776;
		
		
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
			wifiBeaconBroadcaster = new WiFiBeaconBroadcaster(mCastGroupAddress, pharosIP, mCastPort, beacon,true);

			// Start receiving beacons
			wifiBeaconReceiver.start();
			return true;
		} catch (UnknownHostException e) {
			Logger.logErr("Problem initializing WiFi beacons: " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}
	

	/**
	 * Pauses the calling thread a certain amount of time.
	 * 
	 * @param duration The pause duration in milliseconds.
	 */
	private void pause(long duration) {
		synchronized(this) {
			try {
				wait(duration);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void log(String msg) {
		String result = "RobotMover: " + msg;
		System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}

	private static void usage() {
		System.err.println("Usage: pharoslabut.RobotMover <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
		System.err.println("\t-log <file name>: name of file in which to save results (default RobotMover.log)");
		System.err.println("\t-robot <robot type>: The type of robot, either traxxas, segway, or create (default traxxas)");
		System.err.println("\t-speed <speed>: The speed in meters per second (default 0.5)");
		System.err.println("\t-angle <angle>: The angle in radians (default 0)");
		System.err.println("\t-duration <duration>: The duration in milliseconds (default 1000)");
	}

	public static void main(String[] args) throws InterruptedException, NoNewDataException {
		String fileName = "SingleRobotGPSPositioning.log";
		String serverIP = "localhost";
		int serverPort = 6665;
		String robotType = "traxxas";
		double speed = 0.5;
		double angle = 0;
		long duration = 1000;
		
		double fakeLatitude = 30.286838;
		double fakeLongitude = -97.73659;;

		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-server"))
					serverIP = args[++i];
				else if (args[i].equals("-port"))
					serverPort = Integer.valueOf(args[++i]);
				else if (args[i].equals("-log"))
					fileName = args[++i];
				else if (args[i].equals("-robot")) {
					robotType = args[++i];
				}
				else if (args[i].equals("-speed"))
					speed = Double.valueOf(args[++i]);
				else if (args[i].equals("-angle"))
					angle = Double.valueOf(args[++i]);
				else if (args[i].equals("-duration"))
					duration = Long.valueOf(args[++i]);
				else if (args[i].equals("-location"))
				{
					fakeLatitude = Double.valueOf(args[++i]);
					fakeLongitude = Double.valueOf(args[++i]);
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
		System.out.println("File: " + fileName);
		System.out.println("RobotType: " + robotType);
		System.out.println("Speed: " + speed);
		System.out.println("Angle: " + angle);
		System.out.println("Duration: " + duration);

		new RobotPerimeterStarter(serverIP, serverPort, fileName, fakeLatitude, fakeLongitude);
	}
}