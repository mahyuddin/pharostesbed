package pharoslabut.sensors;


/**
 * Defines the interface that all Opaque Listeners must implement.
 * 
 * @author Chien-Liang Fok
 */
public interface ProteusOpaqueListener {
	/**
	 * This is called by OpaqueInterface whenever a new opaque data arrives.
	 * 
	 * @param opaqueData The opaque data that was received.
	 */
	public void newOpaqueData(ProteusOpaqueData opaqueData);
}

