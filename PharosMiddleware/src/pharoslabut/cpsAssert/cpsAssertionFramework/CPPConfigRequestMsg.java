package pharoslabut.cpsAssert.cpsAssertionFramework;

import java.net.InetAddress;

import pharoslabut.io.AckedMsg;

public class CPPConfigRequestMsg implements AckedMsg {

	private static final long serialVersionUID = -1870241912295256776L;

	private String actualSource; 
	private Object logicalReference;

	private InetAddress replyAddress;
	private int replyPort;

	public CPPConfigRequestMsg(Object logicalReference, String actualSource, InetAddress replyAddress, int replyPort) {
		this.logicalReference = logicalReference;
		this.actualSource = actualSource;	
		this.replyAddress = replyAddress;
		this.replyPort = replyPort;
	}

	public InetAddress getReplyAddr() {
		return replyAddress;
	}

	public int getReplyPort() {
		return replyPort;
	}

	public String toString() {
		return replyAddress + ":" + replyPort;
	}
	
	public Object getLogicalReference() {
		return this.logicalReference;
	}


	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
	}


	/**
	 * @return the actualSource
	 */
	public String getActualSource() {
		return actualSource;
	}

}
