package pharoslabut.demo.cancr;

import pharoslabut.RobotIPAssignments;
import pharoslabut.exceptions.PharosException;
import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.Location;
import pharoslabut.navigate.MotionArbiter;
import pharoslabut.navigate.NavigateCompassGPS;
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

public class SimpleMule implements ProteusOpaqueListener {

	String playerServerIP = "localhost";
	int playerServerPort = 6665;
	PlayerClient client;
	private CompassDataBuffer compassDataBuffer;
	private GPSDataBuffer gpsDataBuffer;
	private MotionArbiter motionArbiter;
	double velocity = 2.5;
	long pauseTime = 30000;
	ContextSender contextSender;
	
	Location srcLoc = new Location(30.5263083,	-97.6324533);
	Location sinkLoc = new Location(30.526845,	-97.6324783);
	
	//Location srcLoc = new Location(30.52626, -97.6324133); // lollipop waypoint 1
	//Location sinkLoc = new Location(30.5274533, -97.632525); // lollipop waypoint 6
	//Location sinkLoc = new Location(30.52707, -97.6321167); // lollipop waypoint 4
	
	public SimpleMule(String expName) throws PharosException {
		String fileName = expName + "-" + RobotIPAssignments.getName() + "-SimpleMule_" + FileLogger.getUniqueNameExtension() + ".log"; 
		FileLogger expFlogger = new FileLogger(fileName);
		Logger.setFileLogger(expFlogger);
		
		createPlayerClient(MotionArbiter.MotionType.MOTION_TRAXXAS);
		
		contextSender = new ContextSender(gpsDataBuffer, srcLoc);
		
		Logger.log("Starting experiment at time " + System.currentTimeMillis() + "...");
		
		// Start the individual components
		if (compassDataBuffer != null)			compassDataBuffer.start();
		
		NavigateCompassGPS navigatorGPS = new NavigateCompassGPS(motionArbiter, compassDataBuffer, 
				gpsDataBuffer);
		
		Logger.log("Going to the source...");
		navigatorGPS.go(null, srcLoc, velocity);
		pause(pauseTime);
		
		int counter = 0;
		while(true) {
			Logger.log("Going to the sink " + counter + "...");
			navigatorGPS.go(srcLoc, sinkLoc, velocity);
			contextSender.setDestLoc(sinkLoc);
			
			pause(pauseTime);
			
			Logger.log("Going to the source " + counter + "...");
			navigatorGPS.go(sinkLoc, srcLoc, velocity);
			contextSender.setDestLoc(srcLoc);
			
			counter++;
			pause(pauseTime);
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
	
	@Override
	public void newOpaqueData(ProteusOpaqueData opaqueData) {
		if (opaqueData.getDataCount() > 0) {
			String s = new String(opaqueData.getData());
			Logger.log("MCU Message: " + s);
		}
	}
	
	public static void main(String[] args) {
		try {
			new SimpleMule(args[0]);
		} catch (PharosException e) {
			e.printStackTrace();
		}
	}
}
