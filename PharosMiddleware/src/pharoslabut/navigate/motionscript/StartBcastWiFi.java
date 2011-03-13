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
	 * @param minPeriod The minimum beacon period in milliseconds.
	 * @param maxPeriod The maximum beacon period in milliseconds.
	 * @param txPowerLevel The transmit power.
	 */
	public StartBcastWiFi(long minPeriod, long maxPeriod, int txPowerLevel) {
		super(minPeriod, maxPeriod, txPowerLevel);
	}
	
	@Override
	public InstructionType getType() {
		return InstructionType.START_BCAST_WIFI;
	}
	
	public String toString() {
		return "START_BCAST_WIFI min period = " + minPeriod + ", max period = " + maxPeriod 
		+ ", tx power = " + txPowerLevel;
	}

}
