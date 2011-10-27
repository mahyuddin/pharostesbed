package pharoslabut.demo.autoIntersection.msgs;

import java.net.InetAddress;

import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionEvent;

/**
 * A message sent between the AutoIntersectionClient and
 * AutoIntersectionServer for debugging purposes.
 * 
 * @author Chien-Liang Fok
 */
public class AutoIntDebugMsg extends AutoIntersectionMsg {

	private static final long serialVersionUID = 4357298436618850099L;

	private IntersectionEvent ie;
	
	public AutoIntDebugMsg(InetAddress robotIP, int robotPort, IntersectionEvent ie) {
		super(robotIP, robotPort);
		this.ie = ie;
	}
	
	public IntersectionEvent getIE() {
		return ie;
	}
	
	public String toString() {
		return getClass().getName() + ": Intersection event: " + ie;
	}
}
