package pharoslabut.demo.mrpatrol.logAnalyzer;

import java.util.Vector;

import pharoslabut.logger.Logger;
import pharoslabut.navigate.Location;
import pharoslabut.util.AverageStatistic;

/**
 * Maintains data about a waypoint's idleness and times visited.
 * 
 * @author Chien-Liang Fok
 */
public class WaypointIdlenessState {
	Location waypoint;
	Vector<VisitationState> visitationTimes;
	Vector<Long> idleTimes;
	
	/**
	 * The constructor.
	 * 
	 * @param waypoint The waypoint.
	 * @param visitationTimes The times the waypoint was visited.
	 * @param idleTimes The idle times of the waypoint.
	 */
	public WaypointIdlenessState(Location waypoint, Vector<VisitationState> visitationTimes, Vector<Long> idleTimes) {
		this.waypoint = waypoint;
		this.visitationTimes = visitationTimes;
		this.idleTimes = idleTimes;
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
	public Vector<Long> getIdleTimes() {
		return idleTimes;
	}
	
	/**
	 * Merges the visitation times and idle times of this state with another.
	 * 
	 * @param state The state with which to merge.
	 */
	public void mergeWith(WaypointIdlenessState state) {
		if (state.getWaypoint().equals(waypoint)) {
			visitationTimes.addAll(state.getVisitationTimes());
			idleTimes.addAll(state.getIdleTimes());
		} else {
			Logger.logErr("Cannot merge, waypoints do not match!");
		}
	}
	
	/**
	 * 
	 * @return The average idle time.
	 */
	public AverageStatistic getAvgIdleTime() {
		
		// Convert the idle times into a vector of doubles
		Vector<Double> temp = new Vector<Double>();
		for (int i=0; i < idleTimes.size(); i++) {
			temp.add((double)idleTimes.get(i));
		}
		
		// Compute the average and confidence intervals.
		return new AverageStatistic(temp);
	}
}