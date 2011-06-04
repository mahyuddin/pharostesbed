package pharoslabut.navigate;

import pharoslabut.tasks.MotionTask;
import java.util.*;

/**
 * A RelativeMotionScript consists of a list of <speed, direction, time> tuples that
 * control how the robots move.
 * 
 * @author Chien-Liang Fok
 */
public class RelativeMotionScript implements java.io.Serializable {
	
	private static final long serialVersionUID = -3329801696365597192L;

	Vector<MotionTask> motionTasks = new Vector<MotionTask>();
	Vector<Long> durations = new Vector<Long>();
	
	/**
	 * The constructor.
	 */
	public RelativeMotionScript() {
	}
	
	/**
	 * Add a motion task to the script.
	 * 
	 * @param mt The motion task to add.
	 * @param duration The duration of movement in milliseconds.
	 */
	public void addMotionTask(MotionTask mt, long duration) {
		motionTasks.add(mt);
		durations.add(new Long(duration));
	}
	
	/**
	 * 
	 * @return The number of motion tasks in this script.
	 */
	public int getNumSteps() {
		return motionTasks.size();
	}
	
	/**
	 * 
	 * @param indx The index of the task.
	 * @return The task at the specified index.
	 */
	public MotionTask getMotionTask(int indx) {
		return motionTasks.get(indx);
	}
	
	/**
	 * 
	 * @param indx The index of the task
	 * @return The duration to run the specified task.
	 */
	public long getDuration(int indx) {
		return durations.get(indx);
	}
	
	/**
	 * @return A string representation of this class.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer("RelativeMotionScript:");
		for (int i=0; i < motionTasks.size(); i++) {
			sb.append("\t" + (i+1) + "\t" + motionTasks.get(i) + "\t" + durations.get(i) + "\n");
		}
		return sb.toString();
	}
}
