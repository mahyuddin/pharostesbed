package pharoslabut.navigate;

import java.util.Vector;

/**
 * A GPSMotionScript consists of a list of <GPS location, speed, pause time> tuples.
 * The pause time is the time the robot waits at a particular way point before moving
 * to the next way point.
 * 
 * @author Chien-Liang Fok
 */
public class GPSMotionScript implements java.io.Serializable {
	private static final long serialVersionUID = -4244680032090078020L;
	
	private Vector<WayPoint> wayPoints;
	
	
	public GPSMotionScript() {
		wayPoints = new Vector<WayPoint>();
	}
	
	public double getSpeed(int indx) {
		return wayPoints.get(indx).getSpeed();
	}
	
	/**
	 * Adds a way point to this motion script.
	 * 
	 * @param loc The destination location.
	 * @param pauseTime The time to pause at the specified location in milliseconds.
	 * @param speed The speed at which to move towards the way point.
	 */
	public void addWayPoint(Location loc, long pauseTime, double speed) {
		wayPoints.add(new WayPoint(loc, pauseTime, speed));
	}
	
	/**
	 * @return  the number of way points in this motion script.
	 */
	public int numWayPoints() {
		return wayPoints.size();
	}
	
	/**
	 * @param indx Must be between 0 and numWayPoints()-1;
	 * @return The waypoint at the specified index.
	 */
	public Location getWayPoint(int indx) {
		return wayPoints.get(indx).getLoc();
	}
	
	/**
	 * @param indx Must be between 0 and numWayPoints()-1;
	 * @return The pause time once the waypoint at the specified index is reached.
	 */
	public long getPauseTime(int indx) {
		return wayPoints.get(indx).getPauseTime();
	}
	
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("Number of Way Points: " + numWayPoints() + "\n");
		for (int i=0; i < numWayPoints(); i++) {
			buff.append((i+1) + ": " + wayPoints.get(i) + "\n");
		}
		return buff.toString();
	}
	
	private class WayPoint implements java.io.Serializable {
		private static final long serialVersionUID = -3971031374160666416L;
		private Location loc;
		private long pauseTime;
		private double speed; // speed at which to go towards the way point
		
		public WayPoint(Location loc, long pauseTime, double speed) {
			this.loc = loc;
			this.pauseTime = pauseTime;
			this.speed = speed;
		}
		
		public Location getLoc() {
			return loc;
		}
		
		public long getPauseTime() {
			return pauseTime;
		}
		
		public double getSpeed() {
			return speed;
		}
		
		public String toString() {
			return loc + ", speed=" + speed + ", pauseTime=" + pauseTime;
		}
	}
}
