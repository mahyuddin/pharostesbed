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

	long minPeriod, maxPeriod;
	short txPowerLevel;
	
	/**
	 * The constructor.
	 * 
	 * @param minPeriod The minimum beacon period in milliseconds.
	 * @param maxPeriod The maximum beacon period in milliseconds.
	 * @param txPowerLevel The transmit power.
	 */
	public StartBcastTelosB(long minPeriod, long maxPeriod, short txPowerLevel) {
		this.minPeriod = minPeriod;
		this.maxPeriod = maxPeriod;
		this.txPowerLevel = txPowerLevel;
	}
	
	public long getMinPeriod() {
		return minPeriod;
	}
	
	public long getMaxPeriod() {
		return maxPeriod;
	}
	
	public short getTxPowerLevel() {
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
		return "START_BCAST_TELOSB min period = " + minPeriod + ", max period = " + maxPeriod 
			+ ", tx power = " + txPowerLevel;
	}
}
