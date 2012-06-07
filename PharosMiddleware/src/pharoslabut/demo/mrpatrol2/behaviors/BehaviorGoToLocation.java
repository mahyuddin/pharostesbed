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
	
	private Location startLoc, destLoc;
	
	private double speed;
	
	boolean isDone = false;
	
//	Behavior childBehavior = null;
	
	/**
	 * A constructor that is used when the starting location is not known.
	 * 
	 * @param name The name of the behavior.
	 * @param navigatorCompassGPS The navigator that uses compass and GPS sensors.
	 * @param destLoc The ideal destination location.
	 * @param speed The speed at which to travel towards the destination location.
	 */
	public BehaviorGoToLocation(String name, NavigateCompassGPS navigatorCompassGPS, 
			Location destLoc, double speed) 
	{
		this(name, navigatorCompassGPS, null, destLoc, speed);
	}
	
	/**
	 * The constructor.
	 * 
	 * @param name The name of the behavior.
	 * @param navigatorCompassGPS The navigator that uses compass and GPS sensors.
	 * @param startLoc The ideal starting location.
	 * @param destLoc The ideal destination location.
	 * @param speed The speed at which to travel towards the destination location.
	 */
	public BehaviorGoToLocation(String name, NavigateCompassGPS navigatorCompassGPS, 
			Location startLoc, Location destLoc, double speed) 
	{
		super(name);
		this.navigatorCompassGPS = navigatorCompassGPS;
		this.startLoc = startLoc;
		this.destLoc = destLoc;
		this.speed = speed;
	}
	
	public Location getDestination() {
		return destLoc;
	}
	
//	public void setChildBehavior(Behavior childBehavior) {
//		this.childBehavior = childBehavior;
//	}
	
	@Override
	public void run() {
		navigatorCompassGPS.go(startLoc, destLoc, speed);
		isDone = true;
		Logger.log("Behavior " + getName() + " done.");
	}

	public boolean areWeThereYet(long aheadTime) {
		if (isDone()) {
			//Logger.logDbg("We are there because this behavior is done.");
			return true;
		}
		
		if (!started) {
			//Logger.logDbg("We are not there yet because this behavior is not started.");
			return false;
		}
		
		return navigatorCompassGPS.areWeThereYet(aheadTime);
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

	@Override
	public void stop() {
		navigatorCompassGPS.stop();
		isDone = true;
		
	}
}
