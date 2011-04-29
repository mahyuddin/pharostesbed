package pharoslabut.demo.autoIntersection;

import pharoslabut.sensors.*;

public class LaneIdentifier implements CricketDataListener{

	private LaneSpecs currentLane = new LaneSpecs();
	
	public LaneIdentifier(String comPortName) { 
		CricketInterface ci  = new CricketInterface(comPortName);
		ci.registerCricketDataListener(this);
	}
	
	/**
	 * @return The lane that the robot is currently in.
	 */
	public LaneSpecs getCurrentLane() {
		return currentLane;
	}
	
	@Override
	public void newCricketData(CricketData cd) {
		// full cricket mote specs
		if (cd.getConnection()) {
			if(cd.getDistance() < 40) {
				currentLane.setEntryID(Integer.valueOf(cd.getSpaceID().substring(1)));
			}
		}
	}

}
