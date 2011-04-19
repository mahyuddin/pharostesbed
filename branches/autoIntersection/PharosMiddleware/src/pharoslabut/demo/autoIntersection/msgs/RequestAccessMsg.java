package pharoslabut.demo.autoIntersection.msgs;

import pharoslabut.io.*;

/**
 * This message is sent from the robot to the intersection server when
 * it approaches the intersection.  It's purpose is to request permission 
 * to cross the intersection.
 * 
 * @author Chien-Liang Fok
 *
 */
public class RequestAccessMsg implements Message {

	private static final long serialVersionUID = -3642519285338558989L;

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
