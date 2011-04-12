package pharoslabut.cartographer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import playerclient.IRListener;

public class PathPlannerSimpleTest {
	public static String serverIP = "10.11.12.10";
	//public static String serverIP = "128.83.196.235";
	public static String fileName = "log.txt";
	public static PlayerClient client = null;
	public static FileLogger flogger = null;
	
	
 
	//public final PlayerMsgHdr PLAYER_MSGTYPE_DATA           = 1;

	List<Square> pathFind(){
		int side = 9;
		int radius = side/2;
		int direction = -1;
		OrderedPair start;
		//start = new OrderedPair(radius,radius);	// middle of the sector
		start = new OrderedPair(0,0);	// start at the bottom left corner
		
		System.out.println("entered sector");
		switch(direction){
		case 0:
			start.y+=32;
			break;
		case 1:
			start.y-=32;
			break;
		case 2:
			start.x-=32;
			break;
		case 3:
			start.x+=32;
			break;
		default:
			break;
		}
		MapSector sector = new MapSector(side,side,start, radius);
		direction = sector.findPath();	// this exits as soon as it finds a path
		System.out.println(sector.bestList.size());
		return sector.bestList;
	}
	
	public PathPlannerSimpleTest (String serverIP, int serverPort, String fileName) {
		try {
			client = new PlayerClient(serverIP, serverPort);
		} catch(PlayerException e) {
			log("Error connecting to Player: ");
			log("    [ " + e.toString() + " ]");
			System.exit (1);
		}

		
		new WorldView();
		
		WorldView.createSampleWorldView();
		

		if (fileName != null) {
			flogger = new FileLogger(fileName);
		}


		new LocationTracker();
		
		
		LocationTracker.motors.setSpeed(0, 0);
		pause(2000);
		
		LocationTracker.motors.setSpeed(0.1, 0);
		pause (17000);
		
		LocationTracker.motors.setSpeed(0, 0);
		pause (1000);
		
		LocationTracker.motors.setSpeed(0, Math.PI/16);
		pause (4000);
		
		LocationTracker.motors.setSpeed(0, 0);
		pause (1000);
		
		LocationTracker.motors.setSpeed(0, -Math.PI/16);
		pause (8000);
		
		LocationTracker.motors.setSpeed(0, 0);
		pause (1000);
		
		LocationTracker.motors.setSpeed(0, Math.PI/16);
		pause (4000);
		
//		double start = LocationTracker.getCurrentLocation()[0];
//		while (LocationTracker.getCurrentLocation()[0] < (start + 0.85)) {}
		
		
//		motors.setSpeed(0, Math.PI/32);
//		
//		while (LocationTracker.getCurrentLocation()[2] < Math.PI*0.99) {}

		
//		motors.setSpeed(0.2, 0);
//		pause (5000);
		
		LocationTracker.motors.setSpeed(0, 0);
		pause(2000);
		
		log("Test complete!");

		
		try {
			WorldView.printWorldView();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
//		List<Square> path = pathFind(); // ordered list of coordinates to follow
		try {
			WorldView.fout.close();
		} 
		catch (Exception e) {
			System.err.println("Error closing file stream for 'world.txt': " + e.getMessage());
		}		
		
		
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
		

		new PathPlannerSimpleTest(serverIP, serverPort, fileName);
	}
	


}


