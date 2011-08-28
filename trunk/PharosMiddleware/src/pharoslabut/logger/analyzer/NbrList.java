package pharoslabut.logger.analyzer;

import java.util.Vector;
import pharoslabut.logger.Logger;

/**
 * A neighbor list.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.logger.analyzer.NodeConnectivityStats
 * @see pharoslabut.logger.analyzer.ExpConnectivityStats
 * @see pharoslabut.logger.analyzer.NbrList
 */
public class NbrList {
	
	/**
	 * The internal representation of the neighbor list.
	 */
	private Vector<NbrListElement> list = new Vector<NbrListElement>();
	
	/**
	 * The disconnection interval in milliseconds.
	 */
	private long disconnectionInterval;
	
	/**
	 * Maintain a list of unique neighbors ever added to the neighbor list.  
	 * This is used to calculate the number of unique neighbors ever encountered by this node.
	 */
	private Vector<NbrListElement> uniqueNbrs = new Vector<NbrListElement>();
	
	/**
	 * The constructor.
	 * 
	 * @param disconnectionInterval The expiration interval for removing disconnected neighbors.
	 */
	public NbrList(long disconnectionInterval) {
		this.disconnectionInterval = disconnectionInterval;
	}
	
	/**
	 * 
	 * @param robotData The robot experiment data.
	 * @param timestamp The timestamp at which to obtain the neighbor list.
	 * @param disconnectionInterval The expiration interval for removing disconnected neighbors.
	 * @return The neighbor list of the robot at the specified time.
	 */
	public static NbrList getNbrList(RobotExpData robotData, long timestamp, long disconnectionInterval) {
		Vector<WiFiBeaconRx> rxEvents = robotData.getWiFiBeaconRxs();
		NbrList list = new NbrList(disconnectionInterval);
		
		for (int i=0; i < rxEvents.size(); i++) {
			WiFiBeaconRx currRxEvent = rxEvents.get(i);
			if (currRxEvent.getTimestamp() < timestamp)
				list.update(currRxEvent);
		}
		
		list.clearDisconnected(timestamp);
		
		return list;
	}
	
	/**
	 * 
	 * @return The number of unique neighbors encountered by this robot
	 * during this experiment.
	 */
	public int numUniqueNbrs() {
		return uniqueNbrs.size();
	}
	
	/**
	 * 
	 * @return The number of neighbors in the neighbor list.
	 */
	public int size() {
		return list.size();
	}
	
	/**
	 * Removes all expired elements.
	 * 
	 * @param currTime The current time.
	 * @return A list of durations that removed neighbors resided in the list.
	 */
	public Vector<Long> clearDisconnected(long currTime) {
		Vector<Long> result = new Vector<Long>();
		
		// For each neighbor in the neighbor list.
		for (int i=0; i < list.size(); i++) {
			NbrListElement currNbr = list.get(i);
			
			// If the neighbor is expired, remove it from the list.
			if (currTime - currNbr.timeLastUpdated > disconnectionInterval) {
				long duration = currNbr.timeLastUpdated + disconnectionInterval - currNbr.timeAdded;
				Logger.logDbg("Removing " + currNbr + ", connection duration = " + duration);
				result.add(duration);
				list.remove(i--);
			}
		}
		
		return result;
	}
	
	/**
	 * Updates the last update time of a neighbor, or adds the neighbor to the 
	 * neighbor list if it does not exist.
	 * 
	 * @param beaconEvent The beacon event.
	 * @return true if a new neighbor was added to the list
	 */
	public boolean update(WiFiBeaconRx beaconEvent) {
		boolean found = false;
		
		// For each neighbor in the neighbor list.
		for (int i=0; i < list.size(); i++) {
			NbrListElement currNbr = list.get(i);
			if (currNbr.ownsBeacon(beaconEvent)) {
//				Logger.logDbg("Updating last received time of " + currNbr + " to be " + beaconEvent.getTimestamp());
				currNbr.timeLastUpdated = beaconEvent.getTimestamp();
				found = true; // The neighbor was already in the list and will remain in the list.
			}
		}
		
		// If the neighbor is not in the list, add it!
		if (!found) {
			NbrListElement newNbr = new NbrListElement(beaconEvent);
			Logger.logDbg("Adding " + newNbr);
			list.add(newNbr);
			updateUniqueList(newNbr, beaconEvent);
			return true; // a new neighbor was added to the list
		} else
			return false; // no new neighbor was added to the list
	}
	
	/**
	 * Adds the specified neighbor to the uniqueNbrs vector if it does not
	 * already exist in this vector.
	 * 
	 * @param nbr The neighbor to add.
	 * @param the beacon event that the neighbor sent.
	 */
	private void updateUniqueList(NbrListElement nbr, WiFiBeaconRx beaconEvent) {
		boolean exists = false;
		
		for (int i=0; i < uniqueNbrs.size(); i++) {
			NbrListElement currNbr = uniqueNbrs.get(i);
			if (currNbr.ownsBeacon(beaconEvent))
				exists = true;
		}
		
		if (!exists)
			uniqueNbrs.add(nbr);
	}
}
