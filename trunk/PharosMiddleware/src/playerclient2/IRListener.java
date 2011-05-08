package playerclient2;

import playerclient2.structures.ir.*;

/**
 * Defines the interface that all IR Listeners must implement.
 * 
 * @author Xiaomeng Wu
 */
public interface IRListener {

	/**
	 * This is called by IRInterface whenever a new IRData is
	 * received.
	 * 
	 * @param data The new data received.
	 */
	public void newPlayerIRData(PlayerIrData data);
}
