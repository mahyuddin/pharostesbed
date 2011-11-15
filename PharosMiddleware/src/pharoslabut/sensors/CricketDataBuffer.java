package pharoslabut.sensors;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

//import pharoslabut.logger.FileLogger;
import pharoslabut.exceptions.NoNewDataException;
import pharoslabut.logger.Logger;
import playerclient3.structures.PlayerPoint2d;
import playerclient3.structures.PlayerPoint3d;


/**
 * This class adds itself as a CricketDataListener and provides many ways to get Cricket readings. This is the preferred way to interface with Cricket Beacons. 
 * <li> Supports multiple unique Cricket Beacons with one Cricket Listener. 
 * <li> Only saves the most recent distance value (one reading) at a time for each unique Cricket beacon. <br> <br>
 * *** In the constructor, you must provide a file, such as "cricketBeacons.txt" that lists the beacons, one per line, in the following format: <br>
 * <code> [cricket ID] [x coordinate] [y coordinate] [z coordinate] </code> <br> <b> Each component in the file should be separated by a space. </b> <br> <br>
 * <b> Example file </b> (with 2 beacons): <br>
 * <code> 01:e6:39:39:11:00:00:50 0.0 0.0 1.45 </code> <br>
 * <code> 01:d8:fe:fe:10:00:00:87 2.0 0.0 1.45 </code> <br> <br>
 * 
 * @see CricketInterface
 * @see CricketDataListener
 * @author Kevin Boos
 */
public class CricketDataBuffer implements Runnable, CricketDataListener {
	
	/**
	 * the list of Cricket Mote beacons and their corresponding poses (positional coordinates)
	 */
	Map<String, PlayerPoint3d> cricketPositions = new HashMap<String, PlayerPoint3d>();
	
	/**
	 * the list of Cricket Motes and their latest CricketBeaconReading
	 */
	Map<String, CricketBeaconReading> beaconReadings = Collections.synchronizedMap(new HashMap<String, CricketBeaconReading>());
	
	
	/**
	 * The period in milliseconds at which to check for expired data.
	 */
	public static final int CRICKET_BUFFER_REFRESH_PERIOD = 100;
	
	/**
	 * A proxy to the Cricket device.
	 */
	private CricketInterface cricketIface;
	
	/**
	 * Whether the thread that reads Cricket data is running.
	 */
	private boolean running = false;
	
//	/**
//	 * The file logger for saving debug data.
//	 */
//	private FileLogger flogger;
	
	/**
	 * The constructor. You must call the <code> start() </code> method to start receiving Cricket readings. <br>
	 * You can also call <code> stop() </code> to stop accepting new Cricket readings.
	 * 
	 * @param crckti The proxy to the Cricket device.
	 */
	public CricketDataBuffer(CricketInterface crckti, String cricketFile) {
		this.cricketIface = crckti;
		// read list of cricket beacons and their positions
		cricketPositions = readCricketFile(cricketFile);	
	}
	
	
	/**
	 * Reads a file with cricket beacons IDs and coordinates in order to associate each beacon with its location instead of ID
	 * 
	 * @param fileName the file to read, default is "cricketBeacons.txt"
	 * @return a map of key-value pairs, where the key is the Cricket beacon ID and the value is the 3-d coordinate of the beacon
	 */
	private HashMap<String, PlayerPoint3d> readCricketFile(String fileName) {
		HashMap<String, PlayerPoint3d> beacons = new HashMap<String, PlayerPoint3d>();
		try {
			Scanner sc = new Scanner(new BufferedReader(new FileReader(fileName)));
			while (sc.hasNextLine()) {
				String cricketId = sc.next();
				if (cricketId.contains("//") || cricketId.contains("/*") || cricketId.contains("#") || cricketId.contains(";"))
				{
					// we've reached a commented line in the file
					sc.nextLine(); // skip this line
					continue;
				}
				PlayerPoint3d coords = new PlayerPoint3d();
				coords.setPx(sc.nextDouble());
				coords.setPy(sc.nextDouble());
				coords.setPz(sc.nextDouble());
				sc.nextLine(); // consume the rest of the line
				// store to hashmap entry
				beacons.put(cricketId, coords);
				Logger.logDbg("Cricket Mote " + cricketId + " has coords: (" + coords.getPx() + "," + coords.getPy() + "," + coords.getPz() + ")");
			}
		} catch (FileNotFoundException e) {
			Logger.logErr("Could not find Cricket beacons file: " + fileName);
			e.printStackTrace();
		} catch (InputMismatchException e) {
			Logger.logErr("Error reading Cricket beacons file: " + fileName + ", bad input format.");
			e.printStackTrace();
		} catch (NoSuchElementException e) { }
		
		return beacons;
	}	
	
	
	/**
	 * Starts the CricketDataBuffer.  Creates a thread for reading cricket data.
	 */
	public synchronized void start() {
		if (!running) {
			Logger.log("starting...");
			running = true;
			cricketIface.registerCricketDataListener(this);
//			new Thread(this).start();
		} else
			Logger.log("already started...");
	}
	
