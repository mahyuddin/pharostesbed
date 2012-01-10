package pharoslabut.demo.simonsays;

import java.awt.Image;
//import java.io.BufferedReader;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
//import java.util.Map.Entry;

import pharoslabut.cpsAssert.AssertionRequestMsg;
import pharoslabut.cpsAssert.AssertionRequestThread;
//import pharoslabut.cpsAssert.AssertionResponseMsg;
//import pharoslabut.cpsAssert.CPSAssertSensor;
//import pharoslabut.cpsDemo.CPSRobot;
import pharoslabut.demo.simonsays.io.*;
import pharoslabut.exceptions.PharosException;
import pharoslabut.io.*;
import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.MotionArbiter;
import pharoslabut.sensors.CricketData;
//import pharoslabut.sensors.CricketDataListener;
//import pharoslabut.sensors.CricketInterface;
import pharoslabut.sensors.camera.axis.*;
import playerclient3.structures.PlayerPoint3d;
//import playerclient3.structures.position2d.PlayerPosition2dGeom;

/**
 * This server runs on the robot, and accepts camera commands from the client.
 * These commands include pan, tilt, and taking a snapshot.
 * 
 * @author Chien-Liang Fok
 * @author Kevin Boos
 * 
 */
public class SimonSaysServer implements MessageReceiver {
	public static final int IMAGE_WIDTH = 640;
	public static final int IMAGE_HEIGHT = 480;
//	public static String cricketSerialPort = "/dev/ttyUSB1";
//	public static String cricketFile = "cricketBeacons.txt";
	public static boolean noClient = false;
	
	/**
	 * the list of Cricket Mote beacons and their corresponding poses (positional coordinates)
	 */
	Map<String, PlayerPoint3d> cricketPositions = new HashMap<String, PlayerPoint3d>();
	
	/**
	 * the list of Cricket Mote beacons currently connected to this robot's Cricket Mote Listener
	 */
	Map<String, ArrayList<CricketData>> cricketBeacons = Collections.synchronizedMap(new HashMap<String, ArrayList<CricketData>>());
	
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
	
	
//	public static BeaconDataCollector bdc;
	
	
	/**
	 * The constructor.
	 * 
	 * @param pServerIP The IP address of the server.
	 * @param pServerPort The port of the server.
	 * @param port The port on which to listen for demo client messages.
	 * @param cameraIP The IP address of the camera.
	 * @param mobilityPlane The type of mobility plane to use.
	 */
	public SimonSaysServer(String pServerIP, int pServerPort, int port, String mcuPort, String cameraIP, MotionArbiter.MotionType mobilityPlane) {
		
		
		// TODO: Support multiple types of robots.
		ri = new CreateRobotInterface(pServerIP, pServerPort);
		
		// Create the MCU interface...
		mcu = new MCUInterface(mcuPort);
		
		String cameraURL = "http://" + cameraIP + "/axis-cgi/jpg/image.cgi?resolution=" + IMAGE_WIDTH + "x" + IMAGE_HEIGHT;
		camera = new AxisCameraInterface(cameraURL, "root", "longhorn");
		
		// Open the server port and start receiving messages...
		new TCPMessageReceiver(this, port);
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
		else if (msg instanceof AssertionRequestMsg) 
			handleAssertionRequestMsg((AssertionRequestMsg)msg);
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
//		if (!noClient) {
//			bdc.startTimer();
//			System.out.println("=== started BDC timer");
//		}
		double dist = moveMsg.getDist();
		Logger.log("Moving robot " + dist + " meters...");
		boolean result = ri.move(dist);
		Logger.log("Done moving robot, sending ack, result = " + result + "...");
		sendAck(result, moveMsg); // success
		
//		if (!noClient) {
//			bdc.stopTimer(); // collects beacon data for a single movement cmd
//			System.out.println("=== stopped BDC timer");
//		}
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
	
	
	/**
	 * Performs the actions that should be taken when a AssertionRequestMsg is received.
	 * 
	 * @param sdMsg The AssertionRequestMsg received from the client
	 */
	private void handleAssertionRequestMsg(AssertionRequestMsg arMsg) {
		arMsg.msgReceived();		

		// TODO create new thread to handle it
		AssertionRequestThread arThr = new AssertionRequestThread(arMsg, sender, false);
		arThr.start();
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
		print("\t-port <port number>: This server's port number (default 8887)");
		print("\t-mcuPort <port name>: The serial port on which the MCU is attached (default /dev/ttyS0)");
		print("\t-cameraIP <camera IP address>: The IP address of the camera (default 192.168.0.20)");
		print("\t-mobilityPlane <traxxas|segway|create>: The type of mobility plane being used (default traxxas)");
		print("\t-log <file name>: name of file in which to save results (default SimonSaysServer.log)");
//		print("\t-cricketFile <file name>: name of file where Cricket Beacon IDs and coordinates are stored (default cricketBeacons.txt)");
//		print("\t-cricketPort <port number>: tty port where the Cricket Listener is connected (default /dect/ttyUSB1");
		print("\t-noClient: this option allows you to manually control the robot without the SimonSaysClient (default false)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		int port = 8887;
		String pServerIP = "localhost";
		int pServerPort = 6665;
		String logFile = "SimonSaysServer.log";
		String mcuPort = "/dev/ttyS0";
		String cameraIP = "192.168.0.20";
		MotionArbiter.MotionType mobilityPlane = MotionArbiter.MotionType.MOTION_IROBOT_CREATE;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-pServer")) { // player server IP address
					pServerIP = args[++i];
				}
				else if (args[i].equals("-pPort")) { // player server TCP port
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
//				else if (args[i].equals("-cricketFile")) {
//					cricketFile = args[++i];
//				}
//				else if (args[i].equals("-cricketPort")) {
//					cricketSerialPort = args[++i];
//				}
				else if (args[i].equals("-noClient")) {
					noClient = true;
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
