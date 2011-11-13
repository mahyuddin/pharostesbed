package pharoslabut.cpsAssert;


import pharoslabut.exceptions.NoNewDataException;
import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.sensors.CompassDataBuffer;
import pharoslabut.sensors.CricketData;
import pharoslabut.sensors.CricketDataListener;
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
import playerclient3.structures.PlayerPose2d;
import playerclient3.structures.position2d.PlayerPosition2dData;
import playerclient3.structures.ranger.PlayerRangerData;


public class CPSAssertSensor implements CricketDataListener, RangerListener, Position2DListener{
	
	
//	/**
//	 * the list of Cricket Mote beacons and their corresponding poses (positional coordinates)
//	 */
//	Map<String, PlayerPoint3d> cricketPositions = new HashMap<String, PlayerPoint3d>();
//	
//	/**
//	 * the list of Cricket Mote beacons currently connected to this robot's Cricket Mote Listener
//	 */
//	Map<String, ArrayList<CricketData>> cricketBeacons = Collections.synchronizedMap(new HashMap<String, ArrayList<CricketData>>());
	
	public static final double cricketBeaconHeight = 1.0;
	
	private static Double lastCricketReading = null;
	
	private CompassDataBuffer compassBuffer;
	private Position2DBuffer odometryBuffer;
	private RangerDataBuffer rangerBuffer;


	public CPSAssertSensor (
			PlayerClient pc, FileLogger flogger,
			boolean useCricket, CricketInterface ci, String cricketSerialPort, 
			boolean useRanger, Integer rangerDeviceIndex, RangerInterface rangerInterface, RangerDataBuffer rangerBuffer,
			boolean useOdometry, Integer odometryDeviceIndex, Position2DInterface odometryInterface, Position2DBuffer odometryBuffer,
			boolean useCompass, Integer compassDeviceIndex, Position2DInterface compassInterface, CompassDataBuffer compassBuffer
			) 
	{
		if (flogger == null)
			Logger.setFileLogger(new FileLogger("CPSAssertLog.txt")); // set up logger (this does nothing if logger was already set up)

		if (useCricket) {
			if (ci == null) {
				if (cricketSerialPort == null) {
					throw new NullPointerException("CPSAssertSensor Constructor: Parameter \"cricketSerialPort\" was null when trying to create a new CricketInterface.\n" + 
							"Please specify either a non-null \"cricketSerialPort\" or CricketInterface, or set the useCricket boolean value to false to disable Cricket Mote usage.");
				}
				ci = new CricketInterface(cricketSerialPort);
			}
			ci.registerCricketDataListener(this);
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
			this.rangerBuffer = rangerBuffer;
		}
		
		if (useOdometry) {
			if (odometryBuffer == null) {
				if (odometryInterface == null) {
					if (pc == null) {
						throw new NullPointerException("CPSAssertSensor Constructor: PlayerClient \"pc\" was null when connecting to Odometry Interface.\n" + 
								"Please specify one of the values, or set the useOdometry boolean value to false to disable odometry usage."); 
					}
					if (odometryDeviceIndex == null) {
						odometryDeviceIndex = 0;
						Logger.log("\"odometryDeviceIndex\" was null, will use value = 0 as default.");
					}
					try{
						odometryInterface = pc.requestInterfacePosition2D(odometryDeviceIndex, PlayerConstants.PLAYER_OPEN_MODE);;
					} catch (PlayerException e) { 
						System.out.println("Error: could not connect to Odometry's Position2dInterface."); 
						System.exit(1); 
					}
				}
				odometryBuffer = new Position2DBuffer(odometryInterface);
				odometryBuffer.start();
			}
			// no need to add this as a Pos2DListener, just use getRecentData() for latest odometry value
			this.odometryBuffer = odometryBuffer;
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
			this.compassBuffer = compassBuffer;
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
	public void newCricketData(CricketData cd) {
		lastCricketReading = ((double)cd.getDistance())/100;
	}

	
	@Override
	public void newRangerData(PlayerRangerData data) { /*do nothing*/ }

	
	@Override
	public void newPlayerPosition2dData(PlayerPosition2dData data) { /*do nothing*/ }

	
	
	
	public void AssertCricket(Double expected, Inequality operation, Double delta, boolean blocking) throws NoNewDataException {
				
		if (lastCricketReading == null)
			throw new NoNewDataException("No Cricket Data Available.");
		
		AssertionThread at = new AssertionThread("Asserted that the current Cricket Beacon distance was " + operation.toString() + " the expected value.",
				expected, lastCricketReading, delta, operation);
		
		at.start();
		if (blocking) {
			try {
				at.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void AssertCompass(Double expected, Inequality operation, Double delta, boolean blocking) throws NoNewDataException {
		
		if (compassBuffer == null)
			throw new NullPointerException("compassBuffer not configured properly, cannot use the AssertCompass method.");
		
		AssertionThread at = new AssertionThread("Asserted that the current Compass Bearing was " + operation.toString() + " the expected value.",
				expected, compassBuffer.getMedian(3), delta, operation);
		
		at.start();
		if (blocking) {
			try {
				at.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
	}
	
	
	public void AssertOdometry(Double expectedX, Double expectedY, Inequality ineqX, Inequality ineqY, Double deltaX, Double deltaY, boolean blocking) throws NoNewDataException {
		
		if (odometryBuffer == null)
			throw new NullPointerException("odometryBuffer not configured properly, cannot use the AssertOdometry method.");
		
		// TODO fix for usage with AssertionThread subclass
		
		PlayerPose2d curPos = odometryBuffer.getRecentData().getPos();
		CPSAssertNumerical.AssertInequality("Asserted that the current Odometry x-value was " + ineqX.toString() + " the expected value.",
				expectedX, curPos.getPx(), deltaX, ineqX);		
		CPSAssertNumerical.AssertInequality("Asserted that the current Odometry y-value was " + ineqX.toString() + " the expected value.",
				expectedY, curPos.getPy(), deltaY, ineqX);		
	}
	
	
	public void AssertRange(Double expectedRange, Inequality operation, Double delta, boolean blocking) throws NoNewDataException {
		
		if (rangerBuffer == null)
			throw new NullPointerException("rangerBuffer not configured properly, cannot use the AssertRange method.");
		
		AssertionThread at = new AssertionThread("Asserted that the current Ranger Distance was " + operation.toString() + " the expected value.",
				expectedRange, rangerBuffer.getRecentData().getRanges()[0], delta, operation);
		
		at.start();
		if (blocking) {
			try {
				at.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
						
	}







}
