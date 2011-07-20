package pharoslabut.behavior;

import pharoslabut.io.Message;

public class MultiRobotBehaveMsg implements Message{
		
		private static final long serialVersionUID = -7631305555004386678L;
		
		private String _behaveName;
		
		public MultiRobotBehaveMsg(String behavename) {
			_behaveName = new String(behavename);
		}
		
		public String GetBehaveName() {
			return _behaveName;
		}
		
		@Override
		public MsgType getType() {
			return MsgType.UPDATE_BEH_MSG;
		}
}