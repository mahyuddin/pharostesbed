package pharoslabut.sensors;

import java.util.*;

import pharoslabut.exceptions.NoNewDataException;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.Location;

import playerclient3.*;
import playerclient3.structures.gps.PlayerGpsData;

/**
 * Buffers incoming GPS data.  Implements a Pharos-specific buffer for GPS data.
 * Provides method getCurrLoc() that queries the GPS device for the most recent
 * GPS measurements.  If such a measurement is not available, it returns the
 * previous measurement so long as its age is not over GPS_MAX_AGE milliseconds.
 * 
 * @author Chien-Liang Fok
 */
public class GPSDataBuffer {
	
	/**
	 * The period in milliseconds at which to check for expired compass data.
	 */
//	public static final int GPS_BUFFER_REFRESH_PERIOD = 200;
	
	/**
	 * The maximum age of GPS readings stored in this buffer, in milliseconds.
	 */
	public static final int GPS_MAX_AGE = 2500;
	
	/**
	 * A proxy to the GPS sensor.
	 */
	private GPSInterface gps;
	
	/**
	 * A buffer for storing previous GPS measurements that are up to GPS_MAX_AGE old.
	 */
	private Vector<GPSDataWrapper> buff = new Vector<GPSDataWrapper>();
	
//	private boolean running = false;
	
	/**
	 * The file logger in which to save log data.
	 */
//	private FileLogger flogger;
	
//	private Vector<GPSListener> gpsListeners = new Vector<GPSListener>();
	
	/**
	 * A constructor.
	 * 
	 * @param gps The GPS proxy object.
	 */
	public GPSDataBuffer(GPSInterface gps) {
		this.gps = gps;
	}
	
//	/**
//	 * A constructor with a flogger input parameter.
//	 * 
//	 * @param gps The GPS proxy object.
//	 * @param flogger The file logger used to store debug statements.
//	 */
//	public GPSDataBuffer(GPSInterface gps, FileLogger flogger) {
//		this(gps);
//		this.flogger = flogger;
//	}
	
    /**
     * Adds a listener for GPS data.  All listeners are notified whenever
     * a new GPS data arrives.
     * 
     * @param gpsl  The listener to add.
     */
//    public void addGPSListener(GPSListener gpsl) {
//    	gpsListeners.add(gpsl);
//    }
    
    /**
     * Removes a listener from this object.
     * 
     * @param gpsl  The listener to remove.
     */
//    public void removeGPSListener(GPSListener gpsl) {
//    	gpsListeners.remove(gpsl);
//    }
    
    /**
     * Notifies each of the registered GPSListener objects that a new PlayerGpsData is available.
     * 
     * @param pgdata The new PlayerGpsData that is available.
     */
//    private void notifyGPSListeners(final PlayerGpsData pgdata) {
//    	if (gpsListeners.size() > 0) {
//    		new Thread(new Runnable() {
//    			public void run() {
//    				Enumeration<GPSListener> e = gpsListeners.elements();
//    	    		while (e.hasMoreElements()) {
//    	    			e.nextElement().newGPSData(pgdata);
//    	    		}
//    			}
//    		}).start();
//    	}
//    }
	
    /**
     * Determines whether the specified location is valid.  For now, it is hard-coded that valid
     * locations have the following format:  
     * The longitude must be -97.xxx degrees, and the latitude to be 30.xxx degrees.
     * 
     * @param loc The location to check
     * @return True if the location is valid.
     */
	public static boolean isValid(Location loc) {
		if (loc == null) return false; // a null location is inherently invalid
		
		// For now, constrain the longitude to be -97.xxx degrees, and the latitude to be 30.xxx degrees...
		if (((int)loc.longitude()) != -97 || ((int)loc.latitude()) != 30) {
			return false;
		} else
			return true;
	}
	
//	/**
//	 * Sets the file logger.  If the file logger is not null, this component
//	 * logs GPS data to a file.
//	 * 
//	 * @param flogger The file logger.  This may be null.
//	 */
//	public void setFileLogger(FileLogger flogger) {
//		this.flogger = flogger;
//	}
	
