package pharoslabut.io;


/**
 * All Message objects used in LimeLite implement this interface.
 *
 * @author Chien-Liang Fok
 * @version 3/17/2003
 */
public interface Message extends java.io.Serializable {
    public static enum MsgType {
    	RESET, LOAD_SETTINGS, STARTEXP, STOPEXP, CUSTOM, ACK, // These are the ones most often used.
    	LOAD_GPS_MOTION_SCRIPT,
    	LOAD_RELATIVE_MOTION_SCRIPT, SET_TIME, 
    	LOAD_BEHAVIORCONFIG_FILE, 
    	UPDATE_BEH_MSG, UPDATE_BEH_TABLE_MSG};
    
    /**
     * Returns the type of the message.  Type possible
     * types are defined within Message.
     *
     * @return the type of the message.  Type possible
     * types are defined within Message.
     */
    public MsgType getType();
}
