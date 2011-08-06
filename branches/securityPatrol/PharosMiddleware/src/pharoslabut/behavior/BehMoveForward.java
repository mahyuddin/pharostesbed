package pharoslabut.behavior;

import pharoslabut.behavior.management.WorldModel;

// TODO Implement this!
public class BehMoveForward extends Behavior{
	
	public BehMoveForward(WorldModel wm, MissionData missiondata) {
		// TODO Auto-generated constructor stub
		super(wm, missiondata);
	}

	@Override
	public boolean startCondition() {
		// TODO Auto-generated method stub
		_wm.setCount(0);
		return true;
	}

	@Override
	public boolean stopCondition() {
		// TODO Auto-generated method stub
		if(_wm.getCount() < 10)
		{
			_wm.setCount(_wm.getCount()+1);
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
