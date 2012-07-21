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

public class SimpleMuleHalfLot implements ProteusOpaqueListener {
	enum RouteType {NORTH, SOUTH};
	
	RouteType routeType;
	String playerServerIP = "localhost";
	int playerServerPort = 6665;
	PlayerClient client;
	private CompassDataBuffer compassDataBuffer;
	private GPSDataBuffer gpsDataBuffer;
	private MotionArbiter motionArbiter;
	double velocity = 1.0;
	long pauseTime = 0;
	private boolean updateContext;
	ContextSender contextSender;
	
	Location northLoc = new Location(30.5281667,	-97.6325183);
	Location southLoc = new Location(30.5262633,	-97.6324717);
	Location midNorthLoc = new Location(30.52737,	-97.63248);
	Location midSouthLoc = new Location(30.52718,	-97.63248); 
		
	Location northLocGridCoord = new Location(2,0);
	Location southLocGridCoord = new Location(0,0);
	Location midNorthLocGridCoord = new Location(1, 0);
	Location midSouthLocGridCoord = new Location(1, 0);
	
	public SimpleMuleHalfLot(String expName, boolean updateContext, RouteType routeType) 
	throws PharosException {
		this.updateContext = updateContext;
		
		String fileName = expName + "-" + RobotIPAssignments.getName() + "-SimpleMuleHalfLot_" + FileLogger.getUniqueNameExtension() + ".log"; 
		FileLogger expFlogger = new FileLogger(fileName);
		Logger.setFileLogger(expFlogger);
		
		createPlayerClient(MotionArbiter.MotionType.MOTION_TRAXXAS);
		
		if (updateContext) {
			switch(routeType) {
			case NORTH:
				contextSender = new ContextSender(gpsDataBuffer, northLocGridCoord);
				break;
			case SOUTH:
				contextSender = new ContextSender(gpsDataBuffer, southLocGridCoord);
				break;
			}
		}
		
		Logger.log("Starting experiment at time " + System.currentTimeMillis() + "...");
				
		// Start the individual components
		if (compassDataBuffer != null)
			compassDataBuffer.start();
		
		NavigateCompassGPS navigatorGPS = new NavigateCompassGPS(motionArbiter, compassDataBuffer, 
				gpsDataBuffer);
		
		switch(routeType) {
		case NORTH:
			Logger.log("Going to the north waypoint.");
			navigatorGPS.go(null, northLoc, velocity);
			break;
		case SOUTH:
			navigatorGPS.go(null, southLoc, velocity);
			break;
		}
		
		pause(pauseTime);
		
		int counter = 0;
		while(true) {
			
			switch(routeType) {
			case NORTH:
				goToNorthLoc(navigatorGPS, counter);
				break;
			case SOUTH:
				goToSouthLoc(navigatorGPS, counter);
				break;
			}
			
			pause(pauseTime);
			
			switch(routeType) {
			case NORTH:
				goToMidNorthLoc(navigatorGPS, counter);
				break;
			case SOUTH:
				goToMidSouthLoc(navigatorGPS, counter);
				break;
			}
			
			counter++;
			pause(pauseTime);
		}
	}
	
	private void goToNorthLoc(NavigateCompassGPS navigatorGPS, int counter) {
		Logger.log("Going to the north location " + counter + "...");
		
		if (updateContext)
			contextSender.setDestLoc(northLocGridCoord);
		navigatorGPS.go(midNorthLoc, northLoc, velocity);
	}
	
	private void goToMidNorthLoc(NavigateCompassGPS navigatorGPS, int counter) {
		Logger.log("Going to the middle north location " + counter + "...");
		
		if (updateContext)
			contextSender.setDestLoc(midNorthLocGridCoord);
		navigatorGPS.go(northLoc, midNorthLoc, velocity);
	}
	
	private void goToSouthLoc(NavigateCompassGPS navigatorGPS, int counter) {
		Logger.log("Going to the south location " + counter + "...");
		
		if (updateContext)
			contextSender.setDestLoc(southLocGridCoord);
		navigatorGPS.go(midSouthLoc, southLoc, velocity);
	}
	
	private void goToMidSouthLoc(NavigateCompassGPS navigatorGPS, int counter) {
		Logger.log("Going to the middle south location " + counter + "...");
		
		if (updateContext)
			contextSender.setDestLoc(midSouthLocGridCoord);
		navigatorGPS.go(southLoc, midSouthLoc, velocity);
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
		print("Usage: " + SimpleMuleHalfLot.class.getName() + " <options> [expName]\n");
		print("Where <options> include:");
		print("\t-updateContext: Send context information to the context agent.");
		print("\t-route [north/south]: Select which route the robot will travel (default north).");
		print("\t-debug: enable debug mode");
		System.exit(0);
	}
	
	public static void main(String[] args) {
		boolean updateContext = false;
		RouteType route = RouteType.NORTH;
		
		if (args.length == 0)
			usage();
		
		try {
			for (int i=0; i < args.length - 1; i++) {
				if (args[i].equals("-updateContext")) {
					updateContext = true;
				} 
				else if (args[i].equals("-route")) {
					String routeName = args[++i];
					if (routeName.equals("north"))
						route = RouteType.NORTH;
					else if (routeName.equals("south"))
						route = RouteType.SOUTH;
					else {
						System.err.println("Unknown route type.");
						usage();
					}
				} 
				else if (args[i].equals("-h") || args[i].equals("--help")) {
					usage();
				}
//				else if (args[i].equals("-playerServer")) {
//					playerIP = args[++i];
//				} 
//				else if (args[i].equals("-playerPort")) {
//					playerPort = Integer.valueOf(args[++i]);
//				}
//				else if (args[i].equals("-mCastAddress")) {
//					mCastAddress = args[++i];
//				}
//				else if (args[i].equals("-mCastPort")) {
//					mCastPort = Integer.valueOf(args[++i]);
//				}
//				else if (args[i].equals("-mobilityPlane")) {
//					String mp = args[++i].toLowerCase();
//					if (mp.equals("traxxas"))
//						mobilityPlane = MotionArbiter.MotionType.MOTION_TRAXXAS;
//					else if (mp.equals("segway"))
//						mobilityPlane = MotionArbiter.MotionType.MOTION_SEGWAY_RMP50;
//					else if (mp.equals("create"))
//						mobilityPlane = MotionArbiter.MotionType.MOTION_IROBOT_CREATE;
//					else {
//						System.err.println("Unknown mobility plane " + mp);
//						usage();
//						System.exit(1);
//					}
//				}
				else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				}
//				else if (args[i].equals("-pharosPort")) {
//					pharosPort = Integer.valueOf(args[++i]);
//				}
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
			new SimpleMuleHalfLot(expName, updateContext, route);
		} catch (PharosException e) {
			e.printStackTrace();
		}
	}
}
