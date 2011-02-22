package pharoslabut.demo.irobotcam;

import java.net.*;
import java.io.*;

import pharoslabut.logger.FileLogger;
import pharoslabut.io.*;

/**
 * This is the main class that launches the demo application.
 * 
 * @author Chien-Liang Fok
 */
public class DemoClient {
	
	private FileLogger flogger = null;
	private TCPMessageSender tcpSender;
	private CmdExec cmdExec;
	private ProgramEntryGUI gui;
	
	/**
	 * The Constructor.
	 * 
	 * @param serverIP The IP address of the demo server.
	 * @param serverPort The port of the demo server.
	 * @param fileName The log file name for recording execution state.
	 */
	public DemoClient(String serverIP, int serverPort, String fileName) {

		// Create the FileLogger...
		if (fileName != null) {
			flogger = new FileLogger(fileName);
			
		}
		
		// Create the connection to the server...
		try {
			tcpSender = new TCPMessageSender(InetAddress.getByName(serverIP), serverPort);
		} catch(IOException ioe) {
			log("Unable to connect to the server...");
			ioe.printStackTrace();
		}
		
		// Create the component that executes the commands of the user-provided program...
		cmdExec = new CmdExec(tcpSender, flogger);
		
		// Create a GUI for allowing users to interact with the system...
		gui = new ProgramEntryGUI(cmdExec, flogger);
	}
	
	private void log(String msg) {
		String result = "MotorStressTest: " + msg;
		System.out.println(result);
		if (flogger != null) {
			flogger.log(result);
		}
	}
	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(msg);
	}
	
	private static void usage() {
		System.setProperty ("PharosMiddleware.debug", "true");
		print("Usage: pharoslabut.demo.irobotcam.DemoClient <options>\n");
		print("Where <options> include:");
		print("\t-server <ip address>: The IP address of the Demo Server (default localhost)");
		print("\t-port <port number>: The Demo Server's port bnumber (default 8887)");
		print("\t-log <log file name>: name of file in which to save results (default DemoClient.log)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String logfileName = "DemoClient.log";
		String serverIP = "localhost";
		int serverPort = 8887;

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
				else if (args[i].equals("-log")) {
					logfileName = args[++i];
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
		
		System.out.println("Server IP: " + serverIP);
		System.out.println("Server port: " + serverPort);
		System.out.println("Log File: " + logfileName);
		
		new DemoClient(serverIP, serverPort, logfileName);
	}
}
