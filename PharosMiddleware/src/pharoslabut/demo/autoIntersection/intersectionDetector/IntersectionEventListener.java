package pharoslabut.demo.autoIntersection.intersectionDetector;

/**
 * Defines the interface that all LineFollowerEventListeners must
 * implement.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.navigate.LineFollower
 * @see pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionEvent
 */
public interface IntersectionEventListener {

	/**
	 * This is called when an intersection event occurs.
	 * 
	 * @param ie The event.
	 * @param follower is LineFollower object.
	 */
	public void newIntersectionEvent(IntersectionEvent ie);
	
}
