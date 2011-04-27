package pharoslabut.demo.autoIntersection;

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
	
	private int entry;
	private int exit;
	private char heading;
	
	/**
	 * This default constructor to be deleted
	 */
	public LaneSpecs() {}

	/**
	 * Constructor 1 of 2
	 * The robot can report its lane specifications by determining both its entry point and its exit point
	 * @param entry
	 * @param exit
	 */
	public LaneSpecs(int entry, int exit) {
		this.entry = entry;
		this.exit = exit;
		this.heading = '-';		// the heading can be determined later on based on the IntersectionSpecs
	}
	
	/**
	 * Constructor 2 of 2
	 * The robot can report its lane specifications by determining both its entry point and its heading
	 * @param entry
	 * @param heading
	 */
	public LaneSpecs(int entry, char heading) {
		this.entry = entry;
		this.heading = heading;
		this.exit = -1;			// the exit point can be determined later on based on the IntersectionSpecs
	}
	
	public int getEntry() {
		return this.entry;
	}
	
	public int getExit() {
		return this.exit;
	}
	
	public int getHeading() {
		return this.heading;
	}
	
	public void setExit(int exit) {
		this.exit = exit;
	}
	
	public void setHeading(char heading) {
		this.heading = heading;
	}
	
	
	public String toString() {
		return "LaneSpecs";
	}
	
}
