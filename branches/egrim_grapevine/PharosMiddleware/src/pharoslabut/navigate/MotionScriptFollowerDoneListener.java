package pharoslabut.navigate;

/**
 * This is implemented by components that want to know when the 
 * robot is done navigating the waypoints within a motion script.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.io.StopExpMsg
 */
public interface MotionScriptFollowerDoneListener {
	
	/**
	 * This is called when the WayPointFoller finishes execution.
	 * 
	 * @param success True if it successfully finished following the waypoints.
	 * @param finalIntructionIndx The final instruction within the motion script
	 * that was executed.  If successful, this should be equal to the number of 
	 * instructions in the motion script.
	 * @param continueRunning Whether to continue to run until a StopExpMsg is received.
	 */
	public void motionScriptDone(boolean success, int finalIntructionIndx, boolean continueRunning);
}
