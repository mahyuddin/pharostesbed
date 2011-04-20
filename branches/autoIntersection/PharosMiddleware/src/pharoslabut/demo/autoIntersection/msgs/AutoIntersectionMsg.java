package pharoslabut.demo.autoIntersection.msgs;

import java.net.*;
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
	private InetAddress robotIP;
	private int robotPort;
	
	/**
	 * The constructor.
	 */
	public AutoIntersectionMsg(InetAddress robotIP, int robotPort) {
		this.robotIP = robotIP;
		this.robotPort = robotPort;
	}
	
	/**
     *  @return The robot's IP address
     */
    public InetAddress getRobotIP() {
        return robotIP;
    }
    
    /**
     * @return The robot's port.
     */
    public int getRobotPort() {
    	return robotPort;
    }
	
	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
	}
	
	public String toString() {
		return "AutoIntersectionMsg";
	}
}
