package pharoslabut.demo.autoIntersection;

import pharoslabut.demo.autoIntersection.intersectionSpecs.EntryPoint;
import pharoslabut.logger.Logger;
import pharoslabut.sensors.*;

/**
 * Receives data from the cricket motes and extract lane identification information from these
 * beacons.
 * 
 * @author Chien-Liang Fok
 *
 */
public class LaneIdentifier implements CricketDataListener{

	/**
	 * The entry point ID.
	 */
	private String entryPointID;
	
	/**
	 * The constructor.
	 * 
	 * @param comPortName The comm port to which the cricket is attached.
	 */
	public LaneIdentifier(String comPortName) { 
		CricketInterface ci  = new CricketInterface(comPortName);
		ci.registerCricketDataListener(this);
	}
	
	/**
	 * @return The lane that the robot is currently in.
	 */
	public String getEntryPointID() {
		return entryPointID;
	}
	
	@Override
	public void newCricketData(CricketData cd) {
		// full cricket mote specs
		if (cd.getConnection()) {
			System.out.println("newCricketData: cricket mote distance is " + cd.getDistance());
			if(cd.getDistance() < 40) {
				entryPointID = cd.getSpaceID().substring(1);
				Logger.logDbg("newCricketData: cricket mote " + cd.getSpaceID() + " seen!");
			}
		}
	}
}
