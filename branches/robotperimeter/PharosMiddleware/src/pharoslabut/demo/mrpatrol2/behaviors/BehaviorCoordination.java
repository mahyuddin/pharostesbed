package pharoslabut.demo.mrpatrol2.behaviors;

import java.util.Collection;
import java.util.Iterator;

import pharoslabut.demo.mrpatrol2.config.CoordinationStrength;
import pharoslabut.demo.mrpatrol2.context.Teammate;
import pharoslabut.demo.mrpatrol2.context.WorldModel;
import pharoslabut.logger.Logger;

/**
 * Implements multi-robot coordination for the multi-robot patrol 2 (MRP2) application.
 * It simply waits until all of the (relevant) teammates have become synchronized and then terminates.
 * 
 * There are two types of coordination: loose and tight.  Loose coordination only
 * requires coordinating with robots within range.  Tight coordination requires
 * coordination with *all* robots in the team, not just those in direct range.
 * 
 * In addition, coordination may involve transmitting two types of beacons:
 *   - personal information only: beacons only contain information about the transmitter
 *   - team view information: beacons contain information about the transmitter and
 *     the transitter's world view.
 *   
 * @author Chien-Liang Fok
 */
public class BehaviorCoordination extends Behavior {
	
	/**
	 * The cycle period in milliseconds.
	 */
	public static final long BEHAVIOR_CYCLE_TIME = 1000;
	
	/**
	 * The number of milliseconds that can pass since last hearing 
	 * from a teammate before considering the teammate inactive.
	 */
	public static final long TEAMMATE_ACTIVE_THRESHOLD = 5000;
	
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
	 * Whether to use tight coordination (i.e., coordinate with every teammate)
	 */
	private CoordinationStrength coordStrength;
	
	/**
	 * The latency of coordination.  This is only valid after this
	 * behavior is done.
	 */
	private long latency;
	
	/**
	 * The constructor.
	 * 
	 * @param name The name of the behavior.
	 * @param worldModel The world model.
	 * @param numWaypointsVisited The number of waypoints the local robot has visited
	 * by the time this behavior runs.
	 * @param coordStrength The type of coordination.
	 */
	public BehaviorCoordination(String name, WorldModel worldModel, int numWaypointsVisited, 
			CoordinationStrength coordStrength) 
	{
		super(name);
		this.worldModel = worldModel;
		this.numWaypointsVisited = numWaypointsVisited;
		this.coordStrength = coordStrength;
	}

	/**
	 * This is called when the behavior starts to run (i.e., by this time, the start condition 
	 * of the behavior is met).
	 */
	@Override
	public void run() {
		latency = System.currentTimeMillis();
		while (!isDone) {
			
			StringBuffer sb = new StringBuffer("Checking if team is synchronized (" + numWaypointsVisited + " waypoints)");
			Collection<Teammate> teammates = worldModel.getTeammates();
			Iterator<Teammate> i = teammates.iterator();
			
			boolean teamSynced = true;
			
			while (i.hasNext()) {
				Teammate teammate = i.next();
				
				// If we're using loose coordination, only check the teammate if it is active. 
				boolean checkTeammate = true;
				if (coordStrength == CoordinationStrength.LOOSE) {
					if (!teammate.isActive(TEAMMATE_ACTIVE_THRESHOLD))
						checkTeammate = false;
				}
				
				if (checkTeammate) {
					if (teammate.getNumWaypointsVisited() < numWaypointsVisited) {
						sb.append("\n\tTeammate " + teammate.getName() + " NOT synched (" + teammate.getNumWaypointsVisited() + " waypoints)");
						teamSynced = false;
					} else {
						sb.append("\n\t Teammate " + teammate.getName() + " synched (" + teammate.getNumWaypointsVisited() + " waypoints)");
					}
				} else {
					sb.append("\n\t Teammate " + teammate.getName() + " is inactive");
				}
			}
			
			if (teamSynced) {
				sb.append("\nTeam synchronized!");
				isDone = true;
			} else {
				sb.append("\nTeam NOT synchronized!");
			}
			
			Logger.logDbg(sb.toString());
			
			synchronized(this) {
				try {
					this.wait(BEHAVIOR_CYCLE_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		latency = System.currentTimeMillis() - latency;
	}
	
	/**
	 * 
	 * @return The amount of time (in milliseconds) for the coordination process 
	 * to complete.  THIS IS ONLY VALID AFTER THE BEHAVIOR IS DONE.
	 */
	public long getLatency() {
		return latency;
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
		return "BehaviorCoordination " + super.toString() 
			+ ", numWaypointsVisited = " + numWaypointsVisited 
			+ ", worldModel = " + worldModel 
			+ ", coordStrength = " + coordStrength;
	}

}
