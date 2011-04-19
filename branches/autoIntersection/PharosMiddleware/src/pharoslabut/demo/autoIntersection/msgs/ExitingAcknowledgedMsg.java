package pharoslabut.demo.autoIntersection.msgs;

import pharoslabut.io.*;

public class ExitingAcknowledgedMsg extends Msg implements Message {

	private static final long serialVersionUID = -5317453527956302211L;

	public ExitingAcknowledgedMsg(int robotID) {
		super(robotID);
	}
	
	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
	}
	
	public String toString() {
		return "ExitingAcknowledgedMsg";
	}

}
