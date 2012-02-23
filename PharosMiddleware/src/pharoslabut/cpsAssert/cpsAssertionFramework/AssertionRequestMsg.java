package pharoslabut.cpsAssert.cpsAssertionFramework;


import java.io.Serializable;
import java.net.InetAddress;

import pharoslabut.io.Message;


/**
 * This message is sent from Client to Server with an AssertionRequest
 * 
 * @author Kevin Boos
 */
public class AssertionRequestMsg implements Message{

	private static final long serialVersionUID = 8631382230236717430L;

	private InetAddress replyAddress;
	private int replyPort;
	
	
	/**
	 * the sensor type to use for comparison
	 */
	private Sensors sensorType;
	
	/**
	 * the Inequality to use for comparison
	 */
	private Inequality [] ineqs;
	
	/**
	 * whether the assertion is blocking (foreground) or non-blocking (background)
	 */
	private boolean blocking;
	
	/**
	 * an array of expected values to use for comparison
	 */
	private Object [] expectedValues;
	
	/**
	 * an array of delta values to use as margins of error for comparisons
	 */
	private Object [] deltaValues;
	
	/**
	 * the system time when this AssertionSentMsg was received
	 */
	private long requestSentTime;
	
	/**
	 * the system time when this AssertionRequestMsg was received
	 */
	private long requestReceivedTime;

	
	public AssertionRequestMsg(Sensors st, Inequality in[], boolean block, Object expVals[], Object deltaVals[]) {
		this.sensorType = st;
		this.ineqs = in;
		this.blocking = block;
		this.expectedValues = expVals;
		this.deltaValues = deltaVals;
	}
	
	/**
	 * sets this msg's requestSentTime value to the current system time
	 */
	public void msgSent() {
		this.requestSentTime = System.currentTimeMillis();
	}
	
	/**
	 * sets this msg's requestReceivedTime value to the current system time
	 */
	public void msgReceived() {
		this.requestReceivedTime = System.currentTimeMillis();
	}
	
	
	/**
	 * @return the sensorType
	 */
	public Sensors getSensorType() {
		return sensorType;
	}

	/**
	 * @return the ineq
	 */
	public Inequality [] getInequality() {
		return ineqs;
	}

	/**
	 * @return the actualValues
	 */
	public Object[] getExpectedValues() {
		return expectedValues;
	}

	/**
	 * @return the requestTimestamp
	 */
	public long getSentTimestamp() {
		return requestSentTime;
	}

	
	/**
	 * @return the requestReceivedTime
	 */
	public long getReceivedTimestamp() {
		return requestReceivedTime;
	}

	/**
	 * @return the blocking
	 */
	public boolean isBlocking() {
		return blocking;
	}

	/**
	 * @return the deltaValues
	 */
	public Object[] getDeltaValues() {
		return deltaValues;
	}

	
	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
	}
	
	
	public InetAddress getReplyAddr() {
		return replyAddress;
	}

	public int getReplyPort() {
		return replyPort;
	}

}
