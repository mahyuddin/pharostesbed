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
	public static final double STOP_STEERING_ANGLE = 0;
	
	private double speed;
	private double steeringAngle;
	
	/**
	 * Creates a MotionTask with priority FIRST, velocity 0.0, and heading 0.0.
	 */
	public MotionTask() {
		this(Priority.FIRST, 0, 0);
	}
	
	/**
	 * Creates a MotionTask with the specified parameters.
	 * 
	 * @param priority The priority of the task.
	 * @param speed The velocity in m/s.
	 * @param steeringAngle The heading in rad/s.
	 */
	public MotionTask(Priority priority, double speed, double steeringAngle) {
		super(priority);
		this.speed = speed;
		this.steeringAngle = steeringAngle;
	}
	
	public synchronized void update(Priority priority, double speed, double steeringAngle) {
		setPriority(priority);
		this.speed = speed;
		this.steeringAngle = steeringAngle;
	}
	
//	public void setSpeed(double speed) {
//		this.speed = speed;
//	}
//	
//	public void setHeading(double heading) {
//		this.heading = heading;
//	}
	
	public synchronized double getSpeed() {
		return speed;
	}
	
	public synchronized double getSteeringAngle() {
		return steeringAngle;
	}
	
	public boolean isStop() {
		return speed == STOP_SPEED && steeringAngle == STOP_STEERING_ANGLE;
	}
	
	public boolean equals(Object o) {
		if (o instanceof MotionTask) {
			MotionTask mt = (MotionTask)o;
			return getSpeed() == mt.getSpeed() && 
				getSteeringAngle() == mt.getSteeringAngle() &&
				getPriority() == mt.getPriority();
		} else
			return false;
	}
	
	public String toString() {
		return "(MotionTask Priority=" + getPriority() + " speed=" + speed + " steeringAngle=" + steeringAngle + ")";
	}
	
//	public static final void main(String[] args) {
//		// Test whether the priority comparison function works
//		MotionTask firstPriorityTask = new MotionTask(Priority.FIRST, 0, 0);
//		MotionTask secondPriorityTask = new MotionTask(Priority.SECOND, 0, 0);
//		if (firstPriorityTask.isHigherPriorityThan(secondPriorityTask)) {
//			System.out.println("Test passed!");
//		} else {
//			System.out.println("Test failed!");
//		}
//	}
}
