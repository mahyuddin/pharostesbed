package pharoslabut.demo.simonsays.io;

import java.net.InetAddress;

import pharoslabut.io.AckableMessage;

public class CameraTiltMsg implements AckableMessage {
	private static final long serialVersionUID = 3948105907393563361L;
	private double tiltAngle;
	private InetAddress address;
	private int port;
	
	public CameraTiltMsg(double tiltAngle) {
		this.tiltAngle = tiltAngle;
	}
	
	/**
	 * Returns the desired tilt angle.
	 * 
	 * @return the tilt angle in degrees.
	 */
	public double getTiltAngle() {
		return tiltAngle;
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
		return "CameraTiltMsg, tiltAngle=" + tiltAngle;
	}
}
