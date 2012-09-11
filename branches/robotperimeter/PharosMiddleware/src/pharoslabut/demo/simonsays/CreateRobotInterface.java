package pharoslabut.demo.simonsays;

import java.io.*;

import pharoslabut.logger.*;
//import pharoslabut.navigate.MotionArbiter;
import playerclient3.PlayerClient;
import playerclient3.PlayerException;
import playerclient3.Position2DInterface;
import playerclient3.structures.PlayerConstants;

/**
 * Provides methods for controlling the iRobot Create.
 * 
 * @author Chien-Liang Fok
 */
public class CreateRobotInterface {
	public static final String PLAYER_SERVER = "/usr/local/bin/player";
	public static final String PLAYER_CONFIG_FILE = "/usr/local/share/player/config/proteus-roomba.cfg";
	public static final boolean CALIBRATE_DISTANCE = false;
	public static final boolean CALIBRATE_TURN = true;
	
	/**
	 * The adjustment need to ensure robot rotates at desired rate.
	 */
	//public static final double CALIBRATION_TURN_FACTOR = 0.6475;
	//public static final double CALIBRATION_TURN_FACTOR = 1;
	
	/**
	 * The robot's speed in m/s.
	 */
	public static final double ROBOT_SPEED = 0.2;
	
	/**
	 * The robot turn speed in radians per second.
	 */
	public static final double ROBOT_TURN_SPEED = 0.2;
	
//	private FileLogger flogger = null;
	private String playerIP;
	private int playerPort;
	
	/**
	 * The player client. This is the connection to the player server.
	 */
	private PlayerClient pclient = null;
	
	/**
	 * The interface to control the motors that move the robot.
	 */
	private Position2DInterface motors;
	
	/**
	 * The constructor.
	 * 
	 * @param playerIP The IP address of player.
	 * @param playerPort The TCP port of player.
	 */
	public CreateRobotInterface(String playerIP, int playerPort) {
		this.playerIP = playerIP;
		this.playerPort = playerPort;
		
		stopPlayer();
		startPlayer();
	}
	
