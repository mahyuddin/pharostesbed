package pharoslabut.demo.simonsays;

import pharoslabut.io.Message;

/**
 * This is sent by the Demo Server to the Demo Client informing it that it is done
 * executing the previous command.
 * 
 * @author Chien-Liang Fok
 */
public class CmdDoneMsg extends Message {
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
