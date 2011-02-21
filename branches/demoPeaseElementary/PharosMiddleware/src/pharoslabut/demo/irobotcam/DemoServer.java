package pharoslabut.demo.irobotcam;

import pharoslabut.MotionArbiter;
import pharoslabut.io.*;
import pharoslabut.logger.FileLogger;
import pharoslabut.tasks.MotionTask;
import pharoslabut.tasks.Priority;
import playerclient.PlayerClient;
import playerclient.PlayerException;
import playerclient.Position2DInterface;
import playerclient.structures.PlayerConstants;

/**
 * This server runs on the robot, and accepts camera commands from the client.
 * These commands include pan, tilt, and taking a snapshot.
 * 
 * @author Chien-Liang Fok
 */
public class DemoServer implements MessageReceiver {
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
	 * The constructor.
	 * 
	 * @param pServerIP The IP address of the server.
	 * @param pServerPort The port of the server.
	 * @param port The port on which to listen for demo client messages.
	 * @param fileName The log file name for recording execution state.
	 */
	public DemoServer(String pServerIP, int pServerPort, int port, String mcuPort, String fileName) {
		
		// Create the file logger if necessary...
		if (fileName != null) {
			flogger = new FileLogger(fileName);
		}
		
		ri = new RobotInterface(pServerIP, pServerPort, flogger);
		
		// Create the MCU interface...
		mcu = new MCUInterface(mcuPort, flogger);
		
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
		mcu.setCameraPan(panMsg.getPanAngle());
		sendAck(true, panMsg.getClientHandler()); // success
	}
	
	private void handleCameraTiltMsg(CameraTiltMsg tiltMsg) {
		mcu.setCameraTilt(tiltMsg.getPanAngle());
		sendAck(true, tiltMsg.getClientHandler()); // success
	}
	
	private void handleCameraTakeSnapshotMsg(CameraTakeSnapshotMsg takeSnapshotMsg) {
		
		// take a snapshot...
		
		
		// Package snapshot into a CameraSnapshotMsg
		CameraSnapshotMsg csm = new CameraSnapshotMsg(true /* successful */);
		
		// send resulting CameraSnapshotMsg to client...
		ClientHandler ch = takeSnapshotMsg.getClientHandler();
		ch.sendMsg(csm);
	}
	
	private void handleRobotMoveMsg(RobotMoveMsg moveMsg) {
		double dist = moveMsg.getDist();
		ri.move(dist);
		sendAck(true, moveMsg.getClientHandler()); // success
	}
	
	private void handleRobotTurnMsg(RobotTurnMsg turnMsg) {
		double angle = turnMsg.getAngle() / 180 * Math.PI;
		ri.turn(angle);
		sendAck(true, turnMsg.getClientHandler()); // success
	}
	
	private void sendAck(boolean success, ClientHandler ch) {
		CmdDoneMsg cdm = new CmdDoneMsg(success);
		ch.sendMsg(cdm);
	}
	
	private void log(String msg) {
		String result = "DemoServer: " + msg;
		System.out.println(result);
		if (flogger != null) {
			flogger.log(result);
		}
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
		print("\t-file <file name>: name of file in which to save results (default DemoServer.log)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		int port = 8887;
		String pServerIP = "localhost";
		int pServerPort = 6665;
		String fileName = "DemoServer.log";
		String mcuPort = "/dev/ttyS0";
		
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
		
		new DemoServer(pServerIP, pServerPort, port, mcuPort, fileName);
	}
}
