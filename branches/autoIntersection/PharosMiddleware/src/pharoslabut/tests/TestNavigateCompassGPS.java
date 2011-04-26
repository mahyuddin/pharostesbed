package pharoslabut.tests;

import playerclient.*;
import playerclient.structures.PlayerConstants;
import playerclient.structures.position2d.PlayerPosition2dData;
import pharoslabut.MotionArbiter;
import pharoslabut.logger.*;
import pharoslabut.navigate.*;

/**
 * Navigates a robot to a specific position.
 * 
 * @author Chien-Liang Fok
 */
public class TestNavigateCompassGPS implements Position2DListener{
	
	private Position2DInterface motors;
	private PlayerPosition2dData pos2dData = null;
	
	private Position2DInterface compass;
	private CompassDataBuffer compassDataBuffer;
	
	private GPSDataBuffer gpsDataBuffer;
	private MotionArbiter motionArbiter;
	
	private FileLogger flogger;
	
	public TestNavigateCompassGPS(String fileName) {
		String serverIP = "localhost";
		int serverPort = 6665;
		
		System.setProperty ("PharosMiddleware.debug", "true");
		
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
		
		motionArbiter = new MotionArbiter(motors);
		compassDataBuffer = new CompassDataBuffer(compass);
		compassDataBuffer.setFileLogger(flogger);
		
		log("Subscribing to GPS interface...");
		GPSInterface gps = client.requestInterfaceGPS(0, PlayerConstants.PLAYER_OPEN_MODE);
		gpsDataBuffer = new GPSDataBuffer(gps);
		
		log("Resetting the odometer...");
		motors.resetOdometry();
		
		log("Listening for position2D events...");
		motors.addPos2DListener(this);
		
		motionArbiter.setFileLogger(flogger);
		gpsDataBuffer.setFileLogger(flogger);
		compassDataBuffer.setFileLogger(flogger);
		
		
		//double latitude = 30.3854317;
		//double longitude = -97.7244167;
		double latitude = 30.385645;
		double longitude = -97.7251983;
		double velocity = 1.5;
		
		Location destLoc = new Location(latitude, longitude);
		log("Going to: " + destLoc);
		
		NavigateCompassGPS navigatorGPS = new NavigateCompassGPS(motionArbiter, compassDataBuffer, 
				gpsDataBuffer, flogger);
		
		try {
			navigatorGPS.go(destLoc, velocity);
		} catch (SensorException e) {
			log("ERROR: " + e.toString());
			e.printStackTrace();
		}
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
		String result = "TestNavigateCompassGPS: " + msg;
		System.out.println(result);
		if (flogger != null) {
			flogger.log(result);
		}
	}
	
	public static final void main(String[] args) {
		new TestNavigateCompassGPS(args[0]);
	}
}