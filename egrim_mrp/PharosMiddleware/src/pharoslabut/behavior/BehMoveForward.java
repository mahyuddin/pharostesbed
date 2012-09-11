package pharoslabut.behavior;

import pharoslabut.behavior.management.WorldModel;

// TODO Implement this!
public class BehMoveForward extends Behavior{
	
	public BehMoveForward(WorldModel wm) {
		// TODO Auto-generated constructor stub
		super(wm);
	}

	@Override
	public boolean startCondition() {
		// TODO Auto-generated method stub
		_wm.resetCount(); //setCount(0);
		return true;
	}

	@Override
	public boolean stopCondition() {
		// TODO Auto-generated method stub
		if(_wm.getCount() < 10)
		{
			_wm.incCount(); //setCount(_wm.getCount()+1);
			return false;
		}
		return true;
	}

	@Override
	public void action() {
		// TODO Auto-generated method stub
		System.out.println("Action: BehMoveForward");
	}

}
