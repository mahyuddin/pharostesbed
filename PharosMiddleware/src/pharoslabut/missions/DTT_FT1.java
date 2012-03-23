package pharoslabut.missions;

import pharoslabut.sensors.*;
import pharoslabut.tasks.Priority;
import pharoslabut.tasks.MotionTask;
import pharoslabut.logger.*;
import pharoslabut.navigate.MotionArbiter;

import playerclient3.*;
import playerclient3.structures.*;
import playerclient3.structures.position2d.PlayerPosition2dData;

/**
 * This is the code we used during Distributed Traction Control Field Test 1 (DTT-FT1)
 * 
 * @author Chien-Liang Fok
 */
public class DTT_FT1 implements Position2DListener {
	private PlayerClient client = null;
	private FileLogger flogger = null;
	
	/**
	 * The constructor.
	 * 
	 * @param serverIP The IP address of the robot.
	 * @param serverPort The port on which the robot is listening.
	 * @param logFileName The log file in which to save results.
	 * @param mobilityPlane The type of mobility plane being used.
	 * @param testStartDelay The number of seconds to wait before starting the test.
	 */
	public DTT_FT1(String serverIP, int serverPort,	String logFileName, 
			MotionArbiter.MotionType mobilityPlane, int testStartDelay) 
	{
		if (logFileName != null) {
			flogger = new FileLogger(logFileName);
			Logger.setFileLogger(flogger);
		}
		
		try {
			Logger.log("Connecting to server " + serverIP + ":" + serverPort);
			client = new PlayerClient(serverIP, serverPort);
		} catch(PlayerException e) {
			Logger.logErr("ERROR: Could not connect to player server: ");
			Logger.logErr("    [ " + e.toString() + " ]");
			System.exit (1);
		}
		
		Logger.log("Subscribing to motor interface and creating motion arbiter");
		Position2DInterface motors = client.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (motors == null) {
			Logger.logErr("ERROR: motors is null");
			System.exit(1);
		} else {
			// subscribe to MCU debug messages
			Position2DBuffer p2db = new Position2DBuffer(motors); 
			p2db.addPos2DListener(this);
			p2db.start();
		}
		MotionArbiter motionArbiter = new MotionArbiter(mobilityPlane, motors);
		
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
		
		MotionTask currTask;
		
		double speed = 2.0;
		double heading = 0.0;
		currTask = new MotionTask(Priority.SECOND, speed, heading);
		Logger.log("Submitting motion task: " + currTask);
		motionArbiter.submitTask(currTask);
		pause(5000);
		
		speed = 2.0;
		heading = -20.0;
		currTask = new MotionTask(Priority.SECOND, speed, heading);
		Logger.log("Submitting motion task: " + currTask);
		motionArbiter.submitTask(currTask);
		pause(1000);
		
		speed = 2.0;
		heading = 0.0;
		currTask = new MotionTask(Priority.SECOND, speed, heading);
		Logger.log("Submitting motion task: " + currTask);
		motionArbiter.submitTask(currTask);
		pause(2000);
		
		speed = 2.0;
		heading = 20.0;
		currTask = new MotionTask(Priority.SECOND, speed, heading);
		Logger.log("Submitting motion task: " + currTask);
		motionArbiter.submitTask(currTask);
		pause(1000);
		
		speed = 2.0;
		heading = 0.0;
		currTask = new MotionTask(Priority.SECOND, speed, heading);
		Logger.log("Submitting motion task: " + currTask);
		motionArbiter.submitTask(currTask);
		pause(5000);
		
		currTask = new MotionTask(Priority.FIRST, MotionTask.STOP_SPEED, MotionTask.STOP_STEERING_ANGLE);
		Logger.log("Submitting motion task: " + currTask);
		motionArbiter.submitTask(currTask);
		
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
//		String result = "DTT_FT1: " + msg;
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
		System.err.println("Usage: pharoslabut.missions.DTT_FT1 <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
		System.err.println("\t-log <file name>: name of file in which to save results (default null)");
		System.err.println("\t-mobilityPlane <traxxas|segway|create>: The type of mobility plane being used (default traxxas)");
		System.err.println("\t-testStartDelay <delay in seconds>: The number of seconds to wait before starting the test (default 4)");
	}
	
	public static void main(String[] args) {
		String logFileName = null;
		String serverIP = "localhost";
		int serverPort = 6665;
		int testStartDelay = 4;
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
					logFileName = args[++i];
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
		System.out.println("Log: " + logFileName);
		System.out.println("Mobility Plane: " + mobilityPlane);
		System.out.println("Test start delay: " + testStartDelay);
		
		new DTT_FT1(serverIP, serverPort, logFileName, mobilityPlane, testStartDelay);
	}
}
