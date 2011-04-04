package pharoslabut.demo.simonsays;

public interface MCUConstants {
	
	/**
	 * The amount of time in milliseconds to wait for an ack from the MCU.
	 */
	public static final int MCU_TIMEOUT_PERIOD = 3000;
	
	public static final byte PROTEUS_BEGIN = 0x24;
	public static final byte PROTEUS_END = 0x0A;
	public static final byte PROTEUS_ESCAPE = (byte)0xFF;
	
	public static final byte PROTEUS_OPCODE_CAMERA_PAN = 0x6C;
	public static final byte PROTEUS_OPCODE_CAMERA_TILT = 0x6D;
	
	public static final byte PROTEUS_STATUS_PACKET = 7;
	public static final byte PROTEUS_TEXT_MESSAGE_PACKET = 9;
	public static final byte PROTEUS_ACK_PACKET = 10;
	
	public static final int MCU_STATUS_PACKET_SIZE = 7;
	public static final int MCU_ACK_PACKET_SIZE = 3;
}
