package pharoslabut.navigate;

import playerclient3.*;
import playerclient3.structures.blobfinder.*;
import playerclient3.structures.ptz.PlayerPtzCmd;
import playerclient3.structures.*;
import pharoslabut.logger.*;
//import pharoslabut.navigate.LineFollowerEvent.LineFollowerEventType;
import pharoslabut.sensors.BlobDataConsumer;
import pharoslabut.sensors.ProteusOpaqueInterface;

import java.util.*;

/**
 * Follows a line using a CMUcam2.  It uses BlobfinderInterface
 * and PtzInterface to follow the line.
 * 
 * @author Chien-Liang Fok
 * @author Seth Gee
 * @author Sushen Patel
 * @author Maykel Sabet
 */
public class LineFollower implements Runnable {
	
	/**
	 * This is the cycle period of the LineFollower thread.
	 * It is in milliseconds.
	 */
	//public static int CYCLE_PERIOD = 100; // 10Hz
	//public static int CYCLE_PERIOD = 10; // 100Hz
	public static int CYCLE_PERIOD = 5; // 200Hz
	
	/**
	 * This is the minimum area in pixels that a blob must consume before
	 * being considered a valid blob.
	 */
	public static int BLOB_AREA_MIN_THRESHOLD = 200;
	
	/**
	 * This is the maximum valid age of the blob data.  Anything older than that is discarded.
	 */
	public static final long BLOB_MAX_VALID_AGE = 1500;
	
	/**
	 * The maximum speed of the robot in meters per second.
	 */
	//public static final double MAX_SPEED = 0.5;
	public static final double MAX_SPEED = 0.75;
	
	/**
	 * The minimum speed of the robot in meters per second.
	 */
	public static final double MIN_SPEED = 0.2;
	
	/**
	 * The maximum turn angle of the robot in degrees.
	 */
	public static final double MAX_TURN_ANGLE = 20;
	
	public int turnSign;
	
	private Vector<BlobDataConsumer> blobDataConsumer = new Vector<BlobDataConsumer>();
	private BlobDataConsumerNotifier blobDataConsumerNotifier = new BlobDataConsumerNotifier();
	
	/**-------------------------------------------------------------------------------added by sushen--------
	* The maximum pan angle of the camera (neither in degrees nor radians)
	*/
	public static final double PAN_STEP = 7;
	public static final double MAX_PAN = 40;
	//public static final double PAN_OFFSET = 0;		//implemented in robot driver through configuration file because 
	//public static final double TILT_OFFSET = 50;		//each robot might need different offsets, see wiki on how to change config file and drivers.
	//private boolean panFlag = false;
	public int oldSign = 0;
	public int noBlobs = 0;
	// ------------------------------------------------------------------------------------------------------
	
	private PlayerClient client = null;	
	private BlobfinderInterface bfi = null;
	private PtzInterface ptz = null;
	private ProteusOpaqueInterface poi = null;
	
	private boolean done = false;
	
	private Position2DInterface p2di = null;
	
	private double currMaxSpeed = MAX_SPEED;
	private double angle = 0;
	private double speed = 0;
	private double pan = 0;
	private double panOld = 0;
	
	/**
	 * Whether this line follower is paused.
	 */
	private boolean paused = false;
	
	/**
	 * A reference to the thread performing the line following task.  It is null initially, but
	 * is assigned a value when start() is called.
	 */
	private Thread lineFollowerThread = null;
	
	/**
	 * The listeners for line follower events.
	 */
	private Vector<LineFollowerListener> listeners = new Vector<LineFollowerListener>();

	/**
	 * The constructor.
	 * 
	 * @param client The Player client.
	 */
	public LineFollower(PlayerClient client) {
		// connect to blobfinder
		try {
			bfi = client.requestInterfaceBlobfinder(0, PlayerConstants.PLAYER_OPEN_MODE);
		} catch (PlayerException e) { Logger.logErr("Could not connect to blob finder proxy."); System.exit(1);}
		Logger.log("Created BlobFinder.");
		
		//set up pos. 2d proxy
		try {
			p2di = client.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		} catch (PlayerException e) { Logger.logErr("Could not connect to position 2d proxy."); System.exit(1);}
		Logger.logDbg("Created Position2dProxy.");
		
		// Connect to PTZ
		try {
			ptz = client.requestInterfacePtz(0, PlayerConstants.PLAYER_OPEN_MODE);
		} catch (PlayerException e) { Logger.logErr("Could not connect to PTZ proxy."); System.exit(1);}
		Logger.logDbg("Connected to PTZ proxy.");
		
		p2di.setSpeed(0f,0f);  // ensure robot is initially stopped
		
		PlayerPtzCmd ptzCmd = new PlayerPtzCmd();
		ptzCmd.setPan((float) 0);
		ptzCmd.setTilt((float) 0);
		Logger.log("Resetting position of the camera.");
		ptz.setPTZ(ptzCmd);
	}
	
