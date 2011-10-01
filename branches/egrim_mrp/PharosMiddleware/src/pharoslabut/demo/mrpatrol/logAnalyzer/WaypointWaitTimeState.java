package pharoslabut.demo.mrpatrol.logAnalyzer;

import java.util.Vector;

import pharoslabut.logger.Logger;
import pharoslabut.navigate.Location;
import pharoslabut.util.AverageStatistic;

/**
 * Maintains data about a waypoint's wait times and times visited.
 * 
 * @author Chien-Liang Fok
 */
public class WaypointWaitTimeState {
	Location waypoint;
	Vector<VisitationState> visitationTimes;
	Vector<Long> waitTimes;
	
	public WaypointWaitTimeState(Location waypoint, Vector<VisitationState> visitationTimes, Vector<Long> waitTimes) {
		this.waypoint = waypoint;
		this.visitationTimes = visitationTimes;
		this.waitTimes = waitTimes;
	}
	
	/**
	 * 
	 * @return The waypoint.
	 */
	public Location getWaypoint() {
		return waypoint;
	}
	
	/**
	 * 
	 * @return The times this waypoint was visited.
	 */
	public Vector<VisitationState> getVisitationTimes() {
		return visitationTimes;
	}
	
	/**
	 * 
	 * @return The times this waypoint was idle.
	 */
	public Vector<Long> getWaitTimes() {
		return waitTimes;
	}
	
	/**
	 * Merges the visitation times and idle times of this state with another.
	 * 
	 * @param state The state with which to merge.
	 */
	public void mergeWith(WaypointWaitTimeState state) {
		if (state.getWaypoint().equals(waypoint)) {
			visitationTimes.addAll(state.getVisitationTimes());
			waitTimes.addAll(state.getWaitTimes());
		} else {
			Logger.logErr("Cannot merge, waypoints do not match!");
		}
	}
	/**
	 * 
	 * @return The average idle time.
	 */
	public AverageStatistic getAvgWaitTime() {
		
		// Convert the wait times into a vector of doubles
		Vector<Double> temp = new Vector<Double>();
		for (int i=0; i < waitTimes.size(); i++) {
			temp.add((double)waitTimes.get(i));
		}
		
		// Compute the average and confidence intervals.
		return new AverageStatistic(temp);
	}
}
