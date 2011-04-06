package pharoslabut.demo.simonsays;

import pharoslabut.io.Message;

public class PlayerControlMsg extends Message {
	private static final long serialVersionUID = -1173422675134836470L;
	PlayerControlCmd cmd;
	
	public PlayerControlMsg(PlayerControlCmd cmd) {
		this.cmd = cmd;
	}
	
	public PlayerControlCmd getCmd() {
		return cmd;
	}
	
	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
	}
	
	public String toString() {
		return "PlayerControlMsg, cmd=" + cmd;
	}
}
