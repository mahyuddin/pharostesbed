package pharoslabut.navigate.motionscript;

/**
 * An instruction to stop the periodic broadcasting of TelosB 
 * beacons.
 * 
 * @author Chien-Liang Fok
 */
public class StopBcastTelosB extends Instruction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8123888976643890373L;

	public StopBcastTelosB() {
		
	}
	
	@Override
	public InstructionType getType() {
		return InstructionType.STOP_BCAST_TELOSB;
	}

	@Override
	public boolean isCompatibleWith(Instruction instr) {
		return true;
	}
	
	public String toString() {
		return "STOP_BCAST_TELOSB";
	}

}
