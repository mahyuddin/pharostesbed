package pharoslabut.sensors;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

import pharoslabut.exceptions.NoNewDataException;
import pharoslabut.logger.FileLogger;
import playerclient3.*;
import playerclient3.structures.position2d.PlayerPosition2dData;

/**
 * Continuously checks for new compass data and stores it
 * in a buffer.  Provides access to the current compass reading.
 * 
 * @author Chien-Liang Fok
 */
public class CompassDataBuffer implements Runnable {
	
	/**
	 * The period in milliseconds at which to check for expired compass data.
	 */
	public static final int COMPASS_BUFFER_REFRESH_PERIOD = 100;
	
	/**
	 * The size of the compass buffer.
	 */
	public static final int COMPASS_BUFFER_SIZE = 3;
	
	/**
	 * The maximum age in milliseconds of compass readings stored in this buffer.
	 */
	public static final long COMPASS_MAX_AGE = 800;
	
	/**
	 * The time at which the last sensor reading was received.
	 */
	private long lastTimeStamp = System.currentTimeMillis();
	
	/**
	 * A proxy to the compass device.
	 */
	private Position2DInterface compass;
	
	/**
	 * A circular buffer for storing new compass heading values.
	 */
	private double headingBuffer[] = new double[COMPASS_BUFFER_SIZE];
	
	/**
	 * The number of elements in the headingBuffer.
	 */
	private int headingBufferSize = 0;
	
	/**
	 * The index in which to save the next compass reading.
	 */
	private int headingBufferIndx = 0;
	
	/**
	 * Listeners for compass events.
	 */
	private Vector<Position2DListener> pos2dListeners = new Vector<Position2DListener>();
	
	/**
	 * Whether the thread that reads compass data is running.
	 */
	private boolean running = false;
	
	/**
	 * The file logger for saving debug data.
	 */
	private FileLogger flogger;
	
	/**
	 * The constructor.
	 * 
	 * @param compass The proxy to the compass device.
	 */
	public CompassDataBuffer(Position2DInterface compass) {
		this.compass = compass;
	}
	
	/**
	 * Starts the CompassDataBuffer.  Creates a thread for reading compass data.
	 */
	public synchronized void start() {
		if (!running) {
			running = true;
			new Thread(this).start();
		}
	}
	
	/**
	 * Stops the CompassDataBuffer.  Allows the thread that reads compass data to terminate.
	 */
	public synchronized void stop() {
		if (running) {
			clearHeadingBuffer();
			running = false;
		}
	}
	
	/**
	 * Clears the heading buffer.
	 */
	private void clearHeadingBuffer() {
		headingBufferSize = 0;
		headingBufferIndx = 0;
	}
	
    /**
     * Add a listener to this object.  This listener is notified each time a new PlayerPosition2dData 
     * is available.
     * 
     * @param listener  The listener to add.
     */
    public void addPos2DListener(Position2DListener listener) {
    	pos2dListeners.add(listener);
    }
    
    /**
     * Removes a listener from this object.
     * 
     * @param listener The listener to remove.
     */
    public void removePos2DListener(Position2DListener listener) {
    	pos2dListeners.remove(listener);
    }
    
    /**
     * Notifies each of the registered Position2DListeners that a new PlayerPosition2dData is available.
     */
    private void notifyP2DListeners(final PlayerPosition2dData pp2ddata) {
    	if (pos2dListeners.size() > 0) {
    		//log("notifyP2DListeners: Notifying listeners of new compass data...");
    		new Thread(new Runnable() {
    			public void run() {
    				Enumeration<Position2DListener> e = pos2dListeners.elements();
    				while (e.hasMoreElements()) {
    					e.nextElement().newPlayerPosition2dData(pp2ddata);
    				}
    			}

    		}).start();
    	} else
    		log("notifyP2DListeners: No listeners present...");
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
	 * Returns the median compass measurement.
	 * 
	 * @param filterLength The size of the window over which to calculate the median.
	 * @return The median compass measurement over the window defined by filterLength
	 * @throws NoNewDataException If no compass data was received.
	 */
	public synchronized double getMedian(int filterLength) throws NoNewDataException {
		
		// Just in case there's new data...
		getNewData();
		
		if (headingBufferSize == 0)
			throw new NoNewDataException();
		
		long age = System.currentTimeMillis() - lastTimeStamp;
		if (age > COMPASS_MAX_AGE) {
			String errorMsg = "getMedian: ERROR: Max age exceeded (" + age + " > " + COMPASS_MAX_AGE + ")"; 
			log(errorMsg);
			throw new NoNewDataException(errorMsg);
		}
		
		// There is at least one piece of data in the buffer
		if (headingBufferSize == 1)
			return headingBuffer[0];
		
		else if (headingBufferSize == 2)
			return headingBuffer[1];
		
		else {
			
			// Create a temporary array storing the data...
			Double[] data = new Double[headingBufferSize];
			for (int i=0; i < headingBufferSize; i++) {
				data[i] = headingBuffer[i];
			}
			
			Arrays.sort(data);
			
			int midIndx = (int) Math.floor(headingBufferSize / 2.0);
			return data[midIndx];
		}
	}
	
	/**
	 * Checks whether the compass proxy object has new compass data and grabs the data if it does.
	 */
	private synchronized void getNewData() {
		// If new compass data is available, get it!
		if (compass.isDataReady()) {
			PlayerPosition2dData newData = compass.getData();
			double newHeading = newData.getPos().getPa();
			
			headingBuffer[headingBufferIndx] = newHeading;
			
			// Update the headingBufferIndx
			headingBufferIndx++;
			headingBufferIndx %= COMPASS_BUFFER_SIZE;
			
			// Update the number of elements in the compass buffer
			headingBufferSize++;
			if (headingBufferSize > COMPASS_BUFFER_SIZE) 
				headingBufferSize = COMPASS_BUFFER_SIZE;
			
			// Update the last time stamp and add a log statement
			lastTimeStamp = System.currentTimeMillis();
			log("getNewData: New heading=" + newHeading + ", buffer size=" + headingBufferSize + ", headingBufferIndx=" + headingBufferIndx);
			
			// Notify the listeners
			notifyP2DListeners(newData);
		}
	}
	
	/**
	 * Removes expired compass readings.
	 */
	public void run() {
		while(running) {
			
			// Proactively grab new compass data before 
			// cleaning up the old data.
			getNewData();
			
			// Check if we've exceeded the max age
			long age = System.currentTimeMillis() - lastTimeStamp;
			if (headingBufferSize > 0 && age > COMPASS_MAX_AGE) {
				log("run: ERROR: max sensor age reached, flushing buffer!");
				clearHeadingBuffer();
			}
			
			try {
				synchronized(this) {
					wait(COMPASS_BUFFER_REFRESH_PERIOD);
				}
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/** 
	 * Determinds whether a heading measurement is valid.  It is valid if it
	 * falls between -pi/2 and +pi/2.
	 * 
	 * @param heading The heading measurement.
	 * @return true if the heading measurement is valid.
	 */
	public static final boolean isValid(double heading) {
		return heading <= Math.PI && heading >= -Math.PI;
	}
	
	private void log(String msg) {
		String result = "CompassDataBuffer: " + msg;
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
		if (flogger != null) 
			flogger.log(result);
	}
}