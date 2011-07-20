package pharoslabut.behavior.management;

public class Robot {
	private String _ip;
	private int _port;
	public Robot(String ip, int port)
	{
		_ip = ip;
		_port = port;
		
		/*You need to add here the robot connection*/
	}
	void setSpeed(double speed, double angle)
	{
		System.out.println("Robot: You should send speed command to the robot");
	}
	public String GetIP() {return _ip;}
	public int GetPort(){return _port;}
	public void SetIP(String ip){_ip = ip;}
	public void SetPort(int port){_port = port;}
}
