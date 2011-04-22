package pharoslabut.cartographer;

import playerclient.Position2DInterface;
import playerclient.Position2DListener;
import playerclient.structures.PlayerConstants;
import playerclient.structures.PlayerPose;
import playerclient.structures.position2d.PlayerPosition2dData;
import pharoslabut.logger.CompassLoggerEvent;
import pharoslabut.logger.CompassLoggerEventListener;

public class LocationTracker implements CompassLoggerEventListener, Position2DListener{
	public static Position2DInterface motors;
	private static CompassLoggerEvent compassLogger;
	// change initial values to reflect the robot's starting orientation
	//private static final double initialX = WorldView.WORLD_SIZE/2*WorldView.RESOLUTION;
	//private static final double initialY = WorldView.WORLD_SIZE/2*WorldView.RESOLUTION;
	private static final double initialX = 60*WorldView.RESOLUTION;
	private static final double initialY = 60*WorldView.RESOLUTION;
	private static final double initialBearing = Math.PI/2;
	private static boolean checkLine = false;
	
	private static double currentX;
	private static double currentY;
	
	private static double bearing; // -PI to +PI
	
	private static boolean calibrateTurn;
	// 0 is east, PI/2 is north, PI and -PI are west, and -PI/2 is south
	
	
	public LocationTracker() {
		
		/////////// ROOMBA/ODOMETRY INTERFACE ////////////
		motors = (PathPlanner.client).requestInterfacePosition2D(0, 
				PlayerConstants.PLAYER_OPEN_MODE);
		
		
		// below: the serverIP and 7777 port number do not matter
		// inside the CompassLoggerEvent constructor, it no longer creates a new player client connection, 
		//   instead, it just uses the one client connection from the Path Planner and connects to the same port, 
		//   but using device index "2" for the compass's position2d provider
//		compassLogger = new CompassLoggerEvent(PathPlannerSimpleTest.serverIP, 7777, 2, false);
																	// 2 is device index, true means showGUI
//		compassLogger.addListener(this);
		
		// this just logs the data to a file
//		compassLogger.start(1, "compasslog.txt"); // first param is ignored
			
		
		currentX = getInitialx(); currentY = initialY; bearing = initialBearing; // facing east
		
		// robot should begin in lower-left corner with a bearing of 0, facing east
//		System.out.println("going to write odom");
//		PlayerPose newPose = new PlayerPose();
//		newPose.setPx(currentX);
//		System.out.println("X set to " + newPose.getPx());
//		newPose.setPy(currentY);
//		System.out.println("Y set to " + newPose.getPy());
//		newPose.setPa(bearing);
//		System.out.println("A set to " + newPose.getPa());
//		PathPlannerSimpleTest.motors.setOdometry(newPose);
		
		writeOdometry(currentX, currentY, bearing);
		
		
		// set odometry here to something like (4,4)
		
		
		motors.addPos2DListener(this);
		
		
	}
	
	
	public static void updateLocation(PlayerPose newLoc) { 
		
		currentX = calibrateX(newLoc.getPx());
		currentY = calibrateY(newLoc.getPy());
		bearing = calibrateAngle(newLoc.getPa());
		
		//boundary checking
		if (currentX < 0) {
//			System.out.println("currentX was neg");
			currentX = 0;
		}
		if (currentY < 0) {
//			System.out.println("currentY was neg");
			currentY = 0;
		}	
		
		//PathPlanner.writeOdometry(currentX, currentY, bearing);
		
		bearing = bearing % (2*Math.PI);
		
		if (bearing < -Math.PI) { 
			bearing += 2*Math.PI;
		} else if (bearing > Math.PI) {
			bearing -= 2*Math.PI;
		}
		
		// see if current Location == line again
		if(checkLine == true){
			double [] pos = {currentX, currentY};
			Integer [] coordinates = WorldView.locToCoord(pos);
			if(coordinates[0] == PathPlanner.getLocationSnapshot()[0] && coordinates[1] == PathPlanner.getLocationSnapshot()[1]){
				// do nothing
			}
			else {
				if(WorldView.world.get(coordinates[0]).get(coordinates[1]).getLinePoint()){
					LocationTracker.motors.setSpeed(0,0);
					System.out.println("set to false!!!");
					setLineCheck(false);
				}	
			}
		}
		
//		System.out.println("Being sent to recordLocation: " + currentX + ", " + currentY);
		WorldView.recordLocation(currentX, currentY);		
	}
	
	public static void setLineCheck (boolean t) {
		checkLine = t;
	}
	public static boolean getLineCheck () {
		return checkLine;
	}
	
