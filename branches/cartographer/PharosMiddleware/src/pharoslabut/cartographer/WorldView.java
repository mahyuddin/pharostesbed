package pharoslabut.cartographer;

import java.awt.geom.Point2D;
import java.util.*;
import java.io.*;

import playerclient.IRInterface;
import playerclient.IRListener;
import playerclient.structures.PlayerConstants;
import playerclient.structures.ir.PlayerIrData;


class LocationElement {
	private Integer xCoord;
	private Integer yCoord;
	private double confidence; // percentage 
	private Integer traversedCount;
	private boolean isFrontier;
	private boolean isPath;
	private boolean isPartOfLine;
	
	public LocationElement (Integer x, Integer y) {
		this.xCoord = x;
		this.yCoord = y;
		this.confidence = .5;  // initialized to 50% 
		this.traversedCount = 0;    // initialized to 0, unexplored
		this.isFrontier = false;
		this.isPath = false;
		this.isPartOfLine = false;
	}	
	
	@Override
	public String toString() {
		return "[(" + xCoord + "," + yCoord + ") " + confidence + ", " + traversedCount + "]";
	}

	/**************** GETTERS AND SETTERS ******************/
	public Integer getxCoord() { return xCoord; }
	public void setxCoord(Integer x) { this.xCoord = x;	}
	public Integer getyCoord() { return yCoord;	}
	public void setyCoord(Integer y) { this.yCoord = y;	}
	public double getConfidence() {	return confidence; }
	public void setConfidence(double c) { this.confidence = c; }
	public Integer getTraversed() { return traversedCount; }
	public void setTraversed(Integer i) { this.traversedCount = i; }
	public void setFrontier(boolean t){ this.isFrontier = t; }	// added by felix
	public boolean getFrontier(){ return isFrontier; }	// added by felix
	public void setPath(){ this.isPath = true; }	// added by felix
	public boolean getPath(){ return isPath; }	// added by felix
	public void setLinePoint(boolean t){ this.isPartOfLine = t; }
	public boolean getLinePoint(){ return isPartOfLine; }
	/*************** END GETTERS AND SETTERS ****************/
	
	public void incTraversed() { this.traversedCount++; }
	
}


class OrderedPair {
	
	Integer x;
	Integer y;
	
	public OrderedPair(Integer xValue, Integer yValue) {
		this.x = xValue;
		this.y = yValue;
	}
	
	/**************** GETTERS AND SETTERS ******************/
	public Integer getX() { return x; }
	public void setX(Integer x) { this.x = x; }
	public Integer getY() {	return y; }
	public void setY(Integer y) { this.y = y; }
	/*************** END GETTERS AND SETTERS ****************/
	
	
	@Override
	public String toString() {
		return ( "(" + this.x + "," + this.y + ")" );
	}

}


class OrderedPairConfidence extends OrderedPair {

	double deltaConfidence;

	public OrderedPairConfidence(Integer xValue, Integer yValue, double dC) {
		super(xValue, yValue);
		this.deltaConfidence = dC;
	}
	
	/**************** GETTERS AND SETTERS ******************/
	public double getDeltaConfidence() { return deltaConfidence; }
	public void setDeltaConfidence(double deltaConfidence) { this.deltaConfidence = deltaConfidence; }
	/*************** END GETTERS AND SETTERS ****************/
}


class OrderedPairDouble {
	
	double x;
	double y;
	
	public OrderedPairDouble(double xValue, double yValue) {
		this.x = xValue;
		this.y = yValue;
	}
	
	/**************** GETTERS AND SETTERS ******************/
	public double getX() { return x; }
	public void setX(double x) { this.x = x; }
	public double getY() {	return y; }
	public void setY(double y) { this.y = y; }
	/*************** END GETTERS AND SETTERS ****************/
	
	
	@Override
	public String toString() {
		return ( "(" + this.x + "," + this.y + ")" );
	}

}

public class WorldView implements IRListener {
	
	public static long numIRreadings = 0;
	static ArrayList<ArrayDeque<Float>> dq;
	
	public static FileWriter fstream; 
    public static BufferedWriter fout; 
	
	public static final int WORLD_SIZE = 180;					// initial dimensions of "world" (below)
	public static ArrayList<ArrayList<LocationElement>> world; // full 2-D matrix, world view
	public static ArrayList<ArrayList<LocationElement>> sampleworld; // full 2-D matrix, world view
	public static ArrayList<OrderedPair> pathTracker; 
	
	public static final double RESOLUTION 				= 0.05; // 5 cm
	public static final double MIN_USEFUL_IR_DISTANCE 	= 0.22; // minimum short range distance is 22 cm 
	public static final double MAX_USEFUL_IR_DISTANCE 	= 1.50; // max short range distance is 175 cm
																// looks like the max long range distance detectable is ~550 cm 
																// but more accurate when under 300 cm
	public static final double MAX_POSSIBLE_IR_DISTANCE = 3.00;
	public static final double ROOMBA_RADIUS 			= 0.17; // radius of the roomba from center point out = 17cm
	
	// the POSE values are distance from center of roomba to each IR sensor
	// x value is distance from center left and right to back and front sensor
	// y value is distance from center up and down to left midpoint and right midpoint
	public static final double [] FRONT_LEFT_IR_POSE		= {0.09, 0.10};		// done measuring
	public static final double [] FRONT_CENTER_IR_POSE		= {0.12, 0}; 		// done measuring
	public static final double [] FRONT_RIGHT_IR_POSE		= {0.09, -0.10};	// done measuring
	public static final double [] REAR_LEFT_IR_POSE			= {-0.175, 0.10};	// done measuring
	public static final double [] REAR_CENTER_IR_POSE		= {-0.19, 0};		// done measuring
	public static final double [] REAR_RIGHT_IR_POSE		= {-0.175, -0.10};	// done measuring
	
	public static final double FRONT_LEFT_IR_ANGLE			= Math.PI/4; 	// -45 deg (45 deg left of forward)
	public static final double FRONT_CENTER_IR_ANGLE		= 0; 			// 0 deg (directly ahead)
	public static final double FRONT_RIGHT_IR_ANGLE			= -Math.PI/4; 	// -45 deg (45 deg right of forward)
	public static final double REAR_LEFT_IR_ANGLE			= 3*Math.PI/4;	// -135 deg (45 deg left of forward)
	public static final double REAR_CENTER_IR_ANGLE			= Math.PI; 		// 180 deg (directly behind)
	public static final double REAR_RIGHT_IR_ANGLE			= -3*Math.PI/4; 	// 135 deg (135 deg right of forward)
	
