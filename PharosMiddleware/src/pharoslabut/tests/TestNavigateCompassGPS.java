package pharoslabut.tests;

import playerclient3.*;
import playerclient3.structures.PlayerConstants;
import playerclient3.structures.position2d.PlayerPosition2dData;

import pharoslabut.logger.*;
import pharoslabut.navigate.*;
import pharoslabut.sensors.Position2DBuffer;
import pharoslabut.sensors.Position2DListener;
import pharoslabut.sensors.CompassDataBuffer;
import pharoslabut.sensors.GPSDataBuffer;

/**
 * Navigates a robot to a specific position.
 * 
 * @author Chien-Liang Fok
 */
public class TestNavigateCompassGPS implements Position2DListener {
	
	private CompassDataBuffer compassDataBuffer;
	
	private GPSDataBuffer gpsDataBuffer;
	private MotionArbiter motionArbiter;
	
	private FileLogger flogger;
	
	/**
	 * The constructor.
	 * 
	 * @param serverIP The IP address of the player server.
	 * @param serverPort The port of the player server.
	 * @param mobilityPlane The type of mobility plane to use.
	 * @param latitude The latitude of the destination location.
	 * @param longitude The longitude of the destination location.
	 * @param velocity The velocity at which to travel in m/s.
	 * @param fileName The name of the file in which to log debug info, may be null.
	 */
	public TestNavigateCompassGPS(String serverIP, int serverPort, MotionArbiter.MotionType mobilityPlane, 
			double latitude, double longitude, double velocity, String fileName) 
	{
		if (fileName != null)
			flogger = new FileLogger(fileName);

		log("Connecting to player server " + serverIP + ":" + serverPort + "...");
		PlayerClient client = null;
		try {
			client = new PlayerClient(serverIP, serverPort);
		} catch(PlayerException e) {
			logErr("could not connect to player server: ");
			logErr("    [ " + e.toString() + " ]");
			System.exit (1);
		}

		log("Subscribing to motor interface...");
		Position2DInterface motors = client.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (motors == null) {
			logErr("motors is null");
			System.exit(1);
		}
		
		// The Traxxas and Segway mobility planes' compasses are Position2D devices at index 1,
		// while the Segway RMP 50's compass is on index 2.
		log("Subscribing to compass interface...");
		Position2DInterface compass;
		if (mobilityPlane == MotionArbiter.MotionType.MOTION_IROBOT_CREATE ||
				mobilityPlane == MotionArbiter.MotionType.MOTION_TRAXXAS) {
			compass = client.requestInterfacePosition2D(1, PlayerConstants.PLAYER_OPEN_MODE);
		} else {
			compass = client.requestInterfacePosition2D(2, PlayerConstants.PLAYER_OPEN_MODE);
		}
		if (compass == null) {
			logErr("compass is null");
			System.exit(1);
		}
		
		log("Subscribing to GPS interface...");
		GPSInterface gps = client.requestInterfaceGPS(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (gps == null) {
			logErr("gps is null");
			System.exit(1);
		}
		
		log("Changing Player server mode to PUSH...");
		client.requestDataDeliveryMode(playerclient3.structures.PlayerConstants.PLAYER_DATAMODE_PUSH);
		
		log("Setting Player Client to run in continuous threaded mode...");
		client.runThreaded(-1, -1);
		
		log("Creating MotionArbiter of type " + mobilityPlane + "...");
		motionArbiter = new MotionArbiter(mobilityPlane, motors);
		motionArbiter.setFileLogger(flogger);
		
		log("Creating CompassDataBuffer...");
		compassDataBuffer = new CompassDataBuffer(compass);
		compassDataBuffer.setFileLogger(flogger);
		compassDataBuffer.start();
		
		log("Creating GPSDataBuffer...");
		gpsDataBuffer = new GPSDataBuffer(gps);
		gpsDataBuffer.setFileLogger(flogger);
		
		log("Resetting the odometer...");
		motors.resetOdometry();
		
		log("Listening for Position2D events (odmeter data)...");
		Position2DBuffer p2dBuff = new Position2DBuffer(motors);
		p2dBuff.addPos2DListener(this);
		p2dBuff.start();
		
		log("Creating NavigateCompassGPS object...");
		NavigateCompassGPS navigatorGPS = new NavigateCompassGPS(motionArbiter, compassDataBuffer, 
				gpsDataBuffer, flogger);
		
		Location destLoc = new Location(latitude, longitude);
		log("Going to: " + destLoc + " at " + velocity);
		
		if (!navigatorGPS.go(destLoc, velocity))
			logErr("Unable to reach " + destLoc); 
		else
			log("SUCCESS!");
		
		System.exit(0);
	}

	@Override
	public void newPlayerPosition2dData(PlayerPosition2dData data) {
		log(data.toString());
	}

	private void logErr(String msg) {
		String result = "TestNavigateCompassGPS: ERROR: " + msg;
		System.err.println(result);
		if (flogger != null)
			flogger.log(result);
	}
	
	private void log(String msg) {
		String result = "TestNavigateCompassGPS: " + msg;
		System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
	
	private static void usage() {
		System.err.println("Usage: pharoslabut.tests.TestNavigateCompassGPS <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
		System.err.println("\t-mobilityPlane <traxxas|segway|create>: The type of mobility plane being used (default traxxas)");
		System.err.println("\t-log <file name>: name of file in which to save results (default null)");
		System.err.println("\t-latitude <latitude>: The latitude of the destination location (default 30.385645)");
		System.err.println("\t-longitude <longitude>: The longitude of the destination location (default -97.7251983)");
		System.err.println("\t-velocity <speed>: The velocity in m/s that the robot should move (default 1.5)");
		System.err.println("\t-d: Enable debug mode");
	}
	
	public static final void main(String[] args) {
		
		String fileName = null;
		String serverIP = "localhost";
		int serverPort = 6665;
		
		MotionArbiter.MotionType mobilityPlane = MotionArbiter.MotionType.MOTION_TRAXXAS;
		
		double latitude = Double.MAX_VALUE;  
		double longitude = Double.MAX_VALUE;
		
		double velocity = 1.5;

		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-server")) {
					serverIP = args[++i];
				} 
				else if (args[i].equals("-port")) {
					serverPort = Integer.valueOf(args[++i]);
				} 
				else if (args[i].equals("-log")) {
					fileName = args[++i];
				}
				else if (args[i].equals("-mobilityPlane") || args[i].equals("-mp")) {
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
				else if (args[i].equals("-latitude")) {
					latitude = Double.valueOf(args[++i]);
				}
				else if (args[i].equals("-longitude")) {
					longitude = Double.valueOf(args[++i]);
				}
				else if (args[i].equals("-velocity")) {
					velocity = Double.valueOf(args[++i]);
				}
				else if (args[i].equals("-d") || args[i].equals("-debug")) {
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
		
		if (latitude == Double.MAX_VALUE) {
			System.err.println("Latitude not specified.");
			System.exit(1);
		}
		
		if (longitude == Double.MAX_VALUE) {
			System.err.println("Longitude not specified.");
			System.exit(1);
		}
		
		new TestNavigateCompassGPS(serverIP, serverPort, mobilityPlane, latitude, longitude, velocity, fileName);
	}
}