package pharoslabut.cps;

import java.net.InetAddress;
import java.util.*;

//import pharoslabut.demo.simonsays.io.*;
import pharoslabut.exceptions.PharosException;
import pharoslabut.io.*;
import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.MotionArbiter;
import pharoslabut.sensors.CricketData;
import playerclient3.structures.PlayerPoint3d;

/**
 * This server runs on the robot, and accepts commands from the client. 
 * This class instantiates a CPSSensorServer
 * 
 * @author Kevin Boos
 * 
 */
public class CPSServer implements MessageReceiver {
	public static String cricketSerialPort = "/dev/ttyUSB1";
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
	 * The constructor.
	 * 
	 * @param pServerIP The IP address of the server.
	 * @param pServerPort The port of the server.
	 * @param port The port on which to listen for demo client messages.
	 * @param cameraIP The IP address of the camera.
	 * @param mobilityPlane The type of mobility plane to use.
	 */
	public CPSServer(String pServerIP, int pServerPort, int port, String mcuPort, 
			String cricketFile, MotionArbiter.MotionType mobilityPlane) {
		
		// TODO: Support multiple types of robots.
		ri = new CreateRobotInterface(pServerIP, pServerPort);
		
		// Open the server port and start receiving messages...
		new TCPMessageReceiver(this, port);
	}
	
	
	
	
	//@Override
	public void newMessage(Message msg) {		
		// This is called whenever a new message is received.
		if (msg instanceof RobotMoveMsg)
			handleRobotMoveMsg((RobotMoveMsg)msg);
		else if (msg instanceof RobotTurnMsg)
			handleRobotTurnMsg((RobotTurnMsg)msg);
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
			sendAck(true, null, playerCtrlMsg);
		} else if (playerCtrlMsg.getCmd() == PlayerControlCmd.START) {
			// signifies that a new client has connected to the server
			clients.put(playerCtrlMsg.getReplyAddr(), playerCtrlMsg.getPort());
			Logger.log("Client added: " + playerCtrlMsg.getReplyAddr() + ":" + playerCtrlMsg.getPort());
			sendAck(true, null, playerCtrlMsg);
		} else 
			Logger.log("Unknown PlayerControlMsg, cmd = " + playerCtrlMsg.getCmd());
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
		sendAck(result, null, moveMsg); // success
		
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
		sendAck(result, null, turnMsg); // success
	}
	
	/**
	 * Sends an ack back to the client.
	 * 
	 * @param success Whether the operation was successful.
	 * @param am The message to ack.
	 */
	private void sendAck(boolean success, String info, AckableMessage am) {
		CmdDoneMsg cdm = new CmdDoneMsg(success);
		if (info != null) // if there's also an info String
			cdm = new CmdDoneMsg(success, info);
		
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
		String assertionData = arMsg.getSensorType() + ", " + arMsg.getIneq() + ", " + arMsg.getActualValues();
		
		// TODO parse data out of arMsg
		// TODO call the appropriate assertion
		// TODO (probably need to change the assert methods to return a String of the assertion results)

		try {
			sender.sendMessage(arMsg.getReplyAddr(), arMsg.getPort(), new AssertionResponseMsg("Received Assertion: " + assertionData));
		} catch (PharosException e) {
			e.printStackTrace();
			Logger.logErr("Failed to send AssertionResponseMessage for " + arMsg + ", error=" + e);
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
		print("Usage: " + CPSServer.class.getName() + " <options>\n");
		print("Where <options> include:");
		print("\t-pServer <ip address>: The IP address of the Player Server (default localhost)");
		print("\t-pPort <port number>: The Player Server's port number (default 6665)");
		print("\t-port <port number>: The Demo Server's port bnumber (default 8887)");
		print("\t-mcuPort <port name>: The serial port on which the MCU is attached (default /dev/ttyS0)");
		print("\t-cameraIP <camera IP address>: The IP address of the camera (default 192.168.0.20)");
		print("\t-mobilityPlane <traxxas|segway|create>: The type of mobility plane being used (default traxxas)");
		print("\t-log <file name>: name of file in which to save results (default CPSServer.log)");
		print("\t-cricketFile <file name>: name of file where Cricket Beacon IDs and coordinates are stored (default cricketBeacons.txt)");
		print("\t-cricketPort <port number>: tty port where the Cricket Listener is connected (default /dect/ttyUSB1");
		print("\t-noClient: this option allows you to manually control the robot without the CPSClient (default false)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		int port = 8887;
		String pServerIP = "localhost";
		int pServerPort = 6665;
		String logFile = "CPSServer.log";
		String mcuPort = "/dev/ttyS0";
		String cricketFile = "cricketBeacons.txt";
		MotionArbiter.MotionType mobilityPlane = MotionArbiter.MotionType.MOTION_IROBOT_CREATE;
		
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
				else if (args[i].equals("-cricketFile")) {
					cricketFile = args[++i];
				}
				else if (args[i].equals("-cricketPort")) {
					cricketSerialPort = args[++i];
				}
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
		
		new CPSServer(pServerIP, pServerPort, port, mcuPort, cricketFile, mobilityPlane);
	}

}

