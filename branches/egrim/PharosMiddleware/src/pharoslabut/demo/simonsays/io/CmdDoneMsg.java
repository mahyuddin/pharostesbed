package pharoslabut.demo.simonsays.io;

import pharoslabut.demo.simonsays.SimonSaysClient;
import pharoslabut.demo.simonsays.SimonSaysServer;
import pharoslabut.io.Message;

/**
 * This message is sent by the SimonSaysServer to the SimonSaysClient telling
 * it that it is done executing the previous command.
 * 
 * @author Chien-Liang Fok
 * @see SimonSaysClient
 * @see SimonSaysServer
 */
public class CmdDoneMsg implements Message {
	private static final long serialVersionUID = 4417385349057427024L;
	private boolean success;
	
	/**
	 * The constructor.
	 * 
	 * @param success Whether the operation was successful.
	 */
	public CmdDoneMsg(boolean success) {
		this.success = success;
	}
	
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	public boolean getSuccess() {
		return success;
	}
	
	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
	}
	
	public String toString() {
		return "CmdDoneMsg, success = " + success;
	}
}
