package pharoslabut.demo.autoIntersection.server;

/**
 * Stores the state of a vehicle approaching or entering the intersection.
 * 
 * @author Chien-Liang Fok
 *
 */
public class VehicleState {

	private Vehicle vehicle;
	
	private String entranceID, exitID;
	
	private long timeOfEntry;
	
	/**
	 * The constructor.
	 * 
	 * @param vehicle The vehicle.
	 * @param entranceID The entrance.
	 * @param exitID The exit.
	 * @param timeOfEntry The time of entry.
	 */
	public VehicleState(Vehicle vehicle, String entranceID, String exitID, long timeOfEntry) {
		this.vehicle = vehicle;
		this.entranceID = entranceID;
		this.exitID = exitID;
		this.timeOfEntry = timeOfEntry;
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
	
	public long getTimeOfEntry() {
		return timeOfEntry;
	}
	
	public String toString() {
		return vehicle + ", " + entranceID + ", " + exitID + ", " + timeOfEntry;
	}
}
