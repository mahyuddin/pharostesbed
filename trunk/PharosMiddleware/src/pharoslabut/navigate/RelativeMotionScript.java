package pharoslabut.navigate;

import pharoslabut.tasks.MotionTask;
import java.util.*;

/**
 * A RelativeMotionScript consists of a list of <speed ,direction, time> tuples that
 * control how the robots move.
 * 
 * @author Chien-Liang Fok
 */
public class RelativeMotionScript implements java.io.Serializable {
	
	private static final long serialVersionUID = -3329801696365597192L;

	Vector<MotionTask> motionTasks;
	Vector<Long> durations;
	
	public RelativeMotionScript() {
		motionTasks = new Vector<MotionTask>();
		durations = new Vector<Long>();
	}
	
	public void addMotionTask(MotionTask mt, long duration) {
		motionTasks.add(mt);
		durations.add(new Long(duration));
	}
	
	public int getNumSteps() {
		return motionTasks.size();
	}
	
	public MotionTask getMotionTask(int indx) {
		return motionTasks.get(indx);
	}
	
	public long getDuration(int indx) {
		return durations.get(indx);
	}
}
