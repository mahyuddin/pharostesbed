package playerclient;

import playerclient.structures.opaque.*;

/**
 * Defines the interface that all Opaque Listeners must implement.
 * 
 * @author Chien-Liang Fok
 */
public interface OpaqueListener {
	/**
	 * This is called by OpaqueInterface whenever a new opaque data arrives.
	 * 
	 * @param opaqueData The opaque data that was received.
	 */
	public void newOpaqueData(PlayerOpaqueData opaqueData);
}
