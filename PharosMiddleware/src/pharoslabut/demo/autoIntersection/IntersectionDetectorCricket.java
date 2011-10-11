package pharoslabut.demo.autoIntersection;

import pharoslabut.logger.Logger;
import pharoslabut.sensors.CricketData;
import pharoslabut.sensors.CricketDataListener;
import pharoslabut.sensors.CricketInterface;

/**
 * Uses the Cricket Motes to detect the intersection.
 * 
 * @author Chien-Liang Fok
 */
@Deprecated public class IntersectionDetectorCricket extends IntersectionDetector implements CricketDataListener {

	/**
	 * The distance below which the robot will be considered "at" a particular location.
	 */
	public static final int CRICKET_DISTANCE_THRESHOLD = 40;
	
	/**
	 * The constructor.
	 * 
	 * @param comPortName The serial port to which the cricket is connected.
	 */
	public IntersectionDetectorCricket(String comPortName) { 
		CricketInterface ci  = new CricketInterface(comPortName);
		ci.registerCricketDataListener(this);
	}

	
	@Override
	public void newCricketData(CricketData cd) {
		// full cricket mote specs
		if (cd.getConnection()) {
			
			if (cd.getDistance() < CRICKET_DISTANCE_THRESHOLD) {
				String spaceID = cd.getSpaceID();

//				currentLane.setEntryID(Integer.valueOf(cd.getSpaceID().substring(1))); // the second character is the ID of the lane
				
				if (spaceID.startsWith("A")) {
					genApproachingEvent();
				} 
				
				else if (spaceID.startsWith("E")) {
					genEnteringEvent();
				}
				
				else if (spaceID.startsWith("X")) {
					genExitingEvent();
				}
				
				Logger.logDbg("Name = " + cd.getSpaceID() + ", distance = " + cd.getDistance());
			}
			else {
				Logger.logDbg("Rejecting cricket data because distance " + cd.getDistance() + " > " + CRICKET_DISTANCE_THRESHOLD);
			}
		} 
//		else {
//			Logger.logDbg("Ignoring cricket data because it's not connected.");
//		}
	}

}
