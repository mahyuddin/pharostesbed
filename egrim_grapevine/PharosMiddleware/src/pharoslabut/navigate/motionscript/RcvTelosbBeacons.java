package pharoslabut.navigate.motionscript;

/**
 * Receives a certain number of beacons before continuing.
 * 
 * @author Chien-Liang Fok
 */
public class RcvTelosbBeacons extends Instruction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8912211584049290382L;
	int numBeacons;
	
	/**
	 * The constructor.
	 * 
	 * @param minPeriod The minimum beacon period in milliseconds.
	 * @param maxPeriod The maximum beacon period in milliseconds.
	 * @param txPowerLevel The transmit power.
	 */
	public RcvTelosbBeacons(int numBeacons) {
		this.numBeacons = numBeacons;
	}
	
	public int getNumBeacons() {
		return numBeacons;
	}
	
	@Override
	public InstructionType getType() {
		return InstructionType.RCV_TELOSB_BEACONS;
	}

	@Override
	public boolean isCompatibleWith(Instruction instr) {
		// This instruction is not compatible with any other instruction
		return false;
	}

	public String toString() {
		return "RCV_TELOSB_BEACONS numBeacons = " + numBeacons;
	}
}
