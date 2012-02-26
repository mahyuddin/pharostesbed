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
	
//	/**
//	 * The experiment configuration.
//	 */
//	protected ExpConfig expConfig;

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
	 * The constructor.
	 * 
	 * @param name The name of the behavior.
	 */
	public Behavior(String name) {
		this.name = name;
//		this.expConfig = expConfig;
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
	public boolean canStart() {
		if (started || isDone()) {
			Logger.logDbg("Behavior " + name + " cannot start b/c it was already started or is already done.");
			return false;
		}
		
		Enumeration<Behavior> e = prerequisites.elements();
		while (e.hasMoreElements()) {
			Behavior b = e.nextElement();
			if (!b.isDone()) {
				Logger.logDbg("Behavior " + name + " cannot start b/c prerequisite behavior " + b.getName() + " is not done.");
				return false;
			}
		}
		Logger.logDbg("Behavior " + name + " can start!");
		return true;
	}
	
	/**
	 * After a behavior is done executing, this method should
	 * return true.  In all other circumstances, it should return false.
	 * 
	 * @return Whether this behavior is done.
	 */
	public abstract boolean isDone();
	
	/**
	 * Starts this behavior running.
	 */
	public void start() {
		started = true;
		startTime = System.currentTimeMillis();
		Logger.log("Behavior " + getName() + " starting at time " + startTime);
		new Thread(this).start();
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
		String pStr = prereqStr.substring(0, prereqStr.length()-2) + "]";
		return "name = " + name + ", canStart = " + canStart() + ", started = " + started + ", startTime = " + startTime + ", done = " + isDone() + ", prerequisites = " + pStr;
	}
}
