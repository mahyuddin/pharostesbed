package pharoslabut.cpsAssert;


import pharoslabut.exceptions.NoNewDataException;
import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.sensors.CameraLocalization;
import pharoslabut.sensors.CompassDataBuffer;
//import pharoslabut.sensors.CricketData;
import pharoslabut.sensors.CricketDataBuffer;
//import pharoslabut.sensors.CricketDataListener;
import pharoslabut.sensors.CricketInterface;
import pharoslabut.sensors.Position2DBuffer;
import pharoslabut.sensors.Position2DListener;
import pharoslabut.sensors.RangerDataBuffer;
import pharoslabut.sensors.RangerListener;

import playerclient3.PlayerClient;
import playerclient3.PlayerException;
import playerclient3.RangerInterface;
import playerclient3.Position2DInterface;
import playerclient3.structures.PlayerConstants;
import playerclient3.structures.PlayerPoint2d;
import playerclient3.structures.PlayerPoint3d;
import playerclient3.structures.PlayerPose2d;
import playerclient3.structures.position2d.PlayerPosition2dData;
import playerclient3.structures.ranger.PlayerRangerData;

/**
 * A top-level class for controlling the sensors that provide the data needed to check CPS assertions.
 * 
 * @author Kevin Boos
 */
public class CPSAssertSensor implements RangerListener, Position2DListener{
	
	private static CricketDataBuffer cricketBuffer;
	private static CompassDataBuffer compassBuffer;
	private static Position2DBuffer position2dBuffer;
	private static RangerDataBuffer rangerBuffer;
	private static CameraLocalization cameraLocalizer;


