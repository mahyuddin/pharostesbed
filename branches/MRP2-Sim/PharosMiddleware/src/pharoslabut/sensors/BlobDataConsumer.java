package pharoslabut.sensors;

import playerclient3.structures.blobfinder.PlayerBlobfinderData;

/**
 * Defines the interface that must be implemented by all consumers of blob data.
 * 
 * @author Chien-Liang Fok
 */
public interface BlobDataConsumer {

	/**
	 * This is called whenever new blob data is available.
	 * 
	 * @param blobData The new blob data.
	 */
	public void newBlobData(PlayerBlobfinderData blobData);
}
