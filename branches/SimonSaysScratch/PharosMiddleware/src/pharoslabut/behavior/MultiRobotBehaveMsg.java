package pharoslabut.behavior;

import pharoslabut.behavior.management.WorldModel;
import pharoslabut.io.*;

/**
 * This message notifies team members of the current status of the sender.
 * It specifies which behavior the sender is currently executing.
 * 
 * @author Noa Agmon
 */
public class MultiRobotBehaveMsg implements AckedMsg {
		
		private static final long serialVersionUID = -7631305555004386678L;
		
		private String _behaveName;
		private int _robotID;
		private int _behaveID;

		/**
		 * The constructor.
		 * 
		 * @param wm - world model to extract from it the relevant information.
		 */
		public MultiRobotBehaveMsg(WorldModel wm) {
			_behaveName = new String(wm.getCurrentBehaviorName());
			_behaveID = wm.getCurrentBehaviorID();
			_robotID = wm.getMyIndex();
		}
		
		public String getBehaviorName() {
			return _behaveName;
		}
		
		public int getBehaviorID(){
			return _behaveID;
		}
		
		public int getRobotID(){
			return _robotID;
		}
		
		public MsgType getType() {
			return MsgType.UPDATE_BEH_MSG;
		}
		
		public String toString() {
			return "MultiRobotBehaveMsg: behaveName=" + _behaveName + ", behaveID=" + _behaveID + ", myID=" + _robotID;
		}
}