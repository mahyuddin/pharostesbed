package pharoslabut.behavior;

/*
import pharoslabut.behavior.management.WorldModel;
import pharoslabut.navigate.*;
import pharoslabut.sensors.CompassDataBuffer;
import pharoslabut.sensors.GPSDataBuffer;
import pharoslabut.tasks.MotionTask;
import pharoslabut.tasks.Priority;
import pharoslabut.logger.FileLogger;
import pharoslabut.exceptions.NoNewDataException;
*/

public class MissionData {
	double _Latitude;
	double _Longitude;
	double _Velocity;
	public static final double DEFAULT_VELOCITY   = 3.0;

	public MissionData(double latitude, double longitude, double velocity){
		_Latitude = latitude;
		_Longitude = longitude;
		_Velocity = velocity;
	}

	MissionData(double latitude, double longitude){
		this(latitude, longitude, DEFAULT_VELOCITY);
	}

	public void SetLatitude(double lat){_Latitude = lat;}
	public void SetLongitude(double lon){_Longitude = lon;}
	public void SetVelocity(double velocity){_Velocity = velocity;}
	
	public double GetLatitude(){return _Latitude;}
	public double GetLongitude(){return _Longitude;}
	public double GetVelocity(){return _Velocity;}
	
	
}