	public CPSAssertSensor (
			PlayerClient pc, FileLogger flogger,
			boolean useCricket, CricketDataBuffer cricketBuffer, CricketInterface cricketInterface, String cricketSerialPort, String cricketFile,  
			boolean useRanger, RangerDataBuffer rangerBuffer, RangerInterface rangerInterface, Integer rangerDeviceIndex, 
			boolean usePosition2d, Position2DBuffer position2dBuffer, Position2DInterface position2dInterface, Integer position2dDeviceIndex,
			boolean useCameraLocalization, CameraLocalization cameraLocalizer, Integer refreshInterval, String cameraFileName, 
			boolean useCompass, CompassDataBuffer compassBuffer, Position2DInterface compassInterface, Integer compassDeviceIndex
			)
	{
		if (flogger == null)
			Logger.setFileLogger(new FileLogger("CPSAssertLog.txt")); // set up logger if not yet specified

		if (useCricket) {
			if (cricketBuffer == null) {
				if (cricketInterface == null) {
					if (cricketSerialPort == null) {
						throw new NullPointerException("CPSAssertSensor Constructor: Parameter \"cricketSerialPort\" was null when trying to create a new CricketInterface.\n" + 
								"Please specify either a non-null \"cricketSerialPort\" or CricketInterface, or set the useCricket boolean value to false to disable Cricket Mote usage.");
					}
					cricketInterface = new CricketInterface(cricketSerialPort);
				}
				if (cricketFile == null) {
					throw new NullPointerException("CPSAssertSensor Constructor: Parameter \"cricketFile\" was null when trying to create a new CricketDataBuffer.\n" + 
							"Please specify either a non-null \"cricketFile\" or CricketDataBuffer, or set the useCricket boolean value to false to disable Cricket Mote usage.");
				}
				cricketBuffer = new CricketDataBuffer(cricketInterface, cricketFile);
				cricketBuffer.start();				
			}
			CPSAssertSensor.cricketBuffer = cricketBuffer;
		}
		
		if (useRanger) {
			if (rangerBuffer == null) {
				if (rangerInterface == null) {
					if (pc == null) {
						throw new NullPointerException("CPSAssertSensor Constructor: PlayerClient \"pc\" was null when connecting to Ranger Interface.\n" + 
								"Please specify one of the values, or set the useRanger boolean value to false to disable ranger usage."); 
					}
					if (rangerDeviceIndex == null) {
						rangerDeviceIndex = 0;
						Logger.log("\"rangerDeviceIndex\" was null, will use value = 0 as default.");
					}
					try{
						rangerInterface = pc.requestInterfaceRanger(rangerDeviceIndex, PlayerConstants.PLAYER_OPEN_MODE);
					} catch (PlayerException e) { 
						System.out.println("Error: could not connect to Ranger's Position2dInterface."); 
						System.exit(1); 
					}
				}
				rangerBuffer = new RangerDataBuffer(rangerInterface);
				rangerBuffer.start();
			}
			// no need to add this as a Pos2DListener, just use getRecentData() for latest ranger value
			CPSAssertSensor.rangerBuffer = rangerBuffer;
		}
		
		if (usePosition2d) {
			if (position2dBuffer == null) {
				if (position2dInterface == null) {
					if (pc == null) {
						throw new NullPointerException("CPSAssertSensor Constructor: PlayerClient \"pc\" was null when connecting to Odometry Interface.\n" + 
								"Please specify one of the values, or set the useOdometry boolean value to false to disable odometry usage."); 
					}
					if (position2dDeviceIndex == null) {
						position2dDeviceIndex = 0;
						Logger.log("\"position2dDeviceIndex\" was null, will use value = 0 as default.");
					}
					try{
						position2dInterface = pc.requestInterfacePosition2D(position2dDeviceIndex, PlayerConstants.PLAYER_OPEN_MODE);;
					} catch (PlayerException e) { 
						System.out.println("Error: could not connect to the requested Position2dInterface at deviceIndex=" + position2dDeviceIndex + "."); 
						System.exit(1); 
					}
				}
				position2dBuffer = new Position2DBuffer(position2dInterface);
				position2dBuffer.start();
			}
			// no need to add this as a Pos2DListener, just use getRecentData() for latest odometry value
			CPSAssertSensor.position2dBuffer = position2dBuffer;
		}
		
		if (useCompass) {
			if (compassBuffer == null) {
				if (compassInterface == null) {
					if (pc == null) {
						throw new NullPointerException("CPSAssertSensor Constructor: PlayerClient \"pc\" was null when connecting to Compass Interface.\n" + 
								"Please specify one of the values, or set the useCompass boolean value to false to disable Compass usage."); 
					}
					if (compassDeviceIndex == null) {
						compassDeviceIndex = 1;
						Logger.log("\"compassDeviceIndex\" was null, will use value = 1 as default.");
					}
					try{
						compassInterface = pc.requestInterfacePosition2D(compassDeviceIndex, PlayerConstants.PLAYER_OPEN_MODE);;
					} catch (PlayerException e) { 
						System.out.println("Error: could not connect to Compass's Position2dInterface."); 
						System.exit(1); 
					}
				}
				compassBuffer = new CompassDataBuffer(compassInterface);
				compassBuffer.start();
			}
			// no need to add this as a Pos2DListener, just use getMedian() for latest compass value
			CPSAssertSensor.compassBuffer = compassBuffer;
		}
		
		if (useCameraLocalization) {
			if (cameraLocalizer == null) {
				if (cameraFileName == null) {
					throw new NullPointerException("CPSAssertSensor Constructor: cameraFileName was null when connecting to CameraLocalization.\n" + 
							"Please specify one of the values, or set the useCameraLocalization boolean value to false to disable its usage."); 
				}
				if (refreshInterval == null) {
					Logger.log("CameraLocalization refreshInterval was not specified, using 100ms (10Hz).");
					refreshInterval = 100;
				}
				cameraLocalizer = new CameraLocalization(refreshInterval, cameraFileName); // starts itself				
			}
			CPSAssertSensor.cameraLocalizer = cameraLocalizer;
		}

	}


	/**
//	 * Reads a file with cricket beacons IDs and coordinates in order to associate each beacon with its location instead of ID
//	 * 
//	 * @param fileName the file to read, default is "cricketBeacons.txt"
//	 * @return a map of key-value pairs, where the key is the Cricket beacon ID and the value is the 3-d coordinate of the beacon
//	 */
//	private HashMap<String, PlayerPoint3d> readCricketFile(String fileName) {
//		HashMap<String, PlayerPoint3d> beacons = new HashMap<String, PlayerPoint3d>();
//		try {
//			Scanner sc = new Scanner(new BufferedReader(new FileReader(fileName)));
//			while (sc.hasNextLine()) {
//				String cricketId = sc.next();
//				if (cricketId.contains("//") || cricketId.contains("/*") || cricketId.contains("#") || cricketId.contains(";"))
//				{
//					// we've reached a commented line in the file
//					sc.nextLine(); // skip this line
//					continue;
//				}
//				PlayerPoint3d coords = new PlayerPoint3d();
//				coords.setPx(sc.nextDouble());
//				coords.setPy(sc.nextDouble());
//				coords.setPz(sc.nextDouble());
//				sc.nextLine(); // consume the rest of the line
//				// store to hashmap entry
//				beacons.put(cricketId, coords);
//				Logger.logDbg("Cricket Mote " + cricketId + " has coords: (" + coords.getPx() + "," + coords.getPy() + "," + coords.getPz() + ")");
//			}
//		} catch (FileNotFoundException e) {
//			Logger.logErr("Could not find Cricket beacons file: " + fileName);
//			e.printStackTrace();
//		} catch (InputMismatchException e) {
//			Logger.logErr("Error reading Cricket beacons file: " + fileName + ", bad input format.");
//			e.printStackTrace();
//		} catch (NoSuchElementException e) { }
//		
//		return beacons;
//	}	
	

	
	@Override
	public void newRangerData(PlayerRangerData data) { /*do nothing*/ }

	
	@Override
	public void newPlayerPosition2dData(PlayerPosition2dData data) { /*do nothing*/ }

	

