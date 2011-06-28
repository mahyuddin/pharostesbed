package pharoslabut.behavior;
import java.util.Vector;

import pharoslabut.behavior.management.WorldModel;




public abstract class Behavior {
	protected Vector<Behavior> _nextVector;
	protected WorldModel _wm;
	
	public Behavior(WorldModel wm) {
		// TODO Auto-generated constructor stub
		_nextVector = new Vector<Behavior>();
		_wm = wm;
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
	/*returning the first behavior that its start condition is true.
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
	public void waitToTeam()
	{
		while(_wm.isTeamSynchronized() == false)
			try {
				Thread.currentThread().sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
}
