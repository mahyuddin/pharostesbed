package pharoslabut.demo.irobotcam;

import pharoslabut.io.Message;

public class ResetPlayerMsg extends Message {
	private static final long serialVersionUID = -571214661754671198L;

	public ResetPlayerMsg() {
		
	}
	
	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
	}
}
