package pharoslabut.demo.autoIntersection;

import pharoslabut.navigate.*;
import pharoslabut.logger.*;

/**
 * Communicates with the intersection server to safely navigate the 
 * intersection.
 * 
 * @author Chien-Liang Fok
 * @author Seth Gee
 */
public class RemoteIntersectionManager implements LineFollowerEventListener {

	/**
	 * This is the IP address of the intersection server.
	 */
	private String serverIP;
	
	/**
	 * This is the port on which the intersection server is listening.
	 */
	private int serverPort;
	
	/**
	 * This component is responsible for ensuring the robot follows the line.
	 */
	private LineFollower lf;
	
	/**
	 * For logging debug messages.
	 */
	private FileLogger flogger;
	
	/**
	 * The constructor.
	 * 
	 * @param lf The LineFollower
	 */
	public RemoteIntersectionManager(LineFollower lf, String serverIP, int serverPort, FileLogger flogger) {
		this.lf = lf;
		this.serverIP = serverIP;
		this.serverPort = serverPort;
		this.flogger = flogger;
		
		lf.addListener(this);
	}

	@Override
	public void newLineFollowerEvent(LineFollowerEvent lfe) {
		switch(lfe.getType()) {
		case APPROACHING:
			log("Robot is approaching intersection!");
			//TODO Implement this... Ask server for permission to cross intersection.
			break;
		case ENTERING:
			log("Robot is entering intersection!");
			//TODO Implement this... if approval has not been obtained, pause and wait 
			// for the approval to arrive (may need to query server again).
			break;
		case EXITING:
			log("Robot is exiting intersection!");
			//TODO Implement this...Notify server that this robot is leaving the intersection
			
			break;
		case ERROR:
			log("Received error from line follower!  Aborting demo.");
			lf.stop(); // There was an error, stop!
			break;
		}
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
		String result = "RemoteIntersectionMar: " + msg;
		if (!isDebugMsg || System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
}
