package pharoslabut.demo.autoIntersection.clientDaemons.adHocParallel;

import java.net.InetAddress;
import java.util.Iterator;

import pharoslabut.demo.autoIntersection.clientDaemons.adHocSerial.AdHocSerialBeacon;
import pharoslabut.demo.autoIntersection.clientDaemons.adHocSerial.VehicleStatus;
import pharoslabut.demo.autoIntersection.intersectionSpecs.IntersectionSpecs;
import pharoslabut.demo.autoIntersection.intersectionSpecs.TwoLaneFourWayIntersectionSpecs;
import pharoslabut.logger.Logger;

/**
 * Records the state of the neighboring vehicles in an ad hoc autonomous intersection.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.demo.autoIntersection.clientDaemons.adHocParallel.AdHocParallelClientDaemon
 */
public class NeighborList 
	extends
	pharoslabut.demo.autoIntersection.clientDaemons.adHocSerial.NeighborList
{	
	
	// TODO: Make this a tunable parameter.
	private IntersectionSpecs intersectionSpecs = TwoLaneFourWayIntersectionSpecs.getSpecs();
	
	/**
	 * The constructor.
	 */
	public NeighborList() {
		super();
	}
	
	
	/**
	 * Searches the neighbor list and determines whether it is safe for the
	 * local robot to cross the intersection.
	 * 
	 * @param myEntryID The local vehicle's entry ID into the intersection.
	 * @param myExitID The local vehicle's exit ID from the intersection.
	 * @return true if it's safe to cross the intersection.
	 */
	public boolean isSafeToCross(String myEntryID, String myExitID) {
		if (super.isSafeToCross()) {
			Logger.log("Safe to cross sequentially.");
			return true;
		} else {
			Logger.log("Determining whether it's safe to cross in parallel with another robot.");
			Iterator<InetAddress> itr = list.keySet().iterator();
			while (itr.hasNext()) {
				InetAddress addr = itr.next();
				NeighborState nbrState = (NeighborState)list.get(addr);
				if (nbrState.getStatus() == VehicleStatus.CROSSING) {
					StringBuffer sb = new StringBuffer("Vehicle " + nbrState.getAddress() + " is crossing.");
					sb.append("\n\tChecking if it's OK to cross in parallel with this vehicle.");
					sb.append("\n\tMy entrance: " + myEntryID + ", My exit: " + myExitID);
					sb.append("\n\tNbr entrance: " + nbrState.getEntryPointID() 
							+ ", Nbr exit: " + nbrState.getExitPointID());
					
					if (intersectionSpecs.willIntersect(myEntryID, myExitID, 
							nbrState.getEntryPointID(), nbrState.getExitPointID()))
					{
						sb.append("\n\tWill intersect!  Not safe to cross!");
						Logger.log(sb.toString());
						return false;
					} else {
						sb.append("\n\tWill NOT intersect!  May still be safe to cross.");
						Logger.log(sb.toString());
					}
				}
			}
			
			Logger.log("Checked all neighbors and will not conflict with any of them."
					+ "  Thus, concluding it's safe to cross.");
			return true;
		}
	}
	
	/**
	 * Updates this neighbor list based on a beacon.
	 * 
	 * @param beacon The beacon.
	 */
	public void update(AdHocSerialBeacon beacon) {
		
		if (!(beacon instanceof AdHocParallelBeacon)) {
			Logger.log("Received unexpected beacon " + beacon);
		}
		
		AdHocParallelBeacon actualBeacon = (AdHocParallelBeacon)beacon;
		
		InetAddress vehicleAddress = beacon.getAddress();
		NeighborState neighborState;
		
		if (vehicleAddress.getAddress()[3] == myID) {
			Logger.log("Ignoring my own beacon.");
			return;
		}
		
		if (list.containsKey(vehicleAddress))
			neighborState = (NeighborState)list.get(vehicleAddress);
		else {
			neighborState = new NeighborState(vehicleAddress, actualBeacon.getStatus(), 
					actualBeacon.getEntryPointID(), actualBeacon.getExitPointID());
			
			Logger.log("Adding neighbor " + vehicleAddress + " to neighbor list.");
			list.put(vehicleAddress, neighborState);
		}
		
		Logger.log("Updating neighbor " + vehicleAddress + " to have status " + actualBeacon.getStatus());
		neighborState.setStatus(actualBeacon.getStatus());
	}
	
}
