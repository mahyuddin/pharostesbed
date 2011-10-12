package pharoslabut.demo.autoIntersection.intersectionDetector;

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
	 * The minimum distance in meters a robot must travel before it can possibly
	 * exit the intersection.
	 */
	public static final double MIN_DIST_TO_EXIT = 1;
	
	/**
	 * The minimum distance between markers.
	 */
	public static final double MIN_DIST_BETWEEN_MARKERS = 0.1;
	
	/**
	 * The current state of this detector.  Assume it starts idle, meaning it is prior to
	 * approaching the intersection.
	 */
	private IntersectionEventType state = IntersectionEventType.IDLE;
	
	/**
	 * The distance since the last accepted marker.
	 */
	private double distSinceLastAcceptedMarker = 0;
	
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
		
		distSinceLastAcceptedMarker += distance;
		
		// Filter out invalid marker events.
		// One way to determine if a marker event is invalid is if the distance traveled is less than the 
		// minimum specified distance.
		if (distSinceLastAcceptedMarker < MIN_DIST_BETWEEN_MARKERS) {
			Logger.logErr("Rejecting marker event because distance is less than the minimum (" + distance + " < " + MIN_DIST_BETWEEN_MARKERS);
			return;
		}
		
		switch(state) {
		case IDLE:
			Logger.log("Vehicle is APPROACHING intersection.");
			distSinceLastAcceptedMarker = 0;
			state = IntersectionEventType.APPROACHING;
			genApproachingEvent();
			break;
		case APPROACHING:
			Logger.log("Vehicle is ENTERING intersection.");
			distSinceLastAcceptedMarker = 0;
			state = IntersectionEventType.ENTERING;
			genEnteringEvent();
			break;
		case ENTERING:
			if (distSinceLastAcceptedMarker > MIN_DIST_TO_EXIT) {
				Logger.log("Vehicle is EXITING intersection.");
				distSinceLastAcceptedMarker = 0;
				state = IntersectionEventType.IDLE;
				genExitingEvent();
			} else 
				Logger.logErr("Detected invalid exiting event, distance = " + distSinceLastAcceptedMarker + ", which is less than " + MIN_DIST_TO_EXIT + "m");
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
