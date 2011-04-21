package pharoslabut.tests;

import playerclient.*;
import playerclient.structures.PlayerConstants;
import playerclient.structures.position2d.PlayerPosition2dData;
import pharoslabut.logger.*;
import pharoslabut.navigate.CompassDataBuffer;

/**
 * Tests the odometer on the Proteus robot.
 * The x axis is forward/backwards with a positive value indicating forward.
 * The y axis is left/right movement with a positive value indicating left.
 * The yaw position is the degree to which the robot has turned in radians, 
 * where a positive value indicates a turn to the left (counter-clockwise when viewed from above)
 * 
 * @author Chien-Liang Fok
 */
public class TestOdometer implements Position2DListener{
	
	public static final int COMPASS_MEDIAN_FILTER_LENGTH = 3;
	public static final double MAX_HEADING_DIVERGENCE = Math.PI / 6;
	public static final double MAX_TURN_ANGLE = 0.5; // randians
	
	private Position2DInterface motors;
	private PlayerPosition2dData pos2dData = null;
	
	private Position2DInterface compass;
	private CompassDataBuffer compassDataBuffer;
	
	private FileLogger flogger;
	
	/**
	 * The constructor.
	 * 
	 * @param serverAddr The IP address of the Player server.
	 * @param serverPort The port on which the Player server is listening.
	 * @param speed The speed to move at in meters per second.
	 * @param dist The distance to move in meters.
	 * @param useCompass Whether to use the compass.
	 * @param flogger The FileLogger in which to store log data.
	 */
	public TestOdometer(String serverAddr, int serverPort, double speed, double dist, boolean useCompass, FileLogger flogger) {
		this.flogger = flogger;
		
		log("Connecting to player server " + serverAddr + ":" + serverPort + "...");
		PlayerClient client = null;
		try {
			client = new PlayerClient(serverAddr, serverPort);
		} catch(PlayerException e) {
			System.err.println("Error connecting to Player: ");
			System.err.println("    [ " + e.toString() + " ]");
			System.exit (1);
		}

		log("Subscribing to motor interface...");
		motors = client.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);

		if (motors == null) {
			System.err.println("motors is null");
			System.exit(1);
		}
		
		if (useCompass) {
			compass = client.requestInterfacePosition2D(1, PlayerConstants.PLAYER_OPEN_MODE);
			if (compass == null) {
				System.err.println("compass is null");
				System.exit(1);
			}
			compassDataBuffer = new CompassDataBuffer(compass);
			compassDataBuffer.setFileLogger(flogger);
		}
		
		log("Resetting the odometer...");
		motors.resetOdometry();
		
		log("Listening for position2D events...");
		motors.addPos2DListener(this);
		
