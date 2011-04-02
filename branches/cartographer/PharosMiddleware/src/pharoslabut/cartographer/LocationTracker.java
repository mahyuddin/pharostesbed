package pharoslabut.cartographer;

import playerclient.structures.PlayerPose;
import pharoslabut.logger.CompassLoggerEvent;
import pharoslabut.logger.CompassLoggerEventListener;

public class LocationTracker implements CompassLoggerEventListener{
	private static CompassLoggerEvent compassLogger;
	// change initial values to reflect the robot's starting orientation
	private static final double initialX = WorldView.ROOMBA_RADIUS + 2*WorldView.RESOLUTION;
	private static final double initialY = WorldView.ROOMBA_RADIUS + 2*WorldView.RESOLUTION;
	
	private static double currentX;
	private static double currentY;
	
	private static double bearing; // -PI to +PI
	// 0 is east, PI/2 is north, PI and -PI are west, and -PI/2 is south
	
	
	public LocationTracker() {
		
		
		compassLogger = new CompassLoggerEvent(PathPlannerSimpleTest.serverIP, 7777, 1, false);
																	// 1 is device index, true means showGUI
		compassLogger.addListener(this);
		
		// this just logs the data to a file
		compassLogger.start(1, "compasslog.txt"); // first param is ignored
			
		
		// robot begins in lower-left corner with a bearing of 0, facing east
		currentX = initialX; currentY = initialY; 
		
		// To reset the robot's odometry to (x, y, yaw) = (0,0,0), send
		// a PLAYER_POSITION2D_REQ_RESET_ODOM request.  Null response.
		// note: the PlayerClient pkgs refer to the turn angle as "yaw"
		
//		PathPlanner.writeOdometry(initialX, initialY, 0);	
		
		
		
	}
	
	
	public static void updateLocation(PlayerPose newLoc) { 
		
		currentX = calibrateX(newLoc.getPx() + initialX);
		currentY = calibrateY(newLoc.getPy() + initialY);
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
//		PathPlanner.writeOdometry(currentX, currentY, bearing);
		
		bearing = bearing % (2*Math.PI);
		
		if (bearing < -Math.PI) { 
			bearing += 2*Math.PI;
		} else if (bearing > Math.PI) {
			bearing -= 2*Math.PI;
		}
		
//		System.out.println("Being sent to recordLocation: " + currentX + ", " + currentY);
		WorldView.recordLocation(currentX, currentY);
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
	
	
	public static String printCurrentLocation() {
		return "{" + currentX + ", " + currentY + ", " + bearing + "}";
	}
	
	public static String printCurrentCoordinates() {
		Integer [] c = getCurrentCoordinates();
		return "{" + c[0] + ", " + c[1] + "}";
	}
	
	private static double calibrateX(double xValue) {
		// take odometer value and convert to accurate X distance
		return xValue;
	}
	
	
	private static double calibrateY(double yValue) {
		// take odometer value and convert to accurate Y distance
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
}



