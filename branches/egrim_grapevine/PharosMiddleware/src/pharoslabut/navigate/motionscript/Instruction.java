package pharoslabut.navigate.motionscript;

/**
 * The superclass that all instructions within a motion script must implement.
 * 
 * @author Chien-Liang Fok
 */
public abstract class Instruction implements java.io.Serializable {
	
	private static final long serialVersionUID = -8407258230822580785L;

	/**
	 * Returns the type of the instruction.
	 * @return the type of the instruction.
	 */
	public abstract InstructionType getType();
	
	/**
	 * Determines whether this instruction can execute in parallel with the specified 
	 * instruction.
	 * 
	 * @param instr The specified instruction.
	 * @return True if this instruction and the specified instruction can execute in parallel.
	 */
	public abstract boolean isCompatibleWith(Instruction instr);
	
}
