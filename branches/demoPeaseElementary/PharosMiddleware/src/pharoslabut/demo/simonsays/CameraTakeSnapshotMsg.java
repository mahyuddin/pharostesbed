package pharoslabut.demo.simonsays;

import pharoslabut.io.Message;


/**
 * This message is sent to the Demo Server to initiate the process
 * of taking an image and sending it to the Demo Client.
 * 
 * @author Chien-Liang Fok
 *
 */
public class CameraTakeSnapshotMsg extends Message {
	private static final long serialVersionUID = 2955823645520677730L;

	public CameraTakeSnapshotMsg() {
		
	}
	
	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
	}
	
}
