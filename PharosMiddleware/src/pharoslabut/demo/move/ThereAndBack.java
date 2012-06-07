package pharoslabut.demo.move;

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
 * Implements a simple application that moves the robot to a remote location and then back.
 * It uses the compass and GPS for navigation.
 * 
 * @author Chien-Liang
 *
 */
public class ThereAndBack implements ProteusOpaqueListener {

	String playerServerIP = "localhost";
	int playerServerPort = 6665;
	PlayerClient client;
	private CompassDataBuffer compassDataBuffer;
	private GPSDataBuffer gpsDataBuffer;
	private MotionArbiter motionArbiter;
	double velocity = 0.5;
	long pauseTime = 0;
	
	// Pickle Research Center South Parking Lot
	//Location remoteLoc = new Location(30.3854533,	-97.7243483);
	//Location returnLoc = new Location(30.3857033,	-97.7251833);
	
	// Dell Diamond North-South traversal
	//Location remoteLoc = new Location(30.5281667,	-97.6325183);  // parking space 03 (north end of lot) 
	//Location returnLoc = new Location(30.5262633,	-97.6324717);  // parking space 80 (south end of lot)
	
	// Dell Diamond South-North traversal
	//Location remoteLoc = new Location(30.5262633,	-97.6324717);  // parking space 80 (south end of lot)
	//Location returnLoc = new Location(30.5281667,	-97.6325183);  // parking space 03 (north end of lot) 
	
	// Dell Diamond West
	//Location remoteLoc = new Location(30.5271883,	-97.6314267); // East Loc
	//Location returnLoc = new Location(30.5271783,	-97.6328267); // West Loc
	
	// Dell Diamond East
	Location remoteLoc = new Location(30.5271783,	-97.6328267); // West Loc
	Location returnLoc = new Location(30.5271883,	-97.6314267); // East Loc
	
	public ThereAndBack(String expName) throws PharosException {
		
		String fileName = expName + "-" + RobotIPAssignments.getName() + "-ThereAndBack" + FileLogger.getUniqueNameExtension() + ".log"; 
		FileLogger expFlogger = new FileLogger(fileName);
		Logger.setFileLogger(expFlogger);
		
		createPlayerClient(MotionArbiter.MotionType.MOTION_TRAXXAS);
		
		Logger.log("Starting experiment at time " + System.currentTimeMillis() + "...");
		
		// Start the individual components
		if (compassDataBuffer != null)
			compassDataBuffer.start();
		
		NavigateCompassGPS navigatorGPS = new NavigateCompassGPS(motionArbiter, compassDataBuffer, gpsDataBuffer);
		
		
		Logger.log("Going to " + remoteLoc + " at " + velocity + " m/s");
		navigatorGPS.go(null, remoteLoc, velocity);
	
		pause(pauseTime);
		
		Logger.log("Going to " + returnLoc + " at " + velocity + " m/s");
		navigatorGPS.go(remoteLoc, returnLoc, velocity);
		
		Logger.log("Done.");
		System.exit(0);
	}
		
	private void pause(long duration) {
		if (duration > 0) {
			try {
				synchronized(this) {
					wait(duration);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
		Position2DInterface compass = client.requestInterfacePosition2D(2, PlayerConstants.PLAYER_OPEN_MODE);
		
//		if (mobilityPlane == MotionArbiter.MotionType.MOTION_IROBOT_CREATE ||
//				mobilityPlane == MotionArbiter.MotionType.MOTION_TRAXXAS) {
//			compass = client.requestInterfacePosition2D(1, PlayerConstants.PLAYER_OPEN_MODE);
//		} else {
//			compass = client.requestInterfacePosition2D(2, PlayerConstants.PLAYER_OPEN_MODE);
//		}
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
	
	private static void print(String msg) {
		System.out.println(msg);
	}
	
	private static void usage() {
		print("Usage: " + ThereAndBack.class.getName() + " <options> [expName]\n");
		print("Where <options> include:");
		print("\t-debug: enable debug mode");
		System.exit(0);
	}
	
	public static void main(String[] args) {
		
		if (args.length == 0)
			usage();
		if (args.length == 1 && (args[0].equals("-h") || args[0].equals("--help")))
			usage();
		
		try {
			for (int i=0; i < args.length - 1; i++) {
				if (args[i].equals("-h") || args[i].equals("--help")) {
					usage();
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
		
		String expName = args[args.length - 1];
		
		try {
			new ThereAndBack(expName);
		} catch (PharosException e) {
			e.printStackTrace();
		}
	}
}
