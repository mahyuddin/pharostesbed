package pharoslabut.cartographer;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;


class LocationElement {
	private Integer xCoord;
	private Integer yCoord;
	private double confidence; // percentage 
	private Integer traversedCount;
	
	public LocationElement (Integer x, Integer y) {
		this.xCoord = x;
		this.yCoord = y;
		this.confidence = .5;  // initialized to 50% 
		this.traversedCount = 0;    // initialized to 0, unexplored		
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

}


public class WorldView {
	
	private static ArrayList<ArrayList<LocationElement>> world; // full 2-D matrix, world view
	
	public static final double RESOLUTION 				= 0.05; // 5 cm
	public static final double MIN_USEFUL_IR_DISTANCE 	= 0.20; // minimum short range distance is 20 cm 
	public static final double MAX_USEFUL_IR_DISTANCE 	= 3.00; // max short range distance is 150 cm
																// looks like the max long range distance detectable is ~550 cm 
																// but more accurate when under 300 cm
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
		world = new ArrayList<ArrayList<LocationElement>>(100);
//		world = Collections.synchronizedList(new ArrayList<ArrayList<LocationElement>>(100));
	
		for (Integer i = 0; i < 100; i++) { // iterate through each x coordinate
 
			//add a new list for all the y coordinates at that x coordinate
			world.add(new ArrayList<LocationElement>(100)); 

			
			for (Integer j = 0; j < 100; j++) { // iterate through each y coordinate
				(world.get(i)).add(new LocationElement(i,j)); // add a LocationElement at that coordinate
			}	
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
				writeConfidence(x, y, 0); 
//				synchronized (world) {
//					((world.get(x)).get(y)).incTraversed();
//				}
				
			}
		}
		
//		System.out.println("Current Location (m): " + LocationTracker.printCurrentLocation());
//		System.out.println("Current Coordinates: " + LocationTracker.printCurrentCoordinates());
		
	}
	
	
	/**
	 * @author Kevin
	 * @param dist: an array of the 6 IR distance values (float types). order: FL, FC, FR, RL, RC, RR
	 */
	public static synchronized void recordObstacles(float [] distIR) {
		// extract IR data from dist[], dist data is in mm, convert back to m
		float frontLeftRange 	= distIR[0] / 1000;
		float frontCenterRange 	= distIR[1] / 1000;
		float frontRightRange 	= distIR[2] / 1000;
		float rearLeftRange 	= distIR[3] / 1000;
		float rearCenterRange 	= distIR[4] / 1000;
		float rearRightRange 	= distIR[5] / 1000;
		
		double [] curLoc = LocationTracker.getCurrentLocation();
		double xPos 	= curLoc[0]; 
		double yPos 	= curLoc[1];
		double curAngle	= curLoc[2];
		
		Point2D.Double curPoint = new Point2D.Double(xPos, yPos);
		Point2D.Double obstaclePoint 			= new Point2D.Double();
		Point2D.Double frontLeftSensorPoint		= new Point2D.Double();
		Point2D.Double frontCenterSensorPoint 	= new Point2D.Double();
		Point2D.Double frontRightSensorPoint 	= new Point2D.Double();
		Point2D.Double rearLeftSensorPoint 		= new Point2D.Double();
		Point2D.Double rearCenterSensorPoint 	= new Point2D.Double();
		Point2D.Double rearRightSensorPoint		= new Point2D.Double();
			
		// 2-D ArrayList of spaces to clear 
		// (these need to be calculated and added to the list before the "synchronized (world)" block below)
		ArrayList<OrderedPair> locationsToDecrease = new ArrayList<OrderedPair>();
		ArrayList<OrderedPair> locationsToIncrease = new ArrayList<OrderedPair>();
		
		locationsToDecrease.clear();
		locationsToIncrease.clear();
		
		Line2D.Double lineToClear = new Line2D.Double(); 
		
		Integer [] curCoords = locToCoord(curLoc); // this might be useful later
		Integer [] obstacleCoord;
		double [] obstaclePos = {0,0}; // 

		
		
		// WE NEED TO TRY USING THE Line2D class with the Point2D class for keeping track of coordinates... much MUCH better
		// but both of those are abstract classes... we would need the "double type" implementation of each class
		
		
		// calculate where the object should be recorded
		
		if ((frontLeftRange >= MIN_USEFUL_IR_DISTANCE) && (frontLeftRange <= MAX_USEFUL_IR_DISTANCE)) {
			frontLeftSensorPoint.setLocation(	curLoc[0] + FRONT_LEFT_IR_POSE_HYP*Math.cos(curAngle + Math.atan(FRONT_LEFT_IR_POSE[1]/FRONT_LEFT_IR_POSE[0])),
												curLoc[1] + FRONT_LEFT_IR_POSE_HYP*Math.sin(curAngle + Math.atan(FRONT_LEFT_IR_POSE[1]/FRONT_LEFT_IR_POSE[0]))  );
		
			obstaclePoint.setLocation(	frontLeftSensorPoint.getX() + frontLeftRange*Math.cos(curAngle + FRONT_LEFT_IR_ANGLE),
										frontLeftSensorPoint.getY() + frontLeftRange*Math.sin(curAngle + FRONT_LEFT_IR_ANGLE)  );
			
			lineToClear.setLine(frontCenterSensorPoint, obstaclePoint);
			
			obstacleCoord = WorldView.locToCoord(obstaclePos);
			
			locationsToIncrease.add(new OrderedPair(obstacleCoord[0], obstacleCoord[1]));	
			
			// add all the coordinates that are covered by "lineToClear" to the "locationsToDecrease" list
		
		}
		
		if ((frontCenterRange >= MIN_USEFUL_IR_DISTANCE) && (frontCenterRange <= MAX_USEFUL_IR_DISTANCE)) {
			// for the front center IR, the curAngle is equal to the sensor's angle from the center
			frontCenterSensorPoint.setLocation(	curLoc[0] + FRONT_CENTER_IR_POSE_HYP*Math.cos(curAngle),
												curLoc[1] + FRONT_CENTER_IR_POSE_HYP*Math.sin(curAngle)  );

			obstaclePoint.setLocation(	frontCenterSensorPoint.getX() + frontCenterRange*Math.cos(curAngle + FRONT_CENTER_IR_ANGLE),
										frontCenterSensorPoint.getY() + frontCenterRange*Math.sin(curAngle + FRONT_CENTER_IR_ANGLE)  );

			lineToClear.setLine(frontCenterSensorPoint, obstaclePoint);

			obstacleCoord = WorldView.locToCoord(obstaclePos);

			locationsToIncrease.add(new OrderedPair(obstacleCoord[0], obstacleCoord[1]));
			
			// add all the coordinates that are covered by "lineToClear" to the "locationsToDecrease" list
			
		}

		if ((frontRightRange >= MIN_USEFUL_IR_DISTANCE) && (frontRightRange <= MAX_USEFUL_IR_DISTANCE)) {
			frontRightSensorPoint.setLocation(	curLoc[0] + FRONT_RIGHT_IR_POSE_HYP*Math.cos(curAngle + Math.atan(FRONT_RIGHT_IR_POSE[1]/FRONT_RIGHT_IR_POSE[0])),
												curLoc[1] + FRONT_RIGHT_IR_POSE_HYP*Math.sin(curAngle + Math.atan(FRONT_RIGHT_IR_POSE[1]/FRONT_RIGHT_IR_POSE[0]))  );

			obstaclePoint.setLocation(	frontRightSensorPoint.getX() + frontRightRange*Math.cos(curAngle + FRONT_RIGHT_IR_ANGLE),
										frontRightSensorPoint.getY() + frontRightRange*Math.sin(curAngle + FRONT_RIGHT_IR_ANGLE)  );
			
			lineToClear.setLine(frontCenterSensorPoint, obstaclePoint);
			
			obstacleCoord = WorldView.locToCoord(obstaclePos);

			locationsToIncrease.add(new OrderedPair(obstacleCoord[0], obstacleCoord[1]));	
			
			// add all the coordinates that are covered by "lineToClear" to the "locationsToDecrease" list
		}
		
		if ((rearLeftRange >= MIN_USEFUL_IR_DISTANCE) && (rearLeftRange <= MAX_USEFUL_IR_DISTANCE)) {
			rearLeftSensorPoint.setLocation(	curLoc[0] + REAR_LEFT_IR_POSE_HYP*Math.cos(curAngle + Math.atan(REAR_LEFT_IR_POSE[1]/REAR_LEFT_IR_POSE[0])),
												curLoc[1] + REAR_LEFT_IR_POSE_HYP*Math.sin(curAngle + Math.atan(REAR_LEFT_IR_POSE[1]/REAR_LEFT_IR_POSE[0]))  );
			
			obstaclePoint.setLocation(	rearLeftSensorPoint.getX() + rearLeftRange*Math.cos(curAngle + REAR_LEFT_IR_ANGLE),
										rearLeftSensorPoint.getY() + rearLeftRange*Math.sin(curAngle + REAR_LEFT_IR_ANGLE)  );
			
			lineToClear.setLine(frontCenterSensorPoint, obstaclePoint);
			
			obstacleCoord = WorldView.locToCoord(obstaclePos);
			
			locationsToIncrease.add(new OrderedPair(obstacleCoord[0], obstacleCoord[1]));	
			
			// add all the coordinates that are covered by "lineToClear" to the "locationsToDecrease" list
		}
		
		if ((rearCenterRange >= MIN_USEFUL_IR_DISTANCE) && (rearCenterRange <= MAX_USEFUL_IR_DISTANCE)) {
			// for the rear center IR, the curAngle + PI is equal to the sensor's angle from the center
			rearCenterSensorPoint.setLocation(	curLoc[0] + REAR_CENTER_IR_POSE_HYP*Math.cos(curAngle + Math.PI),
												curLoc[1] + REAR_CENTER_IR_POSE_HYP*Math.sin(curAngle + Math.PI)  );

			obstaclePoint.setLocation(	rearCenterSensorPoint.getX() + rearCenterRange*Math.cos(curAngle + REAR_CENTER_IR_ANGLE),
										rearCenterSensorPoint.getY() + rearCenterRange*Math.sin(curAngle + REAR_CENTER_IR_ANGLE)  );

			lineToClear.setLine(rearCenterSensorPoint, obstaclePoint);

			obstacleCoord = WorldView.locToCoord(obstaclePos);

			locationsToIncrease.add(new OrderedPair(obstacleCoord[0], obstacleCoord[1]));
			
			// add all the coordinates that are covered by "lineToClear" to the "locationsToDecrease" list			
		}
		
		if ((rearRightRange >= MIN_USEFUL_IR_DISTANCE) && (rearRightRange <= MAX_USEFUL_IR_DISTANCE)) {
			rearRightSensorPoint.setLocation(	curLoc[0] + REAR_RIGHT_IR_POSE_HYP*Math.cos(curAngle + Math.atan(REAR_RIGHT_IR_POSE[1]/REAR_RIGHT_IR_POSE[0])),
												curLoc[1] + REAR_RIGHT_IR_POSE_HYP*Math.sin(curAngle + Math.atan(REAR_RIGHT_IR_POSE[1]/REAR_RIGHT_IR_POSE[0]))  );
			
			obstaclePoint.setLocation(	rearRightSensorPoint.getX() + rearRightRange*Math.cos(curAngle + REAR_RIGHT_IR_ANGLE),
										rearRightSensorPoint.getY() + rearRightRange*Math.sin(curAngle + REAR_RIGHT_IR_ANGLE)  );
			
			lineToClear.setLine(frontCenterSensorPoint, obstaclePoint);
			
			obstacleCoord = WorldView.locToCoord(obstaclePos);
			
			locationsToIncrease.add(new OrderedPair(obstacleCoord[0], obstacleCoord[1]));	
			
			// add all the coordinates that are covered by "lineToClear" to the "locationsToDecrease" list	
		}
					
			
			
		
		// iterate through the "locationsToIncrease" list
			// increase confidence value at (xCoord,yCoord)
		ListIterator<OrderedPair> iter = locationsToIncrease.listIterator();
		while (iter.hasNext()) {
			// how much should we increase the confidence? +2, +3, etc?
			OrderedPair cur = iter.next();
			double previousConfidence = WorldView.readConfidence(cur.getX(), cur.getY()); 
			WorldView.writeConfidence( cur.getX(), cur.getY(), previousConfidence + 2); // might want to change this addition 
		}
		
		
		// iterate through the "locationsToDecrease" list
			// decrease confidence values at (xCoord,yCoord)
			
			
			
		
		
	}
	
	
	
	public static synchronized void writeConfidence(Integer x, Integer y, double c) {
//	synchronized (world) {
		// set (x,y)'s confidence = c
		System.out.println(x + ", " + y + ", " + c);
		if ((c >= 0 && c <= 1) && ((x >= 0 && x < world.size()) && (y >= 0 && y < (world.get(x)).size()))) {
			((world.get(x)).get(y)).setConfidence(c);
		}
//	}
	}
	
	
	public static synchronized double readConfidence(Integer x, Integer y) {
//	synchronized (world) {
		// return confidence at (x,y);
		return ((world.get(x)).get(y)).getConfidence();
//	}
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
	
	
	
	private static double calibrateIR(double data) {
		return data;
	}

	
	public static void printWorldVew() {
		System.out.println(""); // skip a line
		
		int x = 0;
		
		// print confidence values in a grid		
		for (x = 0; x < world.size(); x++) {
			System.out.print(x + "\t");
		}
		
		for (int y = (world.get(x).size() - 1); y >= 0; y--) {
			for (x = 0; x < world.size(); x++) {
				System.out.print(y + "\t" + ((world.get(x)).get(y)).getConfidence() + "\t");			
			}
			System.out.println("");
		}
	}
}




