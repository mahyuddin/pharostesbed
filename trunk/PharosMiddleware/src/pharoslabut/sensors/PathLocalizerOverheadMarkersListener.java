package pharoslabut.sensors;

/**
 * Defines the methods that must be defined by all listeners to
 * overhead marker events.
 * 
 * @author Chien-Liang Fok
 *
 */
public interface PathLocalizerOverheadMarkersListener {

	/**
	 * This is called whenever a new marker is detected.
	 * 
	 * @param numMarkers the number of markers.
	 * @param distance The distance since the last marker.
	 */
	public void markerEvent(int numMarkers, double distance);
	
}
