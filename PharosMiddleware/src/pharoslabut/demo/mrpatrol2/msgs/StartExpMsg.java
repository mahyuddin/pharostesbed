package pharoslabut.demo.mrpatrol2.msgs;

import pharoslabut.io.Message;

/**
 * This is transmitted to initiate a multi-robot patrol 2 (MRP2) experiment.
 * 
 * @author Chien-Liang Fok
 */
public class StartExpMsg implements Message {

	private static final long serialVersionUID = 961055814141077894L;

	/**
	 * The constructor.
	 */
	public StartExpMsg() {
	}
	
	@Override
	public MsgType getType() {
		return MsgType.STARTEXP;
	}

	public String toString() {
		return getClass().getName();
	}
}
