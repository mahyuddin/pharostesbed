package pharoslabut.cpsAssert;

import java.net.InetAddress;
import pharoslabut.io.AckedMsg;

/**
 * This message is sent from Client to Server with an AssertionRequest
 * 
 * @author Kevin Boos
 */
public class AssertionResponseMsg implements AckedMsg {

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
	

	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
	}
	
//	@Override
//	public void setReplyAddr(InetAddress address) {
//		this.address = address;
//	}
//
//	@Override
//	public void setPort(int port) {
//		this.port = port;
//	}
//
//	@Override
//	public InetAddress getReplyAddr() {
//		return address;
//	}
//
//	@Override
//	public int getPort() {
//		return port;
//	}

	/**
	 * @return the assertionMsg
	 */
	public String getAssertionMsg() {
		return assertionMsg;
	}

}
