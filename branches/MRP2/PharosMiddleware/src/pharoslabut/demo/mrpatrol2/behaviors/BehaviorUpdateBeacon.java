package pharoslabut.demo.mrpatrol2.behaviors;

import pharoslabut.logger.Logger;

/**
 * Updates when it runs.
 * 
 * @author Chien-Liang Fok
 */
public class BehaviorUpdateBeacon extends Behavior {

	private BehaviorBeacon bb;
	
	private boolean isDone = false;
	
	private int numWaypointsTraversed;
	
	/**
	 * The constructor.
	 * 
	 * @param name The name of the behavior
	 * @param bb The behavior to update.
	 * @param numWaypointsTraversed The number of waypoints the robot has traversed by the
	 * time this behavior executes.
	 */
	public BehaviorUpdateBeacon(String name, BehaviorBeacon bb, int numWaypointsTraversed) {
		super(name);
		this.bb = bb;
		this.numWaypointsTraversed = numWaypointsTraversed;
	}

	@Override
	public void run() {
		if (!isDone) {
			Logger.logDbg("Setting numWaypointsTraversed in beacon to be " + numWaypointsTraversed);
			bb.setWaypointsTraversed(numWaypointsTraversed);
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
