package pharoslabut.tests;

import pharoslabut.MotionArbiter;
import pharoslabut.logger.CompassLogger;
import pharoslabut.logger.FileLogger;
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
	private FileLogger flogger;
	
	public CompassCircleTest(String serverIP, int serverPort, int time, 
			String fileName, boolean showGUI, MotionArbiter.MotionType mobilityPlane, 
			double speed, double turnAngle, boolean getStatusMsgs) 
	{
		
		if (fileName != null)
			this.flogger = new FileLogger(fileName, false);
		
		try {
			log("Connecting to server " + serverIP + ":" + serverPort);
			client = new PlayerClient(serverIP, serverPort);
		} catch(PlayerException e) {
			log("Error connecting to Player: ");
			log("    [ " + e.toString() + " ]");
			System.exit (1);
		}
		
		log("Subscribing to motor interface...");
		Position2DInterface motorInterface = client.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		
		if (motorInterface == null) {
			log("motors is null");
			System.exit(1);
		}
		
		log("Creating motion arbiter...");
		MotionArbiter motionArbiter = new MotionArbiter(mobilityPlane, motorInterface);
		
		log("Moving the robot in circles...");
		MotionTask circleTask = new MotionTask(Priority.SECOND, speed, Math.toRadians(turnAngle));
		motionArbiter.submitTask(circleTask);
		
		log("Creating a CompassLogger...");
		CompassLogger compassLogger = new CompassLogger(serverIP, serverPort, 1 /* device index */, 
				flogger, showGUI, getStatusMsgs);
		
		log("Starting to log compass readings...");
		if (compassLogger.start()) {
			synchronized(this) {
				try {
					if (time > 0) {
						wait(time*1000);
					} else {
						wait(); // wait forever
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			compassLogger.stop();
		}
		motionArbiter.revokeTask(circleTask); // stop moving in circles
		log("Done!");
	}
	
	private void log(String msg) {
		String result = "CompassCircleTest: " + msg;
		System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
	
	private static void usage() {
		System.err.println("Usage: pharoslabut.tests.CompassCircleTest <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
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
		String fileName = null;
		String serverIP = "localhost";
		int serverPort = 6665;
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
				else if (args[i].equals("-log")) {
					fileName = args[++i];
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
		System.out.println("Time: " + time + "s");
		System.out.println("Log: " + fileName);
		System.out.println("ShowGUI: " + showGUI);
		System.out.println("Mobility Plane: " + mobilityPlane);
		System.out.println("Speed: " + speed);
		System.out.println("Turn Angle: " + turnAngle);
		
		new CompassCircleTest(serverIP, serverPort, time, fileName, showGUI, mobilityPlane,
				speed, turnAngle, getStatusMsgs);
	}
}
