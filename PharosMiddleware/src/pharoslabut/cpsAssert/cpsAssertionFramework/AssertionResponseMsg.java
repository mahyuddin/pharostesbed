package pharoslabut.cpsAssert.cpsAssertionFramework;

import java.io.Serializable;

import pharoslabut.io.Message;



/**
 * This message is sent from Client to Server with an AssertionRequest
 * 
 * @author Kevin Boos
 */
public class AssertionResponseMsg implements Message {

	private static final long serialVersionUID = -4151583308474362953L;

	
	/**
	 * the result of the assertion.
	 */
	private String assertionMsg;
	
	/**
	 * the Server system time when the original AssertionRequestMsg was received by the Server.
	 */
	private long requestReceivedServerTimeStamp;
	
	/**
	 * the Client system time when this AssertionResponseMsg was received by the Client.
	 */
	private long responseReceivedClientTimeStamp;

	/**
	 * the Server system time when this assertion started executing.
	 */
	private long assertionStartedServerTimeStamp;
	
	/**
	 * the Server system time when this assertion completed executing.
	 */
	private long assertionFinishedServerTimeStamp;


	
	public AssertionResponseMsg(String am, long startTime, long finishTime) {
		this.assertionMsg = am;		
		this.assertionStartedServerTimeStamp = startTime;
		this.assertionFinishedServerTimeStamp = finishTime;
	}
	
	/**
	 * sets this msg's responseReceivedClientTimeStamp
	 */
	public void msgReceived() {
		this.responseReceivedClientTimeStamp = System.currentTimeMillis();
	}
	

	/**
	 * @return the assertionMsg
	 */
	public String getAssertionMsg() {
		return assertionMsg;
	}

	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
	}

}
