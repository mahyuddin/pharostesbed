package pharoslabut.demo.autoIntersection2;

import pharoslabut.io.Message;

public class StartExpMsg implements Message {

	private static final long serialVersionUID = -4986533524890773140L;
	
//	private String expName;
	
	public StartExpMsg() {
	}
	
//	public StartExpMsg(String expName) {
//		this.expName = expName;
//	}
	
//	public String getExpName() {
//		return expName;
//	}

	@Override
	public MsgType getType() {
		return MsgType.STARTEXP;
	}
	
	public String toString() {
		return getClass().getName();
	}

}
