package pharoslabut.demo.autoIntersection.server;

import pharoslabut.demo.autoIntersection.intersectionSpecs.IntersectionSpecs;

/**
 * Implements a parallel intersection management server.
 * This means multiple robots may traverse the intersection at a time without
 * colliding.  For example, two robots traveling in opposite directions along the 
 * same road can proceed simultaneously.
 * 
 * @author Chien-Liang Fok
 */
public class ParallelDaemon extends ServerDaemon {

	/**
	 * The constructor.
	 * 
	 * @param intersectionSpecs The intersection specifications.
	 * @param serverPort The port on which to listen.
	 */
	public ParallelDaemon(IntersectionSpecs intersectionSpecs, int serverPort) {
		super(intersectionSpecs, serverPort);
	}
	
	public String toString() {
		return getClass().getName();
	}

	@Override
	public synchronized void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
	}
}
