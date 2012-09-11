package pharoslabut.demo.simonsays.io;

import java.net.InetAddress;

import pharoslabut.demo.simonsays.InstructionType;

/**
 * This is a message that contains a command for the SimonSaysServer to follow.
 * 
 * @author Chien-Liang Fok
 *
 */
public class RobotInstrMsg extends SimonSaysClientMsg {
	private static final long serialVersionUID = 4462473889617403180L;

	private InstructionType type;
	private double paramDouble;
	
	/**
	 * The constructor.
	 * 
	 * @param type The instruction.
	 * @param replyAddress The IP address of the client.
	 * @param replyPort The port of the client.
	 */
	public RobotInstrMsg(InstructionType type, InetAddress replyAddress, int replyPort) {
		this(type, 0, replyAddress, replyPort);
	}
	
	/**
	 * The constructor.
	 * 
	 * @param type The instruction.
	 * @param paramDouble A parameter of type double.
	 * @param replyAddress The IP address of the client.
	 * @param replyPort The port of the client.
	 */
	public RobotInstrMsg(InstructionType type, double doubleParam, InetAddress replyAddress, int replyPort) {
		super(replyAddress, replyPort);
		this.type = type;
		this.paramDouble = doubleParam;
	}
	
	/**
	 * 
	 * @return The instruction type.
	 */
	public InstructionType getInstrType() {
		return type;
	}
	
	/**
	 * @return The distance to move in meters.
	 */
	public double getDoubleParam() {
		return paramDouble;
	}
	
	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
	}
	
	public String toString() {
		return getClass().getSimpleName() + ": type=" + type + ", paramDouble=" + paramDouble + ", reply=" + super.toString();
	}
}
