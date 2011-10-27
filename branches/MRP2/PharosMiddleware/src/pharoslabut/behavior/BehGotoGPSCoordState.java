package pharoslabut.behavior;

import pharoslabut.navigate.Location;

/**
 * The behavior state for BehGotoGPSCoord behaviors.
 * 
 * @author Chien-Liang Fok
 * @see BehGotoGPSCoord
 */
public class BehGotoGPSCoordState extends BehaviorState {
	private Location dest;
	
	/**
	 * The constructor.
	 * 
	 * @param dest The destination location.
	 */
	public BehGotoGPSCoordState(Location dest) {
		this.dest = dest;
	}
	
	/**
	 * 
	 * @return The destination location.
	 */
	public Location getDest() {
		return dest;
	}
	
	public static String getBehaviorName() {
		return pharoslabut.behavior.BehGotoGPSCoord.class.getName();
	}
	
	public String toString() {
		String result = getClass().getName() + ", " + super.toString() + ", dest = " + dest;
		return result;
	}
}
