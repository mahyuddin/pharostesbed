package pharoslabut.behavior;

import pharoslabut.behavior.management.WorldModel;
import pharoslabut.navigate.Location;
import pharoslabut.navigate.NavigateCompassGPS;
import pharoslabut.navigate.TargetDirection;
import pharoslabut.logger.*;
import playerclient3.structures.gps.PlayerGpsData;

/**
 * Implements a "go to GPS coordinate" behavior.
 * 
 * @author Noa Agmon
 * @author Chien-Liang Fok
 */
public class BehGotoGPSCoord extends Behavior {
	private NavigateCompassGPS navigator;
	private boolean _stopAtEndBehavior;
	
	/**
	 * The destination location.
	 */
	private Location dest;
	
	/**
	 * The speed at which the robot should move.
	 */
	private double speed;
	
	public static final double GPS_TARGET_RADIUS_METERS = 3.5;
	
	private TargetDirection _lastTargetDirection = null;
	private double _lastCurrHeading;
//	private MissionData _md;
	private Location _lastcurrLoc = null;
	private boolean _simulateAll = false;

	
	/**
	 * The constructor.
	 * 
	 * @param wm The world model.
	 * @param dest The destination location.
	 * @param navigator The object that actually moves the robot to the destination.
	 */
	public BehGotoGPSCoord(WorldModel wm, Location dest, double speed, NavigateCompassGPS navigator) {
		super(wm);
		
		this.speed = speed;
		this.dest = dest;
		this.navigator = navigator;
		
		if(System.getProperty ("simulateBehave") != null)
			_simulateAll = true;
		
		if(_wm.getTeamSize() > 1) {
			_stopAtEndBehavior = true;
		} else {
			Logger.log("No teammates; not stopping at the end of the behavior");
			_stopAtEndBehavior = false;
		}
		
		Logger.log("Constructor: dest = " + dest + ", speed = " + speed);
	}
	

	@Override
	public boolean startCondition() {
		// Check if the robot can receive GPS data
		_wm.resetCount();
		
		if(_simulateAll) {
			Logger.log("Start condition for behavior " + _behaveIndex + " is true because we're in simulation mode.");
			return true;
		}
		
		PlayerGpsData gpsData = navigator.getLocation();
		
		if(gpsData == null) {
			Logger.logErr("Start condition for behavior " + _behaveIndex + " is false because the GPS is not working.");
			return false;
		}
		
		_wm.resetCount();
		
		Logger.logDbg("Start condition for behavior " + _behaveIndex + " is true");
		return true;
	}

	@Override
	public boolean stopCondition() {
		Logger.log("start method call...");
		
		_lastTargetDirection = null; // reset the direction command...
		
		// simulation mode - run each behavior 10 times
//		int mycounter = _wm.getCount();
//		if(_simulateAll){
//			_wm.setCount(mycounter+1);
//			log("Running behavior "+_behaveIndex+" for the "+mycounter+" time");
//			if (mycounter < 20)
//				return false;
//			else
//				return true;
//		}
//			
			// If the coordinates are not close enough to the destination - continue moving.
		
		
		PlayerGpsData pgpsd = navigator.getLocation();
		if (pgpsd != null)
			_lastcurrLoc = new Location(pgpsd);
		else
			_lastcurrLoc = null;
		
		if( _lastcurrLoc== null){
			Logger.logErr("Stop Condition: no current location (gps is null)");
			//return true;
			
			// No GPS data at this time, continue to execute the current behavior
			return false;
		} else {
			Logger.log("Current location: " + _lastcurrLoc);
		}
		
		_lastCurrHeading = navigator.getCompassHeading();
		_lastTargetDirection = navigator.locateTarget(_lastcurrLoc, _lastCurrHeading, dest);
		if (_lastTargetDirection.getDistance() < GPS_TARGET_RADIUS_METERS) {
			Logger.log("Destination reached! "+ _behaveIndex);
			// if we should stop at the end of the behavior (single robot case) - instruct the robot to stop!
			if(_stopAtEndBehavior)
				navigator.stopRobot();
			return true;
		}else if (_lastTargetDirection.getDistance() > 2000) {
			Logger.logErr("Invalid distance: Greater than 2km (" + _lastTargetDirection.getDistance() + "), stopping robot...");
			navigator.stopRobot();
			return true;
		} else{
			Logger.log("Haven't reached destination yet, stop condition false\n");
			return false;
		}
	}

	@Override
	public void action() {
		if(_simulateAll)
			return;
		if (_lastTargetDirection != null && (_lastCurrHeading != NavigateCompassGPS.ERROR_HEADING)) { 
			navigator.SubmitMotionTask(_lastTargetDirection, speed);
			Logger.log("Running action, _LastTargetDirection=" + _lastTargetDirection + ", velocity=" + speed + "\n");
		} else { //stop robot if we have no GPS data
			if(_lastTargetDirection == null)
				Logger.log("Last target direction not set, stopping the robot...");
			else
				Logger.log("_LastCurrHeading is not valid, stopping the robot...");
			
			navigator.stopRobot();
		}
	}
	
	public String toString() {
		return getClass().getName() + ": dest = " + dest + ", stopAtEndBehavior = " + _stopAtEndBehavior;
	}

}
