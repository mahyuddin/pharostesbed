package pharoslabut.tasks;

/**
 * The abstract parent class of all tasks used in the Pharos Middleware.
 * 
 * @author Chien-Liang Fok
 */
public abstract class Task implements java.io.Serializable {
	
	private static final long serialVersionUID = 7952640754776450847L;
	
	private Priority priority;
	
	public Task(Priority p) {
		this.priority = p;
	}
	
	public synchronized void setPriority(Priority priority) {
		this.priority = priority;
	}
	
	public synchronized Priority getPriority() {
		return priority;
	}
	
	public boolean isEqualPriorityTo(Task t) {
		return priority.compareTo(t.getPriority()) == 0;
	}
	
	public boolean isHigherPriorityThan(Task t) {
		return priority.compareTo(t.getPriority()) < 0;
	}
}
