package pharoslabut.tests;

import pharoslabut.logger.*;
//import pharoslabut.sensors.BlobFinderVisualizer;

import playerclient3.*;
//import playerclient3.structures.blobfinder.*;
import playerclient3.structures.ptz.PlayerPtzCmd;
import playerclient3.structures.*;

/**
 * A small test of the PtzInterface. Connects to the PtzInterface, and it pan 
 * and tilt.
 * 
 * @author Chien-Liang Fok
 */
public class TestBlobFinderPTZ {
	
	public static final float PAN_INCREMENT = (float)0.025;
	public static final float TILT_INCREMENT = (float)0.05;
	public static final long PAUSE_INTERVAL = 1;
	
	private PlayerClient client = null;	
		
	/**
	 * The constructor.
	 * 
	 * @param serverIP The IP address of the robot.
	 * @param serverPort The port on which the robot is listening.
	 * @param reset Whether to reset the pan and tilt to their zero positions.
	 */
	public TestBlobFinderPTZ(String serverIP, int serverPort, boolean reset) {

		
		// connect to player server
		try {
			client = new PlayerClient(serverIP, serverPort);
		} catch (PlayerException e) { System.out.println("Error, could not connect to server."); System.exit(1); }
		
		/*// connect to blobfinder
		try {
			bfi = client.requestInterfaceBlobfinder(0, PlayerConstants.PLAYER_OPEN_MODE);
		} catch (PlayerException e) { System.out.println("Error, could not connect to blob finder proxy."); System.exit(1);}	
		*/
		// connect to PTZ
		PtzInterface ptz = null;
		try {
			ptz = client.requestInterfacePtz(0, PlayerConstants.PLAYER_OPEN_MODE);
		} catch (PlayerException e) { System.out.println("Error, could not connect to PTZ proxy."); System.exit(1);}	
		
		Logger.log("Changing Player server mode to PUSH...");
		client.requestDataDeliveryMode(playerclient3.structures.PlayerConstants.PLAYER_DATAMODE_PUSH);
		
		Logger.log("Setting Player Client to run in continuous threaded mode...");
		client.runThreaded(-1, -1);
		
		if (ptz != null) {
			PlayerPtzCmd ptzCmd = new PlayerPtzCmd();
			ptzCmd.setPan((float) 0);
			ptzCmd.setTilt((float) 0);
			
			if (reset) {
				Logger.log("Resetting position of the camera.");
				ptz.setPTZ(ptzCmd);
				pause(1000);
			} else {
				Logger.log("Doing quick test of PTZ...");
				// Do a quick test of PTZ
				ptz.setPTZ(ptzCmd);
				pause(1000);

				//*************these test values will not work if offsets for LineFollower are implemented*****//
				//******** see http://pharos.ece.utexas.edu/wiki/index.php/How_to_Make_a_Proteus_Robot_Follow_a_Line_Using_a_CMUCam#Determine_Offset_of_Hardware_and_Change_Configuration_File

				//Begin test	
				Logger.log("Testing Pan....");	
				for(float pan = (float)0; pan > -70; pan -= PAN_INCREMENT) {
					ptzCmd.setPan((float)pan);
					Logger.log("Setting pan to " + pan);
					ptz.setPTZ(ptzCmd);
					pause(PAUSE_INTERVAL);
				}
				for (float pan = (float)-70; pan < 82; pan += PAN_INCREMENT) {
					ptzCmd.setPan((float)pan);
					Logger.log("Setting pan to " + pan);
					ptz.setPTZ(ptzCmd);
					pause(PAUSE_INTERVAL);
				}
				for(float pan = (float)82; pan > 0; pan -= PAN_INCREMENT) {
					ptzCmd.setPan((float)pan);
					Logger.log("Setting pan to " + pan);
					ptz.setPTZ(ptzCmd);
					pause(PAUSE_INTERVAL);
				}


				ptzCmd.setPan((float) 0);
				ptz.setPTZ(ptzCmd);
				pause(1000);

				Logger.log("Testing Tilt...");	
				for (float tilt = (float)0; tilt > -40; tilt -= TILT_INCREMENT) {
					ptzCmd.setTilt((float)tilt);
					Logger.log("Setting tilt to " + tilt);
					ptz.setPTZ(ptzCmd);
					pause(PAUSE_INTERVAL);
				}
				for (float tilt = (float)-40; tilt < 82; tilt += TILT_INCREMENT) {
					ptzCmd.setTilt((float)tilt);
					Logger.log("Setting tilt to " + tilt);
					ptz.setPTZ(ptzCmd);
					pause(PAUSE_INTERVAL);
				} 
				for (float tilt = (float)82; tilt > 0; tilt -= TILT_INCREMENT) {
					ptzCmd.setTilt((float)tilt);
					Logger.log("Setting tilt to " + tilt);
					ptz.setPTZ(ptzCmd);
					pause(PAUSE_INTERVAL);
				}

				ptzCmd.setTilt((float) 0);
				ptz.setPTZ(ptzCmd);
				pause(3000);
				Logger.log("Test Complete");

			}
		}
		
		System.exit(0);
	}

	private void pause(long duration) {
		synchronized(this) {
			try {
				wait(duration);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void usage() {
		System.err.println("Usage: " + TestBlobFinderPTZ.class.getName() + " <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
		System.err.println("\t-log <file name>: name of file in which to save results (default null)");
		System.err.println("\t-reset: Reset the pan and tilt of the camera to zero");
	}
	
	public static void main(String[] args) {
		String serverIP = "localhost";
		int serverPort = 6665;
		boolean reset = false;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-server")) {
					serverIP = args[++i];
				} 
				else if (args[i].equals("-port")) {
					serverPort = Integer.valueOf(args[++i]);
				}
				else if (args[i].equals("-log")) {
					Logger.setFileLogger(new FileLogger(args[++i], false));
				} 
				else if (args[i].equals("-reset")) {
					reset = true;
				}
				else if (args[i].equals("-h")) {
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
		
		new TestBlobFinderPTZ(serverIP, serverPort, reset);
	}
}