		if (useCompass)
			moveCompass(speed, dist);
		else
			move(speed, dist);
	}
	
	private synchronized PlayerPosition2dData getPosition2dData() {
		return pos2dData;
	}
	
	/**
	 * Moves the robot at a certain speed for a certain distance based solely 
	 * on odometry readings.
	 * 
	 * @param speed The speed to move at in m/s.
	 * @param dist The distance in meters.
	 */
	private void move(double speed, double dist) {
		// busy wait until position2d data arrives (this is the odometer)
		while (getPosition2dData() == null) {
			pause(100);
		}
		
		double startX = getPosition2dData().getPos().getPx();
		log("Start X = " + startX);
		
		PlayerPosition2dData posData;
		
		while ((posData = getPosition2dData()).getPos().getPx() - startX < dist) {
			log("Distance moved: " + (posData.getPos().getPx() - startX));
			log("Sending move command: speed=" + speed + ", heading = " + 0);
			motors.setCarCMD(speed, 0);
			pause(100); // cycle at 10Hz
		}
		
		log("Done moving " + dist + " meters.");
		motors.setCarCMD(0, 0);
	}
	
	/**
	 * Moves the robot at a certain speed for a certain distance based on
	 * compass and odometry readings.
	 * 
	 * @param speed The speed to move at in m/s.
	 * @param dist The distance in meters.
	 */
	private void moveCompass(double speed, double dist) {
		
		// busy wait until got compass heading
		double initHeading = Double.MAX_VALUE;
		double currHeading = Double.MAX_VALUE;
		while (initHeading == Double.MAX_VALUE) {
			try {
				currHeading = initHeading = compassDataBuffer.getMedian(COMPASS_MEDIAN_FILTER_LENGTH);
			} catch (NoNewDataException e) {
				e.printStackTrace();
			}
			Thread.yield();
		}
		log("Initial heading: " + initHeading);
		
		// busy wait until position2d data arrives (this is the odometer)
		while (pos2dData == null) {
			pause(100);
		}
		
		double startX = pos2dData.getPos().getPx();
		log("Start X = " + startX);
		
		while (pos2dData.getPos().getPx() - startX < dist) {
			log("Distance moved: " + (pos2dData.getPos().getPx() - startX));
			
			// Calculate the change in heading to keep robot heading in a straight line
			try {
				currHeading = compassDataBuffer.getMedian(COMPASS_MEDIAN_FILTER_LENGTH);
			} catch(NoNewDataException e) {
				log("Unable to get new heading, using old heading...");
				e.printStackTrace();
			}
			
			double divergence = Math.abs(currHeading - initHeading);
			int sign = (currHeading > initHeading ? -1 : 1);
			
			if (divergence > MAX_HEADING_DIVERGENCE)
				divergence = MAX_HEADING_DIVERGENCE;
			
			double heading = sign * MAX_TURN_ANGLE * (divergence / MAX_HEADING_DIVERGENCE);
			
			log("Sending move command: speed=" + 0.4 + ", heading = " + heading);
			motors.setCarCMD(0.4, heading);
			pause(100);
		}
		
		log("Done moving " + dist + " meters.");
		motors.setCarCMD(0, 0);
	}

	@Override
	public void newPlayerPosition2dData(PlayerPosition2dData data) {
		// This is the odometer data coming in...
		log(data.toString());
		
		synchronized(this) {
			this.pos2dData = data;
		}
	}
	
	private void pause(long duration) {
		synchronized(this) {
			try {
				this.wait(duration);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void log(String msg) {
		String result = "TestOdometer: " + msg;
		System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(msg);
	}
	
	private static void usage() {
		System.setProperty ("PharosMiddleware.debug", "true");
		print("Usage: pharoslabut.tests.TestOdometer <options>\n");
		print("Where <options> include:");
		print("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		print("\t-port <port number>: The Player Server's port number (default 6665)");
		print("\t-speed <speed>: The speed to move at (default 0.4)");
		print("\t-dist <distance>: The distance in meters to move (default 5)");
		print("\t-usecompass: Whether to use the compass to keep the robot going in a straight line.");
		print("\t-log <log file>: The log file in which to save results (default TestOdometer.log)");
		print("\t-debug: enable debug mode");
	}
	
	public static final void main(String[] args) {
		String serverAddr = "localhost";
		int serverPort = 6665;
		double speed = 0.4;
		double dist = 5; 
		String logFile = "TestOdometer.log";
		boolean usecompass = false;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-server")) {
					serverAddr = args[++i];
				} 
				else if (args[i].equals("-port")) {
					serverPort = Integer.valueOf(args[++i]);
				}
				else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				}
				else if (args[i].equals("-dist")) {
					dist = Double.valueOf(args[++i]);
				} 
				else if (args[i].equals("-speed")) {
					speed = Double.valueOf(args[++i]);
				} 
				else if (args[i].equals("-usecompass")) {
					usecompass = true;
				} 
				else if (args[i].equals("-log")) {
					logFile = args[++i];
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
		
		new TestOdometer(serverAddr, serverPort, speed, dist, usecompass, new FileLogger(logFile));
	}
}