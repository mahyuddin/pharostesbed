package pharoslabut.demo.mrpatrol2;

import pharoslabut.demo.mrpatrol2.config.ExpConfig;
import pharoslabut.demo.mrpatrol2.daemons.CoordinatedOutdoorPatrolDaemon;
import pharoslabut.demo.mrpatrol2.daemons.PatrolDaemon;
import pharoslabut.demo.mrpatrol2.daemons.UncoordinatedOutdoorPatrolDaemon;
import pharoslabut.demo.mrpatrol2.msgs.LoadExpSettingsMsg;
import pharoslabut.exceptions.PharosException;
import pharoslabut.io.Message;
import pharoslabut.io.MessageReceiver;
import pharoslabut.io.SetTimeMsg;
import pharoslabut.io.TCPMessageReceiver;
import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.MotionArbiter;

/**
 * This runs on each robot.  It handles the execution of the multi-robot patrol 2 (MRP2)
 * application.
 * 
 * @see pharoslabut.demo.mrpatrol2.ExpCoordinator
 * @author Chien-Liang Fok
 */
public class MRPatrol2Server implements MessageReceiver {
	
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
	 * This TCP port on which this server listens.
	 */
	private int serverPort;
	
    /**
	 * The WiFi multicast group address.
	 */
    private String mCastAddress;
    
    /**
	 * The WiFi multicast port.
	 */
    private int mCastPort;
	
	/**
	 * This is the file logger that is used for debugging purposes.  It is used when debug mode is enabled
	 * and there is no experiments running.
	 */
	private FileLogger debugFileLogger = null;
	
	/**
	 * The message that contains the experiment configuration settings.
	 */
	private ExpConfig expConfig;
	
	/**
	 * The patrol daemon used in this experiment.
	 */
	private PatrolDaemon patrolDaemon;
	
	/**
	 * The mobility plane used.
	 */
	private MotionArbiter.MotionType mobilityPlane;
	
	//private pharoslabut.wifi.UDPRxTx udpTest;
	
	/**
	 * The constructor.  Starts the server running.
	 * 
	 * @param playerServerIP The player server's IP address.
	 * @param playerServerPort The player server's port.
	 * @param serverPort This server's port.
	 * @param mCastAddress The multicast address over which to broadcast WiFi beacons.
	 * @param mCastPort the multicast port over which to broadcast WiFi beacons.
	 * @param mobilityPlane The type of mobility plane being used.
	 */
	public MRPatrol2Server(String playerServerIP, int playerServerPort, int serverPort, 
			String mCastAddress, int mCastPort, MotionArbiter.MotionType mobilityPlane) 
	{
		this.playerServerIP = playerServerIP;
		this.playerServerPort = playerServerPort;
		this.serverPort = serverPort;
		
		this.mCastAddress = mCastAddress;
		this.mCastPort = mCastPort;
		
		this.mobilityPlane = mobilityPlane;
		
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
		
		if (!initPatrolServer()) {
			Logger.logErr("Failed to initialize the Pharos server!");
			System.exit(1);
		}
	}
	
	/**
	 * Initializes this server.  This simply starts the TCPMessageReceiver.
	 * 
	 * @return true if successful.
	 */
	private boolean initPatrolServer() {
		new TCPMessageReceiver(this, serverPort);
		return true;
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
			this.expConfig = ((LoadExpSettingsMsg)msg).getExpConfig();  
			break;
		case RESET:
			reset();
			break;
		case STARTEXP:
			Logger.log("Starting experiment...");
			startExp();
			break;
		case STOPEXP:
			Logger.log("Stopping experiment...");
			stopExp();
			break;
		default:
			patrolDaemon.newMessage(msg);
		}
	}
	
	private void reset() {
		// TODO
	}
	
	/**
	 * Starts the experiment.
	 */
	private void startExp() {
		
		// Start the file logger...
		String fileName = expConfig.getExpName() + "-" + robotName + "-MRPatrol2_" + FileLogger.getUniqueNameExtension() + ".log"; 
		FileLogger expFlogger = new FileLogger(fileName);
		Logger.setFileLogger(expFlogger);
		
		Logger.log("Starting experiment at time " + System.currentTimeMillis() + "...");
        Logger.log("Patrol type: " + expConfig.getExpType());
        
        switch (expConfig.getExpType()) {
			case INDOOR:
				// to do...
				Logger.logDbg("Indoor experiments not implemented yet.");
				break;
			case OUTDOOR:
				startOutdoorExp();
				break;
			default:
				Logger.logErr("Unknown experiment type " + expConfig.getExpType());
				System.exit(1);
		}
		
	}
	
	/**
	 * Starts the appropriate outdoor MRPatrol 2 daemon.
	 */
	private void startOutdoorExp() {
		
        switch(expConfig.getCoordinationType()) {
        case NONE:
        	patrolDaemon = new UncoordinatedOutdoorPatrolDaemon(expConfig, mobilityPlane, 
        			playerServerIP, playerServerPort, 
        			serverPort, 
        			mCastAddress, mCastPort);
        	break;
        case PASSIVE:
        case ANTICIPATED_FIXED:
        	patrolDaemon = new CoordinatedOutdoorPatrolDaemon(expConfig, mobilityPlane, 
            		playerServerIP, playerServerPort, 
            		serverPort, 
            		mCastAddress, mCastPort, expConfig.getCoordinationStrength());
        	 	
        	break;
        case ANTICIPATED_VARIABLE:
        	break;
        default:
        	Logger.logErr("Unknown coordination type " + expConfig.getCoordinationType());
        	System.exit(1);
        }
        
        if (patrolDaemon != null) {
        	new Thread(patrolDaemon).start();
        }
	}
	
	/**
	 * Stops the experiment.
	 */
	private void stopExp() {
		Logger.log("Stopping the experiment.");
		
		Logger.log("Stopping the Patrol Daemon.");
		if (patrolDaemon != null)
			patrolDaemon.stop();
		
		// Restore the debug file logger since the experiment has stopped.
		Logger.logDbg("Stopping experiment log file.");
		Logger.setFileLogger(debugFileLogger);
	}
	
	private static void print(String msg) {
		System.out.println(msg);
	}
	
	private static void usage() {
		print("Usage: " + MRPatrol2Server.class.getName() + " <options>\n");
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
		
		new MRPatrol2Server(playerIP, playerPort, pharosPort, mCastAddress, mCastPort, mobilityPlane);
	}
}