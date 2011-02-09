package pharoslabut.validation;

import pharoslabut.MotionArbiter;
import pharoslabut.tasks.Priority;
import pharoslabut.tasks.MotionTask;
import pharoslabut.logger.*;
import playerclient.PlayerClient;
import playerclient.PlayerException;
import playerclient.Position2DInterface;
import playerclient.structures.PlayerConstants;

/**
 * Stresses out the motor of the Proteus robot by sending it a velocity step function.
 * 
 * @author Chien-Liang Fok
 */
public class MotorStressTest {
	//public static final int COMPASS_LOG_PERIOD = 100; // in milliseconds
	private PlayerClient client = null;
	private FileLogger flogger = null;
	
	public MotorStressTest(String serverIP, int serverPort,	String fileName, boolean useCarLike, double speed, int duration, double heading) {
		try {
			client = new PlayerClient(serverIP, serverPort);
		} catch(PlayerException e) {
			log("Error connecting to Player: ");
			log("    [ " + e.toString() + " ]");
			System.exit (1);
		}
		
		Position2DInterface motors = client.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (motors == null) {
			log("motors is null");
			System.exit(1);
		}
		
		MotionArbiter motionArbiter = null;
		
		if (useCarLike)
			motionArbiter = new MotionArbiter(MotionArbiter.MotionType.MOTION_CAR_LIKE, motors);
		else
			motionArbiter = new MotionArbiter(MotionArbiter.MotionType.MOTION_IROBOT_CREATE, motors);
		
		if (fileName != null) {
			flogger = new FileLogger(fileName);
			motionArbiter.setFileLogger(flogger);
		}
		
		int startTime = 4;
		log("Starting experiment in " + startTime + "...");
		while (startTime-- > 0) {
			synchronized(this) { 
				try {
					wait(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (startTime > 0) log(startTime + "...");
		}
		
		MotionTask currTask;
		
		currTask = new MotionTask(Priority.SECOND, speed, heading);
		log("Submitting: " + currTask);
		motionArbiter.submitTask(currTask);
		pause(duration);
		
		currTask = new MotionTask(Priority.FIRST, MotionTask.STOP_VELOCITY, MotionTask.STOP_HEADING);
		log("Submitting: " + currTask);
		motionArbiter.submitTask(currTask);
		
		log("Pausing 5s before moving in reverse...");
		pause(5000);
		
		currTask = new MotionTask(Priority.SECOND, -1 * speed, heading);
		log("Submitting: " + currTask);
		motionArbiter.submitTask(currTask);
		
		pause(duration);
		
		currTask = new MotionTask(Priority.FIRST, MotionTask.STOP_VELOCITY, MotionTask.STOP_HEADING);
		log("Submitting: " + currTask);
		motionArbiter.submitTask(currTask);
		
		/*currTask = new MotionTask(Priority.SECOND, -speedStep, 0);
		log("Submitting: " + currTask);
		motionArbiter.submitTask(currTask);
		pause(3000);
		
		// Stop the robot
		currTask = new MotionTask(Priority.FIRST, MotionTask.STOP_VELOCITY, MotionTask.STOP_HEADING);
		log("Submitting: " + currTask);
		motionArbiter.submitTask(currTask);*/
		
		log("Test complete!");
		System.exit(0);
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
	
	private void log(String msg) {
		String result = "MotorStressTest: " + msg;
		System.out.println(result);
		if (flogger != null) {
			flogger.log(result);
		}
	}
	
	private static void usage() {
		System.err.println("Usage: pharoslabut.validation.MotorStressTest <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
		System.err.println("\t-file <file name>: name of file in which to save results (default log.txt)");
		System.err.println("\t-car: issue car like commands (default non-car-like)");
		System.err.println("\t-speed: the speed at which to move the robot in m/s (default 2)");
		System.err.println("\t-duration: how long to move the robot in milliseconds (default 2000)");
		System.err.println("\t-heading: the heading in which to turn (default 0)");
	}
	
	public static void main(String[] args) {
		String fileName = "log.txt";
		String serverIP = "localhost";
		int serverPort = 6665;
		boolean useCarLike = false;
		double speed = 2;
		int duration = 2000;
		double heading = 0;

		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-server")) {
					serverIP = args[++i];
				} 
				else if (args[i].equals("-port")) {
					serverPort = Integer.valueOf(args[++i]);
				}
				else if (args[i].equals("-file")) {
					fileName = args[++i];
				}
				else if (args[i].equals("-car")) {
					useCarLike = true;
				}
				else if (args[i].equals("-speed")) {
					speed = Double.valueOf(args[++i]);
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
		System.out.println("File: " + fileName);
		System.out.println("Use Car Commands: " + useCarLike);
		System.out.println("Speed: " + speed);
		System.out.println("Duration: " + duration);
		System.out.println("Heading: " + heading);
		
		new MotorStressTest(serverIP, serverPort, fileName, useCarLike, speed, duration, heading);
	}
}
