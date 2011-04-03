package pharoslabut.navigate;

import playerclient.*;
import playerclient.structures.blobfinder.*;
import playerclient.structures.*;
import pharoslabut.logger.*;

/**
 * Follows a line using a CMUcam2.
 * 
 * @author Seth Gee
 * @author Chien-Liang Fok
 */
public class LineFollower implements BlobfinderListener, Runnable {
	
	/**
	 * This is the maximum valid age of the blob data.  Anything older than that is discarded.
	 */
	public static final long BLOB_MAX_VALID_AGE = 1000;
	
	/**
	 * The maximum speed of the robot in meters per second.
	 */
	public static final double MAX_SPEED = 0.5;
	
	/**
	 * The minimum speed of the robot in meters per second.
	 */
	public static final double MIN_SPEED = 0.3;
	
	/**
	 * The maximum turn angle of the robot in degrees.
	 */
	public static final double MAX_TURN_ANGLE = 30;
	
	/**
	 * The maximum amount of time in milliseconds that the robot may spend in the intersection.
	 */
	public static final long MAX_INTERSECTION_TIME = 6000;
	
	/**
	 * The minimum amount of time in milliseconds that the robot may spend in the intersection.
	 */
	public static final long MIN_INTERSECTION_TIME = 3000;
	
	/**
	 * The minimum amount of time in milliseconds between exiting the previous intersection
	 * and entering the next one.
	 */
	public static final long MIN_INTER_INTERSECTION_TIME = 2000;
	
	/**
	 * The speed and angle settings while the robot crosses the intersection.
	 */
	public static final double INTERSECTION_SPEED = 0.35;
	public static final double INTERSECTION_ANGLE = 0;
	
	/**
	 * The specifications of the intersection marker, which is a thick line perpendicular to the 
	 * lane.
	 */
	public static final int INTERSECTION_MARKER_AREA = 85;
	public static final int INTERSECTION_MARKER_WIDTH = 100;
	
	private PlayerClient client = null;	
	private BlobfinderInterface bfi = null;
	
	/**
	 * This is the latest blob finder data received and the time at which it was received.
	 */
	private PlayerBlobfinderData blobFinderData = null;
	private long blobFinderDataTimestamp;
	
	private boolean done = false;
	private boolean inIntersection = false;
	private long intersectionStartTime;
	private long intersectionEndTime = 0;
	
	private Position2DInterface p2di = null;
	
	double angle = 0;
	double speed = 0;
	
	private FileLogger flogger = null;
	
	private Thread thread;
	
	/**
	 * The constructor.
	 * 
	 * @param serverIP The IP address of the server.
	 * @param serverPort The port of the server.
	 * @param flogger The file logger for recording debug data.
	 */
	public LineFollower(String serverIP, int serverPort, FileLogger flogger) {
		this.flogger = flogger;
		
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
		
		bfi.addListener(this);
		p2di.setSpeed(0f,0f);  // ensure robot is initially stopped
	}
	
	/**
	 * Starts the robot following the line.
	 */
	public void start() {
		if (thread == null) {
			done = false;
			thread = new Thread(this);
			thread.start();
		} else
			log("start: ERROR: Thread already started.");
	}
	
	/**
	 * Stops the robot.
	 */
	public void stop() {
		if (thread != null) {
			done = true;
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			thread = null;
		} else
			log("Stop: ERROR: already stopped.");
	}
	
