package pharoslabut.demo.irobotcam;

import pharoslabut.io.TCPMessageSender;
import pharoslabut.logger.*;

/**
 * Provides the basic methods for controlling the iRobot create and the camera
 * that is attached to it.  This runs on the Demo Client.
 * 
 * @author Chien-Liang Fok
 */
public class CmdExec {
	
	private TCPMessageSender tcpSender;
	
	private FileLogger flogger = null;
	
	/**
	 * The constructor.
	 */
	public CmdExec(TCPMessageSender tcpSender, FileLogger flogger) {
		this.tcpSender = tcpSender;
		this.flogger = flogger;
	}
	
	/**
	 * Waits for an acknowledgment from the server.
	 * 
	 * @return the success value within the acknowledgment.
	 */
	private boolean getAck() {
		log("Waiting for acknowledgment...");
		CmdDoneMsg ackMsg = (CmdDoneMsg)tcpSender.receiveMessage();
		if (ackMsg != null) {
			log("Ack received, success = " + ackMsg.getSuccess());
			return ackMsg.getSuccess(); // success
		} else {
			log("Ack not received...");
			return false;
		}
	}
	
	/**
	 * Moves the robot forward or backward.  This is a blocking operation meaning the calling thread
	 * blocks and does not return until the operation is complete.
	 * 
	 * @param dist The distance to move in meters.  A positive value is forward;
	 * a negative value is backward.
	 * @return whether the operation was successful.
	 */
	public boolean moveRobot(double dist) {
		
		log("Sending move command to DemoServer...");
		RobotMoveMsg moveMsg = new RobotMoveMsg(dist);
		tcpSender.sendMessage(moveMsg);
		
		return getAck();
	}
	
	/**
	 * Turns the robot right or left.  This is a blocking operation meaning the calling thread
	 * blocks and does not return until the operation is complete.
	 * 
	 * @param angle The angle to turn in degrees.  The valid range is from
	 * -180 to 180.  Negative is to turn right (clockwise), positive is to 
	 * turn left (counter-clockwise).
	 * @return whether the operation was successful.
	 */
	public boolean turnRobot(double angle) {
		
		// Some sanity checks...
		if (angle < -180 || angle > 180) {
			log("Invalid turn angle (" + angle + ")...");
			return false;
		}
		
		log("Sending turn command (" + angle + ") to DemoServer...");
		RobotTurnMsg turnMsg = new RobotTurnMsg(angle);
		tcpSender.sendMessage(turnMsg);
		
		return getAck();
	}
	
	/**
	 * Pans the camera side to side.
	 * 
	 * @param angle The pan angle in degrees.  The valid range is from
	 * -90 to 90.  Zero degrees is straight forward. Negative is towards the
	 * left, positive is towards the right.
	 * @return whether the operation was successful.
	 */
	public boolean panCamera(double angle) {
		
		// Some sanity checks...
		if (angle < -90 || angle > 90) {
			log("Invalid camera pan angle (" + angle + ")...");
			return false;
		}
		
		log("Sending pan camera command to DemoServer...");
		CameraPanMsg panMsg = new CameraPanMsg(angle);
		tcpSender.sendMessage(panMsg);
		
		return getAck();
	}
	
	/**
	 * Tilts the camera up and down.
	 * 
	 * @param angle The tilt angle.  The valid range is from
	 * -90 to 90.  Zero degrees is straight forward. Negative is down, positive is up.  
	 * @return whether the operation was successful.
	 */
	public boolean tiltCamera(double angle) {
		
		// Some sanity checks...
		if (angle < -90 || angle > 90) {
			log("Invalid camera tilt angle (" + angle + ")...");
			return false;
		}
		
		log("Sending tilt camera command to DemoServer...");
		CameraTiltMsg tiltMsg = new CameraTiltMsg(angle);
		tcpSender.sendMessage(tiltMsg);
		
		return getAck();
	}
	
	/**
	 * Takes a snapshot using the camera.
	 * 
	 * @return whether the operation was successful.
	 */
	public boolean takeSnapshot() {
		
		log("Sending take camera snapshot command to DemoServer...");
		CameraTakeSnapshotMsg takeSnapshotMsg = new CameraTakeSnapshotMsg();
		tcpSender.sendMessage(takeSnapshotMsg);
		
		log("Waiting for camera snapshot result message...");
		CameraSnapshotMsg ackMsg = (CameraSnapshotMsg)tcpSender.receiveMessage();
		
		if (ackMsg.getSuccess()) {
			
			// get and display the image
			
			// after the user closes the window...
			return true;
		}
		else {
			// display error message telling user that camera failed to take snapshot...
			
			// after the user closes the error message window...
			return false;
		}
	}
	
	private void log(String msg) {
		String result = "CmdExec: " + msg;
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
		if (flogger != null) {
			flogger.log(result);
		}
	}

}
