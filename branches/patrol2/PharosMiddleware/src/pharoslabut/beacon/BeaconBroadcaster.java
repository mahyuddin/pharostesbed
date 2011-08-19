package pharoslabut.beacon;

//import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;

/**
 * The super class of all beacon broadcasters.
 * 
 * @author Chien-Liang Fok
 *
 */
public abstract class BeaconBroadcaster implements Runnable {
    /**
     * The minimum period between beacon broadcasts in ms. 
     * The default minimum broadcasting period is 5 second2.
     */
    protected long minPeriod = 5000;
    
    /**
     * The maximum period between beacon broadcasts in ms.
     * The default maximum broadcasting period is 10 seconds.
     */
    protected long maxPeriod = 10000;
    
    /**
     * The tx power level.  Default is 31.
     */
    protected short txPower = 31;
    
    protected boolean running = false;
    
//    protected FileLogger flogger;
    
    /**
     * Generates a random value between minPeriod and maxPeriod.
     * 
     * @return a random value between minPeriod and maxPeriod.
     */
    protected long randPeriod() {
    	long diff = maxPeriod - minPeriod;
    	double random = Math.random();
    	return (long)(random * diff + minPeriod);
    }
    
    /**
     * Starts the broadcasting of beacons.
     * 
     * @param minPeriod The minimum beacon period.
     * @param maxPeriod The maximum beacon period.
     * @param txPower The transmit power level.
     * @return true if successful.
     */
    public boolean start(long minPeriod, long maxPeriod, short txPower) {
    	
    	if (!running) {
    		running = true;
    		
    		this.minPeriod = minPeriod;
    	    this.maxPeriod = maxPeriod;
    	    this.txPower = txPower;
    	        
    		new Thread(this).start();
    		
    		return true;
    	} else {
    		Logger.logErr("Tried to start the beacon broadcaster when it was already running.");
    		return false;
    	}
    }
    
    /**
     * Stops the broadcasting of beacons.
     */
    public void stop() {
    	running = false;
    }
    
//    /**
//     * Updates the file logger.
//     * 
//     * @param flogger The file logger.
//     */
//    public void setFileLogger(FileLogger flogger) {
//    	this.flogger = flogger;
//    }
    
    /**
     * Records a message using the file logger.
     * 
     * @param msg The message to record.
     */
//    protected abstract void log(String msg);
    
    /**
     * Sends a beacon.  This is implemented by subclasses.
     */
    protected abstract void sendBeacon();
    
    /**
     * Performs the broadcasting of a beacon to a multicast address.
     */
    public void run() {
    	while (running) {
    		sendBeacon();
    		try {
    			synchronized(this) {
    				long delayPeriod = randPeriod();
    				Logger.logDbg("Time till next beacon = " + delayPeriod);
    				wait(delayPeriod);
    			}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    }
}
