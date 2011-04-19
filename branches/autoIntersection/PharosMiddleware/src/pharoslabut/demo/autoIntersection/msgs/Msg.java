package pharoslabut.demo.autoIntersection.msgs;

import pharoslabut.io.*;
import pharoslabut.io.Message.MsgType;

/**
 * @author Michael Hanna
 *
 */
public class Msg implements Message {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1589049167755252361L;
	private int robotID;
	
	/**
	 * The constructor.
	 */
	public Msg(int robotID) {
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
		return "ParentMsg";
	}
}
