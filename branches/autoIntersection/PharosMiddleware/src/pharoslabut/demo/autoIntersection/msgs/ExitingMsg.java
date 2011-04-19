package pharoslabut.demo.autoIntersection.msgs;

import pharoslabut.io.*;

public class ExitingMsg extends Msg implements Message {
	
	private static final long serialVersionUID = -5699029124573209933L;

	public ExitingMsg(int robotID) {
		super(robotID);
	}
	
	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
	}
	
	public String toString() {
		return "ExitingMsg";
	}

}
