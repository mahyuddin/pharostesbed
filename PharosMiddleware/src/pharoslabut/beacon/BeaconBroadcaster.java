package pharoslabut.beacon;

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
     * This is only used by the cc2420 radio (not the WiFi radio).
     */
    protected short txPower = 31;
    
    /**
     * Whether this broadcaster is running.
     */
    protected boolean running = false;
    
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
    	        
    	    Logger.log("Starting to beacon, minPeriod = " + minPeriod + ", maxPeriod = " + maxPeriod + ", tx power = " + txPower);
    		new Thread(this).start();
    		
    		return true;
    	} else {
    		Logger.logErr("Tried to start the beacon broadcaster when it was already running.");
    		return false;
    	}
    }
    
    /**
     * Starts the broadcasting of beacons at the default power level.
     * 
     * @param minPeriod The minimum beacon period.
     * @param maxPeriod The maximum beacon period.
     * @return true if successful.
     */
    public boolean start(long minPeriod, long maxPeriod) {
    	return start(minPeriod, maxPeriod, txPower);
    }
    
    /**
     * Stops the broadcasting of beacons.
     */
    public void stop() {
    	running = false;
    }
    
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
