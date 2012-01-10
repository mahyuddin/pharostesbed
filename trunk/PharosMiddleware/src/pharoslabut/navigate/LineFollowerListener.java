package pharoslabut.navigate;

/**
 * Defines the interface for all LineFollower listeners.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.navigate.LineFollower
 */
public interface LineFollowerListener {
	
	/**
	 * This is called when the line follower begins to work again.
	 * That is, the line is found and the robot is moving again.
	 */
	public void lineFollowerWorking();
	
	/**
	 * This is called when an error occurs within the line follower.
	 * Usually, it is called when the line follower fails to find the line.
	 * 
	 * @param errno An indicator describing the error.
	 */
	public void lineFollowerError(LineFollowerError errno);
}
