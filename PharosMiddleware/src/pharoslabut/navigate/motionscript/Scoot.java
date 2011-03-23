package pharoslabut.navigate.motionscript;

/**
 * Moves the robot forward or backward the minimum amount.
 * 
 * @author Chien-Liang Fok
 */
public class Scoot extends Instruction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1548595252012045259L;
	
	/**
	 * The number of minimum movements.  Positive is forward, zero is negative.
	 */
	int amount;
	
	public Scoot(int amount) {
		this.amount = amount;
	}
	
	public int getAmount() {
		return amount;
	}
	
	@Override
	public InstructionType getType() {
		return InstructionType.SCOOT;
	}

	@Override
	public boolean isCompatibleWith(Instruction instr) {
		// The only instructions not compatible with SCOOT are PAUSE, SCOOT, and MOVE.
		switch(instr.getType()) {
		case PAUSE: 
			return false;
		case MOVE:
			return false;
		case SCOOT:
			return false;
		default:
			return true;
		}
	}

}