	/**
	 * Adds a listener to this object.
	 * 
	 * @param listener The listener to add.
	 */
	public void addListener(LineFollowerListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Notifies the listeners of an error.
	 * 
	 * @param errno The cause of the error.
	 */
	private void notifyListenersError(final LineFollowerError errno) {
		errorState = true;
		new Thread() {
			public void run() {
				Enumeration<LineFollowerListener> e = listeners.elements();
				while (e.hasMoreElements()) {
					e.nextElement().lineFollowerError(errno);
				}
			}
		}.start();
	}
	
	/**
	 * Notifies the listeners that the line follower is working again.
	 */
	private void notifyListenersNoError() {
		errorState = false;
		new Thread() {
			public void run() {
				Enumeration<LineFollowerListener> e = listeners.elements();
				while (e.hasMoreElements()) {
					e.nextElement().lineFollowerWorking();
				}
			}
		}.start();
	}
	
	/**
	 * Sets the maximum speed that this line follower can move the vehicle.
	 * @param maxSpeed The maximum speed.
	 */
	public void setMaxSpeed(double maxSpeed) {
		Logger.log("Max speed set to " + maxSpeed + "m/s");
		this.currMaxSpeed = maxSpeed;
	}
	
	/**
	 * @return The player client.
	 */
	public PlayerClient getPlayerClient() {
		return client;
	}
	
	/**
	 * 
	 * @return The opaque interface
	 */
	public ProteusOpaqueInterface getOpaqueInterface() {
		return poi;
	}
	
	/**
	 * 
	 * @return The Position2DInterface.  This is useful for obtaining odometry information.
	 */
	public Position2DInterface getPosition2DInterface() {
		return p2di;
	}
	
	/**
	 * Starts the line following process.
	 */
	public synchronized void start() {
		if (lineFollowerThread == null) {
			done = false;
			lineFollowerThread = new Thread(this);
			lineFollowerThread.start();
			Logger.logDbg("Thread started");
		} else
			Logger.log("WARNING: Thread already started.");
	}
	
	/**
	 * Stops the line following process.
	 */
	public synchronized void stop() {
		if (lineFollowerThread != null) {
			Logger.logDbg("Pausing the line follower to allow robot to stop.");
			pause();
			
			synchronized(this) {
				try {
					wait(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			Logger.logDbg("Setting done = true");
			done = true;
			
			Logger.logDbg("Joining thread...");
			try {
				lineFollowerThread.join(2000); // wait at most 2 seconds for thread to terminate
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Logger.logDbg("Thread joined...");
			lineFollowerThread = null; // was causing RemoteIntersectionManager to crash 
			Logger.logDbg("Thread stopped.");
		} else
			Logger.log("WARNING: already stopped.");
	}
	
	/**
	 * Sets the speed to be zero, but keeps the line follower running.
	 */
	public synchronized void pause() {
		if (!paused) {
			Logger.log("Pausing...");
			paused = true;
		}
	}
	
	public synchronized void unpause() {
		if (paused) {
			Logger.log("Resuming...");
			paused = false;
		}
	}
	
//	/**
//	 * The maximum turn angle is throttled based on the 
//	 * divergence of the blob from the center.  A large divergence indicates the
//	 * need for a large turn angle and vice-versa.
//	 * Since the robot is programmed to move faster when the
//	 * divergence is lower, this effectively dampens the robot's 
//	 * turn rate when it is moving faster, thus achieving greater
//	 * stability.  Without this, the robot would have a tendency to 
//	 * swerve side-to-side as it increases in speed. 
//	 * 
//	 * @param divergencePct The percent divergence.
//	 * @return The maximum turn angle.
//	 */
//	private double getMaxTurnAngle(double divergencePct) {
//		Logger.log("getMaxTurnAgle: " + divergencePct);
//		if (divergencePct < .05)
//			return MAX_TURN_ANGLE * 0.05;
//		if (divergencePct < .10)
//			return MAX_TURN_ANGLE * 0.10;
//		if (divergencePct < .15)
//			return MAX_TURN_ANGLE * 0.15;
//		if (divergencePct < .20)
//			return MAX_TURN_ANGLE * 0.20;
//		if (divergencePct < .25)
//			return MAX_TURN_ANGLE * 0.25;
//		if (divergencePct < .30)
//			return MAX_TURN_ANGLE * 0.30;
//		if (divergencePct < .35)
//			return MAX_TURN_ANGLE * 0.35;
//		if (divergencePct < .40)
//			return MAX_TURN_ANGLE * 0.40;
//		if (divergencePct < .45)
//			return MAX_TURN_ANGLE * 0.45;
//		if (divergencePct < .50)
//			return MAX_TURN_ANGLE * 0.50;
//		if (divergencePct < .55)
//			return MAX_TURN_ANGLE * 0.55;
//		if (divergencePct < .60)
//			return MAX_TURN_ANGLE * 0.60;
//		if (divergencePct < .65)
//			return MAX_TURN_ANGLE * 0.65;
//		if (divergencePct < .70)
//			return MAX_TURN_ANGLE * 0.70;
//		if (divergencePct < .75)
//			return MAX_TURN_ANGLE * 0.75;
//		if (divergencePct < .80)
//			return MAX_TURN_ANGLE * 0.80;
//		if (divergencePct < .85)
//			return MAX_TURN_ANGLE * 0.85;
//		if (divergencePct < .90)
//			return MAX_TURN_ANGLE * 0.90;
//		if (divergencePct < .95)
//			return MAX_TURN_ANGLE * 0.95;
///*		if (divergencePct < .40)
//			return MAX_TURN_ANGLE * 0.45;	//modified by sushen
//		if (divergencePct < .50)
//			return MAX_TURN_ANGLE * 0.6;	//modified by sushen
//		if (divergencePct < .60)
//			return MAX_TURN_ANGLE * 0.75;	//modified by sushen
//		if (divergencePct < .70)
//			return MAX_TURN_ANGLE * 0.9;	//modified by sushen
//*/		return MAX_TURN_ANGLE;
//	}
	
	/**
	 * Adjusts the heading and speed of the robot based on the position of the blob.
	 * 
	 * @param blob The blob to use to calculate the heading of the robot.
	 * @param midPoint The middle of the field of view.
	 */
	private void adjustHeadingAndSpeed(PlayerBlobfinderBlob blob, int midPoint) {
		Logger.log("Blob area=" + blob.getArea() + ", color=" + blob.getColor() + ", left=" + blob.getLeft() + ", right=" + blob.getRight() + ", x=" + blob.getX());

		if (blob.getX() > midPoint) {
			Logger.log("Center of blob is right of midpoint, must turn right!");
			turnSign = -1;
		} else {
			Logger.log("Center of blob is left of midpoint, must turn left!");
			turnSign = 1;
		}

		// Make the angle adjustment proportional to the degree to which the heading has diverged
		// from being perfectly centered on the line.
		// A divergencePct of 1 means the robot is the most off in terms of following the line.
		// A divergencePct of 0 means that the robot is perfectly centered on the line.
		double divergence = Math.abs(blob.getX() - midPoint);  // blob.getX() returns the centroid's X coordinate
		double divergencePct = divergence / midPoint;
		Logger.log("divergencePct = " + divergencePct);
			
		/*//-----------------------------------------------------added by sushen---------------------------------
		if(divergencePct > 0.7 && panFlag == false){ 
			pan = turnSign * PAN_STEP * divergencePct;
			panFlag = true;
			oldSign = turnSign;
		}
		else if(panFlag == true){
			pan = Math.abs(pan) - 1;
			pan *= oldSign;
		}*/

		//-----------------------------------------alternative method than above----------------------------------
//		if(divergencePct < 0.6)
//			pan = 0;
//		else {pan = turnSign * PAN_STEP * divergencePct;}
		pan = panOld + ( turnSign * divergencePct * PAN_STEP );
		if ( pan > MAX_PAN )
			pan = MAX_PAN;
		if( pan < (MAX_PAN * -1) )
			pan = MAX_PAN * -1;
		Logger.log("panOld=" + panOld + " pan=" + pan + " divergencePct=" + divergencePct);
		panOld = pan;
		
		//--------------------------------------------------------------------------------------------------------
		
//		angle = turnSign * getMaxTurnAngle(divergencePct) * divergencePct;
		angle = MAX_TURN_ANGLE * ( (pan/MAX_PAN) + (turnSign*divergencePct) );
//		angle = MAX_TURN_ANGLE * pan / MAX_PAN;
		Logger.log(":::: pan share=" + pan/MAX_PAN*MAX_TURN_ANGLE + " div share=" + MAX_TURN_ANGLE*turnSign*divergencePct );
		if ( angle > MAX_TURN_ANGLE ) {
			Logger.log("BUG: angle > MAX_TURN_ANGLE. angle=" + angle );
			angle = MAX_TURN_ANGLE;
		}
		if ( angle < (MAX_TURN_ANGLE * -1) ) {
			Logger.log("BUG: angle > MAX_TURN_ANGLE. angle=" + angle );
			angle = MAX_TURN_ANGLE * -1;
		}
		/*/ Make the speed proportional to the degree to which the heading is off. 
		speed = MAX_SPEED * (1 - divergencePct);
		if (speed < MIN_SPEED)
			speed = MIN_SPEED;
		*/
		//---------------------------------------------------------------------------Alternate method added by sushen -------------
		speed = MIN_SPEED + ( (currMaxSpeed - MIN_SPEED) * (1 - Math.abs(angle/MAX_TURN_ANGLE)) );
//speed = 0.65;
//		speed = ((MAX_SPEED - MIN_SPEED) * (1 - divergencePct)) + MIN_SPEED;
//		if(angle > (0.6*MAX_TURN_ANGLE) )
//			speed = MIN_SPEED;
//			angle = turnSign * MAX_TURN_ANGLE;
	}

	/**
	 * Processes the blobs being reported by the CMUCam2.  It assumes that the first blob represents
	 * the line to be followed.
	 * 
	 * It also notifies all of the blob data consumers of the new blob.
	 * 
	 * @param data The blob finder data that contains information about all of the blobs in the
	 * current field of view.
	 * @return whether the blob data contained a valid blob representing the line being followed.
	 */
	private boolean processBlobs(PlayerBlobfinderData data) {
		// Check to make sure we have valid data...
		if (data != null) {
			int numBlobs = data.getBlobs_count();
			Logger.log("There are " + numBlobs + " blobs...");

			if(numBlobs > 0) {
				// Notify the blob data consumers of the new blob data
				blobDataConsumerNotifier.newBlobData(data);
				
				PlayerBlobfinderBlob[] blobListCopy = null;
				
				// Create a copy of the blobList array to avoid conflicts with the array reference
				// being changed while it is being read.
				synchronized(data) {
					PlayerBlobfinderBlob[] blobList = data.getBlobs();
					blobListCopy = new PlayerBlobfinderBlob[blobList.length];
					System.arraycopy(blobList, 0, blobListCopy, 0, blobList.length);
				}

				// All of the following checks should not be necessary.  They were added
				// to counter a null pointer exception being occasionally thrown.
				if(blobListCopy != null && blobListCopy.length > 0 && blobListCopy[0] != null) {
					int midPoint = data.getWidth()/2;  // The midpoint is half of the image width dimension.
					if( (blobListCopy[0].getArea() < BLOB_AREA_MIN_THRESHOLD) ) {
						Logger.log("BLOB Area = " + blobListCopy[0].getArea());
						adjustHeadingAndSpeed(blobListCopy[0], midPoint);
						return true;
					} else {
						Logger.logDbg("Blob are is less than minimum threshold of " + BLOB_AREA_MIN_THRESHOLD + ", ignoring blob.");
					}
				} else {
					Logger.logErr("No primary blob, stopping robot...");
					speed = angle = 0;					
				}

//				// Right now only designed for detection of blue secondary blob
//				try {
//					if(blobListCopy != null && numBlobs > 1 && blobListCopy[1] != null) {
//						handleSecondaryBlob(blobListCopy[0], blobListCopy[1]);
//					} 
//					else {
//						Logger.log("No secondary blob!");
//						previousEventType = null;
//					}
//				} catch(ArrayIndexOutOfBoundsException e) {
//					// TODO Figure out why this sometimes happens.
//					Logger.logErr("got an unexpected ArrayIndexOutOfBoundsException: " + e.getMessage());
//					previousEventType = null;
//				}
				
				
			}
			else {
				Logger.logErr("No blobs present, stopping robot...");
				speed = angle = 0;		
			}
		} else {
			Logger.logErr("Blob data is null, stopping robot...");
			speed = angle = 0;
		}
		return false;
	}
	
	public static final long MIN_MSG_PRINT_DURATION = 1000;
	double prevSpeedCmd = -1;
	double prevAngleCmd = -1;
	double prevPanCmd = -1;
	long prevPrintTime = -1;
	boolean errorState = false;
	
	/**
	 * This contains the main loop of the LineFollower thread.
	 */
	public void run() {
		long blobDataTimeStamp = 0; // The age of the latest valid timestamp.
		
		// Center the camera.
		PlayerPtzCmd ptzCmd = new PlayerPtzCmd();
		ptzCmd.setPan((float) 0);		//change 0 to PAN_OFFSET if not being implemented in driver and initialize variable at top
		ptzCmd.setTilt((float) 0);		//change 0 to TILT_OFFSET if not being implemented in driver and initialize variable at top
		ptz.setPTZ(ptzCmd);
		pause(50);	
		
		while(!done) {
				
			// If new blob data is available, get it and process it.
			if (bfi.isDataReady()) {
				if (processBlobs(bfi.getData()))
					blobDataTimeStamp = System.currentTimeMillis(); // only update timestamp if the blob contained line data.
			}
			
			// If no blob data is received within a certain time window, stop the robot.
			if (System.currentTimeMillis() - blobDataTimeStamp > BLOB_MAX_VALID_AGE) {
				if (System.currentTimeMillis() - prevPrintTime > MIN_MSG_PRINT_DURATION) {
					Logger.logErr("No valid blob data within time window of " + BLOB_MAX_VALID_AGE + "ms, stopping robot.");
					notifyListenersError(LineFollowerError.NO_BLOB);
					prevSpeedCmd = speed;
					prevAngleCmd = angle;
					prevPanCmd = pan;
					prevPrintTime = System.currentTimeMillis();
				}
				speed = angle = pan = 0;
			}
			
			if (paused) {
				Logger.log("LineFollower is paused, setting speed to zero but leaving the angle and pan alone.");
				speed = 0;
			}
			
			if (prevSpeedCmd != speed || prevAngleCmd != angle || prevPanCmd != pan 
					|| System.currentTimeMillis() - prevPrintTime > MIN_MSG_PRINT_DURATION) 
			{
				Logger.log("Sending Command, speed=" + speed + ", angle=" + angle + ", pan=" +pan);
				prevSpeedCmd = speed;
				prevAngleCmd = angle;
				prevPanCmd = pan;
				prevPrintTime = System.currentTimeMillis();
			}
			
			p2di.setSpeed(speed, dtor(angle));
			
			// Update the listeners.  errorState will be set to false prior to notifying the
			// listeners that the line follower is working.
			if (errorState && speed > 0)
				notifyListenersNoError();
			
			ptzCmd.setPan((float)pan);			//move camera, change to pan + PAN_OFFSET if not implemented in driver
			ptz.setPTZ(ptzCmd);
						
			pause(CYCLE_PERIOD);
		}
		
		Logger.log("Thread exiting, ensuring robot is stopped...");
		speed = angle = pan = 0;
		p2di.setSpeed(speed, dtor(angle));
		ptzCmd.setPan((float)pan);			//move camera, change to pan + PAN_OFFSET if not implemented in driver
		ptz.setPTZ(ptzCmd);
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

	/**
	 * Pauses the calling thread.
	 * 
	 * @param duration The number of milliseconds to pause.
	 */
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
	 * Adds a blob data consumer.  It will be notified each time a new
	 * blob arrives.
	 * 
	 * @param consumer The consumer to add.
	 */
	public void addBlobDataConsumer(BlobDataConsumer consumer) {
		blobDataConsumer.add(consumer);
	}

	/**
	 * Removes a blob data consumer.
	 * 
	 * @param consumer The consumer to remove.
	 */
	public void removeBlobDataConsumer(BlobDataConsumer consumer) {
		blobDataConsumer.remove(consumer);
	}
	
	/**
	 * This runs as a separate thread notifying the blob finder consumers of the new blob.
	 */
	private class BlobDataConsumerNotifier implements Runnable {
		PlayerBlobfinderData blobData = null;
		
		public BlobDataConsumerNotifier() {
			new Thread(this).start();
		}
		
		public synchronized void newBlobData(PlayerBlobfinderData blobData) {
			this.blobData = blobData;
			this.notifyAll();
		}
		
		public void run() {
			PlayerBlobfinderData currData = null;
			
			while (true) {
				
				synchronized(this) {
					if (blobData == null) {
						try {
							this.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						currData = blobData;
						blobData = null;
					}
				}
				
				if (currData != null) {
					Enumeration<BlobDataConsumer> e = blobDataConsumer.elements();
					while (e.hasMoreElements()) {
						e.nextElement().newBlobData(currData);
					}
					currData = null;
				}
			}
		}
	}
}
