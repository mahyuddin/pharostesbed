package pharoslabut.behavior;

import pharoslabut.behavior.management.WorldModel;
import pharoslabut.navigate.Location;
//import pharoslabut.navigate.MotionArbiter;
import pharoslabut.navigate.NavigateCompassGPS;
import pharoslabut.navigate.TargetDirection;
import pharoslabut.sensors.*;
//import pharoslabut.tasks.*;
import pharoslabut.logger.*;
import pharoslabut.exceptions.NoNewDataException;
/*import playerclient3.GPSInterface;
import playerclient3.PlayerClient;
import playerclient3.PlayerException;
import playerclient3.Position2DInterface;
import playerclient3.structures.PlayerConstants;
*/

public class BehGotoGPSCoord extends Behavior{
	
/*	private PlayerClient _client;
	private Position2DInterface _motors;
	private Position2DInterface _compass;
	private MotionArbiter _motionArbiter;
	private GPSInterface _gps;*/
//	private CompassDataBuffer _compassDataBuffer;
//	private GPSDataBuffer _gpsDataBuffer;
	private NavigateCompassGPS _navigatorGPS;
	private Location _destLoc;
	protected static FileLogger _flogger = null;
	public static final double GPS_TARGET_RADIUS_METERS = 3.5;
	private TargetDirection _LastTargetDirection;
	private double _LastCurrHeading;
	private MissionData _md;
	private Location _LastcurrLoc;
	private boolean _simulateAll = false;

	
	public BehGotoGPSCoord(WorldModel wm, MissionData md, NavigateCompassGPS navigateData, FileLogger flogger) {
		// TODO Auto-generated constructor stub
		super(wm, md);
		
		_flogger = flogger;
		
		if(System.getProperty ("simulateBehave") != null)
			_simulateAll = true;
		
		_LastcurrLoc = null;
		_md = md;
		
		log("Constructor behavior");
		_navigatorGPS = navigateData;
		
		
		_destLoc = new Location(md.GetLatitude(), md.GetLongitude());
		_LastTargetDirection = null;
	}
	

	@Override
	public boolean startCondition() {
		// Check if the robot can receive GPS data
		Location currLoc = null;
		_wm.setCount(0);
		
		if(_simulateAll)
			return true;
		log("\t BehGotoGPSCoord: start condition for behavior "+_behaveIndex);
		currLoc = new Location(_navigatorGPS.getLocation());
		if( currLoc== null){
			logErr("BehGotoGPSCoord Start condition: no current location (GPS null)");
			return false;
		}
			
		_wm.setCount(0);
		return true;
	}

	@Override
	public boolean stopCondition() {
		// simulation mode - run each behavior 10 times
		int mycounter = _wm.getCount();
		if(_simulateAll){
			_wm.setCount(mycounter+1);
			log("Running behavior "+_behaveIndex+" for the "+mycounter+" time");
			if (mycounter < 20)
				return false;
			else
				return true;
		}
			
			// If the coordinates are not close enough to the destination - continue moving.
			
		_LastcurrLoc = new Location(_navigatorGPS.getLocation());
		if( _LastcurrLoc== null){
			logErr("BehGotoGPSCoord Stop Condition: no current location (gps is null)");
			return true;
		} else {
			log("Current location: " + _LastcurrLoc);
		}
		
		_LastCurrHeading = _navigatorGPS.getCompassHeading();
		_LastTargetDirection = _navigatorGPS.locateTarget(_LastcurrLoc, _LastCurrHeading, _destLoc);
		if (_LastTargetDirection.getDistance() < GPS_TARGET_RADIUS_METERS) {
			log("Destination reached!");
			_navigatorGPS.stopRobot();
			return true;
		}else if (_LastTargetDirection.getDistance() > 2000) {
			log("Invalid distance: Greater than 2km (" + _LastTargetDirection.getDistance() + "), stopping robot...");
			return true;
		} else{
			log("Haven't reached destination yet, stop condition false\n");
			return false;
		}
	}

	@Override
	public void action() {
		if(_simulateAll)
			return;
		_navigatorGPS.SubmitMotionTask(_LastTargetDirection, _md.GetVelocity());
		log("running action()\n");
	}
		
		
		

	private void logErr(String msg) {
		String result = "BehGotoGPSCoord: ERROR: " + msg;
		
		System.err.println(result);
		
		// always log text to file if a FileLogger is present
		if (_flogger != null)
			_flogger.log(result);
	}
	
	private void log(String msg) {
		String result = "BehGotoGPSCoord: " + msg;
		
		// only print log text to string if in debug mode
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
		
		// always log text to file if a FileLogger is present
		if (_flogger != null)
			_flogger.log(result);
	}

}
