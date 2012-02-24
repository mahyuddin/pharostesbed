package pharoslabut.behavior;
import java.util.Vector;

import pharoslabut.behavior.management.WorldModel;

/**
 * A top-level super-class for all behaviors.
 * 
 * @author Noa Agmon
 * @author Chien-Liang Fok
 */
public abstract class Behavior {
	
	protected Vector<Behavior> _nextVector = new Vector<Behavior>();
	protected WorldModel _wm;
//	protected MissionData _misssiondata;
	protected int _behaveIndex;
	
	/**
	 * The constructor.
	 * 
	 * @param wm The world model of this node.
//	 * @param md The mission data.
	 */
	public Behavior(WorldModel wm) {
		_wm = wm;
//		_misssiondata = md;
	}
	public abstract boolean startCondition();
	public abstract boolean stopCondition();
	public abstract void action();
	/*This function will be called when behavior start condition is true and 
	 * the robot waits to its teammates to join the behavior
	 */
	
	public void start()
	{
		this.getClass().getSimpleName();
	}
	//Adding new behavior to the next vector
	public void addNext(Behavior beh)
	{
		_nextVector.add(beh);
	}
	
	/**
	 * Returning the first behavior that its start condition is true.
	 * This is simple sequential decision making. Can make more sophisticated decisions - 
	 * then in would be wise to move the getNext() function to the behavior itself 
	 * (making this an abstract function) 
	 */
	public Behavior getNext()
	{
		for(int i=0;i<_nextVector.size();i++)
			if(_nextVector.get(i).startCondition())
				return _nextVector.get(i);
		
		return null;
	}

	public void BehSetIndex(int myindex)
	{
		_behaveIndex = myindex;
	}
	
	public int BehGetIndex(){
		return _behaveIndex;
	}

	public String toString() {
		return getClass().getName();
	}
	
}
