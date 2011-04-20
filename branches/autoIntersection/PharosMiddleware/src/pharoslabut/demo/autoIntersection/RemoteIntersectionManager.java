package pharoslabut.demo.autoIntersection;

import pharoslabut.navigate.*;
import pharoslabut.demo.autoIntersection.server.Robot;
import pharoslabut.demo.autoIntersection.server.UDPReceiver;
import pharoslabut.demo.autoIntersection.server.UDPSender;
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
	 * Set if server has given robot priority access through the intersection.
	 */
	private boolean access;
	
	/**
	 * Distance from "Approaching" line to "Entering Intersection" line. 
	 * In cm.
	 */
	public static final int distToIntersection_cm = 90;
	
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
	public void newLineFollowerEvent(LineFollowerEvent lfe, LineFollower follower) {
		
		UDPSender sender = new UDPSender(6665); // server to request access	
		UDPReceiver receiver = new UDPReceiver(this.serverPort, this.serverIP); // to receive from server
		
		switch(lfe.getType()) {
		case APPROACHING:
			log("Robot is approaching intersection!");
			//TODO Implement this... Ask server for permission to cross intersection.
			sender.send(new Robot( Integer.parseInt(pharoslabut.beacon.WiFiBeaconBroadcaster.getPharosIP()), "approaching" , ((long)(((distToIntersection_cm / LineFollower.MAX_SPEED) * 1000) + System.currentTimeMillis())), 0));
			//receiver.receive();
			break;
			
		case ENTERING:
			log("Robot is entering intersection!");
			//TODO Implement this... if approval has not been obtained, pause and wait 
			// for the approval to arrive (may need to query server again).
			while(!hasAccess()) {
				try {
					lf.stop();
					this.wait(100);
					sender.send(new Robot( Integer.parseInt(pharoslabut.beacon.WiFiBeaconBroadcaster.getPharosIP()), "at intersection" , System.currentTimeMillis(), 0));
					//receiver.receive();
					// wait until new message received
					// maybe put to sleep?
					// in receive thread have it notify once a message received?
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					// start robot back up once priority access received
					lf.start();
				}
			}
			// else go through intersection
			// need to send a new message to server?
			break;
			
		case EXITING:
			log("Robot is exiting intersection!");
			//TODO Implement this...Notify server that this robot is leaving the intersection
			sender.send(new Robot( Integer.parseInt(pharoslabut.beacon.WiFiBeaconBroadcaster.getPharosIP())));
			break;
			
		case ERROR:
			log("Received error from line follower!  Aborting demo.");
			lf.stop(); // There was an error, stop!
			break;
		}
	}

	/**
	 * @param access the access to set
	 */
	public void setAccess(boolean access) {
		this.access = access;
	}

	/**
	 * @return the access
	 */
	public boolean hasAccess() {
		return access;
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
