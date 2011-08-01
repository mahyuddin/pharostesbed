package pharoslabut.behavior;

/**
 * This is the super class of all behaviors supported by the 
 * Pharos Middleware.
 * 
 * @author Chien-Liang Fok
 */
public abstract class Behavior {

	/**
	 * Determines whether the behavior may start executing.
	 * 
	 * @return true if the behavior can start executing.
	 */
	public abstract boolean canStart();
	
	/**
	 * Determines whether the behavior must top executing.
	 * 
	 * @return true if the behavior must stop.
	 */
	public abstract boolean mustStop();
	
	/**
	 * Starts the behavior running.
	 */
	public abstract void start();
	
	/**
	 * Stops the behavior from running.
	 */
	public abstract void stop();
	
	/**
	 * @return A string representation of this class.
	 */
	public String toString() {
		return "Behavior super class";
	}
}
