package pharoslabut.demo.mrpatrol2.behaviors;

import java.util.Enumeration;
import java.util.Vector;

//import pharoslabut.demo.mrpatrol2.config.ExpConfig;
import pharoslabut.logger.Logger;

/**
 * A top-level super-class for all behaviors.
 * 
 * @author Chien-Liang Fok
 */
public abstract class Behavior implements Runnable {

	/**
	 * The name of the behavior.
	 */
	private String name;
	
	/**
	 * Whether this behavior is started.
	 */
	private boolean started = false;
	
	/**
	 * The start time of this behavior.
	 */
	private long startTime = -1;
	
	/**
	 * All of the behaviors in this vector must be done before this
	 * behavior can run.
	 */
	private Vector<Behavior> prerequisites = new Vector<Behavior>();
	
	/**
	 * This behavior must terminate when all dependencies are done.
	 */
	private Vector<Behavior> dependencies = new Vector<Behavior>();
	
	/**
	 * The constructor.
	 * 
	 * @param name The name of the behavior.
	 */
	public Behavior(String name) {
		this.name = name;
	}
	
	/**
	 * Adds a prerequisite behavior to this behavior.  All prerequisite behaviors
	 * must be done prior to this behavior being able to start.
	 * 
	 * @param b The prerequisite behavior.
	 */
	public void addPrerequisite(Behavior b) {
		prerequisites.add(b);
	}
	
	/**
	 * Adds a dependency to this behavior.  Dependencies are behaviors that this behavior depends on.
	 * This behavior should terminate when all of these dependencies are done.  If at least one
	 * dependency behavior is not done, this behavior's dependency is met.
	 * 
	 * @param b The dependency behavior.
	 */
	public void addDependency(Behavior b) {
		dependencies.add(b);
	}
	
	/**
	 * Determines if the dependencies are met.
	 * 
	 * @return Whether all of the dependencies are met.
	 */
	protected boolean dependenciesMet() {
		if (dependencies.size() == 0)
			return true;
		
		Enumeration<Behavior> e = dependencies.elements();
		while (e.hasMoreElements()) {
			Behavior b = e.nextElement();
			if (!b.isDone())
				return true;
		}
		
		// There were dependencies and all of them are done.
		// Thus, this behavior did not meet all of the dependencies
		// and should terminate.
		return false;
	}
	
	/**
	 * 
	 * @return The name of this behavior.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * A behavior can start when its start condition is met.
	 * The start condition is defined by subclasses.
	 * 
	 * @return Whether this behavior can start.
	 */
	public CanStart canStart() {
		if (isDone())
			return new CanStart(false, "already done");
		
		if (started)
			return new CanStart(false, "currently running");
		
		Enumeration<Behavior> e = prerequisites.elements();
		while (e.hasMoreElements()) {
			Behavior b = e.nextElement();
			if (!b.isDone())
				return new CanStart(false, "prerequisite " + b.getName() + " not done.");
		}
		
		return new CanStart(true, "Can run");
	}
	
	/**
	 * After a behavior is done executing, this method should
	 * return true.  In all other circumstances, it should return false.
	 * 
	 * @return Whether this behavior is done.
	 */
	public abstract boolean isDone();
	
	/**
	 * Stops the behavior.  After calling this method, a call to isDone() must
	 * return true.  Once stop is called, the behavior's thread must run to completion.
	 * In addition, a stopped behavior cannot be started again.
	 */
	public abstract void stop();
	
	/**
	 * Starts this behavior running.
	 */
	public void start() {
		if (isDone()) {
			Logger.logErr("Attempting to start a behavior that is already done.  Ignoring command.");
		} else {
			started = true;
			startTime = System.currentTimeMillis();
			Logger.log("Behavior " + getName() + " starting at time " + startTime);
			new Thread(this).start();
		}
	}

	/**
	 * 
	 * @return The start time of this behavior.
	 */
	public long getStartTime() {
		return startTime;
	}
	
	/**
	 * @return A string representation of this class.
	 */
	@Override
	public String toString() {
		StringBuffer prereqStr = new StringBuffer("[");
		Enumeration<Behavior> e = prerequisites.elements();
		while (e.hasMoreElements()) {
			prereqStr.append(e.nextElement().getName() + ", ");
		}
		String pStr = prereqStr.toString();
		if (prerequisites.size() > 0)
			pStr = pStr.substring(0, pStr.length() - 2) + "]";
		else
			pStr += "]";
		return "name = " + name + ", canStart = " + canStart() + ", started = " + started + ", startTime = " + startTime + ", done = " + isDone() + ", prerequisites = " + pStr;
	}
	
	/**
	 * A container for whether a behavior can start and why.
	 * 
	 * @author Chien-Liang Fok
	 */
	public class CanStart {
		private boolean canStart;
		private String reason;
		
		public CanStart(boolean canStart, String reason) {
			this.canStart = canStart;
			this.reason = reason;
		}
		
		public boolean getCanStart() {
			return canStart;
		}
		
		public String getReason() {
			return reason;
		}
	}
}
