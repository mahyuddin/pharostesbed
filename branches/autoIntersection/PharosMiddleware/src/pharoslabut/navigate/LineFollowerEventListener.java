package pharoslabut.navigate;

/**
 * Defines the interface that all LineFollowerEventListeners must
 * implement.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.navigate.LineFollower
 * @see pharoslabut.navigate.LineFollowerEvent
 */
public interface LineFollowerEventListener {

	/**
	 * This is called whenver the line follower detects an event.
	 * 
	 * @param lfe The event.
	 */
	public void newLineFollowerEvent(LineFollowerEvent lfe);
	
}
