package pharoslabut.missions;

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
 * Navigates a robot around a loop in Mission 17.
 * This is in the North-East parking lot at the Pickle Research Center.
 * 
 * @author Chien-Liang Fok
 */
public class M17MoveInLoop implements Position2DListener {
	
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
	 * @param velocity The velocity at which to travel in m/s.
	 * @param fileName The name of the file in which to log debug info, may be null.
	 */
	public M17MoveInLoop(String serverIP, int serverPort, MotionArbiter.MotionType mobilityPlane, double velocity, String fileName) 
	{
		
		System.setProperty ("PharosMiddleware.debug", "true");
		
		if (fileName != null)
			flogger = new FileLogger(fileName);

		log("Connecting to player server " + serverIP + ":" + serverPort + "...");
		PlayerClient client = null;
		try {
			client = new PlayerClient(serverIP, serverPort);
		} catch(PlayerException e) {
			System.err.println("Error connecting to Player: ");
			System.err.println("    [ " + e.toString() + " ]");
			System.exit (1);
		}

		log("Subscribing to motor interface...");
		Position2DInterface motors = client.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (motors == null) {
			System.err.println("motors is null");
			System.exit(1);
		}
		
		log("Subscribing to compass interface...");
		Position2DInterface compass = client.requestInterfacePosition2D(2, PlayerConstants.PLAYER_OPEN_MODE);
		if (compass == null) {
			System.err.println("compass is null");
			System.exit(1);
		}
		
		log("Subscribing to GPS interface...");
		GPSInterface gps = client.requestInterfaceGPS(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (gps == null) {
			System.err.println("gps is null");
			System.exit(1);
		}
		
		log("Changing Player server mode to PUSH...");
		client.requestDataDeliveryMode(playerclient3.structures.PlayerConstants.PLAYER_DATAMODE_PUSH);
		
		log("Setting Player Client to run in continuous threaded mode...");
		client.runThreaded(-1, -1);
		
		log("Creating MotionArbiter of type " + mobilityPlane + "...");
		motors.setMotorPower(1);
		motionArbiter = new MotionArbiter(mobilityPlane, motors);
		motionArbiter.setFileLogger(flogger);
		
		log("Creating CompassDataBuffer...");
		compassDataBuffer = new CompassDataBuffer(compass);
		compassDataBuffer.setFileLogger(flogger);
		compassDataBuffer.start();
		
		log("Creating GPSDataBuffer...");
		gpsDataBuffer = new GPSDataBuffer(gps);
		gpsDataBuffer.setFileLogger(flogger);
		gpsDataBuffer.start();
		
		log("Resetting the odometer...");
		motors.resetOdometry();
		
		log("Listening for Position2D events (odmeter data)...");
		Position2DBuffer p2dBuff = new Position2DBuffer(motors);
		p2dBuff.addPos2DListener(this);
		p2dBuff.start();
		
		log("Creating NavigateCompassGPS object...");
		NavigateCompassGPS navigatorGPS = new NavigateCompassGPS(motionArbiter, compassDataBuffer, 
				gpsDataBuffer, flogger);
		
		Location waypoint1 = new Location(30.3861367, -97.7242533);
		Location waypoint2 = new Location(30.386335, -97.7241217);
		Location waypoint3 = new Location(30.3863617, -97.7244017);
		Location waypoint4 = new Location(30.38648,	-97.7243117);
		
		
		for (int i = 0; i < 10; i++) {
			log("Going to: " + waypoint2);
			if (!navigatorGPS.go(waypoint2, velocity))
				log("ERROR: Unable to reach " + waypoint2); 
			else
				log("SUCCESS!");

			log("Going to: " + waypoint4);
			if (!navigatorGPS.go(waypoint4, velocity))
				log("ERROR: Unable to reach " + waypoint4); 
			else
				log("SUCCESS!");

			log("Going to: " + waypoint3);
			if (!navigatorGPS.go(waypoint3, velocity))
				log("ERROR: Unable to reach " + waypoint3); 
			else
				log("SUCCESS!");

			log("Going to: " + waypoint1);
			if (!navigatorGPS.go(waypoint1, velocity))
				log("ERROR: Unable to reach " + waypoint1); 
			else
				log("SUCCESS!");
		}
		
		System.exit(0);
	}

	@Override
	public void newPlayerPosition2dData(PlayerPosition2dData data) {
		log(data.toString());
	}
	
	private void log(String msg) {
		String result = "TestNavigateCompassGPS: " + msg;
		System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
	
	private static void usage() {
		System.err.println("Usage: pharoslabut.missions.M17MoveInLoop <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
		System.err.println("\t-mobilityPlane <traxxas|segway|create>: The type of mobility plane being used (default traxxas)");
		System.err.println("\t-log <file name>: name of file in which to save results (default null)");
		System.err.println("\t-velocity <speed>: The velocity at which the robot should travel towards the destination (default 1.5)");
		System.err.println("\t-d: Enable debug mode");
	}
	
	public static final void main(String[] args) {
		
		String fileName = null;
		String serverIP = "localhost";
		int serverPort = 6665;
		
		MotionArbiter.MotionType mobilityPlane = MotionArbiter.MotionType.MOTION_TRAXXAS;
		
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
		
		new M17MoveInLoop(serverIP, serverPort, mobilityPlane, velocity, fileName);
	}
}