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
	
	/**
	 * The constructor.
	 * 
	 * @param timeStamp The time stamp of the divergence measurement.
	 * @param currLoc The current location.
	 * @param idealLoc The ideal location.
	 * @param idealPath The ideal path along which the robot should travel.
	 */
	public SpatialDivergence(long timeStamp, Location currLoc, Location idealLoc, Line idealPath)
			//double actualHeading, double idealHeading) 
	{
		this.timeStamp = timeStamp;
		this.currLoc = currLoc;
		this.idealLoc = idealLoc;
		this.idealPath = idealPath;
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