	// uses pythagorean theorem to find hypotenuse (distance from Roomba's center) of the IR sensor's position 
	public static final double FRONT_LEFT_IR_POSE_HYP		= Math.sqrt(Math.pow(FRONT_LEFT_IR_POSE[0], 2) + Math.pow(FRONT_LEFT_IR_POSE[1], 2));	//
	public static final double FRONT_CENTER_IR_POSE_HYP		= Math.sqrt(Math.pow(FRONT_CENTER_IR_POSE[0], 2) + Math.pow(FRONT_CENTER_IR_POSE[1], 2));	//
	public static final double FRONT_RIGHT_IR_POSE_HYP		= Math.sqrt(Math.pow(FRONT_RIGHT_IR_POSE[0], 2) + Math.pow(FRONT_RIGHT_IR_POSE[1], 2));	//
	public static final double REAR_LEFT_IR_POSE_HYP		= Math.sqrt(Math.pow(REAR_LEFT_IR_POSE[0], 2) + Math.pow(REAR_LEFT_IR_POSE[1], 2));	//	
	public static final double REAR_CENTER_IR_POSE_HYP		= Math.sqrt(Math.pow(REAR_CENTER_IR_POSE[0], 2) + Math.pow(REAR_CENTER_IR_POSE[1], 2));	//
	public static final double REAR_RIGHT_IR_POSE_HYP		= Math.sqrt(Math.pow(REAR_RIGHT_IR_POSE[0], 2) + Math.pow(REAR_RIGHT_IR_POSE[1], 2));	//
	
	// added by felix for frontier creation and grouping
	
