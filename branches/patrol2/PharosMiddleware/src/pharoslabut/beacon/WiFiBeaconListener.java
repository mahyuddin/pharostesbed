package pharoslabut.beacon;

import java.util.*;

/**
  * Defines the interface that all BeaconListeners must
  * implement.
  *
  * @author Chien-Liang Fok
  * @version 7/5/2002
  */
public interface WiFiBeaconListener extends EventListener {

	/**
	  * Called whenever a beacon is received.
	  *
	  * @param be the BeaconEvent containing the beacon received.
	  */
	public void beaconReceived(WiFiBeaconEvent be);
}
