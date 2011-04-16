package pharoslabut.io;


/**
 * All Message objects used in LimeLite implement this interface.
 *
 * @author Chien-Liang Fok
 * @version 3/17/2003
 */
public abstract class Message implements java.io.Serializable {
    public static enum MsgType {RESET, STARTEXP, STOPEXP, LOAD_GPS_MOTION_SCRIPT,
    	LOAD_RELATIVE_MOTION_SCRIPT, CUSTOM};
    
    private ClientHandler ch;
    
    /**
     * Returns the type of the message.  Type possible
     * types are defined within Message.
     *
     * @return the type of the message.  Type possible
     * types are defined within Message.
     */
    public abstract MsgType getType();
    
    public void setClientHandler(ClientHandler ch) {
    	this.ch = ch;
    }
    
    public ClientHandler getClientHandler() {
    	return ch;
    }
}
