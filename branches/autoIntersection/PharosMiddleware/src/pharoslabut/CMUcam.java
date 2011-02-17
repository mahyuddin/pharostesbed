package pharoslabut;

import playerclient.PlayerClient;
import playerclient.PlayerException;
import playerclient.BlobfinderInterface;
import playerclient.structures.blobfinder.*;
import playerclient.structures.PlayerConstants;

public class CMUcam
{
	private PlayerClient client = null;	
	private BlobfinderInterface bfi = null;
	PlayerBlobfinderData blob = null;
	PlayerBlobfinderBlob theBlob = null;
	PlayerBlobfinderBlob[] blobList = null;
	
	public CMUcam(String serverIP, int serverPort)
	{
		// connect to player server
		try{
			client = new PlayerClient(serverIP, serverPort);
		} catch (PlayerException e) { 
			System.out.println("Error, could not connect to server."); System.exit(1);
		}
		// connect to blobfinder
		try{
			bfi = client.requestInterfaceBlobfinder(0, PlayerConstants.PLAYER_OPEN_MODE);
		} catch (PlayerException e) {
			System.out.println("Error, could not connect to blob finder proxy."); System.exit(1);
		}
	
		while(blob == null)
		{
			blob = bfi.getData();
			pause(1000);
		}
		
		blobList = blob.getBlobs();
		theBlob = blobList[0];
		
		while(true)
		{
			blob = bfi.getData();
			blobList = blob.getBlobs();
			theBlob = blobList[0];
			System.out.println("Blob has area: " + theBlob.getArea() + " an ID of : " + theBlob.getId());
			pause(100);
		}
		
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
		
		new CMUcam(serverIP, serverPort);
	}
}