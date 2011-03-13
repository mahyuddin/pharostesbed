package pharoslabut.navigate.motionscript;

/**
 * An instruction to start the periodic broadcasting of TelosB 
 * beacons.
 * 
 * @author Chien-Liang Fok
 */
public class StartBcastTelosB extends Instruction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8333186443038424512L;

	long period;
	int txPowerLevel;
	
	/**
	 * The constructor.
	 * 
	 * @param period The beacon period in milliseconds.
	 * @param txPowerLevel The transmit power.
	 */
	public StartBcastTelosB(long period, int txPowerLevel) {
		this.period = period;
		this.txPowerLevel = txPowerLevel;
	}
	
	public long getPeriod() {
		return period;
	}
	
	public int getTxPowerLevel() {
		return txPowerLevel;
	}
	
	
	@Override
	public InstructionType getType() {
		return InstructionType.START_BCAST_TELOSB;
	}

	@Override
	public boolean isCompatibleWith(Instruction instr) {
		// The only instruction is compatible with all other instructions
		switch(instr.getType()) {
		default:
			return true;
		}
	}

	public String toString() {
		return "START_BCAST_TELOSB period = " + period + ", tx power = " + txPowerLevel;
	}
}
