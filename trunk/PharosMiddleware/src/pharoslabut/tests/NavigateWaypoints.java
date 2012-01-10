package pharoslabut.tests;

import java.util.Vector;

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

/**
 * This is a simple program that makes the robot navigate a sequence
 * of waypoints.  It navigates the waypoints indefinitely.
 * 
 * @author Chien-Liang Fok
 *
 */
public class NavigateWaypoints implements ProteusOpaqueListener {

	String playerServerIP = "localhost";
	int playerServerPort = 6665;
	PlayerClient client;
	private CompassDataBuffer compassDataBuffer;
	private GPSDataBuffer gpsDataBuffer;
	private MotionArbiter motionArbiter;
	
	double velocity = 2.0;
	Vector<Waypoint> waypoints = new Vector<Waypoint>();
	
	public NavigateWaypoints(String expName) throws PharosException {
		
		// Parking lot under water tower at pickle research center
//		waypoints.add(new Waypoint("A", new Location(30.3878217, -97.7255367)));
//		waypoints.add(new Waypoint("B", new Location(30.38802, -97.7254733)));
//		waypoints.add(new Waypoint("C", new Location(30.3881283, -97.7258183)));
//		waypoints.add(new Waypoint("D", new Location(30.38805, -97.7259033)));
//		waypoints.add(new Waypoint("E", new Location(30.3880983, -97.725995)));
//		waypoints.add(new Waypoint("F", new Location(30.3882533, -97.72629)));
//		waypoints.add(new Waypoint("G", new Location(30.3881233, -97.726325)));
//		waypoints.add(new Waypoint("H", new Location(30.3879133, -97.725855)));
//		waypoints.add(new Waypoint("I", new Location(30.3878433, -97.725645)));
		
		// Parking lot North of PRC entrance
//		waypoints.add(new Waypoint("A2", new Location(30.386585, -97.7238433)));
//		waypoints.add(new Waypoint("B2", new Location(30.3868017, -97.7245083)));
		
		// Parking lot NorthWest of PRC entrance.
		waypoints.add(new Waypoint("MM19-A1", new Location(30.3869617, -97.7251817)));
		waypoints.add(new Waypoint("MM19-B1", new Location(30.3866467, -97.7253867)));
		
		
		
		String fileName = expName + "-" + RobotIPAssignments.getName() + "-Pharos_" + FileLogger.getUniqueNameExtension() + ".log"; 
		FileLogger expFlogger = new FileLogger(fileName);
		Logger.setFileLogger(expFlogger);
		
		createPlayerClient(MotionArbiter.MotionType.MOTION_TRAXXAS);
		
		Logger.log("Starting experiment at time " + System.currentTimeMillis() + "...");
		
		// Start the individual components
		if (compassDataBuffer != null)			compassDataBuffer.start();
		
		NavigateCompassGPS navigatorGPS = new NavigateCompassGPS(motionArbiter, compassDataBuffer, 
				gpsDataBuffer);
		
		int loopCounter = 0;
		while(true) {
			for (int i=0; i < waypoints.size(); i++) {
				Waypoint currWP = waypoints.get(i);
				Logger.log("Loop " + loopCounter + ", going to waypoint " + currWP.name + "(" + currWP.loc + ")...");
				navigatorGPS.go(currWP.loc, velocity);
				pause(2000);
			}
			loopCounter++;
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
			new NavigateWaypoints(args[0]);
		} catch (PharosException e) {
			e.printStackTrace();
		}
	}
	
	private class Waypoint {
		String name;
		Location loc;
		
		public Waypoint(String name, Location loc) {
			this.loc = loc;
			this.name = name;
		}
	}
}

