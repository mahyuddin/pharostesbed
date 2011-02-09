package pharoslabut.cartographer;

import pharoslabut.MotionArbiter;
import pharoslabut.logger.FileLogger;
import pharoslabut.tasks.MotionTask;
import pharoslabut.tasks.Priority;
import playerclient.PlayerClient;
import playerclient.PlayerException;
import playerclient.Position2DInterface;
import playerclient.Position2DListener;
import playerclient.structures.PlayerConstants;
import playerclient.structures.PlayerPose;
import playerclient.structures.position2d.PlayerPosition2dData;

public class PathPlanner implements Position2DListener {
	private PlayerClient client = null;
	private FileLogger flogger = null;
	
	public PathPlanner (String serverIP, int serverPort, String fileName) {
		try {
			client = new PlayerClient(serverIP, serverPort);
		} catch(PlayerException e) {
			log("Error connecting to Player: ");
			log("    [ " + e.toString() + " ]");
			System.exit (1);
		}
		
		//client.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		
		Position2DInterface motors = client.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (motors == null) {
			log("unable to connect to Position2D interface");
			System.exit(1);
		}
		
		motors.addPos2DListener(this); 		
		
		MotionArbiter motionArbiter = null;
		
		motionArbiter = new MotionArbiter(MotionArbiter.MotionType.MOTION_IROBOT_CREATE, motors);
		
		if (fileName != null) {
			flogger = new FileLogger(fileName);
			motionArbiter.setFileLogger(flogger);
		}
		
		MotionTask currTask;
		double speedStep = .2;
		
		pause(2000);
		currTask = new MotionTask(Priority.SECOND, speedStep, MotionTask.STOP_HEADING);
		log("Submitting: " + currTask);
		motionArbiter.submitTask(currTask);
		pause(5000);
		
		currTask = new MotionTask(Priority.FIRST, MotionTask.STOP_VELOCITY, MotionTask.STOP_HEADING);
		log("Submitting: " + currTask);
		motionArbiter.submitTask(currTask);
		pause(1000);
		
		
		log("Test complete!");
		System.exit(0);
	}
	

	@Override
	public void newPlayerPosition2dData(PlayerPosition2dData data) {
		PlayerPose pp = data.getPos();
		log("Odometry Data: x=" + pp.getPx() + ", y=" + pp.getPy() + ", a=" + pp.getPa() + ", vel=" + data.getVel() + ", stall=" + data.getStall());
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
		String result = "PathPlanner: " + msg;
		System.out.println(result);
		if (flogger != null) {
			flogger.log(result);
		}
	}
	
	private static void usage() {
		System.err.println("Usage: pharoslabut.validation.PathPlanner <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
		System.err.println("\t-file <file name>: name of file in which to save results (default log.txt)");
		System.err.println("\t-car: issue car like commands (default non-car-like)");
	}
	
	
	/******************** MAIN ****************************
	 * runs the whole shit
	 * @param args
	 *****************************************************/
	public static void main(String[] args) {
		String fileName = "log.txt";
		String serverIP = "10.11.12.10"; // server for St. Arnold
		int serverPort = 6665;

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
		
		new PathPlanner(serverIP, serverPort, fileName);
		
		
		
	}

		
}



