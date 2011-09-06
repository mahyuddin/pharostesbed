package pharoslabut.demo.autoIntersection;

/**
 * This is generated when the robot detects that it is entering or 
 * leaving the intersection.
 * 
 * @author Chien-Liang Fok
 *
 */
public class IntersectionEvent {

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
