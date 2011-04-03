package pharoslabut.tests;

import playerclient.*;
import playerclient.structures.blobfinder.*;
import playerclient.structures.*;

/**
 * A small test of the blob finder. Connects to the blobfinder device, registers itself as a listener,
 * then prints all of the blob data received.
 * 
 * @author Chien-Liang Fok
 */
public class TestBlobFinder implements BlobfinderListener {
	private PlayerClient client = null;	
	
	private BlobfinderInterface bfi = null;
	private PlayerBlobfinderData blobData = null;
	private long blobDataTimestamp;
		
	public TestBlobFinder(String serverIP, int serverPort) {
		// connect to player server
		try {
			client = new PlayerClient(serverIP, serverPort);
		} catch (PlayerException e) { System.out.println("Error, could not connect to server."); System.exit(1); }
		
		// connect to blobfinder
		try {
			bfi = client.requestInterfaceBlobfinder(0, PlayerConstants.PLAYER_OPEN_MODE);
		} catch (PlayerException e) { System.out.println("Error, could not connect to blob finder proxy."); System.exit(1);}	
		
		bfi.addListener(this);
		
		long prevTimeStamp = -1;
		
		while(true) {
			synchronized(this) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
					
			if (blobData != null) {
				double age = (System.currentTimeMillis() - blobDataTimestamp);
				log("New blob data (age = " + age + "ms): " + blobData.toString());
			}
		} // end while(true)
	}
	
	/**
	 * This is called whenever new blob data is available.
	 * 
	 * @param blobData The new blob data available.
	 * @param timestamp The timestamp of the data.  This can be compared to System.currentTimeMillis() to determine the age of the blob data.
	 */
	public void newPlayerBlobfinderData(PlayerBlobfinderData blobData, long timestamp) {
		synchronized(this) {
			this.blobData = blobData;
			this.blobDataTimestamp = timestamp;
			notifyAll();
		}
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