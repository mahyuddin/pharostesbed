package pharoslabut.demo.autoIntersection;

import pharoslabut.sensors.*;

public class LaneIdentifier implements CricketDataListener{

	public LaneIdentifier() {
		CricketInterface ci  = new CricketInterface("/dev/ttyUSB0");
		ci.registerCricketDataListener(this);
	}
	
	/**
	 * @return The lane that the robot is currently in.
	 */
	public LaneSpecs getCurrentLane() {
		return null;
	}
	
	@Override
	public void newCricketData(CricketData cd) {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) {
		new LaneIdentifier();
	}
}
