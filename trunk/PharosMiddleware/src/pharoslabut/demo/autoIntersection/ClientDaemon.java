package pharoslabut.demo.autoIntersection;

import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionEventType;
import pharoslabut.demo.autoIntersection.msgs.AutoIntersectionMsg;

/**
 * The top-level class of all client daemons.
 * 
 * @author Chien-Liang Fok
 */
public abstract class ClientDaemon {
	
	/**
	 * The current state of the client daemon.  It is initialized to IDLE.
	 */
	protected IntersectionEventType currState = IntersectionEventType.IDLE;
	
	/**
	 * Starts the daemon running.
	 */
	public abstract void start();
	
	/**
	 * Called whenever an autonomous intersection message is received.
	 * 
	 * @param msg The received message.
	 */
	public abstract void messageReceived(AutoIntersectionMsg msg);
}
