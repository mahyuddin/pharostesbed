package pharoslabut.demo.simonsays;

import java.awt.Image;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import pharoslabut.demo.simonsays.io.*;
import pharoslabut.exceptions.PharosException;
import pharoslabut.io.*;
import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.MotionArbiter;
import pharoslabut.sensors.CricketData;
import pharoslabut.sensors.CricketDataListener;
import pharoslabut.sensors.CricketInterface;
import pharoslabut.sensors.camera.axis.*;

/**
 * This server runs on the robot, and accepts camera commands from the client.
 * These commands include pan, tilt, and taking a snapshot.
 * 
 * @author Chien-Liang Fok
 * 
 */
public class SimonSaysServer implements MessageReceiver, CricketDataListener {
	public static final int IMAGE_WIDTH = 640;
	public static final int IMAGE_HEIGHT = 480;
	
	public static final String cricketSerialPort = "/dev/ttyUSB1";
	
	/**
	 * the list of clients currently connected to this server
	 */
	HashMap<InetAddress, Integer> clients = new HashMap<InetAddress, Integer>();
	
	/**
     * The connection back to the client.
     */
    private TCPMessageSender sender = TCPMessageSender.getSender();
	
	/**
	 * This is for controlling the movements of the robot.
	 */
	private CreateRobotInterface ri;
	
	/**
	 * This is for controlling the movements of the camera.
	 */
	private MCUInterface mcu;
	
	/**
	 * This is for taking snapshots from the camera.
	 */
	private AxisCameraInterface camera;
	
	/**
	 * The constructor.
	 * 
	 * @param pServerIP The IP address of the server.
	 * @param pServerPort The port of the server.
	 * @param port The port on which to listen for demo client messages.
	 * @param cameraIP The IP address of the camera.
	 * @param mobilityPlane The type of mobility plane to use.
	 */
	public SimonSaysServer(String pServerIP, int pServerPort, int port, String mcuPort, String cameraIP,
			MotionArbiter.MotionType mobilityPlane) {
		
		// TODO: Support multiple types of robots.
		ri = new CreateRobotInterface(pServerIP, pServerPort);
		
		// Create the MCU interface...
		mcu = new MCUInterface(mcuPort);
		
		String cameraURL = "http://" + cameraIP + "/axis-cgi/jpg/image.cgi?resolution=" + IMAGE_WIDTH + "x" + IMAGE_HEIGHT;
		camera = new AxisCameraInterface(cameraURL, "root", "longhorn");
		
		// add listener for cricket mote on USB1
		CricketInterface ci  = new CricketInterface(cricketSerialPort);
		ci.registerCricketDataListener(this);
		
		// Open the server port and start receiving messages...
		new TCPMessageReceiver(this, port);
	}
	
	@Override
	public void newCricketData(CricketData cd) {
		if (!cd.getConnection())
			return;
			
		CricketDataMsg cricketMsg = new CricketDataMsg(cd);
	
		// broadcast new CricketData to clients list
		Iterator<Entry<InetAddress, Integer>> iter = clients.entrySet().iterator();
	    while (iter.hasNext()) {
	    	Entry<InetAddress, Integer> pair = iter.next();
		    try {
		    	sender.sendMessage(pair.getKey(), pair.getValue(), cricketMsg);
		    }
		    catch (PharosException e) {
				e.printStackTrace();
				Logger.logErr("Failed to send Cricket Msg, " + cd.toString());
			}
	    }
	} 
	
	//@Override
	public void newMessage(Message msg) {		
		// This is called whenever a new message is received.
		if (msg instanceof CameraPanMsg)
			handleCameraPanMsg((CameraPanMsg)msg);
		else if (msg instanceof CameraTiltMsg)
			handleCameraTiltMsg((CameraTiltMsg)msg);
		else if (msg instanceof CameraTakeSnapshotMsg)
			handleCameraTakeSnapshotMsg((CameraTakeSnapshotMsg)msg);
		else if (msg instanceof RobotMoveMsg)
			handleRobotMoveMsg((RobotMoveMsg)msg);
		else if (msg instanceof RobotTurnMsg)
			handleRobotTurnMsg((RobotTurnMsg)msg);
//		else if (msg instanceof ResetPlayerMsg)
//			ri.stopPlayer();
		else if (msg instanceof PlayerControlMsg)
			handlePlayerControlMsg((PlayerControlMsg)msg);
		else
			Logger.log("Unkown message: " + msg);
	}
	
