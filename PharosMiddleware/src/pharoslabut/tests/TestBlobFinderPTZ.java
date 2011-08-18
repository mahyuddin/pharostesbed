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
//	private FileLogger flogger = null;
	private PlayerClient client = null;	
//	private BlobfinderInterface bfi = null;
//	private BlobFinderVisualizer visualizer;
		
	/**
	 * The constructor.
	 * 
	 * @param serverIP The IP address of the robot.
	 * @param serverPort The port on which the robot is listening.
	 */
	public TestBlobFinderPTZ(String serverIP, int serverPort) {

		
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
		
		/*javax.swing.SwingUtilities.invokeLater(new Runnable() {
           		 public void run() {
            			visualizer = new BlobFinderVisualizer(flogger);
           		 }
      		  }

	); */
		
		Logger.log("Changing Player server mode to PUSH...");
		client.requestDataDeliveryMode(playerclient3.structures.PlayerConstants.PLAYER_DATAMODE_PUSH);
		
		Logger.log("Setting Player Client to run in continuous threaded mode...");
		client.runThreaded(-1, -1);
		
		if (ptz != null) {
			Logger.log("Doing quick test of PTZ...");
			// Do a quick test of PTZ
			PlayerPtzCmd ptzCmd = new PlayerPtzCmd();

			//Initialize angles
		
			ptzCmd.setPan((float) 0);
			ptzCmd.setTilt((float) 0);
			ptz.setPTZ(ptzCmd);
			pause(1000);

			//*************these test values will not work if offsets for LineFollower are implemented*****//
//******** see http://pharos.ece.utexas.edu/wiki/index.php/How_to_Make_a_Proteus_Robot_Follow_a_Line_Using_a_CMUCam#Determine_Offset_of_Hardware_and_Change_Configuration_File

		//Begin test	
			Logger.log("Testing Pan....");			
			for (float pan = (float)-70; pan < 82; pan += 10) {
				Logger.log("Current Settings\nPan= "+ ptzCmd.getPan() +" PanSpeed= "+ ptzCmd.getPanspeed() +"\nSetting pan to "+ pan +"...\n");
				ptzCmd.setPan((float) pan);
				ptz.setPTZ(ptzCmd);
				pause(1000);
			}


			ptzCmd.setPan((float) 0);
			ptz.setPTZ(ptzCmd);
			pause(3000);
		
			Logger.log("Testing Tilt...");		
			for (float tilt = (float)-40; tilt < 82; tilt += 10) {
				Logger.log("Current Settings\nTilt= "+ ptzCmd.getTilt() +" TiltSpeed= "+ ptzCmd.getTiltspeed()+ "\nSetting tilt to "+ tilt +"...\n");
				ptz.setPTZ(ptzCmd);
				pause(1000);
			} 

			ptzCmd.setTilt((float) 0);
			ptz.setPTZ(ptzCmd);
			pause(3000);
			Logger.log("Test Complete");


		}
		
/*		boolean done = false;
		while(!done) {
			if (visualizer != null && !visualizer.isVisible())
				done = true;
			if (!done && bfi.isDataReady()) {
				PlayerBlobfinderData blobData = bfi.getData();
				if (blobData != null) {
					if (visualizer != null)
						visualizer.visualizeBlobs(blobData);
				}
			}
			pause(100);
		} // end while(true)
*/		System.exit(0);


	}
	
//	private void log(String msg) {
//		System.out.println(msg);
//		if (flogger != null) 
//			flogger.log(msg);
//	}

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
		System.err.println("Usage: " + TestBlobFinderPTZ.class.getName() + " <options>\n");
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
					Logger.setFileLogger(new FileLogger(args[++i], false));
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
		
		new TestBlobFinderPTZ(serverIP, serverPort);
	}
}
