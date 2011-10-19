package pharoslabut.demo.simonsays;

import java.net.*;
import java.io.*;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.junit.Assert;

import pharoslabut.demo.simonsays.io.PlayerControlCmd;
import pharoslabut.demo.simonsays.io.PlayerControlMsg;
import pharoslabut.io.AckableMessage;
import pharoslabut.io.Message.MsgType;
import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
//import pharoslabut.io.*;

/**
 * This is the main class that launches client side of the demo application.
 * Be sure the DemoServer is running prior to launching this class.
 * 
 * @author Chien-Liang Fok
 * @author Kevin Boos
 * @see SimonSaysServer
 */
public class SimonSaysClient {
	
//	private TCPMessageSender tcpSender;
	private CmdExec cmdExec;
	public static boolean testing = false;
	public static BeaconDataCollector bdc = new BeaconDataCollector("beaconData.txt");
	
	/**
	 * The Constructor.
	 * 
	 * @param serverIP The IP address of the demo server.
	 * @param serverPort The port of the demo server.
//	 * @param connect Whether to connect to the server.  This is used for debugging purposes.
	 * @param logFileName The log file name for recording execution state.
	 */
	public SimonSaysClient(String serverIP, int serverPort, String logFileName) {

		// Create the FileLogger if the file name is defined...
		if (logFileName != null)
			Logger.setFileLogger(new FileLogger(logFileName));
		
		try {
//			if (connect) {
//				// Create the connection to the server...
//				tcpSender = TCPMessageSender.getSender();
//			}
			
			// initialize the Trilateration process
//			new Multilateration(0, 0).start(); // robot's starting coordinates (x,y)
			
			// Create the component that executes the commands of the user-provided program...
			cmdExec = new CmdExec(InetAddress.getByName(serverIP), serverPort);
			
			// send an initial PlayerControlMsg with the START cmd, this adds this client to the server's client list
			cmdExec.sendMsg(new PlayerControlMsg(PlayerControlCmd.START));			
			
			// Create a GUI for allowing users to interact with the system...
			// See: http://download.oracle.com/javase/6/docs/api/javax/swing/package-summary.html#threading
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					new ProgramEntryGUI(cmdExec).show();
				}
			});
		} catch(IOException ioe) {
			String msg = "Unable to connect to robot " + serverIP + ":" + serverPort + ",\nEnsure the DemoServer is running."; 
			JOptionPane.showMessageDialog(null, msg);
			Logger.logErr(msg);
			ioe.printStackTrace();
			System.exit(1);
		}
	}
	
//	private void log(String msg) {
//		String result = "DemoClient: " + msg;
//		System.out.println(result);
//		if (flogger != null) {
//			flogger.log(result);
//		}
//	}
	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(msg);
	}
	
	private static void usage() {
		System.setProperty ("PharosMiddleware.debug", "true");
		print("Usage: " + SimonSaysClient.class.getName() + " <options>\n");
		print("Where <options> include:");
		print("\t-server <ip address>: The IP address of the Demo Server (default localhost)");
		print("\t-port <port number>: The Demo Server's port bnumber (default 8887)");
		print("\t-log <log file name>: name of file in which to save results (default DemoClient.log)");
//		print("\t-noconnect: do not server (useful for debugging)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String logfileName = "DemoClient.log";
		String serverIP = "localhost";
		int serverPort = 8887;
//		boolean connect = true;

		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				}
				else if (args[i].equals("-server")) {
					serverIP = args[++i];
				}
				else if (args[i].equals("-port")) {
					serverPort = Integer.valueOf(args[++i]);
				}
//				else if (args[i].equals("-noconnect")) {
//					connect = false;
//				}
				else if (args[i].equals("-log")) {
					logfileName = args[++i];
				}
				else if (args[i].equals("-testing")) {
					testing = true;
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
		
		
		print("Server IP: " + serverIP);
		print("Server port: " + serverPort);
		print("Log File: " + logfileName);
		
		new SimonSaysClient(serverIP, serverPort, logfileName);
	}
}
