package pharoslabut.demo.autoIntersection;

import pharoslabut.logger.Logger;
import pharoslabut.sensors.ProteusOpaqueData;
import pharoslabut.sensors.ProteusOpaqueInterface;
import pharoslabut.sensors.ProteusOpaqueListener;

/**
 * Detects the intersection using the IR sensor.  This data is delivered
 * through the Opaque interface.
 * 
 * @author Chien-Liang Fok
 *
 */
public class IntersectionDetectorIR extends IntersectionDetector implements ProteusOpaqueListener {

	public static final String INTERSECTION_EVENT_KEY = "IE";
	public static final String APPROACH_KEY = "A";
	public static final String ENTRY_KEY = "E";
	public static final String EXIT_KEY = "X";
	
//	private ProteusOpaqueInterface poi;
	
	public IntersectionDetectorIR(ProteusOpaqueInterface poi) {
//		this.poi = poi;
		Logger.log("Connecting to ProteusOpaqueInterface...");
		
		
		Logger.log("Registering self as listener to Opaque data...");
//		while (poi == null) {
//			Logger.logErr("opaque interface is null.");
//			synchronized(this) {
//				try {
//					wait(1000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		}
		poi.addOpaqueListener(this);
	}

	@Override
	public void newOpaqueData(ProteusOpaqueData opaqueData) {
		String msg = new String(opaqueData.getData());
		if (msg.contains(INTERSECTION_EVENT_KEY)) {
			String type = msg.substring(INTERSECTION_EVENT_KEY.length()+1);
			Logger.log("Intersection Event: \"" + msg + "\", type = \"" + type + "\"");
			
			if (type.equals(APPROACH_KEY)) {
				Logger.log("The robot is approaching the intersection!");
				genApproachingEvent();
			} else if (type.equals(ENTRY_KEY)) {
				Logger.log("The robot is entering the intersection!");
				genEnteringEvent();
			} else if (type.equals(EXIT_KEY)) {
				Logger.log("The robot is exiting the intersection!");
				genExitingEvent();
			} else {
				Logger.logErr("Unknown intersection event type: \"" + type + "\"");
			}
		}
	}
}
