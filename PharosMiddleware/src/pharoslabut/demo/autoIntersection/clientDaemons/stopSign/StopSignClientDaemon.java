package pharoslabut.demo.autoIntersection.clientDaemons.stopSign;

import java.net.InetAddress;

import pharoslabut.demo.autoIntersection.clientDaemons.V2I.V2IClientDaemon;
import pharoslabut.demo.autoIntersection.clientDaemons.V2I.GrantAccessMsg;
import pharoslabut.demo.autoIntersection.clientDaemons.V2I.RequestAccessMsg;
import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionDetector;
import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionEventType;
import pharoslabut.demo.autoIntersection.msgs.AutoIntersectionMsg;
import pharoslabut.exceptions.PharosException;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.LineFollower;
import pharoslabut.sensors.Position2DBuffer;

/**
 * Navigates across an intersection by mimicking a stop sign.  It does this by ignoring
 * the approaching event and actually stopping at the entrance marker.  Then, it communicates
 * with a central server running in parallel model to gain access to the intersection in 
 * a FCFS manner.
 * 
 * @author Chien-Liang Fok
 */
public class StopSignClientDaemon extends V2IClientDaemon {
	
	
	/**
	 * The constructor.
	 * 
	 * @param serverIP The IP address of the intersection management server.
	 * @param serverPort The port of the intersection management server.
	 * @param port The local port on which this client should listen.
	 * @param lineFollower The line follower.
	 * @param intersectionDetector The intersection detector.
	 * @param entryPointID The ID of the entry point.
	 * @param exitPointID The ID of the exit point.
	 */
	public StopSignClientDaemon(InetAddress serverIP, int serverPort, int port,
			LineFollower lineFollower, IntersectionDetector intersectionDetector, Position2DBuffer pos2DBuffer,
			String entryPointID, String exitPointID) 
	{
		super(serverIP, serverPort, port, lineFollower, intersectionDetector, pos2DBuffer, entryPointID, exitPointID);
	}
	
	@Override
	public void messageReceived(AutoIntersectionMsg msg) {
		if (msg instanceof GrantAccessMsg) {
//			GrantAccessMsg grantMsg = (GrantAccessMsg)msg;
			
			accessGranted = true;
			
			if (currState == IntersectionEventType.ENTERING) {
				Logger.log("Received grant message, resuming robot movement.");
				lineFollower.unpause();
			}
			else {
				Logger.logErr("Received unexpected grant message, currState = " + currState);
			}
		}
		
	}
	
	@Override
	public void run() {
		Logger.log("Thread starting...");
		
		Logger.log("Starting the line follower.");
		lineFollower.start();
		
		while(isRunning) {
			
			if (currState == IntersectionEventType.ENTERING)
			{
				
				if (!accessGranted) {

					long currTime = System.currentTimeMillis();
					long timeSinceLastReq = currTime - lastRequestTime;
					if (timeSinceLastReq > REQUEST_TIMEOUT) {
						Logger.log("Sending request to server.");
						RequestAccessMsg requestMsg = new RequestAccessMsg(ip, port, 
								entryPointID, exitPointID);

						Logger.log("Sending request access message to server.");
						try {
							msgSender.sendMessage(serverIP, serverPort, requestMsg);
						} catch (PharosException e) {
							Logger.logErr(e.toString());
							e.printStackTrace();
						}
						
						lastRequestTime = currTime;
					}
				}
			}
			
			synchronized(this) {
				try {
					this.wait(CYCLE_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		Logger.log("Thread terminating...");
		System.exit(0);
	}
}
