package pharoslabut.behavior;

import pharoslabut.behavior.management.WorldModel;

// TODO Implement this!
public class BehLeftTrun extends Behavior{

	public BehLeftTrun(WorldModel wm, MissionData missiondata) {
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
	//If you want your team to be synchronized in this behavior implement this function.
	public void waitToTeam()
	{
		while(_wm.isTeamSynchronized() == false)
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}

	@Override
	public void action() {
		// TODO Auto-generated method stub
		System.out.println("Action: BehLeftTurn");
	}
}
