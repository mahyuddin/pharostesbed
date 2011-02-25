package pharoslabut.experiment;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import pharoslabut.io.*;

public class RobotExpSettings {
	private String robotName;
	private String scriptFileName;
	private InetAddress ipAddr;
	private int port;
	private TCPMessageSender sender;
	
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
	
	public TCPMessageSender getSender() {
		if (sender == null) {
			try {
				sender = new TCPMessageSender(ipAddr, port, null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sender;
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
