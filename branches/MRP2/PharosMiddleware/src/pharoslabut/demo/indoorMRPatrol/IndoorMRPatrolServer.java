package pharoslabut.demo.indoorMRPatrol;

import java.net.InetAddress;
import java.net.UnknownHostException;

import pharoslabut.RobotIPAssignments;
import pharoslabut.beacon.WiFiBeacon;
import pharoslabut.beacon.WiFiBeaconBroadcaster;
import pharoslabut.beacon.WiFiBeaconEvent;
import pharoslabut.beacon.WiFiBeaconListener;
import pharoslabut.beacon.WiFiBeaconReceiver;
import pharoslabut.exceptions.PharosException;
import pharoslabut.io.Message;
import pharoslabut.io.MessageReceiver;
import pharoslabut.io.SetTimeMsg;
import pharoslabut.io.TCPMessageReceiver;
import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.LineFollower;
import pharoslabut.navigate.MotionArbiter;
import pharoslabut.sensors.PathLocalizerOverheadMarkers;
import pharoslabut.sensors.Position2DBuffer;
import pharoslabut.sensors.ProteusOpaqueData;
import pharoslabut.sensors.ProteusOpaqueInterface;
import pharoslabut.sensors.ProteusOpaqueListener;
import pharoslabut.sensors.RangerDataBuffer;
import playerclient3.PlayerClient;
import playerclient3.PlayerException;
import playerclient3.Position2DInterface;
import playerclient3.RangerInterface;
import playerclient3.structures.PlayerConstants;
import edu.utexas.ece.mpc.context.ContextHandler;
import edu.utexas.ece.mpc.context.ContextHandler.WireSummaryType;

/**
 * This should run on each robot.  It handles the execution of the indoor multi-robot patrol
 * application.
 * 
 * @see IndoorMRPatrolClient
 * @author Chien-Liang Fok
 */
public class IndoorMRPatrolServer implements MessageReceiver, WiFiBeaconListener, ProteusOpaqueListener {
	
	/**
	 * The name of the local robot.
	 */
	private String robotName;
	
	/**
	 * The IP address of the player server.
	 */
	private String playerServerIP;
	
	/**
	 * The TCP port on which the player server listens.
	 */
	private int playerServerPort;
	
    /**
     * The player client that connects to the player server.
     */
	private PlayerClient playerClient = null;
	
	/**
	 * This TCP port on which this server listens.
	 */
	private int indoorMRPatrolServerPort;
	
    /**
	 * The WiFi multicast group address.
	 */
    private String mCastAddress;
    
    /**
	 * The WiFi multicast port.
	 */
    private int mCastPort;
	
	// Components for sending and receiving WiFi beacons
	private WiFiBeaconBroadcaster wifiBeaconBroadcaster;
	private WiFiBeaconReceiver wifiBeaconReceiver;
	
	// Components for sending and receiving TelosB beacons
	//private TelosBeaconBroadcaster telosRadioSignalMeter;
	
	//private MotionScript gpsMotionScript;
	//private RelativeMotionScript relMotionScript;
	
	/**
	 * This is the component that allows the robot to follow a line.
	 */
	private LineFollower lineFollower;
	
	/**
	 * This localizes the robot by detecting overhead markers
	 * and using odometry.
	 */
	private PathLocalizerOverheadMarkers pathLocalizer;
	
	/**
	 * This is the file logger that is used for debugging purposes.  It is used when debug mode is enabled
	 * and there is no experiments running.
	 */
	private FileLogger debugFileLogger = null;
	
	private LoadExpSettingsMsg loadSettingsMsg;
	
	//private pharoslabut.wifi.UDPRxTx udpTest;
	
	/**
	 * The constructor.  Starts the server running.
	 * 
	 * @param playerServerIP The player server's IP address.
	 * @param playerServerPort The player server's port.
	 * @param pharosServerPort This server's port.
	 * @param mCastAddress The multicast address over which to broadcast WiFi beacons.
	 * @param mCastPort the multicast port over which to broadcast WiFi beacons.
	 * @param mobilityPlane The type of mobility plane being used.
	 */
	public IndoorMRPatrolServer(String playerServerIP, int playerServerPort, int pharosServerPort, 
			String mCastAddress, int mCastPort, MotionArbiter.MotionType mobilityPlane) 
	{
		this.playerServerIP = playerServerIP;
		this.playerServerPort = playerServerPort;
		this.indoorMRPatrolServerPort = pharosServerPort;
		
		this.mCastAddress = mCastAddress;
		this.mCastPort = mCastPort;
		
		/*
		 * If we're running in debug mode, start logging debug statements even before the experiment begins.
		 */
		if (System.getProperty ("PharosMiddleware.debug") != null) {
			debugFileLogger = new FileLogger("PharosExpServer-" + FileLogger.getUniqueNameExtension() + ".log");
			Logger.setFileLogger(debugFileLogger);
			Logger.log("Creating a " + getClass().getName() + "...");
		}
		
		// Get the robot's name...		
		try {
			robotName = pharoslabut.RobotIPAssignments.getName();
			Logger.log("Robot name: " + robotName);
		} catch (PharosException e1) {
			Logger.logErr("Unable to get robot's name, using 'JohnDoe'");
			robotName = "JohnDoe";
			e1.printStackTrace();
		}
		
		if (!initIndoorMRPatrolServer()) {
			Logger.logErr("Failed to initialize the Pharos server!");
			System.exit(1);
		}
		
		if (!createPlayerClient(mobilityPlane)) {
			Logger.logErr("Failed to connect to Player server!");
			System.exit(1);
		}

        // initWifi delayed until experiment type is known
	}
	
