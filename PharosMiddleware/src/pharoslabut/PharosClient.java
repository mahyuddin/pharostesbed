package pharoslabut;

//import java.net.InetAddress;
//import java.net.UnknownHostException;
//import java.util.*;

import pharoslabut.io.*;
import pharoslabut.experiment.*;
import pharoslabut.beacon.*;
import pharoslabut.navigate.*;
//import pharoslabut.tasks.Priority;

/**
 * Connects to the PharosServer.  This is used by the application to perform application-specific tasks.
 * 
 * @author Chien-Liang Fok
 * @see PharosServer
 */
public class PharosClient implements BeaconListener {
	public static final boolean debug = (System.getProperty ("PharosMiddleware.debug") != null) ? true : false;
	
	private BeaconReceiver beaconReceiver;
	
    /**
	 * The multicast group address.  By default this is 230.1.2.3.
	 */
    private String mCastAddress = "230.1.2.3";
    
    /**
	 * The multicast port.  By default this is 6000.
	 */
    private int mCastPort = 6000;
    
    private TCPMessageSender sender;
    
    /**
     * The constructor.
     * 
     * @param mCastAddress
     * @param mCastPort
     */
	public PharosClient(String expConfigFileName, String mCastAddress, int mCastPort) {
		this.mCastAddress = mCastAddress;
		this.mCastPort = mCastPort;
		
		sender = new TCPMessageSender();
		
//		if (!initBeaconRcvr()) {
//			log("Unable to initialize beacon receiver!");
//			System.exit(1);
//		}
		
		doExp(expConfigFileName);
	}
	
