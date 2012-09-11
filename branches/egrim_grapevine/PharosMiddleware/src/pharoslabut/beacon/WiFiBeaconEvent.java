package pharoslabut.beacon;

/**
  * This class is used to propagate a BeaconEvent.
  *
  * @author Chien-Liang Fok
  * @version 7/5/2002
  */
public class WiFiBeaconEvent {
	private WiFiBeacon beacon;

	public WiFiBeaconEvent(WiFiBeacon beacon) {   
		this.beacon = beacon;
	}

	public WiFiBeacon getBeacon() {
		return beacon;
	}

	public String toString() {
		return beacon.toString();
	}
}
