package pharoslabut.behavior;

import pharoslabut.navigate.Location;

/**
 * This class holds a latitude, longitude and velocity.
 * It is used while processing the experiment configuration specifications.
 * 
 * @author Noa Agmon
 * @see pharoslabut.behavior.fileParsing.ReadWithScanner
 * @see pharoslabut.behavior.fileParsing.StringParsing
 */
public class MissionData {
	public static final double DEFAULT_VELOCITY   = 3.0;
	
	private double latitude;
	private double longitude;
	private double velocity;
	

	/**
	 * A constructor that uses the default velocity.
	 * 
	 * @param latitude
	 * @param longitude
	 */
	public MissionData(double latitude, double longitude){
		this(latitude, longitude, DEFAULT_VELOCITY);
	}
	
	/**
	 * A constructor that fully-specifies the fields in this class.
	 * 
	 * @param latitude
	 * @param longitude
	 * @param velocity
	 */
	public MissionData(double latitude, double longitude, double velocity){
		this.latitude = latitude;
		this.longitude = longitude;
		this.velocity = velocity;
	}

//	public void SetLatitude(double latitude){this.latitude = latitude;}
//	public void SetLongitude(double longitude){this.longitude = longitude;}
//	public void SetVelocity(double velocity){this.velocity = velocity;}
	
//	public double GetLatitude(){return latitude;}
//	public double GetLongitude(){return longitude;}
	
	public Location getDest() {
		return new Location(latitude, longitude);
	}
	
	public double getVelocity(){
		return velocity;
	}
}
