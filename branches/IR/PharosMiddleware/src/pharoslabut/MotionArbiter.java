package pharoslabut;


import pharoslabut.tasks.MotionTask;
import pharoslabut.logger.FileLogger;
import playerclient.Position2DInterface;

/**
 * Arbitrates which motion task is executed.  During each cycle, it
 * selects the highest-priority task and executes it, discarding all
 * other tasks.  Processes that submit tasks must be aware that their
 * requests may be ignored due to being preempted by higher-priority
 * tasks. They must continuously submit their tasks until it is 
 * fulfilled.
 * 
 * Once a motion task is selected and executed, the vehicle will continue
 * to move in the manner specified by the task until another task is run.
 * Thus, users must explicitly submit a "stop" task to halt the vehicle.
 * 
 * @author Chien-Liang Fok
 */
public class MotionArbiter implements Runnable {
	public static enum MotionType {MOTION_CAR_LIKE, MOTION_IROBOT_CREATE, MOTION_SEGWAY_RMP50};
	
	/**
	 * The cycle time in milliseconds, which controls the frequency at which
	 * this arbiter sends motion commands to the robot.  This is necessary 
	 * because the robot runs in "safe" mode in which it will stop moving
	 * if it does not receive an updated motion command from the client within
	 * a certain period of time.  To keep the robot moving, this CYCLE_TIME
	 * must be less than the stop period.
	 */
	public static final int CYCLE_TIME = 500;
	
	/**
	 * Whether the robot moves in a car-like fashion or a ball-like fashion.
	 */
	private MotionType motionType;
	
	/**
	 * The interface to control the motors that move the robot.
	 */
	private Position2DInterface motors;
	
	/**
	 * This is the queue of pending tasks that need to be completed.
	 */
//	private Vector<MotionTask> taskQueue;
	
	/**
	 * The current task that is being executed.
	 */
	private MotionTask currTask = null;
	
	/**
	 * For logging debug output to a file.
	 */
	private FileLogger flogger = null;
	
	/**
	 * Whether the most recent command sent to the robot was to stop.  
	 * If so, this component needs to continue to periodically send the current
	 * motion task to the robot.
	 */
	private boolean isStopped = false;
	
	//private AccelerationControl accelControl = new AccelerationControl();
	
	/**
	 * Creates a MotionArbiter with the default motion type (MOTION_CAR_LIKE).
	 * 
	 * @param motors The player proxy for accessing the movement motors.
	 */
	public MotionArbiter(Position2DInterface motors) {
		this(MotionType.MOTION_CAR_LIKE, motors);
		
	}
	
	/**
	 * Creates a MotionArbiter with the specified motion type.
	 * 
	 * @param motionType The motion type of the robot being controlled.
	 * @param motors The player proxy for accessing the movement motors.
	 */
	public MotionArbiter(MotionType motionType, Position2DInterface motors) {
		this.motionType = motionType;
		this.motors = motors;
//		taskQueue = new Vector<MotionTask>();
		
		// The MotionArbiter has its own thread that processes MotionTasks
		new Thread(this).start();
	}
	
	/**
	 * Sets the file logger.  This can be null to stop this component from logging data.
	 * 
	 * @param flogger The file logger to use.
	 */
	public void setFileLogger(FileLogger flogger) {
		this.flogger = flogger;
	}
	
	/**
	 * Submit a task to be executed.  Note that there is no guarantee that it will actually be 
	 * executed since it may be preempted by a higher-priority task.
	 * 
	 * If the newly submitted task has the same or higher priority than the currently-running task,
	 * revoke the current task and then add the new task to the queue.
	 * 
	 * @param mt The motion task being submitted.
	 */
	public synchronized void submitTask(MotionTask mt) {
		if (currTask != null) {
			if (currTask.isEqualPriorityTo(mt) || mt.isHigherPriorityThan(currTask)) {
				currTask = mt;
				notifyAll();
			} // else discard task since higher priority task is running			
		} else {
			currTask = mt;
			notifyAll();
		}
	}
	
	/**
	 * Remove a previously-submitted task.
	 * 
	 * @param mt The motion task to be removed.
	 */
	public synchronized void revokeTask(MotionTask mt) {
		if (currTask != null && currTask.equals(mt)) {
			currTask = null;
			//accelControl.reset();
			notifyAll();
		} // else the task was never accepted, ignore the revocation request
	}
	
	/**
	 * Finds the task with the highest priority.  If there are no
	 * tasks in the queue, return the currently-running task.
	 *
	 * Note: In the future, motion tasks should include an expiration 
	 * time.  Instead of removing all items in the queue each cycle
	 * period, it should only remove those tasks that have expired.
	 * 
	 * @return The task with the highest priority.  If there are no
	 * tasks in the task queue, return the one that is currently running
	 * or null if no task was ever run.
	 */
//	private synchronized MotionTask getHighestPriorityTask() {
//			if (!taskQueue.isEmpty()) {
//				currTask = taskQueue.get(0);
//				Enumeration<MotionTask> e = taskQueue.elements();
//				while (e.hasMoreElements()) {
//					MotionTask mt = e.nextElement();
//					if (mt.isHigherPriorityThan(currTask)) {
//						currTask = mt;
//					}
//				}
//				taskQueue.clear();
//			}
//		return currTask;
//	}
	
