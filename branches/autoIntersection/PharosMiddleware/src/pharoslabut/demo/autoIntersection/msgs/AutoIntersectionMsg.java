package pharoslabut.demo.autoIntersection.msgs;

import pharoslabut.io.*;

/**
 * A top-level class for all messages used in the autonomous intersection demo.
 * It implements the Message interface and adds a new member, the RobotID.
 * 
 * @author Michael Hanna
 * @author Chien-Liang Fok
 */
public abstract class AutoIntersectionMsg implements Message {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1589049167755252361L;
	private int robotID;
	
	/**
	 * The constructor.
	 */
	public AutoIntersectionMsg(int robotID) {
		this.robotID = robotID;	
	}
	
	/**
     *  @return robotID
     */
    public int getRobotID() {
        return this.robotID;
    }
	
	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
	}
	
	public String toString() {
		return "AutoIntersectionMsg";
	}
}
