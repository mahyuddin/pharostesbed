package pharoslabut.cartographer;

import pharoslabut.MotionArbiter;
import pharoslabut.logger.FileLogger;
import pharoslabut.tasks.MotionTask;
import pharoslabut.tasks.Priority;
import playerclient.IRInterface;
import playerclient.PlayerClient;
import playerclient.PlayerDevice;
import playerclient.PlayerException;
import playerclient.Position2DInterface;
import playerclient.Position2DListener;
import playerclient.structures.*;
import playerclient.structures.PlayerConstants;
import playerclient.structures.PlayerDevAddr;
import playerclient.structures.PlayerMsgHdr;
import playerclient.structures.player.PlayerDeviceDevlist;
import playerclient.structures.player.PlayerDeviceDriverInfo;
import playerclient.structures.ir.PlayerIrData;
import playerclient.structures.position2d.PlayerPosition2dData;
import playerclient.IRInterface;
import playerclient.IRListener;

public class PathPlanner implements Position2DListener, IRListener {
	private PlayerClient client = null;
	private FileLogger flogger = null;
	private static Position2DInterface motors; 
	//public final PlayerMsgHdr PLAYER_MSGTYPE_DATA           = 1;
		
	public PathPlanner (String serverIP, int serverPort, String fileName) {
		try {
			client = new PlayerClient(serverIP, serverPort);
		} catch(PlayerException e) {
			log("Error connecting to Player: ");
			log("    [ " + e.toString() + " ]");
			System.exit (1);
		}
		
		/////////// ROOMBA/ODOMETRY INTERFACE ////////////
		motors = client.requestInterfacePosition2D(0, 
				PlayerConstants.PLAYER_OPEN_MODE);
		
		
//		if (motors == null){
//			log("unable to connect to Position2D interface");
//			System.exit(1);
//		}
		motors.addPos2DListener(this); 
		MotionArbiter motionArbiter = null;
		motionArbiter = new MotionArbiter(MotionArbiter.MotionType.MOTION_IROBOT_CREATE, motors);

		if (fileName != null) {
			flogger = new FileLogger(fileName);
			motionArbiter.setFileLogger(flogger);
		}
		
		
		/////////// IR INTERFACE ///////////////
//		System.out.print("Trying to Establish IR interface...");
		IRInterface ir = client.requestInterfaceIR(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (ir == null) {
			System.out.println("unable to connect to IR interface");
			System.exit(1);
		}
		System.out.print("established\n");
		
		ir.addIRListener(this);
		pause(2000);
		
		MotionTask currTask;
		double speedStep = .2;
		
		
		
		//RotateDegrees(Math.PI/16, motionArbiter);
		
		while(true){
			//while no obstacle detected, move forward
			while((ir.getData()).getRanges()[1]>1000){
				currTask = new MotionTask(Priority.FIRST, .2, MotionTask.STOP_HEADING);
				log("Submitting: " + currTask);
				motionArbiter.submitTask(currTask);
				pause(1000);
			}
			//stop
			currTask = new MotionTask(Priority.FIRST, MotionTask.STOP_VELOCITY, MotionTask.STOP_HEADING);
			log("Submitting: " + currTask);
			motionArbiter.submitTask(currTask);
			pause(1000);
			
			//turn 90 degrees
			RotateDegrees((Math.PI)/16,motionArbiter);
		}
		/*log("Test complete!");
		System.exit(0);*/
		///////////// END OF IR INTERFACING ///////////////
		
	}
	
	
	public static void writeOdometry(double newX, double newY, double newAngle) {
		PlayerPose newPose = new PlayerPose();
		newPose.setPx(newX);
		newPose.setPy(newY);
		newPose.setPa(newAngle);
		(PathPlanner.motors).setOdometry(newPose);
		return;
	}
	
	
	
	public boolean RotateDegrees(double radians, MotionArbiter robot){
		MotionTask currTask;
		int time = 5000;
		//double radiansPerSecond = radians/time;
		currTask = new MotionTask(Priority.SECOND, 0, -radians);
		log("Submitting: " + currTask);
		robot.submitTask(currTask);
		pause(time);
		return true;
	}

	//@Override
	public void newPlayerPosition2dData(PlayerPosition2dData data) {
		PlayerPose pp = data.getPos();
		LocationTracker.updateLocation(pp);
		log("Odometry Data: x=" + pp.getPx() + ", y=" + pp.getPy() + ", a=" + pp.getPa() 
				+ ", vela=" + data.getVel().getPa() + ", stall=" + data.getStall());
	}
	
	public void newPlayerIRData(PlayerIrData data) {
		float[] dist = data.getRanges();
		WorldView.recordObstacles(dist);
		log(data.getRanges_count() + " sensors, IR Data: FL=" + dist[0] + ", FC=" + 
				dist[1] + ", FR=" + dist[2] + ", RL=" + dist[3] + ", RC=" + dist[4] + ", RR=" + 
				dist[5]);
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
	 * starts the entire navigation/mapping routine
	 * @param args
	 *****************************************************/
	public static void main(String[] args) {
		String fileName = "log.txt";
		String serverIP = "10.11.12.10"; // server for SAINTARNOLD
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


