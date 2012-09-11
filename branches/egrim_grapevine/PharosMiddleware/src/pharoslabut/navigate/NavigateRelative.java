package pharoslabut.navigate;

//import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.tasks.MotionTask;

/**
 * Takes absolute navigation commands like move forward/backward, left/right
 * and moves the robot as instructed.
 * 
 * @author Chien-Liang Fok
 */
public class NavigateRelative implements Runnable {
	
	private MotionArbiter motionArbiter;
//	private FileLogger flogger;
	private RelativeMotionScript script;
	private boolean running = false;
	
	public NavigateRelative(MotionArbiter motionArbiter, RelativeMotionScript script) {
		this.motionArbiter = motionArbiter;
		this.script = script;
//		this.flogger = flogger;
	}
	
	public void start() {
		if (!running) {
			running = true;
			new Thread(this).start();
		}
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public void stop() {
		running = false;
	}
	
	public void run() {
		int indx = 0;
		
		while (running && indx < script.getNumSteps()) {
			MotionTask currMotionTask = script.getMotionTask(indx);
			long currDuration = script.getDuration(indx);
			
			Logger.log("Submitting task: " + currMotionTask + ", duration: " + currDuration + "...");
			motionArbiter.submitTask(currMotionTask);
			
			try {
				synchronized(this) {
					Logger.log("Waiting for " + currDuration + "ms...");
					wait(currDuration);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			motionArbiter.revokeTask(currMotionTask);
			indx++;
		}
		
		Logger.log("Done following motion script...");
	}
	
//	private void log(String msg) {
//		String result = "NavigateRelative: " + msg;
//		if (System.getProperty ("PharosMiddleware.debug") != null) 
//			System.out.println(result);
//		if (flogger != null) {
//			flogger.log(result);
//		}
//	}
}
