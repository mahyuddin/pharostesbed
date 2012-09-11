package pharoslabut.sensors;

import playerclient3.structures.ranger.PlayerRangerData;;

/**
 * Defines the interface that all RangerListeners must implement.
 * 
 * @author Chien-Liang Fok
 */
public interface RangerListener {
	/**
	 * This is called by IRInterface whenever a new IRData is
	 * received.
	 * 
	 * @param data The new data received.
	 */
	public void newRangerData(PlayerRangerData data);
}
