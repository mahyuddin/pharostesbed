package playerclient;

import playerclient.structures.position2d.*;

/**
 * Defines the interface that all Position2DListeners must implement.
 * 
 * @author Chien-Liang Fok
 */
public interface Position2DListener {

	/**
	 * This is called by Position2DInterface whenever a new PlayerPosition2dData is
	 * received.
	 * 
	 * @param data The new data received.
	 */
	public void newPlayerPosition2dData(PlayerPosition2dData data);
}
