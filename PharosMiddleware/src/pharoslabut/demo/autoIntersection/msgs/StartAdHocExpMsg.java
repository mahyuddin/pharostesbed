package pharoslabut.demo.autoIntersection.msgs;

import pharoslabut.demo.autoIntersection.ExpType;
import pharoslabut.io.Message;

/**
 * This is transmitted from the ExpMgr to the AutoIntersetionClient
 * to initiate a patrol experiment.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.demo.autoIntersection.ExpMgr
 * @see pharoslabut.demo.autoIntersection.AutoIntersectionClient
 */
public class StartAdHocExpMsg implements Message {
	
	private static final long serialVersionUID = 8215875794859743401L;

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
	public StartAdHocExpMsg(String expName, ExpType expType) {
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