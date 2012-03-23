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
 * Navigates a robot to a specific position.
 * 
 * @author Chien-Liang Fok
 */
public class MM23TestNavigateCompassGPS implements Position2DListener {
	
	private CompassDataBuffer compassDataBuffer;
	
	private GPSDataBuffer gpsDataBuffer;
	private MotionArbiter motionArbiter;
	
//	private FileLogger flogger;
	
	/**
	 * The constructor.
	 * 
	 * @param serverIP The IP address of the player server.
	 * @param serverPort The port of the player server.
	 * @param mobilityPlane The type of mobility plane to use.
	 * @param compassIndex The index of the compass device.
	 */
	public MM23TestNavigateCompassGPS(String serverIP, int serverPort, 
			MotionArbiter.MotionType mobilityPlane, int compassIndex) 
	{
		Logger.logDbg("Connecting to player server " + serverIP + ":" + serverPort + "...");
		PlayerClient client = null;
		try {
			client = new PlayerClient(serverIP, serverPort);
		} catch(PlayerException e) {
			Logger.logErr("could not connect to player server: ");
			Logger.logErr("    [ " + e.toString() + " ]");
			System.exit (1);
		}

		Logger.logDbg("Subscribing to motor interface...");
		Position2DInterface motors = client.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (motors == null) {
			Logger.logErr("motors is null");
			System.exit(1);
		}
		
		Logger.logDbg("Subscribing to compass interface...");
		Position2DInterface compass;
		compass = client.requestInterfacePosition2D(compassIndex, PlayerConstants.PLAYER_OPEN_MODE);
		
		/*if (mobilityPlane == MotionArbiter.MotionType.MOTION_IROBOT_CREATE ||
				mobilityPlane == MotionArbiter.MotionType.MOTION_TRAXXAS) {
			compass = client.requestInterfacePosition2D(1, PlayerConstants.PLAYER_OPEN_MODE);
		} else {
			compass = client.requestInterfacePosition2D(2, PlayerConstants.PLAYER_OPEN_MODE);
		}*/
		if (compass == null) {
			Logger.logErr("compass is null");
			System.exit(1);
		}
		
		Logger.log("Subscribing to GPS interface...");
		GPSInterface gps = client.requestInterfaceGPS(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (gps == null) {
			Logger.logErr("gps is null");
			System.exit(1);
		}
		
		Logger.logDbg("Changing Player server mode to PUSH...");
		client.requestDataDeliveryMode(playerclient3.structures.PlayerConstants.PLAYER_DATAMODE_PUSH);
		
		Logger.logDbg("Setting Player Client to run in continuous threaded mode...");
		client.runThreaded(-1, -1);
		
		Logger.logDbg("Creating MotionArbiter of type " + mobilityPlane + "...");
		motionArbiter = new MotionArbiter(mobilityPlane, motors);
//		motionArbiter.setFileLogger(flogger);
		
		Logger.logDbg("Creating CompassDataBuffer...");
		compassDataBuffer = new CompassDataBuffer(compass);
//		compassDataBuffer.setFileLogger(flogger);
		compassDataBuffer.start();
		
		Logger.logDbg("Creating GPSDataBuffer...");
		gpsDataBuffer = new GPSDataBuffer(gps);
//		gpsDataBuffer.setFileLogger(flogger);
		
		Logger.logDbg("Resetting the odometer...");
		motors.resetOdometry();
		
		Logger.logDbg("Listening for Position2D events (odometer data)...");
		Position2DBuffer p2dBuff = new Position2DBuffer(motors);
		p2dBuff.addPos2DListener(this);
		p2dBuff.start();
		
		Logger.logDbg("Creating NavigateCompassGPS object...");
		NavigateCompassGPS navigatorGPS = new NavigateCompassGPS(motionArbiter, compassDataBuffer, gpsDataBuffer);
		
		Location wp1 = new Location(30.2824279,	-97.7245635);
		Location wp2 = new Location(30.2821587,	-97.7245004);
		
		Logger.log("Going to waypoint 1 " + wp1 + " at 1m/s");
		if (!navigatorGPS.go(null, wp1, 1)) {
			Logger.logErr("Unable to reach " + wp1);
			System.exit(1);
		}
		
		Logger.log("Going to waypoint 2 " + wp2 + " at 1m/s");
		if (!navigatorGPS.go(wp1, wp2, 1)) {
			Logger.logErr("Unable to reach " + wp2);
			System.exit(1);
		}
		
		Logger.log("Done!");
		System.exit(0);
	}

	@Override
	public void newPlayerPosition2dData(PlayerPosition2dData data) {
		Logger.log(data.toString());
	}

//	private void logErr(String msg) {
//		String result = "TestNavigateCompassGPS: ERROR: " + msg;
//		System.err.println(result);
//		if (flogger != null)
//			flogger.log(result);
//	}
//	
//	private void log(String msg) {
//		String result = "TestNavigateCompassGPS: " + msg;
//		System.out.println(result);
//		if (flogger != null)
//			flogger.log(result);
//	}
	
	private static void usage() {
		System.err.println("Usage: pharoslabut.tests.TestNavigateCompassGPS <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
		System.err.println("\t-mobilityPlane <traxxas|segway|create>: The type of mobility plane being used (default traxxas)");
		System.err.println("\t-compassIndex <index>: The index of the compass device (default 1)");
		System.err.println("\t-log <file name>: name of file in which to save results (default null)");
		System.err.println("\t-d: Enable debug mode");
	}
	
	public static final void main(String[] args) {
		
		String serverIP = "localhost";
		int serverPort = 6665;
		
		MotionArbiter.MotionType mobilityPlane = MotionArbiter.MotionType.MOTION_TRAXXAS;
				
		int compassIndex = 1;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-server")) {
					serverIP = args[++i];
				} 
				else if (args[i].equals("-port")) {
					serverPort = Integer.valueOf(args[++i]);
				} 
				else if (args[i].equals("-log")) {
					Logger.setFileLogger(new FileLogger(args[++i], true));
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
				else if (args[i].equals("-compassIndex")) {
					compassIndex = Integer.valueOf(args[++i]);
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
		
		new MM23TestNavigateCompassGPS(serverIP, serverPort, mobilityPlane, compassIndex);
	}
}