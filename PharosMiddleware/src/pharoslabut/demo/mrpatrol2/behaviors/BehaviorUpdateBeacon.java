package pharoslabut.demo.mrpatrol2.behaviors;

import java.util.Enumeration;
import java.util.Vector;

import pharoslabut.logger.Logger;

/**
 * Updates the beacon when it runs.
 * 
 * @author Chien-Liang Fok
 */
public class BehaviorUpdateBeacon extends Behavior {

	private Vector<UpdateBeaconBehavior> setToUpdate = new Vector<UpdateBeaconBehavior>();
	
	private boolean isDone = false;
	
	private int numWaypointsTraversed;
	
	/**
	 * The constructor.
	 * 
	 * @param name The name of the behavior
	 * @param numWaypointsTraversed The number of waypoints the robot has traversed by the
	 * time this behavior executes.
	 */
	public BehaviorUpdateBeacon(String name, int numWaypointsTraversed) {
		super(name);
		this.numWaypointsTraversed = numWaypointsTraversed;
	}
	
	public void addBehaviorToUpdate(UpdateBeaconBehavior ubb) {
		this.setToUpdate.add(ubb);
	}

	@Override
	public void run() {
		if (!isDone) {
			Logger.logDbg("Setting numWaypointsTraversed in beacon to be " + numWaypointsTraversed);
			Enumeration<UpdateBeaconBehavior> e = setToUpdate.elements();
			while (e.hasMoreElements()) {
				e.nextElement().setWaypointsTraversed(numWaypointsTraversed);
			}
			isDone = true;
		}
	}

	@Override
	public boolean isDone() {
		return isDone;
	}

	@Override
	public void stop() {
		isDone = true;
	}

	/**
	 * @return A string representation of this class.
	 */
	@Override
	public String toString() {
		return "BehaviorUpdateBeacon " + super.toString() + ", numWaypointsTraversed = " + numWaypointsTraversed;
	}
}
