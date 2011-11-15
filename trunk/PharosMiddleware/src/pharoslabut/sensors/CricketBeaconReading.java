package pharoslabut.sensors;

import playerclient3.structures.PlayerPoint2d;

public class CricketBeaconReading implements java.io.Serializable {

	private static final long serialVersionUID = 5053764047088598082L;

	long timeStamp;
	PlayerPoint2d coord = new PlayerPoint2d();
	
	/**
	 * distance from Cricket beacon to listener in meters
	 */
	double distance;
	
	public CricketBeaconReading() {}
	
	public CricketBeaconReading(long ts, double x, double y, double dist) {
		this.timeStamp = ts;
		this.coord.setPx(x);
		this.coord.setPy(y);
		this.distance = dist;
	}
	
	
	public long getTimeStamp() {
		return timeStamp;
	}

	public PlayerPoint2d getCoord() {
		return coord;
	}

	public double getDistance() {
		return distance;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "At time=" + timeStamp + ", " + distance + " from (" + coord.getPx() + "," + coord.getPy() + ")."; 
	}
	
	
	
}
