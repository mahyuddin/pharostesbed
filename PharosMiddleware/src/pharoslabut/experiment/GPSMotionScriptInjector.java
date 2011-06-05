package pharoslabut.experiment;

import java.net.InetAddress;
//import java.net.UnknownHostException;
//import java.util.*;

import pharoslabut.io.*;
import pharoslabut.navigate.motionscript.MotionScript;
import pharoslabut.navigate.motionscript.MotionScriptReader;

/**
 * Sends a motion script to a PharosExpServer.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.experiment.PharosExpServer
 */
public class GPSMotionScriptInjector {
    /**
     * The connection to the PharosExpServer.
     */
    private TCPMessageSender sender = TCPMessageSender.getSender();
    
    /**
     * The constructor.
     * 
     * @param address The IP address of the robot.
     * @param port The port on which the robot is listening.
     * @param expName The name of the experiment.
     * @param scriptFileName The name of the file containing the motion script.
     */
	public GPSMotionScriptInjector(String address, int port, String expName, String scriptFileName) {
		try {
			InetAddress ipAddr = InetAddress.getByName(address);
			
			log("Reading the motion script...");
			MotionScript script = MotionScriptReader.readTraceFile(scriptFileName);
			
			log("Sending the motion script to server " + ipAddr + ":" + port + "...");
			MotionScriptMsg gpsMsg = new MotionScriptMsg(script);
			sender.sendMessage(ipAddr, port, gpsMsg);

			log("Pausing 2s to prevent out-of-order messages...");
			synchronized(this) {
				wait(2000);
			}
			
			log("Starting the GPS-based motion script following experiment...");
			sender.sendMessage(ipAddr, port, new StartExpMsg(expName, ExpType.FOLLOW_GPS_MOTION_SCRIPT));
		} catch(Exception e) {
			logErr("ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void logErr(String msg) {
		System.err.println("GPSMotionScriptInjector: " + msg);
	}
	
	private void log(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null) 
			System.out.println("GPSMotionScriptInjector: " + msg);
	}
	
	private static void print(String msg) {
		System.out.println(msg);
	}
	
	private static void usage() {
		print("Usage: pharoslabut.experiment.GPSMotionScriptInjector <options>\n");
		print("Where <options> include:");
		print("\t-server <ip address>: The Pharos experiment server's IP address (required)");
		print("\t-port <port number>: The Pharos experiment server's port (default 7776)");
		print("\t-script <script file name>: The name of the motion script file (required)");
		print("\t-exp <exp name>: The name of the experiment (required)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String serverAddress = null;
		int port = 7776;
		String scriptFileName = null;
		String expName = null;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-debug") || args[i].equals("-d"))
					System.setProperty ("PharosMiddleware.debug", "true");
				else if (args[i].equals("-server") || args[i].equals("-s"))
					serverAddress = args[++i];
				else if (args[i].equals("-port") || args[i].equals("-p"))
					port = Integer.valueOf(args[++i]);
				else if (args[i].equals("-script"))
					scriptFileName = args[++i];
				else if (args[i].equals("-exp"))
					expName = args[++i];
				else {
					System.err.println("Unknown parameter: " + args[i]);
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			usage();
			System.exit(1);
		}
		
		if (serverAddress == null) {
			System.err.println("Server IP address not specified...");
			usage();
			System.exit(1);
		}
		
		if (scriptFileName == null) {
			System.err.println("Motion script not specified...");
			usage();
			System.exit(1);
		}
		
		if (expName == null) {
			System.err.println("Experiment name not specified...");
			usage();
			System.exit(1);
		}
		
		print("Server Address: " + serverAddress);
		print("Server Port: " + port);
		print("Motion Script: " + scriptFileName);
		print("Exp Name: " + expName);
		print("Debug: " + ((System.getProperty ("PharosMiddleware.debug") != null) ? true : false));
		
		new GPSMotionScriptInjector(serverAddress, port, expName, scriptFileName);
	}
}
