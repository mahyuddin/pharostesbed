package pharoslabut.demo.mrpatrol2.behaviors;

import java.util.Collection;
import java.util.Iterator;

import pharoslabut.demo.mrpatrol2.context.Teammate;
import pharoslabut.demo.mrpatrol2.context.WorldModel;
import pharoslabut.logger.Logger;

/**
 * Implements tight coordination.  Tight coordination forces the robot to wait until *all* of 
 * its teammates are synchronized.
 * 
 * @author Chien-Liang Fok
 *
 */
public class BehaviorTightCoordination extends Behavior {
	
	/**
	 * The cycle period in milliseconds.
	 */
	public static final long BEHAVIOR_CYCLE_TIME = 1000;
	
	/**
	 * Whether this behavior is done.
	 */
	private boolean isDone = false;
	
	/**
	 * The world model.
	 */
	private WorldModel worldModel;
	
	/**
	 * The number of waypoints the local robot has visited by this time
	 * this behavior is run.  This value is used to determine whether the
	 * teammates are synchronized with this robot.
	 */
	private int numWaypointsVisited;
	
	/**
	 * The constructor.
	 * 
	 * @param name The name of the behavior.
	 * @param worldModel The world model.
	 */
	public BehaviorTightCoordination(String name, WorldModel worldModel, int numWaypointsVisited) {
		super(name);
		this.worldModel = worldModel;
		this.numWaypointsVisited = numWaypointsVisited;
	}

	/**
	 * This is called when the behavior starts to run (i.e., by this time, the start condition 
	 * of the behavior is met).
	 */
	@Override
	public void run() {
		
		while (!isDone) {
			
			Logger.logDbg("Checking whether every teammate is synchronized with me (numWaypointsVisited = " + numWaypointsVisited + ")");
			Collection<Teammate> teammates = worldModel.getTeammates();
			Iterator<Teammate> i = teammates.iterator();
			
			boolean teamSynced = true;
			
			while (i.hasNext()) {
				Teammate teammate = i.next();
				if (teammate.getNumWaypointsVisited() < numWaypointsVisited) {
					Logger.logDbg("Team not synched, teammate " + teammate.getName() + " only visited " + teammate.getNumWaypointsVisited() + " waypoints, expecting " + numWaypointsVisited);
					teamSynced = false;
					break;
				}
			}
			
			if (teamSynced) {
				Logger.logDbg("Team is synchronized!");
				isDone = true;
			}
			
			synchronized(this) {
				try {
					this.wait(BEHAVIOR_CYCLE_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
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
		return "BehaviorTightCoordination " + super.toString() 
			+ ", numWaypointsVisited = " + numWaypointsVisited 
			+ ", worldModel = " + worldModel;
	}

}
