package pharoslabut.demo.autoIntersection.server;

import pharoslabut.demo.autoIntersection.intersectionSpecs.IntersectionSpecs;

/**
 * The top-level class of all server daemons.
 * 
 * @author Chien-Liang Fok
 */
public abstract class ServerDaemon {

	/**
	 * The server's port.
	 */
	protected int serverPort;
	
	/**
	 * The intersection specifications.
	 */
	protected IntersectionSpecs intersectionSpecs;
	
	/**
	 * The constructor.
	 * 
	 * @param intersectionSpecs The intersection specifications.
	 * @param serverPort The server's port.
	 */
	public ServerDaemon(IntersectionSpecs intersectionSpecs, int serverPort) {
		this.intersectionSpecs = intersectionSpecs;
		this.serverPort = serverPort;
	}
	
	/**
	 * Starts the daemon.
	 */
	public abstract void start();
	
	/**
	 * Stops this daemon.
	 */
	public abstract void stop();
}
