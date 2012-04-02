package pharoslabut.demo.mrpatrol2.behaviors;

import java.util.Enumeration;

//import pharoslabut.demo.mrpatrol2.behaviors.Behavior.CanStart;
//import pharoslabut.logger.Logger;

public class BehaviorAnticipatedUpdateBeacon extends BehaviorUpdateBeacon {

	/**
	 * The time prior to reaching the next waypoint when we should
	 * start coordinating with teammates.
	 */
	private long aheadTime;
	
	/**
	 * The go to location behavior that should be running when this behavior
	 * starts to run.  It informs this behavior of how much longer it will
	 * take before reaching the next waypoint.  This behavior uses this
	 * information to decide whether it's time to start.
	 */
	private BehaviorGoToLocation prevGoToLoc;
	
	/**
	 * 
	 * @param name
	 * @param numWaypointsTraversed
	 * @param prevGoToLoc The previous go to location behavior.
	 * @param aheadTime the time before the prevGoToLoc behavior finishes when
	 * this behavior should start.
	 */
	public BehaviorAnticipatedUpdateBeacon(String name,
			int numWaypointsTraversed,
			BehaviorGoToLocation prevGoToLoc, long aheadTime) 
	{
		super(name, numWaypointsTraversed);
		this.prevGoToLoc = prevGoToLoc;
		this.aheadTime = aheadTime;
	}
	
	public void setAheadTime(long aheadTime) {
		this.aheadTime = aheadTime;
	}
	
	public long getAheadTime() {
		return aheadTime;
	}
	
	@Override
	public CanStart canStart() {
		if (isDone())
			return new CanStart(false, "already done");
		
		if (started)
			return new CanStart(false, "currently running");
		
		Enumeration<Behavior> e = prerequisites.elements();
		while (e.hasMoreElements()) {
			Behavior b = e.nextElement();
			if (!b.isDone())
				return new CanStart(false, "prerequisite " + b.getName() + " not done.");
		}
		
//		if (!currentlyRunningRequirementMet())
//			return new CanStart(false, "a behavior that must be running concurrently is not yet running");
		
		// check if the time till reaching the next waypoint is < threshold
		if (prevGoToLoc.areWeThereYet(aheadTime)) {
			return new CanStart(true, "time to start coordinating.");
		} else
			return new CanStart(false, "Not time to start coordinating yet.");
	}

	/**
	 * @return A string representation of this class.
	 */
	@Override
	public String toString() {
		return "BehaviorAnticipatedUpdateBeacon " + super.toString() + ", numWaypointsTraversed = " + numWaypointsTraversed;
	}
}
