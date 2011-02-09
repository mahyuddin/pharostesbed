package pharoslabut.beacon;

/**
  * This class is used to propagate a BeaconEvent.
  *
  * @author Chien-Liang Fok
  * @version 7/5/2002
  */
public class BeaconEvent {
	private Beacon beacon;

	public BeaconEvent(Beacon beacon) {   
		this.beacon = beacon;
	}

	public Beacon getBeacon() {
		return beacon;
	}

	public String toString() {
		return beacon.toString();
	}
}
