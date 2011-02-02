package pharoslabut;

import java.net.InetAddress;
//import java.net.UnknownHostException;
//import java.util.*;

import pharoslabut.io.*;
//import pharoslabut.tasks.*;
//import pharoslabut.beacon.*;
import pharoslabut.navigate.*;
//import pharoslabut.tasks.Priority;

/**
 * Connects to the PharosServer.  This is used by the application to perform application-specific tasks.
 * 
 * @author Chien-Liang Fok
 * @see PharosServer
 */
public class GPSMotionScriptInjector {
	public static final boolean debug = (System.getProperty ("PharosMiddleware.debug") != null) ? true : false;
    
    /**
     * The connection to the Pharos Server.
     */
    private TCPMessageSender sender;
    
    /**
     * The constructor.
     * 
     * @param mCastAddress
     * @param mCastPort
     */
	public GPSMotionScriptInjector(String address, int port, String mCastAddress, int mCastPort,
			String expName, String robotName, String scriptFileName) {
		
		try {

			InetAddress ipAddr = InetAddress.getByName(address);

			sender = new TCPMessageSender();

			log("Reading the motion script...");
			GPSMotionScript script = GPSTraceReader.readTraceFile(scriptFileName);
			
			log("Sending the motion script to server " + ipAddr + ":" + port + "...");
			GPSMotionScriptMsg gpsMsg = new GPSMotionScriptMsg(script);
			sender.sendMessage(ipAddr, port, gpsMsg);

			
			synchronized(this) {
				wait(2000); // to prevent out-of-order messages...
			}
			
			log("Sending the experiment start message to server " + ipAddr + ":" + port + "...");
			sender.sendMessage(ipAddr, port, new StartExpMsg(expName, robotName, ExpType.FOLLOW_GPS_MOTION_SCRIPT));

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void log(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null) 
			System.out.println("GPSMotionScriptInjector: " + msg);
	}
	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println("GPSMotionScriptInjector: " + msg);
	}
	
	private static void usage() {
		print("Usage: pharoslabut.PharosServer <options>\n");
		print("Where <options> include:");
		print("\t-server <ip address>: The Pharos Server's IP address (default 10.11.12.20)");
		print("\t-port <port number>: The Pharos Server's port (default 230.1.2.3)");
		print("\t-mCastAddress <ip address>: The Pharos Server's multicast group address (default 230.1.2.3)");
		print("\t-mCastPort <port number>: The Pharos Server's multicast port number (default 6000)");
		print("\t-script <script file name>: The name of the motion script file (default MotionScript.txt)");
		print("\t-exp <exp name>: The name of the experiment (default Exp)");
		print("\t-robot <robot name>: The name of the robot (default John Doe)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String mCastAddress = "230.1.2.3";
		String serverAddress = "10.11.12.20";
		int mCastPort = 6000;
		int port = 7776;
		String scriptFileName = "MotionScript.txt";
		String expName = "Exp";
		String robotName = "RobotJohnDoe";
		
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
				else if (args[i].equals("-server") || args[i].equals("-s")) {
					serverAddress = args[++i];
				}
				else if (args[i].equals("-port") || args[i].equals("-p")) {
					port = Integer.valueOf(args[++i]);
				}
				else if (args[i].equals("-script")) {
					scriptFileName = args[++i];
				}
				else if (args[i].equals("-exp")) {
					expName = args[++i];
				}
				else if (args[i].equals("-robot")) {
					robotName = args[++i];
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
		
		print("Server Address: " + serverAddress);
		print("Server Port: " + port);
		print("Multicast Address: " + mCastAddress);
		print("Multicast Port: " + mCastPort);
		print("Motion Script: " + scriptFileName);
		print("Exp Name: " + expName);
		print("Robot Name: " + robotName);
		print("Debug: " + ((System.getProperty ("PharosMiddleware.debug") != null) ? true : false));
		
		new GPSMotionScriptInjector(serverAddress, port, mCastAddress, mCastPort, 
				expName, robotName, scriptFileName);
	}
}
