package pharoslabut.demo.irobotcam;

import java.awt.Image;

import pharoslabut.io.*;
import pharoslabut.logger.FileLogger;
import pharoslabut.sensors.camera.axis.*;

/**
 * This server runs on the robot, and accepts camera commands from the client.
 * These commands include pan, tilt, and taking a snapshot.
 * 
 * @author Chien-Liang Fok
 */
public class DemoServer implements MessageReceiver {
	public static final int IMAGE_WIDTH = 640;
	public static final int IMAGE_HEIGHT = 480;
	
	private FileLogger flogger = null;
	
	//private TCPMessageReceiver rcvr;
	
	/**
	 * This is for controlling the movements of the robot.
	 */
	private RobotInterface ri;
	
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
	 * @param fileName The log file name for recording execution state.
	 */
	public DemoServer(String pServerIP, int pServerPort, int port, String mcuPort, String cameraIP, String fileName) {
		
		// Create the file logger if necessary...
		if (fileName != null) {
			flogger = new FileLogger(fileName);
		}
		
		ri = new RobotInterface(pServerIP, pServerPort, flogger);
		
		// Create the MCU interface...
		mcu = new MCUInterface(mcuPort, flogger);
		
		String cameraURL = "http://" + cameraIP + "/axis-cgi/jpg/image.cgi?resolution=" + IMAGE_WIDTH + "x" + IMAGE_HEIGHT;
		camera = new AxisCameraInterface(cameraURL, "root", "longhorn", flogger);
		
		// Open the server port and start receiving messages...
		new TCPMessageReceiver(this, port);
	}
	
	@Override
	public void newMessage(Message msg) {
		// This is called whenever a new message is received.
		if (msg instanceof CameraPanMsg) {
			handleCameraPanMsg((CameraPanMsg)msg);
		}
		else if (msg instanceof CameraTiltMsg) {
			handleCameraTiltMsg((CameraTiltMsg)msg);
		}
		else if (msg instanceof CameraTakeSnapshotMsg) {
			handleCameraTakeSnapshotMsg((CameraTakeSnapshotMsg)msg);
		}
		else if (msg instanceof RobotMoveMsg) {
			handleRobotMoveMsg((RobotMoveMsg)msg);
		}
		else if (msg instanceof RobotTurnMsg) {
			handleRobotTurnMsg((RobotTurnMsg)msg);
		}
	}
	
	private void handleCameraPanMsg(CameraPanMsg panMsg) {
		log("Panning camera to " + panMsg.getPanAngle() + " degrees...");
		mcu.setCameraPan(panMsg.getPanAngle());
		sendAck(true, panMsg.getClientHandler()); // success
	}
	
	private void handleCameraTiltMsg(CameraTiltMsg tiltMsg) {
		log("Tilting camera to " + tiltMsg.getTiltAngle() + " degrees...");
		mcu.setCameraTilt(tiltMsg.getTiltAngle());
		sendAck(true, tiltMsg.getClientHandler()); // success
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
			csm.setImage(image, IMAGE_WIDTH, IMAGE_HEIGHT);
		} else {
			csm = new CameraSnapshotMsg(false /* not successful */);
		}
		
		// Send resulting CameraSnapshotMsg to client...
		log("Sending camera snapshot result to client...");
		ClientHandler ch = takeSnapshotMsg.getClientHandler();
		ch.sendMsg(csm);
	}
	
	private void handleRobotMoveMsg(RobotMoveMsg moveMsg) {
		double dist = moveMsg.getDist();
		log("Moving robot " + dist + " meters...");
		ri.move(dist);
		log("Done moving robot, sending ack...");
		sendAck(true, moveMsg.getClientHandler()); // success
	}
	
	private void handleRobotTurnMsg(RobotTurnMsg turnMsg) {
		double angle = turnMsg.getAngle() / 180 * Math.PI;
		log("Turning robot " + turnMsg.getAngle() + " degrees...");
		ri.turn(angle);
		log("Done turning robot, sending ack...");
		sendAck(true, turnMsg.getClientHandler()); // success
	}
	
	private void sendAck(boolean success, ClientHandler ch) {
		CmdDoneMsg cdm = new CmdDoneMsg(success);
		ch.sendMsg(cdm);
	}
	
	private void log(String msg) {
		String result = "DemoServer: " + msg;
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println("DemoServer: " + msg);
	}
	
	private static void usage() {
		print("Usage: pharoslabut.demo.irobotcam.DemoServer <options>\n");
		print("Where <options> include:");
		print("\t-pServer <ip address>: The IP address of the Player Server (default localhost)");
		print("\t-pPort <port number>: The Player Server's port number (default 6665)");
		print("\t-port <port number>: The Demo Server's port bnumber (default 8887)");
		print("\t-mcuPort <port name>: The serial port on which the MCU is attached (default /dev/ttyS0)");
		print("\t-cameraIP <camera IP address>: The IP address of the camera (default 192.168.0.20)");
		print("\t-file <file name>: name of file in which to save results (default DemoServer.log)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		int port = 8887;
		String pServerIP = "localhost";
		int pServerPort = 6665;
		String fileName = "DemoServer.log";
		String mcuPort = "/dev/ttyS0";
		String cameraIP = "192.168.0.20";
		
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
				else if (args[i].equals("-file")) {
					fileName = args[++i];
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
				else {
					System.setProperty ("PharosMiddleware.debug", "true");
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			usage();
			System.exit(1);
		}
		
		print("Demo Server port: " + port);
		print("Debug: " + ((System.getProperty ("PharosMiddleware.debug") != null) ? true : false));
		
		new DemoServer(pServerIP, pServerPort, port, mcuPort, cameraIP, fileName);
	}
}
