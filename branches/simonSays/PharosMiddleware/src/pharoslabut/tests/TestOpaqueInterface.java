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
	private FileLogger flogger;
	private ProteusOpaqueInterface poi;
	
	/**
	 * The constructor.
	 * 
	 * @param serverIP The IP address of the player server
	 * @param serverPort The port on which the player server is listening.
	 * @param logFileName The name of the file to record log messages, may be null.
	 */
	public TestOpaqueInterface(String serverIP, int serverPort, String logFileName) {
		
		if (logFileName != null)
			flogger = new FileLogger(logFileName);
		
		log("Connecting to the player server " + serverIP + ":" + serverPort + "...");
		try {
			client = new PlayerClient(serverIP, serverPort);
		} catch(PlayerException e) {
			log("Error connecting to Player server: ");
			log("    [ " + e.toString() + " ]");
			System.exit (1);
		}
		
		log("Changing Player server mode to PUSH...");
		client.requestDataDeliveryMode(playerclient3.structures.PlayerConstants.PLAYER_DATAMODE_PUSH);
		
		log("Setting Player Client to run in continuous threaded mode...");
		client.runThreaded(-1, -1);
		
		log("Connecting to ProteusOpaqueInterface...");
		poi = (ProteusOpaqueInterface)client.requestInterfaceOpaque(0, PlayerConstants.PLAYER_OPEN_MODE);
		
		log("Registering self as listener to Opaque data...");
		poi.addOpaqueListener(this);
	}
	
	@Override
	public void newOpaqueData(ProteusOpaqueData opaqueData) {
		String s = new String(opaqueData.getData());
		log(s);
	}
	
	private void log(String msg) {
		String result = "TestOpaqueInterface: " + msg;
		System.out.println(result);
		if (flogger != null) {
			flogger.log(result);
		}
	}
	
	private static void usage() {
		System.err.println("Usage: pharoslabut.tests.TestOpaqueInterface <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
		System.err.println("\t-log <file name>: name of file in which to save results (default null)");
	}
	
	public static void main(String[] args) {
		String fileName = null;
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
					fileName = args[++i];
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
		System.out.println("Log: " + fileName);
		
		new TestOpaqueInterface(serverIP, serverPort, fileName);
	}
}
