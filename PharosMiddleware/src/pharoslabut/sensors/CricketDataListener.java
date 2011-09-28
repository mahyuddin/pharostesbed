package pharoslabut.sensors;

/**
 * Defines the interface that all CricketDataListeners must implement.
 * 
 * @author Kevin Boos
 */
public interface CricketDataListener {
	
	/**
	 * This is called by CricketDataInterface whenever new CricketData is received.
	 * 
	 * @param data The new data received.
	 */
	public void newCricketData(CricketData cd);
}
