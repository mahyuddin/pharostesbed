package pharoslabut.behavior;

import java.net.InetAddress;

import pharoslabut.io.*;

public class MultiRobotBehaveMsg implements AckableMessage{
		
		private static final long serialVersionUID = -7631305555004386678L;
		
		private String _behaveName;
		private int _robotID; 
		private InetAddress _replyAddress;
		private int _replyPort;
		private int _behaveID;

		public MultiRobotBehaveMsg(String behavename, int behaveID, int myID, InetAddress replyAddress, int replyPort) {
			_behaveName = new String(behavename);
			_behaveID = behaveID;
			_robotID = myID;
			_replyAddress = replyAddress;
			_replyPort = replyPort;
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