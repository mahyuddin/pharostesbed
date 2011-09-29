package pharoslabut.tests;

import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.LineFollower;
import pharoslabut.sensors.PathLocalizerOverheadMarkers;
import pharoslabut.sensors.RangerDataBuffer;
import playerclient3.PlayerClient;
import playerclient3.PlayerException;
import playerclient3.RangerInterface;
import playerclient3.structures.PlayerConstants;

/**
 * Tests the ability to use overhead markers to localize a robot along a path.
 * 
 * @author Chien-Liang Fok
 */
public class TestPathLocalizerOverheadMarkers {

	private static void print(String msg) {
		System.out.println(msg);
	}
	
	private static void usage() {
		System.setProperty ("PharosMiddleware.debug", "true");
		print("Usage: " + TestPathLocalizerOverheadMarkers.class.getName() + " <options>\n");
		print("Where <options> include:");
		print("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		print("\t-port <port number>: The Player Server's port number (default 6665)");
		print("\t-log <log file name>: The name of the file in which to save debug output (default null)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String serverIP = "localhost";
		int serverPort = 6665;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-server")) {
					serverIP = args[++i];
				} else if (args[i].equals("-port")) {
					serverPort = Integer.valueOf(args[++i]);
				} else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				} else if (args[i].equals("-log")) {
					Logger.setFileLogger(new FileLogger(args[++i]));
				} else if (args[i].equals("-h")) {
					usage();
					System.exit(0);
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
 
		print("Server IP: " + serverIP);
		print("Server port: " + serverPort);
		
		PlayerClient client = null;
		
		// Connect to the player server.
		try {
			client = new PlayerClient(serverIP, serverPort);
		} catch (PlayerException e) { Logger.logErr("Could not connect to server."); System.exit(1); }
		Logger.log("Created robot client.");
		
		// Subscribe to the ranger interface.
		RangerInterface ri = client.requestInterfaceRanger(0, PlayerConstants.PLAYER_OPEN_MODE);
		RangerDataBuffer rangerBuffer = new RangerDataBuffer(ri);
		rangerBuffer.start();
		Logger.log("Subscribed to the ranger interface.");
		
		// Start the PathLocalizerOverheadMarkers
		PathLocalizerOverheadMarkers pathLocalizer = new PathLocalizerOverheadMarkers(rangerBuffer);
		Logger.log("Created the PathLocalizerOverheadMarkers.");
		
		// Start the robot following the line
		LineFollower lf = new LineFollower(client);
		lf.start();
		Logger.log("Started the line follower.");
		
		client.requestDataDeliveryMode(playerclient3.structures.PlayerConstants.PLAYER_DATAMODE_PUSH);
		Logger.logDbg("Changed Player server mode to PUSH...");
		
		
		client.runThreaded(-1, -1);
		Logger.logDbg("Configured Player client to run in continuous threaded mode...");
	}
}
