package pharoslabut.demo.autoIntersection.clientDaemons.V2VSerial;

import java.net.InetAddress;

/**
 * Contains the state about a specific neighbor in the neighbor list.
 * 
 * @author Chien-Liang Fok
 */
public class NeighborState {
	
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
	
	/**
	 *
	 * @return The neighbor's IP address.
	 */
	public InetAddress getAddress() {
		return address;
	}
	
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
	
	/**
	 * @return A string representation of this class.
	 */
	public String toString() {
		return address + ", status=" + status + ", lastUpdatedTime=" + lastUpdatedTime 
		+ ", age=" + (System.currentTimeMillis() - lastUpdatedTime);
	}
}