	public static AssertionThread AssertCricket(PlayerPoint3d cricket3dCoord, Double expected, Inequality operation, Double delta, boolean blocking) throws NoNewDataException {
		return AssertCricket(cricketBuffer.getCricketBeaconID(cricket3dCoord), expected, operation, delta, blocking);
	}
	
	
	public static AssertionThread AssertCricket(PlayerPoint2d cricket2dCoord, Double expected, Inequality operation, Double delta, boolean blocking) throws NoNewDataException {
		return AssertCricket(cricketBuffer.getCricketBeaconID(cricket2dCoord), expected, operation, delta, blocking);
	}
		
		
	public static AssertionThread AssertCricket(String cricketBeaconID, Double expected, Inequality operation, Double delta, boolean blocking) throws NoNewDataException {
					
		AssertionThread at = new AssertionThread("Asserted that the Cricket Beacon distance was " + operation.toString() + " the expected value.",
				expected, cricketBuffer.getLastReading(cricketBeaconID).getDistance2dComponent(), delta, operation);
		
		at.runBlocking(blocking);
		return at;		
	}
	
	
	public static AssertionThread AssertCompass(Double expected, Inequality operation, Double delta, boolean blocking) throws NoNewDataException {
		
		if (compassBuffer == null)
			throw new NullPointerException("compassBuffer not configured properly, cannot use the AssertCompass method.");
		
		AssertionThread at = new AssertionThread("Asserted that the current Compass Bearing was " + operation.toString() + " the expected value.",
				expected, compassBuffer.getMedian(3), delta, operation);
		
		at.runBlocking(blocking);
		return at;
	}
	
	
	public static AssertionThread AssertPosition2d(Double expectedX, Double expectedY, Inequality ineqX, Inequality ineqY, Double deltaX, Double deltaY, boolean blocking) throws NoNewDataException {
		
		if (position2dBuffer == null)
			throw new NullPointerException("position2dBuffer not configured properly, cannot use the AssertPosition2d method.");
		
		PlayerPose2d curPos = position2dBuffer.getRecentData().getPos();
		AssertionThread at = new AssertionThreadPosition2d("position", expectedX, expectedY, curPos.getPx(), curPos.getPy(), deltaX, deltaY, ineqX, ineqY);
		
		at.runBlocking(blocking);
		return at;
	}
	
	
	public static AssertionThread AssertCameraLocalization(Double expectedX, Double expectedY, Inequality ineqX, Inequality ineqY, Double deltaX, Double deltaY, boolean blocking) throws NoNewDataException {
		
		if (cameraLocalizer == null)
			throw new NullPointerException("cameraLocalizer not configured properly, cannot use the AssertCameraLocalization method.");
		
		PlayerPoint2d curPos = cameraLocalizer.getCurrentLocation();
		while (curPos == null)
			curPos = cameraLocalizer.getCurrentLocation();
		AssertionThread at = new AssertionThreadPosition2d("camera-based localization", expectedX, expectedY, curPos.getPx(), curPos.getPy(), deltaX, deltaY, ineqX, ineqY);
		
		at.runBlocking(blocking);
		return at;
	}
	
	
	public static AssertionThread AssertRange(Double expectedRange, Inequality operation, Double delta, boolean blocking) throws NoNewDataException {
		
		if (rangerBuffer == null)
			throw new NullPointerException("rangerBuffer not configured properly, cannot use the AssertRange method.");
		
		AssertionThread at = new AssertionThread("Asserted that the current Ranger Distance was " + operation.toString() + " the expected value.",
				expectedRange, rangerBuffer.getRecentData().getRanges()[0], delta, operation);
		
		at.runBlocking(blocking);
		return at;	
	}


}
