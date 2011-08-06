package pharoslabut.navigate;

import pharoslabut.tasks.*;
import pharoslabut.logger.FileLogger;

/**
 * Scoots the robot a small distance forward or backward.  This is to induce small
 * changes in distance between the wireless transmitter and receiver, which will
 * hopefully impact RSSI by removing noise due to multi-path effects.
 * 
 * @author Chien-Liang Fok
 */
public class Scooter implements Runnable {

	private MotionArbiter motionArbiter;
	private FileLogger flogger;
	private boolean running = false;
	
	public Scooter(MotionArbiter motionArbiter, FileLogger flogger) {
		this.motionArbiter = motionArbiter;
		this.flogger = flogger;
	}
	
	public void scoot(int amt) {
		running = true;
		new Thread(this).start();
	}
	
	public void run() {
		log("Scooting robot..");
		
		MotionTask currTask;
		boolean goForward = true;
		
		while (running) {
		
			double speed = 1;
			if (!goForward)
				speed = -1.05;
			goForward = !goForward;
		
			currTask = new MotionTask(Priority.SECOND, speed, 0); // move forward or backwards
			log("Submitting: " + currTask);
			motionArbiter.submitTask(currTask);
		
			pause(1500);
			
			currTask = new MotionTask(Priority.SECOND, MotionTask.STOP_SPEED, MotionTask.STOP_HEADING);
			log("Submitting: " + currTask);
			motionArbiter.submitTask(currTask);
			
			pause(1000);
		}
	}
	
	private void pause(int duration) {
		synchronized(this) {
			try {
				wait(duration);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void log(String msg) {
		String result = "Scooter: " + msg;
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
}
