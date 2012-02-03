package pharoslabut.sensors;

import java.util.Enumeration;
import java.util.Vector;

//import pharoslabut.logger.Logger;
import playerclient3.BlobfinderInterface;
import playerclient3.structures.blobfinder.PlayerBlobfinderData;

/**
 * Reads in blob data and distributes it among all registered consumers.
 * 
 * @author Chien-Liang Fok
 */
public class BlobDataProvider implements Runnable {
	public static int CYCLE_PERIOD_MS = 5; // 200Hz
	private BlobfinderInterface bfi;
	private boolean done = false;
	private Vector<BlobDataConsumer> blobDataConsumer = new Vector<BlobDataConsumer>();
	
	/**
	 * The constructor.
	 * 
	 * @param bfi The blob finder interface.
	 */
	public BlobDataProvider(BlobfinderInterface bfi) {
		this.bfi = bfi;
		new Thread(this).start();
	}
	
	/**
	 * Adds a blob data consumer.  It will be notified each time a new
	 * blob arrives.
	 * 
	 * @param consumer The consumer to add.
	 */
	public void addBlobDataConsumer(BlobDataConsumer consumer) {
		blobDataConsumer.add(consumer);
	}

	/**
	 * Removes a blob data consumer.
	 * 
	 * @param consumer The consumer to remove.
	 */
	public void removeBlobDataConsumer(BlobDataConsumer consumer) {
		blobDataConsumer.remove(consumer);
	}
	
	/**
	 * Sits in a loop receiving blob data and distributing it to all listeners.
	 */
	public void run() {
		while(!done) {
			
			// If new blob data is available, get and distribute it.
			if (bfi.isDataReady()) {
				PlayerBlobfinderData blobData = bfi.getData();
				//Logger.log("Got blob data!  Distributing it to all consumers.");
				Enumeration<BlobDataConsumer> e = blobDataConsumer.elements();
				while (e.hasMoreElements()) {
					e.nextElement().newBlobData(blobData);
				}
			}
			
			synchronized(this) {
				try {
					wait(CYCLE_PERIOD_MS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
