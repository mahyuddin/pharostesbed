package pharoslabut.io;

/**
 * An ack message that is transmitted to the sender by the receiver.
 * 
 * @author Chien-Liang Fok
 *
 */
public class PharosAckMsg implements Message {
	
	private static final long serialVersionUID = 1123861325094865776L;

	private static PharosAckMsg ack = new PharosAckMsg();
	
	/**
	 * The constructor is private because this is a singleton class.
	 */
	private PharosAckMsg() {
		
	}
	
	/**
	 * Access to the PharosAckMsg singleton.
	 * @return
	 */
	public static PharosAckMsg getAckMsg() {
		return ack;
	}
	
	@Override
	public MsgType getType() {
		return MsgType.ACK;
	}
}
