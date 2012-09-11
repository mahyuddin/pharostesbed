package pharoslabut.demo.autoIntersection;

import java.io.Serializable;

/**
 * This is generated when the robot detects that it is entering or 
 * leaving the intersection.
 * 
 * @author Chien-Liang Fok
 *
 */
public class IntersectionEvent implements Serializable {

	private static final long serialVersionUID = -6535896318270525624L;

	public enum IntersectionEventType {APPROACHING, ENTERING, EXITING, ERROR};
	
	private IntersectionEventType type;
	
	public IntersectionEvent(IntersectionEventType type) {
		this.type = type;
	}
	
	public IntersectionEventType getType() {
		return type;
	}
	
	public String toString() {
		return "LineFollowerEvent, type=" + type;
	}
}
