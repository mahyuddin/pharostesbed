package pharoslabut.demo.simonsays;

//import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;

import pharoslabut.cpsAssert.AssertionRequestMsg;
import pharoslabut.cpsAssert.AssertionResponseMsg;
import pharoslabut.demo.simonsays.io.*;
import pharoslabut.exceptions.PharosException;
import pharoslabut.io.*;
import pharoslabut.logger.*;
//import pharoslabut.sensors.CricketData;
//import playerclient3.structures.PlayerPoint3d;

/**
 * This runs on the SimonSaysClient and provides the API for 
 * controlling the robot's movements and its camera. 
 * 
 * @author Chien-Liang Fok 
 * @author Kevin Boos
 * @see SimonSaysClient
 */
public class CmdExec implements MessageReceiver {
	//public static final long MAX_ACK_LATENCY = 5000; // in milliseconds
	
	private TCPMessageSender tcpSender = TCPMessageSender.getSender();
	private TCPMessageReceiver tcpReceiver = new TCPMessageReceiver(this);
//	private FileLogger flogger = null;
	
//	public static BeaconDataCollector bdc = new BeaconDataCollector("beaconData.txt");
	
	private InetAddress destAddr;
	private int destPort;
	
	private InetAddress localAddress;
	private int localPort;
	
	private Message rcvMsg = null;
	
	/**
	 * The constructor.
	 * 
	 * @param destAddr The destination IP address.
	 * @param destPort The destination port.
	 */
	public CmdExec(InetAddress destAddr, int destPort) {
		this.destAddr = destAddr;
		this.destPort = destPort;
		
		String localAddrStr = null;
		try {
			localAddrStr = pharoslabut.RobotIPAssignments.getAdHocIP();
			localAddress = InetAddress.getByName(localAddrStr);
			localPort = tcpReceiver.getPort();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			Logger.logErr("Failed to get InetAddress from ad hoc IP address: " + localAddrStr);
			System.exit(1);
		} catch (PharosException e) {
			e.printStackTrace();
			Logger.logErr("Failed to get ad hoc IP address.");
			System.exit(1);
		}
	}
	
	/**
	 * Halts the execution of commands.
	 */
	public void stop() {
		tcpSender.kill();
		tcpReceiver.kill();
	}
	
