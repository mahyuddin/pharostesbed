package pharoslabut.demo.autoIntersection;

import java.util.Iterator;
//import java.util.Map;
//import java.util.Set;
import java.util.Vector;

/**
 * The lane specifications.  Describes which lane the robot is traveling through.
 * 
 * @author Chien-Liang Fok
 * @author Michael Hanna
 */
public class LaneSpecs implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3753742070260486383L;
	
	private int entryID;
	private int exitID;
	
	/**
	 * This is not an absolute lane direction: North or South or West or East
	 * This is the robot's anticipated heading RELATIVE to its current direction
	 * 'S' = Straight - the robot will continue going Straight
	 * 'R' = Right    - the robot is going to turn right
	 * 'L' = Left     - the robot is going to turn left
	 * '-' = ERROR    - an error will be returned if you supplied wrong data to laneSpecs
	 *                  For example: a robot going North, cannot head South at the intersection
	 */
	private char heading;
	
	/**
	 * This default constructor to be deleted
	 */
	public LaneSpecs() {}

	/**
	 * Constructor 1 of 2
	 * The robot can report its lane specifications by determining both its entry point and its exit point
	 * @param IntersectionSpecs
	 * @param entry
	 * @param exit
	 */
	public LaneSpecs(IntersectionSpecs is, int entryID, int exitID) {
		if( (! is.getMapEntry2Exits().get(entryID).contains(exitID))  || (getHeading(is) == '-') ) {
			System.out.println("FATAL ERROR: The robot cannot go to lane " +exitID+ " from lane " +entryID);
			System.exit(0);
		}
		this.entryID = entryID;
		this.exitID = exitID;
		this.heading = getHeading(is);		// the robot's heading can be determined based on the IntersectionSpecs
	}
	
	/**
	 * Constructor 2 of 2
	 * The robot can report its lane specifications by determining both its entry point and its heading
	 * @param IntersectionSpecs
	 * @param entry
	 * @param heading
	 */
	public LaneSpecs(IntersectionSpecs is, int entryID, char heading) {
		if( (! is.getMapEntry2Exits().get(entryID).contains(getExitID(is)))  ||  (getExitID(is) == -1) ) {
			System.out.println("FATAL ERROR: The robot cannot go to lane " +exitID+ " from lane " +entryID);
			System.exit(0);
		}
		this.entryID = entryID;
		this.heading = heading;
		this.exitID = getExitID(is);			// the robot's exit point can be determined based on the IntersectionSpecs
	}
	
	private char getHeading(IntersectionSpecs is) {
		char entryDirection = is.getMapEntry2Direction().get(this.entryID);
		char exitDirection  = is.getMapExit2Direction().get(this.exitID);
		if( entryDirection == exitDirection )
			return 'S';
		if( (entryDirection=='N' && exitDirection=='E')  ||  (entryDirection=='W' && exitDirection=='N')  ||
			(entryDirection=='S' && exitDirection=='W')  ||  (entryDirection=='E' && exitDirection=='S') )
			return 'R';
		if( (entryDirection=='N' && exitDirection=='W')  ||  (entryDirection=='W' && exitDirection=='S')  ||
			(entryDirection=='S' && exitDirection=='E')  ||  (entryDirection=='E' && exitDirection=='N') )
			return 'L';
		return '-';
	}
	
	private int getExitID(IntersectionSpecs is) {
		char entryDirection = is.getMapEntry2Direction().get(this.entryID);
		Vector<Integer> allowedExits = is.getMapEntry2Exits().get(this.entryID);
		Iterator<Integer> it = allowedExits.iterator();
		while(it.hasNext()) {
			int allowedExit = it.next().intValue();
			char exitDirection  = is.getMapExit2Direction().get(allowedExit);
			if( this.heading == 'S' ) {
				if( entryDirection == exitDirection )
					return allowedExit;
			}
			if( this.heading == 'R' ) {
				if( (entryDirection=='N' && exitDirection=='E')  ||  (entryDirection=='W' && exitDirection=='N')  ||
					(entryDirection=='S' && exitDirection=='W')  ||  (entryDirection=='E' && exitDirection=='S') )
					return allowedExit;
			}
			if( this.heading == 'L' ) {
				if( (entryDirection=='N' && exitDirection=='W')  ||  (entryDirection=='W' && exitDirection=='S')  ||
					(entryDirection=='S' && exitDirection=='E')  ||  (entryDirection=='E' && exitDirection=='N') )
					return allowedExit;
			}
		}
		return -1;
	}
	
	public int getEntryID() {
		return this.entryID;
	}
	
	public int getExitID() {
		return this.exitID;
	}
	
	public void setEntryID(int ent) {
		this.entryID = ent;
	}
	
	public int getHeading() {
		return this.heading;
	}
	
	public void setExitID(int exit) {
		this.exitID = exit;
	}
	
	public void setHeading(char heading) {
		this.heading = heading;
	}
	
	
	public String toString() {
		return "LaneSpecs";
	}

	
}
