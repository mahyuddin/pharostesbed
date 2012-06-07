package pharoslabut.sensors;

import java.util.Enumeration;
import java.util.Vector;

import pharoslabut.exceptions.NoNewDataException;
import pharoslabut.logger.Logger;

import playerclient3.RangerInterface;
import playerclient3.structures.position2d.PlayerPosition2dData;
import playerclient3.structures.ranger.PlayerRangerData;

/**
 * Polls for IR data and generates events whenever IR data is received.
 * 
 * @author Chien-Liang Fok
 */
public class RangerDataBuffer implements Runnable {
	
	/**
	 * Defines this buffer's cycle time in milliseconds.
	 */
	//public static final long RANGER_BUFFER_REFRESH_PERIOD = 100; // 10Hz
	//public static final long RANGER_BUFFER_REFRESH_PERIOD = 10; // 100Hz
	public static final long RANGER_BUFFER_REFRESH_PERIOD = 5; // 200Hz
	
	private RangerInterface ranger;
	
	/**
	 * The time at which the last Position2D device reading was received.
	 */
	private long lastTimeStamp = System.currentTimeMillis();
	
	/**
	 * The most recent Position2D device reading.
	 */
	private PlayerRangerData recentRangerReading = null;
	
	/**
	 * Whether the thread that reads compass data is running.
	 */
	private boolean running = false;
	
	/**
	 * Listeners for range events.
	 */
	private Vector<RangerListener> listeners = new Vector<RangerListener>();
	
	/**
	 * The constructor.
	 * 
	 * @param ranger The proxy to the IR device.
	 */
	public RangerDataBuffer(RangerInterface ranger) {
		this.ranger = ranger;
	}
	
    /**
     * Adds a listener for range data.  All listeners are notified whenever
     * a new range data arrives.
     * 
     * @param rl  The listener to add.
     */
    public void addRangeListener(RangerListener rl) {
    	listeners.add(rl);
    }
    
    /**
     * Removes a listener from this object.
     * 
     * @param rl  The listener to remove.
     */
    public void removeGPSListener(RangerListener rl) {
    	listeners.remove(rl);
    }
    
    /**
     * Notifies each of the registered RangerListener objects that a new PlayerRangerData is available.
     * 
     * @param pgdata The new PlayerRangerData that is available.
     */
    private void notifyRangerListeners(final PlayerRangerData prdata) {
    	if (listeners.size() > 0) {
    		new Thread(new Runnable() {
    			public void run() {
    				Enumeration<RangerListener> e = listeners.elements();
    	    		while (e.hasMoreElements()) {
    	    			e.nextElement().newRangerData(prdata);
    	    		}
    			}
    		}).start();
    	}
    }
    
	/**
	 * Starts the RangerDataBuffer.  Creates a thread for reading ranger data.
	 */
	public synchronized void start() {
		if (!running) {
			running = true;
			new Thread(this).start();
		}
	}
	
	/**
	 * Stops the RangerDataBuffer.  Allows the thread that reads ranger data to terminate.
	 */
	public synchronized void stop() {
		if (running)
			running = false;
	}
	
	
    /**
	 * Returns the most recent Ranger device reading.
	 * 
	 * @return The most recent Position2D device reading
	 * @throws NoNewDataException If no new data was received (the most recent reading is null).
	 */
	public synchronized PlayerRangerData getRecentData() throws NoNewDataException {
		if (recentRangerReading == null)
			throw new NoNewDataException("No Ranger data available yet.");
		return recentRangerReading;
	}
	
	
	
	/**
	 * Removes expired ranger readings.
	 */
	public void run() {
		Logger.log("thread starting...");
		
		while(running) {
			
			// If new compass data is available, get it and notify the listeners.
			if (ranger.isDataReady()) {
				PlayerRangerData newData = ranger.getData();
				
				recentRangerReading = newData;
				lastTimeStamp = System.currentTimeMillis();
				
				notifyRangerListeners(newData);
			}
			
			try {
				synchronized(this) {
					wait(RANGER_BUFFER_REFRESH_PERIOD);
				}
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		Logger.log("thread terminating...");
	}
}