	/**
	 * returns current location in terms of position (meters)
	 * @author Kevin
	 * @return array of doubles: {currentX, currentY, bearing}
	 */
	public static double [] getCurrentLocation () {
		double [] loc = {currentX, currentY, bearing};
		return loc;
	}
	
	/**
	 * returns current location in terms of coordinates, an (x,y) ordered pair
	 * @author Kevin
	 * @return array of Integers: {xCoord, yCoord}
	 */
	public static Integer [] getCurrentCoordinates() {
		Integer [] coords = WorldView.locToCoord(getCurrentLocation());
		return coords;
	}
	
	public static Integer[] getBottomLeftCoord(double[] loc){
		loc[0] -= WorldView.ROOMBA_RADIUS;
		loc[1] -= WorldView.ROOMBA_RADIUS;
		Integer[] coord =  WorldView.locToCoord(loc);
		return coord;
	}
	public static Integer[] getBottomRightCoord(double[] loc){
		loc[0] += WorldView.ROOMBA_RADIUS;
		loc[1] -= WorldView.ROOMBA_RADIUS;
		Integer[] coord =  WorldView.locToCoord(loc);
		return coord;
	}
	public static Integer[] getTopLeftCoord(double[] loc){
		loc[0] -= WorldView.ROOMBA_RADIUS;
		loc[1] += WorldView.ROOMBA_RADIUS;
		Integer[] coord =  WorldView.locToCoord(loc);
		return coord;
	}
	public static Integer[] getTopRightCoord(double[] loc){
		loc[0] += WorldView.ROOMBA_RADIUS;
		loc[1] += WorldView.ROOMBA_RADIUS;
		Integer[] coord =  WorldView.locToCoord(loc);
		return coord;
	}
	public static double getCurrentBearing(){
		return bearing;
	}
	
	
	public static String printCurrentLocation() {
		return "{" + currentX + ", " + currentY + ", " + bearing + "}";
	}
	
	public static String printCurrentCoordinates() {
		Integer [] c = getCurrentCoordinates();
		return "{" + c[0] + ", " + c[1] + "}";
	}
	
	private static double calibrateX(double xValue) {
		// take odometer value and convert to accurate X distance
		// return (double) (-3.7331*xValue*xValue+8.1825*xValue-3.466);
		return xValue;
	}
	
	
	private static double calibrateY(double yValue) {
		// take odometer value and convert to accurate Y distance
		//return (double) (-3.7331*yValue*yValue+8.1825*yValue-3.466);
		return yValue;
	}
	
	
	private static double calibrateAngle(double angle) {
		// take odometer value and convert to accurate angle measurement
		return angle;
	}


	@Override
	public void newHeading(double heading) {
		//this is where the compass data is received
		System.out.println(heading);
	}
	
	
	public static void writeOdometry(double newX, double newY, double newAngle) {
		PlayerPose newPose = new PlayerPose();
		newPose.setPx(newX);
//		System.out.println("X set to " + newPose.getPx());
		newPose.setPy(newY);
//		System.out.println("Y set to " + newPose.getPy());
		newPose.setPa(newAngle);
//		System.out.println("A set to " + newPose.getPa());
//		System.out.println("Writing Odom: newX=" + newX + ", newY=" + newY + ", newA=" + newAngle);
		
		motors.setOdometry(newPose);
		return;
	}
	
	//@Override
	public void newPlayerPosition2dData(PlayerPosition2dData data) {
		PlayerPose pp = data.getPos();
		
		//insert 5-wide median filter here
		//if(calibrateTurn) writeOdometry(pp.getPx(), pp.getPy(), pp.getPa()*1.005);
		PathPlanner.setPlayerPose(pp);
		LocationTracker.updateLocation(pp);
		
		//writeOdometry(pp.getPx(), pp. getPy(), pp.getPa());
		//		}
		//log("Odometry Data: x=" + pp.getPx() + ", y=" + pp.getPy() + ", a=" + pp.getPa() 
		//		+ ", vela=" + data.getVel().getPa() + ", stall=" + data.getStall());
		
//		log("Odometry Data: x=" + pp.getPx() + ", y=" + pp.getPy() + ", a=" + pp.getPa() 
//				+ ", vela=" + data.getVel().getPa() + ", stall=" + data.getStall());
				
//		System.out.println("Odometry Data: x=" + pp.getPx() + ", y=" + pp.getPy() + ", a=" + pp.getPa());
	}
	
	public static void setTurnCalibrate(boolean truth){
		calibrateTurn = truth;
	}


	public static double getInitialx() {
		return initialX;
	}
	
	public static double getInitialy() {
		return initialY;
	}
	

}




