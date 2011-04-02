package playerclient;

import playerclient.structures.blobfinder.*;

/**
 * The interface that all blobfinder listeners must implement.
 * 
 * @author Chien-Liang Fok
 */
public interface BlobfinderListener {

	/**
	 * This is called whenever new blob data is available.
	 * 
	 * @param blobData The new blob data available.
	 * @param timestamp The timestamp of the data.  This can be compared to System.currentTimeMillis() to determine the age of the blob data.
	 */
	public void newPlayerBlobfinderData(PlayerBlobfinderData blobData, long timestamp);
}
