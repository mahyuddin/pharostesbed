package pharoslabut.tests;

import playerclient2.*;
import playerclient2.structures.*;
import playerclient2.structures.opaque.PlayerOpaqueData;

/**
 * Subscribes to the OpaqueInterface to determine whether it works.
 * 
 * @author Chien-Liang Fok
 */
public class TestOpaqueInterface implements OpaqueListener {
	
	private PlayerClient client = null;
	
	public TestOpaqueInterface(String serverIP, int serverPort, String logFileName) {
		try {
			client = new PlayerClient(serverIP, serverPort);
		} catch(PlayerException e) {
			log("Error connecting to Player: ");
			log("    [ " + e.toString() + " ]");
			System.exit (1);
		}
		
		OpaqueInterface oi = client.requestInterfaceOpaque(0, PlayerConstants.PLAYER_OPEN_MODE);
		oi.addOpaqueListener(this);
	}
	
	@Override
	public void newOpaqueData(PlayerOpaqueData opaqueData) {
		//System.out.println("Opaque data: " + opaqueData);
		String s = new String(opaqueData.getData());
		log(s);
	}
	
	private static void log(String msg) {
		System.out.println(msg);
	}
	
	private static void usage() {
		System.err.println("Usage: pharoslabut.tests.TestOpaqueInterface <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
		System.err.println("\t-file <file name>: name of file in which to save results (default log.txt)");
	}
	
	public static void main(String[] args) {
		String fileName = "log.txt";
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
				else if (args[i].equals("-file")) {
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
		
		log("Server IP: " + serverIP);
		log("Server port: " + serverPort);
		log("File: " + fileName);
		
		new TestOpaqueInterface(serverIP, serverPort, fileName);
	}
}
