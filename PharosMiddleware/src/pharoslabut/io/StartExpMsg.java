package pharoslabut.io;

import pharoslabut.ExpType;

public class StartExpMsg implements Message {

	private static final long serialVersionUID = -2260269827599336883L;

	private String expName;
	private String robotName;
	private ExpType expType;
	
	public StartExpMsg(String expName, String robotName, ExpType expType) {
		this.expName = expName;
		this.robotName = robotName;
		this.expType = expType;
	}
	
	public String getRobotName() {
		return robotName;
	}
	
	public String getExpName() {
		return expName;
	}
	
	public ExpType getExpType() {
		return expType;
	}
	
	@Override
	public MsgType getType() {
		return MsgType.STARTEXP;
	}
}
