package pharoslabut.demo.autoIntersection.clientDaemons.V2VReservation;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.Vector;

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
		
		// If this vehicle has the highest priority, simply return a positive safe state with a minimum safety duration.
		if (hasHighestPriority()) {
			Logger.log("This vehicle has highest priority.  Thus we think it is safe to cross (still need to wait a min safety duration).");
			return new SafeState(true);
		} 
		
		/*
		 * By now, we know there is either another vehicle crossing,
		 * or there is another vehicle with higher priority that is requesting.
		 *
		 * First check if this vehicle can cross in parallel with a vehicle that is crossing.
		 * If it can, cross with that vehicle.  Regardless, maintain a list of conflicting vehicles 
		 * that are currently crossing the intersection.
		 */
		Vector<ReservationNeighborState> crossingConflicts = new Vector<ReservationNeighborState>();
		
		Iterator<InetAddress> itr = list.keySet().iterator();
		while (itr.hasNext()) {
			InetAddress addr = itr.next();
			
			ReservationNeighborState nbrState = (ReservationNeighborState)list.get(addr);
			Logger.log("Checking neighbor " + nbrState);
			
			long nbrEntryTime = nbrState.getEntryTime();
			
			// If a vehicle is crossing and it does not intersect with this vehicle, conclude it is OK to immediately cross.
			if (nbrState.getStatus() == VehicleStatus.CROSSING) {
					
				StringBuffer sb = new StringBuffer("Vehicle " + nbrState.getAddress() + " is crossing at time " + nbrEntryTime);
				sb.append("\n\tChecking if it's OK to cross in parallel with this vehicle.");
				sb.append("\n\tMy entrance: " + myEntryID + ", My exit: " + myExitID);
				sb.append("\n\tNbr entrance: " + nbrState.getEntryPointID() 
						+ ", Nbr exit: " + nbrState.getExitPointID());
					
				if (intersectionSpecs.willIntersect(myEntryID, myExitID, 
						nbrState.getEntryPointID(), nbrState.getExitPointID()))
				{
					sb.append("\n\tWill intersect!  Not safe to cross with this vehicle!");
					Logger.log(sb.toString());
					crossingConflicts.add(nbrState); // save the conflict
				} else {
					sb.append("\n\tWill NOT intersect!  Let's cross at the same time as the crossing neighbor!");
					Logger.log(sb.toString());
					return new SafeState(true, nbrEntryTime);
				}
			}
		}
		
		
		/*
		 * This vehicle cannot cross in parallel with a vehicle that is already crossing.
		 * See if this vehicle has the highest priority among all of the requesting vehicles.
		 * 
		 * If it does have highest priority among the requesting vehicles, set the time to enter
		 * to be immediately after the crossing vehicles exit (or the current time if there are no
		 * crossing vehicles).
		 */
		// Get a list of all requesting vehicles.
		Vector<ReservationNeighborState> requestingVehicles = new Vector<ReservationNeighborState>();
		itr = list.keySet().iterator();
		while (itr.hasNext()) {
			InetAddress addr = itr.next();
			
			ReservationNeighborState nbrState = (ReservationNeighborState)list.get(addr);
			if (nbrState.getStatus() == VehicleStatus.REQUESTING) {	
				requestingVehicles.add(nbrState);
			}
		}
		
		// Next, see if this vehicle has the highest priority among the requesting vehicles.
		boolean hasHighestPriorityAmongRequesting = true;
		for (int i=0; i < requestingVehicles.size(); i++) {
			ReservationNeighborState nbrState = requestingVehicles.get(i);
			InetAddress addr = nbrState.getAddress();
			int nbrID = addr.getAddress()[3]; 
			if (nbrID > myID) {
				Logger.log("I do not have highest priority: Neighbor " + addr + " is requesting and has a greater ID (" + nbrID + " vs. " + myID + ")");
				hasHighestPriorityAmongRequesting = false;
			}
		}
		
		if (hasHighestPriorityAmongRequesting) {
			Logger.log("I have highest priority among requesting vehicles.  Setting my entry time to be after the last conflicting crossing vehicle.");
			long earliestSafeTime = Long.MAX_VALUE;
			for (int i=0; i < crossingConflicts.size(); i++) {
				ReservationNeighborState currNbr = crossingConflicts.get(i);
				long currExitTime = currNbr.getEntryTime() + currNbr.getTimeToCross();
				if (currExitTime < earliestSafeTime)
					earliestSafeTime = currExitTime;
			}
			
			if (earliestSafeTime == Long.MAX_VALUE) {
				Logger.log("No conflicting crossing vehicles and I have highest priority among requestors.  Thus, setting earliest safe time to be now!");
				earliestSafeTime = System.currentTimeMillis();
			} 
			
			Logger.log("Earliest safe crossing time is " + earliestSafeTime);
			return new SafeState(true, earliestSafeTime);
		}
		
		/*
		 * The local vehicles does not have the highest priority among the requesting vehicles.
		 * See if it can cross in parallel with any of them who have set their entry times.
		 * 
		 * First get all of the requesting vehicles with entry times set.
		 */
		Vector<ReservationNeighborState> requestingVehiclesWithEntryTime = new Vector<ReservationNeighborState>();
		for (int i=0; i < requestingVehicles.size(); i++) {
			ReservationNeighborState nbrState = requestingVehicles.get(i);
			if (nbrState.getEntryTime() != -1)
				requestingVehiclesWithEntryTime.add(nbrState);
		}
		
		if (requestingVehiclesWithEntryTime.size() > 0) {
			// There are requesting neighbors with higher priority who have their entry times set.
			
			// TODO For now, I select a random higher priority requesting neighbor with their entry time set who can travel in parallel with this vehicle.  In the future, select the vehicle that has the soonest entry time.  
			for (int i=0; i < requestingVehiclesWithEntryTime.size(); i++) {
				ReservationNeighborState nbrState = requestingVehiclesWithEntryTime.get(i);
				if (!intersectionSpecs.willIntersect(myEntryID, myExitID, 
						nbrState.getEntryPointID(), nbrState.getExitPointID()))
				{
					Logger.log("Can cross in parallel with requesting vehicle " + nbrState.getAddress());
					return new SafeState(true, nbrState.getEntryTime());
				}
			}
			
			// At this point, I cannot cross in parallel with any of the requesting neighbors.
			// Schedule myself to be after them.
			long latestExitTime = 0;
			for (int i=0; i < requestingVehiclesWithEntryTime.size(); i++) {
				ReservationNeighborState nbrState = requestingVehiclesWithEntryTime.get(i);
				long exitTime = nbrState.getEntryTime() + nbrState.getTimeToCross();
				if (exitTime > latestExitTime)
					latestExitTime = exitTime;
			}
			Logger.log("Cannot cross in parallel with any of the higher-priority requesting vehicles.  "
					+ "Thus, scheduling myself after them at time " 
					+ latestExitTime + ", which is " + (latestExitTime - System.currentTimeMillis()) + "ms from now");
			return new SafeState(true, latestExitTime);
			
		} else {
			Logger.log("There are requesting vehicles with higher priority but none have entry times. "
					+ "Thus waiting for them to set their entry times before deciding it's safe to cross.");
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
