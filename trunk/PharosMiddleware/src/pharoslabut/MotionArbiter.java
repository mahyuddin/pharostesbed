package pharoslabut;

import pharoslabut.tasks.MotionTask;
import pharoslabut.logger.FileLogger;
import playerclient3.Position2DInterface;

/**
 * Determines which motion task to execute and continuously executes it
 * until a higher-priority task arrives.  Task submissions may be rejected
 * if a higher-priority task is being executed. The process submitting the task
 * can tell if its task was accepted based on the return value of the 
 * submitTask(...) method.
 * 
 * Once a motion task accepted, it will be continuously executed until another 
 * task of equal or higher priority is submitted, or if the task is explicitly
 * revoked using the revokeTask(...) method.
 * 
 * @author Chien-Liang Fok
 */
public class MotionArbiter implements Runnable {
	
	/**
	 * Defines the type of motion to use.  This depends on the type
	 * of mobility plane being used.
	 */
	public static enum MotionType {MOTION_TRAXXAS, MOTION_IROBOT_CREATE, MOTION_SEGWAY_RMP50};
	
	/**
	 * The cycle time in milliseconds, which controls the frequency at which
	 * this arbiter sends motion commands to the robot.  This is necessary 
	 * because the robot runs in "safe" mode in which it will stop moving
	 * if it does not receive an updated motion command within
	 * a certain period of time.  To keep the robot moving, this CYCLE_TIME
	 * must be less than this stop period.  On the Traxxas mobility plane,
	 * the stop period is 2 seconds (0.5Hz refresh rate minimum).  
	 * On the Segway RMP 50, it is 400ms (2.5Hz refresh rate minimum).
	 */
	public static final int CYCLE_TIME = 100; //250;
	
	/**
	 * Whether the robot moves in a car-like fashion or a ball-like fashion.
	 */
	private MotionType motionType;
	
	/**
	 * The interface to control the motors that move the robot.
	 */
	private Position2DInterface motors;
	
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
		this(MotionType.MOTION_TRAXXAS, motors);
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
		
		motors.setMotorPower(1); // Turn the motors on.  This is needed by the Segway RMP 50s
		new Thread(this).start(); // The MotionArbiter has its own thread that processes MotionTasks
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
	 * Submit a task to be executed.  If the submitted task has equal or higher
	 * priority than the currently-running task, it will preempt the current task.
	 * Otherwise, it will be rejected.  
	 * 
	 * @param mt The motion task being submitted.
	 * @return true if the motion task was accepted for execution, false otherwise.
	 */
	public synchronized boolean submitTask(MotionTask mt) {
		boolean result = false;
		
		if (currTask != null) {
			if (currTask.isEqualPriorityTo(mt) || mt.isHigherPriorityThan(currTask)) {
				currTask = mt;
				notifyAll();
				result = true;
			} // else discard task since higher priority task is running			
		} else {
			
			// Always except the task if there is no current task being executed.
			currTask = mt;
			notifyAll();
			result = true;
		}
		return result;
	}
	
	/**
	 * Remove a previously-submitted task.
	 * 
	 * @param mt The motion task to be removed.
	 */
	public synchronized void revokeTask(MotionTask mt) {
		if (currTask != null && currTask.equals(mt)) {
			currTask = null;
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
	
	/**
	 * Sends a motion command to the robot.
	 * 
	 * @param speed The speed at which to move.
	 * @param heading The angle in which to turn.
	 */
	private void sendMotionCmd(double speed, double heading) {
		log("Sending motion command velocity=" + speed + ", heading=" + heading);
		
		if (motionType == MotionType.MOTION_TRAXXAS) {
			motors.setCarCMD(speed, heading);
		} else {
			// Both the Segway RMP 50 and Irobot Create use this method
			motors.setSpeed(speed, heading);
		}
		
		isStopped = (speed == MotionTask.STOP_SPEED);
	}
	
	private void log(String msg) {
		String result = "MotionArbiter: " + msg;
		if (System.getProperty ("PharosMiddleware.debug") != null) 
			System.err.println(result);
		
		if (flogger != null)
			flogger.log(result);
	}
	
	public void run() {
		MotionTask motionTask = null;
		
		while(true) {
			
			// Grab a reference to the current motion task in a manner that is synchronized with
			// the setting of the current motion task.
			synchronized(this) {
				motionTask = currTask;
			}
			
			if (motionTask != null) {
				sendMotionCmd(motionTask.getVelocity(), motionTask.getHeading());

				// No point in repeatedly sending a stop motion command
				// (The robot will by default stop moving when no command is received)
				if (motionTask.isStop()) {
					log("MotionTask is stop, resorting to initial state");
					currTask = null;
				}
			} else {
				// There is no motion task to execute...

				if (!isStopped) {
					// The robot is not stopped...stop the robot.
					sendMotionCmd(MotionTask.STOP_SPEED, MotionTask.STOP_HEADING);
				}

				// Task queue is empty and there is no task to run, wait for a motion task to 
				// be inserted into the taskQueue.
				try {
					synchronized(this) {
						// Only pause if the currTask is null.  Note that this is
						// executed within a synchronized block to prevent race
						// conditions with the submitTask(...) and revokeTask(...) methods.
						if (currTask == null)
							wait();
					} 
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			try {
				synchronized(this) {
					wait(CYCLE_TIME);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
