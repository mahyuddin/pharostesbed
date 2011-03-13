package pharoslabut.navigate;

import pharoslabut.logger.FileLogger;
import pharoslabut.navigate.motionscript.*;

/**
 * Follows a motion script.  This is a simple implementation that
 * only uses a single speed throughout the entire process.
 * 
 * @author Chien-Liang Fok
 */
public class MotionScriptFollower implements Runnable {
	private NavigateCompassGPS navigator;
	private MotionScript script;
	private boolean running = false;
	private FileLogger flogger;
	private MotionScriptFollowerDoneListener doneListener;
	
	/**
	 * The constructor.
	 * 
	 * @param navigator The object that moves the robot towards a GPS waypoint.
	 * @param script The motion script to follow.
	 * @param flogger The file logger for saving debug/experiment output.
	 */
	public MotionScriptFollower(NavigateCompassGPS navigator, MotionScript script, FileLogger flogger) {
		this.navigator = navigator;
		this.script = script;
		this.flogger = flogger;
	}
	
	/**
	 * Starts the robot following the motion script.
	 * This method should only be called when the WayPointFoller is stopped.  If it
	 * is called when the WayPointFollower is running, a false value will be returned.
	 * 
	 * @param doneListener The listener that should be notified when the WayPointFoller is done.
	 * @return true if the call was successful, false otherwise.
	 */
	public boolean start(MotionScriptFollowerDoneListener doneListener) {
		if (!running) {
			running = true;
			this.doneListener = doneListener;
			new Thread(this).start();
			return true;
		} else
			return false;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public void stop() {
		running = false;
		doneListener = null;
	}
	
	/**
	 * Executes a move instruction.
	 * 
	 * @param moveInstr The move instruction to execute.
	 * @return true if successful.
	 */
	private boolean handleMove(Move moveInstr) {
		boolean result = true;
		
		try {
			log("Going to " + moveInstr.getDest() + " at " + moveInstr.getSpeed() + "m/s");
			
			// The following is a blocking operation.  It does not return until the robot is at the destination.
			if (navigator.go(moveInstr.getDest(), moveInstr.getSpeed())) { 
				log("Destination Reached!");
			} else {
				log("Destination not reached!");
			}
			
		} catch (SensorException e) {
			log("Error accessing sensor: " + e.getMessage());
			e.printStackTrace();
			result = false;
		}
		
		return result;
	}
	
	/**
	 * Executes a pause instruction.
	 * 
	 * @param moveInstr The move instruction to execute.
	 * @return true if successful.
	 */
	private boolean handlePause(Pause pauseInstr) {
		boolean result = true;
		
		log("Pausing for " + pauseInstr.getPauseTime() + "ms");
		synchronized(this) {
			try {
				wait(pauseInstr.getPauseTime());
			} catch (InterruptedException e) {
				log("Interrupted while paused... " + e.getMessage());
				e.printStackTrace();
				result = false;
			}
		}
		return result;
	}
	
	private boolean handleStartBcastTelosB(StartBcastTelosB msg) {
		// TODO
		return true;
	}
	
	private boolean handleStartBcastWiFi(StartBcastWiFi msg) {
		// TODO
		return true;
	}
	
	private boolean handleStopBcastTelosB(StopBcastTelosB msg) {
		// TODO
		return true;
	}
	
	private boolean handleStopBcastWiFi(StopBcastWifi msg) {
		// TODO
		return true;
	}
	
	public void run() {
		int instrIndex = 0;
		
		while (running && instrIndex < script.numInstructions()) {
			Instruction instr = script.getInstruction(instrIndex);
			
			switch(instr.getType()) {
			case MOVE:
				if (!handleMove((Move)instr))
					running = false;
				break;
			case PAUSE:
				if (!handlePause((Pause)instr))
					running = false;
				break;
			case START_BCAST_TELOSB:
				if (!handleStartBcastTelosB((StartBcastTelosB)instr))
					running = false;
				break;
			case START_BCAST_WIFI:
				if (!handleStartBcastWiFi((StartBcastWiFi)instr))
					running = false;
				break;
			case STOP_BCAST_TELOSB:
				if (!handleStopBcastTelosB((StopBcastTelosB)instr))
					running = false;
				break;
			case STOP_BCAST_WIFI:
				if (!handleStopBcastWiFi((StopBcastWifi)instr))
					running = false;
				break;
			default:
				log("ERROR Unknown instruction: " + instr);
				running = false;
			}
			
			if (!running) 
				log("ERROR while executing instruction " + instrIndex + " of the motion script.");
			else 
				instrIndex++;
		}
		
		// Check whether the motion script completed successfully.
		boolean success = (instrIndex == script.numInstructions());
		
		// Log whether the execution of the motion script was successful.
		if (success)
			log("Motion Script Completed!");
		else 
			log("ERROR: Motion Script terminated prematurely.");
		
		// Notify done listeners that this motion script has finished executing.
		if (doneListener != null)
			doneListener.motionScriptDone(success, instrIndex);
		
		stop();
	}
	
	private void log(String msg) {
		String result = "MotionScriptFollower: " + msg;
		
		// only print log text to string if in debug mode
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
		
		// always log text to file if a FileLogger is present
		if (flogger != null)
			flogger.log(result);
	}
}
