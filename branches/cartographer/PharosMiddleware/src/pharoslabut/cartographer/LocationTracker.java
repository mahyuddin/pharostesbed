package pharoslabut.cartographer;

import playerclient.structures.PlayerPose;


public class LocationTracker {
	
	// change initial values to reflect the robot's starting orientation
	private static final double initialBearing = 0;
	private static final double initialX = 0;
	private static final double initialY = 0;
	
	private static double currentX;
	private static double currentY;
	
	private static double bearing; // -PI to +PI
	// 0 is north, -PI/2 is west, -PI or +PI are both south, and PI/2 is east
	
	
	public LocationTracker() {
		// robot begins in upper-left corner with a bearing of "initialBearing"
		currentX = initialX; currentY = initialY; bearing = initialBearing; 
		
		// To reset the robot's odometry to (x, y, yaw) = (0,0,0), send
		// a PLAYER_POSITION2D_REQ_RESET_ODOM request.  Null response.
		// note: the PlayerClient pkgs refer to the turn angle as "yaw"
		PathPlanner.writeOdometry(initialX, initialY, initialBearing);		
		
	}
	
	
	public static void updateLocation(PlayerPose newLoc) { 
		
		currentX = calibrateX(newLoc.getPx());
		currentY = calibrateY(newLoc.getPy());
		
		// if the writeOdometry line in the Constructor works, then we don't need to add initialBearing to the calibrated new location
		bearing = (Math.abs(initialBearing + calibrateAngle(newLoc.getPa()))); 
		if (bearing < -Math.PI) { 
			bearing = (bearing % (2*Math.PI)) + Math.PI;
		} else if (bearing > Math.PI) {
			bearing = (bearing % (2*Math.PI)) - Math.PI;
		}
		
		WorldView.recordLocation(currentX, currentY);
	}
	
	
	/**
	 * @author Kevin
	 * @return array of doubles: {currentX, currentY, bearing}
	 */
	public static double [] getCurrentLocation () {
		double [] loc = {currentX, currentY, bearing};
		return loc;
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
}



