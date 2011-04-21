package pharoslabut.demo.autoIntersection;

import pharoslabut.logger.FileLogger;
import pharoslabut.navigate.LineFollower;

/**
 * The top-level class of the autonomous intersection
 * demo client.
 * 
 * @author Chien-Liang Fok
 */
public class ClientManager {

	private FileLogger flogger;
	private LineFollower lf;
	private RemoteIntersectionManager rim;
	
	public ClientManager(String serverIP, int port, String playerIP, int playerPort, FileLogger flogger) {
		
		this.flogger = flogger;
		
		lf = new LineFollower(playerIP, playerPort, flogger);
		rim = new RemoteIntersectionManager(lf, serverIP, port, flogger);
		
		// Start the line follower.  This starts the robot moving following the line.
		lf.start();
	}
	
	/**
	 * Logs a debug message.  This message is only printed when debug mode is enabled.
	 * 
	 * @param msg The message to log.
	 */
	private void log(String msg) {
		log(msg, true);
	}
	
	/**
	 * Logs a message.
	 * 
	 * @param msg  The message to log.
	 * @param isDebugMsg Whether the message is a debug message.
	 */
	private void log(String msg, boolean isDebugMsg) {
		String result = "ClientManager: " + msg;
		if (!isDebugMsg || System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(msg);
	}
	
	private static void usage() {
		System.setProperty ("PharosMiddleware.debug", "true");
		print("Usage: pharoslabut.demo.autoIntersection.ClientManager <options>\n");
		print("Where <options> include:");
		print("\t-server <ip address>: The IP address of the intersection server (required)");
		print("\t-port <port number>: The port on which the intersection server is listening (required)");
		print("\t-playerServer <ip address>: The IP address of the Player Server (default localhost)");
		print("\t-playerPort <port number>: The Player Server's port number (default 6665)");
		print("\t-log <log file name>: The name of the file in which to save debug output (default null)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String serverIP = null;
		int serverPort = -1;
		
		String playerServerIP = "localhost";
		int playerServerPort = 6665;
		FileLogger flogger = null;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-server")) {
					serverIP = args[++i];
				} else if (args[i].equals("-port")) {
					serverPort = Integer.valueOf(args[++i]);
				} else if (args[i].equals("-playerServer")) {
					playerServerIP = args[++i];
				} else if (args[i].equals("-playerPort")) {
					playerServerPort = Integer.valueOf(args[++i]);
				} else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				} else if (args[i].equals("-log")) {
					flogger = new FileLogger(args[++i]);
				} else {
					print("Unknown argument " + args[i]);
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
 
		if (serverIP == null || serverPort == -1) {
			System.setProperty ("PharosMiddleware.debug", "true");
			print("Must specify intersection server's IP and port.");
			usage();
			System.exit(1);
		}
		
		print("Server IP: " + serverIP);
		print("Server port: " + serverPort);
		
		new ClientManager(serverIP, serverPort, playerServerIP, playerServerPort, flogger);
	}
}
