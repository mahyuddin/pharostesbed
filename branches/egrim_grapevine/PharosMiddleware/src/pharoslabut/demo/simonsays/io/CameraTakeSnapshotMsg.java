package pharoslabut.demo.simonsays.io;

import java.net.InetAddress;

import pharoslabut.demo.simonsays.SimonSaysClient;
import pharoslabut.demo.simonsays.SimonSaysServer;
import pharoslabut.io.AckableMessage;


/**
 * This message is sent by the SimonSaysClient to the SimonSaysServer telling it
 * to take a picture and send it back.  The SimonSaysServer should reply 
 * with a CameraSnapshotMsg containing the image.
 * 
 * @author Chien-Liang Fok
 * @see SimonSaysClient
 * @see SimonSaysServer
 * @see CameraSnapshotMsg
 */
public class CameraTakeSnapshotMsg implements AckableMessage {
	private static final long serialVersionUID = 2955823645520677730L;

	private InetAddress address;
	private int port;
	
	public CameraTakeSnapshotMsg() {
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
		return "CameraTakeSnapshotMsg";
	}
}
