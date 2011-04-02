package pharoslabut;

//import java.net.InetAddress;
//import java.net.UnknownHostException;
//import java.util.*;

import pharoslabut.io.*;
import pharoslabut.experiment.*;
import pharoslabut.beacon.*;
//import pharoslabut.navigate.*;
//import pharoslabut.tasks.Priority;
import pharoslabut.navigate.motionscript.MotionScript;
import pharoslabut.navigate.motionscript.MotionScriptReader;

/**
 * Connects to the PharosServer.  This is used by the application to perform application-specific tasks.
 * 
 * @author Chien-Liang Fok
 * @see PharosServer
 */
public class PharosClient implements WiFiBeaconListener {
	private WiFiBeaconReceiver beaconReceiver;
	
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
     * @param expConfigFileName The name of the file containing the experiment configuration.
     * @param mCastAddress The multicast address over which to broadcast 802.11 beacons
     * @param mCastPort The multicast port on which to broadcast 802.11 beacons
     * @see pharoslabut.experiment.ExpConfig
     * @see pharoslabut.navigate.motionscript.MotionScriptReader
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
				
				MotionScript script = MotionScriptReader.readTraceFile(currRobot.getMotionScript());
				MotionScriptMsg gpsMsg = new MotionScriptMsg(script);
				sender.sendMessage(currRobot.getIPAddress(), currRobot.getPort(), gpsMsg);
			}
			
			// Pause two seconds to ensure each robot receives their motion script.
			// This is to prevent out-of-order messages...
			int startTime = 5;
			log("Starting experiment in " + startTime + "...");
			while (startTime-- > 0) {
				synchronized(this) { 
					try {
						wait(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (startTime > 0) log(startTime + "...");
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
	}
	
	private boolean initBeaconRcvr() {
		String pharosNI = WiFiBeaconReceiver.getPharosNetworkInterface();
		if (pharosNI != null) {
			beaconReceiver = new WiFiBeaconReceiver(mCastAddress, mCastPort, pharosNI);
			// Start  receiving beacons
			beaconReceiver.start();
			return true;
		} else {
			log("Problems getting Pharos network interface");
			return false;
		}
	}
	
	
	@Override
	public void beaconReceived(WiFiBeaconEvent be) {
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
					System.setProperty ("PharosMiddleware.debug", "true");
					print("Unknown parameter: " + args[i]);
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
