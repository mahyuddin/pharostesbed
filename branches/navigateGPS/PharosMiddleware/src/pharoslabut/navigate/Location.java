package pharoslabut.navigate;

import playerclient3.structures.gps.PlayerGpsData;

public class Location implements java.io.Serializable {
	private static final long serialVersionUID = -2689555631414682934L;
	private double latitude, longitude, elevation;
	private long time = System.currentTimeMillis(); // time of location
	
	/**
	 * A constructor that extract location information from GPS sensor data.
	 * 
	 * @param gpsData The GPS sensor data from the Player middleware.
	 */
	public Location(PlayerGpsData gpsData) {
			this(gpsData.getLatitude()/1e7, gpsData.getLongitude()/1e7, gpsData.getAltitude());
	}
	
	/**
	 * A constructor that takes the latitude and longitude explicitly.
	 * 
	 * @param latitude The latitude.
	 * @param longitude The longitude.
	 */
	public Location(double latitude, double longitude) {
		this(latitude, longitude, 0);
	}
	
	/**
	 * A constructor that takes the latitude, longitude, and elevation explicitly.
	 * 
	 * @param latitude The latitude.
	 * @param longitude The longitude.
	 * @param elevation The elevation.
	 */	
	public Location(double latitude, double longitude, double elevation) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.elevation = elevation;
	}
	
	public boolean equals(Object obj) {
		if (obj != null) {
			if (obj instanceof Location) {
				Location coord = (Location)obj;
				return coord.latitude() == latitude() && 
					coord.longitude() == longitude() &&
					coord.elevation() == elevation();
			}
		}
		return false;
	}
	
	public double latitude() {
		return latitude;
	}
	
	public double longitude() {
		return longitude;
	}
	
	public double elevation() {
		return elevation;
	}
	
	public long getTime() {
		return time;
	}
	
	public void setTime(long newTime) {
		this.time = newTime;
	}
	
	/*::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::                                                                         :*/
	/*::  This routine calculates the distance between two points (given the     :*/
	/*::  latitude/longitude of those points). It is being used to calculate     :*/
	/*::  the distance between two ZIP Codes or Postal Codes using our           :*/
	/*::  ZIPCodeWorld(TM) and PostalCodeWorld(TM) products.                     :*/
	/*::                                                                         :*/
	/*::  Definitions:                                                           :*/
	/*::    South latitudes are negative, east longitudes are positive           :*/
	/*::                                                                         :*/
	/*::  Passed to function:                                                    :*/
	/*::    lat1, lon1 = Latitude and Longitude of point 1 (in decimal degrees)  :*/
	/*::    lat2, lon2 = Latitude and Longitude of point 2 (in decimal degrees)  :*/
	/*::    unit = the unit you desire for results                               :*/
	/*::           where: 'M' is statute miles                                   :*/
	/*::                  'K' is kilometers (default)                            :*/
	/*::                  'N' is nautical miles                                  :*/
	/*::  United States ZIP Code/ Canadian Postal Code databases with latitude & :*/
	/*::  longitude are available at http://www.zipcodeworld.com                 :*/
	/*::                                                                         :*/
	/*::  For inquiries, please contact sales@zipcodeworld.com                   :*/
	/*::                                                                         :*/
	/*::  Official Web site: http://www.zipcodeworld.com                         :*/
	/*::                                                                         :*/
	/*::  Hexa Software Development Center (c) All Rights Reserved 2004          :*/
	/*::                                                                         :*/
	/*::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	private static double distance(double lat1, double lon1, double lat2, double lon2, char unit) {
		
		// check for special condition of exact match
		//if ((lat1 - lat2) < 0.000001 && (lon1 - lon2) < 0.000001) return 0;
		if (lat1 == lat2 && lon1 == lon2) return 0;
		
		double theta = lon1 - lon2;
		
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		
//		System.out.println("theta=" + theta + ", dist = " + dist);
		
		// HACK:  not sure why this would ever happen
		// TODO: Remove this after figuring out why this happens. See Mission 9, experiment 3, Relative Divergence calculation for an example of when it happens
		if (dist > 1) dist = 1;
		
		dist = Math.acos(dist);
//		System.out.println("After acos(...), dist = " + dist);
		
		dist = rad2deg(dist);
//		System.out.println("After rad2deg(...), dist = " + dist);
		
		dist = dist * 60 * 1.1515;
		if (unit == 'K') {
			dist = dist * 1.609344;
		} else if (unit == 'N') {
			dist = dist * 0.8684;
		}
		return (dist);
	}

	/**
	 * Finds the angle formed by the line between two points in reference to the horizontal. 
	 * The angle is adjusted to correspond to the unit circle.
	 * 
	 * @param lat1 latitude of first point in decimal degrees
	 * @param lon1 longitude of first point in decimal degrees
	 * @param lat2 latitude of second point in decimal degrees
	 * @param lon2 longitude of second point in decimal degrees
	 * @return the angle formed by the line between the two points (in radians), adjusted to the unit circle.
	 */
	private static double angle(double lat1, double lon1, double lat2, double lon2) {
		
		// check for special condition of exact match
		//if ((lat1 - lat2) < 0.000001 && (lon1 - lon2) < 0.000001) return 0;
		if (lat1 == lat2 && lon1 == lon2) return 0;
		
		double theta = 0; // angle between the two points
		double yDelta = deg2rad(lat2 - lat1); // latitude is the y direction
		double xDelta = deg2rad(lon2 - lon1); // longitude is the x direction
		
		theta = Math.atan(yDelta/xDelta);
		
		// make quadrant adjustments
		if ( (xDelta <= 0 && yDelta >= 0) || (xDelta <= 0 && yDelta < 0) ) { // 2nd & 3rd quadrant
			theta = Math.PI + theta;
		} else if( xDelta > 0 && yDelta < 0 ) { // 4th quadrant
			theta = (2*Math.PI) + theta;
		}
		
		return theta;
	}
	
	/**
	 * This function converts decimal degrees to radians
	 * 
	 * @param deg angle measurement in decimal degree units
	 * @return angle measurement in radians
	 */
	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/**
	 * This function converts radians to decimal degrees   
	 * 
	 * @param rad angle measurement in radian units
	 * @return angle measurement in degrees
	 */
	private static double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}

	/**
	 * Returns the distance between two GPS coordinates in meters.
	 * 
	 * @param targetLoc The destination location
	 * @return The distance between currLoc and targetLoc in meters.
	 */
	public double distanceTo(Location targetLoc) {
		double result = distance(latitude(), longitude(), 
				targetLoc.latitude(), targetLoc.longitude(), 'K');
		return result * 1000; // convert to meters
	}
	
	public double angleTo(Location targetLoc) {
		double result = angle(latitude(), longitude(), targetLoc.latitude(), targetLoc.longitude());
		return result;
	}
	
	public String toString() {
		return "(" + latitude + ", " + longitude + ", " + elevation + ")";
	}
	
	public static void main(String[] args) {
		//Location currLoc = new Location(30.2890583,-97.7359517);
		//Location destLoc = new Location(30.2891267,-97.7358933);
		//Location currLoc = new Location(30.2891183,	-97.7358567); // point A
		//Location destLoc = new Location(30.2811217,	-97.7641183); // point B
//		Location currLoc = new Location(30.2888667,	-97.7357467); // point C
//		Location destLoc = new Location(30.2890833,	-97.7359383); // point A 2
//		
//		double dist = currLoc.distanceTo(destLoc);
//		System.out.println("The distance between " + currLoc + " and " + destLoc + " is " + dist + " meters.");
		
		Location currLoc = new Location(0,0); // point C
		Location destLoc = new Location(0.0005,	0); // point A 2
		
		double dist = currLoc.distanceTo(destLoc);
		System.out.println("The distance between " + currLoc + " and " + destLoc + " is " + dist + " meters.");
		
	}
}
