package pharoslabut.navigate;

import java.util.Arrays;
import java.util.Vector;

import pharoslabut.logger.FileLogger;
import playerclient.*;
import playerclient.structures.position2d.PlayerPosition2dData;

/**
 * Implements a thread that continuously reads the compass reading and stores it
 * in a buffer.  Provides access to the current compass reading.
 * 
 * @author Chien-Liang Fok
 */
public class CompassDataBuffer implements Runnable, Position2DListener {
	/**
	 * The maximum age in milliseconds of compass readings stored in this buffer.
	 */
	//public static final int COMPASS_MAX_AGE = 1000;
	public static final int COMPASS_MAX_AGE = 500;
	//public static final int COMPASS_MAX_AGE = 200;
	
	/**
	 * The period in milliseconds at which to check for expired compass data.
	 */
	//public static final int COMPASS_BUFFER_SWEEP_PERIOD = 1000;
	//public static final int COMPASS_BUFFER_SWEEP_PERIOD = 500;
	public static final int COMPASS_BUFFER_SWEEP_PERIOD = 100;
	
	private Position2DInterface compass;
	private Vector<CompassDataWrapper> buff = new Vector<CompassDataWrapper>();
	private boolean running = false;
	private FileLogger flogger;
	
	/**
	 * The constructor.
	 * 
	 * @param compass The proxy to the compass device
	 */
	public CompassDataBuffer(Position2DInterface compass) {
		this.compass = compass;
		start();
	}
	
	public synchronized void start() {
		if (!running) {
			running = true;
			compass.addPos2DListener(this);
			new Thread(this).start();
		}
	}
	
	public synchronized void stop() {
		if (running) {
			compass.removePos2DListener(this);
			buff.clear();
			running = false;
		}
	}
	
	/**
	 * Sets the file logger.  If the file logger is not null, this component
	 * logs compass data to a file.
	 * 
	 * @param flogger The file logger.  This may be null.
	 */
	public void setFileLogger(FileLogger flogger) {
		this.flogger = flogger;
	}
	
	/**
	 * Returns the median compass measurement in the set defined by the last filterlength
	 * measurements received.
	 * 
	 * @param filterLength The size of the window over which to calculate the median.
	 * @return The median compass measurement over the window defined by filterLength
	 * @throws NoNewDataException If no compass data was received.
	 */
	public synchronized double getMedian(int filterLength) throws NoNewDataException {
		if (buff.size() > 0) {
			int endIndx = (buff.size() > filterLength ? filterLength : buff.size());
			CompassDataWrapper[] wc = buff.subList(0, endIndx).toArray(new CompassDataWrapper[] {}); // create working copy
			
			// TODO: remove this later by making CompassDataWrapper sortable
			Double[] data = new Double[wc.length];
			for (int i=0; i < data.length; i++) {
				data[i] = wc[i].getHeading();
			}
			
			int midIndx = (int)Math.floor(wc.length/2.0);
			
			//Arrays.sort(wc);
			//return wc[midIndx];
			
			Arrays.sort(data);
			return data[midIndx];
			
		} else
			throw new NoNewDataException();
	}
	
	@Override
	public void newPlayerPosition2dData(PlayerPosition2dData data) {
		CompassDataWrapper heading = new CompassDataWrapper(data.getPos().getPa()); 
		synchronized(this) {
			buff.add(0, heading);
		}
		log("New heading: " + heading.getHeading() + ", buffer size=" + buff.size());
	}
	
	/**
	 * Traverses the compass data buffer and removes the elements that have expired.
	 */
	private synchronized void removeExpiredElements() {
		
		for (int i=0; i < buff.size(); i++) {
			if (buff.get(i).isOlderThan(COMPASS_MAX_AGE))
				buff.remove(i);
		}
		log("Removed expired elements, buffer size: " + buff.size());
	}
	
	/**
	 * Removes expired compass readings.
	 */
	public void run() {
		while(running) {
			removeExpiredElements();
			try {
				synchronized(this) {
					wait(COMPASS_BUFFER_SWEEP_PERIOD);
				}
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void log(String msg) {
		String result = "CompassDataBuffer: " + msg;
		//if (System.getProperty ("PharosMiddleware.debug") != null)
		//	System.out.println(result);
		if (flogger != null) 
			flogger.log(result);
	}
	
	/**
	 * Wraps a compass heading measurement with a timestamp.
	 * 
	 * @author Chien-Liang Fok
	 *
	 */
	private class CompassDataWrapper {
		private double heading;
		private long timeStamp;
		
		public CompassDataWrapper(double heading) {
			this.heading = heading;
			this.timeStamp = System.currentTimeMillis();
		}
		
		/**
		 * An accessor to the heading measurement.
		 * 
		 * @return The heading.
		 */
		public double getHeading() {
			return heading;
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