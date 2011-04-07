package pharoslabut.cartographer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;

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
	public static String serverIP = "128.83.196.235";
	public static String fileName = "log.txt";
	public static PlayerClient client = null;
	public static FileLogger flogger = null;
	public static long numIRreadings = 0;
	static ArrayList<ArrayDeque<Float>> dq;

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

		
		new LocationTracker();
		new WorldView();
		
		WorldView.createSampleWorldView();
		
		
		/////////// ROOMBA/ODOMETRY INTERFACE ////////////
		motors = client.requestInterfacePosition2D(0, 
				PlayerConstants.PLAYER_OPEN_MODE);

		
		motors.addPos2DListener(this); 

		if (fileName != null) {
			flogger = new FileLogger(fileName);
		}


		/////////// IR INTERFACE ///////////////
		IRInterface ir = client.requestInterfaceIR(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (ir == null) {
			System.out.println("unable to connect to IR interface");
			System.exit(1);
		}

		ir.addIRListener(this);

		motors.setSpeed(0, 0);
		pause(1000);

		motors.setSpeed(0.2, 0);
		pause(5000);
		
		motors.setSpeed(0, -Math.PI/16);
		pause(4000);
		
//		motors.setSpeed(0.2, 0);
//		pause (5000);
		
		motors.setSpeed(0, 0);
		pause(1000);

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
		
		//TODO insert 5-wide median filter here
		
		LocationTracker.updateLocation(pp);
		
//		log("Odometry Data: x=" + pp.getPx() + ", y=" + pp.getPy() + ", a=" + pp.getPa() 
//				+ ", vela=" + data.getVel().getPa() + ", stall=" + data.getStall());
				
		System.out.println("Odometry Data: x=" + pp.getPx() + ", y=" + pp.getPy() + ", a=" + pp.getPa());
	}


	public void newPlayerIRData(PlayerIrData data) {

		if (++(PathPlannerSimpleTest.numIRreadings) == 1) {
			dq = new ArrayList<ArrayDeque<Float>>();
			
			for (int i = 0; i < 6; i++) {
				dq.add(new ArrayDeque<Float>(5));
			}
		}
		
		float [] window = new float [6];
		float [] ranges = data.getRanges();
		//5-wide median filter here

		// add all the ranges to each Deque (one per IR sensor)
		for (int i = 0; i < 6; i++) {
			dq.get(i).add(ranges[i]);
		}

		if (numIRreadings > 4) { // taken at least five IR sample sets
			for (int i = 0; i < 6; i++) {
				ArrayList<Float> arr = new ArrayList<Float>(5);
				Float [] fArray = (Float[]) dq.get(i).toArray(new Float[5]);
				for (int j = 0; j < 5; j++) {
					arr.add((Float) fArray[j]);
				}
				window[i] = findMedian(arr);
				dq.get(i).removeFirst(); // scrolling window, make room for next sensor reading
			}
		}

		WorldView.recordObstacles(window);

//		System.out.println("FL=" + window[0] + ", FC=" + window[1] + ", FR=" + window[2] + ", RL=" + window[5] + ", RC=" + window[4] + ", RR=" + window[3]);
	}



	public static float findMedian (ArrayList<Float> arr) {
		Collections.sort(arr);
		return (float) arr.get(2); // always get 3rd element (in a 5-wide window)
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
		

		//		try {
		//			WorldView.printWorldView();
		//		} catch (IOException e) {
		//			e.printStackTrace();
		//		}

		new PathPlannerSimpleTest(serverIP, serverPort, fileName);
	}

}

