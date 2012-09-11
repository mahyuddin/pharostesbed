package pharoslabut.navigate.motionscript;

public class StopBcastWifi extends Instruction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1447540864955184219L;

	public StopBcastWifi() {
		
	}
	
	@Override
	public InstructionType getType() {
		return InstructionType.STOP_BCAST_WIFI;
	}

	@Override
	public boolean isCompatibleWith(Instruction instr) {
		return true;
	}

	public String toString() {
		return "STOP_BCAST_WIFI";
	}
}
