package pharoslabut.demo.autoIntersection.clientDaemons.V2VParallel;

import java.net.InetAddress;
import java.util.Iterator;

import pharoslabut.demo.autoIntersection.clientDaemons.V2VSerial.V2VSerialBeacon;
import pharoslabut.demo.autoIntersection.clientDaemons.V2VSerial.SafeState;
import pharoslabut.demo.autoIntersection.clientDaemons.V2VSerial.VehicleStatus;
import pharoslabut.demo.autoIntersection.intersectionSpecs.IntersectionSpecs;
import pharoslabut.demo.autoIntersection.intersectionSpecs.TwoLaneFourWayIntersectionSpecs;
import pharoslabut.logger.Logger;

/**
 * Records the state of the neighboring vehicles in a V2V-Parallel intersection.
 * It's isSafeToCross method allows multiple vehicles to simultaneously cross the intersection.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.demo.autoIntersection.clientDaemons.V2VParallel.V2VParallelClientDaemon
 */
public class ParallelNeighborList 
	extends
	pharoslabut.demo.autoIntersection.clientDaemons.V2VSerial.NeighborList
{	
	/**
	 * The entry point.
	 */
	protected String myEntryID;
	
	/**
	 * The exit point.
	 */
	protected String myExitID;
	
	// TODO: Make this a tunable parameter.
	protected IntersectionSpecs intersectionSpecs = TwoLaneFourWayIntersectionSpecs.getSpecs();
	
	/**
	 * The constructor.
	 * 
	 * @param myEntryID The entry point.
	 * @param myExitID The exit point.
	 */
	public ParallelNeighborList(String myEntryID, String myExitID) {
		super();
		this.myEntryID = myEntryID;
		this.myExitID = myExitID;
	}
	
	
	/**
	 * Searches the neighbor list and determines whether it is safe for the
	 * local robot to cross the intersection.
	 * 
	 * @return true if it's safe to cross the intersection.
	 */
	@Override
	public SafeState isSafeToCross() {
		Logger.log("Checking to see if it's OK to cross in a serial manner.");
		if (super.isSafeToCross().isSafe()) {
			Logger.log("Safe to cross sequentially.");
			return new SafeState(true);
		} else {
			Logger.log("Determining whether it's safe to cross in parallel with another robot.");
			
			int numCrossing = 0;
			
			Iterator<InetAddress> itr = list.keySet().iterator();
			while (itr.hasNext()) {
				InetAddress addr = itr.next();
				ParallelNeighborState nbrState = (ParallelNeighborState)list.get(addr);
				Logger.log("Checking neighbor " + nbrState);
				
				if (nbrState.getStatus() == VehicleStatus.CROSSING) {
					
					numCrossing++;
					
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
						return new SafeState(false);
					} else {
						sb.append("\n\tWill NOT intersect!  May still be safe to cross.");
						Logger.log(sb.toString());
					}
				}
			}
			
			if (numCrossing > 0) {
				Logger.log("There are neighbors crossing and I do not conflict. Thus, concluding it's immediately safe to cross.");
				return new SafeState(true, System.currentTimeMillis());
			} else {
				Logger.log("No neighbors crossing, resorting to conclusion of serial ad hoc manager.");
				return new SafeState(false);
			}
		}
	}
	
	/**
	 * Updates this neighbor list based on a beacon.
	 * 
	 * @param beacon The beacon.
	 */
	@Override
	public void update(V2VSerialBeacon beacon) {
		
		if (!(beacon instanceof V2VParallelBeacon)) {
			Logger.log("Received unexpected beacon of wrong type " + beacon);
			return;
		}
		
		V2VParallelBeacon actualBeacon = (V2VParallelBeacon)beacon;
		
		InetAddress vehicleAddress = beacon.getAddress();
		ParallelNeighborState neighborState;
		
		if (vehicleAddress.getAddress()[3] == myID) {
			Logger.log("Ignoring my own beacon.");
			return;
		}
		
		if (list.containsKey(vehicleAddress))
			neighborState = (ParallelNeighborState)list.get(vehicleAddress);
		else {
			neighborState = new ParallelNeighborState(vehicleAddress, actualBeacon.getStatus(), 
					actualBeacon.getEntryPointID(), actualBeacon.getExitPointID());
			
			Logger.log("Adding neighbor " + vehicleAddress + " to neighbor list.");
			list.put(vehicleAddress, neighborState);
		}
		
		Logger.log("Updating neighbor " + vehicleAddress + " to have status " + actualBeacon.getStatus());
		neighborState.setStatus(actualBeacon.getStatus());
	}
	
}
