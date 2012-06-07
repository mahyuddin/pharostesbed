package pharoslabut.tests;

import pharoslabut.sensors.*;
import pharoslabut.tasks.Priority;
import pharoslabut.tasks.MotionTask;
import pharoslabut.logger.*;
import pharoslabut.navigate.MotionArbiter;

import playerclient3.*;
import playerclient3.structures.*;
import playerclient3.structures.position2d.PlayerPosition2dData;

/**
 * Stresses the motor of the Proteus robot by making it move at varying speeds.
 * It initially starts slowly but ramps up the speed to the max speed.
 * 
 * @author Chien-Liang Fok
 */
public class MotorStressTest implements Position2DListener {
	private PlayerClient client = null;
	
	/**
	 * The constructor.
	 * 
	 * @param serverIP The IP address of the robot.
	 * @param serverPort The port on which the robot is listening.
	 * @param mobilityPlane The type of mobility plane being used.
	 * @param testStartDelay The number of seconds to wait before starting the test.
	 * @param maxSpeed The maximum speed in m/s at which to move.
	 * @param minSpeed The minimum speed in m/s at which to move.
	 * @param speedStep The steps in which to incrementally increase the speed being tested.
	 * @param duration The duration in milliseconds to move.
	 * @param heading The heading in which to turn in radians.
	 */
	public MotorStressTest(String serverIP, int serverPort,
			MotionArbiter.MotionType mobilityPlane, int testStartDelay,
			double minSpeed, double maxSpeed, double speedStep, int duration, double heading) 
	{
		
		try {
			Logger.logDbg("Connecting to server " + serverIP + ":" + serverPort);
			client = new PlayerClient(serverIP, serverPort);
		} catch(PlayerException e) {
			Logger.logErr("Could not connect to player server: ");
			Logger.logErr("    [ " + e.toString() + " ]");
			System.exit (1);
		}
		
		Logger.logDbg("Subscribing to motor interface and creating motion arbiter");
		Position2DInterface motors = client.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (motors == null) {
			Logger.logErr("Motors is null");
			System.exit(1);
		} else {
			// subscribe to MCU debug messages
			Position2DBuffer p2db = new Position2DBuffer(motors); 
			p2db.addPos2DListener(this);
			p2db.start();
		}
		MotionArbiter motionArbiter = new MotionArbiter(mobilityPlane, motors);
		
		MotionTask currTask;
		
//		Logger.log("Stopping the motor.");
		currTask = new MotionTask(Priority.FIRST, MotionTask.STOP_SPEED, MotionTask.STOP_STEERING_ANGLE);
		
		Logger.log("Starting motor stress test in " + testStartDelay + " seconds ...");
		while (testStartDelay-- > 0) {
			synchronized(this) { 
				try {
					wait(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (testStartDelay > 0) Logger.log(testStartDelay + "...");
		}
		
		
		Logger.log("Performing motor stress test in forward direction.");
		for (double currSpeed = minSpeed; currSpeed <= maxSpeed; currSpeed += speedStep) {
			currTask = new MotionTask(Priority.SECOND, currSpeed, heading);
			Logger.log("Moving the robot at " + currSpeed + "m/s with heading " + heading + " for " + duration + "ms.");
			motionArbiter.submitTask(currTask);
			pause(duration);
			
//			currTask = new MotionTask(Priority.FIRST, MotionTask.STOP_SPEED, MotionTask.STOP_HEADING);
//			Logger.log("Stopping the robot.");
//			motionArbiter.submitTask(currTask);
//			pause(3000);
		}
		
		currTask = new MotionTask(Priority.FIRST, MotionTask.STOP_SPEED, MotionTask.STOP_STEERING_ANGLE);
		Logger.log("Stopping the robot before testing reverse direction.");
		motionArbiter.submitTask(currTask);
		pause(3000);
		
		Logger.log("Performing motor stress test in reverse direction.");
		for (double currSpeed = minSpeed; currSpeed <= maxSpeed; currSpeed += speedStep) {
			currTask = new MotionTask(Priority.SECOND, -1*currSpeed, heading);
			Logger.log("Moving the robot at " + (-1*currSpeed) + "m/s with heading " + heading + " for " + duration + "ms.");
			motionArbiter.submitTask(currTask);
			pause(duration);
		}
		
		currTask = new MotionTask(Priority.FIRST, MotionTask.STOP_SPEED, MotionTask.STOP_STEERING_ANGLE);
		Logger.log("Stopping the robot.");
		motionArbiter.submitTask(currTask);
//		pause(3000);

		Logger.log("Test complete!");
		System.exit(0);
	}
	
	@Override
	public void newPlayerPosition2dData(PlayerPosition2dData data) {
		PlayerPose2d pp = data.getPos();
		Logger.log("Odometry Data: x=" + pp.getPx() + ", y=" + pp.getPy() + ", a=" + pp.getPa() + ", vel=" + data.getVel() + ", stall=" + data.getStall());
	}
	
	private void pause(int duration) {
		synchronized(this) {
			try {
				wait(duration);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
//	private void logErr(String msg) {
//		String result = "MotorStressTest: " + msg;
//		System.err.println(result);
//		if (flogger != null)
//			flogger.log(result);
//	}
//	
//	private void log(String msg) {
//		String result = "MotorStressTest: " + msg;
//		System.out.println(result);
//		if (flogger != null)
//			flogger.log(result);
//	}
	
	private static void usage() {
		System.err.println("Usage: " + MotorStressTest.class.getName() + " <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
		System.err.println("\t-log <file name>: name of file in which to save results (default null)");
		System.err.println("\t-mobilityPlane <traxxas|segway|create>: The type of mobility plane being used (default traxxas)");
		System.err.println("\t-testStartDelay <delay in seconds>: The number of seconds to wait before starting the test (default 4)");
		System.err.println("\t-minSpeed: the minimum speed at which to move the robot in m/s (default 0.5)");
		System.err.println("\t-maxSpeed: the maximum speed at which to move the robot in m/s (default 3.5)");
		System.err.println("\t-speedStep: The steps in which to incrementally increase the speed being tested. (default 0.5)");
		System.err.println("\t-duration: how long to move the robot at each speed in milliseconds (default 5000)");
		System.err.println("\t-heading: the heading in which to turn (default 0)");
	}
	
	public static void main(String[] args) {
		String serverIP = "localhost";
		int serverPort = 6665;
		int testStartDelay = 4;
		double minSpeed = 0.5;
		double maxSpeed = 3.5;
		double speedStep = 0.5;
		int duration = 5000;
		double heading = 0;
		MotionArbiter.MotionType mobilityPlane = MotionArbiter.MotionType.MOTION_TRAXXAS;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-server")) {
					serverIP = args[++i];
				} 
				else if (args[i].equals("-port")) {
					serverPort = Integer.valueOf(args[++i]);
				}
				else if (args[i].equals("-log")) {
					Logger.setFileLogger(new FileLogger(args[++i], false));
				}
				else if (args[i].equals("-testStartDelay")) {
					testStartDelay = Integer.valueOf(args[++i]);
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
				else if (args[i].equals("-maxSpeed")) {
					maxSpeed = Double.valueOf(args[++i]);
				}
				else if (args[i].equals("-duration")) {
					duration = Integer.valueOf(args[++i]);
				}
				else if (args[i].equals("-heading")) {
					heading = Double.valueOf(args[++i]);
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
		System.out.println("Mobility Plane: " + mobilityPlane);
		System.out.println("Test start delay: " + testStartDelay);
		System.out.println("Min Speed: " + minSpeed);
		System.out.println("Max Speed: " + maxSpeed);
		System.out.println("Speed step: " + speedStep);
		System.out.println("Duration: " + duration);
		System.out.println("Heading: " + heading);
		
		new MotorStressTest(serverIP, serverPort, mobilityPlane, testStartDelay, minSpeed, maxSpeed, speedStep, duration, heading);
	}
}
