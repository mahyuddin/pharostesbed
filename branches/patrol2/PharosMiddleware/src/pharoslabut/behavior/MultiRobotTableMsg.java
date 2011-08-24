package pharoslabut.behavior;

import pharoslabut.behavior.management.WorldModel;
import pharoslabut.io.*;
import pharoslabut.io.Message.MsgType;

public class MultiRobotTableMsg implements AckedMsg  {
	
	private int[] _behaviorIDList;
	private String[] _behaviorNameList;
	private int _senderRobotID;
	
	
	public MultiRobotTableMsg(WorldModel wm) {
		_behaviorIDList = new int[wm.getTeamSize()];
		_behaviorNameList = new String[wm.getTeamSize()];
		_senderRobotID = wm.getMyIndex();
		
		wm.copyWM(_behaviorIDList, _behaviorNameList);
	}
	
	public String getBehaviorName(int index){
		return _behaviorNameList[index];
	}
	
	public int getSenderID(){
		return _senderRobotID;
	}
	public int getBehaviorID(int index){
		return _behaviorIDList[index];
	}

	public int getTableSize(){
		return _behaviorIDList.length;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -4838033113752427054L;
	
	public MsgType getType() {
		return MsgType.UPDATE_BEH_TABLE_MSG;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getName() + ", Sender RobotID: " + _senderRobotID + "\nRobot ID\tBehaviorID\tBeahviorName\n");
		for (int i=0; i < _behaviorIDList.length; i++) {
			sb.append(i + "\t" + _behaviorIDList[i] + "\t" + _behaviorNameList[i] + "\n");
		}
		return sb.toString();
	}
}
