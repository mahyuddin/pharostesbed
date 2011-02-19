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
	
	/**
	 * This is the speed in m/s at which to move the robot.
	 */
	public static final double ROBOT_SPEED = 0.5;
	
	private FileLogger flogger = null;
	private PlayerClient pclient = null;
	private TCPMessageReceiver rcvr;
	private TCPMessageSender sndr;
	private MotionArbiter motionArbiter;
	
	/**
	 * The constructor.
	 * 
	 * @param pServerIP The IP address of the server.
	 * @param pServerPort The port of the server.
	 * @param port The port on which to listen for demo client messages.
	 * @param fileName The log file name for recording execution state.
	 */
	public DemoServer(String pServerIP, int pServerPort, int port, String fileName) {
		
		// Create the file logger if necessary...
		if (fileName != null) {
			flogger = new FileLogger(fileName);
		}
		
		// Connect to the Player server...
		try {
			pclient = new PlayerClient(pServerIP, pServerPort);
		} catch(PlayerException e) {
			log("Error connecting to Player: ");
			log("    [ " + e.toString() + " ]");
			System.exit (1);
		}
		
		// Get the Position2D interface for controlling the robot's movements...
		Position2DInterface motors = pclient.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (motors == null) {
			log("motors is null");
			System.exit(1);
		}
		
		// Create the motion arbiter...
		motionArbiter = new MotionArbiter(MotionArbiter.MotionType.MOTION_IROBOT_CREATE, motors);
		motionArbiter.setFileLogger(flogger);
		
		// Open the server port and start receiving messages...
		rcvr = new TCPMessageReceiver(this, port);
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
		
	}
	
	private void handleCameraTiltMsg(CameraTiltMsg tiltMsg) {
		
	}
	
	private void handleCameraTakeSnapshotMsg(CameraTakeSnapshotMsg takeSnapshotMsg) {
		
	}
	
	private void handleRobotMoveMsg(RobotMoveMsg moveMsg) {
		double dist = moveMsg.getDist();
		int duration = (int)(dist / ROBOT_SPEED * 1000); // in milliseconds
		MotionTask currTask;
		
		currTask = new MotionTask(Priority.SECOND, ROBOT_SPEED, 0 /* heading */);
		log("Submitting: " + currTask);
		motionArbiter.submitTask(currTask);
		
		pause(duration);
		
		currTask = new MotionTask(Priority.FIRST, MotionTask.STOP_VELOCITY, MotionTask.STOP_HEADING);
		log("Submitting: " + currTask);
		motionArbiter.submitTask(currTask);
		
		sendAck(true); // success
	}
	
	private void handleRobotTurnMsg(RobotTurnMsg turnMsg) {
		
	}
	
	private void sendAck(boolean success) {
		CmdDoneMsg cdm = new CmdDoneMsg(success);
		
	}
	
	/**
	 * Pauses the calling thread for the specified duration.
	 * 
	 * @param duration The time to pause the thread in milliseconds.
	 */
	private void pause(int duration) {
		synchronized(this) {
			try {
				wait(duration);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
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
		print("Usage: pharoslabut.demo.irobotcam <options>\n");
		print("Where <options> include:");
		print("\t-pServer <ip address>: The IP address of the Player Server (default localhost)");
		print("\t-pPort <port number>: The Player Server's port number (default 6665)");
		print("\t-port <port number>: The Demo Server's port bnumber (default 8887)");
		print("\t-file <file name>: name of file in which to save results (default DemoServer.log)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		int port = 8887;
		String pServerIP = "localhost";
		int pServerPort = 6665;
		String fileName = "DemoServer.log";
		
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
		
		new DemoServer(pServerIP, pServerPort, port, fileName);
	}
}