	private void doExp(String expConfigFileName) {
		ExpConfig expConfig = ExpConfigReader.readExpConfig(expConfigFileName);
		try {
			// Send each of the robots their motion script.
			for (int i=0; i < expConfig.numRobots(); i++) {
				RobotExpSettings currRobot = expConfig.getRobot(i);
				log("Sending Motion script to robot " + currRobot.getName());
				
				GPSMotionScript script = GPSTraceReader.readTraceFile(currRobot.getMotionScript());
				GPSMotionScriptMsg gpsMsg = new GPSMotionScriptMsg(script);
				sender.sendMessage(currRobot.getIPAddress(), currRobot.getPort(), gpsMsg);
			}
			
			// Pause two seconds to ensure each robot receives their motion script.
			// This is to prevent out-of-order messages...
			synchronized(this) {
				wait(2000);
			}
			
			int delay = 0;
			// Send each robot the start experiment command.
			for (int i=0; i < expConfig.numRobots(); i++) {
				RobotExpSettings currRobot = expConfig.getRobot(i);
				
				StartExpMsg sem = new StartExpMsg(expConfig.getExpName(), currRobot.getName(), 
						ExpType.FOLLOW_GPS_MOTION_SCRIPT, delay);
				
				log("Sending start exp message to robot " + currRobot.getName() + "...");
				sender.sendMessage(currRobot.getIPAddress(), currRobot.getPort(), sem);
				
				// Update the delay between each robot.
				delay += expConfig.getStartInterval();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
//		
//		Vector<RobotSetting> robots = new Vector<RobotSetting>();
//		try {
//			String expName = "MM10-Exp7";
//			int startInterval = 1000; // in milliseconds
//			robots.add(new RobotSetting("Guiness", "10.11.12.20", 7776, "MM10/MotionScript2.txt"));
//			robots.add(new RobotSetting("Porterhouse", "10.11.12.19", 7776, "MM10/MotionScript2.txt"));
//			//robots.add(new RobotSetting("LiveOak", "10.11.12.26", 7776, "M12/MotionScripts/m12-gps-lollipop-1.5-4spause.txt"));
//			//robots.add(new RobotSetting("Manny", "10.11.12.13", 7776, "M12/MotionScripts/m12-gps-lollipop-1.5-4spause.txt"));
//			//robots.add(new RobotSetting("Czechvar", "10.11.12.14", 7776, "M12/MotionScripts/m12-gps-lollipop-1.5-4spause.txt"));
//			//robots.add(new RobotSetting("Guiness", "10.11.12.20", 7776, "M12/MotionScripts/m12-gps-lollipop-1.5-4spause.txt"));
//			//robots.add(new RobotSetting("Wynkoop", "10.11.12.25", 7776, "M12/MotionScripts/m12-gps-lollipop-1.5-4spause.txt"));
//			//robots.add(new RobotSetting("Mardesous", "10.11.12.24", 7776, "M12/MotionScripts/m12-gps-lollipop-1.5-4spause.txt"));
//			
//			doGPSWaypointExp(expName, robots, startInterval);
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
	}
	
//	private void doGPSWaypointExp(String expName, Vector<RobotSetting> robots, int startInterval) {
//		try {
//			for (int i=0; i < robots.size(); i++) {
//				RobotSetting currRobot = robots.get(i);
//				log("Sending Motion script to robot " + currRobot.robotName);
//				
//				GPSMotionScript script = GPSTraceReader.readTraceFile(currRobot.scriptFileName);
//				GPSMotionScriptMsg gpsMsg = new GPSMotionScriptMsg(script);
//				sender.sendMessage(currRobot.ipAddr, currRobot.port, gpsMsg);
//			}
//			
//			synchronized(this) {
//				wait(2000); // to prevent out-of-order messages...
//			}
//			
//			for (int i=0; i < robots.size(); i++) {
//				RobotSetting currRobot = robots.get(i);
//				log("Sending start exp message to robot " + currRobot.robotName + "...");
//				sender.sendMessage(currRobot.ipAddr, currRobot.port, new StartExpMsg(expName, currRobot.robotName, ExpType.FOLLOW_GPS_MOTION_SCRIPT));
//				
//				synchronized(this) {
//					wait(startInterval); // to prevent out-of-order messages...
//				}
//			}
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	private boolean initBeaconRcvr() {
		String pharosNI = BeaconReceiver.getPharosNetworkInterface();
		if (pharosNI != null) {
			beaconReceiver = new BeaconReceiver(mCastAddress, mCastPort, pharosNI);
			// Start  receiving beacons
			beaconReceiver.start();
			return true;
		} else {
			log("Problems getting Pharos network interface");
			return false;
		}
	}
	
	
	@Override
	public void beaconReceived(BeaconEvent be) {
		log("Received beacon: " + be);
	}
	
	private void log(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null) 
			System.out.println("PharosClient: " + msg);
	}
	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println("PharosClient: " + msg);
	}
	
	private static void usage() {
		print("Usage: pharoslabut.PharosClient <options>\n");
		print("Where <options> include:");
		print("\t-file <experiment configuration file name>: The name of the file containing the experiment configuration (required)");
		print("\t-mCastAddress <ip address>: The Pharos Server's multicast group address (default 230.1.2.3)");
		print("\t-mCastPort <port number>: The Pharos Server's multicast port number (default 6000)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String expConfigFileName = null;
		String mCastAddress = "230.1.2.3";
		int mCastPort = 6000;
		
		try {
			for (int i=0; i < args.length; i++) {
		
				if (args[i].equals("-file")) {
					expConfigFileName = args[++i];
				} else if (args[i].equals("-mCastAddress")) {
					mCastAddress = args[++i];
				}
				else if (args[i].equals("-mCastPort")) {
					mCastPort = Integer.valueOf(args[++i]);
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
		
		if (expConfigFileName == null) {
			usage();
			System.exit(1);
		}
		
		print("Exp Config: " + expConfigFileName);
		print("Multicast Address: " + mCastAddress);
		print("Multicast Port: " + mCastPort);
		print("Debug: " + ((System.getProperty ("PharosMiddleware.debug") != null) ? true : false));
		
		new PharosClient(expConfigFileName, mCastAddress, mCastPort);
	}
}
