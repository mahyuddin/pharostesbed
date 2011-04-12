package pharoslabut.cartographer;

import java.io.BufferedWriter;
import java.lang.*;
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
import playerclient.IRInterface;
import playerclient.IRListener;

public class PathPlanner {
	double WIDTH_OF_ROOMBA = 0.38;
	double SPEED_STEP = .2;
	private PlayerClient client = null;
	private FileLogger flogger = null;
	private static Position2DInterface motors;
	private List<Square> path;
	int left = 1;
	int right = -1;
	double bearing = 0;//initially facing right. We're gonna need to modify this and make it 0 only 1 time ever.
	
	public static String fileName = "log.txt";
	//public static String serverIP = "10.11.12.10"; // server for SAINTARNOLD
	//public static String serverIP = "128.83.52.224";
	public static String serverIP = "128.83.196.249";
	
	public PathPlanner (String serverIP, int serverPort, String fileName) {
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
		
		/////////// ASTAR ///////////////
		//motors.setSpeed(0, Math.PI/8);
		//pause(4000);
		/*motors.setSpeed(0, Math.PI/8);
		pause(3000);
		motors.setSpeed(0, -Math.PI/8);
		pause(6000);
		motors.setSpeed(0, Math.PI/8);
		pause(3000);*/
		LocationTracker.motors.setSpeed(.2, 0);
		pause(6000);

		
		//path = pathFind(); // ordered list of coordinates to follow
		//move(path); //moves according to the path. If there is a problem, choose another point based on LURD
		//motors.setSpeed(0, 0);
		//pause(5000);
		
		//System.out.println("got a path " + path.size());
		
		
		// STOP THE ROOMBA 
		LocationTracker.motors.setSpeed(0, 0);
		pause(2000);

		
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
	}
	
