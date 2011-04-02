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
	 */
	public void newPlayerBlobfinderData(PlayerBlobfinderData blobData);
}