	/**
	 * Sends a message to the server and waits for an acknowledgment.
	 * 
	 * @return The success value within the acknowledgment.
	 */
	public boolean sendMsg(AckableMessage msg) {
		
		// Set the reply address and port so an acknowledgment can be returned.
		msg.setReplyAddr(localAddress);
		msg.setPort(localPort);
		
		// Send the message
		try {
			rcvMsg = null;
			if (msg instanceof AssertionRequestMsg)
				((AssertionRequestMsg)msg).msgSent();
			tcpSender.sendMessage(destAddr, destPort, msg);
		} catch (PharosException e1) {
			e1.printStackTrace();
			Logger.logErr("Failed to send message.");
			return false;
		}
		
//		if (msg instanceof RobotMoveMsg) // movement starts the timer
//			bdc.startTimer();
		
		boolean result = false;
		
		// Wait until an ack arrives...
		synchronized(this) {
			if (rcvMsg == null) {
				Logger.log("Waiting for acknowledgment...");
				try {
					//wait(MAX_ACK_LATENCY);
					// TODO: abort this wait if network connectivity to robot is broken.
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			if (rcvMsg != null) {
				if (rcvMsg instanceof CmdDoneMsg) {
					CmdDoneMsg ackMsg = (CmdDoneMsg)rcvMsg;
					Logger.log("Ack received, success = " + ackMsg.getSuccess());
					result = ackMsg.getSuccess(); // success
				} else {
					Logger.logErr("Ack received but is of wrong type: " + rcvMsg);
				}
				rcvMsg = null;
			} else {
				Logger.logErr("Ack not received...");
			}
		}
		
//		if (result)
//			bdc.stopTimer();
			
		return result;
	}
	
	@Override
	public void newMessage(Message msg) {
		synchronized(this) {
			this.rcvMsg = msg;
			
			if (rcvMsg instanceof CricketDataMsg) {
//				CricketDataMsg cricketMsg = (CricketDataMsg)rcvMsg;
//				CricketData cd = cricketMsg.getCricketData();
//				PlayerPoint3d coords = cricketMsg.getPoint();
				
				// pass new cricket data to Trilateration thread
				//double height = coords.getPz();
				//double dist = ((double)cd.getDistance())/100;
				//double radius = Math.sqrt(Math.abs(dist * dist - height * height)); // pythagorean theorem to find distance along ground
				
//				bdc.newBeaconData(System.currentTimeMillis(), coords.getPx(), coords.getPy(), radius);
				//Multilateration.newBeaconReading(cd.getCricketID(), System.currentTimeMillis(), coords.getPx(), coords.getPy(), radius);
				//Logger.log("Robot is " + radius + "m from (" + coords.getPx() + "," + coords.getPy() + ")" );
//				System.out.println(cd.getCricketID() + ", dist = " + cd.getDistance());
			}
//			else if (rcvMsg instanceof BeaconReadingMsg) {
//				BeaconReadingMsg brMsg = (BeaconReadingMsg)rcvMsg;
//				SimonSaysClient.bdc.newBeaconData(brMsg.getBeaconReading());
//			}
			
			else if (rcvMsg instanceof AssertionResponseMsg) {
				handleReceivedAssertion((AssertionResponseMsg)rcvMsg);
			}
			else {
				// used to signal a received ack msg
				this.notifyAll();
			}
		}
	}
	
	
	/**
	 * Performs the actions that should be taken when a AssertionResponseMsg is received.
	 * 
	 * @param sdMsg The AssertionResponseMsg received from the client
	 */
	private void handleReceivedAssertion(AssertionResponseMsg arMsg) {
		arMsg.msgReceived();
		
		System.out.println(arMsg.getAssertionMsg());	
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
	 * 
	 * @return true if the message was sent successfully.
	 */
	public boolean stopPlayer() {
		Logger.log("Stopping the player server...");
		return sendMsg(new PlayerControlMsg(PlayerControlCmd.STOP));
	}
	
	/**
	 * Starts the player server.
	 * 
	 * @return true if the message was sent successfully.
	 */
	public boolean startPlayer() {
		Logger.log("Starting the player server...");
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
		Logger.log("Sending take camera snapshot command to DemoServer...");
		
		// Create the message
		CameraTakeSnapshotMsg takeSnapshotMsg = new CameraTakeSnapshotMsg();
		
		// Set the reply address and port so an acknowledgment can be returned.
		takeSnapshotMsg.setReplyAddr(localAddress);
		takeSnapshotMsg.setPort(localPort);
		
		// Send the message
		try {
			rcvMsg = null;
			tcpSender.sendMessage(destAddr, destPort, takeSnapshotMsg);
		} catch (PharosException e1) {
			e1.printStackTrace();
			Logger.logErr("ERROR: Failed to send message.");
			return null;
		}
		
		BufferedImage result = null;
		
		// Wait until an ack arrives...
		synchronized(this) {
			if (rcvMsg == null) {
				Logger.log("Waiting for CameraSnapshotMsg...");
				try {
					//wait(MAX_ACK_LATENCY);
					
					// TODO: abort this wait if network connectivity to robot is broken.
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			if (rcvMsg != null) {
				if (rcvMsg instanceof CameraSnapshotMsg) {
					CameraSnapshotMsg ackMsg = (CameraSnapshotMsg)rcvMsg;
					Logger.log("CameraSnapshotMsg received, success = " + ackMsg.getSuccess());
					if (ackMsg.getSuccess()) {
						try {
							result = ackMsg.getImage();
						} catch(IOException e) {
							e.printStackTrace();
							Logger.logErr("Failed to get image from CameraSnapshotMsg, error = " + e.getMessage());
						}
					} else
						Logger.logErr("CameraSnapshotMsg.getSuccess() returned false.");
				} else
					Logger.logErr("Ack received but is of wrong type: " + rcvMsg);
				rcvMsg = null;
			} else
				Logger.logErr("Ack not received...");
		}
		
		return result;
	}
	
//	private void logErr(String msg) {
//		String result = "CmdExec: " + msg;
//		System.err.println(result);
//		if (flogger != null)
//			flogger.log(result);
//	}
//	
//	private void log(String msg) {
//		String result = "CmdExec: " + msg;
//		if (System.getProperty ("PharosMiddleware.debug") != null)
//			System.out.println(result);
//		if (flogger != null)
//			flogger.log(result);
//	}
}
