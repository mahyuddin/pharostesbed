package pharoslabut.demo.autoIntersection;

import pharoslabut.logger.FileLogger;
import pharoslabut.navigate.LineFollower;
import pharoslabut.navigate.LineFollowerEvent;
import pharoslabut.navigate.LineFollowerEventListener;

import playerclient3.*;
import playerclient3.structures.PlayerConstants;
import playerclient3.structures.ir.PlayerIrData;

/**
 * Handles the protocol for navigating through an intersection
 * without a server.
 * 
 * @author Seth Gee
 */
public class LocalIntersectionManager implements LineFollowerEventListener, Runnable {

	/**
	 * This component is responsible for making the robot follow the line,
	 * which represents a lane.
	 */
	private LineFollower lf;
	
	/**
	 * For logging debug messages.
	 */
	private FileLogger flogger;
	
	/**
	 * Keeps track of whether the LocalIntersectionManager is running.
	 */
	private boolean isRunning = false;
	
	/**
	 * Whether the robot has reached the exit of the intersection.
	 */
	private boolean reachedExit = false;
	
	/**
	 * The specification of the entry/exit lanes that the robot wants to
	 * travel through.  This class is responsible for ensuring the robot
	 * can travel through the intersection safely.
	 */
	private LaneSpecs laneSpecs;
	
	/**
	 * The client manager is the main component of the robot-side of the application.
	 * It coordinates the line follower, remote intersection manager, and 
	 * local intersection manager.
	 */
	private ClientManager clientMgr;
	
	
	/**
	 * Holds the latest IR range data.
	 */
	PlayerIrData irData;
	
	/**
	 * Records when the latest IR data was received.
	 */
	long irDataTimeStamp;
	
	/**
	 * The constructor.
	 * 
	 * @param lf The LineFollower
	 * @param clientmgr The client manager that should be notified should this
	 * class fail to navigate the intersection.
	 * @param flogger The FileLogger for recording debug statements.
	 */
	public LocalIntersectionManager(LineFollower lf, ClientManager clientMgr, FileLogger flogger) {
		this.lf = lf;
		this.clientMgr = clientMgr;
		this.flogger = flogger;
		this.isRunning = true; 
		
		// Connect to the IR sensors...
		PlayerClient client = lf.getPlayerClient();
		
		// Uncomment these lines when ready to enable IR sensors.
		try{
			// TODO replace with ranger interface
			IRInterface ir = client.requestInterfaceIR(0, PlayerConstants.PLAYER_OPEN_MODE);
			//ir.addIRListener(this);
		} catch (PlayerException e) { 
			log("Error, could not connect to IR proxy.", false);
			System.exit(1);
		}
	}
	
	/**
	 * Starts the RemoteIntersectionManager running.  This should be
	 * called when the LineFollower approaches the intersection.
	 * 
	 * @param laneSpecs The lane specifications.  This specifies which line the robot is
	 * approaching from and which lane it would like to exit the intersection from.
	 */
	public void start(LaneSpecs laneSpecs) {
		this.isRunning = true; 
		if (isRunning) {
			this.laneSpecs = laneSpecs;
			this.reachedExit = false;
			new Thread(this).start();
		} else {
			log("start: ERROR: Trying to start twice!", false);
		}
	}
	
	@Override
	public void newLineFollowerEvent(LineFollowerEvent lfe, LineFollower follower) {
		if (isRunning) {
			switch(lfe.getType()) {
			// The APPROACHING event is now handled by the ClientManager
			case APPROACHING:
//				doApproaching();
				break;
			
//			By the time this method is called, the robot should already be at the entrance 
//			to the intersection
			case ENTERING:
//			doEntering();
				break;
			case EXITING:
				reachedExit = true;
				break;
				
//			This message is now handled by the ClientManager
			case ERROR:
				log("newLineFollowerEvent: Received error from line follower!  Aborting demo.", false);
				lf.stop(); // There was an error, stop!
//				break;
			default:
				log("newLineFollowerEvent: Unexpected event from line follower (discarding): " + lfe, false);
			}
		} else
			log("newLineFollowerEvent: Ignoring event because not running: " + lfe);
	}
	
//	@Override
//	public void newPlayerIRData(PlayerIrData data) {
//		this.irData = data;
//		this.irDataTimeStamp = System.currentTimeMillis();
//	}
	
	public void run() {
		log("run: Thread starting...");
		
		while(!reachedExit) {
			// TODO...
			// Use IR sensors to safely traverse intersection!
			// If IR sensors detect obstacle, call lf.stop()
			// If all clear, call lf.go()
			// Make use of member variables "irData" and "irDataTimeStamp"
			float ranges[] = (irData.getRanges());
			log("ranges are: " + ranges[0] + " " + ranges[1] + " " + ranges[2]);
			
			if(ranges[0] < 1000 || ranges[1] < 2200 || ranges[2] < 1000) {	
				lf.stop();
			}
			else {
				reachedExit = true; 
				lf.start();
			}
		}
		
		// After it is done...
		isRunning = false;
		
		// Notify the ClientManager that the LocalIntersectionManager is done.
		clientMgr.localIntersectionMgrDone(true);
		
		log("run: Thread terminating...");
	}
	
	/**
	 * Logs a debug message.  This message is only printed when debug mode is enabled.
	 * 
	 * @param msg The message to log.
	 */
	private void log(String msg) {
		log(msg, true);
	}
	
	/**
	 * Logs a message.
	 * 
	 * @param msg  The message to log.
	 * @param isDebugMsg Whether the message is a debug message.
	 */
	private void log(String msg, boolean isDebugMsg) {
		String result = "LocalIntersectionMar: " + msg;
		if (!isDebugMsg || System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
}
