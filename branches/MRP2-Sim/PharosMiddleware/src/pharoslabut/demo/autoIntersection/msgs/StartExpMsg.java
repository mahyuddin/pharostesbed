package pharoslabut.demo.autoIntersection.msgs;

import pharoslabut.demo.autoIntersection.ExpType;
import pharoslabut.io.Message;
//import pharoslabut.io.Message.MsgType;

/**
 * This is the parent class of all start experiment messages in the autonomous
 * intersection demo.
 * 
 * @author Chien-Liang Fok
 */
public abstract class StartExpMsg implements Message {

	private static final long serialVersionUID = 7629528331155557780L;

	/**
	 * The name of the experiment.
	 */
	private String expName;
	
	/**
	 * The type of experiment.
	 */
	private ExpType expType;
	
	/**
	 * The constructor.
	 * 
	 * @param expName The name of the experiment.
	 * @param expType The type of experiment.
	 */
	public StartExpMsg(String expName, ExpType expType) {
		this.expName = expName;
		this.expType = expType;
	}
	/**
	 * 
	 * @return The The name of the experiment
	 */
	public String getExpName() {
		return expName;
	}
	
	/**
	 * 
	 * @return The type of experiment
	 */
	public ExpType getExpType() {
		return expType;
	}
	
	@Override
	public MsgType getType() {
		return MsgType.STARTEXP;
	}

	public String toString() {
		return getClass().getName() + ", " + expName + ", " + expType;
	}
}
