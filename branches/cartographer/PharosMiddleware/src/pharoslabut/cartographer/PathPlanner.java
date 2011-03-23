package pharoslabut.cartographer;

import java.io.BufferedWriter;
import java.lang.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
import playerclient.IRInterface;
import playerclient.IRListener;

public class PathPlanner implements Position2DListener, IRListener {
	private PlayerClient client = null;
	private FileLogger flogger = null;
	private static Position2DInterface motors;
	private List<Square> path;
	
	//public final PlayerMsgHdr PLAYER_MSGTYPE_DATA           = 1;
	
	public List<Square> pathFind(){
		List<OrderedPair> goalPoints = new ArrayList<OrderedPair>();
		int side = 11;
		int radius = side/2;
		OrderedPair start;
		OrderedPair end;
		start = new OrderedPair(radius,radius);	// middle of the sector
		
		// right now, just four possible places
		end = new OrderedPair(start.getX()+radius,start.getY());
		goalPoints.add(end);
		end = new OrderedPair(start.getX()-radius,start.getY());
		goalPoints.add(end);
		end = new OrderedPair(start.getX(),start.getY()+radius);
		goalPoints.add(end);
		end = new OrderedPair(start.getX(),start.getY()-radius);
		goalPoints.add(end);
		
		// Reminder to change name to Astar and delete this java file
		MapSector sector = new MapSector(side,side,start,goalPoints);
		sector.findPath();	// this exits as soon as it finds a path	
		return sector.bestList;
	}
	
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
		IRInterface ir = client.requestInterfaceIR(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (ir == null) {
			System.out.println("unable to connect to IR interface");
			System.exit(1);
		}
		
		ir.addIRListener(this);
		pause(2000);
		
		MotionTask currTask;
		double speedStep = .2;
		
		//PathPlanner.writeOdometry(5.0, 5.0, 0.0);
		int time,turntime,x1,x2,y1,y2;
		double angle,dist;
		/////////// ASTAR ///////////////
		path = pathFind(); // ordered list of coordinates to follow
		for(int i = path.size()-1; i==0; i--){
			x1 = path.get(i+1).getX();
			x2 = path.get(i).getX();
			y1 = path.get(i+1).getY();
			y2 = path.get(i).getY();
			
			angle = Math.atan((y2-y1)/(x2-x1));
			turntime = 1000;	// change this.. 
			motors.setSpeed(0, Math.PI/16);
			pause(turntime);
			
			dist = Math.sqrt(Math.pow((x2-x1),2) + Math.pow((y2-y1),2)); // use the distance formula (pythagorean theorem)
			time = (int)dist*10;
			motors.setSpeed(.1, 0);
			pause(time);
		}
		
		//RotateDegrees(Math.PI/16, motionArbiter);
		//currTask = new MotionTask(Priority.SECOND, 0, Math.PI/8);
		//log("Submitting: " + currTask);
		//motionArbiter.submitTask(currTask);
		
		//currTask = new MotionTask(Priority.FIRST, 0, MotionTask.STOP_HEADING);
		//log("Submitting: " + currTask);
		//motionArbiter.submitTask(currTask);
		
		motors.setSpeed(0, 0);
		pause(5000);
		
		motors.setSpeed(0.1, 0);
		pause(5000);
		
		motors.setSpeed(0, 0.1);
		pause(10000);
		
		motors.setSpeed(.1, 0);
		pause(5000);
		
		motors.setSpeed(0, 0);
		pause(5000);
		
		
//		while(true){
//			//while no obstacle detected, move forward
//			while((ir.getData()).getRanges()[1]>1000){
//				currTask = new MotionTask(Priority.FIRST, .2, MotionTask.STOP_HEADING);
//				log("Submitting: " + currTask);
//				motionArbiter.submitTask(currTask);
//				pause(1000);
//			}
//			//stop
//			currTask = new MotionTask(Priority.FIRST, MotionTask.STOP_VELOCITY, MotionTask.STOP_HEADING);
//			log("Submitting: " + currTask);
//			motionArbiter.submitTask(currTask);
//			pause(1000);
//			
//			//turn 90 degrees
//			RotateDegrees((Math.PI)/16,motionArbiter);
//		}
		log("Test complete!");
		
		try {
			WorldView.printWorldView();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			WorldView.fout.close();
		} 
		catch (Exception e) {
		      System.err.println("Error closing file stream for 'world.txt': " + e.getMessage());
		}	
		
		System.exit(0);
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
		//motors.setOdometry(pp);
//		if (!(pp.equals(null))) {
			LocationTracker.updateLocation(pp);
//		}
		log("Odometry Data: x=" + pp.getPx() + ", y=" + pp.getPy() + ", a=" + pp.getPa() 
				+ ", vela=" + data.getVel().getPa() + ", stall=" + data.getStall());
	}
	
	public void newPlayerIRData(PlayerIrData data) {
		float[] dist = data.getRanges();
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
		String fileName = "log.txt";
		//String serverIP = "10.11.12.10"; // server for SAINTARNOLD
		String serverIP = "128.83.52.224";
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
		
		new LocationTracker();
		new WorldView();
		
//		try {
//			WorldView.printWorldView();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		new PathPlanner(serverIP, serverPort, fileName);
	}

}

