package pharoslabut.demo.autoIntersection.clientDaemons.V2VReservation;

import java.net.InetAddress;

import pharoslabut.demo.autoIntersection.clientDaemons.V2VSerial.VehicleStatus;

/**
 * Contains the state about a specific neighbor in the neighbor list.
 * 
 * @author Chien-Liang Fok
 */
public class ReservationNeighborState extends pharoslabut.demo.autoIntersection.clientDaemons.V2VParallel.ParallelNeighborState {

	/**
	 * The time when the vehicle thinks it will exit the intersection.
	 */
	private long entryTime = -1;
	
	/**
	 * The time it'll take the vehicle to cross the intersection.
	 */
	private long timeToCross;
	
	/**
	 * The constructor.
	 * 
	 * @param address The IP address of the neighbor.
	 * @param status The status of the neighbor.
	 * @param entryPointID The ID of the entry point.
	 * @param exitPointID The ID of the exit point.
	 */
	public ReservationNeighborState(InetAddress address, VehicleStatus status,
			String entryPointID, String exitPointID) 
	{
		super(address, status, entryPointID, exitPointID);
	}
	
	/**
	 * Sets the time in milliseconds the transmitter will take to cross the intersection.
	 * @param timeToCross the time in milliseconds the transmitter will take to cross the intersection.
	 */
	public void setTimeToCross(long timeToCross) {
		this.timeToCross = timeToCross;
	}
	
	/**
	 * 
	 * @return The time in milliseconds the transmitter will take to cross the intersection.
	 */
	public long getTimeToCross() {
		return timeToCross;
	}
	
	/**
	 * Sets the time when the vehicle thinks it will exit the intersection.
	 * @param exitTime the time when the vehicle thinks it will exit the intersection.
	 */
	public void setEntryTime(long entryTime) {
		this.entryTime = entryTime;
	}
	
	/**
	 * 
	 * @return The time the vehicle needs to cross the intersection in milliseconds.
	 */
	public long getEntryTime() {
		return entryTime;
	}
	
	public String toString() {
		return super.toString() + ", entryTime = " + entryTime;
	}

}
