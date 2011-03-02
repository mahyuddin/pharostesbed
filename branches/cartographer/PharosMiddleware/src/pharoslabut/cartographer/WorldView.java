package pharoslabut.cartographer;

import java.util.*;

class LocationElement {
	private Integer xCoord;
	private Integer yCoord;
	private double confidence; // percentage 
	private double elevation;
	
	public LocationElement (Integer x, Integer y) {
		this.xCoord = x;
		this.yCoord = y;
		this.confidence = .5;  // initialized to 50% 
		this.elevation = 0;    // initialized to 0, floor level		
	}	
	
	/**************** GETTERS AND SETTERS ******************/
	public Integer getxCoord() { return xCoord; }
	public void setxCoord(Integer x) { this.xCoord = x;	}
	public Integer getyCoord() { return yCoord;	}
	public void setyCoord(Integer y) { this.yCoord = y;	}
	public double getConfidence() {	return confidence; }
	public void setConfidence(double c) { this.confidence = c; }
	public double getElevation() { return elevation; }
	public void setElevation(double e) { this.elevation = e; }
	/*************** END GETTERS AND SETTERS ****************/
	
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
	private static List<ArrayList<LocationElement>> world; // full 2-D matrix, world view
	
	public static final double RESOLUTION 				= 0.05; // 5 cm
	public static final double MIN_USEFUL_IR_DISTANCE 	= 0.08; // 8 cm (is this correct??) 
	public static final double MAX_USEFUL_IR_DISTANCE 	= 4.00; // 4 m  (is this correct??)
	public static final double ROOMBA_RADIUS 			= 0.17; // radius of the roomba from center point out = 17cm
	
	
	public WorldView() {
		world = Collections.synchronizedList(new ArrayList<ArrayList<LocationElement>>());
		for (Integer i = 0; i < 100; i++) { // iterate through each x coordinate
 
			//add a new list for all the y coordinates at that x coordinate
			world.add(new ArrayList<LocationElement>());  
			
			for (Integer j = 0; j < 100; j++) { // iterate through each y coordinate
				world.get(i).add(j, new LocationElement(i,j)); // add a LocationElement at that coordinate
			}	
		}
	}
	
	
	/**
	 * This is called by LocationTracker whenever updateLocation is called. <br> <
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
		
		// convert Roomba boundaries to coordinates
		Integer [] upperLeftCoord = locToCoord(upperLeftPos);
		Integer [] lowerRightCoord = locToCoord(lowerRightPos);
		
		// add all coordinates from upperLeftCoord to lowerRightCoord to the locationsToClear list
		for (int x = upperLeftCoord[0]; x <= lowerRightCoord[0]; x++) { // iterate through all xCoords, from left to right 
			for (int y = upperLeftCoord[1]; y >= lowerRightCoord[1]; x--) { // iterate through all yCoords, from top to bottom
				
				// since we have passed through this location, we are full confident an object doesn't exist there
				//   so the confidence value should be set to zero to indicate no obstacle is present
				writeConfidence(x, y, 0);   		
			}
		}
		
	}
	
	
	/**
	 * @author Kevin
	 * @param dist: an array of the 6 IR distance values (float types). order: FL, FC, FR, RL, RC, RR
	 */
	public static synchronized void recordObstacles(float [] dist) {
		// extract IR data from dist[], dist data is in mm
		float frontLeft	 	= dist[0];
		float frontCenter 	= dist[1];
		float frontRight 	= dist[2];
		float rearLeft 		= dist[3];
		float rearCenter 	= dist[4];
		float rearRight 	= dist[5];
		
		double [] curLoc = LocationTracker.getCurrentLocation();
		double xPos = curLoc[0]; 
		double yPos = curLoc[1];
		double angle = curLoc[2];
		
		// 2-D ArrayList of spaces to clear 
		// (these need to be calculated and added to the list before the "synchronized (world)" block below)
		ArrayList<OrderedPair> locationsToDecrease = new ArrayList<OrderedPair>();
		ArrayList<OrderedPair> locationsToIncrease = new ArrayList<OrderedPair>();
		
		Integer [] curCoords = locToCoord(curLoc); // this might be useful later
		Integer [] obstacleCoord;
		double [] obstaclePos = curLoc; // start with the current location, add IR values to it below
		
		// calculate where the object should be recorded
		
		if ((frontLeft >= MIN_USEFUL_IR_DISTANCE) && (frontLeft <= MAX_USEFUL_IR_DISTANCE)) {
			
		}
		
		if ((frontCenter >= MIN_USEFUL_IR_DISTANCE) && (frontCenter <= MAX_USEFUL_IR_DISTANCE)) {
			
			// this is wrong... it needs to factor in the current angle
			
			obstaclePos[1] += frontCenter; // add IR value to yPos, cuz it's directly in front
			obstacleCoord = locToCoord(obstaclePos); // convert from actual position to coordinate
			locationsToIncrease.add(new OrderedPair(obstacleCoord[0], obstacleCoord[1]));
			
			// xCoord and obstacleCoord[0] should be the same (they should have the same x value)
			// keep xCoord the same, iterate through all the yCoords from current Y coord to obstacle's y coord
			for (int y = curCoords[1]; y < obstacleCoord[1]; y++) {
				locationsToDecrease.add(new OrderedPair(curCoords[0], y));
			}
			
			
		}

		if ((frontRight >= MIN_USEFUL_IR_DISTANCE) && (frontRight <= MAX_USEFUL_IR_DISTANCE)) {
	
		}
		
		if ((rearLeft >= MIN_USEFUL_IR_DISTANCE) && (rearLeft <= MAX_USEFUL_IR_DISTANCE)) {
			
		}
		
		if ((rearCenter >= MIN_USEFUL_IR_DISTANCE) && (rearCenter <= MAX_USEFUL_IR_DISTANCE)) {
			
		}
		
		if ((rearRight >= MIN_USEFUL_IR_DISTANCE) && (rearRight <= MAX_USEFUL_IR_DISTANCE)) {
			
		}
					
			
			
		
		
		
		
		
		
		synchronized (world) {
			// 1) increase confidence value at (xCoord,yCoord)
			
			// 2) decrease confidence values at coordinates in a straight line of sight 
			//    from the curLoc to the sensed position (cuz the IR is seeing through blank space,
			//    so there probably isn't something there in it's path
			
			
		}
		
		
	}
	
	
	
	public static synchronized void writeConfidence(Integer x, Integer y, double c) {
	synchronized (world) {
		// set (x,y)'s confidence = c
		world.get(x).get(y).setConfidence(c);
	}
	}
	
	
	public static synchronized double readConfidence(Integer x, Integer y) {
	synchronized (world) {
		// return confidence at (x,y);
		return world.get(x).get(y).getConfidence();
	}
	}
	
	
	/**
	 * converts actual x and y positions to x and y coordinates for indexing the WorldView 2-D matrix
	 * @param loc array of doubles = {x, y, angle}  (angle is not used)
	 * @return array of Integers = {xCoord, yCoord}
	 */
	private static Integer [] locToCoord(double [] loc) {
		double xValue = loc[0];
		double yValue = loc[1];
		//double angle = loc[2]; // this is probably not needed
		
		Long x = Math.round(xValue/RESOLUTION);
		Long y = Math.round(yValue/RESOLUTION);
		
		Integer [] coord = {x.intValue(), y.intValue()};
		return coord;
	}
	
	
	
	private static double calibrateIR(double data) {
		return data;
	}

}




