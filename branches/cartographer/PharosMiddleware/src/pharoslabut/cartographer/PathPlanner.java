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

class Vector{
	Integer[] coord = {0,0};
	int centroid = 0;
	public Vector(){
		this.coord[0] = 0;
		this.coord[1] = 0;
	}
	public Vector(Integer[] coord){
		this.coord = coord;
	}
	public void setCoord(Integer[] coord){
		this.coord = coord;
	}
	public Integer[] getCoord(){
		return coord;
	}
	public void setCentroid(int cent){
		this.centroid = cent;
	}
	public int getCentroid(){
		return centroid;
	}
}

public class PathPlanner {
	
	// Runtime, etc
	private static int RUNTIME = 180;	// in seconds
	final int BUFFER_TIME = 10;	// in seconds
	
	// Roomba Size
	final double ROOMBA_RADIUS = WorldView.ROOMBA_RADIUS;
	final double WIDTH_OF_ROOMBA = ROOMBA_RADIUS*2;
	
	final double BEACON_RADIUS = ROOMBA_RADIUS*2;
	final double NUMBER_OF_CENTROIDS = 3;
	
	// Distance to keep from wall in mm
	final static double DISTANCE_FROM_WALL = 0.5*1000;
	//final double FACE_DISTANCE_FROM_WALL = 0.6*1000; 
	final static double FACE_DISTANCE_FROM_WALL = 0.7*1000; 
	
	// Translational Speed
	final double TRANS_RATE_FAST = .3;	
	final double TRANS_RATE_MED = .2;	
	final double TRANS_RATE_SLOW = .1;
	double SPEED_STEP = TRANS_RATE_SLOW;
	final int WAIT_TIME = 200;
	
	// Rotational Speed
	final double ROT_RATE_FAST = Math.PI/4;	
	final double ROT_RATE_MED = Math.PI/8;	
	final double ROT_RATE_SLOW = Math.PI/16;
	double TURN_STEP = ROT_RATE_MED;
	
	// Clients and handles
	public static PlayerClient client = null;
	public static String serverIP = "10.11.12.10"; // server for SAINTARNOLD
	//public static String serverIP = "128.83.52.224";
	//public static String serverIP = "128.83.196.249";
	
	private FileLogger flogger = null;
	public static String fileName = "log.txt";
	
	private static float LeftHandDistance = 0;
	private static float RightHandDistance = 0;
	private static float FaceDistance = 0;
	
	private static double LeftInnerHandDistance = 0;
	private static double RightInnerHandDistance = 0;
	private static double FaceInnerDistance = 0;
	
	private static Integer [] snapshotLocation = {0,0};
	
	private static boolean startCheck = false;
	
	private static PlayerPose pose;
	
	private static ArrayList<ArrayList<Integer[]>> myRoute = new ArrayList<ArrayList<Integer[]>>(); 
	private static ArrayList<Integer[]> myNodes = new ArrayList<Integer[]>();
	private static ArrayList<Integer[]> myCentroids = new ArrayList<Integer[]>();
	private static ArrayList<Integer[]> lineToFollow= new ArrayList<Integer[]>();
	private static ArrayList<Vector> frontierCells = new ArrayList<Vector>();
	private static double certaintyFactor = 1.0;
	
	static int currentLayer = 0;
	
	// obsolete definitions
	int left = 1;
	int right = -1;
	double bearing = 0;//initially facing right. We're gonna need to modify this and make it 0 only 1 time ever.
	
	public enum State {
		INIT,
		MAP_CONTOUR,
		MAP_EXPLORE,
		END
	}
	
	public static OrderedPairDouble startingCoordinates = null;
	public static double initialBearing = 0;
	
	
	/**
	 * Path Planner constructor
	 * @param serverIP
	 * @param serverPort
	 * @param fileName
	 */
	public PathPlanner (String serverIP, int serverPort, String fileName) {
		try {
			client = new PlayerClient(serverIP, serverPort);
		} catch(PlayerException e) {
			log("Error connecting to Player: ");
			log("    [ " + e.toString() + " ]");
			System.exit (1);
		}
		
		ArrayList<Integer[]> firstLayer = new ArrayList<Integer[]>();
		myRoute.add(firstLayer);
		
		new WorldView();// initialize the World
		new LocationTracker(PathPlanner.startingCoordinates, PathPlanner.initialBearing);// initialize its localization scheme
		
		RealtimeImgOut mapThread = new RealtimeImgOut();
		mapThread.start();
		
		//if (fileName != null) {
		//	flogger = new FileLogger(fileName);
		//}
		
		runPathPlanner();	// should never return	
	}
	
