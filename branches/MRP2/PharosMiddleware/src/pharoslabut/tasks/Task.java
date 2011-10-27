package pharoslabut.tasks;

/**
 * The abstract parent class of all tasks used in the Pharos Middleware.
 * 
 * @author Chien-Liang Fok
 *
 */
public abstract class Task implements java.io.Serializable {
//	public static enum Priority {FIRST, SECOND, THIRD, FOURTH, FIFTH, SIXTH, SEVENTH, EIGHTH, NINTH, TENTH};
	
	private static final long serialVersionUID = 7952640754776450847L;
	
	private Priority priority;
	
	public Task(Priority p) {
		this.priority = p;
	}
	
	public Priority getPriority() {
		return priority;
	}
	
	public boolean isEqualPriorityTo(Task t) {
		return priority.compareTo(t.getPriority()) == 0;
	}
	
	public boolean isHigherPriorityThan(Task t) {
		return priority.compareTo(t.getPriority()) < 0;
	}
}