	/**
	 * Stops the CricketDataBuffer.  Allows the thread that reads Cricket data to terminate.
	 */
	public synchronized void stop() {
		if (running) {
			Logger.log("stopping...");
			cricketIface.deregisterCricketDataListener(this);
			running = false;
		} else
			Logger.log("already stopped...");
	}

	
//	/**
//	 * Sets the file logger.  If the file logger is not null, this component
//	 * logs compass data to a file.
//	 * 
//	 * @param flogger The file logger.  This may be null.
//	 */
//	public void setFileLogger(FileLogger flogger) {
//		this.flogger = flogger;
//	}
	
    /**
	 * Returns the most recent Cricket device reading.
	 * @param cricketBeaconID the id of the Cricket Beacon to get the last reading from
	 * @return The most recent Cricket device reading
	 * @throws NoNewDataException If no new data was received (the most recent reading is null).
	 */
	public synchronized CricketBeaconReading getLastReading(String cricketBeaconID) throws NoNewDataException {
		if (!cricketPositions.containsKey(cricketBeaconID))
			throw new NoNewDataException("Cricket Beacon " + cricketBeaconID + " does not exist in the configuration.");
		
		CricketBeaconReading br = beaconReadings.get(cricketBeaconID);
		if (br == null) 
			throw new NoNewDataException("Cricket Beacon " + cricketBeaconID + " has no new data available.");
		return br;
	}
	
	
	public CricketBeaconReading getLastReading(PlayerPoint2d coord) throws NoNewDataException {
		Iterator<Entry<String, PlayerPoint3d>> iter = getCricketPositions().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, PlayerPoint3d> e = iter.next();
			PlayerPoint3d coord3d = e.getValue();
			if (coord3d.getPx() == coord.getPx() && coord3d.getPy() == coord.getPy()) { // found the request cricket beacon
				return getLastReading(e.getKey());
			}
		}
		throw new NoNewDataException("Cricket Beacon at (" + coord.getPx() + "," + coord.getPy() + ") does not exist in the configuration.");
	}
	
	
	public CricketBeaconReading getLastReading(PlayerPoint3d coord) throws NoNewDataException {
		return getLastReading(new PlayerPoint2d(coord.getPx(), coord.getPy()));
	}
    
    
	/**
	 * Currently does nothing
	 */
	public void run() {
		Logger.log("thread starting...");
		while(running) {
	
			
			try {
				synchronized(this) {
					wait(CRICKET_BUFFER_REFRESH_PERIOD);
				}
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		Logger.log("thread terminating...");
	}

	
	@Override
	public void newCricketData(CricketData cd) {
		if (!cd.getConnection())
			return;
			
		boolean recognized = true;
		String id = cd.getCricketID();
		PlayerPoint3d coord = cricketPositions.get(id);
		if (coord == null) {
			recognized = false;
			Logger.log("New Cricket Data from " + id + ", unrecognized CricketID.");
			return;
		}
		
		double x = coord.getPx();
		double y = coord.getPy();
		
		if (!beaconReadings.containsKey(id)) { // is the first time reading from this beacon
			Logger.logDbg("Found Cricket Mote " + (recognized ? ("at (" + x + "," + y + "),") : "with unrecognized") + " ID=" + id);
		}
		
		beaconReadings.put(id, new CricketBeaconReading(System.currentTimeMillis(), x, y, (double)(cd.getDistance()/100))); // convert distance from cm to meters	
	}
	
	
	
	public Set<Entry<String, PlayerPoint3d>> getCricketPositions() {
		return cricketPositions.entrySet();
	}
	
	
	public PlayerPoint3d getCricketBeaconCoord3d(String cricketID) {
		return cricketPositions.get(cricketID);
	}
	
	public PlayerPoint2d getCricketBeaconCoord2d(String cricketID) {
		PlayerPoint3d coord = cricketPositions.get(cricketID);
		return new PlayerPoint2d(coord.getPx(), coord.getPy());
	}
	
	
	
//	private void log(String msg) {
//		String result = "CricketBuffer: " + msg;
//		if (System.getProperty ("PharosMiddleware.debug") != null)
//			System.out.println(result);
//		if (flogger != null) 
//			flogger.log(result);
//	}
}