	private void handlePlayerControlMsg(PlayerControlMsg playerCtrlMsg) {
		if (playerCtrlMsg.getCmd() == PlayerControlCmd.STOP) {
			ri.stopPlayer();
			sendAck(true, playerCtrlMsg);
		} else if (playerCtrlMsg.getCmd() == PlayerControlCmd.START) {
			// signifies that a new client has connected to the server
			clients.put(playerCtrlMsg.getReplyAddr(), playerCtrlMsg.getPort());
			Logger.log("Client added: " + playerCtrlMsg.getReplyAddr() + ":" + playerCtrlMsg.getPort());
			sendAck(true, playerCtrlMsg);
		} else 
			Logger.log("Unknown PlayerControlMsg, cmd = " + playerCtrlMsg.getCmd());
	}
	
	private void handleCameraPanMsg(CameraPanMsg panMsg) {
		Logger.log("Panning camera to " + panMsg.getPanAngle() + " degrees...");
		mcu.setCameraPan(panMsg.getPanAngle());
		sendAck(true, panMsg); // success
	}
	
	private void handleCameraTiltMsg(CameraTiltMsg tiltMsg) {
		Logger.log("Tilting camera to " + tiltMsg.getTiltAngle() + " degrees...");
		mcu.setCameraTilt(tiltMsg.getTiltAngle());
		sendAck(true, tiltMsg); // success
	}
	
