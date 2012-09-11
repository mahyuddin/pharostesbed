package pharoslabut.demo.indoorMRPatrol;

import pharoslabut.io.Message;

/**
 * This is transmitted from the IndoorMRPatrolClient to the IndoorMRPatrolServer
 * to initiate a patrol experiment.
 * 
 * @author Chien-Liang Fok
 */
public class StartExpMsg implements Message {
	
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
	 * The number of rounds to patrol.
	 */
	private int numRounds;
	
	/**
	 * The constructor.
	 * 
	 * @param expName The name of the experiment.
	 * @param expType The type of experiment.
	 */
	public StartExpMsg(String expName, ExpType expType, int numRounds) {
		this.expName = expName;
		this.expType = expType;
		this.numRounds = numRounds;
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
	
	/**
	 * 
	 * @return The number of rounds to patrol.
	 */
	public int getNumRounds() {
		return numRounds;
	}
	
	@Override
	public MsgType getType() {
		return MsgType.STARTEXP;
	}

	public String toString() {
		return getClass().getName() + ", " + expName + ", " + expType;
	}
}
