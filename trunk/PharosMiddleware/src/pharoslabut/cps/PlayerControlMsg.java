package pharoslabut.cps;

import java.net.InetAddress;

import pharoslabut.io.AckableMessage;

public class PlayerControlMsg implements AckableMessage {
	private static final long serialVersionUID = -1173422675134836470L;
	private PlayerControlCmd cmd;
	private InetAddress address;
	private int port;
	
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
	
	@Override
	public void setReplyAddr(InetAddress address) {
		this.address = address;
	}

	@Override
	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public InetAddress getReplyAddr() {
		return address;
	}

	@Override
	public int getPort() {
		return port;
	}
	
	public String toString() {
		return "PlayerControlMsg, cmd=" + cmd;
	}
}
