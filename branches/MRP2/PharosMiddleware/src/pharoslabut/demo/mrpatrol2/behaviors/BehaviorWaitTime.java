package pharoslabut.demo.mrpatrol2.behaviors;

import pharoslabut.logger.Logger;

/**
 * A behavior that pauses the robot until a certain amount of time 
 * has passed.  The reference time is when the behavior first starts
 * to run.
 * 
 * @author Chien-Liang Fok
 *
 */
public class BehaviorWaitTime extends Behavior {
	
	/**
	 * The amount of time to wait.  The reference time is the start time of the
	 * reference behavior.
	 */
	private long waitTime;
	
	/**
	 * Whether this behavior is done.
	 */
	private boolean isDone = false;
	
	/**
	 * The constructor.
	 * 
	 * @param name The name of the behavior.
	 * @param referenceBehavior The behavior whose start time defines the temporal reference point.
	 * @param waitTime The wait time in milliseconds.
	 */
	public BehaviorWaitTime(String name, long waitTime) {
		super(name);
		this.waitTime = waitTime;
	}

	@Override
	public void run() {
		// Wait until it's time to start.
		
		Logger.logDbg("Pausing for " + waitTime + " milliseconds");
		if (waitTime != 0) {
			synchronized(this) {
				try {
					this.wait(waitTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
					Logger.logWarn("Interrupted while waiting. + " + e.getLocalizedMessage());
				}
			}
		}
		
		isDone = true;
		Logger.log("Behavior " + getName() + " done.");
	}

	@Override
	public boolean isDone() {
		return isDone;
	}
	
	/**
	 * @return A string representation of this class.
	 */
	@Override
	public String toString() {
		return "BehaviorWaitTime " + super.toString() + ", waitTime = " + waitTime + ", isDone = " + isDone;
	}

	@Override
	public void stop() {
		synchronized(this) {
			this.notifyAll();
		}
		isDone = true;
	}
}