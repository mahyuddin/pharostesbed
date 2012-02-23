package pharoslabut.demo.cancr;

import pharoslabut.RobotIPAssignments;
import pharoslabut.exceptions.PharosException;
import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.Location;
import pharoslabut.navigate.MotionArbiter;
import pharoslabut.sensors.GPSDataBuffer;
import pharoslabut.sensors.ProteusOpaqueData;
import pharoslabut.sensors.ProteusOpaqueInterface;
import pharoslabut.sensors.ProteusOpaqueListener;
import playerclient3.GPSInterface;
import playerclient3.PlayerClient;
import playerclient3.PlayerException;
import playerclient3.structures.PlayerConstants;

/**
 * This provides context information to the static nodes in outdoor tests.
 * 
 * @author Chien-Liang Fok
 */
public class SimpleContextProvider implements ProteusOpaqueListener {

	String playerServerIP = "localhost";
	int playerServerPort = 6665;
	PlayerClient client;
	private GPSDataBuffer gpsDataBuffer;
	ContextSender contextSender;
	
	Location srcLoc = new Location(30.5263083,	-97.6324533);
	Location sinkLoc = new Location(30.526845,	-97.6324783);
	
	//Location srcLoc = new Location(30.52626, -97.6324133); // lollipop waypoint 1
	//Location sinkLoc = new Location(30.5274533, -97.632525); // lollipop waypoint 6
	
	public SimpleContextProvider(String expName) throws PharosException {
		String fileName = expName + "-" + RobotIPAssignments.getName() + "-SimpleContextProvider_" + FileLogger.getUniqueNameExtension() + ".log"; 
		FileLogger expFlogger = new FileLogger(fileName);
		Logger.setFileLogger(expFlogger);
		
		createPlayerClient(MotionArbiter.MotionType.MOTION_TRAXXAS);
		
		contextSender = new ContextSender(gpsDataBuffer, srcLoc);
		
		Logger.log("Starting experiment at time " + System.currentTimeMillis() + "...");
		while(true) {
			pause(30000);
		}
	}
	
	private void pause(long duration) {
		try {
			synchronized(this) {
				wait(duration);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates the player client and obtains the necessary interfaces from it.
	 * Creates the CompassDataBuffer, GPSDataBuffer, and MotionArbiter objects.
	 * 
	 * @return true if successful.
	 */
	private boolean createPlayerClient(MotionArbiter.MotionType mobilityPlane) {
		
		Logger.log("Creating player client...");
		try {
			client = new PlayerClient(playerServerIP, playerServerPort);
		} catch(PlayerException e) {
			Logger.logErr("Unable to connecting to Player: ");
			Logger.logErr("    [ " + e.toString() + " ]");
			return false;
		}
		
//		Logger.log("Subscribing to motors.");
//		Position2DInterface motors = client.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
//		if (motors == null) {
//			Logger.logErr("motors is null");
//			return false;
//		}
//		
		// The Traxxas and Segway mobility planes' compasses are Position2D devices at index 1,
		// while the Segway RMP 50's compass is on index 2.
//		Logger.log("Subscribing to compass.");
//		Position2DInterface compass;
//		if (mobilityPlane == MotionArbiter.MotionType.MOTION_IROBOT_CREATE ||
//				mobilityPlane == MotionArbiter.MotionType.MOTION_TRAXXAS) {
//			compass = client.requestInterfacePosition2D(1, PlayerConstants.PLAYER_OPEN_MODE);
//		} else {
//			compass = client.requestInterfacePosition2D(2, PlayerConstants.PLAYER_OPEN_MODE);
//		}
//		if (compass == null) {
//			Logger.logErr("compass is null");
//			return false;
//		}
		
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
		
		gpsDataBuffer = new GPSDataBuffer(gps);
		oi.addOpaqueListener(this);
		
		Logger.log("Changing Player server mode to PUSH...");
		client.requestDataDeliveryMode(playerclient3.structures.PlayerConstants.PLAYER_DATAMODE_PUSH);
		
		Logger.log("Setting Player Client to run in continuous threaded mode...");
		client.runThreaded(-1, -1);
		
		return true;
	}
	
	@Override
	public void newOpaqueData(ProteusOpaqueData opaqueData) {
		if (opaqueData.getDataCount() > 0) {
			String s = new String(opaqueData.getData());
			Logger.log("MCU Message: " + s);
		}
	}
	
	public static void main(String[] args) {
		try {
			new SimpleContextProvider(args[0]);
		} catch (PharosException e) {
			e.printStackTrace();
		}
	}
}
