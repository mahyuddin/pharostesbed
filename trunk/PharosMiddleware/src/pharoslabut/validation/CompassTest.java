package pharoslabut.validation;

import pharoslabut.logger.CompassLogger;
import playerclient.PlayerClient;
import playerclient.PlayerException;
import playerclient.Position2DInterface;
import playerclient.structures.PlayerConstants;

/**
 * Logs the compass data.  Does not move the robot.
 * 
 * @author Chien-Liang Fok
 */
public class CompassTest {
	public static final double ROBOT_CIRCLE_VELOCITY = 0.6;
	public static final double ROBOT_CIRCLE_ANGLE = -20;
	public static final int ROBOT_REFRESH_PERIOD = 500; // interval of sending commands to robot in ms
	public static final int COMPASS_LOG_PERIOD = 100; // in milliseconds
	
	private PlayerClient client = null;
	
	public CompassTest(String serverIP, int serverPort, int time, 
			String fileName, boolean showGUI) {
		
		try {
			client = new PlayerClient(serverIP, serverPort);
		} catch(PlayerException e) {
			log("Error connecting to Player: ");
			log("    [ " + e.toString() + " ]");
			System.exit (1);
		}
		
		Position2DInterface compass = client.requestInterfacePosition2D(1, PlayerConstants.PLAYER_OPEN_MODE);
		
		if (compass == null) {
			log("compass is null");
			System.exit(1);
		}
		
		CompassLogger compassLogger = new CompassLogger(compass, showGUI);
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
	}
	
	private static void log(String msg) {
		System.out.println("CompassTest: " + msg);
	}
	
	private static void usage() {
		System.err.println("Usage: pharoslabut.CompassTest <options>\n");
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
		boolean showGUI = false;

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
		
		new CompassTest(serverIP, serverPort, time, fileName, showGUI);
	}
}
