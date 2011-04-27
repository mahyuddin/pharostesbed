package pharoslabut.tests;

import pharoslabut.logger.*;
import pharoslabut.sensors.*;
import playerclient.*;
import playerclient.structures.*;
import playerclient.structures.ir.PlayerIrData;
import playerclient.structures.opaque.PlayerOpaqueData;

/**
 * Tests the IR interface.  Subscribes to the IR interface and prints all of the IR
 * range data received.  There are six IR sensors and they are printed in the following
 * order: FL, FC, FR, RL, RC, RR.
 * 
 * When running this test, be sure the player server is using a configuration with the
 * "ir" and "opaque" interfaces.  For example:
 * 
 * <pre>
 * driver (
 *   name "proteus"
 *   plugin "/usr/local/share/player/modules/libproteusdriver.so"
 *   provides ["opaque:0" "ir:0"]
 *   port "/dev/ttyS0"
 *   safe 1
 * )
 * 
 * @author Chien-Liang Fok
 */
public class TestIRInterface implements IRListener, OpaqueListener {
	private PlayerClient client = null;
	private FileLogger flogger = null;
	private IRVisualizer irVisualizer;
	
	public TestIRInterface(String serverIP, int serverPort, String logFileName) {
		try {
			client = new PlayerClient(serverIP, serverPort);
		} catch(PlayerException e) {
			log("Error connecting to Player: ");
			log("    [ " + e.toString() + " ]");
			System.exit (1);
		}
		
		if (logFileName != null)
			flogger = new FileLogger(logFileName);
		
		irVisualizer = new IRVisualizer();
		irVisualizer.show();
		
		OpaqueInterface oi = client.requestInterfaceOpaque(0, PlayerConstants.PLAYER_OPEN_MODE);
		oi.addOpaqueListener(this);
		
		IRInterface ir = client.requestInterfaceIR(0, PlayerConstants.PLAYER_OPEN_MODE);
		ir.addIRListener(this);
	}
	

	@Override
	public void newPlayerIRData(PlayerIrData data) {
		//System.out.println("Opaque data: " + opaqueData);
		irVisualizer.updateDistances(data);
		float[] ranges = data.getRanges();
		String s = "";
		for (int i=0; i < ranges.length; i++) {
			s += ranges[i];
			if (i < ranges.length-1) 
				s+= "\t";
		}
		log(s);
	}
	
	@Override
	public void newOpaqueData(PlayerOpaqueData opaqueData) {
		String s = new String(opaqueData.getData());
		log(s);
	}
	
	private void log(String msg) {
		log(msg, flogger);
	}
	
	private static void log(String msg, FileLogger flogger) {
		System.out.println(msg);
		if (flogger != null) 
			flogger.log(msg);
	}
	
	private static void usage() {
		System.err.println("Usage: pharoslabut.tests.TestIRInterface <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
		System.err.println("\t-file <file name>: name of file in which to save results (default null)");
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
		
		log("Server IP: " + serverIP, null);
		log("Server port: " + serverPort, null);
		log("File: " + fileName, null);
		
		new TestIRInterface(serverIP, serverPort, fileName);
	}
}
