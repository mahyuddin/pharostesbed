package pharoslabut.cps;

import java.net.InetAddress;

import pharoslabut.io.AckableMessage;
import pharoslabut.io.Message;
import pharoslabut.io.Message.MsgType;

/**
 * This message is sent from Client to Server with an AssertionRequest
 * 
 * @author Kevin Boos
 */
public class AssertionRequestMsg implements AckableMessage {

	private static final long serialVersionUID = 8631382230236717430L;

	private InetAddress address;
	private int port;
	
	/**
	 * the sensor type to use for comparison
	 */
	private SensorType sensorType;
	
	/**
	 * the Inequality to use for comparison
	 */
	private Inequality ineq;
	
	/**
	 * an array actual values to use for comparison
	 */
	private Object [] actualValues;
	
	/**
	 * the system time when this AssertionRequestMsg was received
	 */
	private long requestTimestamp;

	
	public AssertionRequestMsg(SensorType st, Inequality in, Object vals[]) {
		this.sensorType = st;
		this.ineq = in;
		this.actualValues = vals;		
	}
	
	/**
	 * sets this msg's receivedTimestamp value to the current system time
	 */
	public void msgReceived() {
		this.requestTimestamp = System.currentTimeMillis();
	}
	
	/**
	 * @return the sensorType
	 */
	public SensorType getSensorType() {
		return sensorType;
	}

	/**
	 * @return the ineq
	 */
	public Inequality getIneq() {
		return ineq;
	}

	/**
	 * @return the actualValues
	 */
	public Object[] getActualValues() {
		return actualValues;
	}

	/**
	 * @return the requestTimestamp
	 */
	public long getRequestTimestamp() {
		return requestTimestamp;
	}

	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
	}
	
	@Override
	public void setReplyAddr(InetAddress address) {
		this.address = address;
	}

	@Override
	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public InetAddress getReplyAddr() {
		return address;
	}

	@Override
	public int getPort() {
		return port;
	}

}
