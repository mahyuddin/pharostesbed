package pharoslabut.navigate;

import playerclient.*;
import playerclient.structures.blobfinder.*;
import playerclient.structures.*;
import pharoslabut.logger.*;

import java.util.*;

/**
 * Follows a line using a CMUcam2.
 * 
 * @author Seth Gee
 * @author Chien-Liang Fok
 */
public class LineFollower implements BlobfinderListener, Runnable {
	
	/**
	 * This is the cycle period of the LineFollower thread.
	 * It is in milliseconds.
	 */
	public static int CYCLE_PERIOD = 100; // 10Hz
	
	/**
	 * This is the maximum valid age of the blob data.  Anything older than that is discarded.
	 */
	public static final long BLOB_MAX_VALID_AGE = 1000;
	
	/**
	 * The maximum speed of the robot in meters per second.
	 */
	public static final double MAX_SPEED = .75;
	
	/**
	 * The minimum speed of the robot in meters per second.
	 */
	public static final double MIN_SPEED = 0.3;
	
	/**
	 * The maximum turn angle of the robot in degrees.
	 */
	public static final double MAX_TURN_ANGLE = 40;
	
	private Vector<LineFollowerEventListener> listeners = new Vector<LineFollowerEventListener>();
	
	private PlayerClient client = null;	
	private BlobfinderInterface bfi = null;
	
	/**
	 * This is the latest blob finder data received and the time at which it was received.
	 */
	private PlayerBlobfinderData blobFinderData = null;
	private long blobFinderDataTimestamp;
	
	private boolean done = false;
	
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
	 * Adds a LineFollowerEventListener to this object.
	 * 
	 * @param lfel the LineFollowerEventListener to add.
	 */
	public void addListener(LineFollowerEventListener lfel) {
		listeners.add(lfel);
	}
	
	/**
	 * Removes a LineFollowerEventListener from this object.
	 * 
	 * @param lfel the LineFollowerEventListener to remove.
	 */
	public void removeListener(LineFollowerEventListener lfel) {
		listeners.remove(lfel);
	}
	
	/**
	 * Broadcasts the LineFollowerEvent a to all registered listeners.
	 * 
	 * @param lfe The event to broadcast.
	 */
	private void notifyListeners(LineFollowerEvent lfe) {
		Enumeration<LineFollowerEventListener> e = listeners.elements();
		while(e.hasMoreElements()) {
			e.nextElement().newLineFollowerEvent(lfe);
		}
	}
	
	/**
	 * Starts the robot following the line.
	 */
	public void start() {
		if (thread == null) {
			done = false;
			thread = new Thread(this);
			thread.start();
			log("start: thread started");
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
			log("Stop: thread stopped.");
		} else
			log("Stop: ERROR: already stopped.");
	}
	
	/**
	 * The maximum turn angle should be throttled based on the 
	 * divergence.  A large divergence indicates the
	 * need for a large turn angle and vice-versa.
	 * Since the robot is programmed to move faster when the
	 * divergence is lower, this effectively dampens the robot's 
	 * turn rate when it is moving faster, thus achieving greater
	 * stability.  Without this, the robot would have a tendency to 
	 * swerve side-to-side as it increases in speed. 
	 * 
	 * @param divergencePct The percent divergence.
	 * @return The maximum turn angle.
	 */
	private double getMaxTurnAngle(double divergencePct) {
		if (divergencePct < 10)
			return MAX_TURN_ANGLE * 0.6;
		if (divergencePct < 20)
			return MAX_TURN_ANGLE * 0.7;
		if (divergencePct < 30)
			return MAX_TURN_ANGLE * 0.75;
		if (divergencePct < 40)
			return MAX_TURN_ANGLE * 0.8;
		if (divergencePct < 50)
			return MAX_TURN_ANGLE * 0.9;
		return MAX_TURN_ANGLE;
	}
	
	/**
	 * Adjusts the heading and speed of the robot based on the position of the blob.
	 * 
	 * @param blob The blob to use to calculate the heading of the robot.
	 */
	private void adjustHeadingAndSpeed(PlayerBlobfinderBlob blob) {
		log("adjustHeadingAndSpeed: Blob area=" + blob.getArea() + ", color=" + blob.getColor() + ", left=" + blob.getLeft() + ", right=" + blob.getRight());

		int midPoint = blobFinderData.getWidth()/2;

		int turnSign;		
		if (blob.getX() > midPoint) {
			log("adjustHeadingAndSpeed: Center of blob is left of midpoint, must turn right!");
			turnSign = -1;
		} else {
			log("adjustHeadingAndSpeed: Center of blob is right of midpoint, must turn left!");
			turnSign = 1;
		}

		// Make the angle adjustment proportional to the degree to which the heading has diverged
		// from being perfectly centered on the line.
		// A divergencePct of 1 means the robot is the most off in terms of following the line.
		// A divergencePct of 0 means that the robot is perfectly centered on the line.
		double divergence = Math.abs(blob.getX() - midPoint);  // blob.getX() returns the centroid's X coordinate
		double divergencePct = divergence / midPoint;
		log("adjustHeadingAndSpeed: divergencePct = " + divergencePct);
		
		
		angle = turnSign * getMaxTurnAngle(divergencePct) * divergencePct;

		// Make the speed proportional to the degree to which the heading is off. 
		speed = MAX_SPEED * (1 - divergencePct);
		if (speed < MIN_SPEED)
			speed = MIN_SPEED;
	}
	
	/**
	 * The secondary blob is used for indicating three things: 
	 * 
	 * APPROACHING_INTERSECTION: secondary blob right of line
	 * ENTERING_INTERSECTION: secondary blob left of line
	 * EXITING_INTERSECTION: secondary blob crossing line
	 * 
	 * @param blob
	 */
	private void handleSecondaryBlob(PlayerBlobfinderBlob blob) {
		// TODO: Add logic that determines the type of event that was detected.
		// One the type of event is determined, broadcast it to all registered listeners.
		
		// Here's an example of how to broadcast an APPROACHING event. 
//		LineFollowerEvent lfe = new LineFollowerEvent(LineFollowerEvent.LineFollowerEventType.APPROACHING);
//		notifyListeners(lfe);
	}
	
	/**
	 * Performs the calculations that determine the turn angle and speed to ensure
	 * the robot follows the line.  It sets variables speed and angle.
	 */
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
						log("doLineFollow: ERROR: No primary blob, stopping robot...");
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
	
	/**
	 * This contains the main loop of the LineFollower thread.
	 */
	public void run() {
		
		while(!done) {
			doLineFollow();
			
			log("Sending Robot Command, speed=" + speed + ", angle=" + angle);
			p2di.setSpeed(speed, dtor(angle));
			
			pause(CYCLE_PERIOD); 
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
	
	/**
	 * Logs a debug message.  This message is only printed when debug mode is enabled.
	 * 
	 * @param msg The message to log.
	 */
	private void log(String msg) {
		log(msg, true);
	}
	
	/**
	 * Logs a message.
	 * 
	 * @param msg  The message to log.
	 * @param isDebugMsg Whether the message is a debug message.
	 */
	private void log(String msg, boolean isDebugMsg) {
		String result = "LineFollower: " + msg;
		if (!isDebugMsg || System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
		if (flogger != null)
			flogger.log(result);
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
		
		LineFollower lf = new LineFollower(serverIP, serverPort, flogger);
		lf.start();
	}
}