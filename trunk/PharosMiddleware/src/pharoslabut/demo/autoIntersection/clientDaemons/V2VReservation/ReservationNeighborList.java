package pharoslabut.demo.autoIntersection.clientDaemons.V2VReservation;

import java.net.InetAddress;
import java.util.Iterator;

import pharoslabut.demo.autoIntersection.clientDaemons.V2VSerial.SafeState;
import pharoslabut.demo.autoIntersection.clientDaemons.V2VSerial.V2VSerialBeacon;
import pharoslabut.demo.autoIntersection.clientDaemons.V2VSerial.VehicleStatus;
import pharoslabut.logger.Logger;

/**
 * Records the state of the neighboring vehicles in a V2V-Reservation intersection.
 * Its isSafeToCross method allows multiple vehicles to simultaneously cross the intersection.
 * It uses the time each vehicle will take in the intersection to determine when it's safe to cross.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.demo.autoIntersection.clientDaemons.V2VParallel.V2VParallelClientDaemon
 */
public class ReservationNeighborList extends 
	pharoslabut.demo.autoIntersection.clientDaemons.V2VParallel.ParallelNeighborList 
{

	public ReservationNeighborList(String myEntryID, String myExitID) {
		super(myEntryID, myExitID);
	}
	
	/**
	 * Searches the neighbor list and determines whether it is safe for the
	 * local robot to cross the intersection.
	 * 
	 * @return true if it's safe to cross the intersection.
	 */
	@Override
	public SafeState isSafeToCross() {
		
		if (hasHighestPriority()) {
			Logger.log("This vehicle has highest priority.  This we think it is safe to cross (still need to wait a min safety duration).");
			return new SafeState(true);
		} 
		
		// The local vehicle does not have the highest priority.
		// Go through the neighbor list and find the soonest time that's safe to cross.
		long earliestSafeTime = Long.MAX_VALUE;
		
		Iterator<InetAddress> itr = list.keySet().iterator();
		while (itr.hasNext()) {
			InetAddress addr = itr.next();
			
			ReservationNeighborState nbrState = (ReservationNeighborState)list.get(addr);
			Logger.log("Checking neighbor " + nbrState);
			
			// If a vehicle is crossing and it does not intersect with this vehicle, conclude it is OK to immediately cross.
			if (nbrState.getStatus() == VehicleStatus.CROSSING) {
				
					
				StringBuffer sb = new StringBuffer("Vehicle " + nbrState.getAddress() + " is crossing.");
				sb.append("\n\tChecking if it's OK to cross in parallel with this vehicle.");
				sb.append("\n\tMy entrance: " + myEntryID + ", My exit: " + myExitID);
				sb.append("\n\tNbr entrance: " + nbrState.getEntryPointID() 
						+ ", Nbr exit: " + nbrState.getExitPointID());
					
				if (intersectionSpecs.willIntersect(myEntryID, myExitID, 
						nbrState.getEntryPointID(), nbrState.getExitPointID()))
				{
					sb.append("\n\tWill intersect!  Not safe to cross with this vehicle!");
					Logger.log(sb.toString());
				} else {
					sb.append("\n\tWill NOT intersect!  Let's cross immediately!");
					Logger.log(sb.toString());
					return new SafeState(true, System.currentTimeMillis());
				}
			} else if (nbrState.getStatus() == VehicleStatus.REQUESTING) {
				long nbrEntryTime = nbrState.getEntryTime();
				long nbrTimeToCross = nbrState.getTimeToCross();
				long nbrExitTime = nbrEntryTime + nbrTimeToCross;
				
				if (nbrEntryTime != -1 && nbrEntryTime < earliestSafeTime) {
					StringBuffer sb = new StringBuffer("Vehicle " + nbrState.getAddress() + " is requesting and"
							+ " has an entry time of " + nbrEntryTime 
							+ ", which is less than the earliest known safest time.");
					sb.append("\n\tChecking if it's OK to cross in parallel with this vehicle.");
					sb.append("\n\tMy entrance: " + myEntryID + ", My exit: " + myExitID);
					sb.append("\n\tNbr entrance: " + nbrState.getEntryPointID() 
						+ ", Nbr exit: " + nbrState.getExitPointID());
					
					if (intersectionSpecs.willIntersect(myEntryID, myExitID, 
						nbrState.getEntryPointID(), nbrState.getExitPointID()))
					{
						sb.append("\n\tWill intersect!  Setting exit time of " + nbrExitTime + " to be the earliest safe time!");
						earliestSafeTime = nbrExitTime;
					} else {
						sb.append("\n\tWill NOT intersect, setting entry time of " + nbrEntryTime + " to be the earliest safe time!");
						earliestSafeTime = nbrEntryTime;
					}
					Logger.log(sb.toString());
				} else {
					Logger.log("Vehicle " + nbrState.getAddress() 
							+ " either has no entry time or has an exit time that's after the earliest known safe time.");
				}
			}
		}
		
		if (earliestSafeTime != Long.MAX_VALUE) {
			Logger.log("Earliest time to safely cross is " + earliestSafeTime 
					+ ", which is in " + (earliestSafeTime - System.currentTimeMillis()) + "ms");
			return new SafeState(true, earliestSafeTime);
		} else {
			Logger.log("Unable to fine a time to safely cross.");
			return new SafeState(false);
		}
	}
	
	/**
	 * Updates this neighbor list based on a beacon.
	 * 
	 * @param beacon The beacon.
	 */
	@Override
	public void update(V2VSerialBeacon beacon) {
		
		if (!(beacon instanceof V2VReservationBeacon)) {
			Logger.log("Received unexpected beacon of wrong type " + beacon);
			return;
		}
		
		V2VReservationBeacon actualBeacon = (V2VReservationBeacon)beacon;
		
		InetAddress vehicleAddress = beacon.getAddress();
		ReservationNeighborState neighborState;
		
		if (vehicleAddress.getAddress()[3] == myID) {
			Logger.log("Ignoring my own beacon.");
			return;
		}
		
		if (list.containsKey(vehicleAddress))
			neighborState = (ReservationNeighborState)list.get(vehicleAddress);
		else {
			neighborState = new ReservationNeighborState(vehicleAddress, actualBeacon.getStatus(), 
					actualBeacon.getEntryPointID(), actualBeacon.getExitPointID());
			
			Logger.log("Adding neighbor " + vehicleAddress + " to neighbor list.");
			list.put(vehicleAddress, neighborState);
		}
		
		long entryTime = actualBeacon.getEntryTime();
		long timeToCross = actualBeacon.getTimeToCross();
		
		Logger.log("Updating neighbor " + vehicleAddress + " to have status " + actualBeacon.getStatus() 
				+ " and entry time of " + entryTime + ", which is " + (entryTime - System.currentTimeMillis()) 
				+ "ms from now, and time to cross of " + timeToCross);
		
		neighborState.setStatus(actualBeacon.getStatus());
		neighborState.setEntryTime(entryTime);
		neighborState.setTimeToCross(timeToCross);
	}

}
