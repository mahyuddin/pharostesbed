package pharoslabut.demo.autoIntersection.clientDaemons.V2VReservation;

import java.net.InetAddress;

/**
 * This is the beacon used in the V2V-Reservation autonomous intersection policy.
 * It includes an estimated time to traverse the intersection.
 * 
 * @author Chien-Liang Fok
 */
public class V2VReservationBeacon extends pharoslabut.demo.autoIntersection.clientDaemons.V2VParallel.V2VParallelBeacon {

	private static final long serialVersionUID = 4097462079501429669L;
	
	/**
	 * The estimated time the sending vehicle will enter the intersection. 
	 */
	long entryTime = -1;
	
	/**
	 * The time in milliseconds the transmitter will take to cross the intersection.
	 */
	long timeToCross;
	
	/**
	 * The constructor.
	 * 
	 * @param address The address of this host.
	 * @param port The single-cast port number being used.
	 * @param entryPointID The entry point.
	 * @param exitPointID The exit point.
	 * @param timeToCross The time in milliseconds the transmitter will take to cross the intersection.
	 */
	public V2VReservationBeacon(InetAddress address, int port,
			String entryPointID, String exitPointID, long timeToCross) 
	{
		super(address, port, entryPointID, exitPointID);
		this.timeToCross = timeToCross;
	}
	
	public long getTimeToCross() {
		return timeToCross;
	}
	
	/**
	 * Sets the estimated time the sending vehicle will exit the intersection. 
	 * @param entryTime The estimated time the sending vehicle will exit the intersection. 
	 */
	public void setEntryTime(long entryTime) {
		this.entryTime = entryTime;
	}
	
	/**
	 * 
	 * @return The amount of time in milliseconds it will take the sending vehicle
	 * to cross the intersection. 
	 */
	public long getEntryTime() {
		return entryTime;
	}
	
	/**
	 * @return A string representation of this class.
	 */
	public String toString() {
		return super.toString() + ", entryTime = " + entryTime + ", timeToCross = " + timeToCross;
	}

}
