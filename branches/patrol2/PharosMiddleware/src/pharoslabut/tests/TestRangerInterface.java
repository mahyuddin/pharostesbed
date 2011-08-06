package pharoslabut.tests;

import pharoslabut.logger.*;
import pharoslabut.sensors.*;
import playerclient3.*;
import playerclient3.structures.*;
import playerclient3.structures.ranger.PlayerRangerData;

/**
 * Tests the IR sensors.  Subscribes to the IR sensors and prints all of the IR
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
 *   provides ["opaque:0" "ranger:0"]
 *   port "/dev/ttyS0"
 *   safe 1
 * )
 * 
 * @author Chien-Liang Fok
 * @see http://pharos.ece.utexas.edu/wiki/index.php/Accessing_the_IR_sensor_plane
 */
public class TestRangerInterface implements RangerListener, ProteusOpaqueListener {
	private PlayerClient client = null;
	private FileLogger flogger = null;
	private RangerVisualizer irVisualizer;
	
	public TestRangerInterface(String serverIP, int serverPort, String logFileName) {
		try {
			client = new PlayerClient(serverIP, serverPort);
		} catch(PlayerException e) {
			log("Error connecting to Player: ");
			log("    [ " + e.toString() + " ]");
			System.exit (1);
		}
		
		if (logFileName != null)
			flogger = new FileLogger(logFileName);
		
		irVisualizer = new RangerVisualizer();
		irVisualizer.show();
		
		ProteusOpaqueInterface oi = (ProteusOpaqueInterface)client.requestInterfaceOpaque(0, PlayerConstants.PLAYER_OPEN_MODE);
		oi.addOpaqueListener(this);
		
		RangerInterface ri = client.requestInterfaceRanger(0, PlayerConstants.PLAYER_OPEN_MODE);
		RangerDataBuffer rangerBuffer = new RangerDataBuffer(ri);
		rangerBuffer.addRangeListener(this);
		rangerBuffer.start();
		
		log("Changing Player server mode to PUSH...");
		client.requestDataDeliveryMode(playerclient3.structures.PlayerConstants.PLAYER_DATAMODE_PUSH);
		
		log("Setting Player Client to run in continuous threaded mode...");
		client.runThreaded(-1, -1);
	}
	

	@Override
	public void newRangerData(PlayerRangerData data) {
		irVisualizer.updateDistances(data);
		double[] ranges = data.getRanges();
		String s = "Range data: ";
		for (int i=0; i < ranges.length; i++) {
			s += ranges[i];
			if (i < ranges.length-1) 
				s+= "\t";
		}
		log(s);
	}
	
	@Override
	public void newOpaqueData(ProteusOpaqueData opaqueData) {
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
		
		new TestRangerInterface(serverIP, serverPort, fileName);
	}
}
