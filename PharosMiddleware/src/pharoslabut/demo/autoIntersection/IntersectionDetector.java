package pharoslabut.demo.autoIntersection;

import java.util.Enumeration;
import java.util.Vector;

import pharoslabut.demo.autoIntersection.IntersectionEvent.IntersectionEventType;
import pharoslabut.logger.Logger;

/**
 * Detects when the robot is approaching, entering, and leaving the intersection.
 * 
 * @author Chien-Liang Fok
 */
public abstract class IntersectionDetector  {
	
	/**
	 * Keeps track of whether an event for the secondary blob was already generated.
	 * Prevents many secondary blob events from being generated by a single secondary
	 * blob.
	 */
	protected IntersectionEventType previousEventType = null;
	
	/**
	 * The listeners of intersection events.
	 */
	private Vector<IntersectionEventListener> listeners = new Vector<IntersectionEventListener>();
	
	/**
	 * The constructor.
	 */
	public IntersectionDetector() {
		
	}
	
	/**
	 * Adds a IntersectionEventListener to this object.
	 * 
	 * @param listener the IntersectionEventListener to add.
	 */
	public void addIntersectionEventListener(IntersectionEventListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Removes a IntersectionEventListener from this object.
	 * 
	 * @param listener the IntersectionEventListener to remove.
	 */
	public void removeListener(IntersectionEventListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Broadcasts the LineFollowerEvent a to all registered listeners.
	 * 
	 * @param ie The event to broadcast.
	 */
	protected void notifyListeners(final IntersectionEvent ie) {
		Logger.log("New IntersectionEvent: " + ie);
		Enumeration<IntersectionEventListener> e = listeners.elements();
		while(e.hasMoreElements()) {
			final IntersectionEventListener lfel = e.nextElement();
			new Thread(new Runnable() {
				public void run () {
					lfel.newIntersectionEvent(ie);
				}
			}).start();
		}
	}
	
	protected void genApproachingEvent() {
		
		if (previousEventType != null && previousEventType != IntersectionEventType.EXITING) {
			Logger.logErr("Got unexpected APPROACHING event, previous event = " + previousEventType);
		} else {
			Logger.log("APPROACHING Intersection!");
			previousEventType = IntersectionEventType.APPROACHING;
			IntersectionEvent lfe = new IntersectionEvent(IntersectionEventType.APPROACHING);
			notifyListeners(lfe);
		}
	}
	
	protected void genEnteringEvent() {
		
		if (previousEventType != IntersectionEventType.APPROACHING) {
			Logger.logErr("Got unexpected ENTERING event, previous event = " + previousEventType);
		} else {
			Logger.log("ENTERING Intersection!");
			previousEventType = IntersectionEventType.ENTERING;
			IntersectionEvent lfe = new IntersectionEvent(IntersectionEventType.ENTERING);
			notifyListeners(lfe);
		}
	}
	
	protected void genExitingEvent() {
		
		if (previousEventType != IntersectionEventType.ENTERING) {
			Logger.logErr("Got unexpected EXITING event, previous event = " + previousEventType);
		} else {
			Logger.log("EXITING Intersection!");
			previousEventType = IntersectionEventType.EXITING;
			IntersectionEvent lfe = new IntersectionEvent(IntersectionEventType.EXITING);
			notifyListeners(lfe);
		}
	}
}
