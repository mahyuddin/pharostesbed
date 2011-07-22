package pharoslabut.behavior;

import java.net.InetAddress;

import pharoslabut.io.*;

public class MultiRobotBehaveMsg implements AckableMessage{
		
		private static final long serialVersionUID = -7631305555004386678L;
		
		private String _behaveName;
		int _robotID;
		InetAddress _replyAddress;
		int _replyPort;
		
		public MultiRobotBehaveMsg(String behavename, int myID, InetAddress replyAddress, int replyPort) {
			_behaveName = new String(behavename);
			_robotID = myID;
			_replyAddress = replyAddress;
			_replyPort = replyPort;
		}
		
		public String GetBehaveName() {
			return _behaveName;
		}
		
		public int GetRobotID(){
			return _robotID;
		}
		
		@Override
		public MsgType getType() {
			return MsgType.UPDATE_BEH_MSG;
		}

		@Override
		public int getPort() {
			return _replyPort;
		}

		@Override
		public InetAddress getReplyAddr() {
			return _replyAddress;
		}

		@Override
		public void setPort(int port) {
			_replyPort = port;
			
		}

		@Override
		public void setReplyAddr(InetAddress address) {
			_replyAddress = address;
		}
}