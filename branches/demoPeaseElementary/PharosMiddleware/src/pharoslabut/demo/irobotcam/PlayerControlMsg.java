package pharoslabut.demo.irobotcam;

import pharoslabut.io.Message;

public class PlayerControlMsg extends Message {
    
	PlayerControlCmd cmd;
	
	private static final long serialVersionUID = -2277264706385493797L;

	
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
}
