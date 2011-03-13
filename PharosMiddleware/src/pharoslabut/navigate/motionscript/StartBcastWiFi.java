package pharoslabut.navigate.motionscript;

/**
 * An instruction to start the periodic broadcasting of WiFi 
 * beacons.
 * 
 * @author Chien-Liang Fok
 */
public class StartBcastWiFi extends StartBcastTelosB {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7440946264712747183L;
	
	/**
	 * The constructor.
	 * 
	 * @param period The beacon period in milliseconds.
	 * @param txPowerLevel The transmit power.
	 */
	public StartBcastWiFi(long period, int txPowerLevel) {
		super(period, txPowerLevel);
	}
	
	@Override
	public InstructionType getType() {
		return InstructionType.START_BCAST_WIFI;
	}
	
	public String toString() {
		return "START_BCAST_WIFI period = " + period + ", tx power = " + txPowerLevel;
	}

}
