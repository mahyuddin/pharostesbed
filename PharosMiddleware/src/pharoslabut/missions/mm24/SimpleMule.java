package pharoslabut.missions.mm24;

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
	double velocity = 0.5;
	long pauseTime = 0;
	
	Location PRCE = new Location(30.3854533,	-97.7243483);
	Location PRCF = new Location(30.3857033,	-97.7251833);
	
	Location srcLoc = PRCE; //new Location(30.5263083,	-97.6324533);
	Location sinkLoc = PRCF; //new Location(30.526845,	-97.6324783);
	
	Location sinkGridCoord = new Location(0,0);
	Location srcGridCoord = new Location(1,0);
	
	public SimpleMule(String expName) throws PharosException {
		
		String fileName = expName + "-" + RobotIPAssignments.getName() + "-SimpleMule_" + FileLogger.getUniqueNameExtension() + ".log"; 
		FileLogger expFlogger = new FileLogger(fileName);
		Logger.setFileLogger(expFlogger);
		
		createPlayerClient(MotionArbiter.MotionType.MOTION_TRAXXAS);
		
		Logger.log("Starting experiment at time " + System.currentTimeMillis() + "...");
		
		// Start the individual components
		if (compassDataBuffer != null)
			compassDataBuffer.start();
		
		NavigateCompassGPS navigatorGPS = new NavigateCompassGPS(motionArbiter, compassDataBuffer, 
				gpsDataBuffer);
		
		
		Logger.log("Going to " + srcLoc + " at " + velocity + " m/s");
		navigatorGPS.go(null, srcLoc, velocity);
	
		pause(pauseTime);
		
		Logger.log("Going to " + sinkLoc + " at " + velocity + " m/s");
		navigatorGPS.go(srcLoc, sinkLoc, velocity);
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
		print("Usage: " + SimpleMule.class.getName() + " <options> [expName]\n");
		print("Where <options> include:");
		print("\t-debug: enable debug mode");
		System.exit(0);
	}
	
	public static void main(String[] args) {
		
		if (args.length == 0)
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
			new SimpleMule(expName);
		} catch (PharosException e) {
			e.printStackTrace();
		}
	}
}
