package pharoslabut.navigate.motionscript;

/**
 * An instruction to pause the robot (i.e., stop it from moving).
 * 
 * @author Chien-Liang Fok
 */
public class Pause extends Instruction {
	
	private static final long serialVersionUID = -3748283557820113037L;
	
	/**
	 * The pause time in milliseconds.
	 */
	private long pauseTime;
	
	/**
	 * The constructor.
	 * 
	 * @param pauseTime The pause time in milliseconds.
	 */
	public Pause(long pauseTime) {
		this.pauseTime = pauseTime;
	}
	
	@Override
	public InstructionType getType() {
		return InstructionType.PAUSE;
	}
	
	public long getPauseTime() {
		return pauseTime;
	}

	@Override
	public boolean isCompatibleWith(Instruction instr) {
		// The only instruction not compatible with PAUSE is MOVE.
		switch(instr.getType()) {
		case MOVE: 
			return false;
		default:
			return true;
		}
	}
	
	public String toString() {
		return "PAUSE for " + pauseTime;
	}
}
