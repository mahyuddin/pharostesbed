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
	
	public TestOdometer(double dist, String fileName) {
		String serverIP = "localhost";
		int serverPort = 6665;
		
		flogger = new FileLogger(fileName);

		log("Connecting to player server " + serverIP + ":" + serverPort + "...");
		PlayerClient client = null;
		try {
			client = new PlayerClient(serverIP, serverPort);
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
		
		compass = client.requestInterfacePosition2D(1, PlayerConstants.PLAYER_OPEN_MODE);
		if (compass == null) {
			System.err.println("compass is null");
			System.exit(1);
		}
		compassDataBuffer = new CompassDataBuffer(compass);
		compassDataBuffer.setFileLogger(flogger);
		
		log("Resetting the odometer...");
		motors.resetOdometry();
		
		log("Listening for position2D events...");
		motors.addPos2DListener(this);
		
		move(dist);
	}
	
	private void move(double dist) {
		
		// busy wait until got compass heading
		double initHeading = Double.MAX_VALUE;
		double currHeading = Double.MAX_VALUE;
		while (initHeading == Double.MAX_VALUE) {
			try {
				currHeading = initHeading = compassDataBuffer.getMedian(COMPASS_MEDIAN_FILTER_LENGTH);
			} catch (NoNewDataException e) {
				e.printStackTrace();
			}
		}
		log("Got init heading: " + initHeading);
		
		// busy wait until position2d data arrives
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
		
		log("Done moving 2 meters.");
		motors.setCarCMD(0, 0);
	}

	@Override
	public void newPlayerPosition2dData(PlayerPosition2dData data) {
		log(data.toString());
		this.pos2dData = data;
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
		if (flogger != null) {
			flogger.log(result);
		}
	}
	
	public static final void main(String[] args) {
		new TestOdometer(Double.valueOf(args[0]), args[1]);
	}
}