	/**
	 * Creates the player client and obtains the necessary interfaces from it.
	 * 
	 * @return true if successful.
	 */
	private boolean createPlayerClient(MotionArbiter.MotionType mobilityPlane) {
		
		// Connect to the player server.
		try {
			playerClient = new PlayerClient(playerServerIP, playerServerPort);
		} catch(PlayerException e) {
			Logger.logErr("Unable to connecting to Player: ");
			Logger.logErr("    [ " + e.toString() + " ]");
			return false;
		}
		Logger.log("Created player client.");
		
		// Subscribe to the ranger proxy.
		RangerInterface ri = playerClient.requestInterfaceRanger(0, PlayerConstants.PLAYER_OPEN_MODE);
		RangerDataBuffer rangerBuffer = new RangerDataBuffer(ri);
		rangerBuffer.start();
		Logger.log("Subscribed to the ranger proxy.");
		
		Position2DInterface p2di = playerClient.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (p2di == null) {
			Logger.logErr("motors is null");
			return false;
		}
		Position2DBuffer pos2DBuffer = new Position2DBuffer(p2di);
		pos2DBuffer.start();
		Logger.logDbg("Subscribed to Position2d proxy.");
		
		
		// Start the PathLocalizerOverheadMarkers
		pathLocalizer = new PathLocalizerOverheadMarkers(rangerBuffer, pos2DBuffer);
		Logger.log("Created the PathLocalizerOverheadMarkers.");
		
		// Start the robot following the line
		lineFollower = new LineFollower(playerClient);
		Logger.log("Created the line follower.");
		
		Logger.log("Subscribing to opaque interface.");
		ProteusOpaqueInterface oi = (ProteusOpaqueInterface)playerClient.requestInterfaceOpaque(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (oi == null) {
			Logger.logErr("opaque interface is null");
			return false;
		} else {
			oi.addOpaqueListener(this);
			Logger.log("Subscribed to opaque proxy.");
		}
		
		Logger.log("Changing Player server mode to PUSH...");
		playerClient.requestDataDeliveryMode(playerclient3.structures.PlayerConstants.PLAYER_DATAMODE_PUSH);
		
		Logger.log("Setting Player Client to run in continuous threaded mode...");
		playerClient.runThreaded(-1, -1);
		
		return true;
	}
	
	private boolean initIndoorMRPatrolServer() {
		new TCPMessageReceiver(this, indoorMRPatrolServerPort);
		return true;
	}
	
	    /**
     * Initializes the components that transmit and receive beacons.
     * 
     * @param expType
     * 
     * @return true if successful.
     */
    private boolean initWiFiBeacons(ExpType expType) {
    	// Obtain the multicast address		
		InetAddress mCastGroupAddress = null;
		try {
			mCastGroupAddress = InetAddress.getByName(mCastAddress);
		} catch (UnknownHostException uhe) {
			Logger.logErr("Problems getting multicast address");
			uhe.printStackTrace();
			return false;
		}
		
		String pharosIP;
		try {
			pharosIP = RobotIPAssignments.getAdHocIP();
		} catch (PharosException e1) {
			Logger.logErr("Unable to get ad hoc IP address: " + e1.getMessage());
			e1.printStackTrace();
			return false;
		}
		
		String pharosNI;
		try {
			pharosNI = RobotIPAssignments.getAdHocNetworkInterface();
		} catch (PharosException e1) {
			Logger.logErr("Unable to get ad hoc network interface: " + e1.getMessage());
			e1.printStackTrace();
			return false;
		}
		
		if (pharosIP == null || pharosNI == null) {
			Logger.logErr("Unable to get pharos IP or pharos network interface...");
			return false;
		}
		
		try {
            WiFiBeacon beacon = null;
            switch (expType) {
            	case UNCOORDINATED:
                case LOOSELY:
                    beacon = new IndoorMRPatrolBeacon(InetAddress.getByName(pharosIP),
                                                      indoorMRPatrolServerPort);

                    wifiBeaconReceiver = new WiFiBeaconReceiver(mCastAddress, mCastPort, pharosNI);
                    wifiBeaconBroadcaster = new WiFiBeaconBroadcaster(mCastGroupAddress, pharosIP,
                                                                      mCastPort, beacon);

                    break;
                case LABELED:
                case BLOOMIER:
                    beacon = new WiFiBeacon(InetAddress.getByName(pharosIP),
                                            indoorMRPatrolServerPort);
                    wifiBeaconReceiver = new WiFiBeaconReceiver(mCastAddress, mCastPort, pharosNI,
                                                                true);
                    wifiBeaconBroadcaster = new WiFiBeaconBroadcaster(mCastGroupAddress, pharosIP,
                                                                      mCastPort, beacon, true);

                    break;
                default:
                    Logger.logErr("Could not initialize beacon - unknown experiment type: "
                                  + expType);
                    return false;
            }


			// Start receiving beacons
			wifiBeaconReceiver.start();
			return true;
		} catch (UnknownHostException e) {
			Logger.logErr("Problem initializing WiFi beacons: " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Sets the local system time.
	 * 
	 * @param time The parameter to the 'date' command
	 */
	private void setSystemTime(String time) {
		try {
			Runtime rt = Runtime.getRuntime();
			String cmd = "sudo date " + time;
			Process pr = rt.exec(cmd);
			int exitVal = pr.waitFor();
			if (exitVal == 0)
				Logger.log("Successfully set system time to " + time);
			else
				Logger.logErr("Failed to set system time to " + time + ", error code " + exitVal);
		} catch(Exception e) {
			Logger.logErr("Unable to set system time, error: " + e.toString());
		}
	}
	
	/**
	 * This is called whenever a message is received.
	 */
	@Override
	public void newMessage(Message msg) {
		Logger.log("Received message: " + msg);
		switch(msg.getType()) {
		case SET_TIME:
			setSystemTime(((SetTimeMsg)msg).getTime());
			break;
		case LOAD_SETTINGS:
			// Simply save the message.  This message will be used
			// when the start experiment message arrives.
			this.loadSettingsMsg = (LoadExpSettingsMsg)msg;  
			
			// Calculate the minimum marker distance, which is a percentage of the
			// specified marker distance.
			double minMarkerDist = loadSettingsMsg.getMarkerDist() * 0.80;
			Logger.log("Settings the minimum marker distance to be " + minMarkerDist);
			pathLocalizer.setMinMarkerDist(minMarkerDist);
			break;
		case RESET:
			reset();
			break;
		case STARTEXP:
			Logger.log("Starting experiment...");
			startExp((StartExpMsg)msg);
			break;
		case STOPEXP:
			Logger.log("Stopping experiment...");
			stopExp();
			break;
		default:
			Logger.log("Unknown Message: " + msg);
		}
	}
	
	private void reset() {
		// TODO
	}
	
	/**
	 * Starts the experiment.
	 * 
	 */
	private void startExp(StartExpMsg startExpMsg) {
		
		// Start the file logger and set it in the Logger.
		String fileName = startExpMsg.getExpName() + "-" + robotName + "-IndoorMRPatrol_" + FileLogger.getUniqueNameExtension() + ".log"; 
		FileLogger expFlogger = new FileLogger(fileName);
		Logger.setFileLogger(expFlogger);
		
		Logger.log("Starting experiment at time " + System.currentTimeMillis() + "...");
		
		// Start the individual components
//		if (compassDataBuffer != null)			compassDataBuffer.start();
		
		pathLocalizer.start();
        
        ContextHandler contextHandler = ContextHandler.getInstance();
        ExpType expType = startExpMsg.getExpType();

        Logger.log("Patrol type: " + expType);
        
        switch (expType) {
			case UNCOORDINATED:
			case LOOSELY:
                if (!initWiFiBeacons(startExpMsg.getExpType())) {
                    Logger.logErr("Beacon initialization failed, exiting!");
                    System.exit(1);
                }
				
                
                if (expType == ExpType.UNCOORDINATED)
                	new UncoordinatedPatrolDaemon(loadSettingsMsg, lineFollower, pathLocalizer, 
    						startExpMsg.getNumRounds(), wifiBeaconBroadcaster, wifiBeaconReceiver);
                else
                	new LooselyCoordinatedPatrolDaemon(loadSettingsMsg, lineFollower, pathLocalizer, 
                		startExpMsg.getNumRounds(), wifiBeaconBroadcaster, wifiBeaconReceiver);
				break;
			case LABELED:
            case BLOOMIER:
				
            	contextHandler.setLoggerDelegate(new PharosLoggingDelegate());
            	
                if (expType == ExpType.LABELED) {
                    contextHandler.setWireSummaryType(WireSummaryType.LABELED);
                } else {
                    contextHandler.setWireSummaryType(WireSummaryType.BLOOMIER);
                }

           
                if (!initWiFiBeacons(startExpMsg.getExpType())) {
                    Logger.logErr("Beacon initialization failed, exiting!");
                    System.exit(1);
                }

                new ContextCoordinatedPatrolDaemon(loadSettingsMsg, lineFollower, pathLocalizer,
                                                   startExpMsg.getNumRounds(),
                                                   wifiBeaconBroadcaster);

                break;
		}
		
	}
	
	/**
	 * Stops the experiment.
	 */
	private void stopExp() {
		Logger.log("Stopping the experiment.");
//		
//		if (motionArbiter != null) {
//			motionArbiter.setFileLogger(null);
//		}
		
		Logger.log("PharosServer: Stopping the WiFi beacon broadcaster.");
		if (wifiBeaconBroadcaster != null) {
//			wifiBeaconBroadcaster.setFileLogger(null);
			wifiBeaconBroadcaster.stop();
		}
		
//		Logger.log("PharosServer: Stopping the WiFi beacon receiver.");
//		if (wifiBeaconReceiver != null) {
//			wifiBeaconReceiver.setFileLogger(null);
//		}
		
//		Logger.log("PharosServer: Stopping the GPS data buffer.");
//		if (gpsDataBuffer != null)	{
////			gpsDataBuffer.setFileLogger(null);
////			gpsDataBuffer.stop();
//		}
		
//		Logger.log("PharosServer: Stopping the compass data buffer.");
//		if (compassDataBuffer != null) {
////			compassDataBuffer.setFileLogger(null);
//			compassDataBuffer.stop();
//		}
//		
//		Logger.log("PharosServer: Stopping the TelosB signal meter.");
//		if (telosRadioSignalMeter != null) {
////			telosRadioSignalMeter.setFileLogger(null);
//			telosRadioSignalMeter.stop();
//		}
		
		//flogger.log("PharosServer: Stopping the UDP tester.");
		//udpTest.stop();
		
		// Restore the debug file logger since the experiment has stopped.
		Logger.logDbg("Stopping experiment log file.");
		Logger.setFileLogger(debugFileLogger);
	}
	
	@Override
	public void newOpaqueData(ProteusOpaqueData opaqueData) {
		if (opaqueData.getDataCount() > 0) {
			String s = new String(opaqueData.getData());
			Logger.log("MCU Message: " + s);
		}
	}
	
	@Override
	public void beaconReceived(WiFiBeaconEvent be) {
		Logger.log("Received beacon: " + be);
	}
	
	private static void print(String msg) {
		System.out.println(msg);
	}
	
	private static void usage() {
		print("Usage: " + IndoorMRPatrolServer.class.getName() + " <options>\n");
		print("Where <options> include:");
		print("\t-playerServer <ip address>: The IP address of the Player Server (default localhost)");
		print("\t-playerPort <port number>: The Player Server's port number (default 6665)");
		print("\t-pharosPort <port number>: The Pharos Server's port number (default 7776)");
		print("\t-mobilityPlane <traxxas|segway|create>: The type of mobility plane being used (default traxxas)");
		print("\t-mCastAddress <ip address>: The Pharos Server's multicast group address (default 230.1.2.3)");
		print("\t-mCastPort <port number>: The Pharos Server's multicast port number (default 6000)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String playerIP = "localhost";
		int playerPort = 6665;
		int pharosPort = 7776;
		String mCastAddress = "230.1.2.3";
		int mCastPort = 6000;
		MotionArbiter.MotionType mobilityPlane = MotionArbiter.MotionType.MOTION_TRAXXAS;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-playerServer")) {
					playerIP = args[++i];
				} 
				else if (args[i].equals("-playerPort")) {
					playerPort = Integer.valueOf(args[++i]);
				}
				else if (args[i].equals("-mCastAddress")) {
					mCastAddress = args[++i];
				}
				else if (args[i].equals("-mCastPort")) {
					mCastPort = Integer.valueOf(args[++i]);
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
				else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				}
				else if (args[i].equals("-pharosPort")) {
					pharosPort = Integer.valueOf(args[++i]);
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
		
		print("Player Server IP: " + playerIP);
		print("Player Server port: " + playerPort);
		print("Pharos Server Port: " + pharosPort);
		print("Mobility plane: " + mobilityPlane);
		print("Multicast Address: " + mCastAddress);
		print("Multicast Port: " + mCastPort);
		print("Debug: " + ((System.getProperty ("PharosMiddleware.debug") != null) ? true : false));
		
		new IndoorMRPatrolServer(playerIP, playerPort, pharosPort, mCastAddress, mCastPort, mobilityPlane);
	}
}
