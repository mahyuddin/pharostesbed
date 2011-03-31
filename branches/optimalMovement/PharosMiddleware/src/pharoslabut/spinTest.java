package pharoslabut;

import pharoslabut.MotionArbiter;
import pharoslabut.logger.CompassLoggerEvent;
import pharoslabut.logger.CompassLoggerEventListener;
import pharoslabut.tasks.MotionTask;
import pharoslabut.tasks.Priority;
import playerclient.PlayerClient;
import playerclient.PlayerException;
import playerclient.Position2DInterface;
import playerclient.structures.PlayerConstants;

/**
 * Moves the robot in circles while logging the compass data.
 * This should result in a saw-tooth graph.
 * 
 * @author Chien-Liang Fok
 */
public class spinTest implements CompassLoggerEventListener {
	public static final double ROBOT_TURNSPEED = 0.2;
//	public static final double ROBOT_CIRCLE_ANGLE = -20;
	public static final int ROBOT_REFRESH_PERIOD = 500; // interval of sending commands to robot in ms
	public static final int COMPASS_LOG_PERIOD = 100; // in milliseconds
	public double i_heading;
	public double f_heading;
	public boolean check = false;
	public MotionArbiter motionArbiter;
	public MotionTask circleTask;
	private PlayerClient client = null;
	
	public spinTest(String serverIP, int serverPort, int time, 
			String fileName, boolean showGUI) {
		
		try {
			client = new PlayerClient(serverIP, serverPort);
		} catch(PlayerException e) {
			log("Error connecting to Player: ");
			log("    [ " + e.toString() + " ]");
			System.exit (1);
		}
		
		Position2DInterface motorInterface = client.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		//Position2DInterface compass = client.requestInterfacePosition2D(1, PlayerConstants.PLAYER_OPEN_MODE);
		
		if (motorInterface == null) {
			log("motors is null");
			System.exit(1);
		}
		
//		if (compass == null) {
//			log("compass is null");
//			System.exit(1);
//		}
		
		motionArbiter = new MotionArbiter(MotionArbiter.MotionType.MOTION_IROBOT_CREATE, motorInterface);
		
		// First start the robot moving in circles
	//
		circleTask = new MotionTask(Priority.SECOND, 0, ROBOT_TURNSPEED);
	//	circleTask = new MotionTask(Priority.SECOND, 0, 0);
		motionArbiter.submitTask(circleTask);
		
		//CompassLogger compassLogger = new CompassLogger(compass, showGUI);
		CompassLoggerEvent compassLogger = new CompassLoggerEvent(serverIP, 7777, 1 /* device index */, showGUI);
		compassLogger.addListener(this);
		

		
		if (compassLogger.start(COMPASS_LOG_PERIOD, fileName)) {
			
			synchronized(this) {
				
				try {
					if (time > 0) {
						wait(time*1000);
					} else {
						wait(); // wait forever
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			compassLogger.stop();
		}
		
	//	if ((f_heading - i_heading) >= 0.5){
	//		motionArbiter.revokeTask(circleTask);
		 // stop moving in circles		
	//}
	}
	
	private static void log(String msg) {
		System.out.println("spinTest: " + msg);
	}
	
	private static void usage() {
		System.err.println("Usage: pharoslabut.spinTest <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
		System.err.println("\t-time <period in s>: duration of test (default infinity)");
		System.err.println("\t-file <file name>: name of file in which to save results (default log.txt)");
		System.err.println("\t-gui: display GUI (default not shown)");
	}
	
	public static void main(String[] args) {
		int time = 0;
		String fileName = "log.txt";
		String serverIP = "localhost";
		int serverPort = 6665;
		boolean showGUI = true;

		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-server")) {
					serverIP = args[++i];
				} 
				else if (args[i].equals("-port")) {
					serverPort = Integer.valueOf(args[++i]);
				} 
				else if (args[i].equals("-time")) {
					time = Integer.valueOf(args[++i]);
				} 
				else if (args[i].equals("-file")) {
					fileName = args[++i];
				}
				else if (args[i].equals("-gui")) {
					showGUI = true;
				}
				else {
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			usage();
			System.exit(1);
		}
		
		log("Server IP: " + serverIP);
		log("Server port: " + serverPort);
		log("Time: " + time + "s");
		log("File: " + fileName);
		log("ShowGUI: " + showGUI);
		
		new spinTest(serverIP, serverPort, time, fileName, showGUI);
	}

	@Override
	public void newHeading(double heading) {
		// TODO Auto-generated method stub
		if(check == false){
			i_heading = heading;
			check = true;
		}
		else{
			f_heading = heading;
		}
		
		if ((f_heading-i_heading) >= 2)
		{
			motionArbiter.revokeTask(circleTask);
		}
	
		System.out.println("compass heading: " + heading);
		System.out.println("initial heading: " + i_heading);
		System.out.println("current reading" + f_heading);
	
	}
}

