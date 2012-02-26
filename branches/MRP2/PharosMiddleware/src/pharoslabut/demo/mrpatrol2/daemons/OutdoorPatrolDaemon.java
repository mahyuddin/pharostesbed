package pharoslabut.demo.mrpatrol2.daemons;

import pharoslabut.demo.mrpatrol2.config.ExpConfig;
import pharoslabut.exceptions.NoNewDataException;
//import pharoslabut.io.Message;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.Location;
import pharoslabut.navigate.MotionArbiter;
import pharoslabut.navigate.NavigateCompassGPS;
import pharoslabut.sensors.CompassDataBuffer;
import pharoslabut.sensors.GPSDataBuffer;
//import pharoslabut.sensors.Position2DBuffer;
//import pharoslabut.sensors.Position2DListener;
import playerclient3.GPSInterface;
//import playerclient3.PlayerClient;
//import playerclient3.PlayerException;
import playerclient3.Position2DInterface;
import playerclient3.structures.PlayerConstants;

/**
 * The top-level class for all outdoor patrol daemons.  It initializes the
 * components shared by all outdoor patrol daemons.  These components include
 * the compass and GPS objects.
 * 
 * @author Chien-Liang Fok
 *
 */
public abstract class OutdoorPatrolDaemon extends PatrolDaemon {

	/**
	 * The speed (in m/s) at which the robot should travel when it goes back home.
	 */
	public static final double SPEED_TO_HOME = 1.0;
	
	/**
	 * The speed (in m/s) at which the robot should travel when it goes to the first
	 * waypoint.
	 */
	public static final double SPEED_TO_FIRST_WAYPOINT = 1.0;
	
	/**
	 * The robot's home location.  This is the location of the robot prior to 
	 * starting the experiment and to which the robot should move after the
	 * end of the experiment.
	 */
	//private Location homeLocation;
	
	/**
	 * A buffer for incoming compass data.
	 */
	private CompassDataBuffer compassDataBuffer;
	
	/**
	 * A buffer for incoming GPS data.
	 */
	private GPSDataBuffer gpsDataBuffer;
	
	/**
	 * Provides access to the mobility plane.
	 */
	private MotionArbiter motionArbiter;
	
	/**
	 * Navigates a robot from its current location to a specified location.
	 */
	protected NavigateCompassGPS navigatorCompassGPS;
	
	/**
	 * The constructor.  It initializes the player client for outdoor patrol daemons.
	 * 
	 * @param expConfig The experiment configuration.
	 * @param mobilityPlane The mobility plane used.
	 * @param playerServerIP The IP address of the player server.
	 * @param playerServerPort The TCP port on which the player server listens.
	 */
	public OutdoorPatrolDaemon(ExpConfig expConfig, MotionArbiter.MotionType mobilityPlane, String playerServerIP, int playerServerPort, String mCastAddress, int mCastPort) {
		super(expConfig, mobilityPlane, mCastAddress, mCastPort);
		initPlayerClient(playerServerIP, playerServerPort);
	}
	
	/**
	 * Initializes the player client.
	 * 
	 * @param playerServerIP The IP address of the player server.
	 * @param playerServerPort The TCP port on which the player server listens.
	 */
	protected void initPlayerClient(String playerServerIP, int playerServerPort) {
		super.initPlayerClient(playerServerIP, playerServerPort);
		
		Logger.logDbg("Subscribing to compass interface...");
		Position2DInterface compass;
		if (mobilityPlane == MotionArbiter.MotionType.MOTION_IROBOT_CREATE ||
				mobilityPlane == MotionArbiter.MotionType.MOTION_TRAXXAS) {
			compass = playerClient.requestInterfacePosition2D(1, PlayerConstants.PLAYER_OPEN_MODE);
		} else {
			compass = playerClient.requestInterfacePosition2D(2, PlayerConstants.PLAYER_OPEN_MODE);
		}
		if (compass == null) {
			Logger.logErr("compass is null");
			System.exit(1);
		}
		
		Logger.log("Subscribing to GPS interface...");
		GPSInterface gps = playerClient.requestInterfaceGPS(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (gps == null) {
			Logger.logErr("gps is null");
			System.exit(1);
		}
		
		Logger.log("Changing Player server mode to PUSH...");
		playerClient.requestDataDeliveryMode(playerclient3.structures.PlayerConstants.PLAYER_DATAMODE_PUSH);
		
		Logger.log("Setting Player Client to run in continuous threaded mode...");
		playerClient.runThreaded(-1, -1);
		
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
		
		Logger.logDbg("Creating NavigateCompassGPS object...");
		navigatorCompassGPS = new NavigateCompassGPS(motionArbiter, compassDataBuffer, 
				gpsDataBuffer);
	}
	
	/**
	 * Obtains the robot's location.  This is a blocking call.  It does not return until GPS data becomes available.
	 * 
	 * @return The current GPS location
	 */
	protected Location getLocation() {
		Location result = null;
		Logger.logDbg("Getting current location...");
		while (result == null) {
			try {
				result = new Location(gpsDataBuffer.getCurrLoc());
			} catch(NoNewDataException e) {
				Logger.logDbg("No GPS data, pausing for one second then trying again...");
				synchronized(this) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		Logger.logDbg("Current location: " + result);
		return result;
	}
	
//	/**
//	 * Moves the robot to the home location.
//	 */
//	protected void gotoHomeLocation() {
//		if (homeLocation != null) {
//			Logger.logDbg("Going to home location " + homeLocation);
//			goToLocation(homeLocation);
//		} else {
//			Logger.logErr("Home location not set!");
//			System.exit(1);
//		}
//	}
	
//	protected void goToLocation(Location loc) {
//		if (navigatorCompassGPS != null) {
//			navigatorCompassGPS.go(loc, SPEED_TO_HOME);
//		} else {
//			Logger.logErr("Navigator not initialized.");
//			System.exit(1);
//		}
//	}
}