	private void handleCameraTakeSnapshotMsg(CameraTakeSnapshotMsg takeSnapshotMsg) {
		
		// Pause 2 second to allow camera to stop vibrating
		synchronized(this) {
			try {
				wait(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// Take a snapshot...
		Image image = camera.getSnapshot();
		
		CameraSnapshotMsg csm = null;
		if (image != null) {
			// Package snapshot into a CameraSnapshotMsg
			csm = new CameraSnapshotMsg(true /* successful */);
			try {
				csm.setImage(image, IMAGE_WIDTH, IMAGE_HEIGHT);
				Logger.log("Image size: " + csm.getImageSize() + " bytes");
			} catch (IOException e) {
				Logger.logErr("ERROR: Failed to save image in CameraSnapshotMsg: " + e.getMessage());
				csm.setSuccess(false);
				e.printStackTrace();
			}
		} else {
			csm = new CameraSnapshotMsg(false /* not successful */);
		}
		
		// Send resulting CameraSnapshotMsg to client...
		Logger.log("Sending camera snapshot result to client (success=" + csm.getSuccess() + ")...");
		try {
			sender.sendMessage(takeSnapshotMsg.getReplyAddr(), takeSnapshotMsg.getPort(), csm);
		} catch (PharosException e) {
			e.printStackTrace();
			Logger.logErr("ERROR: Failed to send CameraSnapshotMsg, error=" + e);
		}
	}
	
	/**
	 * Performs the actions that should be taken when a RobotMoveMsg is received.
	 * 
	 * @param moveMsg The move message.
	 */
	private void handleRobotMoveMsg(RobotMoveMsg moveMsg) {
		double dist = moveMsg.getDist();
		Logger.log("Moving robot " + dist + " meters...");
		boolean result = ri.move(dist);
		Logger.log("Done moving robot, sending ack, result = " + result + "...");
		sendAck(result, moveMsg); // success
	}
	
	/**
	 * Performs the actions that should be taken when a RobotTurnMsg is received.
	 * 
	 * @param moveMsg The turn message.
	 */
	private void handleRobotTurnMsg(RobotTurnMsg turnMsg) {
		double angle = turnMsg.getAngle() / 180 * Math.PI;
		Logger.log("Turning robot " + turnMsg.getAngle() + " degrees...");
		boolean result = ri.turn(angle);
		Logger.log("Done turning robot, sending ack, result = " + result + "...");
		sendAck(result, turnMsg); // success
	}
	
	/**
	 * Sends an ack back to the client.
	 * 
	 * @param success Whether the operation was successful.
	 * @param am The message to ack.
	 */
	private void sendAck(boolean success, AckableMessage am) {
		CmdDoneMsg cdm = new CmdDoneMsg(success);
		try {
			sender.sendMessage(am.getReplyAddr(), am.getPort(), cdm);
		} catch (PharosException e) {
			e.printStackTrace();
			Logger.logErr("Failed to send ack for " + am + ", error=" + e);
		}
	}
	
//	private void logErr(String msg) {
//		String result = "DemoServer: " + msg;
//		System.err.println(result);
//		if (flogger != null)
//			flogger.log(result);
//	}
//	
//	private void log(String msg) {
//		String result = "DemoServer: " + msg;
//		if (System.getProperty ("PharosMiddleware.debug") != null)
//			System.out.println(result);
//		if (flogger != null)
//			flogger.log(result);
//	}
	
	private static void print(String msg) {
		System.out.println("DemoServer: " + msg);
	}
	
	private static void usage() {
		print("Usage: " + SimonSaysServer.class.getName() + " <options>\n");
		print("Where <options> include:");
		print("\t-pServer <ip address>: The IP address of the Player Server (default localhost)");
		print("\t-pPort <port number>: The Player Server's port number (default 6665)");
		print("\t-port <port number>: The Demo Server's port bnumber (default 8887)");
		print("\t-mcuPort <port name>: The serial port on which the MCU is attached (default /dev/ttyS0)");
		print("\t-cameraIP <camera IP address>: The IP address of the camera (default 192.168.0.20)");
		print("\t-mobilityPlane <traxxas|segway|create>: The type of mobility plane being used (default traxxas)");
		print("\t-log <file name>: name of file in which to save results (default SimonSaysServer.log)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		int port = 8887;
		String pServerIP = "localhost";
		int pServerPort = 6665;
		String logFile = "SimonSaysServer.log";
		String mcuPort = "/dev/ttyS0";
		String cameraIP = "192.168.0.20";
		MotionArbiter.MotionType mobilityPlane = MotionArbiter.MotionType.MOTION_TRAXXAS;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-pServer")) {
					pServerIP = args[++i];
				}
				else if (args[i].equals("-pPort")) {
					pServerPort = Integer.valueOf(args[++i]);
				}
				else if (args[i].equals("-port")) {
					port = Integer.valueOf(args[++i]);
				}
				else if (args[i].equals("-log")) {
					logFile = args[++i];
				}
				else if (args[i].equals("-mcuPort")) {
					mcuPort = args[++i];
				}
				else if (args[i].equals("-cameraIP")) {
					cameraIP = args[++i];
				}
				else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				}
				else if (args[i].equals("-mobilityPlane")) {
					String mp = args[++i].toLowerCase();
					if (mp.equals("traxxas"))
						mobilityPlane = MotionArbiter.MotionType.MOTION_TRAXXAS;
					else if (mp.equals("segway"))
						mobilityPlane = MotionArbiter.MotionType.MOTION_SEGWAY_RMP50;
					else if (mp.equals("create"))
						mobilityPlane = MotionArbiter.MotionType.MOTION_IROBOT_CREATE;
					else {
						System.err.println("Unknown mobility plane " + mp);
						usage();
						System.exit(1);
					}
				}
				else {
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			usage();
			System.exit(1);
		}
		
		// Create the file logger if necessary...
		Logger.setFileLogger(new FileLogger(logFile));
		
		new SimonSaysServer(pServerIP, pServerPort, port, mcuPort, cameraIP, mobilityPlane);
	}

}
