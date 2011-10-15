package pharoslabut.demo.autoIntersection;

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
 * @see pharoslabut.demo.autoIntersection.AdHocClientDaemon
 */
public class NeighborList {

	/**
	 * The ID of the local node.
	 */
	int myID;
	
	/**
	 * The internal data structure used to store the neighbor list.
	 */
	private HashMap<InetAddress, NeighborState> list = new HashMap<InetAddress, NeighborState>();
	
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
	public boolean isSafeToCross() {
		
		Logger.log("Determining whether it's safe to cross the intersection.");
		Iterator<InetAddress> itr = list.keySet().iterator();
		while (itr.hasNext()) {
			InetAddress addr = itr.next();
			NeighborState nbrState = list.get(addr);
			if (nbrState.getStatus() == VehicleStatus.CROSSING) {
				Logger.log("Not safe to cross!  Neighbor " + addr + " is crossing.");
				return false;
			}
			else if (nbrState.getStatus() == VehicleStatus.REQUESTING) {
				int nbrID = addr.getAddress()[3]; 
				if (nbrID > myID) {
					Logger.logErr("Not safe to cross!  Neighbor " + addr + " is requesting and has a greater ID (" + nbrID + " vs. " + myID + ")");
					return false;
				}
			}
		}
		
		Logger.log("Safe to cross!");
		return true;
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
	public void update(AdHocAutoIntersectionBeacon beacon) {
		InetAddress vehicleAddress = beacon.getAddress();
		NeighborState neighborState;
		
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
	
	/**
	 * Contains the state about a specific neighbor in the neighbor list.
	 * 
	 * @author Chien-Liang Fok
	 */
	private class NeighborState {
		
		/**
		 * The IP address of the neighbor.
		 */
		InetAddress address;
		
		/**
		 * The status of the neighbor.
		 */
		VehicleStatus status;
		
		/**
		 * The time since this neighbor was updated.
		 */
		long lastUpdatedTime;
		
		/**
		 * The constructor.
		 * 
		 * @param address The IP address of the neighbor.
		 * @param status The status of the neighbor.
		 */
		public NeighborState(InetAddress address, VehicleStatus status) {
			this.address = address;
			this.status = status;
			lastUpdatedTime = System.currentTimeMillis();
		}
		
//		/**
//		 *
//		 * @return The neighbor's IP address.
//		 */
//		public InetAddress getAddress() {
//			return address;
//		}
		
		/**
		 * Sets the status of this neighbor.
		 * 
		 * @param status The new status of the neighbor.
		 */
		public void setStatus(VehicleStatus status) {
			this.status = status;
			lastUpdatedTime = System.currentTimeMillis();
		}
		
		/**
		 * 
		 * @return The status of the neighbor.
		 */
		public VehicleStatus getStatus() {
			return status;
		}
		
		/**
		 * 
		 * @return The time since this neighbor state was last updated.
		 */
		public long getAge() {
			return System.currentTimeMillis() - lastUpdatedTime;
		}
		
		public String toString() {
			return address + ", status=" + status + ", lastUpdatedTime=" + lastUpdatedTime 
			+ ", age=" + (System.currentTimeMillis() - lastUpdatedTime);
		}
	}
}
