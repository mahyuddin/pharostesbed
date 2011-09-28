package pharoslabut.sensors;

import java.util.Enumeration;
import java.util.Vector;

//import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;

import playerclient3.Position2DInterface;
import playerclient3.structures.position2d.PlayerPosition2dData;

/**
 * Polls for new Position2D data and generates an event whenever new data arrives.
 * 
 * @author Chien-Liang Fok
 */
public class Position2DBuffer implements Runnable {
	
	/**
	 * The period in milliseconds at which to check for expired compass data.
	 */
	public static final int POSITION2D_BUFFER_REFRESH_PERIOD = 100;
	
	/**
	 * A proxy to the compass device.
	 */
	private Position2DInterface pos2di;
	
	/**
	 * Listeners for compass events.
	 */
	private Vector<Position2DListener> pos2dListeners = new Vector<Position2DListener>();
	
	/**
	 * Whether the thread that reads compass data is running.
	 */
	private boolean running = false;
	
//	/**
//	 * The file logger for saving debug data.
//	 */
//	private FileLogger flogger;
	
	/**
	 * The constructor.
	 * 
	 * @param p2di The proxy to the Position2D device.
	 */
	public Position2DBuffer(Position2DInterface p2di) {
		this.pos2di = p2di;
	}
	
	/**
	 * Starts the Position2DBuffer.  Creates a thread for reading compass data.
	 */
	public synchronized void start() {
		if (!running) {
			Logger.log("starting...");
			running = true;
			new Thread(this).start();
		} else
			Logger.log("already started...");
	}
	
	/**
	 * Stops the Position2DBuffer.  Allows the thread that reads compass data to terminate.
	 */
	public synchronized void stop() {
		if (running) {
			Logger.log("stopping...");
			running = false;
		} else
			Logger.log("already stopped...");
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
    		new Thread(new Runnable() {
    			public void run() {
    				Enumeration<Position2DListener> e = pos2dListeners.elements();
    				while (e.hasMoreElements()) {
    					e.nextElement().newPlayerPosition2dData(pp2ddata);
    				}
    			}

    		}).start();
    	}
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
	 * Removes expired compass readings.
	 */
	public void run() {
		Logger.log("thread starting...");
		while(running) {
			
			// If new Position2D data is available, get it!
			if (pos2di.isDataReady()) {
				PlayerPosition2dData newData = pos2di.getData();
				
				// Notify the listeners
				notifyP2DListeners(newData);
			}
			
			try {
				synchronized(this) {
					wait(POSITION2D_BUFFER_REFRESH_PERIOD);
				}
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		Logger.log("thread terminating...");
	}
	
//	private void log(String msg) {
//		String result = "Position2DBuffer: " + msg;
//		if (System.getProperty ("PharosMiddleware.debug") != null)
//			System.out.println(result);
//		if (flogger != null) 
//			flogger.log(result);
//	}
}