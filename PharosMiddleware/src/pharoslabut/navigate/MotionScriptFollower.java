package pharoslabut.navigate;

import pharoslabut.beacon.WiFiBeaconBroadcaster;
import pharoslabut.logger.FileLogger;
import pharoslabut.navigate.motionscript.*;
import pharoslabut.radioMeter.cc2420.*;

/**
 * Follows a motion script.  A motion script consists of a series of instructions
 * that control the movement and communication activities of the robot.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.navigate.motionscript.MotionScript
 */
public class MotionScriptFollower implements Runnable {
	private NavigateCompassGPS navigator;
	private MotionScript script;
	private boolean running = false;
	private FileLogger flogger;
	private MotionScriptFollowerDoneListener doneListener;
	
	/**
	 * This is responsible for sending WiFi beacons.
	 */
	private WiFiBeaconBroadcaster wifiBroadcaster;
	
	/**
	 * This is responsible for sending and receiving TelosB beacons.
	 */
	private TelosBeaconBroadcaster telosRadioSignalMeter;
	
	/**
	 * The constructor.
	 * 
	 * @param navigator The object that moves the robot towards a GPS waypoint.
	 * @param beaconBroadcaster The WiFi beacon broadcaster.
	 * @param telosRadioSignalMeter The Telos beacon broadcaster.
	 * @param flogger The file logger for saving debug/experiment output.
	 */
	public MotionScriptFollower(NavigateCompassGPS navigator, WiFiBeaconBroadcaster wifiBroadcaster, 
			TelosBeaconBroadcaster telosRadioSignalMeter, FileLogger flogger) 
	{
		this.navigator = navigator;
		this.wifiBroadcaster = wifiBroadcaster;
		this.telosRadioSignalMeter = telosRadioSignalMeter;
		this.flogger = flogger;
	}
	
	/**
	 * Starts the robot following the motion script.
	 * This method should only be called when the WayPointFoller is stopped.  If it
	 * is called when the WayPointFollower is running, a false value will be returned.
	 * 
	 * @param script The motion script to follow.
	 * @param doneListener The listener that should be notified when the WayPointFoller is done.
	 * @return true if the call was successful, false otherwise.
	 */
	public boolean start(MotionScript script, MotionScriptFollowerDoneListener doneListener) {
		boolean result = true;
		
		if (!running) {
			running = true;
			
			this.script = script;
			this.doneListener = doneListener;
			
			new Thread(this).start();
		} else
			result = false;
		
		return result;
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
	
	private boolean handleStartBcastTelosB(StartBcastTelosB instr) {
		// Start the TelosB cc2420 radio signal meter
		flogger.log("Starting TelosB beacon broadcaster, minPeriod = " 
				+ instr.getMinPeriod() + ", maxPeriod = " + instr.getMaxPeriod());
		return telosRadioSignalMeter.start(instr.getMinPeriod(), instr.getMaxPeriod());
	}
	
	private boolean handleStartBcastWiFi(StartBcastWiFi instr) {
		flogger.log("Starting WiFi beacon broadcaster, minPeriod = " 
				+ instr.getMinPeriod() + ", maxPeriod = " + instr.getMaxPeriod());
		return wifiBroadcaster.start(instr.getMinPeriod(), instr.getMaxPeriod());
	}
	
	private boolean handleStopBcastTelosB(StopBcastTelosB msg) {
		flogger.log("Stopping TelosB beacon broadcaster.");
		telosRadioSignalMeter.stop();
		return true;
	}
	
	private boolean handleStopBcastWiFi(StopBcastWifi msg) {
		flogger.log("Stopping WiFi beacon broadcaster.");
		wifiBroadcaster.stop();
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