package pharoslabut.tests;

import pharoslabut.logger.CompassLogger;
import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.MotionArbiter;
import pharoslabut.tasks.MotionTask;
import pharoslabut.tasks.Priority;
import playerclient3.PlayerClient;
import playerclient3.PlayerException;
import playerclient3.Position2DInterface;
import playerclient3.structures.PlayerConstants;

/**
 * Moves the robot in circles while logging the compass data.
 * This should result in a saw-tooth graph.
 * 
 * @author Chien-Liang Fok
 */
public class CompassCircleTest {
	public static final int ROBOT_REFRESH_PERIOD = 500; // interval of sending commands to robot in ms
	public static final int COMPASS_LOG_PERIOD = 100; // in milliseconds
	
	private PlayerClient client = null;
	
	/**
	 * The constructor. 
	 * 
	 * @param serverIP The IP address of the robot.
	 * @param serverPort The port on which the robot is listening.
	 * @param time The duration in milliseconds that the robot should move in circles.
	 * @param compassIndex The index of the compass.
	 * @param logFileName The log file in which to save results.
	 * @param showGUI Whether to show the GUI.
	 * @param mobilityPlane The type of mobility plane being used.
	 * @param speed The speed in m/s at which to move.
	 * @param turnAngle The heading in which to turn in degrees.
	 * @param getStatusMsgs Whether to subscribe to status messages sent from the Proteus MCU.
	 */
	public CompassCircleTest(String serverIP, int serverPort, int time, int compassIndex,
			boolean showGUI, MotionArbiter.MotionType mobilityPlane, 
			double speed, double turnAngle, boolean getStatusMsgs) 
	{
		
		try {
			Logger.log("Connecting to server " + serverIP + ":" + serverPort);
			client = new PlayerClient(serverIP, serverPort);
		} catch(PlayerException e) {
			Logger.logErr("Could not connect to player server: ");
			Logger.logErr("    [ " + e.toString() + " ]");
			System.exit (1);
		}
		
		Logger.log("Subscribing to motor interface and creating motion arbiter");
		Position2DInterface motors = client.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (motors == null) {
			Logger.logErr("motors is null");
			System.exit(1);
		}
		MotionArbiter motionArbiter = new MotionArbiter(mobilityPlane, motors);
		
		Logger.log("Start the robot moving in circles...");
		MotionTask circleTask = new MotionTask(Priority.SECOND, speed, Math.toRadians(turnAngle));
		if (motionArbiter.submitTask(circleTask))
			Logger.log("Circular motion task accepted...");
		
		Logger.log("Creating a CompassLogger...");
		CompassLogger compassLogger;
		compassLogger = new CompassLogger(serverIP, serverPort, compassIndex, showGUI, getStatusMsgs);
		
		Logger.log("Starting to log compass readings...");
		if (compassLogger.start()) {
			synchronized(this) {
				try {
					if (time > 0) {
						Logger.log("logging for " + time + " seconds...");
						wait(time*1000);
					} else {
						wait(); // wait forever
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Logger.log("Stopping to log compass readings...");
			compassLogger.stop();
		} else 
			Logger.logErr("Failed to start logging compass data...");
		
		Logger.log("Stopping the robot...");
		motionArbiter.revokeTask(circleTask); // stop moving in circles
		
		Logger.log("Done!");
	}

//	private void logErr(String msg) {
//		String result = "CompassCircleTest: " + msg;
//		System.err.println(result);
//		if (flogger != null)
//			flogger.log(result);
//	}
//	
//	private void log(String msg) {
//		String result = "CompassCircleTest: " + msg;
//		System.out.println(result);
//		if (flogger != null)
//			flogger.log(result);
//	}
//	
	private static void usage() {
		System.err.println("Usage: " + CompassCircleTest.class.getName() + " <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
		System.err.println("\t-compassIndex <index number>: The index of the compass device (default 1)");
		System.err.println("\t-time <period in s>: duration of test (default infinity)");
		System.err.println("\t-log <file name>: name of file in which to save results (default null)");
		System.err.println("\t-gui: display GUI (default not shown)");
		System.err.println("\t-mobilityPlane <traxxas|segway|create>: The type of mobility plane being used (default traxxas)");
		System.err.println("\t-speed <speed in m/s>: the speed at which the robot should move (default 0.6)");
		System.err.println("\t-turnAngle <angle in degrees>: The angle in which to turn, negative means right (default -20)");
		System.err.println("\t-getStatusMsgs: whether to subscribe to the interface that provides MCU status messages (default false)");
	}
	
	public static void main(String[] args) {
		int time = 0;
		String serverIP = "localhost";
		int serverPort = 6665;
		int compassIndex = 1;
		double speed = 0.6;
		double turnAngle = -20;
		boolean showGUI = false;
		boolean getStatusMsgs = false;
		MotionArbiter.MotionType mobilityPlane = MotionArbiter.MotionType.MOTION_TRAXXAS;

		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-server")) {
					serverIP = args[++i];
				} 
				else if (args[i].equals("-port")) {
					serverPort = Integer.valueOf(args[++i]);
				} 
				else if (args[i].equals("-time")) {
					time = Integer.valueOf(args[++i]);
				} 
				else if (args[i].equals("-compassIndex")) {
					compassIndex = Integer.valueOf(args[++i]);
				}
				else if (args[i].equals("-log")) {
					Logger.setFileLogger(new FileLogger(args[++i], false));
				}
				else if (args[i].equals("-gui")) {
					showGUI = true;
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
				else if (args[i].equals("-speed")) {
					speed = Double.valueOf(args[++i]);
				}
				else if (args[i].equals("-turnAngle")) {
					turnAngle = Double.valueOf(args[++i]);
				}
				else if (args[i].equals("-getStatusMsgs")) {
					getStatusMsgs = true;
				} 
				else if (args[i].equals("-debug") || args[i].equals("-d"))
					System.setProperty ("PharosMiddleware.debug", "true");
				else {
					System.err.println("ERROR: Unknown option " + args[i]);
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
		System.out.println("Time: " + time + "s");
		System.out.println("Compass Index: " + compassIndex);
		System.out.println("ShowGUI: " + showGUI);
		System.out.println("Mobility Plane: " + mobilityPlane);
		System.out.println("Speed: " + speed);
		System.out.println("Turn Angle: " + turnAngle);
		
		new CompassCircleTest(serverIP, serverPort, time, compassIndex, showGUI, mobilityPlane,
				speed, turnAngle, getStatusMsgs);
	}
}
