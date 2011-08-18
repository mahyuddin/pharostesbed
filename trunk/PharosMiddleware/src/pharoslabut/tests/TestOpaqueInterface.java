package pharoslabut.tests;

import pharoslabut.logger.*;
import pharoslabut.sensors.*;

import playerclient3.*;
import playerclient3.structures.*;

/**
 * Subscribes to the OpaqueInterface to determine whether it works.
 * 
 * @author Chien-Liang Fok
 */
public class TestOpaqueInterface implements ProteusOpaqueListener {
	
	private PlayerClient client = null;
//	private FileLogger flogger;
	private ProteusOpaqueInterface poi;
	
	/**
	 * The constructor.
	 * 
	 * @param serverIP The IP address of the player server
	 * @param serverPort The port on which the player server is listening.
	 */
	public TestOpaqueInterface(String serverIP, int serverPort) {
		
		Logger.log("Connecting to the player server " + serverIP + ":" + serverPort + "...");
		try {
			client = new PlayerClient(serverIP, serverPort);
		} catch(PlayerException e) {
			Logger.logErr("Error connecting to Player server: ");
			Logger.logErr("    [ " + e.toString() + " ]");
			System.exit (1);
		}
		
		Logger.log("Changing Player server mode to PUSH...");
		client.requestDataDeliveryMode(playerclient3.structures.PlayerConstants.PLAYER_DATAMODE_PUSH);
		
		Logger.log("Setting Player Client to run in continuous threaded mode...");
		client.runThreaded(-1, -1);
		
		Logger.log("Connecting to ProteusOpaqueInterface...");
		poi = (ProteusOpaqueInterface)client.requestInterfaceOpaque(0, PlayerConstants.PLAYER_OPEN_MODE);
		
		Logger.log("Registering self as listener to Opaque data...");
		poi.addOpaqueListener(this);
	}
	
	@Override
	public void newOpaqueData(ProteusOpaqueData opaqueData) {
		String s = new String(opaqueData.getData());
		Logger.log(s);
	}
	
//	private void log(String msg) {
//		String result = "TestOpaqueInterface: " + msg;
//		System.out.println(result);
//		if (flogger != null) {
//			flogger.log(result);
//		}
//	}
	
	private static void usage() {
		System.err.println("Usage: " + TestOpaqueInterface.class.getName() + " <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
		System.err.println("\t-log <file name>: name of file in which to save results (default null)");
	}
	
	public static void main(String[] args) {
		String serverIP = "localhost";
		int serverPort = 6665;

		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-server")) {
					serverIP = args[++i];
				} 
				else if (args[i].equals("-port")) {
					serverPort = Integer.valueOf(args[++i]);
				} 
				else if (args[i].equals("-log")) {
					Logger.setFileLogger(new FileLogger(args[++i])); 
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
		
		new TestOpaqueInterface(serverIP, serverPort);
	}
}
