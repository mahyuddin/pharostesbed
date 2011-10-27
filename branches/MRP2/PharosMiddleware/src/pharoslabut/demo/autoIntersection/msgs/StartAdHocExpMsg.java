package pharoslabut.demo.autoIntersection.msgs;

import pharoslabut.demo.autoIntersection.ExpType;
import pharoslabut.io.Message;

/**
 * This is transmitted from the ExpMgr to the AutoIntersetionClient
 * to initiate a patrol experiment.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.demo.autoIntersection.AutoIntersectionExpMgr
 * @see pharoslabut.demo.autoIntersection.AutoIntersectionClient
 */
public class StartAdHocExpMsg extends StartExpMsg implements Message {
	
	private static final long serialVersionUID = 8215875794859743401L;

	/**
	 * The constructor.
	 * 
	 * @param expName The name of the experiment.
	 * @param expType The type of experiment.
	 */
	public StartAdHocExpMsg(String expName, ExpType expType) {
		super(expName, expType);
	}
}