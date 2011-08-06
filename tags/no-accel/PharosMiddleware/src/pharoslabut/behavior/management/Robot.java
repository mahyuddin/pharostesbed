package pharoslabut.behavior.management;

/**
 * Records a robot's IP address and port.
 * 
 * @author Noa Agmon
 */
public class Robot {
	private String ip;
	private int port;
	
	public Robot(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}
	
//	void setSpeed(double speed, double angle) {
//		System.out.println("Robot: You should send speed command to the robot");
//	}
	
	public String GetIP() {return ip;}
	public int GetPort(){return port;}
	public void SetIP(String ip){this.ip = ip;}
	public void SetPort(int port){this.port = port;}
}
