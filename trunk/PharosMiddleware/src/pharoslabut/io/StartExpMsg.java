package pharoslabut.io;

import pharoslabut.ExpType;

public class StartExpMsg implements Message {

	private static final long serialVersionUID = -2260269827599336883L;

	private String expName;
	private String robotName;
	private ExpType expType;
	
	/**
	 * The pause time in milliseconds before the robot starts to follow the motion script.
	 */
	private int delay;
	
	public StartExpMsg(String expName, String robotName, ExpType expType, int delay) {
		this.expName = expName;
		this.robotName = robotName;
		this.expType = expType;
		this.delay = delay;
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
	
	public int getDelay() {
		return delay;
	}
	
	@Override
	public MsgType getType() {
		return MsgType.STARTEXP;
	}
}
