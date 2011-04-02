package pharoslabut.tests;

import playerclient.*;
import playerclient.structures.blobfinder.*;
import playerclient.structures.*;

public class TestBlobFinder {
	private PlayerClient client = null;	
	
	private BlobfinderInterface bfi = null;
//	PlayerBlobfinderData blob = null;
//	PlayerBlobfinderBlob primaryBlob = null;
//	PlayerBlobfinderBlob secondaryBlob = null;
//	PlayerBlobfinderBlob[] blobList = null;
		
	public TestBlobFinder(String serverIP, int serverPort) {
		// connect to player server
		try {
			client = new PlayerClient(serverIP, serverPort);
		} catch (PlayerException e) { System.out.println("Error, could not connect to server."); System.exit(1); }
		
		// connect to blobfinder
		try {
			bfi = client.requestInterfaceBlobfinder(0, PlayerConstants.PLAYER_OPEN_MODE);
		} catch (PlayerException e) { System.out.println("Error, could not connect to blob finder proxy."); System.exit(1);}	
		
		while(true) {
			PlayerBlobfinderData blob = bfi.getData();
			
			if (blob != null)
				log(blob.toString());
			
			pause(100);
		} // end while(true)
	}
	
	private void log(String msg) {
		System.out.println(msg);
	}
//
//	private double dtor(double degrees) {
//		double radians = degrees * (Math.PI / 180);
//		return radians;
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
	
	public static void main(String[] args)
	{
		String serverIP = "localhost";
		int serverPort = 6665;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-server")) {
					serverIP = args[++i];
				} 
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
 
		System.out.println("Server IP: " + serverIP);
		System.out.println("Server port: " + serverPort);
		
		new TestBlobFinder(serverIP, serverPort);
	}
}