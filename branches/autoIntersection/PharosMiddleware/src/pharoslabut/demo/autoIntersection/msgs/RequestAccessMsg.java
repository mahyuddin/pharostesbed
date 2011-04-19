package pharoslabut.demo.autoIntersection.msgs;

import pharoslabut.io.*;
import pharoslabut.io.Message.MsgType;


/**
 * This message is sent from the robot to the intersection server when
 * it approaches the intersection.  It's purpose is to request permission 
 * to cross the intersection.
 * 
 * @author Chien-Liang Fok
 *
 */
public class RequestAccessMsg implements Message {

	/**
	 * The constructor.
	 */
	public RequestAccessMsg() {
		
	}
	
	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
	}
	
	public String toString() {
		return "RequestAccessMsg";
	}
}