	/**
	 * Adjusts the heading and speed of the robot based on the position of the blob.
	 * 
	 * @param blob The blob to use to calculate the heading of the robot.
	 */
	private void adjustHeadingAndSpeed(PlayerBlobfinderBlob blob) {
		log("adjustHeadingAndSpeed: Blob area=" + blob.getArea() + ", color=" + blob.getColor() + ", left=" + blob.getLeft() + ", right=" + blob.getRight());
		
		long interIntersectionTime = System.currentTimeMillis() - intersectionEndTime;
		
		// This is for detecting when the robot is entering the intersection.
		// Since there is a big line marking the entrance to the intersection,
		// we can detect it if the blobs area is > INTERSECTION_MARKER_AREA and the width of the blob
		// is greater than INTERSECTION_MARKER_WIDTH.
		if (interIntersectionTime > MIN_INTER_INTERSECTION_TIME 
				&& blob.getArea() > INTERSECTION_MARKER_AREA 
				&& blob.getRight() - blob.getLeft() > INTERSECTION_MARKER_WIDTH) 
		{
			log("adjustHeadingAndSpeed: Entering intersection...");
			inIntersection = true;
			intersectionStartTime = System.currentTimeMillis();
			angle = INTERSECTION_ANGLE;
			speed = INTERSECTION_SPEED;
		} else {
			int midPoint = blobFinderData.getWidth()/2;
			
			int turnSign;		
			if (blob.getX() > midPoint) {
				log("adjustHeadingAndSpeed: Center of blob is left of midpoint, must turn right!");
				turnSign = -1;
			} else {
				log("adjustHeadingAndSpeed: Center of blob is right of midpoint, must turn left!");
				turnSign = 1;
			}
			
			// Make the angle adjustment proportional to the degree to which the heading is off.
			double divergence = Math.abs(blob.getX() - midPoint);
			double divergencePct = divergence / midPoint;
			log("adjustHeadingAndSpeed: divergencePct = " + divergencePct);
			angle = turnSign * MAX_TURN_ANGLE * divergencePct;
		
			// Make the speed proportional to the degree to which the heading is off. 
			speed = MAX_SPEED * (1 - divergencePct);
			if (speed < MIN_SPEED)
				speed = MIN_SPEED;
		}
	}
	
	private void handleSecondaryBlob(PlayerBlobfinderBlob blob) {
		// detect secondary blob
		// for now, pause for 3 seconds then resume
		
//			if((blob.getArea()) > 150 && blobDetect == false) {
//				blobDetect = true; 
//			}
//			else if(blob.getArea() < 100) blobDetect = false;
	}
	
	private void doIntersection() {
		long timeInIntersection = System.currentTimeMillis() - intersectionStartTime;
		if (timeInIntersection > MAX_INTERSECTION_TIME) {
			log("doIntersection: ERROR: Spent too much time in intersection, stopping robot");
			speed = angle = 0;
		} 
		else {
			// Check to make sure we have valid data...
			if (blobFinderData != null) {
				
				// Check to ensure data is not too old...
				long blobAge = System.currentTimeMillis() - blobFinderDataTimestamp;
				if (blobAge < BLOB_MAX_VALID_AGE) {
					
					int numBlobs = blobFinderData.getBlobs_count();
					log("doIntersection: There are " + numBlobs + " blobs...");

					if(numBlobs > 0) {	
						PlayerBlobfinderBlob[] blobList = blobFinderData.getBlobs();

						if(blobList != null && blobList[0] != null) {
							PlayerBlobfinderBlob blob = blobList[0];
							
							log("doIntersection: Blob area=" + blob.getArea() + ", color=" + blob.getColor() + ", left=" + blob.getLeft() + ", right=" + blob.getRight());
							
							if (timeInIntersection < MIN_INTERSECTION_TIME) {
								log("doIntersection: Haven't spent the minimum amount of time in intersection, continuing to move forward (timeInIntersection=" + timeInIntersection + ")...");
							}
							// The end of the intersection may be detected when
							// the blob area is > INTERSECTION_MARKER_AREA and the width is > INTERSECTION_MARKER_WIDTH
							else if (//blob.getArea() > INTERSECTION_MARKER_AREA && 
									blob.getRight() - blob.getLeft() > INTERSECTION_MARKER_WIDTH) 
							{
								log("doIntersection: End of intersection detected, continuing to follow line...");
								inIntersection = false;
								intersectionEndTime = System.currentTimeMillis();
							}
						} else {
							log("doIntersection: No primary blob, assuming still in intersection, continuing to move forward (timeInIntersection=" + timeInIntersection + ")...");
						}

						// While in intersection, ignore secondary blob
//						if(numBlobs > 1 && blobList[1] != null) {
//							handleSecondaryBlob(blobList[1]);
//						} else log("No secondary blob!");
					} else {
						log("doIntersection: No blobs present, assuming still in intersection, continuing to move forward (timeInIntersection=" + timeInIntersection + ")...");
					}

				} else {
					log("doIntersection: Blob too old (" + blobAge + "ms), assuming still in intersection, continuing to move forward (timeInIntersection=" + timeInIntersection + ")...");
				}
			} else {
				log("doIntersection: No blob data, assuming still in intersection, continuing to move forward (timeInIntersection=" + timeInIntersection + ")...");
			}
		}
	}
	
