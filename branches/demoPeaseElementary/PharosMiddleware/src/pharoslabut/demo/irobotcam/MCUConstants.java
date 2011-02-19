package pharoslabut.demo.irobotcam;

public interface MCUConstants {
	public static final byte PROTEUS_BEGIN = 0x24;
	public static final byte PROTEUS_END = 0x0A;
	public static final byte PROTEUS_ESCAPE = (byte)0xFF;
	
	public static final byte PROTEUS_OPCODE_CAMERA_PAN = 0x6C;
	public static final byte PROTEUS_OPCODE_CAMERA_TILT = 0x6D;

}
