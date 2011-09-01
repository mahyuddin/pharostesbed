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
 */
public class BehGotoGPSCoord extends Behavior {
	private NavigateCompassGPS _navigatorGPS;
	private boolean _stopAtEndBehavior;
	private Location _destLoc;
//	protected static FileLogger _flogger = null;
	public static final double GPS_TARGET_RADIUS_METERS = 3.5;
	private TargetDirection _lastTargetDirection;
	private double _lastCurrHeading;
	private MissionData _md;
	private Location _lastcurrLoc;
	private boolean _simulateAll = false;

	
	public BehGotoGPSCoord(WorldModel wm, MissionData md, NavigateCompassGPS navigateData) {
		super(wm, md);
		
		if(System.getProperty ("simulateBehave") != null)
			_simulateAll = true;
		
		_lastcurrLoc = null;
		_md = md;
		
		if(_wm.getTeamSize() > 1){
			_stopAtEndBehavior = true;
		} else {
			Logger.log("No teammates; not stopping at the end of the behavior");
			_stopAtEndBehavior = false;
		}
		
		Logger.log("Constructor behavior; latitude = "+ _md.GetLatitude() + "longitude = "+ _md.GetLongitude());

		_navigatorGPS = navigateData;
		
		
		_destLoc = new Location(_md.GetLatitude(), _md.GetLongitude());
		_lastTargetDirection = null;
	}
	

	@Override
	public boolean startCondition() {
		// Check if the robot can receive GPS data
		_wm.resetCount();
		
		if(_simulateAll)
			return true;
		
		Logger.log("start condition for behavior " + _behaveIndex);
		
		PlayerGpsData gpsData = _navigatorGPS.getLocation();
		
		if(gpsData == null){
			Logger.logErr("No current location (GPS null), thus start condition is false.");
			return false;
		}
		
		_wm.resetCount();
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
		
		
		PlayerGpsData pgpsd = _navigatorGPS.getLocation();
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
		
		_lastCurrHeading = _navigatorGPS.getCompassHeading();
		_lastTargetDirection = _navigatorGPS.locateTarget(_lastcurrLoc, _lastCurrHeading, _destLoc);
		if (_lastTargetDirection.getDistance() < GPS_TARGET_RADIUS_METERS) {
			Logger.log("Destination reached! "+ _behaveIndex);
			// if we should stop at the end of the behavior (single robot case) - instruct the robot to stop!
			if(_stopAtEndBehavior)
				_navigatorGPS.stopRobot();
			return true;
		}else if (_lastTargetDirection.getDistance() > 2000) {
			Logger.logErr("Invalid distance: Greater than 2km (" + _lastTargetDirection.getDistance() + "), stopping robot...");
			_navigatorGPS.stopRobot();
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
			_navigatorGPS.SubmitMotionTask(_lastTargetDirection, _md.GetVelocity());
			Logger.log("Running action, _LastTargetDirection=" + _lastTargetDirection + ", velocity=" + _md.GetVelocity() + "\n");
		} else { //stop robot if we have no GPS data
			if(_lastTargetDirection == null)
				Logger.log("Last target direction not set, stopping the robot...");
			else
				Logger.log("_LastCurrHeading is not valid, stopping the robot...");
			
			_navigatorGPS.stopRobot();
		}
	}
		
		
		
//
//	private void logErr(String msg) {
//		String result = "BehGotoGPSCoord: ERROR: " + msg;
//		
//		System.err.println(result);
//		
//		// always log text to file if a FileLogger is present
//		if (_flogger != null)
//			_flogger.log(result);
//	}
//	
//	private void log(String msg) {
//		String result = "BehGotoGPSCoord: " + msg;
//		
//		// only print log text to string if in debug mode
//		if (System.getProperty ("PharosMiddleware.debug") != null)
//			System.out.println(result);
//		
//		// always log text to file if a FileLogger is present
//		if (_flogger != null)
//			_flogger.log(result);
//	}
	
	public String toString() {
		return getClass().getName() + ": dest = " + _destLoc + ", stopAtEndBehavior = " + _stopAtEndBehavior;
	}

}
