package pharoslabut.tests;

import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.LineFollower;
import pharoslabut.sensors.PathLocalizerOverheadMarkers;
import pharoslabut.sensors.PathLocalizerOverheadMarkersListener;
import pharoslabut.sensors.Position2DBuffer;
import pharoslabut.sensors.RangerDataBuffer;
import playerclient3.PlayerClient;
import playerclient3.PlayerException;
import playerclient3.Position2DInterface;
import playerclient3.RangerInterface;
import playerclient3.structures.PlayerConstants;

/**
 * Tests how fast a robot can stop after detecting the overhead marker.
 * 
 * @author Chien-Liang Fok
 */
public class TestOverheadMarkerReactionTime implements PathLocalizerOverheadMarkersListener {

	private LineFollower lineFollower;
	
	/**
	 * The constructor.
	 * 
	 * @param client The connection to the player server.
	 */
	public TestOverheadMarkerReactionTime(PlayerClient client) {

		// Subscribe to the ranger proxy.
		RangerInterface ri = client.requestInterfaceRanger(0, PlayerConstants.PLAYER_OPEN_MODE);
		RangerDataBuffer rangerBuffer = new RangerDataBuffer(ri);
		rangerBuffer.start();
		Logger.log("Subscribed to the ranger interface.");
		
		// Subscribe to Posistion2D proxy.
		Position2DInterface p2di = null;
		try {
			p2di = client.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		} catch (PlayerException e) { Logger.logErr("Could not connect to position 2d proxy."); System.exit(1);}
		Position2DBuffer pos2DBuffer = new Position2DBuffer(p2di);
		pos2DBuffer.start();
		Logger.logDbg("Created Position2dProxy.");
		
		// Start the PathLocalizerOverheadMarkers 
		PathLocalizerOverheadMarkers markerDetector = new PathLocalizerOverheadMarkers(rangerBuffer, pos2DBuffer, false /* no GUI */);
		Logger.log("Created the PathLocalizerOverheadMarkers.");
		markerDetector.addListener(this);
		markerDetector.start();
		
		// Start the robot following the line
		lineFollower = new LineFollower(client);
		lineFollower.start();
		Logger.log("Started the line follower.");
		
		
		client.requestDataDeliveryMode(playerclient3.structures.PlayerConstants.PLAYER_DATAMODE_PUSH);
		Logger.logDbg("Changed Player server mode to PUSH...");
		
		
		client.runThreaded(-1, -1);
		Logger.logDbg("Configured Player client to run in continuous threaded mode...");
	}
	
	@Override
	public void markerEvent(int numMarkers) {
		Logger.log("Detected marker!");
		Logger.log("Stopping the line follower.");
		lineFollower.stop();
		Logger.log("Line follower stopped, exiting system.");
		System.exit(0);
	}
	
	
	private static void print(String msg) {
		System.out.println(msg);
	}
	
	private static void usage() {
		System.setProperty ("PharosMiddleware.debug", "true");
		print("Usage: " + TestOverheadMarkerReactionTime.class.getName() + " <options>\n");
		print("Where <options> include:");
		print("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		print("\t-port <port number>: The Player Server's port number (default 6665)");
		print("\t-log <log file name>: The name of the file in which to save debug output (default null)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String serverIP = "localhost";
		int serverPort = 6665;
		boolean doLineFollow = true;
		
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
				} else if (args[i].equals("-noLineFollow"))  {
					doLineFollow = false;
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
		Logger.log("Created player client.");
		
		new TestOverheadMarkerReactionTime(client);
	}
}
