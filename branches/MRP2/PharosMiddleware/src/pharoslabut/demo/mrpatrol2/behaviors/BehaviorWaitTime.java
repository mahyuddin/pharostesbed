package pharoslabut.demo.mrpatrol2.behaviors;

import pharoslabut.logger.Logger;

/**
 * A behavior that pauses the robot until a certain amount of time 
 * has passed.  The reference time is the start time of the reference
 * behavior.
 * 
 * @author Chien-Liang Fok
 *
 */
public class BehaviorWaitTime extends Behavior {

	/**
	 * The reference behavior.
	 */
	private Behavior referenceBehavior;
	
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
	public BehaviorWaitTime(String name, Behavior referenceBehavior, long waitTime) {
		super(name);
		this.referenceBehavior = referenceBehavior;
		this.waitTime = waitTime;
		addPrerequisite(referenceBehavior);  // This behavior cannot start until the reference behavior is done.
	}

	@Override
	public void run() {
		// Wait until it's time to start.
		long currTime = System.currentTimeMillis();
		long deltaTime = currTime - referenceBehavior.getStartTime();
		if (deltaTime < waitTime) {
			long remainingWaitTime = waitTime - deltaTime;
			Logger.logDbg("Pausing for " + remainingWaitTime + " milliseconds, deltaTime = " + deltaTime + ", waitTime = " + waitTime);
			synchronized(this) {
				try {
					this.wait(remainingWaitTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else {
			Logger.logDbg("No need to wait!  waitTime = " + waitTime + ", deltaTime = " + deltaTime);
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
		return "BehaviorWaitTime " + super.toString() + ", waitTime = " + waitTime + ", isDone = " + isDone + ", referenceBehavior = " + referenceBehavior.getName() + ", referenceStartTime = " + referenceBehavior.getStartTime();
	}

}
