package pharoslabut.tests;

import pharoslabut.logger.*;
import pharoslabut.sensors.BlobFinderVisualizer;

import playerclient3.*;
import playerclient3.structures.blobfinder.*;
import playerclient3.structures.*;

/**
 * A small test of the BlobFinder service. Connects to the BlobFinderInterface, and periodically
 * polls it for data.  It prints all of the blob data received to the screen.
 * 
 * @author Chien-Liang Fok
 */
public class TestBlobFinder {
	private FileLogger flogger = null;
	private PlayerClient client = null;	
	private BlobfinderInterface bfi = null;
	private BlobFinderVisualizer visualizer;
		
	/**
	 * The constructor.
	 * 
	 * @param serverIP The IP address of the robot.
	 * @param serverPort The port on which the robot is listening.
	 * @param logFileName The log file in which to save results.
	 */
	public TestBlobFinder(String serverIP, int serverPort, String logFileName) {
		
		if (logFileName != null)
			flogger = new FileLogger(logFileName);
		
		// connect to player server
		try {
			client = new PlayerClient(serverIP, serverPort);
		} catch (PlayerException e) { System.out.println("Error, could not connect to server."); System.exit(1); }
		
		// connect to blobfinder
		try {
			bfi = client.requestInterfaceBlobfinder(0, PlayerConstants.PLAYER_OPEN_MODE);
		} catch (PlayerException e) { System.out.println("Error, could not connect to blob finder proxy."); System.exit(1);}	
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	visualizer = new BlobFinderVisualizer(flogger);
            	//visualizer.show();
            }
        });
		
		log("Changing Player server mode to PUSH...");
		client.requestDataDeliveryMode(playerclient3.structures.PlayerConstants.PLAYER_DATAMODE_PUSH);
		
		log("Setting Player Client to run in continuous threaded mode...");
		client.runThreaded(-1, -1);
		
		while(true) {
			if (bfi.isDataReady()) {
				PlayerBlobfinderData blobData = bfi.getData();
				if (blobData != null) {
//					log(blobData.toString());
					if (visualizer != null)
						visualizer.visualizeBlobs(blobData);
				}
			}
			pause(100);
		} // end while(true)
	}
	
	private void log(String msg) {
		System.out.println(msg);
		if (flogger != null) 
			flogger.log(msg);
	}

	private void pause(int duration) {
		synchronized(this) {
			try {
				wait(duration);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void usage() {
		System.err.println("Usage: pharoslabut.tests.TestBlobFinder <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
		System.err.println("\t-log <file name>: name of file in which to save results (default null)");
	}
	
	public static void main(String[] args) {
		String logFileName = null;
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
					logFileName = args[++i];
				} else if (args[i].equals("-h")) {
					usage();
					System.exit(0);
				} else {
					System.err.println("Unknown option: " + args[i]);
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
 
		System.out.println("Server IP: " + serverIP);
		System.out.println("Server port: " + serverPort);
		System.out.println("Log: " + logFileName);
		
		new TestBlobFinder(serverIP, serverPort, logFileName);
	}
}