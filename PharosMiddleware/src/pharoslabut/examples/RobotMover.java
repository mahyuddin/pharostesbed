package pharoslabut.examples;

import pharoslabut.MotionArbiter;
import pharoslabut.tasks.Priority;
import pharoslabut.tasks.MotionTask;
import pharoslabut.logger.*;
import playerclient3.PlayerClient;
import playerclient3.PlayerException;
import playerclient3.Position2DInterface;
import playerclient3.structures.PlayerConstants;

public class RobotMover {
	private PlayerClient client = null;
	private FileLogger flogger = null;
	
	public RobotMover(String serverIP, int serverPort,	String fileName, String robotType,
			double speed, double angle, long duration) 
	{
		
		// Connect to the player server...
		try {
			client = new PlayerClient(serverIP, serverPort);
		} catch(PlayerException e) {
			log("Error connecting to Player: ");
			log("    [ " + e.toString() + " ]");
			System.exit (1);
		}
		
		// Subscribe to robot motors...
		Position2DInterface motors = client.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (motors == null) {
			log("motors is null");
			System.exit(1);
		}
		
		// Create a motion arbiter...
		MotionArbiter motionArbiter = null;
		if (robotType.equals("traxxas"))
			motionArbiter = new MotionArbiter(MotionArbiter.MotionType.MOTION_TRAXXAS, motors);
		else if (robotType.equals("create"))
			motionArbiter = new MotionArbiter(MotionArbiter.MotionType.MOTION_IROBOT_CREATE, motors);
		else
			motionArbiter = new MotionArbiter(MotionArbiter.MotionType.MOTION_SEGWAY_RMP50, motors);
		
		// Enable logging...
		if (fileName != null) {
			flogger = new FileLogger(fileName);
			motionArbiter.setFileLogger(flogger);
		}
		
		MotionTask currTask;
		
		currTask = new MotionTask(Priority.SECOND, speed, angle);
		log("Submitting: " + currTask);
		motionArbiter.submitTask(currTask);
		
		log("Allowing robot to move for " + duration + "ms...");
		pause(duration);
		
		
		currTask = new MotionTask(Priority.SECOND, MotionTask.STOP_SPEED, MotionTask.STOP_HEADING);
		log("Submitting: " + currTask);
		motionArbiter.submitTask(currTask);
		
		log("Test complete!");
		System.exit(0);
	}
	
	/**
	 * Pauses the calling thread a certain amount of time.
	 * 
	 * @param duration The pause duration in milliseconds.
	 */
	private void pause(long duration) {
		synchronized(this) {
			try {
				wait(duration);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void log(String msg) {
		String result = "RobotMover: " + msg;
		System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
	
	private static void usage() {
		System.err.println("Usage: pharoslabut.examples.RobotMover <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
		System.err.println("\t-log <file name>: name of file in which to save results (default RobotMover.log)");
		System.err.println("\t-robot <robot type>: The type of robot, either traxxas, segway, or create (default traxxas)");
		System.err.println("\t-speed <speed>: The speed in meters per second (default 0.5)");
		System.err.println("\t-angle <angle>: The angle in radians (default 0)");
		System.err.println("\t-duration <duration>: The duration in milliseconds (default 1000)");
	}
	
	public static void main(String[] args) {
		String fileName = "RobotMover.log";
		String serverIP = "localhost";
		int serverPort = 6665;
		String robotType = "traxxas";
		double speed = 0.5;
		double angle = 0;
		long duration = 1000;

		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-server"))
					serverIP = args[++i];
				else if (args[i].equals("-port"))
					serverPort = Integer.valueOf(args[++i]);
				else if (args[i].equals("-log"))
					fileName = args[++i];
				else if (args[i].equals("-robot")) {
					robotType = args[++i];
				}
				else if (args[i].equals("-speed"))
					speed = Double.valueOf(args[++i]);
				else if (args[i].equals("-angle"))
					angle = Double.valueOf(args[++i]);
				else if (args[i].equals("-duration"))
					duration = Long.valueOf(args[++i]);
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
		
		System.out.println("Server IP: " + serverIP);
		System.out.println("Server port: " + serverPort);
		System.out.println("File: " + fileName);
		System.out.println("RobotType: " + robotType);
		System.out.println("Speed: " + speed);
		System.out.println("Angle: " + angle);
		System.out.println("Duration: " + duration);
		
		new RobotMover(serverIP, serverPort, fileName, robotType, speed, angle, duration);
	}
}