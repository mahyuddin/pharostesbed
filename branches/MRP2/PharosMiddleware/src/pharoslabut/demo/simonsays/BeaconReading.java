package pharoslabut.demo.simonsays;

import playerclient3.structures.PlayerPoint2d;

public class BeaconReading implements java.io.Serializable {

	private static final long serialVersionUID = 5053764047088598082L;

	long timeStamp;
	PlayerPoint2d coord = new PlayerPoint2d();
	double distance;
	
	public BeaconReading() {}
	
	public BeaconReading(long ts, double x, double y, double dist) {
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "At time=" + timeStamp + ", " + distance + " from (" + coord.getPx() + "," + coord.getPy() + ")."; 
	}
	
	
	
}
