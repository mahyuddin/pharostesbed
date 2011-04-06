package pharoslabut.demo.simonsays;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import pharoslabut.io.*;
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
	 * Halts the program.
	 */
	public void stop() {
		tcpSender.kill();
	}
	
	/**
	 * Sends a message to the server and waits for an acknowledgment.
	 * The acknowledgment is a CmdDoneMsg.
	 * 
	 * @return The success value within the acknowledgment.
	 */
	public boolean sendMsg(Message msg) {
		if (tcpSender == null) {
			log("sendMsg: No TCP Sender...");
			return false;
		}
		
		if (tcpSender.sendMessage(msg)) {
			log("sendMsg: Waiting for acknowledgment...");
			CmdDoneMsg ackMsg = (CmdDoneMsg)tcpSender.receiveMessage();
			if (ackMsg != null) {
				log("sendMsg: Ack received, success = " + ackMsg.getSuccess());
				return ackMsg.getSuccess(); // success
			} else {
				log("sendMsg: ERROR: Ack not received...");
				return false;
			}
		} else {
			log("sendMsg: ERROR while sending message.");
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
//	public boolean moveRobot(double dist) {
//		log("moveRobot: Sending move command to DemoServer...");
//		return sendMsg(new RobotMoveMsg(dist));
//	}
	
	/**
	 * Stops the player server.
	 */
	public boolean stopPlayer() {
		log("stopPlayer: Stopping the player server...");
		return sendMsg(new PlayerControlMsg(PlayerControlCmd.STOP));
	}
	
	public boolean startPlayer() {
		log("startPlayer: Starting the player server...");
		return sendMsg(new PlayerControlMsg(PlayerControlCmd.START));
	}
	
	/**
	 * Shuts down the player server.
	 */
//	public void resetPlayer() {
//		log("Resetting the player server...");
//		ResetPlayerMsg msg = new ResetPlayerMsg();
//		tcpSender.sendMessage(msg);
//	}
	
	/**
	 * Turns the robot right or left.  This is a blocking operation meaning the calling thread
	 * blocks and does not return until the operation is complete.
	 * 
	 * @param angle The angle to turn in degrees.  The valid range is from
	 * -360 to 360.  Negative is to turn right (clockwise), positive is to 
	 * turn left (counter-clockwise).
	 * @return whether the operation was successful.
	 */
//	public boolean turnRobot(double angle) {
//		
//		// Some sanity checks...
//		if (angle < -360 || angle > 360) {
//			log("turnRobot: ERROR: Invalid turn angle (" + angle + ")...");
//			return false;
//		}
//		
//		log("turnRobot: Sending turn command (" + angle + ") to DemoServer...");
//		return sendMsg(new RobotTurnMsg(angle));
//	}
	
	/**
	 * Pans the camera side to side.
	 * 
	 * @param angle The pan angle in degrees.  The valid range is from
	 * -45 to 45.  Zero degrees is straight forward. Negative is towards the
	 * left, positive is towards the right.
	 * @return whether the operation was successful.
	 */
//	public boolean panCamera(double angle) {
//		
//		// Some sanity checks...
//		if (angle < -45 || angle > 45) {
//			log("panCamera: Invalid camera pan angle (" + angle + ")...");
//			return false;
//		}
//		
//		log("panCamera: Sending pan camera command to DemoServer...");
//		return sendMsg(new CameraPanMsg(angle));
//	}
	
	/**
	 * Tilts the camera up and down.
	 * 
	 * @param angle The tilt angle.  The valid range is from
	 * -20 to 30.  Zero degrees is straight forward. Negative is down, positive is up.  
	 * @return whether the operation was successful.
	 */
//	public boolean tiltCamera(double angle) {
//		
//		// Some sanity checks...
//		if (angle < -20 || angle > 30) {
//			log("tileCamera: ERROR: Invalid camera tilt angle (" + angle + ")...");
//			return false;
//		}
//		
//		log("tiltCamera: Sending tilt camera command to DemoServer...");
//		return sendMsg(new CameraTiltMsg(angle));
//	}
	
	/**
	 * Takes a snapshot using the camera.
	 * 
	 * @return The image taken, or null if error.
	 */
	public BufferedImage takeSnapshot() {
		log("takeSnapshot: Sending take camera snapshot command to DemoServer...");
		CameraTakeSnapshotMsg takeSnapshotMsg = new CameraTakeSnapshotMsg();
		if (tcpSender.sendMessage(takeSnapshotMsg)) {
		
			log("takeSnapshot: Waiting for camera snapshot result message...");
			CameraSnapshotMsg ackMsg = (CameraSnapshotMsg)tcpSender.receiveMessage();
		
			log("takeSnapshot: Received camera snapshot results, success = " + ackMsg.getSuccess());
			if (ackMsg.getSuccess()) {
				BufferedImage bi = null;
				try {
					bi = ackMsg.getImage();
				} catch(IOException e) {
					e.printStackTrace();
					log("takeSnapshot: ERROR: Unable to get image from CameraSnapshotMsg, error = " + e.getMessage());
				}
				return bi;
			} else 
				return null;
		} else
			return null;
	}
	
	private void log(String msg) {
		String result = "CmdExec: " + msg;
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}

}
