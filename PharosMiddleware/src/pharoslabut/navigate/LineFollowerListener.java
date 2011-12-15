package pharoslabut.navigate;

/**
 * Defines the interface for all LineFollower listeners.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.navigate.LineFollower
 */
public interface LineFollowerListener {
	
	/**
	 * This is called whenever an error occurs within the line follower.
	 * 
	 * @param errno An indicator describing the error.
	 */
	public void lineFollowerError(LineFollowerError errno);
}