	/**
	 * Initiates and runs the PathPlanner algorithm
	 * 		Lefthand rule for Wall following
	 */
	private void runPathPlanner(){
		
		State event = State.INIT;	// initial state is initialization routine
		System.out.println("starting movement control");
		while(true){
			switch(event){
			case INIT:
				//////// Initialization Routine /////////
				// LocationTracker.writeOdometry(0, 0, 0);
				LocationTracker.motors.setSpeed(0, 0);
				pause(2000);	// 2s initial delay		
				//swipe();	// initial swipe
				event = State.MAP_CONTOUR;
				break;
			case MAP_CONTOUR:
				
				// contour mapping
				leftWallFollow();
				event = State.MAP_EXPLORE;
				break;
			case MAP_EXPLORE:
				
				// TODO
				//Integer[] centroid = {60, 110};
				Integer[] centroid = findCenter();
				System.out.println("Center: (" + centroid[0] + "," + centroid[1] + ")");
				double initAng = createLine(centroid);
				faceToAngle(initAng);
				followLine(centroid, initAng);
				swipe();
				// exploration strategy
				//calculateNodes();
				//myCentroids = makeBatches();
				//goBackwards(20);
				//leftInnerWallFollow();
				//event = State.MAP_EXPLORE;
				event = State.END;
				break;
			case END:					
				//////// Finishing Routine //////////
				// Print WorldView
				//printRoute();	// print to console
				try {
					WorldView.printWorldView();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				// Close the file and exit
				try {
					WorldView.fout.close();
				} 
				catch (Exception e) {
				      System.err.println("Error closing file stream for 'world.txt': " + e.getMessage());
				}
				finally{
					//System.exit(0);	
					while(true){}
				}
				//break;
			}
		}
	}
	
	private void calculateNodes(){
		OrderedPair coord;
		for(int i = 0; i < myRoute.size(); i += myRoute.size()/5){
			//myNodes.add(myRoute.get(i));
		}
		for(int x = 0; x<WorldView.WORLD_SIZE; x++){
			for(int y = 0; y<WorldView.WORLD_SIZE; y++){
				coord = new OrderedPair(x,y);
				if(WorldView.world.get(x).get(y).getFrontier())
					addFrontierCell(coord);
			}
		}
	}
	
	/**
	 * @param coord
	 */
	public static void addFrontierCell(OrderedPair coord){
		Integer[] newCoord = {coord.getX(),coord.getY()};
		Vector v = new Vector(newCoord);
		boolean alreadyExists = false;
		for(int i = 0; i<frontierCells.size(); i++){
			if(frontierCells.get(i).getCoord()[0].equals(coord.getX()) && frontierCells.get(i).getCoord()[1].equals(coord.getY())){
				alreadyExists = true;
				break;
			}				
		}
		if(alreadyExists == false)
			frontierCells.add(v);
	}
	
	/**
	 * @param centroids
	 * @param points
	 * @return
	 */
	public static ArrayList<Integer[]> batch(ArrayList<Vector> centroids, ArrayList<Vector> points){
		
		//ArrayList<Vector> newData = dataBase.;
		for(int x = 0; x < 20; x++)
		{
			for(int y = 0; y < points.size(); y++)
			{
				double minDistance = Double.MAX_VALUE;
				int indexOfCentroid = 0;
				
				for(int z = 0; z < centroids.size(); z++)
				{
					Vector first = points.get(y);//data
					Vector second = centroids.get(z);//centroid
					
					double currDistance = euclideanDistance(first, second);
					if(currDistance < minDistance)
					{
						minDistance = currDistance;
						indexOfCentroid = z;
						first.setCentroid(indexOfCentroid);
						//dataBase.remove(y);
						//dataBase.add(y, first);
						//dataBase.get(y).setCentroid(indexOfCentroid);
						//System.out.print(z+" ");
					}
				}
			}

			//recalculate centroids
			for(int a = 0; a < centroids.size(); a++)
			{
				Vector total = new Vector();
				int count=0;
				for(int b = 0; b < points.size(); b++)
				{
					Vector temp = points.get(b);
					if(temp.getCentroid() == a)
					{
						Integer[] bah = total.getCoord();
						Integer[] bahtmp = temp.getCoord();
						bah[0] += bahtmp[0];
						bah[1] += bahtmp[1];
						total.setCoord(bah);
						//total = Vector.sum(total, temp);
						count++;
					}
				}
				
				Integer[] bah2 = total.getCoord();
				bah2[0] = bah2[0]/count;
				bah2[1] = bah2[1]/count;
				Vector centroid = new Vector(bah2);
				//Vector centroid = Vector.divide(total, count);
				centroids.set(a, centroid);
			}				
		}
		System.out.println(centroids.size());
		System.out.println(points.size());
		
		ArrayList<ArrayList<Integer[]>> retArr = new ArrayList<ArrayList<Integer[]>>();
		
		for(int i=0; i<centroids.size(); i++){
			System.out.print(i + ":");
			ArrayList<Integer[]> centroidArr = new ArrayList<Integer[]>();
			for(int c=0; c<points.size(); c++){
				if (points.get(c).centroid == i){
					//System.out.print(c + " ");
					System.out.println("(" + points.get(c).getCoord()[0] + ", " + points.get(c).getCoord()[1] + ")");
					centroidArr.add(points.get(c).getCoord());
				}
			}
			System.out.println();
			retArr.add(centroidArr);
		}
		ArrayList<Integer[]> medians = new ArrayList<Integer[]>();
		// find the median of each centroid block
		for(int aa = 0; aa < retArr.size(); aa++){
			medians.add(retArr.get(aa).get(retArr.get(aa).size()/2));
		}
		return medians;
	}
	
	
	private ArrayList<Integer[]> makeBatches(){
		ArrayList<Vector> centroids = new ArrayList<Vector>();
		for(int i = 0; i < NUMBER_OF_CENTROIDS; i++){
			centroids.add(frontierCells.get(i));
		}
		return batch(centroids, frontierCells);
	}
	
	public static double euclideanDistance(Vector first, Vector second)
	{
		double euclideanSum = 0;
		Integer[] firstInt = first.getCoord();
		Integer[] secondInt = second.getCoord();
		euclideanSum += Math.pow(firstInt[0] - secondInt[0], 2);
		euclideanSum += Math.pow(firstInt[1] - secondInt[1], 2);
		return Math.sqrt(euclideanSum);
	}
	
	private boolean hasArrivedHome(){
		
		double[] coord = LocationTracker.getCurrentLocation(); // coord[0] 
		if(coord[0] < LocationTracker.getInitialx()+BEACON_RADIUS && coord[1] < LocationTracker.getInitialy()+BEACON_RADIUS
				&& coord[0] > LocationTracker.getInitialx()-BEACON_RADIUS && coord[1] > LocationTracker.getInitialy()-BEACON_RADIUS){
			return true;
		}
		else return false;
	}
	
	/**
	 * Checks whether the robot has closed a loop
	 * @param count
	 * @return
	 */
	private boolean checkHome(int count){
		if(count >= BUFFER_TIME*1000){
			if(hasArrivedHome()){
				//boolean state = false;
				// reset location, but not bearing
				//LocationTracker.writeOdometry(LocationTracker.getInitialx(), LocationTracker.getInitialy(), LocationTracker.getCurrentBearing());
				ArrayList<Integer[]> nextLayer = new ArrayList<Integer[]>();
				myRoute.add(nextLayer);
				currentLayer++;
				// currentLayer = myRoute.size()-1;	// should be the same
				System.out.println("has arrived home!!!");
				stop(1000);
				return false;	// initiate exploration
			}
		}
		return true;		
	}
	
	private Integer [] findCenter(){
		int sumX = 0;
		int sumY = 0;
		int size = myRoute.get(0).size();
		int centerX = 0;
		int centerY = 0;
		
		for(int a = 0; a < size; a++){
			sumX += myRoute.get(0).get(a)[0];
			sumY += myRoute.get(0).get(a)[1];
		}
		
		centerX = sumX / size;
		centerY = sumY / size;
		Integer [] centerPt = {centerX, centerY};
		
		return centerPt;
	}
	
	/**
	 * Follows left wall
	 */ 
	// TODO
	private void leftWallFollow() {
		
		boolean notDone = true;
		double dtheta = 1;
		double arcFactor = dtheta * 32;
		int count = 0;
		System.out.println("feedback wall following");
		LocationTracker.motors.setSpeed(0, 0);
		pause(1000);
		while( notDone ){
			while( notDone ){			
				
				if(count >= RUNTIME*1000){
					System.out.println("Finished the run");
					notDone = false;
					break;
				}
				
				LocationTracker.motors.setSpeed(SPEED_STEP, Math.PI/arcFactor);
				System.out.println("Attempting to hug wall");
				while(LeftHandDistance > DISTANCE_FROM_WALL && FaceDistance > FACE_DISTANCE_FROM_WALL){
					//System.out.println("LeftHand = " + LeftHandDistance + ", Face = " + FaceDistance);
					pause(WAIT_TIME);
					count += WAIT_TIME;
					//calibrateYaw(.999);
				}
				if(FaceDistance < FACE_DISTANCE_FROM_WALL || LeftHandDistance < 0.5*DISTANCE_FROM_WALL) break;
				if(checkHome(count) == false) {notDone = false; break;}
				
				LocationTracker.motors.setSpeed(SPEED_STEP, -Math.PI/arcFactor);
				System.out.println("Attempting to distance from wall");
				while(LeftHandDistance < DISTANCE_FROM_WALL && FaceDistance > FACE_DISTANCE_FROM_WALL) {//&& FaceDistance > DISTANCE_FROM_WALL){
					//System.out.println("LeftHand = " + LeftHandDistance + ", Face = " + FaceDistance);
					pause(WAIT_TIME);
					count += WAIT_TIME;
					//calibrateYaw(.999);
				}
				if(FaceDistance < FACE_DISTANCE_FROM_WALL || LeftHandDistance < 0.5*DISTANCE_FROM_WALL) break;
				if(checkHome(count) == false) {notDone = false; break;}
			}
			
			if(notDone == false) {break;}
			count += leftSwipe(); // left swipe to get blind spot
			
			// Right turn until free to proceed
			LocationTracker.motors.setSpeed(0, -Math.PI/12);
			System.out.println("Right Turn");
			double theta1 = LocationTracker.getCurrentBearing();
			System.out.println("theta1 = " + theta1);
			setCertaintyFactor(.50);
			while((LeftHandDistance < DISTANCE_FROM_WALL || FaceDistance < FACE_DISTANCE_FROM_WALL) && notDone){
				pause(WAIT_TIME);
				count += WAIT_TIME;
				//System.out.println(pose.getPa());
				//calibrateYaw(1.005);
				//calibrateYaw(.0005);
				//System.out.println(pose.getPa()*1.005);
				//System.out.println("LH = " + LeftHandDistance + ", F = " + FaceDistance);
				//System.out.println(pose.getPx() + ", " + pose.getPy() + ", " + pose.getPa());
			}
			setCertaintyFactor(1.0);
			stop(250);
			if(checkHome(count) == false) {notDone = false; break;}
			double theta2 = LocationTracker.getCurrentBearing();
			System.out.println("theta2 = " + theta2);
			dtheta = Math.abs(theta1 - theta2);
			System.out.println("d(theta) = " + dtheta);
			
		}
		stop(1000);
	}
	
	/**
	 * Follows inner wall
	 */ 
	// TODO
	private void leftInnerWallFollow() {
		
		boolean notDone = true;
		double dtheta = 1;
		double arcFactor = dtheta * 32;
		int count = 0;
		System.out.println("feedback inned wall following");
		LocationTracker.motors.setSpeed(0, 0);
		pause(1000);
		while( notDone ){
			while( notDone ){			
				
				if(count >= RUNTIME*1000){
					System.out.println("Finished the run");
					notDone = false;
					break;
				}
				System.out.println("OdometryStuff: " + LeftInnerHandDistance + ", " + FaceInnerDistance);
				LocationTracker.motors.setSpeed(SPEED_STEP, Math.PI/arcFactor);
				System.out.println("Attempting to hug wall");
				while(LeftInnerHandDistance > DISTANCE_FROM_WALL && FaceInnerDistance > FACE_DISTANCE_FROM_WALL){
					System.out.println("LeftHand = " + LeftInnerHandDistance + ", Face = " + FaceInnerDistance);
					pause(WAIT_TIME);
					count += WAIT_TIME;
					//calibrateYaw(.999);
				}
				if(FaceInnerDistance < FACE_DISTANCE_FROM_WALL || LeftInnerHandDistance < 0.5*DISTANCE_FROM_WALL) break;
				if(count >= BUFFER_TIME*1000 && checkHome(count) == false) {notDone = false; break;}
				
				LocationTracker.motors.setSpeed(SPEED_STEP, -Math.PI/arcFactor);
				System.out.println("Attempting to distance from wall");
				while(LeftInnerHandDistance < DISTANCE_FROM_WALL && FaceInnerDistance > FACE_DISTANCE_FROM_WALL) {//&& FaceDistance > DISTANCE_FROM_WALL){
					//System.out.println("LeftHand = " + LeftHandDistance + ", Face = " + FaceDistance);
					pause(WAIT_TIME);
					count += WAIT_TIME;
					//calibrateYaw(.999);
				}
				if(FaceInnerDistance < FACE_DISTANCE_FROM_WALL || LeftInnerHandDistance < 0.5*DISTANCE_FROM_WALL) break;
				if(count >= BUFFER_TIME*1000  && checkHome(count) == false) {notDone = false; break;}
			}
			
			if(notDone == false) {break;}
			// Right turn until free to proceed
			LocationTracker.motors.setSpeed(0, -Math.PI/12);
			System.out.println("Right Turn");
			double theta1 = LocationTracker.getCurrentBearing();
			System.out.println("theta1 = " + theta1);
			setCertaintyFactor(.50);
			while((LeftInnerHandDistance < DISTANCE_FROM_WALL || FaceInnerDistance < FACE_DISTANCE_FROM_WALL) && notDone){
				pause(WAIT_TIME);
				count += WAIT_TIME;
			}
			setCertaintyFactor(1.0);
			stop(250);
			if(count >= BUFFER_TIME*1000 && checkHome(count) == false) notDone = false;
			double theta2 = LocationTracker.getCurrentBearing();
			System.out.println("theta2 = " + theta2);
			dtheta = Math.abs(theta1 - theta2);
			System.out.println("d(theta) = " + dtheta);
			
		}
		stop(1000);
	}
	
	/**
	 * @param goalPoint
	 * @return theta to turn to angle toward goal point
	 */
	private double createLine(Integer[] goalPoint){
		
		//double theta = LocationTracker.getCurrentBearing();
		double [] loc = LocationTracker.getCurrentLocation();
		Integer [] coord = WorldView.locToCoord(loc);
		
		int newX = goalPoint[0];
		int newY = goalPoint[1];
		
		int deltaY = newY - coord[1];
		int deltaX = newX - coord[0];
		double slope;
		int xi, yi;
		int minx = Math.min(coord[0], newX);
		int miny = Math.min(coord[1], newY);
		int maxx = Math.max(coord[0], newX);
		int maxy = Math.max(coord[1], newY);
		double angle = Math.atan2(deltaY, deltaX);
		
		if (Math.abs(deltaX) >= Math.abs(deltaY)){ //if |slope| < 1, increment with x
			slope = (double) deltaY/deltaX;
			for(xi = minx + 1; xi < maxx; xi++){
				if(slope >= 0) //if slope positive, start with miny
					yi = miny + (int) Math.round((xi - minx) * slope);
				else //if slope negative, start with maxy
					yi = maxy + (int) Math.round((xi - minx) * slope);
				WorldView.world.get(xi).get(yi).setLinePoint(true);
				WorldView.world.get(xi-1).get(yi+1).setLinePoint(true);
				WorldView.world.get(xi+1).get(yi-1).setLinePoint(true);
				// add the line to clear later
				Integer [] point = {xi, yi};
				lineToFollow.add(point);
				System.out.println(xi + "," + yi);
			}
		}
		else{									//if |slope| > 1, increment with y
			slope = (double) deltaX/deltaY;
			for(yi = miny + 1; yi < maxy; yi++){
				if(slope >= 0) //if slope positive, start with minx	
					xi = minx + (int) Math.round((yi - miny) * slope);
				else //if slope negative, start with maxx
					xi = maxx + (int) Math.round((yi - miny) * slope);
				WorldView.world.get(xi).get(yi).setLinePoint(true);		// set's as line point	
				WorldView.world.get(xi-1).get(yi+1).setLinePoint(true);
				WorldView.world.get(xi+1).get(yi-1).setLinePoint(true);
				
				// add the line to clear later
				Integer [] point = {xi, yi};
				lineToFollow.add(point);
				System.out.println(xi + "," + yi);
			}
		}
		return angle;
	}
	
	/**
	 * 
	 */
	private void clearLineToFollow(){
		lineToFollow.clear();
	}
	
	/**
	 * Turns to face a specified angle
	 * @param theta
	 */
	private void faceToAngle(double theta){
		
		stop(200);
		bearing = LocationTracker.getCurrentBearing();
		double turnAngle = bearing-theta; 
		int turnDirection = right;	// initially set turn direction to right
		
		if (bearing - theta < 0){
			turnDirection = left;
		}
		if( Math.abs(bearing - theta) > Math.PI ){	// special case
			turnAngle = 2*Math.PI - Math.abs(bearing - theta);
			turnDirection = turnDirection*(-1);	// find smaller direction
		}

		int turntime = (int)(Math.abs(turnAngle)*12/Math.PI * 1000)+1;
		LocationTracker.motors.setSpeed(0, Math.PI/12*turnDirection);	// turnDirection is either left or right
		pause(turntime);
	}
	
	/**
	 * Checks whether it has arrived to destination point
	 * @param goalPoint
	 * @return
	 */
	private boolean hasArrivedToDestination(Integer [] goalPoint){
		Integer [] loc = LocationTracker.getCurrentCoordinates();
		if(		goalPoint[0] + 4 > loc[0] && 
				goalPoint[0] - 4 < loc[0] && 
				goalPoint[1] + 4 > loc[1] &&
				goalPoint[1] - 4 < loc[1] )
		{
			System.out.println("has arrived to goal!!");
			stop(500);
			return true;
		}
		else return false;
	}
	
	
	private boolean checkGoalPoint(Integer [] goalPoint){
		
		double[] coord = LocationTracker.getCurrentLocation(); // coord[0] 
		if(coord[0] < goalPoint[0]+BEACON_RADIUS && coord[1] < goalPoint[1]+BEACON_RADIUS
				&& coord[0] > goalPoint[0]-BEACON_RADIUS && coord[1] > goalPoint[1]-BEACON_RADIUS){
			return true;
		}
		else return false;
	}
	
	/**
	 * Follows Line
	 */ 
	// TODO
	private void followLine(Integer [] goalPoint, double theta) {
		
		// i am assuming I am already facing the toward the line
		boolean notDone = true;
		double dtheta = 1;
		double arcFactor = dtheta * 32;
		int count = 0;
		System.out.println("feedback line following");
		LocationTracker.motors.setSpeed(0, 0);
		pause(1000);
		
		while(notDone){
			
			LocationTracker.motors.setSpeed(SPEED_STEP, 0);	// line following
			while(FaceDistance > DISTANCE_FROM_WALL 
				&& LeftHandDistance > DISTANCE_FROM_WALL 
				&& RightHandDistance > DISTANCE_FROM_WALL
				&& !hasArrivedToDestination(goalPoint)){	// while no obstacle detected in front
				//follow the line
				pause(WAIT_TIME);
				count += WAIT_TIME;
			}
			if(hasArrivedToDestination(goalPoint)){
				notDone = false;
				break;
			}
			int leftToleranceFactor = 1; 
			int rightToleranceFactor = 1;
			
			int leftTurn = 1;
			boolean distanceChoice = true; // true = left
			// check to see if turning left or turnin right is better
			if(LeftHandDistance<RightHandDistance){
				LocationTracker.motors.setSpeed(0, -Math.PI/12);	// turning until there is good clearance
				rightToleranceFactor = 2;
				distanceChoice = true;
				leftTurn = 1;
			}
			else{
				LocationTracker.motors.setSpeed(0, Math.PI/12);	// turning until there is good clearance
				leftToleranceFactor = 2;
				distanceChoice = false;
				leftTurn = -1;
			}
			
			while((LeftHandDistance < DISTANCE_FROM_WALL/leftToleranceFactor
					|| FaceDistance < FACE_DISTANCE_FROM_WALL
					|| RightHandDistance < DISTANCE_FROM_WALL/rightToleranceFactor) 
					&& notDone){
				pause(WAIT_TIME);
				count += WAIT_TIME;
			}
			
			locationSnapshot(LocationTracker.getCurrentCoordinates());
			setStartCheck(false);
			LocationTracker.setLineCheck(true);	// start checking to see when I get back on track
			
			System.out.println("set to true!!");
			count = 0;
			// follow left wall
			boolean started = false;
			boolean lineStatus = false;
			while(notDone && !lineStatus){
				
				if(count >= 4000 && started == false){
					setStartCheck(true);
					started = true;
				}
				
				if(!lineStatus) LocationTracker.motors.setSpeed(SPEED_STEP, leftTurn * Math.PI/32);	// attempt to hug wall
				//while(LeftHandDistance > DISTANCE_FROM_WALL && !lineStatus){ //&& LocationTracker.getLineCheck()){
				//while(distance > DISTANCE_FROM_WALL && !lineStatus){ //&& LocationTracker.getLineCheck()){
				while((distanceChoice?LeftHandDistance:RightHandDistance) > DISTANCE_FROM_WALL && !lineStatus){ //&& LocationTracker.getLineCheck()){
					pause(WAIT_TIME);
					count += WAIT_TIME;
					if(checkLineStatus()){
						lineStatus = true;
						stop(250);
					}
				}
				//if(LocationTracker.getLineCheck() == false) break;	// 3000 gives the robot a little time to move away from coord
				
				if(!lineStatus) LocationTracker.motors.setSpeed(SPEED_STEP, (-1) * leftTurn * Math.PI/32);
				//while(LeftHandDistance < DISTANCE_FROM_WALL && !lineStatus){ //&& LocationTracker.getLineCheck()) {
				//while(distance < DISTANCE_FROM_WALL && !lineStatus){ //&& LocationTracker.getLineCheck()) {
				while((distanceChoice?LeftHandDistance:RightHandDistance) < DISTANCE_FROM_WALL && !lineStatus){ //&& LocationTracker.getLineCheck()) {
					pause(WAIT_TIME);
					count += WAIT_TIME;
					if(checkLineStatus()){
						lineStatus = true;
						stop(250);
					}
				}
				//if(LocationTracker.getLineCheck() == false) break;
				
				// Note: This exits when the roomba intersects the original line again
			}
			// if found line again, then face toward original goal point again
			System.out.println("face to angle!!");
			faceToAngle(theta);
		}	
		

	}
	
	private boolean checkLineStatus(){
		
		Integer [] coordinates = LocationTracker.getCurrentCoordinates();
		if(WorldView.world.get(coordinates[0]).get(coordinates[1]).getLinePoint() && getStartCheck()){
			//LocationTracker.motors.setSpeed(0,0);
			System.out.println("set to false!!!");
			//setLineCheck(false);
			PathPlanner.setStartCheck(false);
			System.out.println(coordinates[0] + "," + coordinates[1]);
			return true;	// found its way back
		}	
		return false;
	}
	
	private void locationSnapshot(Integer [] snapshotLoc){
		this.snapshotLocation = snapshotLoc;
	}
	
	public static Integer [] getLocationSnapshot(){
		return snapshotLocation;
	}
	
	public synchronized static void setStartCheck(boolean t){
		startCheck = t;
	}
	
	public synchronized static boolean getStartCheck(){
		return startCheck; 
	}
	
	/**
	 * goBackwards
	 * goes backwards the distance of a diameter of a roomba
	 */
	private void goBackwards(int numRev){
//		int time = ((int)(4*ROOMBA_RADIUS/SPEED_STEP))*1000;
//		LocationTracker.motors.setSpeed(-SPEED_STEP, 0);
//		System.out.println("Pausing for " + time);
//		pause(time);
		
		int time,turntime,x1,x2,y1,y2;
		int backLength = myRoute.get(currentLayer-1).size();
		
		
			y2 = myRoute.get(currentLayer-1).get(backLength - 8)[1];
			y1 = LocationTracker.getCurrentCoordinates()[1];
			x2 = myRoute.get(currentLayer-1).get(backLength - 8)[0];
			x1 = LocationTracker.getCurrentCoordinates()[0];
			
			//System.out.println("Backwards:(" + x1 + "," + y1 + ")===>(" + x2 + "," + y2 + ")");
			bearing = LocationTracker.getCurrentBearing();
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
			//if(checkTerrain(x1,y1,x2,y2)){
				
			//bearing = theta;	// new bearing is theta

			turntime = (int)(Math.abs(turnAngle)*12/Math.PI * 1000)+1;
			// DEBUGGING STATEMENTS
			System.out.println("Backwards:(" + x1 + "," + y1 + ")===>(" + x2 + "," + y2 + ")");
			System.out.println("polar(" +r+","+theta+")");
			System.out.println("turnangle=" + turnAngle + " bearing=" + bearing);
			System.out.println("turntime = " + turntime);
			LocationTracker.motors.setSpeed(0, Math.PI/12*turnDirection);	// turnDirection is either left or right
			pause(turntime);
			//time = (int)r*1000;
			time = (int)(((r*WorldView.RESOLUTION)/SPEED_STEP)*1000);	// scales the coord to roughly the size of the roomba
			LocationTracker.motors.setSpeed(SPEED_STEP, 0);
			pause(time);
			
			//TODO turn to face inside hard coded
			LocationTracker.motors.setSpeed(0, Math.PI/12);
			pause(6000);
		
		
	}
	
	/**
	 * @return count
	 */
	private int leftSwipe(){
		stop(250);
		LocationTracker.motors.setSpeed(0, ROT_RATE_MED);
		pause(2000);
		stop(250);
		LocationTracker.motors.setSpeed(0, -ROT_RATE_MED);
		pause(2000);
		stop(250);
		return 4750;	// return time in ms waited inside this function
	}
	
	private void lawnmowFromLeft() {
		
		boolean notDone = true;
		double dtheta = 1;
		double arcFactor = dtheta * 32;
		int count = 0;
		System.out.println("feedback wall following");
		LocationTracker.motors.setSpeed(0, 0);
		pause(1000);
		
		while(notDone){
			
			if(count >= RUNTIME*1000) break;			
			// go straight until see wall			
			LocationTracker.motors.setSpeed(SPEED_STEP, 0);
			while(FaceDistance > FACE_DISTANCE_FROM_WALL){
				pause(250);
				count += 250;
			}
			if(count >= RUNTIME*1000) break;			

			// turn 90 degrees to the right
			LocationTracker.motors.setSpeed(0, -Math.PI/16);
			double theta = LocationTracker.getCurrentBearing();
			while(Math.abs(theta - LocationTracker.getCurrentBearing()) < Math.PI/4){
				pause(250);
				count += 250;
			}
			if(count >= RUNTIME*1000) break;			

			// go straight for a bit
			LocationTracker.motors.setSpeed(SPEED_STEP, 0);
			pause(3000);
			if(count >= RUNTIME*1000) break;			

			// turn 90 degrees to the right
			LocationTracker.motors.setSpeed(0, -Math.PI/16);
			theta = LocationTracker.getCurrentBearing();
			while(Math.abs(theta - LocationTracker.getCurrentBearing()) < Math.PI/4){
				pause(250);
				count += 250;
			}
			if(count >= RUNTIME*1000) break;			
			
			// go straight until see wall
			LocationTracker.motors.setSpeed(SPEED_STEP, 0);
			while(FaceDistance > FACE_DISTANCE_FROM_WALL){
				pause(250);
				count += 250;
			}
			if(count >= RUNTIME*1000) break;			

			// turn 90 degrees to the left
			LocationTracker.motors.setSpeed(0, Math.PI/16);
			theta = LocationTracker.getCurrentBearing();
			while(Math.abs(theta - LocationTracker.getCurrentBearing()) < Math.PI/4){
				pause(250);
				count += 250;
			}
			if(count >= RUNTIME*1000) break;			

			// go straight for a bit
			LocationTracker.motors.setSpeed(SPEED_STEP, 0);
			pause(3000);
			if(count >= RUNTIME*1000) break;			

			// turn 90 degrees to the left
			LocationTracker.motors.setSpeed(0, Math.PI/16);
			theta = LocationTracker.getCurrentBearing();
			while(Math.abs(theta - LocationTracker.getCurrentBearing()) < Math.PI/4){
				pause(250);
				count += 250;
			}
			if(count >= RUNTIME*1000) break;			

			
		}
		
		stop(1000);
	}
	
	private static void setCertaintyFactor(double perc){
		certaintyFactor = perc;
	}
	public static double getCertaintyFactor(){
		return certaintyFactor;
	}
	
	public static void setPlayerPose(PlayerPose pp){
		pose = pp;
	}
	
	/**
	 * swipes left and right 
	 */
	private void swipe() {
		System.out.println("Swiping rangefinders");
		LocationTracker.motors.setSpeed(0, ROT_RATE_MED);
		pause(2000);
		stop(500);
		LocationTracker.motors.setSpeed(0, -ROT_RATE_MED);
		pause(4000);
		stop(500);
		LocationTracker.motors.setSpeed(0, ROT_RATE_MED);
		pause(2000);
		stop(500);
	}
	
	/**
	 * @param time
	 */
	private void stop(int time){
		LocationTracker.motors.setSpeed(0, 0);
		pause(time);
	}
	
	public static void setLeftHandDistance(float distance){
		PathPlanner.LeftHandDistance = distance;
	}
	public static void setRightHandDistance(float distance){
		PathPlanner.RightHandDistance = distance;
	}
	public static void setFaceDistance(float distance){
		PathPlanner.FaceDistance = distance;
	}
	public static void setLeftInnerHandDistance(double distance){
		PathPlanner.LeftInnerHandDistance = distance;
	}
	public static void setRightInnerHandDistance(double distance){
		PathPlanner.RightInnerHandDistance = distance;
	}
	public static void setFaceInnerDistance(double distance){
		PathPlanner.FaceInnerDistance = distance;
	}
	
	/**
	 * Adds a coord to the route taken by the robot
	 * @param location
	 */
	public static void addRoute(Integer [] location){
		//if(myRoute.indexOf(location) == -1)
		//	System.out.println();
		boolean alreadyExists = false;
		for(int i = 0; i<myRoute.get(currentLayer).size(); i++){
			if(myRoute.get(currentLayer).get(i)[0].equals(location[0]) && myRoute.get(currentLayer).get(i)[1].equals(location[1])){
				alreadyExists = true;
				break;
			}				
		}
		if(alreadyExists == false)
			myRoute.get(currentLayer).add(location);
	}
	
	/**
	 * Prints the route taken
	 * @param location
	 */
	public static void printRoute(){
		for(int n = 0; n<myRoute.size(); n++){				
			for(int i = 0; i<myRoute.get(n).size(); i++){
				System.out.println("(" + myRoute.get(n).get(i)[0] + ", " + myRoute.get(n).get(i)[1] + ")");
			}
		}
	}
	
	/**
	 * @param center: the real location of the roomba in the worldview
	 * @return Integer coordinate of bottom left corner
	 */
	private Integer[] getBotLeftCorner(double[] center){
		
		Integer[] coord;
		center[0] -= ROOMBA_RADIUS;
		center[1] -= ROOMBA_RADIUS;		
		return WorldView.locToCoord(center);
	}
	
	/**
	 * readWorldViewBlock()
	 * @param x WorldView x-coordinate
	 * @param y WorldView y-coordinate
	 */
	public boolean readWorldViewBlock(int x, int y){
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
	
	
	/******************** beginPathPlanner ****************************
	 * starts the entire navigation/mapping routine
	 * @param initialBearing 
	 * @param sl 
	 * @param args
	 *****************************************************************/
	public static void beginPathPlanner(int executionTimeOut, OrderedPairDouble sl, double initialBearing) {
		int serverPort = 6665;
		PathPlanner.RUNTIME = executionTimeOut;
		PathPlanner.startingCoordinates = sl;
		PathPlanner.initialBearing = initialBearing;
		
		System.out.println("Runtime = " + PathPlanner.RUNTIME + "\nStarting Coordinates: " + 
				PathPlanner.startingCoordinates + "\nInitial Bearing: " + PathPlanner.initialBearing);
		
//		try {
//			for (int i=0; i < args.length; i++) {
//				if (args[i].equals("-server")) {
//					serverIP = args[++i];
//				} 
//				else if (args[i].equals("-port")) {
//					serverPort = Integer.valueOf(args[++i]);
//				}
//				else if (args[i].equals("-file")) {
//					fileName = args[++i];
//				}
//				else {
//					usage();
//					System.exit(1);
//				}
//			}
//		} catch(Exception e) {
//			e.printStackTrace();
//			usage();
//			System.exit(1);
//		}
		
		System.out.println("Server IP: " + serverIP);
		System.out.println("Server port: " + serverPort);
		System.out.println("File: " + fileName);
			
		new PathPlanner(serverIP, serverPort, fileName);
		
	}

}


