package pharoslabut.demo.autoIntersection;

import pharoslabut.navigate.*;
import pharoslabut.logger.*;

/**
 * Communicates with the intersection server to safely navigate the 
 * intersection.
 * 
 * @author Chien-Liang Foks
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
	
	private LineFollower lf;
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
			log("Received approaching event!");
			//TODO Implement this...
			break;
		case ENTERING:
			//TODO Implement this...
			break;
		case EXITING:
			//TODO Implement this...
			break;
		case ERROR:
			//TODO Implement this...
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
