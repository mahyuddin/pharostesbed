package pharoslabut;


import playerclient.PlayerClient;
import playerclient.PlayerException;
import playerclient.BlobfinderInterface;
import playerclient.structures.blobfinder.*;
import playerclient.structures.PlayerConstants;
import javaclient2.Position2DInterface;

public class CMUcam
{
	private PlayerClient client = null;	
	
	private BlobfinderInterface bfi = null;
	PlayerBlobfinderData blob = null;
	PlayerBlobfinderBlob primaryBlob = null;
	PlayerBlobfinderBlob secondaryBlob = null;
	PlayerBlobfinderBlob[] blobList = null;
	playerclient.Position2DInterface p2di = null;
	
	double angle = 0.0;
	double velocity = 0.15;
	
	
	public CMUcam(String serverIP, int serverPort)
	{
		// connect to player server
		try{
			client = new PlayerClient(serverIP, serverPort);
		} catch (PlayerException e) { System.out.println("Error, could not connect to server."); System.exit(1); }
		
		// connect to blobfinder
		try{
			bfi = client.requestInterfaceBlobfinder(0, PlayerConstants.PLAYER_OPEN_MODE);
		} catch (PlayerException e) { System.out.println("Error, could not connect to blob finder proxy."); System.exit(1);}
	
		//set up pos. 2d proxy
		try{
			p2di = client.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		} catch (PlayerException e) { System.out.println("Error, could not connect to position 2d proxy."); System.exit(1);}
		
		p2di.setSpeed(0f,0f);
		
		
		//wait until you can connect to blobs
		//should be implemented as an event based model, not a polling
		while(blob == null)
		{
			blob = bfi.getData();
			pause(1000);
		}
	
		blobList = blob.getBlobs();
		
		boolean pausing = false;
		//boolean correct = false; //not using htis now, but probably will, along with inIntersection boolean
		boolean right = false, left = false;
		
		int secondBlob_cnt = 0;
		
		while(true)
		{
			blob = bfi.getData();
			blobList = blob.getBlobs();
			int numBlobs = blob.getBlobs_count();
			System.out.println("there are " + numBlobs + " blobs");
			
			if(numBlobs > 0) 
			{	
				
				if(blobList[0] != null) {
					primaryBlob = blobList[0];
					System.out.println("Black Blob has area: " + primaryBlob.getArea() + " and color of " + primaryBlob.getColor());
				}
				else System.out.println("Blob[0] null");
				
				if(numBlobs > 1) //right now only designed for detection of blue for secondary
				{
					if(blobList[1] != null) {
						secondaryBlob = blobList[1];
						System.out.println("Blue Blob has area: " + secondaryBlob.getArea() + " an ID of : " + secondaryBlob.getId() + " and color of " + secondaryBlob.getColor());
					}
					else System.out.println("Blob[1] null");
				}
				
				// main line follow process 
				if(primaryBlob != null){
					int primaryArea = primaryBlob.getArea();
					// in  next if statement, need to add logic for which side of
					// intersection robot is looking at
					if(primaryArea == 255){ // at black line
						velocity = 0.0;
					}
					else {
						velocity = .25;
					}
					
					// detect secondary blob
					// for now, pause for 3 seconds then resume
					if(secondaryBlob != null){ // && pausing == false){
						if(secondaryBlob.getArea() > 50) secondBlob_cnt++;
						//pausing = true;
						//p2di.setSpeed(0.0, 0.0);
						//pause(3000);
					}
					System.out.println("there are " + secondBlob_cnt + " second blobs");
					
					// find turn angle below
					// use blob.getWidth/2 to get a left half and right half of viewing area
					// divide by constant 4
					// with image area mapped to +/- 20 degree turn radius
					//		4 gives an appropriate angle to turn at with most speeds without over-compensation
					angle = (int)((blob.getWidth()/2) - primaryBlob.getX());
					System.out.println("raw angle is " + angle);
					
					angle /= 4; //maybe not enough?
					
					System.out.println("relative angle is " + angle);
					
					// logic to correct line given last known line position
					if(angle < -5.0) {
						right = true; left = false;
					}
					else if(angle > 5.0) {
						right = false; left = true;
					}
					else right = false; left = false;
					
					System.out.println("actual angle is " + (dtor(angle)));
					System.out.println();
				}
			} // end numBlobs > 0
			
			else {
				if(left == true) angle = 6.0;
				else if (right == true) angle = -6.0;
				//p2di.setSpeed(velocity, dtor(angle));
				if(left == true) System.out.println("correcting left");
				else if(right==true) System.out.println("correcting right");
			}
			
			// set robot speed and angle
			p2di.setSpeed(velocity, dtor(angle));
			pause(10);
		} // end while(true)
	}

	private double dtor(double degrees) {
		double radians = degrees * (Math.PI / 180);
		return radians;
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