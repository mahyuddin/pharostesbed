package pharoslabut.behavior;

//import java.net.InetAddress;

import pharoslabut.io.*;

/**
 * This message notifies teammates of the current status of the sender.
 * It specifies which behavior the sender is currently executing.
 * 
 * @author Noa Agmon
 */
public class MultiRobotBehaveMsg implements AckedMsg {
		
		private static final long serialVersionUID = -7631305555004386678L;
		
		private String _behaveName;
		private int _robotID; 
//		private InetAddress _replyAddress;
//		private int _replyPort;
		private int _behaveID;

		/**
		 * The constructor.
		 * 
		 * @param behavename The name of the behavior being executed.
		 * @param behaveID The ID of the behavior being executed.
		 * @param myID The ID of the sender.
		 */
		public MultiRobotBehaveMsg(String behavename, int behaveID, int myID) {
			_behaveName = new String(behavename);
			_behaveID = behaveID;
			_robotID = myID;
//			_replyAddress = replyAddress;
//			_replyPort = replyPort;
		}
		
		public String getBehaveName() {
			return _behaveName;
		}
		
		public int getBehaveID(){
			return _behaveID;
		}
		
		public int getRobotID(){
			return _robotID;
		}
		
		@Override
		public MsgType getType() {
			return MsgType.UPDATE_BEH_MSG;
		}
		
		public String toString() {
			return "MultiRobotBehaveMsg: behaveName=" + _behaveName + ", behaveID=" + _behaveID + ", myID=" + _robotID;
		}

//		@Override
//		public int getPort() {
//			return _replyPort;
//		}
//
//		@Override
//		public InetAddress getReplyAddr() {
//			return _replyAddress;
//		}
//
//		@Override
//		public void setPort(int port) {
//			_replyPort = port;
//			
//		}
//
//		@Override
//		public void setReplyAddr(InetAddress address) {
//			_replyAddress = address;
//		}
}