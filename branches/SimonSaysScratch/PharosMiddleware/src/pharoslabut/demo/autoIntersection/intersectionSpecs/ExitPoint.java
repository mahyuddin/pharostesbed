package pharoslabut.demo.autoIntersection.intersectionSpecs;

import pharoslabut.navigate.Location;

/**
 * Defines an entry point into an intersection.
 * 
 * @author Chien-Liang Fok
 */
public class ExitPoint extends EntryPoint {

	/**
	 * The constructor.
	 * 
	 * @param id The ID of the entry point.  Typically, this is "E[x]" where [x] is an integer that
	 * enumerates the entry points.
	 * @param heading
	 * @param location
	 */
	public ExitPoint(String id, double heading, Location location) {
		super(id, heading, location);
	}	
}