	private void sendMotionCmd(double velocity, double heading) {
		log("Sending the following motion command: velocity=" + velocity + ", heading=" + heading);
		
		if (motionType == MotionType.MOTION_CAR_LIKE) {
			motors.setCarCMD(velocity, heading);
		} else {
			motors.setSpeed(velocity, heading);
		}
		
		isStopped = (velocity == MotionTask.STOP_VELOCITY);
	}
	
	private void log(String msg) {
		String result = "MotionArbiter: " + msg;
		if (System.getProperty ("PharosMiddleware.debug") != null) 
			System.err.println(result);
		
		if (flogger != null) {
			flogger.log(result);
		}
	}
	
	public void run() {
		MotionTask motionTask = null;
		
		// Ensure robot is not moving
		//sendMotionCmd(MotionTask.STOP_VELOCITY, MotionTask.STOP_HEADING);
		
		while(true) {
			synchronized(this) {
				motionTask = currTask; //getHighestPriorityTask();
				if (motionTask != null) {
					
					// get the current speed to send it to the robot
					//double currSpeed = motionTask.getVelocity();   
					
					// This was removed because the MCU firmware now controls the acceleration
					// double currSpeed = accelControl.getDampenedSpeed(motionTask.getVelocity());
					
					sendMotionCmd(motionTask.getVelocity(), motionTask.getHeading());
					
					// no point in periodically sending a stop motion command...
					if (motionTask.isStop()) {
						log("MotionTask is stop, resorting to initial state");
						currTask = null;
					}
				} else {
					// There is no motion task to execute...
					
					if (!isStopped) {
						// The robot is not stopped...stop the robot.
						sendMotionCmd(MotionTask.STOP_VELOCITY, MotionTask.STOP_HEADING);
					}
					
					// Task queue is empty and there is no task to run, wait for a motion task to 
					// be inserted into the taskQueue.
					try {
						wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				try {
					wait(CYCLE_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Controls the rate at which the robot accelerates.  It prevents
	 * the robot from receiving a very high step function, which may
	 * cause such a large current to be drawn that the motor battery's fuse is
	 * tripped.
	 * 
	 * The tunable variables in this class are deltaTime and deltaSpeed.  The
	 * rate of acceleration is controlled as follows: For every deltaTime, the speed
	 * command can change by at most deltaSpeed.  For example, if deltaTime is 500ms
	 * and deltaSpeed is 0.2m/s, then for every 500ms, the speed can change at most 0.2m/s.
	 * Thus, if the robot is stoped and receives a command to go at 1m/s, it will take
	 * (1m/s) / (0.2m/s) * 0.5s = 2.5s to reach that speed.
	 * 
	 * @author Chien-Liang Fok
	 */
//	private class AccelerationControl {
//		private final long deltaTime = 200; // The time step interval in milliseconds
//		private final double deltaSpeed = 0.5; // The speed step interval in in m/s
//		
//		private double currSpeed; 
//		
//		private long lastUpdateTime;
//		
//		public AccelerationControl() {
//			reset();
//		}
//		
//		public void reset() {
//			currSpeed = 0; // assume the robot always starts at speed 0m/s
//			lastUpdateTime = System.currentTimeMillis();
//		}
//		
//		private double calcNewSpeed(double currSpeed, double targetSpeed) {
//			double result;
//			if (targetSpeed > currSpeed) {
//				result = currSpeed + deltaSpeed;
//				if (result > targetSpeed)
//					result = targetSpeed; // ensure the target speed is not exceeded positively
//			} else {
//				result = currSpeed - deltaSpeed;
//				if (result < targetSpeed)
//					result = targetSpeed; // ensure the target speed is not exceeded negatively
//			}
//			return result;
//		}
//		
//		public double getDampenedSpeed(double targetSpeed) {
//			if (currSpeed == targetSpeed) {
//				// The current speed is already the target speed, 
//				// no need to dampen the acceleration
//				return targetSpeed;
//			} else {
//				long currTime = System.currentTimeMillis();
//				long delta = currTime - lastUpdateTime;
//				lastUpdateTime = currTime; // update the last update time to be now
//				if (delta > deltaTime) {
//					// The target speed differs from the current speed, and the update
//					// time has passed.  Calculate the new current speed.
//					currSpeed = calcNewSpeed(currSpeed, targetSpeed);
//					return currSpeed;
//				} else {
//					// The speed step interval has not yet passed, 
//					// maintain the current speed command
//					return currSpeed;
//				}
//			}
//			
//		}
//	}
}
