package pharoslabut.navigate;

import java.util.Enumeration;
import java.util.Vector;

import pharoslabut.logger.Logger;
import pharoslabut.sensors.BlobDataConsumer;
import pharoslabut.sensors.BlobDataProvider;
import playerclient3.Position2DInterface;
import playerclient3.PtzInterface;
import playerclient3.structures.blobfinder.PlayerBlobfinderBlob;
import playerclient3.structures.blobfinder.PlayerBlobfinderData;
import playerclient3.structures.ptz.PlayerPtzCmd;

/**
 * Uses the CMUCam2 to follow a line.  Implements two PID controllers
 * one to keep the camera facing the line, the other to control the
 * speed and front servos.
 * 
 * @author Chien-Liang Fok
 *
 */
public class LineFollower2 implements Runnable, BlobDataConsumer {
	/**
	 * This is the cycle period of the LineFollower thread.
	 * It is in milliseconds.
	 */
	//public static int CYCLE_PERIOD = 100; // 10Hz
	//public static int CYCLE_PERIOD = 10; // 100Hz
	public static int CYCLE_PERIOD = 5; // 200Hz
	
	/**
	 * Implements the PID controller for keeping the camera facing the line.
	 */
	private CameraPanController cameraPanController;
	
	/**
	 * Implements the PID controller for keeping the robot on the line.
	 */
	private RobotController robotController;
	
	/**
	 * This is the maximum area in pixels that a blob can consume before
	 * being considered a valid blob.
	 */
	public static int BLOB_AREA_MAX_THRESHOLD = 200;
	
	/**
	 * This is the maximum valid age of the blob data.  Anything older than that is discarded.
	 */
	public static final long BLOB_MAX_VALID_AGE = 1500;
	
	/**
	 * Whether new blob data was received.
	 */
	private boolean newBlobData = false;
	
	/**
	 * The latest blob data available.
	 */
	private PlayerBlobfinderData blobData = null;
	
//	public int turnSign;
	
	private boolean done = false;
	