	/**
	 * Start the reception of GPS data.
	 */
//	public synchronized void start() {
//		if (!running) {
//			running = true;
//			new Thread(this).start();
//		}
//	}
	
	/**
	 * Stop the reception of GPS data.
	 */
//	public synchronized void stop() {
//		if (running) {
//			buff.clear();
//			running = false;
//		}
//	}
	
//	private synchronized void add(PlayerGpsData gpsData) {
//		buff.add(0, gpsData);
//		if (buff.size() > bufferSize) 
//			buff.remove(buff.size()-1);
//	}
	
	/**
	 * Returns the current location.  It first checks whether new GPS data
	 * is available.  If it is, it returns the new GPS data.  Otherwise, it returns
	 * the previous GPS data that has not expired.
	 * 
	 * @throws NoNewDataException If no GPS data exists.
	 */
	public synchronized PlayerGpsData getCurrLoc() throws NoNewDataException {
		// Grab any new GPS data...
		if (gps.isDataReady()) {
			PlayerGpsData newData = gps.getData();
			buff.add(0, new GPSDataWrapper(newData)); // add new data to the front of the buffer
			Logger.log("New GPS Data: " + newData + ", buffer size=" + buff.size());
			
			// Estimate the robot's speed
			if (buff.size() > 1) {
				Location currLoc = new Location(buff.get(0).getGpsData());
				Location prevLoc = new Location(buff.get(1).getGpsData());
				
				Double dist = currLoc.distanceTo(prevLoc);
				double time = (buff.get(0).getTimeStamp() - buff.get(1).getTimeStamp()) / 1000.0;
				Logger.log("Estimated Speed: " + dist/time + " m/s");
			}
		}
		
		// Remove any expired GPS location measurements...
		removeExpiredElements();
		//Logger.log("buffer size after clearing expired elements: " + buff.size());
		
		if (buff.size() > 0)
			return buff.get(0).getGpsData();
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
//	public void run() {
//		
//		log("run: thread starting...");
//		
//		while(running) {
//			
//			// Grab any new GPS data...
//			if (gps.isDataReady()) {
//				PlayerGpsData newData = gps.getData();
//				buff.add(0, new GPSDataWrapper(newData)); // add new data to the front of the buffer
//				log("run: New GPS Data: " + newData + ", buffer size=" + buff.size());
//				
//				// Estimate the robot's speed
//				if (buff.size() > 1) {
//					Location currLoc = new Location(buff.get(0).getGpsData());
//					Location prevLoc = new Location(buff.get(1).getGpsData());
//					
//					Double dist = currLoc.distanceTo(prevLoc);
//					double time = (buff.get(0).getTimeStamp() - buff.get(1).getTimeStamp()) / 1000.0;
//					log("run: Speed (m/s): " + dist/time);
//				}
//				
//				// Notify the listeners of the new GPS data
//				notifyGPSListeners(newData);
//			}
//			
//			removeExpiredElements();
//			
//			try {
//				synchronized(this) {
//					wait(GPS_BUFFER_REFRESH_PERIOD);
//				}
//			} catch(InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//		
//		log("run: thread terminating...");
//	}
//	
//	private void log(String msg) {
//		String result = "GPSDataBuffer: " + msg;
//		if (System.getProperty ("PharosMiddleware.debug") != null)
//			System.out.println(result);
//		if (flogger != null) 
//			flogger.log(result);
//	}
	
	/**
	 * Wraps a GPS location measurement with a time stamp.  This is used instead of
	 * the time stamp within the PlayerGpsData object since the local system clock may
	 * not be calibrated.
	 * 
	 * @author Chien-Liang Fok
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
		 * @return The location data.
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