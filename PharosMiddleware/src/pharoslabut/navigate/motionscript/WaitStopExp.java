package pharoslabut.navigate.motionscript;


/**
 * Tells the robot to continue to participate in the experiment
 * until a stop experiment message is received.
 * 
 * @author Chien-Liang Fok
 */
public class WaitStopExp extends Instruction {
	private static final long serialVersionUID = 6690173404212662083L;

	public WaitStopExp() {
		
	}
	
	@Override
	public InstructionType getType() {
		return InstructionType.WAIT_EXP_STOP;
	}

	@Override
	public boolean isCompatibleWith(Instruction instr) {
		// This is not compatible with any other instruction.
		return false;
	}

}
