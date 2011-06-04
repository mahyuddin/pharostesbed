package pharoslabut.tasks;

/**
 * This is submitted to the MotionArbiter, which decides whether to execution
 * the motion.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.navigate.MotionArbiter
 */
public class MotionTask extends Task {
	private static final long serialVersionUID = -1005883794694320611L;
	public static final double STOP_SPEED = 0;
	public static final double STOP_HEADING = 0;
	
	private double velocity;
	private double heading;
	
	public MotionTask(Priority priority, double velocity, double heading) {
		super(priority);
		this.velocity = velocity;
		this.heading = heading;
	}
	
	public double getVelocity() {
		return velocity;
	}
	
	public double getHeading() {
		return heading;
	}
	
	public boolean isStop() {
		return velocity == STOP_SPEED && heading == STOP_HEADING;
	}
	
	public boolean equals(Object o) {
		if (o instanceof MotionTask) {
			MotionTask mt = (MotionTask)o;
			return getVelocity() == mt.getVelocity() && 
				getHeading() == mt.getHeading() &&
				getPriority() == mt.getPriority();
		} else
			return false;
	}
	
	public String toString() {
		return "(MotionTask Priority=" + getPriority() + " velocity=" + velocity + " heading=" + heading + ")";
	}
	
	public static final void main(String[] args) {
		// Test whether the priority comparison function works
		MotionTask firstPriorityTask = new MotionTask(Priority.FIRST, 0, 0);
		MotionTask secondPriorityTask = new MotionTask(Priority.SECOND, 0, 0);
		if (firstPriorityTask.isHigherPriorityThan(secondPriorityTask)) {
			System.out.println("Test passed!");
		} else {
			System.out.println("Test failed!");
		}
	}
}
