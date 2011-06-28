package pharoslabut.behavior.management;

public class Robot {
	String _ip;
	int _port;
	Robot(String ip, int port)
	{
		_ip = ip;
		_port = port;
		
		/*You need to add here the robot connection*/
	}
	void setSpeed(double speed, double angle)
	{
		System.out.println("Robot: You should send speed command to the robot");
	}
}
