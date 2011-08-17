package pharoslabut.logger.analyzer.motion;

import pharoslabut.logger.analyzer.Line;
import pharoslabut.navigate.Location;

/**
 * A spatial divergence measurement.
 *
 * @author Chien-Liang Fok
 */
public class SpatialDivergence {
	/**
	 * The time stamp of the divergence measurement.
	 */
	private long timeStamp;
	
	/**
	 * The ideal path along which the robot should traverse.
	 */
	private Line idealPath;
	
	/**
	 * The actual location of the robot.
	 */
	private Location currLoc;
	
	/**
	 * The ideal location that the robot should be at.  This depends on the 
	 * type of divergence being calculated.
	 */
	private Location idealLoc;
	
//	/**
//	 * The actual heading of the robot.
//	 */
//	private double actualHeading;
//	
//	/**
//	 * The ideal heading for the robot to point to the next waypoint.
//	 */
//	private double idealHeading;
	
	/**
	 * The constructor.
	 * 
	 * @param timeStamp The time stamp of the divergence measurement.
	 * @param currLoc The current location.
	 * @param idealLoc The ideal location.
	 * @param idealPath The ideal path along which the robot should travel.
//	 * @param actualHeading The actual heading of the robot.
//	 * @param idealHeading The ideal heading for the robot to point to the next waypoint.
	 */
	public SpatialDivergence(long timeStamp, Location currLoc, Location idealLoc, Line idealPath)
			//double actualHeading, double idealHeading) 
	{
		this.timeStamp = timeStamp;
		this.currLoc = currLoc;
		this.idealLoc = idealLoc;
		this.idealPath = idealPath;
//		this.actualHeading = actualHeading;
//		this.idealHeading = idealHeading;
	}
	
	/**
	 * @return The time stamp.
	 */
	public long getTimeStamp() {
		return timeStamp;
	}
	
	/**
	 * @return The ideal edge.
	 */
	public Line getIdealPath() {
		return idealPath;
	}
	
	/**
	 * @return The actual location of the robot.
	 */
	public Location getCurrLoc() {
		return currLoc;
	}
	
	/**
	 * @return The ideal location that the robot should be at.  This depends on the 
	 * type of divergence being calculated.
	 */
	public Location getIdealLoc() {
		return idealLoc;
	}
	
//	/**
//	 * 
//	 * @return The ideal heading for the robot to point to the next waypoint.
//	 */
//	public double getIdealHeading() {
//		return idealHeading;
//	}
//	
//	/**
//	 * 
//	 * @return The actual heading of the robot.
//	 */
//	public double getActualHeading() {
//		return actualHeading;
//	}
//	
//	/**
//	 * Returns the heading error.  A negative heading error means the robot must turn 
//	 * right.  A positive heading error means the robot must turn left.
//	 *   
//	 * @return The heading error.
//	 */
//	public double getHeadingError() {
//		return pharoslabut.navigate.Navigate.headingError(actualHeading, idealHeading);
//	}
//	
	/**
	 * @return The absolute value of the divergence.
	 */
	public double getDivergence() {
		return getDivergence(true);
	}
	
	/**
	 * 
	 * @param noheading Whether to ignore the heading when determining the sign of the divergence.
	 * @return The divergence.
	 */
	public double getDivergence(boolean absolute) {
		double divergence = currLoc.distanceTo(idealLoc);
		if (!absolute && Line.isLocationLeftOf(getCurrLoc(), getIdealPath()))
			divergence *= -1;
		return divergence;
	}
	
	public String toString() {
		return "SpatialDivergence: " + timeStamp + "\t" + currLoc + "\t" + idealLoc 
			+ "\t" + getDivergence(); // + "\t" + getIdealHeading() + "\t" + getActualHeading();
	}
}