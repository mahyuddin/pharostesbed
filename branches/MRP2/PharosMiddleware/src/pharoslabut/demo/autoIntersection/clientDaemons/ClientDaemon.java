package pharoslabut.demo.autoIntersection.clientDaemons;

import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionDetector;
import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionEventType;
import pharoslabut.demo.autoIntersection.msgs.AutoIntersectionMsg;
import pharoslabut.navigate.LineFollower;
import pharoslabut.sensors.Position2DBuffer;

/**
 * The top-level class of all client daemons.
 * 
 * @author Chien-Liang Fok
 */
public abstract class ClientDaemon {
	/**
	 * The cycle time of this daemon in milliseconds.
	 */
	public static final long CYCLE_TIME = 100;
	
	/**
	 * The current state of the client daemon.  It is initialized to IDLE.
	 */
	protected IntersectionEventType currState = IntersectionEventType.IDLE;
	
	/**
	 * Makes the robot follow the line, which denotes a lane.
	 */
	protected LineFollower lineFollower;
	
	/**
	 * Detects the intersection.
	 */
	protected IntersectionDetector intersectionDetector;
	
	/**
	 * A buffer for position 2d information.   This includes the robot's odometry data.
	 */
	protected Position2DBuffer pos2DBuffer;
	
	/**
	 * The ID of the entry point into the intersection.
	 */
	protected String entryPointID;
	
	/**
	 * The ID of the exit point from the intersection.
	 */
	protected String exitPointID;
	
	/**
	 * The constructor.
	 * 
	 * @param lineFollower The line follower.
	 * @param intersectionDetector The intersection detector.
	 * @param pos2DBuffer The position 2d buffer.
	 * @param entryPointID The entry point ID.
	 * @param exitPointID The exit point ID.
	 */
	public ClientDaemon(LineFollower lineFollower, IntersectionDetector intersectionDetector, 
			Position2DBuffer pos2DBuffer,
			String entryPointID, String exitPointID) 
	{
		this.lineFollower = lineFollower;
		this.intersectionDetector = intersectionDetector;
		this.pos2DBuffer = pos2DBuffer;
		this.entryPointID = entryPointID;
		this.exitPointID = exitPointID;
	}
	
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
