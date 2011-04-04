package pharoslabut.cartographer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

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
import playerclient.structures.player.PlayerDeviceDevlist;
import playerclient.structures.player.PlayerDeviceDriverInfo;
import playerclient.structures.ir.PlayerIrData;
import playerclient.structures.position2d.PlayerPosition2dData;
import playerclient.IRInterface;
import playerclient.IRListener;

public class PathPlannerSimpleTest implements Position2DListener, IRListener {
	public static String serverIP = "10.11.12.10";
	public static String fileName = "log.txt";
	private PlayerClient client = null;
	private FileLogger flogger = null;

	private static Position2DInterface motors; 
	//public final PlayerMsgHdr PLAYER_MSGTYPE_DATA           = 1;
		
	public PathPlannerSimpleTest (String serverIP, int serverPort, String fileName) {
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
		IRInterface ir = client.requestInterfaceIR(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (ir == null) {
			System.out.println("unable to connect to IR interface");
			System.exit(1);
		}
		
		ir.addIRListener(this);

		
//		MotionTask currTask;
//		double speedStep = .2;
		
		//PathPlanner.writeOdometry(5.0, 5.0, 0.0);
		
		
		
		//RotateDegrees(Math.PI/16, motionArbiter);
		//currTask = new MotionTask(Priority.SECOND, 0, Math.PI/8);
		//log("Submitting: " + currTask);
		//motionArbiter.submitTask(currTask);
		
		//currTask = new MotionTask(Priority.FIRST, 0, MotionTask.STOP_HEADING);
		//log("Submitting: " + currTask);
		//motionArbiter.submitTask(currTask);
		
		motors.setSpeed(0, 0);
		pause(5000);

		motors.setSpeed(0, 0);
		pause(5000);


		log("Test complete!");
		
		try {
			WorldView.printWorldView();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			WorldView.fout.close();
		} 
		catch (Exception e) {
		      System.err.println("Error closing file stream for 'world.txt': " + e.getMessage());
		}	
		
		try {
            BitmapOut bitmap = new BitmapOut(WorldView.WORLD_SIZE,WorldView.WORLD_SIZE);
        } catch (IOException e2) {
            e2.printStackTrace();
        }
		
		ir.removeIRListener(this);
		motors.removePos2DListener(this);
		System.exit(0);
		///////////// END OF IR INTERFACING ///////////////
		
	}
	
	
	public static void writeOdometry(double newX, double newY, double newAngle) {
		PlayerPose newPose = new PlayerPose();
		newPose.setPx(newX);
		newPose.setPy(newY);
		newPose.setPa(newAngle);
		(PathPlannerSimpleTest.motors).setOdometry(newPose);
		return;
	}
	


	//@Override
	public void newPlayerPosition2dData(PlayerPosition2dData data) {
		PlayerPose pp = data.getPos();
		//motors.setOdometry(pp);
//		if (!(pp.equals(null))) {
		
			//TODO insert 5-wide median filter here
			LocationTracker.updateLocation(pp);
//		}
		log("Odometry Data: x=" + pp.getPx() + ", y=" + pp.getPy() + ", a=" + pp.getPa() 
				+ ", vela=" + data.getVel().getPa() + ", stall=" + data.getStall());
	}
	
	public void newPlayerIRData(PlayerIrData data) {
		float[] dist = data.getRanges();
		//TODO insert 5-wide median filter here
		WorldView.recordObstacles(dist);
		//log(data.getRanges_count() + " sensors, IR Data: FL=" + dist[0] + ", FC=" + 
			//	dist[1] + ", FR=" + dist[2] + ", RL=" + dist[3] + ", RC=" + dist[4] + ", RR=" + 
				//dist[5]);
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
//		String result = "PathPlanner: " + msg;
//		System.out.println(result);
//		if (flogger != null) {
//			flogger.log(result);
//		}
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
		int serverPort = 6665;

		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-server")) {
					PathPlannerSimpleTest.serverIP = args[++i];
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
		
		new LocationTracker();
		new WorldView();
		
//		try {
//			WorldView.printWorldView();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		new PathPlannerSimpleTest(serverIP, serverPort, fileName);
	}

}


