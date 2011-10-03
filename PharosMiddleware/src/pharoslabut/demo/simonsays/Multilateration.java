package pharoslabut.demo.simonsays;

import java.awt.Polygon;
import java.awt.geom.Path2D;
import java.util.*;
import java.util.Map.Entry;

import playerclient3.structures.PlayerPoint2d;

/**
 * Holds a continuous running thread that calculates the robot's location using at least three beacons' distances and coordinates. 
 * Each beacon is a cricket mote, as set up in the SimonSaysServer and used in the SimonSaysClient. 
 * 
 * <br><br>***** TRIANGULATION ALGORITHM *****<br>
 * This requires <b>(n-1)!</b> iterations, where <b>n</b> is the number of unique Cricket beacons.<br>  
 * 1) Take two circles (beacon readings), solve system of those two equations to come up with zero, one, or two intersection points.<br>
 * 2) Add those intersection points to the set of all intersection points.<br> 
 * 3) Remove any points outside of the beacon area.<br>
 * 4) Repeat Steps 1-3 with two different circles until all the unique combinations of two circles have been used.<br>
 * 5) Find the point that has a minimum distance between every other point.<br>
 * 
 * @author Kevin Boos
 * @see SimonSaysServer
 * @see SimonSaysClient
 */
public class Multilateration extends Thread{
	
	/** 
	 * the robot's current location. This is updated in real time, provided there are enough unique beacon readings available
	 */
	private static PlayerPoint2d currentLocation = new PlayerPoint2d();
	
	/**
	 * the time stamp of the last time {@code saveCurrentLocation()} was called
	 */
	private static long lastSaveTime;
	
	/**
	 * The list of previously-saved locations
	 * @see {@code Trilateration.saveCurrentLocation()} 
	 */
	private static List<PlayerPoint2d> savedLocations = Collections.synchronizedList(new ArrayList<PlayerPoint2d>());
	
