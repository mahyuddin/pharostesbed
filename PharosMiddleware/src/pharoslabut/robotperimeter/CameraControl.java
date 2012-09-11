package robotPerimeter;

import java.util.Enumeration;
import java.util.Vector;

import pharoslabut.logger.Logger;
import pharoslabut.navigate.LineFollowerError;
import pharoslabut.navigate.LineFollowerListener;
import pharoslabut.sensors.BlobDataConsumer;
import pharoslabut.sensors.BlobDataProvider;
import playerclient3.Position2DInterface;
import playerclient3.PtzInterface;
import playerclient3.structures.blobfinder.PlayerBlobfinderBlob;
import playerclient3.structures.blobfinder.PlayerBlobfinderData;
import playerclient3.structures.ptz.PlayerPtzCmd;

/**
 * 
 * Code controls the target detection for each host, and calls the relevant vision interface upon detecting the target
 * @author Nikhil Garg
 * 
 * Code adapted from LineFollower2, whose information is as follows:
 * 
 * Uses the CMUCam2 to follow a line.  Implements two PID controllers
 * one to keep the camera facing the line, the other to control the
 * speed and front servos.
 * 
 * @author Chien-Liang Fok
 *
 */
public class CameraControl implements Runnable, BlobDataConsumer {
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
	 * A reference to the thread performing the line following task.  It is null initially, but
	 * is assigned a value when start() is called.
	 */
	private Thread lineFollowerThread = null;

	/**
	 * The listeners for line follower events.
	 */
	private Vector<LineFollowerListener> listeners = new Vector<LineFollowerListener>();

	private Vision vision;

	/**
	 * The constructor.
	 * 
	 * @param client The Player client.
	 * @param motors The robot's motors.
	 * @param ptz The camera's PTZ actuators.
	 */
	public CameraControl(BlobDataProvider provider,
			PtzInterface ptz)
	{

		Logger.log("Registering as blob data consumer.");
		provider.addBlobDataConsumer(this);

		this.cameraPanController = new CameraPanController(ptz);

		Logger.log("Ensuring the motors are initially stopped.");

		Logger.log("Resetting camera position.");
		PlayerPtzCmd ptzCmd = new PlayerPtzCmd();
		ptzCmd.setPan(0f);
		ptzCmd.setTilt(0f);
		ptz.setPTZ(ptzCmd);

		vision = new Vision();
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

		double steeringAngle = 0;

		while(!done) {

			if (!override) {
				// If new blob data is available, get and process it.
				synchronized(this) {
					if (newBlobData) {
						newBlobData = false;
						if (processBlobs(blobData)) {
							blobDataTimeStamp = System.currentTimeMillis(); // only update timestamp if the blob contained line data.
							//Added code to call vision interface (by Nikhil)
							//TODO modify to detect people's shirts, also change targetID (currently hardcoded to 0)
							//to reflect shirt color
							vision.detectedBlob(cameraPanController.getPanAngle(), 0);
						} else
							steeringAngle = 0;
					}
				}

				// If no blob data is received within a threshold time window, stop the robot.
				if (steeringAngle != 0 && System.currentTimeMillis() - blobDataTimeStamp > BLOB_MAX_VALID_AGE) {
					Logger.logErr("No valid blob data in past " + BLOB_MAX_VALID_AGE + "ms, stopping robot.");
					notifyListenersError(LineFollowerError.NO_BLOB);
					steeringAngle = 0;
				}
			} else {
				steeringAngle = overrideSteeringAngle;
			}


			// Only print the status if something changed or a minimum interval of time has passed.
			if (prevAngleCmd != steeringAngle
					|| System.currentTimeMillis() - prevPrintTime > MIN_MSG_PRINT_DURATION) 
			{
				Logger.log("steering angle=" + steeringAngle);
				prevAngleCmd = steeringAngle;
				prevPrintTime = System.currentTimeMillis();
			}


			// Update the listeners.  errorState will be set to false prior to notifying the
			// listeners that the line follower is working.
			if (errorState)
				notifyListenersNoError();

			pharoslabut.util.ThreadControl.pause(this, CYCLE_PERIOD);
		}

		Logger.log("Thread exiting, ensuring robot is stopped...");
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
}
