package pharoslabut.experiment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Contains the details of a specific robot used in an experiment.
 * 
 * @author Chien-Liang Fok
 */
public class RobotExpSettings {
	private String robotName;
	private String scriptFileName;
	private InetAddress ipAddr;
	private int port;
	
	public RobotExpSettings() {
	}
	
	public void setName(String robotName) {
		this.robotName = robotName;
	}
	
	public void setIPAddress(String ipAddr) throws UnknownHostException {
		this.ipAddr = InetAddress.getByName(ipAddr);
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public void setMotionScript(String fileName) {
		this.scriptFileName = fileName;
	}
	
	public String getName() {
		return robotName;
	}
	
	public String getMotionScript() {
		return scriptFileName;
	}
	
	public InetAddress getIPAddress() {
		return ipAddr;
	}
	
	public int getPort() {
		return port;
	}
}