	private List<Square> pathFind(){
		int side = 9;
		int radius = side/2;
		int direction = -1;
		OrderedPair start;
		//start = new OrderedPair(radius,radius);	// middle of the sector
		start = new OrderedPair(radius,radius);	// start at the bottom left corner
		
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
	
	/**
	 * Moves the robot according to the list of Squares passed as the argument
	 * @param path
	 */
	public void move(List<Square> path){
		int time,turntime,x1,x2,y1,y2;
		
		for(int i = path.size()-2; i>=0; i--){
			y1 = path.get(i+1).getX();
			y2 = path.get(i).getX();
			x1 = path.get(i+1).getY();
			x2 = path.get(i).getY();
			
			System.out.println("(" + x1 + "," + y1 + ")===>(" + x2 + "," + y2 + ")");
			
			// convert from cartesian to polar coordinates
			double theta = Math.atan2(y2-y1, x2-x1);
			double r = Math.sqrt(Math.pow(x2-x1,2) + Math.pow(y2-y1,2));
			double turnAngle = bearing-theta; 
			int turnDirection = right;	// initially set turn direction to right
			if (bearing - theta < 0){
				turnDirection = left;
			}
			if( Math.abs(bearing - theta) > Math.PI ){	// special case
				turnAngle = 2*Math.PI - Math.abs(bearing - theta);
				turnDirection = turnDirection*(-1);	// find smaller direction
			}
			
			//Check to see if the robot can safely traverse to the target location
			if(checkTerrain(x1,y1,x2,y2)){
			//if(Math.random() > 0.5){
					
				bearing = theta;	// new bearing is theta
	
				turntime = (int)(Math.abs(turnAngle)*8/Math.PI * 1000)+1;
				// DEBUGGING STATEMENTS
				//System.out.println("(" + x1 + "," + y1 + ")===>(" + x2 + "," + y2 + ")");
				//System.out.println("polar(" +r+","+theta+")");
				//System.out.println("turnangle=" + turnAngle + " bearing=" + bearing);
				//System.out.println("turntime = " + turntime);
				motors.setSpeed(0, Math.PI/16*turnDirection);	// turnDirection is either left or right
				pause(turntime);
				//time = (int)r*1000;
				time = (int)(((r*WIDTH_OF_ROOMBA)/SPEED_STEP)*1000);	// scales the coord to roughly the size of the roomba
				motors.setSpeed(SPEED_STEP, 0);
				pause(time);
			}
			else { //BACKTRACK
				System.out.println("ERROR: OBSTACLE DETECTED. WLIL TRY TO REVERSE");
				motors.setSpeed(0, 0);
	
				pause(2000); //pause for 2 seconds
				for(int j = i+1; j<path.size()-1; j++){
					y2 = path.get(j+1).getX();
					y1 = path.get(j).getX();
					x2 = path.get(j+1).getY();
					x1 = path.get(j).getY();
					
					System.out.println("(" + x1 + "," + y1 + ")===>(" + x2 + "," + y2 + ")");
					
					// convert from cartesian to polar coordinates
					theta = Math.atan2(y2-y1, x2-x1);
					r = Math.sqrt(Math.pow(x2-x1,2) + Math.pow(y2-y1,2));
					turnAngle = bearing-theta; 
					turnDirection = right;	// initially set turn direction to right
					if (bearing - theta < 0){
						turnDirection = left;
					}
					if( Math.abs(bearing - theta) > Math.PI ){	// special case
						turnAngle = 2*Math.PI - Math.abs(bearing - theta);
						turnDirection = turnDirection*(-1);	// find smaller direction
					}
					bearing = theta;	// new bearing is theta

					//Check to see if the robot can safely traverse to the target location
					//if(checkTerrain(x1,y1,x2,y2)){

						turntime = (int)(Math.abs(turnAngle)*8/Math.PI * 1000)+1;
						System.out.println("polar(" +r+","+theta+")");
						motors.setSpeed(0, Math.PI/16*turnDirection);	// turnDirection is either left or right
						pause(turntime);

						time = (int)(((r*WIDTH_OF_ROOMBA)/SPEED_STEP)*1000);	// scales the coord to roughly the size of the roomba
						motors.setSpeed(SPEED_STEP, 0);
						pause(time);
				}
				
				motors.setSpeed(0, 0);
				pause(5000);
				//System.exit(0);
				break;
			}
		}
	}
	
	/**
	 * Checks to see if the destination is traversable before actually going there.
	 * @param currX
	 * @param currY
	 * @param newX
	 * @param newY
	 */
	public boolean checkTerrain(int currX, int currY, int newX, int newY){
		//There are 8 cases: 4 cardinal direction cases and 4 intermediate direction cases
		
		//Case 1: Moving South
		if(newX == currX && newY > currY){
			//Need to check the MapSectorElement directly South of the current position
			if(readWorldView(newX,newY))
				return true;
			else 
				return false;
		}
		//Case 2: Moving South
		else if(newX == currX && newY < currY){
			//Need to check the MapSectorElement directly South of the current position
			if(readWorldView(newX,newY))
				return true;
			else 
				return false;
		}
		//Case 3: Moving East
		else if(newX > currX && newY == currY){
			//Need to check the MapSectorElement directly East of the current position
			if(readWorldView(newX,newY))
				return true;
			else 
				return false;
		}
		//Case 4: Moving West
		else if(newX < currX && newY == currY){
			//Need to check the MapSectorElement directly West of the current position
			if(readWorldView(newX,newY))
				return true;
			else 
				return false;
		}
		//Case 5: Moving NorthWest
		else if(newX < currX && newY > currY){
			//Need to check 3 MapSectorElements North, NorthWest, and West of the current position
			if(readWorldView(newX,newY) && readWorldView(currX,currY+1) && readWorldView(currX-1,currY))
				return true;
			else 
				return false;
		}
		//Case 6: Moving NorthEast
		else if(newX > currX && newY > currY){
			//Need to check 3 MapSectorElements North, NorthEast, and East of the current position
			if(readWorldView(newX,newY) && readWorldView(currX,currY+1) && readWorldView(currX+1,currY))
				return true;
			else 
				return false;
		}
		//Case 7: Moving SouthWest
		else if(newX < currX && newY < currY){
			//Need to check 3 MapSectorElements South, SouthWest, and West of the current position
			if(readWorldView(newX,newY) && readWorldView(currX,currY-1) && readWorldView(currX-1,currY))
				return true;
			else 
				return false;
		}
		//Case 8: Moving SouthEast
		//if(newX > currX && newY < currY){
		else{
			//Need to check 3 MapSectorElements South, SouthEast, and East of the current position
			if(readWorldView(newX,newY) && readWorldView(currX,currY-1) && readWorldView(currX+1,currY))
				return true;
			else 
				return false;
		}

	}
	
	/**
	 * readWorldView() reads from the World View map only the MapSector element needed to determine if target location is free
	 * @param x MapSector x-coordinate
	 * @param y MapSector y-coordinate
	 */
	public boolean readWorldView(int x, int y){
		for(int r = 0; r < 8; r++){
			for(int c = 0; c < 8; c++){
				if(WorldView.readConfidence(8*x + r, 8*y + c) > 0.65)
					return false;
			}
		}
		return true;

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
		
		// TODO		
		new PathPlanner(serverIP, serverPort, fileName);
	}

}


