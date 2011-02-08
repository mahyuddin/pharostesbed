package pharoslabut.cartographer;

public class LocationTracker {
	private static int currentX;
	private static int currentY;
	
	public LocationTracker() {
		
	}
	
	public synchronized void updateLocation() { 
		// this might take in parameters, or it could read them in here
		// remember, it must factor in expected location from PathPlanner, 
		   // and also real location from Player odometer
	}
}
