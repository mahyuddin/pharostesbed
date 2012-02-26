package pharoslabut.demo.mrpatrol2.behaviors;

//import pharoslabut.demo.mrpatrol2.config.ExpConfig;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.Location;
import pharoslabut.navigate.NavigateCompassGPS;

/**
 * Implements a behavior that moves the robot to a particular location.
 * 
 * @author Chien-Liang Fok
 *
 */
public class BehaviorGoToLocation extends Behavior {

	/**
	 * Navigates a robot from its current location to a specified location.
	 */
	private NavigateCompassGPS navigatorCompassGPS;
	
	private Location destLoc;
	
	private double speed;
	
	boolean isDone = false;
	
	/**
	 * The constructor.
	 * 
	 * @param name The name of the behavior.
	 * @param navigatorCompassGPS The navigator that uses compass and GPS sensors.
	 * @param destLoc The destination location.
	 * @param speed The speed at which to travel towards the destination location.
	 */
	public BehaviorGoToLocation(String name, NavigateCompassGPS navigatorCompassGPS, Location destLoc, double speed) {
		super(name);
		this.navigatorCompassGPS = navigatorCompassGPS;
		this.destLoc = destLoc;
		this.speed = speed;
	}
	
	@Override
	public void run() {
		navigatorCompassGPS.go(destLoc, speed);
		isDone = true;
		Logger.log("Behavior " + getName() + " done.");
	}

//	@Override
//	public boolean canStart() {
//		return super.canStart();
//	}

	@Override
	public boolean isDone() {
		return isDone;
	}

	/**
	 * @return A string representation of this class.
	 */
	@Override
	public String toString() {
		return "BehaviorGoToLocation " + super.toString() + ", destLoc = " + destLoc + ", speed = " + speed;
	}
}
