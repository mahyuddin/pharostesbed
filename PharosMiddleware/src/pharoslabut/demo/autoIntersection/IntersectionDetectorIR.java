package pharoslabut.demo.autoIntersection;

import pharoslabut.logger.Logger;
import pharoslabut.sensors.PathLocalizerOverheadMarkers;
import pharoslabut.sensors.PathLocalizerOverheadMarkersListener;

/**
 * Detects the intersection using an upward-facing IR sensor.
 * 
 * @author Chien-Liang Fok
 */
public class IntersectionDetectorIR extends IntersectionDetector implements PathLocalizerOverheadMarkersListener {
	
	/**
	 * The current state of this detector.  Assume it starts idle, meaning it is prior to
	 * approaching the intersection.
	 */
	private IntersectionEventType state = IntersectionEventType.IDLE;
	
	/**
	 * The source of range information.
	 */
//	private PathLocalizerOverheadMarkers markerDetector;
	
	/**
	 * The constructor.
	 * 
	 * @param markerDetector The detector of overhead markers.
	 */
	public IntersectionDetectorIR(PathLocalizerOverheadMarkers markerDetector) {
//		this.markerDetector = markerDetector;
		Logger.log("Registering self as listener to overhead marker events...");
		markerDetector.addListener(this);
	}

	/**
	 * This is called whenever new marker event occurs.
	 */
	@Override
	public void markerEvent(int numMarkers, double distance) {
		
		// TODO: Consider distance when determining whether this is a valid event.
		
		switch(state) {
		case IDLE:
			Logger.log("Vehicle is APPROACHING intersection.");
			state = IntersectionEventType.APPROACHING;
			genApproachingEvent();
			break;
		case APPROACHING:
			Logger.log("Vehicle is ENTERING intersection.");
			state = IntersectionEventType.ENTERING;
			genEnteringEvent();
			break;
		case ENTERING:
			Logger.log("Vehicle is EXITING intersection.");
			state = IntersectionEventType.IDLE;
			genExitingEvent();
			break;
		default: 
			Logger.logErr("Unknown state: " + state);
			System.exit(1);
		}
		
	}

//	@Override
//	public void newOpaqueData(ProteusOpaqueData opaqueData) {
//		String msg = new String(opaqueData.getData());
//		if (msg.contains(INTERSECTION_EVENT_KEY)) {
//			String type = msg.substring(INTERSECTION_EVENT_KEY.length()+1);
//			Logger.log("Intersection Event: \"" + msg + "\", type = \"" + type + "\"");
//			
//			if (type.equals(APPROACH_KEY)) {
//				Logger.log("The robot is approaching the intersection!");
//				genApproachingEvent();
//			} else if (type.equals(ENTRY_KEY)) {
//				Logger.log("The robot is entering the intersection!");
//				genEnteringEvent();
//			} else if (type.equals(EXIT_KEY)) {
//				Logger.log("The robot is exiting the intersection!");
//				genExitingEvent();
//			} else {
//				Logger.logErr("Unknown intersection event type: \"" + type + "\"");
//			}
//		}
//	}
}
