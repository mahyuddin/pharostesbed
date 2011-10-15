package pharoslabut.demo.autoIntersection;

import java.net.InetAddress;

import pharoslabut.beacon.WiFiBeacon;
import pharoslabut.logger.Logger;

/**
 * This is the WiFi beacon that is transmitted in the ad hoc
 * autonomous intersection.
 * 
 * @author Chien-Liang Fok
 */
public class AdHocAutoIntersectionBeacon extends WiFiBeacon {

	private static final long serialVersionUID = -6681705407266292187L;

	/**
	 * The status of the vehicle that sent this beacon.
	 */
	private VehicleStatus status = VehicleStatus.IDLE;
	
	/**
	 * The constructor.
	 * 
	 * @param address The address of this host.
	 * @param port The single-cast port number being used.
	 */
	public AdHocAutoIntersectionBeacon(InetAddress address, int port) {
		super(address, port);
	}
	
	/**
	 * Sets the vehicle status.
	 * 
	 * @param status The status of the vehicle that is sending this beacon.
	 */
	public void setVehicleStatus(VehicleStatus status) {
		Logger.log("Changing beacon status to " + status);
		this.status = status;
	}
	
	/**
	 * 
	 * @return The status of the vehicle that sent this beacon.
	 */
	public VehicleStatus getStatus() {
		return status;
	}
	
	/**
	 * @return A string representation of this class.
	 */
	public String toString() {
		return getClass().getName() + ", " + super.toString() + ", status=" + status;
	}
}
