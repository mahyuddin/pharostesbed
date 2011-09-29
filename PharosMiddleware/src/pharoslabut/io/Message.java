package pharoslabut.io;


/**
 * All Message objects used in LimeLite implement this interface.
 *
 * @author Chien-Liang Fok
 * @version 3/17/2003
 */
public interface Message extends java.io.Serializable {
    public static enum MsgType {RESET, STARTEXP, STOPEXP, LOAD_GPS_MOTION_SCRIPT,
    	LOAD_RELATIVE_MOTION_SCRIPT, SET_TIME, CUSTOM, LOAD_BEHAVIORCONFIG_FILE, 
    	UPDATE_BEH_MSG, ACK, UPDATE_BEH_TABLE_MSG,
    	LOAD_MRPATROL_SPECS, START_MRPATROL_EXP};
    
    /**
     * Returns the type of the message.  Type possible
     * types are defined within Message.
     *
     * @return the type of the message.  Type possible
     * types are defined within Message.
     */
    public MsgType getType();
}