	/**
	 * Connect to Player.  If an error occurs, pause 
	 * for a second and then try again.  Repeat this process until a connection is 
	 * successfully established.
	 */
	private void connect() {
		while (!connectToPlayer()) {
			synchronized(this) {
				Logger.log("Unable to connect to Player.  Pausing 1s then trying again...");
				try {
					wait(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Creates a connection to Player.
	 * 
	 * @return Whether a connection was established.
	 */
	private boolean connectToPlayer() {
		
		Logger.log("Connecting to player...");
		
		// If an existing player client exists, done.
		if (pclient != null) {
			Logger.log("Connection already exists, done.");
			return true;
//			Logger.log("Closing the existing connection to player.");
//			pclient.close();
//			pclient = null;
//			stopPlayer();
		}
		
		// Start the player server if it is not running...
		if (!isPlayerRunning()) {
			Logger.log("Player not running, starting it...");
			if (!startPlayer()) {
				Logger.logErr("Unable to start player server...");
				return false;
			} else
				Logger.log("Player started...");
		} else
			Logger.log("Player already running...");
		
		
		try {
			pclient = new PlayerClient(playerIP, playerPort);
			Logger.log("Connected to player.");
		} catch(PlayerException e) {
			Logger.logErr("Unable to conect to player at " + playerIP + ":" 
					+ playerPort + ", Player running = " + isPlayerRunning() + ", Error message: " + e.toString());
			return false;
		}
		
		// Get the Position2D interface for controlling the robot's movements...
		motors = pclient.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (motors == null) {
			Logger.logErr("ERROR: motors is null");
			return false;
		} else
			Logger.log("Subscribed to motors.");
		
		Logger.log("Changing Player to PUSH mode...");
		pclient.requestDataDeliveryMode(playerclient3.structures.PlayerConstants.PLAYER_DATAMODE_PUSH);
		
		Logger.log("Setting Player Client to run in continuous threaded mode...");
		pclient.runThreaded(-1, -1);
		
		return true;
	}
	
	private void disconnectFromPlayer() {
		Logger.log("Closing connection to player.");
		if (pclient != null) {
			pclient.close();
			pclient = null;
			motors = null;
		} else {
			Logger.logWarn("Already disconnected from player.");
		}
	}
	
	/**
	 * Moves the robot forward or backwards.
	 * 
	 * @param dist The distance to move in meters.  Positive if forward, negative is backwards.
	 * @return true if successful.
	 */
	public boolean move(double dist) {
		Logger.log("Moving the robot, dist = " + dist);
		
		if (dist == 0) {
			Logger.log("dist is zero, done...");
			return true;
		}
		
		connect();
//		
//		if (!isPlayerRunning()) {
//			Logger.log("Player not running, reconnecting...");
//			connect();
//		}
		
		// Calibrate the distance
		double calibratedDist;
		
		if (CALIBRATE_DISTANCE) { 
			calibratedDist = (dist + 0.0755) / 0.933;
			Logger.log("Calibrated distance = " + calibratedDist);	
		} else
			calibratedDist = dist;
		
		dist = calibratedDist;
		
		int direction = dist < 0 ? -1 : 1;
		int duration = (int)(Math.abs(dist) / ROBOT_SPEED * 1000); // in milliseconds
		int heading = 0;
		
		motors.isDataReady(); // clears the readyPp2ddata flag
		
		Logger.log("Start speed=" + (direction * ROBOT_SPEED) + ", heading=" + heading);
		motors.setSpeed(direction * ROBOT_SPEED, heading);
		
		Logger.log("Pause for " + duration + " milliseconds");
		pause(duration);
		
		Logger.log("Stop");
		motors.setSpeed(0, 0);
		
		disconnectFromPlayer();
		
		return true;
		
		// The following doesn't work because the robot does not report data very fast,
		// meaning data may not be ready by the time the movement is done.
//		if (!motors.isDataReady()) {
//			logErr("ERROR: robot did not move!");
//			return false;
//		} else
//			return true;
	}
	
	/**
	 * Turns the robot left or right.
	 * 
	 * @param angle The angle to turn in radians.  Left is positive, right is negative.
	 * @return true if successful.
	 */
	public boolean turn(double angle) {
		if (angle == 0) {
			Logger.log("Turn angle is zero, done.");
			return true;
		}
		
		connect();
		
//		if (!isPlayerRunning()) {
//			Logger.log("player not running, reconnecting...");
//			connect();
//		}
		
		int direction = angle < 0 ? -1 : 1;
		
		// Calibrate the the turn
		//double calibratedAngle = (angle + 0.0636) / 1.1741;
		double calibratedAngle;
		if (CALIBRATE_TURN) {
			calibratedAngle = (Math.abs(angle) + 0.2611) / 1.9504;
			Logger.log("Desired angle = " + angle + ", Calibrated angle = " + 
				(angle < 0 ? -1 * calibratedAngle : calibratedAngle));
		} else
			calibratedAngle = angle;
		
		angle = calibratedAngle;
		
		double speed = 0;
		
		int duration = (int)(Math.abs(angle) / ROBOT_TURN_SPEED * 1000); // in milliseconds
		
		motors.isDataReady(); // clears the readyPp2ddata flag
		
		Logger.log("turning robot: " + speed + ", " + direction * ROBOT_TURN_SPEED);
		motors.setSpeed(speed, direction * ROBOT_TURN_SPEED);
		
		Logger.log("Pausing for " + duration);
		pause(duration);
		
		Logger.log("Stopping turn...");
		motors.setSpeed(0, 0);
		
		disconnectFromPlayer();
		
		return true;
		
		// The following doesn't work because the robot does not report data very fast,
		// meaning data may not be ready by the time the movement is done.
//		if (!motors.isDataReady()) {
//			logErr("ERROR: robot did not turn!");
//			return false;
//		} else
//			return true;
	}
	
	/**
	 * Pauses the calling thread for the specified duration.
	 * 
	 * @param duration The time to pause the thread in milliseconds.
	 */
	private void pause(int duration) {
		synchronized(this) {
			try {
				wait(duration);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Checks whether the Player server is running.
	 * 
	 * @return true if the Player server is running.
	 */
	public boolean isPlayerRunning() {
		try {
			String cmd = "ps aux";
			
			Runtime rt = Runtime.getRuntime();
			Process pr = rt.exec(cmd);
			
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			BufferedReader errInput = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
			
			boolean playerRunning = false;
			String errResponseText = "";
			String line;
			
			while((line = stdInput.readLine()) != null) {
				if (line.contains("player")) {
					//log("Line contains player: " + line);
					playerRunning = true;
				}
			}
			
			while((line = errInput.readLine()) != null) {
				errResponseText += line + "\n";
			}
			
			int exitVal = pr.waitFor();
			if (exitVal != 0)
				Logger.log("Command ps aux exited with code " + exitVal);
			
			if (!errResponseText.equals(""))
				Logger.logErr("Problem while running ps aux: " + errResponseText);
			
			//log("playerRunning = " + playerRunning);
			return playerRunning;
		} catch(Exception e) {
			String eMsg = "Unable check if Player server is running : " + e.toString();
			System.err.println(eMsg);
			Logger.logErr(eMsg);
			return false;
		}
	}
	
	/**
	 * Starts the player server.  First tries to detect on which port the iRobot Create is connected.
	 * It then creates a configuration file for connecting to the robot, and launches the player
	 * server.
	 * 
	 * @return true if successful.
	 */
	public boolean startPlayer() {
		Logger.log("Starting player...");
		
		if (isPlayerRunning()) {
			Logger.logWarn("player already running");
			return true;  // Do not start player if it is already running...
		}
		
		// Try to detect which port the iRobot Create appears as
		String robotPort = null;
		try {
			String cmd = "dmesg";
			
			Runtime rt = Runtime.getRuntime();
			Process pr = rt.exec(cmd);
			
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			BufferedReader errInput = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
			
			String errResponseText = "";
			String line;
			
			while((line = stdInput.readLine()) != null) {
				if (line.contains("cp210x converter now attached to")) {
					String[] tokens = line.split("[\\s]");
					for (int i=0; i < tokens.length; i++) {
						if (tokens[i].contains("tty")) {
							robotPort = "/dev/" + tokens[i];
						}
					}
				}
			}
			
			while((line = errInput.readLine()) != null) {
				errResponseText += line + "\n";
			}
			
			int exitVal = pr.waitFor();
			if (exitVal != 0)
				Logger.logErr("Command 'dmesg' exited with code " + exitVal);
			
			if (!errResponseText.equals(""))
				Logger.logErr("Problem while running command 'dmesg': " + errResponseText);
			
		} catch(Exception e) {
			String eMsg = "Unable find iRobot Create port : " + e.toString();
			Logger.logErr(eMsg);
			return false;
		}
		
		// Create a Player config file...
		if (robotPort != null) {
			Logger.log("Robot detected on port " + robotPort);
			
			String configText = "driver\n"
				+ "(\n"
				+ "  name \"roomba\"\n"
				+ "  provides [\"position2d:0\"]\n"
				+ "  port \"" + robotPort + "\"\n"
				+ "  safe 1\n"
				+ "  alwayson 1\n"
				+ ")";
			
			try {
				FileWriter fw = new FileWriter(PLAYER_CONFIG_FILE);
				PrintWriter out = new PrintWriter(fw);
				out.print(configText);
				out.close();
			} catch (IOException e){
				e.printStackTrace();
			}

			Logger.log("Robot config file saved to " + PLAYER_CONFIG_FILE);
		} else {
			Logger.logErr("iRobot Create's port is null");
			return false;
		}
		
		try {
			Runtime rt = Runtime.getRuntime();
			Logger.log("Launching player server...");
			rt.exec(PLAYER_SERVER + " " + PLAYER_CONFIG_FILE);
			
			// pause to allow player server to start
			synchronized(this) {
				try{
					this.wait(1000);
				} catch(InterruptedException ie) {
					ie.printStackTrace();
				}
			}
			return true;
		} catch(Exception e) {
			Logger.logErr("Unable launch player server: " + e.toString());
			return false;
		}
	}
	
	/**
	 * Stops the player server and removes the reference to the player client.
	 * 
	 * @return true if successful.
	 */
	public boolean stopPlayer() {
		if (!isPlayerRunning()) {
			Logger.logWarn("Player already stopped.");
			return true;  // abort if player is not running...
		}
		
		Logger.log("Stopping player...");
		
		try {
			String cmd = "sudo killall -s 15 player";
			
			Runtime rt = Runtime.getRuntime();
			Process pr = rt.exec(cmd);
			pclient = null;
			
			int exitVal = pr.waitFor();
			if (exitVal != 0) {
				Logger.logErr("Player exited with code " + exitVal);
				return false;
			} else
				return true;
		} catch(Exception e) {
			Logger.logErr("Problem while stopping player: " + e.toString());
			return false;
		}
	}
	
//	private static void usage() {
//		System.setProperty ("PharosMiddleware.debug", "true");
//		System.out.println("Usage: " + CreateRobotInterface.class.getName() + " <options>\n");
//		System.out.println("Where <options> include:");
//		System.out.println("\t-pServer <ip address>: The IP address of the Player Server (default localhost)");
//		System.out.println("\t-pPort <port number>: The Player Server's port number (default 6665)");
//		System.out.println("\t-mcuPort <port name>: The serial port on which the MCU is attached (default /dev/ttyS0)");
//		System.out.println("\t-file <file name>: name of file in which to save results (default null)");
//		System.out.println("\t-move <distance>: Move the robot forward or backwards.");
//		System.out.println("\t-turn <angle>: Turn the robot side to side.");
//		System.out.println("\t-debug: enable debug mode");
//	}
//	
//	/**
//	 * Perform some basic tests on the RobotInterface.
//	 * 
//	 * @param args The command line arguments.
//	 */
//	public static void main(String[] args) {
//		String pServerIP = "localhost";
//		int pServerPort = 6665;
//		String fileName = null;
//		
//		boolean doMove = false;
//		boolean doTurn = false;
//		double moveDist = 0;
//		double turnAngle = 0;
//		
//		try {
//			for (int i=0; i < args.length; i++) {
//				if (args[i].equals("-pServer")) {
//					pServerIP = args[++i];
//				}
//				else if (args[i].equals("-pPort")) {
//					pServerPort = Integer.valueOf(args[++i]);
//				}
//				else if (args[i].equals("-file")) {
//					fileName = args[++i];
//				}
//				else if (args[i].equals("-mcuPort")) {
//					fileName = args[++i];
//				}
//				else if (args[i].equals("-move")) {
//					doMove = true;
//					moveDist = Double.valueOf(args[++i]).doubleValue();
//				}
//				else if (args[i].equals("-turn")) {
//					doTurn = true;
//					turnAngle = Double.valueOf(args[++i]).doubleValue();
//				}
//				else if (args[i].equals("-debug") || args[i].equals("-d")) {
//					System.setProperty ("PharosMiddleware.debug", "true");
//				}
//				else {
//					usage();
//					System.exit(1);
//				}
//			}
//		} catch(Exception e) {
//			e.printStackTrace();
//			usage();
//			System.exit(1);
//		}
//		
//		if (!doMove && !doTurn) {
//			usage();
//			System.exit(1);
//		}
//		
//		if (fileName != null)
//			Logger.setFileLogger(new FileLogger(fileName));
//		
//		CreateRobotInterface ri = new CreateRobotInterface(pServerIP, pServerPort);
//		ri.startPlayer();
//		
//		if (doMove) {
//			ri.move(moveDist);
//		} else if (doTurn) {
//			ri.turn(turnAngle / 180 * Math.PI);
//		}
//		
//		System.exit(0);
//	}
}
