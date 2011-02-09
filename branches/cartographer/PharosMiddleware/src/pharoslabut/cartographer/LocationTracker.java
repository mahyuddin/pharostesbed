package pharoslabut.cartographer;

public class LocationTracker {
	private static int currentX;
	private static int currentY;
	
	private static double bearing; // 0 to 2pi radians counterclockwise
	  // 0 is North, pi/2 is West, pi is South, 3*pi/2 (-pi/2) is East
	
	public LocationTracker() {
		// robot begins in upper-left corner facing down (south)
		currentX = 0; currentY = 0; bearing = Math.PI; 
		
		// To reset the robot's odometry to (x, y, yaw) = (0,0,0), send
		// a PLAYER_POSITION2D_REQ_RESET_ODOM request.  Null response.
		// note: the PlayerClient pkgs refer to the turn angle as "yaw"
				
		
		
	}
	
	public static synchronized void updateLocation() { 
		// this might take in parameters, or it could read them in here
		// remember, it must factor in expected location from PathPlanner, 
		   // and also real location from Player odometer
		
	}
}
