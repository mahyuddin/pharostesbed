package pharoslabut.demo.autoIntersection.msgs;

import java.net.*;

import pharoslabut.io.*;

/**
 * A top-level class for all messages used in the autonomous intersection demo.
 * 
 * @author Michael Hanna
 * @author Chien-Liang Fok
 */
public abstract class AutoIntersectionMsg implements Message {
	
	private static final long serialVersionUID = 6086049361771571057L;
	
	/**
	 * The IP address of the vehicle.
	 */
	private String ip;
	
	/**
	 * The port on which the vehicle is listening.
	 */
	private int port;
	
	/**
	 * The constructor.
	 * 
	 * @param ip The IP address of the vehicle.
	 * @param port The port on which the vehicle is listening.
	 */
	public AutoIntersectionMsg(InetAddress ip, int port) {
		this.ip = ip.getHostAddress();
		this.port = port;
	}
	
	/**
     *  @return The vehicle's IP address
     */
    public InetAddress getIP() {
        InetAddress result = null;
        try {
			result = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return result;
    }
    
    /**
     * 
     * @return The vehicle's IP address in string format.
     */
    public String getIPString() {
    	return ip;
    }
    
    /**
     * @return The vehicle's port.
     */
    public int getPort() {
    	return port;
    }
	
	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
	}
	
	public String toString() {
		return ip + ":" + port;
	}
}
