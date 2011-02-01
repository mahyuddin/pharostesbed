package pharoslabut;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import pharoslabut.io.*;
//import pharoslabut.tasks.*;
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
	public PharosClient(String mCastAddress, int mCastPort) {
		this.mCastAddress = mCastAddress;
		this.mCastPort = mCastPort;
		
		sender = new TCPMessageSender();
		
//		if (!initBeaconRcvr()) {
//			log("Unable to initialize beacon receiver!");
//			System.exit(1);
//		}
		
		doM12Exp();
	}
	
	private void doM12Exp() {
		Vector<RobotSetting> robots = new Vector<RobotSetting>();
		try {
			String expName = "M12-Exp14";
			int startInterval = 30000; // in milliseconds
			robots.add(new RobotSetting("Shiner", "10.11.12.17", 7776, "M12/MotionScripts/m12-gps-lollipop-1.5-4spause.txt"));
			//robots.add(new RobotSetting("LiveOak", "10.11.12.26", 7776, "M12/MotionScripts/m12-gps-lollipop-1.5-4spause.txt"));
			//robots.add(new RobotSetting("Manny", "10.11.12.13", 7776, "M12/MotionScripts/m12-gps-lollipop-1.5-4spause.txt"));
			//robots.add(new RobotSetting("Czechvar", "10.11.12.14", 7776, "M12/MotionScripts/m12-gps-lollipop-1.5-4spause.txt"));
			//robots.add(new RobotSetting("Guiness", "10.11.12.20", 7776, "M12/MotionScripts/m12-gps-lollipop-1.5-4spause.txt"));
			//robots.add(new RobotSetting("Wynkoop", "10.11.12.25", 7776, "M12/MotionScripts/m12-gps-lollipop-1.5-4spause.txt"));
			//robots.add(new RobotSetting("Mardesous", "10.11.12.24", 7776, "M12/MotionScripts/m12-gps-lollipop-1.5-4spause.txt"));
			
			doGPSWaypointExp(expName, robots, startInterval);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	class RobotSetting {
		String robotName;
		String scriptFileName;
		InetAddress ipAddr;
		int port;
		
		public RobotSetting(String robotName, String ipAddr, int port, String scriptFileName) throws UnknownHostException {
			this.robotName = robotName;
			this.scriptFileName = scriptFileName;
			this.ipAddr = InetAddress.getByName(ipAddr);
			this.port = port;
		}
		
	}
	
	private void doGPSWaypointExp(String expName, Vector<RobotSetting> robots, int startInterval) {
		try {
			for (int i=0; i < robots.size(); i++) {
				RobotSetting currRobot = robots.get(i);
				log("Sending Motion script to robot " + currRobot.robotName);
				
				GPSMotionScript script = GPSTraceReader.readTraceFile(currRobot.scriptFileName);
				GPSMotionScriptMsg gpsMsg = new GPSMotionScriptMsg(script);
				sender.sendMessage(currRobot.ipAddr, currRobot.port, gpsMsg);
			}
			
			synchronized(this) {
				wait(2000); // to prevent out-of-order messages...
			}
			
			for (int i=0; i < robots.size(); i++) {
				RobotSetting currRobot = robots.get(i);
				log("Sending start exp message to robot " + currRobot.robotName + "...");
				sender.sendMessage(currRobot.ipAddr, currRobot.port, new StartExpMsg(expName, currRobot.robotName, ExpType.FOLLOW_GPS_MOTION_SCRIPT));
				
				synchronized(this) {
					wait(startInterval); // to prevent out-of-order messages...
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
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
		print("Usage: pharoslabut.PharosServer <options>\n");
		print("Where <options> include:");
		print("\t-mCastAddress <ip address>: The Pharos Server's multicast group address (default 230.1.2.3)");
		print("\t-mCastPort <port number>: The Pharos Server's multicast port number (default 6000)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String mCastAddress = "230.1.2.3";
		int mCastPort = 6000;
		
		try {
			for (int i=0; i < args.length; i++) {
		
				if (args[i].equals("-mCastAddress")) {
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
		
		print("Multicast Address: " + mCastAddress);
		print("Multicast Port: " + mCastPort);
		print("Debug: " + ((System.getProperty ("PharosMiddleware.debug") != null) ? true : false));
		
		new PharosClient(mCastAddress, mCastPort);
	}
}
