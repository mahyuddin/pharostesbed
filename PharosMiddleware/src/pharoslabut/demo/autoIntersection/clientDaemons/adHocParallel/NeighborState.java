package pharoslabut.demo.autoIntersection.clientDaemons.adHocParallel;

import java.net.InetAddress;

import pharoslabut.demo.autoIntersection.clientDaemons.adHocSerial.VehicleStatus;

/**
 * Contains the state about a specific neighbor in the neighbor list.
 * 
 * @author Chien-Liang Fok
 */
public class NeighborState
		extends
		pharoslabut.demo.autoIntersection.clientDaemons.adHocSerial.NeighborState 
{

	/**
	 * The entry point.
	 */
	private String entryPointID;
	
	/**
	 * The exit point.
	 */
	private String exitPointID;
	
	/**
	 * The constructor.
	 * 
	 * @param address The IP address of the neighbor.
	 * @param status The status of the neighbor.
	 * @param entryPointID The ID of the entry point.
	 * @param exitPointID The ID of the exit point.
	 */
	public NeighborState(InetAddress address, VehicleStatus status, 
			String entryPointID, String exitPointID) 
	{
		super(address, status);
		this.entryPointID = entryPointID;
		this.exitPointID = exitPointID;
	}
	
	/**
	 * 
	 * @return The entry point.
	 */
	public String getEntryPointID() {
		return entryPointID;
	}
	
	/**
	 * 
	 * @return The exit point.
	 */
	public String getExitPointID() {
		return exitPointID;
	}
	
	/**
	 * @return A string representation of this class.
	 */
	public String toString() {
		return super.toString() + ", entryPointID=" + entryPointID + ", exitPointID=" + exitPointID;
	}
}