	/**
	 * The object that moves the robot.
	 */
	private Position2DInterface motors = null;
	
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
	 * @param motors The robot's motors.
	 * @param ptz The camera's PTZ actuators.
	 */
	public LineFollower2(BlobDataProvider provider, Position2DInterface motors, 
			PtzInterface ptz)
	{
		
		Logger.log("Registering as blob data consumer.");
		provider.addBlobDataConsumer(this);
		
		this.motors = motors;
		this.cameraPanController = new CameraPanController(ptz);
		this.robotController = new RobotController();
		
		Logger.log("Ensuring the motors are initially stopped.");
		motors.setSpeed(0f,0f);  // ensure robot is initially stopped
		
		Logger.log("Resetting camera position.");
		PlayerPtzCmd ptzCmd = new PlayerPtzCmd();
		ptzCmd.setPan(0f);
		ptzCmd.setTilt(0f);
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
		robotController.setMaxSpeed(maxSpeed);
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
	
	/**
	 * Unpauses the line follower.
	 */
	public synchronized void unpause() {
		if (paused) {
			Logger.log("Resuming...");
			paused = false;
		}
	}
	
//	/**
//	 * Adjusts the heading and speed of the robot based on the position of the blob.
//	 * 
//	 * @param blob The blob to use to calculate the heading of the robot.
//	 * @param midPoint The middle of the field of view.
//	 */
//	private void adjustHeadingAndSpeed(PlayerBlobfinderBlob blob, int midPoint) {
//		
//		if (blob.getX() > midPoint) {
//			Logger.log("Center of blob is right of midpoint, must pan right!");
//			turnSign = -1;
//		} else {
//			Logger.log("Center of blob is left of midpoint, must pan left!");
//			turnSign = 1;
//		}
//		
//		// Make the angle adjustment proportional to the degree to which the heading has diverged
//		// from being perfectly centered on the line.
//		// A divergencePct of 1 means the robot is the most off in terms of following the line.
//		// A divergencePct of 0 means that the robot is perfectly centered on the line.
//		double divergence = Math.abs(blob.getX() - midPoint);  // blob.getX() returns the centroid's X coordinate
//		double divergencePct = divergence / midPoint;
//		Logger.log("divergencePct = " + divergencePct);
//			
//		/*//-----------------------------------------------------added by sushen---------------------------------
//		if(divergencePct > 0.7 && panFlag == false){ 
//			pan = turnSign * PAN_STEP * divergencePct;
//			panFlag = true;
//			oldSign = turnSign;
//		}
//		else if(panFlag == true){
//			pan = Math.abs(pan) - 1;
//			pan *= oldSign;
//		}*/
//
//		//-----------------------------------------alternative method than above----------------------------------
////		if(divergencePct < 0.6)
////			pan = 0;
////		else {pan = turnSign * PAN_STEP * divergencePct;}
//		
//		
//		//--------------------------------------------------------------------------------------------------------
//		
////		angle = turnSign * getMaxTurnAngle(divergencePct) * divergencePct;
//		angle = MAX_TURN_ANGLE * ( (pan/MAX_PAN) + (turnSign*divergencePct) );
////		angle = MAX_TURN_ANGLE * pan / MAX_PAN;
//		Logger.log(":::: pan share=" + pan/MAX_PAN*MAX_TURN_ANGLE + " div share=" + MAX_TURN_ANGLE*turnSign*divergencePct );
//		if ( angle > MAX_TURN_ANGLE ) {
//			Logger.log("BUG: angle > MAX_TURN_ANGLE. angle=" + angle );
//			angle = MAX_TURN_ANGLE;
//		}
//		if ( angle < (MAX_TURN_ANGLE * -1) ) {
//			Logger.log("BUG: angle > MAX_TURN_ANGLE. angle=" + angle );
//			angle = MAX_TURN_ANGLE * -1;
//		}
//		/*/ Make the speed proportional to the degree to which the heading is off. 
//		speed = MAX_SPEED * (1 - divergencePct);
//		if (speed < MIN_SPEED)
//			speed = MIN_SPEED;
//		*/
//		//---------------------------------------------------------------------------Alternate method added by sushen -------------
//		speed = MIN_SPEED + ( (currMaxSpeed - MIN_SPEED) * (1 - Math.abs(angle/MAX_TURN_ANGLE)) );
////speed = 0.65;
////		speed = ((MAX_SPEED - MIN_SPEED) * (1 - divergencePct)) + MIN_SPEED;
////		if(angle > (0.6*MAX_TURN_ANGLE) )
////			speed = MIN_SPEED;
////			angle = turnSign * MAX_TURN_ANGLE;
//	}

	/**
	 * Processes the blobs being reported by the CMUCam2.  It assumes that the first blob represents
	 * the line to be followed.
	 * 
	 * @param data The blob finder data that contains information about all of the blobs in the
	 * current field of view.
	 * @return whether the blob data contained a valid blob representing the line.
	 */
	private synchronized boolean processBlobs(PlayerBlobfinderData data) {
		// Check to make sure we have valid data...
		if (data != null) {
			int numBlobs = data.getBlobs_count();
//			Logger.log("There are " + numBlobs + " blobs...");

			if(numBlobs > 0) {
				try {
					PlayerBlobfinderBlob blob = data.getBlobs()[0];
					int midPoint = data.getWidth()/2;  // The midpoint is half of the image width dimension.
					if (blob.getArea() < BLOB_AREA_MAX_THRESHOLD) {
						
						// Adjust the camera's pan to center it on the line
						cameraPanController.adjustCameraPan(blob, midPoint);
						
						// Adjust the robot's steering angle and speed based on
						// the camera's pan angle
						robotController.adjustSteeringAndSpeed(cameraPanController.getPanAngle());
						return true;
					} else {
						Logger.logDbg("Blob area is " + blob.getArea() 
								+ ", max threshold is " + BLOB_AREA_MAX_THRESHOLD 
								+ " pixels, ignoring blob.");
					}
				} catch(Exception e) {
					Logger.logErr("Error while fetching primary blob: " + e.toString());
				}
				
			} else {
				Logger.logErr("No blobs present, stopping robot...");	
			}
		} else {
			Logger.logErr("Blob data is null, stopping robot...");
		}
		return false;
	}
	
	public static final long MIN_MSG_PRINT_DURATION = 2000;
	double prevSpeedCmd = -1;
	double prevAngleCmd = -1;
	long prevPrintTime = -1;
	boolean errorState = false;
	
	
	boolean override = false;
	double overrideSteeringAngle;
	double overrideSpeed;
	
	// The following was added for the "evade" operating in the autonomous intersection.
	public synchronized void override(double steeringAngle, double speed) {
		this.override = true;
		this.overrideSteeringAngle = steeringAngle;
		this.overrideSpeed = speed;
	}
	
	/**
	 * This contains the main loop of the LineFollower thread.
	 */
	public void run() {
		long blobDataTimeStamp = 0; // The age of the latest valid timestamp.
		
		double speed = 0, steeringAngle = 0;
		
		while(!done) {
			
			if (!override) {
				// If new blob data is available, get and process it.
				synchronized(this) {
					if (newBlobData) {
						newBlobData = false;
						if (processBlobs(blobData)) {
							blobDataTimeStamp = System.currentTimeMillis(); // only update timestamp if the blob contained line data.
							speed = robotController.getSpeed();
							steeringAngle = robotController.getSteeringAngle();
						} else
							speed = steeringAngle = 0;
					}
				}
				
				// If no blob data is received within a threshold time window, stop the robot.
				if (speed != 0 && steeringAngle != 0 && System.currentTimeMillis() - blobDataTimeStamp > BLOB_MAX_VALID_AGE) {
					Logger.logErr("No valid blob data in past " + BLOB_MAX_VALID_AGE + "ms, stopping robot.");
					notifyListenersError(LineFollowerError.NO_BLOB);
					speed = steeringAngle = 0;
				}
			} else {
				speed = overrideSpeed;
				steeringAngle = overrideSteeringAngle;
			}
			
			if (paused) {
				Logger.log("LineFollower is paused, setting speed to zero but leaving the steering angle alone.");
				speed = 0;
			}
			
			// Only print the status if something changed or a minimum interval of time has passed.
			if (prevSpeedCmd != speed || prevAngleCmd != steeringAngle
					|| System.currentTimeMillis() - prevPrintTime > MIN_MSG_PRINT_DURATION) 
			{
				Logger.log("Sending Command, speed=" + speed + ", steering angle=" + steeringAngle);
				prevSpeedCmd = speed;
				prevAngleCmd = steeringAngle;
				prevPrintTime = System.currentTimeMillis();
			}

			motors.setSpeed(speed, dtor(steeringAngle));
			
			// Update the listeners.  errorState will be set to false prior to notifying the
			// listeners that the line follower is working.
			if (errorState && speed > 0)
				notifyListenersNoError();
			
			pharoslabut.util.ThreadControl.pause(this, CYCLE_PERIOD);
		}
		
		Logger.log("Thread exiting, ensuring robot is stopped...");
		motors.setSpeed(0, dtor(0));
		cameraPanController.shutdown();
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

	@Override
	public void newBlobData(PlayerBlobfinderData blobData) {
//		Logger.log("Received new blob data.");
		synchronized(this) {
			this.blobData = blobData;
			this.newBlobData = true;
			this.notifyAll();
		}
	}
	
	/**
	 * Implements a PID controller for keeping the camera pointing to the blob.
	 * 
	 * @see http://en.wikipedia.org/wiki/PID_controller
	 */
	private class CameraPanController {
		/**
		 * The maximum pan angle approximately in degrees.
		 */
		public static final double MAX_PAN = 40;
		
		/**
		 * The camera's tilt angle approximately in degrees.
		 */
		public static final float CAMERA_TILT = 0;
		
		/**
		 * Weight of correction that is proportional to the instantaneous error.
		 */
		public static final double KP = 0.3;
		
		/**
		 * Weight of correction that is proportional to the cumulative error.
		 */
		public static final double KI = 0.1;
		
		/**
		 * Weight of correction that is proportional to the change in error.
		 */
		public static final double KD = 0;
		
		/**
		 * The sign of the pan angle (either 1 or -1).
		 */
		//private int panSign = 1;
		
		/**
		 * The current pan angle of the camera in approximate degrees.
		 */
		private double pan = 0;
		
		/**
		 * The previous error.  This is used for computing the derivative term
		 * of the PID controller.
		 */
		private double prevErr = 0;
		
		/**
		 * The sum of all errors.  This is used for computing the integral term
		 * of the PID controller.
		 */
		private double errSum = 0;
		
		/**
		 * The component through which we can set the pan/tilt/zoom of the camera.
		 */
		private PtzInterface ptz = null;
		
		/**
		 * The PTZ command to send to the camera.
		 */
		PlayerPtzCmd ptzCmd;
		
		/**
		 * The constructor.
		 * 
		 * @param ptz The component that enables the camera's pan/tilt/zoom.
		 */
		public CameraPanController(PtzInterface ptz) {
			this.ptz = ptz;
			
			// Center the camera.
			ptzCmd = new PlayerPtzCmd();
			ptzCmd.setPan((float) 0);		//change 0 to PAN_OFFSET if not being implemented in driver and initialize variable at top
			ptzCmd.setTilt((float) 0);		//change 0 to TILT_OFFSET if not being implemented in driver and initialize variable at top
			ptz.setPTZ(ptzCmd);
			
			// Pause to allow the camera's position to stabilize.
			pharoslabut.util.ThreadControl.pause(this, 50);	
		}
		
		/**
		 * This is used by the line follower to adjust the steering of the robot
		 * to ensure it stays on the line.
		 * 
		 * @return The current pan angle of the camera in approximate degrees.
		 */
		public double getPanAngle() {
			return pan;
		}
		
		/**
		 * Shuts down the camera pan controller.  Resets the position of the camera.
		 */
		public void shutdown() {
			PlayerPtzCmd ptzCmd = new PlayerPtzCmd();
			ptzCmd.setPan((float)0);
			ptzCmd.setTilt((float)0);
			ptz.setPTZ(ptzCmd);
		}
		
		/**
		 * Adjusts the camera's pan angle to keep the blob centered in the field
		 * of view.
		 * 
		 * @param blob The blob representing the line being followed.
		 * @param midPoint The middle of the field of view.
		 */
		private void adjustCameraPan(PlayerBlobfinderBlob blob, int midPoint) {
			
			/*
			 * The x coordinate of the blob is defined as follows:
			 *   0 is full-left, 174 is full right.
			 * Ideally, the x component of the blob's centroid should be 87.
			 * A positive error means the blob is too far to the left, meaning
			 * the camera should pan left, and vice versa.
			 */
			double error = midPoint - blob.getX(); 

			// Update the sum of all errors.
			errSum += error;
			
			// Compute the change in error.
			double errDelta = error - prevErr;
			prevErr = error;
			
			pan = KP * error + KI * errSum + KD * errDelta;
			
			Logger.log("Blob area=" + blob.getArea() + ", color=" + blob.getColor() + ", left=" + blob.getLeft() 
					+ ", right=" + blob.getRight() + ", x=" + blob.getX() + ", midpoint=" + midPoint 
					+ ", error=" + error + ", error sum=" + errSum + ", prevErr=" + prevErr 
					+ ", pan=" + pan);
			
			if ( pan > MAX_PAN ) {
				Logger.logWarn("Clipping pan to max value.");
				pan = MAX_PAN;
			}
			if( pan < -MAX_PAN ) {
				pan = -MAX_PAN;
				Logger.logWarn("Clipping pan to min value.");
			}
			
			
			ptzCmd.setPan((float)pan);
			ptz.setPTZ(ptzCmd);
			
			ptzCmd.setPan((float) 0);
		}
	}
	
	/**
	 * Implements a PID controller for keeping the robot on the line.
	 * 
	 * @see http://en.wikipedia.org/wiki/PID_controller
	 */
	private class RobotController {
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
		
		/**
		 * Weight of correction that is proportional to the instantaneous error.
		 */
		public static final double KP = 0.2;
		
		/**
		 * Weight of correction that is proportional to the cumulative error.
		 */
		public static final double KI = 0.1;
		
		/**
		 * Weight of correction that is proportional to the change in error.
		 */
		public static final double KD = 0;
		
		/**
		 * The current steering angle of the robot in approximate degrees.
		 */
		private double steeringAngle = 0;
		
		/**
		 * The current speed of the robot in approximate meters per second.
		 */
		private double speed = 0;
		
		/**
		 * The previous error.  This is used for computing the derivative term
		 * of the PID controller.
		 */
		private double prevErr = 0;
		
		/**
		 * The sum of all errors.  This is used for computing the integral term
		 * of the PID controller.
		 */
		private double errSum = 0;
		
		/**
		 * The max speed of the robot in m/s.
		 */
		private double maxSpeed = MAX_SPEED;
		
		/**
		 * The constructor.
		 */
		public RobotController() {
		}
		
		/**
		 * @return The current desired steering angle in approximate degrees.
		 */
		public double getSteeringAngle() {
			return steeringAngle;
		}
		
		/**
		 * @return The current desired speed in meters per second.
		 */
		public double getSpeed() {
			return speed;
		}
		
		/**
		 * Sets the maximum speed that the robot should travel at.
		 * @param maxSpeed The maximum speed.
		 */
		public void setMaxSpeed(double maxSpeed) {
			this.maxSpeed = maxSpeed;
		}
		
		/**
		 * Adjusts the camera's pan angle to keep the blob centered in the field
		 * of view.
		 * 
		 * @param pan The current pan angle of the camera in approximate degrees.
		 */
		private void adjustSteeringAndSpeed(double pan) {
			
			// Abort if we're currently paused.
			if (paused) return;
			
			/*
			 * The pan angle is defined as follows:
			 * 
			 *   [+40 . . . . 0 . . . . -40]
			 * 
			 * Ideally, the pan angle should be zero.
			 * A positive error means the camera is facing right meaning the
			 * robot should steer right.
			 */
			double error = pan; 

			int steeringSign = 1;
			if (pan > 0)
				steeringSign = 1;
			else
				steeringSign = -1;
			
			double divergencePct = Math.abs(pan) / CameraPanController.MAX_PAN;
			steeringAngle = MAX_TURN_ANGLE * divergencePct * steeringSign;
			// Update the sum of all errors.
//			errSum += error;
//			
//			// Compute the change in error.
//			double errDelta = error - prevErr;
//			prevErr = error;
//			
//			steeringAngle = KP * error + KI * errSum + KD * errDelta;
//		
//			
//			if (steeringAngle > MAX_TURN_ANGLE) {
//				Logger.logWarn("Clipping steering angle to max value.");
//				steeringAngle = MAX_TURN_ANGLE;
//			}
//			if(steeringAngle < -MAX_TURN_ANGLE) {
//				steeringAngle = -MAX_TURN_ANGLE;
//				Logger.logWarn("Clipping steering angle to min value.");
//			}
			
			/*
			 * Adjust the speed based on the steering angle.
			 */
			speed = MIN_SPEED + ( (maxSpeed - MIN_SPEED) * (1 - Math.abs(steeringAngle/MAX_TURN_ANGLE)) );
			
			Logger.log("pan=" + pan + ", error=" + error + ", error sum=" + errSum + ", prevErr=" + prevErr 
					+ ", steeringAngle=" + steeringAngle + ", speed=" + speed);
		}
	}
}