	private void doLineFollow() {
		// Check to make sure we have valid data...
		if (blobFinderData != null) {
			
			// Check to ensure data is not too old...
			long blobAge = System.currentTimeMillis() - blobFinderDataTimestamp;
			if (blobAge < BLOB_MAX_VALID_AGE) {
				int numBlobs = blobFinderData.getBlobs_count();
				log("doLineFollow: There are " + numBlobs + " blobs...");

				if(numBlobs > 0) {	
					PlayerBlobfinderBlob[] blobList = blobFinderData.getBlobs();

					if(blobList != null && blobList[0] != null)
						adjustHeadingAndSpeed(blobList[0]);
					else {
						log("doLineFollow: No primary blob, stopping robot...");
						speed = angle = 0;
					}

					// Right now only designed for detection of blue secondary blob
					if(blobList != null && numBlobs > 1 && blobList[1] != null) {
						handleSecondaryBlob(blobList[1]);
					} else log("doLineFollow: No secondary blob!");
				} else {
					log("doLineFollow: ERROR: No blobs present, stopping robot...");
					speed = angle = 0;
				}
			
			} else {
				log("doLineFollow: ERROR: Blob too old (" + blobAge + "ms), stopping robot...");
				speed = angle = 0;
			}
		} else {
			log("doLineFollow: ERROR: No blob data, stopping robot...");
			speed = angle = 0;
		}
	}
	
	public void run() {
		
		while(!done) {
			if (inIntersection)
				doIntersection();
			else
				doLineFollow();
			
			log("Sending Robot Command, speed=" + speed + ", angle=" + angle);
			p2di.setSpeed(speed, dtor(angle));
			
			pause(100); // execute while loop at a minimum of 10Hz
		}
		
		log("run: thread exiting, ensuring robot is stopped...");
		speed = angle = 0;
		p2di.setSpeed(speed, dtor(angle));
	}

	/**
	 * This is called whenever new blob data is available.
	 * 
	 * @param blobData The new blob data available.
	 * @param timestamp The timestamp of the data.  This can be compared to System.currentTimeMillis() to determine the age of the blob data.
	 */
	public void newPlayerBlobfinderData(PlayerBlobfinderData blobData, long timestamp) {
		synchronized(this) {
			this.blobFinderData = blobData;
			this.blobFinderDataTimestamp = timestamp;
			notifyAll();
		}
	}
	
	/**
	 * Converts degrees to radians.
	 * 
	 * @param degrees The angle in degrees.
	 * @return The angle in radians.
	 */
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
	
	private void log(String msg) {
		log(msg, true);
	}
	
	private void log(String msg, boolean isDebugMsg) {
		if (!isDebugMsg || System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(msg);
		if (flogger != null)
			flogger.log(msg);
	}
	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(msg);
	}
	
	private static void usage() {
		System.setProperty ("PharosMiddleware.debug", "true");
		print("Usage: pharoslabut.navigate.LineFollower <options>\n");
		print("Where <options> include:");
		print("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		print("\t-port <port number>: The Player Server's port number (default 6665)");
		print("\t-log <log file name>: The name of the file in which to save debug output (default null)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String serverIP = "localhost";
		int serverPort = 6665;
		FileLogger flogger = null;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-server")) {
					serverIP = args[++i];
				} else if (args[i].equals("-port")) {
					serverPort = Integer.valueOf(args[++i]);
				} else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				} else if (args[i].equals("-log")) {
					flogger = new FileLogger(args[++i]);
				} else {
					print("Unknown argument " + args[i]);
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
 
		print("Server IP: " + serverIP);
		print("Server port: " + serverPort);
		
		new LineFollower(serverIP, serverPort, flogger);
	}
}