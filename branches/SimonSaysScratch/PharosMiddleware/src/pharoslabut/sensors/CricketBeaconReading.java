package pharoslabut.sensors;

import playerclient3.structures.PlayerPoint2d;

public class CricketBeaconReading implements java.io.Serializable {

	private static final long serialVersionUID = 5053764047088598082L;

	long timeStamp;
	PlayerPoint2d coord;
	
	/**
	 * the actual distance in 3D space from beacon to listener
	 */
	double distance;
	
	/**
	 * distance along the ground from Cricket beacon to listener in meters
	 */
	double distanceAlongGround;
	
	
	public CricketBeaconReading() {}
	
	public CricketBeaconReading(long ts, double x, double y, double z, double dist) {
		this.timeStamp = ts;
		this.coord = new PlayerPoint2d(x, y);
		this.distance = dist;
		this.distanceAlongGround = Math.sqrt(Math.abs(dist * dist - z * z));
	}
	
	
	public long getTimeStamp() {
		return timeStamp;
	}

	public PlayerPoint2d getCoord() {
		return coord;
	}

	/**
	 * @return distance along the ground from Cricket beacon to listener in <b> **meters** </b>
	 */
	public double getDistance2dComponent() {
		return distanceAlongGround;
	}
	
	/**
	 * @return the actual distance in 3D space from beacon to listener
	 */
	public double getDistance3d() {
		return distance;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "At time=" + timeStamp + ", " + distanceAlongGround + " from (" + coord.getPx() + "," + coord.getPy() + ")."; 
	}
	
	
	
}