	/* Y  | | | | | | | | | | | | | | | | | | | | | | | | 20,20
	 *    | | | | | | | | | | | | | | | | | | | | | | | | 
	 * ^  | | | | | | | | | | | | | | | | | | | | | | | |
	 * |  | | | | | | | | | | | | | | | | | | | | | | | |
	 * |  | | | | | | | | | | | | | | | | | | | | | | | |
	 * |  | | | | | | | | | | | | | | | | | | | | | | | |
	 *    | | | | | | | | | | | | | | | | | | | | | | | |
	 * y  | | | | | | | | | | | | | | | | | | | | | | | | 20,10 
	 *    | | | | | | | | | | | | | | | | | | | | | | | |
	 * l  | | | | | | | | | | | | | | | | | | | | | | | |
	 * l  | | | | | | | | | | | | | | | | | | | | | | | |
	 * a  | | | | | | | | | | | | | | | | | | | | | | | |
	 * m  | | | | | | | | | | | | | | | | | | | | | | | |
	 * s  | | | | | | | | | | | | | | | | | | | | | | | |
	 *    | | | | | | | | | | | | | | | | | | | | | | | |
	 *    | | | | | | | | | | | | | | | | | | | | | | | | 20,0 
	 *0,0       small x   ----->   large x        
	 */
	
	
	public WorldView() {
		
		world = new ArrayList<ArrayList<LocationElement>>();
		sampleworld = new ArrayList<ArrayList<LocationElement>>();
		
//		world = Collections.synchronizedList(new ArrayList<ArrayList<LocationElement>>(WORLD_SIZE));
	
		for (int i = 0; i < WORLD_SIZE; i++) { // iterate through each x coordinate
 
			//add a new list for all the y coordinates at that x coordinate
			world.add(new ArrayList<LocationElement>()); 
			sampleworld.add(new ArrayList<LocationElement>()); 
			
			for (int j = 0; j < WORLD_SIZE; j++) { // iterate through each y coordinate
				(world.get(i)).add(new LocationElement(i,j)); // add a LocationElement at that coordinate
				(sampleworld.get(i)).add(new LocationElement(i,j)); 
			}	
		}
		
		pathTracker = new ArrayList<OrderedPair>();
		
		try {
			fstream = new FileWriter("world.txt");
			fout = new BufferedWriter(fstream);
		} 
		catch (Exception e) {
		      System.err.println("Error opening file stream for 'world.txt': " + e.getMessage());
		}
		
		
		/////////// IR INTERFACE ///////////////
		IRInterface ir = (PathPlanner.client).requestInterfaceIR(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (ir == null) {
			System.out.println("unable to connect to IR interface");
			System.exit(1);
		}

		ir.addIRListener(this);
		
	}
	
	public static synchronized void createSampleWorldView(){
		int i,j = 0;
		for(i=0;i<WORLD_SIZE; i++){
			for(j=0; j<WORLD_SIZE;j++)
				((sampleworld.get(i)).get(j)).setConfidence(0);
		}
		
		//walls
		for(i=0;i<WORLD_SIZE; i++){
			for(j=0;j<WORLD_SIZE;j++)
				if(i == 0 || i == 71 || j == 0 || j == 71)
					((sampleworld.get(i)).get(j)).setConfidence(1);
		}
		
		//obstacle one
		for(i=10;i<20; i++){
			for(j=0; j<10;j++)
				((sampleworld.get(i)).get(j)).setConfidence(1);
		}
		for(i=11;i<19; i++){
			for(j=1; j<9;j++)
				((sampleworld.get(i)).get(j)).setConfidence(0);
		}
		
		//obstacle 2
		for(i=30;i<40; i++){
			for(j=30;j<40;j++)
				((sampleworld.get(i)).get(j)).setConfidence(1);
		}
		for(i=31;i<39; i++){
			for(j=31;j<39;j++)
				((sampleworld.get(i)).get(j)).setConfidence(0);
		}
		
		//obstacle 3
		for(i=15;i<25; i++){
			for(j=35;j<45;j++)
				((sampleworld.get(i)).get(j)).setConfidence(1);
		}
		for(i=16;i<24; i++){
			for(j=36;j<44;j++)
				((sampleworld.get(i)).get(j)).setConfidence(0);
		}
	}
	
	/**
	 * This is called by LocationTracker whenever updateLocation is called. <br>
	 * Clears the confidence values at the roomba's location, which spans several coordinates 
	 * depending on <b>ROOMBA_RADIUS</b> and the <b>RESOLUTION</b> of the map  
	 * @author Kevin
	 * @param xPos current x position of the Roomba (the center point of the robot)
	 * @param yPos current y position of the Roomba (the center point of the robot)
	 */
	public static synchronized void recordLocation(double xPos, double yPos) {
		
		double [] pos = {xPos, yPos};
		Integer [] coordinates = locToCoord(pos); // coordinates[0] is xCoord, coordinates[1] is yCoord
		PathPlanner.addRoute(coordinates);
		setPathCell(new OrderedPair(coordinates[0], coordinates[1]));		
		
		// "coordinates" corresponds to the middle of the Roomba, 
		//    and we'll need to clear the locations that cover the whole circular area of the Roomba  
		
		// calculate the boundaries of Roomba in terms of position (in meters)
		double [] upperLeftPos = {xPos - ROOMBA_RADIUS, yPos + ROOMBA_RADIUS};
		double [] lowerRightPos = {xPos + ROOMBA_RADIUS, yPos - ROOMBA_RADIUS};
//		System.out.println("upperLeftPos: " + upperLeftPos[0] + ", " + upperLeftPos[1]);
//		System.out.println("lowerRightPos: " + lowerRightPos[0] + ", " + lowerRightPos[1]);
		
		// convert Roomba boundaries to coordinates
		Integer [] upperLeftCoord = locToCoord(upperLeftPos);
		Integer [] lowerRightCoord = locToCoord(lowerRightPos);
		
		
		
		// add all coordinates from upperLeftCoord to lowerRightCoord to the locationsToClear list
		for (int x = upperLeftCoord[0]; x <= lowerRightCoord[0]; x++) { // iterate through all xCoords, from left to right 
			for (int y = lowerRightCoord[1]; y <= upperLeftCoord[1]; y++) { // iterate through all yCoords, from top to bottom
				
				// since we have passed through this location, we are full confident an object doesn't exist there
				//   so the confidence value should be set to zero to indicate no obstacle is present
				WorldView.writeConfidence(x, y, 0); 
				WorldView.increaseTraversed(x, y);
				WorldView.setFrontierCell(new OrderedPair(x,y), false);
				
				WorldView.pathTracker.add(new OrderedPair(x,y));
				
			}
		}
		
		
		//PathPlanner.setLeftInnerHandDistance(leftDistance());
		//PathPlanner.setFaceInnerDistance(frontDistance());
		
//		System.out.println("Current Location (m): " + LocationTracker.printCurrentLocation());
//		System.out.println("Current Coordinates: " + LocationTracker.printCurrentCoordinates());
		
	}
	
	
	/**
	 * calculates the location of obstacles based on range values from 6 IR sensors
	 * @author Kevin Boos
	 * @param dist: an array of the 6 IR distance values (float types). order: FL, FC, FR, RR, RC, RL
	 */
	public static synchronized void recordObstacles(float [] distIR, double [] curLoc) {
		// extract IR data from dist[], dist data is in mm, convert back to m
		float frontLeftRange 	= WorldView.calibrateIR(distIR[0] / 1000);
		float frontCenterRange 	= WorldView.calibrateIR(distIR[1] / 1000);
		float frontRightRange 	= WorldView.calibrateIR(distIR[2] / 1000);
		float rearLeftRange 	= WorldView.calibrateIR(distIR[5] / 1000); // [5] is actually the rearLeft
		float rearCenterRange 	= WorldView.calibrateIR(distIR[4] / 1000);
		float rearRightRange 	= WorldView.calibrateIR(distIR[3] / 1000); // [3] is actually the rearRight
		
		//System.out.println("FL=" + frontLeftRange + ", FC=" + frontCenterRange + ", FR=" + frontRightRange + ", RL=" + rearLeftRange + ", RC=" + rearCenterRange + ", RR=" + rearRightRange);
		
		double xPos 	= curLoc[0]; 
		double yPos 	= curLoc[1];
		double curAngle	= curLoc[2];
		
		Point2D.Double obstaclePoint 			= new Point2D.Double();
		Point2D.Double frontLeftSensorPoint		= new Point2D.Double();
		Point2D.Double frontCenterSensorPoint 	= new Point2D.Double();
		Point2D.Double frontRightSensorPoint 	= new Point2D.Double();
		Point2D.Double rearLeftSensorPoint 		= new Point2D.Double();
		Point2D.Double rearCenterSensorPoint 	= new Point2D.Double();
		Point2D.Double rearRightSensorPoint		= new Point2D.Double();
			
		// 2-D ArrayList of spaces to clear 
		// (these need to be calculated and added to the list before the "synchronized (world)" block below)
		ArrayList<OrderedPairConfidence> locationsToDecrease = new ArrayList<OrderedPairConfidence>();
		ArrayList<OrderedPairConfidence> locationsToIncrease = new ArrayList<OrderedPairConfidence>();
		
		locationsToDecrease.clear();
		locationsToIncrease.clear();
		
		
		Integer [] curCoords = locToCoord(curLoc); // this might be useful later
		Integer [] obstacleCoord, sensorCoord;
		double [] obstaclePos = {0,0}; // 
		double [] sensorPos = {0,0};
		
	
		
		// calculate where the object should be recorded
		
		
		// Tests for calculation correctness:
		// __________________________________________________________
		// | Sensor Position	| SensorPoint		| ObstaclePoint	|
		// |--------------------|-------------------|---------------|
		// | FrontLeft			| correct			| untested		|
		// | FrontCenter		| correct			| untested		|
		// | FrontRight			| correct			| untested		|
		// | rearLeft			| correct			| untested		|
		// | rearCenter			| correct			| untested		|
		// | rearRight			| correct			| untested		|
		// ----------------------------------------------------------
		
		/////////// Added by felix for frontier mapping /////////////////
		if (frontLeftRange > MAX_USEFUL_IR_DISTANCE && frontLeftRange < MAX_POSSIBLE_IR_DISTANCE) {
			frontLeftSensorPoint.setLocation(	
					curLoc[0] + FRONT_LEFT_IR_POSE_HYP*Math.cos(curAngle + Math.atan(FRONT_LEFT_IR_POSE[1]/FRONT_LEFT_IR_POSE[0])),
					curLoc[1] + FRONT_LEFT_IR_POSE_HYP*Math.sin(curAngle + Math.atan(FRONT_LEFT_IR_POSE[1]/FRONT_LEFT_IR_POSE[0]))  );			
			obstaclePoint.setLocation(	
					frontLeftSensorPoint.getX() + MAX_USEFUL_IR_DISTANCE*Math.cos(curAngle + FRONT_LEFT_IR_ANGLE),
					frontLeftSensorPoint.getY() + MAX_USEFUL_IR_DISTANCE*Math.sin(curAngle + FRONT_LEFT_IR_ANGLE)  );						
			obstaclePos[0] = obstaclePoint.getX();
			obstaclePos[1] = obstaclePoint.getY();
			sensorPos[0] = frontLeftSensorPoint.getX();
			sensorPos[1] = frontLeftSensorPoint.getY();	
			obstacleCoord = WorldView.locToCoord(obstaclePos);
			sensorCoord = WorldView.locToCoord(sensorPos);
			WorldView.decreaseLineConfidence(locationsToDecrease, sensorCoord[0], obstacleCoord[0], sensorCoord[1], obstacleCoord[1]);			
			if( WorldView.readConfidence(obstacleCoord[0], obstacleCoord[1]) == 0.5 )
				WorldView.setFrontierCell(new OrderedPair(obstacleCoord[0], obstacleCoord[1]), true);
		}
		if (frontCenterRange > MAX_USEFUL_IR_DISTANCE && frontCenterRange < MAX_POSSIBLE_IR_DISTANCE) {
			frontCenterSensorPoint.setLocation(	
					curLoc[0] + FRONT_CENTER_IR_POSE_HYP*Math.cos(curAngle),
					curLoc[1] + FRONT_CENTER_IR_POSE_HYP*Math.sin(curAngle)  );			
			obstaclePoint.setLocation(	
					frontCenterSensorPoint.getX() + MAX_USEFUL_IR_DISTANCE*Math.cos(curAngle + FRONT_CENTER_IR_ANGLE),
					frontCenterSensorPoint.getY() + MAX_USEFUL_IR_DISTANCE*Math.sin(curAngle + FRONT_CENTER_IR_ANGLE)  );			
			obstaclePos[0] = obstaclePoint.getX();
			obstaclePos[1] = obstaclePoint.getY();
			sensorPos[0] = frontCenterSensorPoint.getX();
			sensorPos[1] = frontCenterSensorPoint.getY();
			obstacleCoord = WorldView.locToCoord(obstaclePos);
			sensorCoord = WorldView.locToCoord(sensorPos);
			WorldView.decreaseLineConfidence(locationsToDecrease, sensorCoord[0], obstacleCoord[0], sensorCoord[1], obstacleCoord[1]);			
			if( WorldView.readConfidence(obstacleCoord[0], obstacleCoord[1]) == 0.5 )
				WorldView.setFrontierCell(new OrderedPair(obstacleCoord[0], obstacleCoord[1]), true);		
		}
		if (frontRightRange > MAX_USEFUL_IR_DISTANCE && frontRightRange < MAX_POSSIBLE_IR_DISTANCE) {
			frontRightSensorPoint.setLocation(	
					curLoc[0] + FRONT_RIGHT_IR_POSE_HYP*Math.cos(curAngle + Math.atan(FRONT_RIGHT_IR_POSE[1]/FRONT_RIGHT_IR_POSE[0])),
					curLoc[1] + FRONT_RIGHT_IR_POSE_HYP*Math.sin(curAngle + Math.atan(FRONT_RIGHT_IR_POSE[1]/FRONT_RIGHT_IR_POSE[0]))  );
			obstaclePoint.setLocation(	
					frontRightSensorPoint.getX() + MAX_USEFUL_IR_DISTANCE*Math.cos(curAngle + FRONT_RIGHT_IR_ANGLE),
					frontRightSensorPoint.getY() + MAX_USEFUL_IR_DISTANCE*Math.sin(curAngle + FRONT_RIGHT_IR_ANGLE)  );		
			obstaclePos[0] = obstaclePoint.getX();
			obstaclePos[1] = obstaclePoint.getY();
			sensorPos[0] = frontRightSensorPoint.getX();
			sensorPos[1] = frontRightSensorPoint.getY();
			obstacleCoord = WorldView.locToCoord(obstaclePos);
			sensorCoord = WorldView.locToCoord(sensorPos);
			WorldView.decreaseLineConfidence(locationsToDecrease, sensorCoord[0], obstacleCoord[0], sensorCoord[1], obstacleCoord[1]);
			if( WorldView.readConfidence(obstacleCoord[0], obstacleCoord[1]) == 0.5 )
				WorldView.setFrontierCell(new OrderedPair(obstacleCoord[0], obstacleCoord[1]), true);		
		}
		if (rearLeftRange > MAX_USEFUL_IR_DISTANCE && rearLeftRange < MAX_POSSIBLE_IR_DISTANCE) {
			rearLeftSensorPoint.setLocation(	
					curLoc[0] + REAR_LEFT_IR_POSE_HYP*Math.cos(curAngle + Math.atan(REAR_LEFT_IR_POSE[1]/REAR_LEFT_IR_POSE[0]) + Math.PI),
					curLoc[1] + REAR_LEFT_IR_POSE_HYP*Math.sin(curAngle + Math.atan(REAR_LEFT_IR_POSE[1]/REAR_LEFT_IR_POSE[0]) + Math.PI)  );
			obstaclePoint.setLocation(	
					rearLeftSensorPoint.getX() + MAX_USEFUL_IR_DISTANCE*Math.cos(curAngle + REAR_LEFT_IR_ANGLE),
					rearLeftSensorPoint.getY() + MAX_USEFUL_IR_DISTANCE*Math.sin(curAngle + REAR_LEFT_IR_ANGLE)  );
			obstaclePos[0] = obstaclePoint.getX();
			obstaclePos[1] = obstaclePoint.getY();
			sensorPos[0] = rearLeftSensorPoint.getX();
			sensorPos[1] = rearLeftSensorPoint.getY();
			obstacleCoord = WorldView.locToCoord(obstaclePos);
			sensorCoord = WorldView.locToCoord(sensorPos);
			WorldView.decreaseLineConfidence(locationsToDecrease, sensorCoord[0], obstacleCoord[0], sensorCoord[1], obstacleCoord[1]);		
			if( WorldView.readConfidence(obstacleCoord[0], obstacleCoord[1]) == 0.5 )
				WorldView.setFrontierCell(new OrderedPair(obstacleCoord[0], obstacleCoord[1]), true);		
		}
		if (rearCenterRange > MAX_USEFUL_IR_DISTANCE && rearCenterRange < MAX_POSSIBLE_IR_DISTANCE) {
			rearCenterSensorPoint.setLocation(	
					curLoc[0] + REAR_CENTER_IR_POSE_HYP*Math.cos(curAngle + Math.PI),
					curLoc[1] + REAR_CENTER_IR_POSE_HYP*Math.sin(curAngle + Math.PI)  );
			obstaclePoint.setLocation(	
					rearCenterSensorPoint.getX() + MAX_USEFUL_IR_DISTANCE*Math.cos(curAngle + REAR_CENTER_IR_ANGLE),
					rearCenterSensorPoint.getY() + MAX_USEFUL_IR_DISTANCE*Math.sin(curAngle + REAR_CENTER_IR_ANGLE)  );
			obstaclePos[0] = obstaclePoint.getX();
			obstaclePos[1] = obstaclePoint.getY();
			sensorPos[0] = rearCenterSensorPoint.getX();
			sensorPos[1] = rearCenterSensorPoint.getY();					
			obstacleCoord = WorldView.locToCoord(obstaclePos);
			sensorCoord = WorldView.locToCoord(sensorPos);
			WorldView.decreaseLineConfidence(locationsToDecrease, sensorCoord[0], obstacleCoord[0], sensorCoord[1], obstacleCoord[1]);					
			if( WorldView.readConfidence(obstacleCoord[0], obstacleCoord[1]) == 0.5 )
				WorldView.setFrontierCell(new OrderedPair(obstacleCoord[0], obstacleCoord[1]), true);	
		}
		if (rearRightRange > MAX_USEFUL_IR_DISTANCE && rearRightRange < MAX_POSSIBLE_IR_DISTANCE) {
			rearRightSensorPoint.setLocation(	
					curLoc[0] + REAR_RIGHT_IR_POSE_HYP*Math.cos(curAngle + Math.atan(REAR_RIGHT_IR_POSE[1]/REAR_RIGHT_IR_POSE[0]) + Math.PI),												
					curLoc[1] + REAR_RIGHT_IR_POSE_HYP*Math.sin(curAngle + Math.atan(REAR_RIGHT_IR_POSE[1]/REAR_RIGHT_IR_POSE[0]) + Math.PI)  );
			obstaclePoint.setLocation(	
					rearRightSensorPoint.getX() + MAX_USEFUL_IR_DISTANCE*Math.cos(curAngle + REAR_RIGHT_IR_ANGLE),
					rearRightSensorPoint.getY() + MAX_USEFUL_IR_DISTANCE*Math.sin(curAngle + REAR_RIGHT_IR_ANGLE)  );
			obstaclePos[0] = obstaclePoint.getX();
			obstaclePos[1] = obstaclePoint.getY();
			sensorPos[0] = rearRightSensorPoint.getX();
			sensorPos[1] = rearRightSensorPoint.getY();
			obstacleCoord = WorldView.locToCoord(obstaclePos);
			sensorCoord = WorldView.locToCoord(sensorPos);
			WorldView.decreaseLineConfidence(locationsToDecrease, sensorCoord[0], obstacleCoord[0], sensorCoord[1], obstacleCoord[1]);			
			if( WorldView.readConfidence(obstacleCoord[0], obstacleCoord[1]) == 0.5 )
				WorldView.setFrontierCell(new OrderedPair(obstacleCoord[0], obstacleCoord[1]), true);	
		}
		////////// END FELIX & ALEX MODIFICATIONS /////////////
		
		
		if ((frontLeftRange >= MIN_USEFUL_IR_DISTANCE) && (frontLeftRange <= MAX_USEFUL_IR_DISTANCE)) {
			frontLeftSensorPoint.setLocation(	curLoc[0] + FRONT_LEFT_IR_POSE_HYP*Math.cos(curAngle + Math.atan(FRONT_LEFT_IR_POSE[1]/FRONT_LEFT_IR_POSE[0])),
												curLoc[1] + FRONT_LEFT_IR_POSE_HYP*Math.sin(curAngle + Math.atan(FRONT_LEFT_IR_POSE[1]/FRONT_LEFT_IR_POSE[0]))  );
		
			obstaclePoint.setLocation(	frontLeftSensorPoint.getX() + frontLeftRange*Math.cos(curAngle + FRONT_LEFT_IR_ANGLE),
										frontLeftSensorPoint.getY() + frontLeftRange*Math.sin(curAngle + FRONT_LEFT_IR_ANGLE)  );
			
			
			obstaclePos[0] = obstaclePoint.getX();
			obstaclePos[1] = obstaclePoint.getY();
			sensorPos[0] = frontLeftSensorPoint.getX();
			sensorPos[1] = frontLeftSensorPoint.getY();
						
			obstacleCoord = WorldView.locToCoord(obstaclePos);
			sensorCoord = WorldView.locToCoord(sensorPos);
			
//			System.out.println("FL Coord: (" + sensorCoord[0] + "," + sensorCoord[1] + ")");
			
			locationsToIncrease.add(new OrderedPairConfidence(obstacleCoord[0], obstacleCoord[1], WorldView.rangeBasedConfidenceIncrease(frontLeftRange)));	
			
			// add all the coordinates that are covered by "lineToClear" to the "locationsToDecrease" list
			WorldView.decreaseLineConfidence(locationsToDecrease, sensorCoord[0], obstacleCoord[0], sensorCoord[1], obstacleCoord[1]);
			WorldView.setFrontierCell(new OrderedPair(obstacleCoord[0], obstacleCoord[1]), false);

		}
		
		if ((frontCenterRange >= MIN_USEFUL_IR_DISTANCE) && (frontCenterRange <= MAX_USEFUL_IR_DISTANCE)) {
			// for the front center IR, the curAngle is equal to the sensor's angle from the center
			frontCenterSensorPoint.setLocation(	curLoc[0] + FRONT_CENTER_IR_POSE_HYP*Math.cos(curAngle),
												curLoc[1] + FRONT_CENTER_IR_POSE_HYP*Math.sin(curAngle)  );

			obstaclePoint.setLocation(	frontCenterSensorPoint.getX() + frontCenterRange*Math.cos(curAngle + FRONT_CENTER_IR_ANGLE),
										frontCenterSensorPoint.getY() + frontCenterRange*Math.sin(curAngle + FRONT_CENTER_IR_ANGLE)  );

			obstaclePos[0] = obstaclePoint.getX();
			obstaclePos[1] = obstaclePoint.getY();
			sensorPos[0] = frontCenterSensorPoint.getX();
			sensorPos[1] = frontCenterSensorPoint.getY();
						
			obstacleCoord = WorldView.locToCoord(obstaclePos);
			sensorCoord = WorldView.locToCoord(sensorPos);
			
//			System.out.println("FC Coord: (" + sensorCoord[0] + "," + sensorCoord[1] + ")");
			
			locationsToIncrease.add(new OrderedPairConfidence(obstacleCoord[0], obstacleCoord[1], WorldView.rangeBasedConfidenceIncrease(frontCenterRange)));	
			
			// add all the coordinates that are covered by "lineToClear" to the "locationsToDecrease" list
			WorldView.decreaseLineConfidence(locationsToDecrease, sensorCoord[0], obstacleCoord[0], sensorCoord[1], obstacleCoord[1]);
			WorldView.setFrontierCell(new OrderedPair(obstacleCoord[0], obstacleCoord[1]), false);

		}

		if ((frontRightRange >= MIN_USEFUL_IR_DISTANCE) && (frontRightRange <= MAX_USEFUL_IR_DISTANCE)) {
			frontRightSensorPoint.setLocation(	curLoc[0] + FRONT_RIGHT_IR_POSE_HYP*Math.cos(curAngle + Math.atan(FRONT_RIGHT_IR_POSE[1]/FRONT_RIGHT_IR_POSE[0])),
												curLoc[1] + FRONT_RIGHT_IR_POSE_HYP*Math.sin(curAngle + Math.atan(FRONT_RIGHT_IR_POSE[1]/FRONT_RIGHT_IR_POSE[0]))  );

			obstaclePoint.setLocation(	frontRightSensorPoint.getX() + frontRightRange*Math.cos(curAngle + FRONT_RIGHT_IR_ANGLE),
										frontRightSensorPoint.getY() + frontRightRange*Math.sin(curAngle + FRONT_RIGHT_IR_ANGLE)  );
			
			obstaclePos[0] = obstaclePoint.getX();
			obstaclePos[1] = obstaclePoint.getY();
			sensorPos[0] = frontRightSensorPoint.getX();
			sensorPos[1] = frontRightSensorPoint.getY();
						
			obstacleCoord = WorldView.locToCoord(obstaclePos);
			sensorCoord = WorldView.locToCoord(sensorPos);
			
//			System.out.println("FR Coord: (" + sensorCoord[0] + "," + sensorCoord[1] + ")");
			
			locationsToIncrease.add(new OrderedPairConfidence(obstacleCoord[0], obstacleCoord[1], WorldView.rangeBasedConfidenceIncrease(frontRightRange)));	
			
			// add all the coordinates that are covered by "lineToClear" to the "locationsToDecrease" list
			WorldView.decreaseLineConfidence(locationsToDecrease, sensorCoord[0], obstacleCoord[0], sensorCoord[1], obstacleCoord[1]);
			WorldView.setFrontierCell(new OrderedPair(obstacleCoord[0], obstacleCoord[1]), false);

		}
		
		if ((rearLeftRange >= MIN_USEFUL_IR_DISTANCE) && (rearLeftRange <= MAX_USEFUL_IR_DISTANCE)) {
			rearLeftSensorPoint.setLocation(	curLoc[0] + REAR_LEFT_IR_POSE_HYP*Math.cos(curAngle + Math.atan(REAR_LEFT_IR_POSE[1]/REAR_LEFT_IR_POSE[0]) + Math.PI),
												curLoc[1] + REAR_LEFT_IR_POSE_HYP*Math.sin(curAngle + Math.atan(REAR_LEFT_IR_POSE[1]/REAR_LEFT_IR_POSE[0]) + Math.PI)  );
			
			obstaclePoint.setLocation(	rearLeftSensorPoint.getX() + rearLeftRange*Math.cos(curAngle + REAR_LEFT_IR_ANGLE),
										rearLeftSensorPoint.getY() + rearLeftRange*Math.sin(curAngle + REAR_LEFT_IR_ANGLE)  );
			
			obstaclePos[0] = obstaclePoint.getX();
			obstaclePos[1] = obstaclePoint.getY();
			sensorPos[0] = rearLeftSensorPoint.getX();
			sensorPos[1] = rearLeftSensorPoint.getY();
						
			obstacleCoord = WorldView.locToCoord(obstaclePos);
			sensorCoord = WorldView.locToCoord(sensorPos);
			
//			System.out.println("RL Coord: (" + sensorCoord[0] + "," + sensorCoord[1] + ")");
			
			locationsToIncrease.add(new OrderedPairConfidence(obstacleCoord[0], obstacleCoord[1], WorldView.rangeBasedConfidenceIncrease(rearLeftRange)));	
			
			// add all the coordinates that are covered by "lineToClear" to the "locationsToDecrease" list
			WorldView.decreaseLineConfidence(locationsToDecrease, sensorCoord[0], obstacleCoord[0], sensorCoord[1], obstacleCoord[1]);
			WorldView.setFrontierCell(new OrderedPair(obstacleCoord[0], obstacleCoord[1]), false);

		}
		
		if ((rearCenterRange >= MIN_USEFUL_IR_DISTANCE) && (rearCenterRange <= MAX_USEFUL_IR_DISTANCE)) {
			// for the rear center IR, the curAngle + PI is equal to the sensor's angle from the center
			rearCenterSensorPoint.setLocation(	curLoc[0] + REAR_CENTER_IR_POSE_HYP*Math.cos(curAngle + Math.PI),
												curLoc[1] + REAR_CENTER_IR_POSE_HYP*Math.sin(curAngle + Math.PI)  );

			obstaclePoint.setLocation(	rearCenterSensorPoint.getX() + rearCenterRange*Math.cos(curAngle + REAR_CENTER_IR_ANGLE),
										rearCenterSensorPoint.getY() + rearCenterRange*Math.sin(curAngle + REAR_CENTER_IR_ANGLE)  );

			obstaclePos[0] = obstaclePoint.getX();
			obstaclePos[1] = obstaclePoint.getY();
			sensorPos[0] = rearCenterSensorPoint.getX();
			sensorPos[1] = rearCenterSensorPoint.getY();
						
			obstacleCoord = WorldView.locToCoord(obstaclePos);
			sensorCoord = WorldView.locToCoord(sensorPos);
			
//			System.out.println("RC Coord: (" + sensorCoord[0] + "," + sensorCoord[1] + ")");
			
			locationsToIncrease.add(new OrderedPairConfidence(obstacleCoord[0], obstacleCoord[1], WorldView.rangeBasedConfidenceIncrease(rearCenterRange)));	
			
			// add all the coordinates that are covered by "lineToClear" to the "locationsToDecrease" list
			WorldView.decreaseLineConfidence(locationsToDecrease, sensorCoord[0], obstacleCoord[0], sensorCoord[1], obstacleCoord[1]);
			WorldView.setFrontierCell(new OrderedPair(obstacleCoord[0], obstacleCoord[1]), false);

		}
		
		if ((rearRightRange >= MIN_USEFUL_IR_DISTANCE) && (rearRightRange <= MAX_USEFUL_IR_DISTANCE)) {
			rearRightSensorPoint.setLocation(	curLoc[0] + REAR_RIGHT_IR_POSE_HYP*Math.cos(curAngle + Math.atan(REAR_RIGHT_IR_POSE[1]/REAR_RIGHT_IR_POSE[0]) + Math.PI),
												curLoc[1] + REAR_RIGHT_IR_POSE_HYP*Math.sin(curAngle + Math.atan(REAR_RIGHT_IR_POSE[1]/REAR_RIGHT_IR_POSE[0]) + Math.PI)  );
			
			obstaclePoint.setLocation(	rearRightSensorPoint.getX() + rearRightRange*Math.cos(curAngle + REAR_RIGHT_IR_ANGLE),
										rearRightSensorPoint.getY() + rearRightRange*Math.sin(curAngle + REAR_RIGHT_IR_ANGLE)  );
			
			obstaclePos[0] = obstaclePoint.getX();
			obstaclePos[1] = obstaclePoint.getY();
			sensorPos[0] = rearRightSensorPoint.getX();
			sensorPos[1] = rearRightSensorPoint.getY();
						
			obstacleCoord = WorldView.locToCoord(obstaclePos);
			sensorCoord = WorldView.locToCoord(sensorPos);
			
//			System.out.println("RR Coord: (" + sensorCoord[0] + "," + sensorCoord[1] + ")");
			
			locationsToIncrease.add(new OrderedPairConfidence(obstacleCoord[0], obstacleCoord[1], WorldView.rangeBasedConfidenceIncrease(rearRightRange)));	
			
			// add all the coordinates that are covered by "lineToClear" to the "locationsToDecrease" list
			WorldView.decreaseLineConfidence(locationsToDecrease, sensorCoord[0], obstacleCoord[0], sensorCoord[1], obstacleCoord[1]);
			WorldView.setFrontierCell(new OrderedPair(obstacleCoord[0], obstacleCoord[1]), false);

		}
					
	
		// iterate through the "locationsToIncrease" list
			// increase confidence value at (xCoord,yCoord)
		ListIterator<OrderedPairConfidence> iter = locationsToIncrease.listIterator();
		while (iter.hasNext()) {
			// increase the confidence based on distance to obstacle (closer means more confident)
			OrderedPairConfidence cur = iter.next();
			double previousConfidence = WorldView.readConfidence(cur.getX(), cur.getY());
			if (previousConfidence != -1) { // if no error reading confidence
				WorldView.writeConfidence(cur.getX(), cur.getY(), previousConfidence + cur.getDeltaConfidence()*PathPlanner.getCertaintyFactor());  
			}
		}
		
		
		// iterate through the "locationsToDecrease" list
			// decrease confidence values at (xCoord,yCoord)
		iter = locationsToDecrease.listIterator();
		while (iter.hasNext()) {
			OrderedPairConfidence cur = iter.next();
			double previousConfidence = WorldView.readConfidence(cur.getX(), cur.getY());
			if (previousConfidence != -1) {
				WorldView.writeConfidence(cur.getX(), cur.getY(), previousConfidence - cur.getDeltaConfidence()*PathPlanner.getCertaintyFactor());  
			}
		}	
				
	}
	
	/**
	 * Sets the cell to be either frontier or not
	 * @param coord	Ordered Pair coordinate of worldview
	 * @param truth Whether cell is a frontier cell
	 */
	private static void setFrontierCell(OrderedPair coord, boolean truth){
		((world.get(coord.getX())).get(coord.getY())).setFrontier(truth);
		//PathPlanner.addFrontierCell(coord);
	}
	
	private static void setPathCell(OrderedPair coord){
		((world.get(coord.getX())).get(coord.getY())).setPath();
	}
	
	/**
	 * adds OrderedPairs on the line from sensor coordinate to obstacle coordinate to the list
	 * that decreases the confidence of these locations 
	 * @author Aaron Chen
	 * @param locationsToDecrease Arraylist of OrderedPairConfidence
	 * @param x1 Sensor X Coordinate
	 * @param x2 Obstacle X Coordinate
	 * @param y1 Sensor Y Coordinate
	 * @param y2 Obstacle Y Coordinate
	 */
	private static void decreaseLineConfidence(ArrayList<OrderedPairConfidence> locationsToDecrease, Integer x1, Integer x2, Integer y1, Integer y2) {
		Integer xi,yi;
		Integer deltax = x2-x1;
		Integer deltay = y2-y1;
		Integer minx = Math.min(x1,x2);
		Integer maxx = Math.max(x1,x2);
		Integer miny = Math.min(y1,y2);
		Integer maxy = Math.max(y1,y2);
		Double slope;
		
		if (Math.abs(deltax) >= Math.abs(deltay)){ //if |slope| < 1, increment with x
			slope = (double) deltay/deltax;
			for(xi = minx + 1; xi < maxx; xi++){
				if(slope >= 0) //if slope positive, start with miny
					yi = miny + (int) Math.round((xi - minx) * slope);
				else //if slope negative, start with maxy
					yi = maxy + (int) Math.round((xi - minx) * slope);
				locationsToDecrease.add(new OrderedPairConfidence ( xi, yi,
						WorldView.rangeBasedConfidenceDecrease(Math.sqrt( (x2-x1)*(x2-x1) + (y2-y1)*(y2-y1) )))); 
				WorldView.setFrontierCell(new OrderedPair(xi, yi), false);
				//System.out.print("(" + xi + "," + yi + ")");
			}
		}
		else{									//if |slope| > 1, increment with y
			slope = (double) deltax/deltay;
			for(yi = miny + 1; yi < maxy; yi++){
				if(slope >= 0) //if slope positive, start with minx	
					xi = minx + (int) Math.round((yi - miny) * slope);
				else //if slope negative, start with maxx
					xi = maxx + (int) Math.round((yi - miny) * slope);
				locationsToDecrease.add(new OrderedPairConfidence ( xi, yi,
						WorldView.rangeBasedConfidenceDecrease(Math.sqrt( (x2-x1)*(x2-x1) + (y2-y1)*(y2-y1) )))); 
				WorldView.setFrontierCell(new OrderedPair(xi, yi), false);
				//System.out.print("(" + xi + "," + yi + ")");		
			}
		}
	}
	
	
	public static synchronized void writeConfidence(Integer x, Integer y, double c) {
//	synchronized (world) {
		// set (x,y)'s confidence = c
//		System.out.println(x + ", " + y + ", " + c);
		
		// ensure that c is between 0 and 1
		if (c < 0) { 
			c = 0; // min value
		} else if (c > 1) {
			c = 1; // max value
		} else {
			if ((x >= 0 && x < world.size()) && (y >= 0 && y < (world.get(x)).size())) { 
				((world.get(x)).get(y)).setConfidence(c);
			}
		}
//	}
	}
	
	
	public static synchronized void increaseTraversed(Integer x, Integer y) {
//		synchronized (world) {
			if ((x >= 0 && x < world.size()) && (y >= 0 && y < (world.get(x)).size())) {
				((world.get(x)).get(y)).incTraversed();
			}
//		}
	}
	
	/**
	 * 
	 * @param x X Coordinate
	 * @param y Y Coordinate
	 * @return double confidenceValue at (x,y). Returns -1 if out of bounds. 
	 */
	public static synchronized double readConfidence(Integer x, Integer y) {
//	synchronized (world) {
		// return confidence at (x,y);
		if ((x >= 0 && x < world.size()) && (y >= 0 && y < (world.get(x)).size())) {
			return ((world.get(x)).get(y)).getConfidence();
		} else return -1; // error, out of bounds
//	}
	}
	
	
	/**
	 * 
	 * @param range the distance from the sensor to the obstacle coordinate
	 * @return deltaConfidence value, which is the amount that the 
	 * confidence value will be adjusted, dependent on range. 
	 * Will return 0 if outside the acceptable range
	 */
	public static double rangeBasedConfidenceIncrease (float range) {
		if ((range >= 0.22) && (range < 0.4)) {
			return 0.15;
		} else if ((range >= 0.4) && (range < 0.6)) {
			return 0.13;
		} else if ((range >= 0.6) && (range < 0.8)) {
			return 0.12;
		} else if ((range >= 0.8) && (range < 1.05)) {
			return 0; // throw out this unstable window where the IR switching board is deciding b/w long and short range IR
		} else if ((range >= 1.05) && (range < 1.2)) {
			return 0.06;
		} else if ((range >= 1.2) && (range < 1.45)) {
			return 0.04;
		} else if ((range >= 1.45) && (range < 1.75)) {
			return 0.02;
		} else
			return 0;
			
	}
	
	
	/**
	 * 
	 * @param range the distance from the sensor to the coordinate (while iterating through locations to decrease).
	 * "range" value is in units of coordinates
	 * @return deltaConfidence value, which is the amount that the 
	 * confidence value will be adjusted, dependent on range. 
	 * Will return 0 if outside the acceptable range
	 */
	public static double rangeBasedConfidenceDecrease (double range) {
		if ((range >= 0) && (range < 5)) {
			return 0.15;
		} else if ((range >= 5) && (range < 10)) {
			return 0.10;
		} else if ((range >= 10) && (range < 15)) {
			return 0.08;
		} else if ((range >= 15) && (range < 20)) {
			return 0.06;
		} else if ((range >= 20) && (range < 25)) {
			return 0.04;
		} else if ((range >= 25) && (range < 30)) {
			return 0.01;
		} else if ((range >= 30) && (range < 35)) {
			return 0.01;
		} else
			return 0;
		
	}
	
	/**
	 * converts actual x and y positions to x and y coordinates for indexing the WorldView 2-D matrix
	 * @param loc array of doubles = {x, y, angle}  (angle is not used)
	 * @return array of Integers = {xCoord, yCoord}
	 */
	public static Integer [] locToCoord(double [] loc) {
		
		Integer x = (int) Math.round(loc[0]/RESOLUTION);
		Integer y = (int) Math.round(loc[1]/RESOLUTION);
		
		if (x < 0) x = 0;
		if (y < 0) y = 0;
		
		Integer [] coord = {x,y};
		return coord;
	}
	
	
	
	public void newPlayerIRData(PlayerIrData data) {

		double [] curLoc = LocationTracker.getCurrentLocation();
		
		if (++(numIRreadings) == 1) {
			dq = new ArrayList<ArrayDeque<Float>>();
			
			for (int i = 0; i < 6; i++) {
				dq.add(new ArrayDeque<Float>(5));
			}
		}
		
		float [] window = new float [6];
		float [] ranges = data.getRanges();
		
		//System.out.println("FL=" + ranges[0] + ", FC=" + ranges[1] + ", FR=" + ranges[2] + ", RL=" + ranges[5] + ", RC=" + ranges[4] + ", RR=" + ranges[3]);
		
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

		WorldView.recordObstacles(window, curLoc);
		
		// for Path Planner
		PathPlanner.setLeftHandDistance(window[0]);
		PathPlanner.setFaceDistance(window[1]);
		PathPlanner.setRightHandDistance(window[2]);
		//PathPlanner.FaceDistance = window[1];
		//PathPlanner.RightHandDistance = window[2];
//		System.out.println("FL=" + window[0] + ", FC=" + window[1] + ", FR=" + window[2] + ", RL=" + window[5] + ", RC=" + window[4] + ", RR=" + window[3]);
	}



	public static float findMedian (ArrayList<Float> arr) {
		Collections.sort(arr);
		return (float) arr.get(2); // always get 3rd element (in a 5-wide window)
	}

	
	
	
	
	
	private static float calibrateIR(float data) {
		//return (float) (-0.00003*data*data + 1.0509*data - 21.926);
		return data;
	}

	
	
	
	public static void printWorldView() throws IOException {
		int x = 0;
		
		WorldView.fout.write("\n\n");
		
		// print column headers (x coordinates)		
		WorldView.fout.write(String.format("%6s", " "));
		
		for (x = 0; x < world.size(); x++) {
			WorldView.fout.write(String.format("%6d", x));

		}
		
		x = 0;
		// print confidence values in a grid
//		System.out.println(world.size() + ", " + world.get(0).size());
		for (int y = ((world.get(x)).size() - 1); y >= 0; y--) {
			
			WorldView.fout.write("\n" + String.format("%6d", y));
			for (x = 0; x < world.size(); x++) {
				double conf = WorldView.readConfidence(x, y);
				
				if (conf == 0.50) {
					WorldView.fout.write(String.format("%6s", " "));
				} else {
					if (conf != -1) {
						WorldView.fout.write(String.format("%6.2f", (float) WorldView.readConfidence(x, y)));
					}
				}
				
			}
			
		}
		
		try {
			@SuppressWarnings("unused")
			BitmapOut bitmap = new BitmapOut(WorldView.WORLD_SIZE,WorldView.WORLD_SIZE);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}
	
	/**
	 * Returns the distance between the current location and the path
	 * @return
	 */
	public static double frontDistance(){
		double theta = LocationTracker.getCurrentBearing();
		double [] loc = LocationTracker.getCurrentLocation();
		//loc[0] += ROOMBA_RADIUS;//add offset of the roomba to get to edge
		//loc[1] += ROOMBA_RADIUS;//add offset of the roomba to get to edge
		Integer [] coord = WorldView.locToCoord(loc);
		int x = coord[0];
		int y = coord[1];
		
		int xOffset = (int)((double) PathPlanner.FACE_DISTANCE_FROM_WALL*RESOLUTION * Math.cos(theta));
		int yOffset = (int)((double) PathPlanner.FACE_DISTANCE_FROM_WALL*RESOLUTION * Math.sin(theta));
		
		int newX = x + xOffset;
		int newY = y + yOffset;
		
		int deltaY = newY - y;
		int deltaX = newX - x;
		double slope;
		int xi, yi;
		int minx = Math.min(x, newX);
		int miny = Math.min(y, newY);
		int maxx = Math.max(x, newX);
		int maxy = Math.max(y, newY);
		
		
		if (Math.abs(deltaX) >= Math.abs(deltaY)){ //if |slope| < 1, increment with x
			slope = (double) deltaY/deltaX;
			for(xi = minx + 1; xi < maxx; xi++){
				if(slope >= 0) //if slope positive, start with miny
					yi = miny + (int) Math.round((xi - minx) * slope);
				else //if slope negative, start with maxy
					yi = maxy + (int) Math.round((xi - minx) * slope);
				
				if(WorldView.world.get(xi).get(yi).getPath()){
					//return distance
					//System.out.println("Current Loc @: (" + x + "," + y + ")");
					//System.out.println("Path found @: (" + xi + "," + yi + ")");
					return Math.sqrt(Math.pow(xi-x,2) + Math.pow(yi-y,2)) * 50;
				}
				
			}
		}
		else{									//if |slope| > 1, increment with y
			slope = (double) deltaX/deltaY;
			for(yi = miny + 1; yi < maxy; yi++){
				if(slope >= 0) //if slope positive, start with minx	
					xi = minx + (int) Math.round((yi - miny) * slope);
				else //if slope negative, start with maxx
					xi = maxx + (int) Math.round((yi - miny) * slope);
				
				if(WorldView.world.get(xi).get(yi).getPath()){
					//return distance
					//System.out.println("Current Loc @: (" + x + "," + y + ")");
					//System.out.println("Path found @: (" + xi + "," + yi + ")");
					return Math.sqrt(Math.pow(xi-x,2) + Math.pow(yi-y,2)) * 50;
				}
				
			}
		}		
		return Integer.MAX_VALUE;
	}
	
	/**
	 * Returns the distance from left to "wall"
	 * @return
	 */
	public static double leftDistance(){
		double theta = LocationTracker.getCurrentBearing();
		double thetaOffset = Math.PI/6; //30 degrees
		double degree = theta + thetaOffset;
		double [] loc = LocationTracker.getCurrentLocation();
		//loc[0] += ROOMBA_RADIUS;//add offset of the roomba to get to edge
		//loc[1] += ROOMBA_RADIUS;//add offset of the roomba to get to edge
		Integer [] coord = WorldView.locToCoord(loc);
		int x = coord[0];
		int y = coord[1];
		
		if (degree > Math.PI){
			degree -= 2*Math.PI;
		}
		
		int xOffset = (int)((double) PathPlanner.DISTANCE_FROM_WALL*RESOLUTION * Math.cos(degree));
		int yOffset = (int)((double) PathPlanner.DISTANCE_FROM_WALL*RESOLUTION * Math.sin(degree));
		
		int newX = x + xOffset;
		int newY = y + yOffset;
		
		int deltaY = newY - y;
		int deltaX = newX - x;
		double slope;
		int xi, yi;
		int minx = Math.min(x, newX);
		int miny = Math.min(y, newY);
		int maxx = Math.max(x, newX);
		int maxy = Math.max(y, newY);
		
		
		if (Math.abs(deltaX) >= Math.abs(deltaY)){ //if |slope| < 1, increment with x
			slope = (double) deltaY/deltaX;
			for(xi = minx + 1; xi < maxx; xi++){
				if(slope >= 0) //if slope positive, start with miny
					yi = miny + (int) Math.round((xi - minx) * slope);
				else //if slope negative, start with maxy
					yi = maxy + (int) Math.round((xi - minx) * slope);
				//System.out.println(xi + ", " + yi);
				if(WorldView.world.get(xi).get(yi).getPath()){
					//return distance
					//System.out.println("Current Loc @: (" + x + "," + y + ")");
					//System.out.println("Path found @: (" + xi + "," + yi + ")");
					return Math.sqrt(Math.pow(xi-x,2) + Math.pow(yi-y,2)) * 50;
				}
				
			}
		}
		else{									//if |slope| > 1, increment with y
			slope = (double) deltaX/deltaY;
			for(yi = miny + 1; yi < maxy; yi++){
				if(slope >= 0) //if slope positive, start with minx	
					xi = minx + (int) Math.round((yi - miny) * slope);
				else //if slope negative, start with maxx
					xi = maxx + (int) Math.round((yi - miny) * slope);
				
				if(WorldView.world.get(xi).get(yi).getPath()){
					//return distance
					//System.out.println("Current Loc @: (" + x + "," + y + ")");
					//System.out.println("Path found @: (" + xi + "," + yi + ")");
					return Math.sqrt(Math.pow(xi-x,2) + Math.pow(yi-y,2)) * 50;
				}
				
			}
		}
		
		
		return Integer.MAX_VALUE;
	}
}




