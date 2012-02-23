package pharoslabut.tests;

import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.LineFollower2;
import pharoslabut.sensors.BlobDataProvider;
import playerclient3.BlobfinderInterface;
import playerclient3.PlayerClient;
import playerclient3.PlayerException;
import playerclient3.Position2DInterface;
import playerclient3.PtzInterface;
import playerclient3.structures.PlayerConstants;

/**
 * Tests the LineFollower service.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.navigate.LineFollower2
 */
public class TestLineFollower2 {

	private static void print(String msg) {
			System.out.println(msg);
	}
	
	private static void usage() {
		System.setProperty ("PharosMiddleware.debug", "true");
		print("Usage: " + TestLineFollower2.class.getName() + " <options>\n");
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
		
		// connect to blobfinder
		BlobfinderInterface bfi = null;
		try {
			bfi = client.requestInterfaceBlobfinder(0, PlayerConstants.PLAYER_OPEN_MODE);
		} catch (PlayerException e) { Logger.logErr("Could not connect to blob finder proxy."); System.exit(1);}
		Logger.log("Created BlobFinder.");
		
		// Connect to PTZ
		PtzInterface ptz = null;
		try {
			ptz = client.requestInterfacePtz(0, PlayerConstants.PLAYER_OPEN_MODE);
		} catch (PlayerException e) { Logger.logErr("Could not connect to PTZ proxy."); System.exit(1);}
		Logger.logDbg("Connected to PTZ proxy.");
		
		//set up pos. 2d proxy
		Position2DInterface p2di = null;
		try {
			p2di = client.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		} catch (PlayerException e) { Logger.logErr("Could not connect to position 2d proxy."); System.exit(1);}
		Logger.logDbg("Created Position2dProxy.");
		
		Logger.logDbg("Changing Player server mode to PUSH...");
		client.requestDataDeliveryMode(playerclient3.structures.PlayerConstants.PLAYER_DATAMODE_PUSH);
		
		Logger.logDbg("Setting Player Client to run in continuous threaded mode...");
		client.runThreaded(-1, -1);	
		
		BlobDataProvider bdp = new BlobDataProvider(bfi);
		LineFollower2 lf = new LineFollower2(bdp, p2di, ptz);
		
		lf.start();
	}
}
