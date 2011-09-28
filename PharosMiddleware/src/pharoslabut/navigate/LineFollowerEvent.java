package pharoslabut.navigate;

/**
 * This is created by the LineFollower whenever it detects an 
 * event as it follows the line.
 * 
 * @author Chien-Liang Fok
 *
 */
public class LineFollowerEvent {

	public enum LineFollowerEventType {APPROACHING, ENTERING, EXITING, ERROR};
	
	private LineFollowerEventType type;
	
	public LineFollowerEvent(LineFollowerEventType type) {
		this.type = type;
	}
	
	public LineFollowerEventType getType() {
		return type;
	}
	
	public String toString() {
		return "LineFollowerEvent, type=" + type;
	}
}
