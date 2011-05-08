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
	private Scooter scooter;
	
	private MotionScript script;
	private boolean running = false;
	private FileLogger flogger;
	private MotionScriptFollowerDoneListener doneListener;
	
	/**
	 * Whether the server should continue to run after this motion script
	 * is finished.  This becomes true if the last instruction in the
	 * motion script is WAIT_EXP_STOP.
	 */
	private boolean continueRunning = false;
	
	/**
	 * This is responsible for sending WiFi beacons.
	 */
	private WiFiBeaconBroadcaster wifiBroadcaster;
	
	/**
	 * This is responsible for sending TelosB beacons.
	 */
	private TelosBeaconBroadcaster telosBeaconBroadcaster;
	
	/**
	 * This is responsible for receiving TelosB beacons.
	 */
	private TelosBeaconReceiver telosBeaconReceiver;
	
	/**
	 * The constructor.
	 * 
	 * @param navigator The object that moves the robot towards a GPS waypoint.
	 * @param scooter The object that scoots the robot forwards or backwards a small amount.
	 * @param wifiBroadcaster The object that broadcasts WiFi beacons.
	 * @param beaconBroadcaster The WiFi beacon broadcaster.
	 * @param telosBeaconBroadcaster The Telos beacon broadcaster.
	 * @param flogger The file logger for saving debug/experiment output.
	 */
	public MotionScriptFollower(NavigateCompassGPS navigator, Scooter scooter,
			WiFiBeaconBroadcaster wifiBroadcaster, 
			TelosBeaconBroadcaster telosBeaconBroadcaster,
			FileLogger flogger) 
	{
		this.navigator = navigator;
		this.scooter = scooter;
		this.wifiBroadcaster = wifiBroadcaster;
		
		// Only save the TelosB components if the TelosB is not disabled...
		if (System.getProperty ("PharosMiddleware.disableTelosB") == null) {
			this.telosBeaconBroadcaster = telosBeaconBroadcaster;
			this.telosBeaconReceiver = telosBeaconBroadcaster.getReceiver();
		}
		
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
	 * Executes a move instruction.  This moves the robot to a specific
	 * GPS location.
	 * 
	 * @param moveInstr The move instruction to execute.
	 * @return true if successful.
	 */
	private boolean handleMove(Move moveInstr) {
		boolean result = true;
		
		log("Going to " + moveInstr.getDest() + " at " + moveInstr.getSpeed() + "m/s");

		// The following is a blocking operation.  It does not return until the robot is at the destination.
		if (navigator.go(moveInstr.getDest(), moveInstr.getSpeed())) { 
			log("Destination Reached!");
		} else {
			log("Destination not reached!");
			result = false;
		}
		
		return result;
	}
	
	/**
	 * Executes a pause instruction.
	 * 
	 * @param pauseInstr The pause instruction to execute.
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
	
	/**
	 * This instruction starts the broadcasting of TelosB beacons.
	 * 
	 * @param instr The instruction.
	 * @return true if successful, false otherwise.
	 */
	private boolean handleStartBcastTelosB(StartBcastTelosB instr) {
		if (System.getProperty ("PharosMiddleware.disableTelosB") == null) {
			// Start the TelosB cc2420 radio signal meter
			flogger.log("Starting TelosB beacon broadcaster, minPeriod = " 
					+ instr.getMinPeriod() + ", maxPeriod = " + instr.getMaxPeriod());
			return telosBeaconBroadcaster.start(instr.getMinPeriod(), instr.getMaxPeriod(), instr.getTxPowerLevel());
		} else {
			log("ERROR: Cannot execute the following instruction because the TelosB is disabled: " + instr);
			return false; // the TelosB was disabled!
		}
	}
	
	private boolean handleStartBcastWiFi(StartBcastWiFi instr) {
		flogger.log("Starting WiFi beacon broadcaster, minPeriod = " 
				+ instr.getMinPeriod() + ", maxPeriod = " + instr.getMaxPeriod());
		return wifiBroadcaster.start(instr.getMinPeriod(), instr.getMaxPeriod(), instr.getTxPowerLevel());
	}
	
	/**
	 * This instruction stops the broadcasting of TelosB beacons.
	 * 
	 * @param instr The instruction.
	 * @return true if successful, false otherwise.
	 */
	private boolean handleStopBcastTelosB(StopBcastTelosB instr) {
		if (System.getProperty ("PharosMiddleware.disableTelosB") == null) {
			flogger.log("Stopping TelosB beacon broadcaster.");
			telosBeaconBroadcaster.stop();
			return true;
		} else {
			log("ERROR: Cannot execute the following instruction because the TelosB is disabled: " + instr);
			return false; // the TelosB was disabled!
		}
	}
	
	private boolean handleStopBcastWiFi(StopBcastWifi msg) {
		flogger.log("Stopping WiFi beacon broadcaster.");
		wifiBroadcaster.stop();
		return true;
	}
	
	private boolean handleScoot(Scoot msg) {
		flogger.log("Scooting the robot " + msg.getAmount());
		scooter.scoot(msg.getAmount());
		return true;
	}
	
	/**
	 * This instruction blocks until a certain number of TelosB beacons are received.
	 * 
	 * @param instr The instruction.
	 * @return true if successful, false otherwise.
	 */
	private boolean handleRcvTelosbBeacons(RcvTelosbBeacons instr) {
		if (System.getProperty ("PharosMiddleware.disableTelosB") == null) {
			telosBeaconReceiver.rcvBeacons(instr.getNumBeacons());
			return true;
		} else {
			log("ERROR: Cannot execute the following instruction because the TelosB is disabled: " + instr);
			return false; // the TelosB was disabled!
		}
	}
	
	public void run() {
		int instrIndex = 0;
		
		while (running && instrIndex < script.numInstructions()) {
			Instruction instr = script.getInstruction(instrIndex);
			
			switch(instr.getType()) {
			case MOVE:
				running = handleMove((Move)instr);
				break;
			case PAUSE:
				running = handlePause((Pause)instr);
				break;
			case START_BCAST_TELOSB:
				running = handleStartBcastTelosB((StartBcastTelosB)instr);
				break;
			case START_BCAST_WIFI:
				running = handleStartBcastWiFi((StartBcastWiFi)instr);
				break;
			case STOP_BCAST_TELOSB:
				running = handleStopBcastTelosB((StopBcastTelosB)instr);
				break;
			case STOP_BCAST_WIFI:
				running = handleStopBcastWiFi((StopBcastWifi)instr);
				break;
			case RCV_TELOSB_BEACONS:
				running = handleRcvTelosbBeacons((RcvTelosbBeacons)instr);
				break;
			case SCOOT:
				running = handleScoot((Scoot)instr);
				break;
			case WAIT_EXP_STOP:
				continueRunning = true; // Tell PharosServer to continue to run until a StopExpMsg is received.
				running = false; // Tell this MotionScriptFollower to stop running (this is the end fo the script)
				break;
			default:
				log("ERROR Unknown instruction: " + instr);
				running = false;
			}
			
			if (!continueRunning && !running) 
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
			doneListener.motionScriptDone(success, instrIndex, continueRunning);
		
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