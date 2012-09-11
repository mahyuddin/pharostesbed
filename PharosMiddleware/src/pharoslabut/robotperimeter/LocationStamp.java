package robotPerimeter;
import pharoslabut.navigate.Location;


public class LocationStamp {

	Location location;
	public double timestamp;
	
	public LocationStamp(Location location, double timestamp){
		this.location = location;
		this.timestamp = timestamp;
	}
	public LocationStamp(){}
	
	public String toString()
	{
		return location.toString() + " timestamp: " + timestamp;
	}
	
}
