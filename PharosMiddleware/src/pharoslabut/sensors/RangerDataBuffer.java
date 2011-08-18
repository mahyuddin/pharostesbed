package pharoslabut.sensors;

import java.util.Enumeration;
import java.util.Vector;

import pharoslabut.logger.Logger;

import playerclient3.RangerInterface;
import playerclient3.structures.ranger.PlayerRangerData;

/**
 * Polls for IR data and generates events whenever IR data is received.
 * 
 * @author Chien-Liang Fok
 */
public class RangerDataBuffer implements Runnable {
	public static final long RANGER_BUFFER_REFRESH_PERIOD = 100;
	
	private RangerInterface ranger;
	
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
	 * @param compass The proxy to the compass device.
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
     * Notifies each of the registered GPSListener objects that a new PlayerGpsData is available.
     * 
     * @param pgdata The new PlayerGpsData that is available.
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
		if (running)
			running = false;
	}
	
	/**
	 * Removes expired compass readings.
	 */
	public void run() {
		
		Logger.log("thread starting...");
		
		while(running) {
			
			// If new compass data is available, get it and notify the listeners.
			if (ranger.isDataReady()) {
				PlayerRangerData newData = ranger.getData();
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
	
//	private void log(String msg) {
//		String result = "RangerDataBuffer: " + msg;
//		if (System.getProperty ("PharosMiddleware.debug") != null)
//			System.out.println(result);
//		if (flogger != null) 
//			flogger.log(result);
//	}
}
