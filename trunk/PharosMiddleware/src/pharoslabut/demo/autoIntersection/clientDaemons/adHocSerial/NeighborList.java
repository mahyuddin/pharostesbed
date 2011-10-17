package pharoslabut.demo.autoIntersection.clientDaemons.adHocSerial;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import pharoslabut.RobotIPAssignments;
import pharoslabut.exceptions.PharosException;
import pharoslabut.logger.Logger;

/**
 * Records the state of the neighboring vehicles in an ad hoc autonomous intersection.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.demo.autoIntersection.clientDaemons.adHocSerial.AdHocSerialClientDaemon
 */
public class NeighborList {

	/**
	 * The ID of the local node.
	 */
	protected int myID;
	
	/**
	 * The internal data structure used to store the neighbor list.
	 */
	protected HashMap<InetAddress, NeighborState> list = new HashMap<InetAddress, NeighborState>();
	
	/**
	 * The constructor.
	 */
	public NeighborList() {
		try {
			myID = RobotIPAssignments.getID();
		} catch (PharosException e) {
			Logger.logErr("Failed to get my own ID, " + e.toString());
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Searches the neighbor list and determines whether it is safe for the
	 * local robot to cross the intersection.
	 * 
	 * @return true if it's safe to cross the intersection.
	 */
	public SafeState isSafeToCross() {
		
		Logger.log("Determining whether it's safe to cross the intersection.");
		Iterator<InetAddress> itr = list.keySet().iterator();
		while (itr.hasNext()) {
			InetAddress addr = itr.next();
			NeighborState nbrState = list.get(addr);
			if (nbrState.getStatus() == VehicleStatus.CROSSING) {
				Logger.log("Not safe to cross!  Neighbor " + addr + " is crossing.");
				return new SafeState(false);
			}
			else if (nbrState.getStatus() == VehicleStatus.REQUESTING) {
				int nbrID = addr.getAddress()[3]; 
				if (nbrID > myID) {
					Logger.logErr("Not safe to cross!  Neighbor " + addr + " is requesting and has a greater ID (" + nbrID + " vs. " + myID + ")");
					return new SafeState(false);
				}
			}
		}
		
		Logger.log("Safe to cross!");
		return new SafeState(true);
	}
	
	/**
	 * Removes expired neighbors from this list.
	 * 
	 * @param maxAge The maximum age of the neighbor in the list.
	 */
	public void flushOldEntries(long maxAge) {
		
		Logger.log("Flushing old entries, max age=" + maxAge);
		Iterator<InetAddress> itr = list.keySet().iterator();
		while (itr.hasNext()) {
			InetAddress addr = itr.next();
			NeighborState nbrState = list.get(addr);
			if (nbrState.getAge() > maxAge) {
				Logger.log("Removing neighbor " + addr + ", age=" + nbrState.getAge() + ", max age=" + maxAge);
				list.remove(addr);
			}
		}
		Logger.log("Done flushing old entries.");
	}
	
	/**
	 * Updates this neighbor list based on a beacon.
	 * 
	 * @param beacon The beacon.
	 */
	public void update(AdHocSerialBeacon beacon) {
		InetAddress vehicleAddress = beacon.getAddress();
		NeighborState neighborState;
		
		if (vehicleAddress.getAddress()[3] == myID) {
			Logger.log("Ignoring my own beacon.");
			return;
		}
		
		if (list.containsKey(vehicleAddress))
			neighborState = list.get(vehicleAddress);
		else {
			neighborState = new NeighborState(vehicleAddress, beacon.getStatus());
			
			Logger.log("Adding neighbor " + vehicleAddress + " to neighbor list.");
			list.put(vehicleAddress, neighborState);
		}
		
		Logger.log("Updating neighbor " + vehicleAddress + " to have status " + beacon.getStatus());
		neighborState.setStatus(beacon.getStatus());
	}
	
	/**
	 * @return A string representation of this class.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(getClass().getName() + "\n");
		Set<InetAddress> keys = list.keySet();
		Iterator<InetAddress> itr = keys.iterator();
		while(itr.hasNext()) {
			InetAddress addr = itr.next();
			sb.append("\t" + addr + "\t" + list.get(addr));
			if (itr.hasNext())
				sb.append("\n");
		}
		return sb.toString();
	}
}