	/**
	 * one key for each beacon, the "value" is the most recent distance reading
	 */
	private static Map<String, BeaconReading> beaconData = Collections.synchronizedMap(new HashMap<String, BeaconReading>());
	
	
	/**
	 * Constructor, initializes the robot's starting position.
	 * @param initX robot's initial x coordinate
	 * @param initY robot's initial y coordinate
	 */
	public Multilateration (double initX, double initY) {
	    	
		Multilateration.currentLocation.setPx(initX);
		Multilateration.currentLocation.setPy(initY);
		Multilateration.lastSaveTime = System.currentTimeMillis();
	}
	
	
	public void run() {
		
		while (true) {
			
			// wait until we have readings from three unique beacons
			if (beaconData.size() < 3) 
			{ 
				try {
					beaconData.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} 
			else 
			{

				ArrayList<PlayerPoint2d> intersectionPoints = new ArrayList<PlayerPoint2d>();
				double xCenter1, yCenter1, radius1, xCenter2, yCenter2, radius2, distanceBetweenCenters;
				double xSol1, ySol1, xSol2, ySol2;
				
				// wrap the beaconData map in an ArrayList
				ArrayList<Entry<String, BeaconReading>> beaconDataList = new ArrayList<Entry<String, BeaconReading>>(beaconData.entrySet());
				
				// iterate through each circle in the list
				for (int i = 0; i < beaconDataList.size() - 1; i++) {
					BeaconReading circle1 = beaconDataList.get(i).getValue();
					xCenter1 = circle1.coord.getPx();
					yCenter1 = circle1.coord.getPy();
					radius1 = circle1.distance;
					
					// iterate through every other circle in the list
					for (int j = i + 1; j < beaconDataList.size(); j++) {
						if (i == j) 
							continue; // don't compare a circle with itself
						
						BeaconReading circle2 = beaconDataList.get(i).getValue();
						xCenter2 = circle2.coord.getPx();
						yCenter2 = circle2.coord.getPy();
						radius2 = circle2.distance;
						
						distanceBetweenCenters = Math.sqrt(((xCenter2 - xCenter1) * (xCenter2 - xCenter1)) + ((yCenter2 - yCenter1) * (yCenter2 - yCenter1)));
						
						// TODO use margin of error in this conditional
						if ((radius1 + radius2) == distanceBetweenCenters) // one intersection point (circles are tangent)
						{
							PlayerPoint2d point = new PlayerPoint2d();
							point.setPx((xCenter1*radius1 + xCenter2*radius2) / (radius1 + radius2));
							point.setPy((yCenter1*radius1 + yCenter2*radius2) / (radius1 + radius2));
							
						}
				
						// TODO use margin of error in this conditional
						else if ((radius1 + radius2) > distanceBetweenCenters)  // two intersection points
						{
							double xDiff = xCenter2 - xCenter1;
							double yDiff = yCenter2 - yCenter1; 
							double hValue = (distanceBetweenCenters*distanceBetweenCenters + radius1*radius1 - radius2*radius2) / (2*distanceBetweenCenters); 
							
							xSol1 = xCenter1 + (xDiff*hValue / distanceBetweenCenters) + (yDiff/distanceBetweenCenters) * Math.sqrt(radius1*radius1 - hValue*hValue);
							ySol1 = yCenter1 + (yDiff*hValue / distanceBetweenCenters) - (xDiff/distanceBetweenCenters) * Math.sqrt(radius1*radius1 - hValue*hValue);
							
							xSol2 = xCenter1 + (xDiff*hValue / distanceBetweenCenters) - (yDiff/distanceBetweenCenters) * Math.sqrt(radius1*radius1 - hValue*hValue);
							ySol2 = yCenter1 + (yDiff*hValue / distanceBetweenCenters) + (xDiff/distanceBetweenCenters) * Math.sqrt(radius1*radius1 - hValue*hValue);
							
							PlayerPoint2d point1 = new PlayerPoint2d();
							point1.setPx(xSol1);
							point1.setPy(ySol1);
							intersectionPoints.add(point1);
							
							PlayerPoint2d point2 = new PlayerPoint2d();
							point2.setPx(xSol2);
							point2.setPy(ySol2);
							intersectionPoints.add(point2);
						}
						else // no intersection point
						{
							continue;
						}
					} // end of inner for loop "j"
				} // end of outer for loop "i"
				
				
				// remove points outside of beacon polygon
				intersectionPoints = removeOutsidePoints(intersectionPoints, beaconDataList);
				
				// find minimum point
				currentLocation = findMinimumCluster(intersectionPoints);

			}
			
		} // end of while(true) loop
	}
	
	
	private static PlayerPoint2d findMinimumCluster(ArrayList<PlayerPoint2d> pointList) {
		PlayerPoint2d solution = new PlayerPoint2d();
		double minDistance = Double.MAX_VALUE;
		
		for (int i = 0; i < pointList.size(); i++) {		
			double distanceSum = 0;
			for (int j = 0; j < pointList.size(); j++) {
				if (i != j) // don't calculate the distance from a point to itself
					distanceSum += Math.sqrt(Math.pow(pointList.get(i).getPx() - pointList.get(j).getPx(), 2) + Math.pow(pointList.get(i).getPy() - pointList.get(j).getPy(), 2));
			}
			
			// TODO use margin of error in this conditional
			// could also save a list of points that have equal distanceSums, and then average all those points
			if (distanceSum < minDistance) {
				minDistance = distanceSum;
				solution = pointList.get(i);
			}
		}
			
		return solution;
	}
	
	
	private static ArrayList<PlayerPoint2d> removeOutsidePoints(ArrayList<PlayerPoint2d> intersectionList, ArrayList<Entry<String, BeaconReading>> beaconDataList) {
		Polygon beaconBounds = new Polygon();
		
		// since Polygon can only consist of integer points, used fixed-point integers with a resolution of 1mm (times 1000)
		for (Entry<String, BeaconReading> e : beaconDataList) { // iterate through each beacon location
			int xCoord = (int)(e.getValue().coord.getPx() * 1000);
			int yCoord = (int)(e.getValue().coord.getPy() * 1000);
			
			if (!beaconBounds.contains(xCoord, yCoord))
				beaconBounds.addPoint(xCoord, yCoord); // establish a polygon shape with each beacon as the endpoints
		}
				
		Iterator<PlayerPoint2d> iter = intersectionList.iterator();
		while (iter.hasNext()) {
			PlayerPoint2d curPoint = iter.next();
			if (!beaconBounds.contains((int)(curPoint.getPx()*1000), (int)(curPoint.getPy()*1000))) {
				iter.remove(); // remove points outside of the boundaries
			}
		}
		
		return intersectionList;	
	}
	

	
	public static synchronized void newBeaconReading(String id, long ts, double x, double y, double dist) {
		synchronized(beaconData) {
			beaconData.put(id, new BeaconReading(ts, x, y, dist));
			beaconData.notifyAll();
		}
	}
	
	
	public static synchronized void saveCurrentLocation() {
		synchronized(savedLocations) {
			savedLocations.add(currentLocation);		
			lastSaveTime = System.currentTimeMillis();
		}
	}
	
	
	public static synchronized PlayerPoint2d getCurrentLocation() {
		return currentLocation;
	}
	
	
	public static synchronized PlayerPoint2d getLastSavedLocation() {
		if (!savedLocations.isEmpty())
			return savedLocations.get(savedLocations.size() - 1);
		else {
			PlayerPoint2d zero = new PlayerPoint2d();
			zero.setPx(0); 
			zero.setPy(0);
			return zero;
		}
	}
	
}




class BeaconReading {
	
	long timeStamp;
	PlayerPoint2d coord = new PlayerPoint2d();
	double distance;
	
	public BeaconReading(long ts, double x, double y, double dist) {
		this.timeStamp = ts;
		this.coord.setPx(x);
		this.coord.setPy(y);
		this.distance = dist;
	}
	
}