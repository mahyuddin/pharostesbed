package pharoslabut.demo.autoIntersection.server;

/**
 * Stores the state of a vehicle approaching or entering the intersection.
 * 
 * @author Chien-Liang Fok
 *
 */
public class VehicleState {

	/**
	 * The vehicle whose state is being stored.
	 */
	private Vehicle vehicle;
	
	/**
	 * The ID of the entrance through which the vehicle will travel.
	 */
	private String entranceID;
	
	/**
	 * The ID of the exit through which the vehicle will travel.
	 */
	private String exitID;
	
	/**
	 * The time in which the vehicle is authorized to enter the intersection.
	 */
	private long grantTime;
	
	/**
	 * The constructor.
	 * 
	 * @param vehicle The vehicle whose state is being stored.
	 * @param entranceID The ID of the entrance through which the vehicle will travel.
	 * @param exitID The ID of the exit through which the vehicle will travel.
	 * @param grantTime The time in which the vehicle is authorized to enter the intersection.
	 */
	public VehicleState(Vehicle vehicle, String entranceID, String exitID, long grantTime) {
		this.vehicle = vehicle;
		this.entranceID = entranceID;
		this.exitID = exitID;
		this.grantTime = grantTime;
	}
	
	public Vehicle getVehicle() {
		return vehicle;
	}
	
	public String getEntranceID() {
		return entranceID;
	}
	
	public String getExitID() {
		return exitID;
	}
	
	public long getGrantTime() {
		return grantTime;
	}
	
	public String toString() {
		return vehicle + ", " + entranceID + ", " + exitID + ", " + grantTime;
	}
}
