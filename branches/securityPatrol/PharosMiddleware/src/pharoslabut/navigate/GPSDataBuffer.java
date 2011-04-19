package pharoslabut.navigate;

import java.util.*;
import pharoslabut.logger.*;
import playerclient.*;
import playerclient.structures.gps.*;

/**
 * Buffers incoming GPS data.  Implements a thread that cleans up "old" GPS readings, which are readings older
 * than GPS_MAX_AGE.  Provides access to the current GPS data.
 * 
 * @author Chien-Liang Fok
 */
public class GPSDataBuffer implements Runnable, GPSListener {
	
	/**
	 * The maximum age of GPS readings stored in this buffer, in milliseconds.
	 */
	public static final int GPS_MAX_AGE = 5000;
	
	/**
	 * The period in milliseconds at which to check for expired GPS data.
	 */
	public static final int GPS_BUFFER_SWEEP_PERIOD = 1500;
	
	private GPSInterface gps;
	private Vector<GPSDataWrapper> buff = new Vector<GPSDataWrapper>();;
	private boolean running = false;
	private FileLogger flogger;
	
	/**
	 * The constructor.
	 * 
	 * @param gps The GPS proxy object.
	 */
	public GPSDataBuffer(GPSInterface gps) {
		this.gps = gps;
		start();
	}
	
	public static boolean isValid(Location loc) {
		if (loc == null) return false; // a null location is inherently invalid
		// For now, let's constrain the longitude to be -97.xxx degrees, and the latitude to be 30.xxx degrees
		if (((int)loc.longitude()) != -97 || ((int)loc.latitude()) != 30) {
			//log("isValid(...): Invalid loc: " + loc);
			return false;
		} else
			return true;
	}
	
	/**
	 * Sets the file logger.  If the file logger is not null, this component
	 * logs GPS data to a file.
	 * 
	 * @param flogger The file logger.  This may be null.
	 */
	public void setFileLogger(FileLogger flogger) {
		this.flogger = flogger;
	}
	
	/**
	 * Start the reception of GPS data.
	 */
	public synchronized void start() {
		if (!running) {
			gps.addGPSListener(this);
			running = true;
			new Thread(this).start();
		}
	}
	
	/**
	 * Stop the reception of GPS data.
	 */
	public synchronized void stop() {
		if (running) {
			gps.removeGPSListener(this);
			buff.clear();
			running = false;
		}
	}
	
//	private synchronized void add(PlayerGpsData gpsData) {
//		buff.add(0, gpsData);
//		if (buff.size() > bufferSize) 
//			buff.remove(buff.size()-1);
//	}
	
	/**
	 * Returns the current location.
	 * 
	 * @throws NoNewDataException If no GPS data exists in the buffer.
	 */
	public synchronized Location getCurrLoc() throws NoNewDataException {
		if (buff.size() > 0)
			return new Location(buff.get(0).getGpsData());
		else
			throw new NoNewDataException();
	}
	
	/**
	 * Traverses the GPS data buffer and removes the elements that have expired.
	 */
	private synchronized void removeExpiredElements() {
		for (int i=0; i < buff.size(); i++) {
			if (buff.get(i).isOlderThan(GPS_MAX_AGE))
				buff.remove(i);
		}
	}
	
	/**
	 * Removes expired GPS readings.
	 */
	public void run() {
		while(running) {
			removeExpiredElements();
			try {
				synchronized(this) {
					wait(GPS_BUFFER_SWEEP_PERIOD);
				}
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void newGPSData(PlayerGpsData gpsData) {
		buff.add(0, new GPSDataWrapper(gpsData)); // add new data to the front of the buffer
		log("New GPS Data: " + gpsData + ", buffer size=" + buff.size());
		if (buff.size() > 1) {
			Location currLoc = new Location(buff.get(0).getGpsData());
			Location prevLoc = new Location(buff.get(1).getGpsData());
			
			Double dist = currLoc.distanceTo(prevLoc);
			double time = (buff.get(0).getTimeStamp() - buff.get(1).getTimeStamp()) / 1000.0;
			log("Speed (m/s): " + dist/time);
		}
	}
	
	private void log(String msg) {
		String result = "GPSDataBuffer: " + msg;
//		if (System.getProperty ("PharosMiddleware.debug") != null)
//			System.out.println(result);
		if (flogger != null) 
			flogger.log(result);
	}
	
	/**
	 * Wraps a GPS location measurement with a time stamp.  This is used instead of
	 * the time stamp within the PlayerGpsData object since the local clock may
	 * not be calibrated.
	 * 
	 * @author Chien-Liang Fok
	 *
	 */
	private class GPSDataWrapper {
		private PlayerGpsData data;
		private long timeStamp;
		
		public GPSDataWrapper(PlayerGpsData data) {
			this.data = data;
			this.timeStamp = System.currentTimeMillis();
		}
		
		public long getTimeStamp() {
			return timeStamp;
		}
		
		/**
		 * An accessor to the GPS data.
		 * 
		 * @return The heading.
		 */
		public PlayerGpsData getGpsData() {
			return data;
		}
		
		/**
		 * Returns true if the GPS data stored in this object is older than the
		 * specified age.
		 * 
		 * @param age The age in milliseconds.
		 * @return True if the GPS data stored in this object is older than age.
		 */
		public boolean isOlderThan(long age) {
			long delta = System.currentTimeMillis() - timeStamp;
			return delta > age;
		}
	}
}