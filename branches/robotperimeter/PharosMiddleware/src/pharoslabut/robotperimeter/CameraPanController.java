package robotPerimeter;


import pharoslabut.logger.Logger;
import playerclient3.PtzInterface;
import playerclient3.structures.blobfinder.PlayerBlobfinderBlob;
import playerclient3.structures.ptz.PlayerPtzCmd;

public class CameraPanController {

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
	public void adjustCameraPan(PlayerBlobfinderBlob blob, int midPoint) {
		
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
