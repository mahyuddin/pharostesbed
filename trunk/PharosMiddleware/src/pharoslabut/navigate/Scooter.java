package pharoslabut.navigate;

import pharoslabut.tasks.*;
import pharoslabut.MotionArbiter;
import pharoslabut.logger.FileLogger;

/**
 * Scoots the robot a small distance forward or backward.  This is to induce small
 * changes in distance between the wireless transmitter and receiver, which will
 * hopefully impact RSSI by removing noise due to multipath effects.
 * 
 * @author Chien-Liang Fok
 */
public class Scooter {

	private MotionArbiter motionArbiter;
	private FileLogger flogger;
	
	public Scooter(MotionArbiter motionArbiter, FileLogger flogger) {
		this.motionArbiter = motionArbiter;
		this.flogger = flogger;
	}
	
	public void scoot(int amt) {
		log("Scooting robot " + amt + " units");
		
		MotionTask currTask;
		double speed = 0.5;
		if (amt < 0)
			speed *= -1;
		
		currTask = new MotionTask(Priority.SECOND, speed, 0); // move forward or backwards
		log("Submitting: " + currTask);
		motionArbiter.submitTask(currTask);
		
		pause(1000*amt);
		currTask = new MotionTask(Priority.SECOND, MotionTask.STOP_VELOCITY, MotionTask.STOP_HEADING);
		log("Submitting: " + currTask);
		motionArbiter.submitTask(currTask);
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
