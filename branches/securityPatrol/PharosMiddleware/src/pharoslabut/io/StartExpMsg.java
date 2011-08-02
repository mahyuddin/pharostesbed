package pharoslabut.io;

import pharoslabut.experiment.ExpType;

/**
 * This is sent by the PharosExpClient to the PharosExpServer to start
 * an experiment.
 * 
 * @author Chien-Liang Fok
 */
public class StartExpMsg implements AckedMsg {

	private static final long serialVersionUID = -2260269827599336883L;

	private String expName;
	private ExpType expType;
	private int delay;
	
	/**
	 * A constructor for a message containing zero delay.
	 * 
	 * @param expName The name of the experiment.
	 * @param expType The type of the experiment.
	 */
	public StartExpMsg(String expName, ExpType expType) {
		this(expName, expType, 0);
	}
	
	/**
	 * A constructor for a fully-defined message.
	 * 
	 * @param expName The name of the experiment.
	 * @param expType The type of the experiment.
	 * @param delay The pause time in milliseconds before the robot starts to follow the motion script.
	 */
	public StartExpMsg(String expName, ExpType expType, int delay) {
		this.expName = expName;
		this.expType = expType;
		this.delay = delay;
	}
	
	
	/**
	 * @return The name of the experiment.
	 */
	public String getExpName() {
		return expName;
	}
	
	/**
	 * @return The experiment type.
	 */
	public ExpType getExpType() {
		return expType;
	}
	
	/**
	 * @return The pause time in milliseconds before the robot starts to follow the motion script.
	 */
	public int getDelay() {
		return delay;
	}
	
	@Override
	public MsgType getType() {
		return MsgType.STARTEXP;
	}
	
	/**
	 * @return A string representation of this class.
	 */
	public String toString() {
		return "START_EXP_MSG exp_name=" + expName + ", expType=" + expType + ", delay=" + delay;
	}
}
