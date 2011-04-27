package pharoslabut.sensors;

public class CricketData {
	private double version;
	private String cricketID;
	private String spaceID;
	private int distance;
	private int duration;
	private long flightTime;
	private long sysTime;
	private boolean isConnected; // if beacon is in range isConnected is true
	
	public CricketData(double vr, String cid, String spid, int dist, int dur, long ft, long sys) {
		version = vr;
		cricketID = cid;
		spaceID = spid;
		distance = dist;
		duration = dur;
		flightTime = ft;
		sysTime = sys;
		isConnected = true;
	}
	
	public CricketData(double vr, String cid, String spid, long sys) {
		version = vr;
		cricketID = cid;
		spaceID = spid;
		sysTime = sys;
		isConnected = false;
	}

	public double getVersion(){
		return version;
	}
	
	public String getCricketID(){
		return cricketID;
	}
	
	public String getSpaceID(){
		return spaceID;
	}
	
	public int getDistance(){
		return distance;
	}
	
	public int getDuration(){
		return duration;
	}
	
	public long getFlightTime(){
		return flightTime;
	}
	
	public long getSystemTime(){
		return sysTime;
	}
	
	public boolean getConnection(){
		return isConnected;
	}
	
	public String toString() {
		return "CricketData: version=" + version + ", cricketID=" + cricketID + ", spaceID=" + spaceID + ", distance=" + distance + ", duration=" + duration + ", flightTime=" + flightTime + ", systemTime=" + sysTime + ", isConnected=" + isConnected;
	}
}
