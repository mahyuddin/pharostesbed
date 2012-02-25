package pharoslabut.demo.mrpatrol2.daemons;

import pharoslabut.demo.mrpatrol2.config.ExpConfig;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.Location;
import pharoslabut.navigate.MotionArbiter;
import pharoslabut.sensors.Position2DBuffer;
import playerclient3.GPSInterface;
//import playerclient3.PlayerClient;
//import playerclient3.PlayerException;
import playerclient3.Position2DInterface;
import playerclient3.structures.PlayerConstants;

/**
 * The top-level class for all outdoor patrol daemons.  It initializes the
 * components shared by all outdoor patrol daemons.
 * 
 * @author Chien-Liang Fok
 *
 */
public class OutdoorPatrolDaemon extends PatrolDaemon {

	/**
	 * The robot's home location.  This is the location of the robot prior to 
	 * starting the experiment and to which the robot should move after the
	 * end of the experiment.
	 */
	private Location homeLocation;
	
	/**
	 * The constructor.
	 * 
	 * @param expConfig The experiment configuration.
	 * @param mobilityPlane The mobility plane used.
	 * @param playerServerIP The IP address of the player server.
	 * @param playerServerPort The TCP port on which the player server listens.
	 */
	public OutdoorPatrolDaemon(ExpConfig expConfig, MotionArbiter.MotionType mobilityPlane, String playerServerIP, int playerServerPort) {
		super(expConfig, mobilityPlane);
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
		
		Logger.logDbg("Subscribing to motor interface...");
		Position2DInterface p2di = playerClient.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (p2di == null) {
			Logger.logErr("Motors is null");
			System.exit(1);
		}
		Position2DBuffer pos2DBuffer = new Position2DBuffer(p2di);
		pos2DBuffer.start();
		
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
		
		// create the buffers and the NavigateCompassGPS object....
	}
	
	/**
	 * Obtains the robot's home location.
	 */
	protected void getHomeLocation() {
		// TODO...
	}
	
	/**
	 * Moves the robot to the home location.
	 */
	protected void gotoHomeLocation() {
		// TODO...
	}
}
