package pharoslabut.navigate.motionscript;

import java.util.*;

/**
 * A parallel instruction contains multiple regular instructions
 * that should be executed in parallel.  All of the instructions that are executed
 * in parallel must complete before the Parallel instruction terminates.
 * 
 * @author Chien-Liang Fok
 */
public class ParallelInstruction extends Instruction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5017425943786424442L;
	/**
	 * These are the instructions that should be executed in parallel.
	 */
	private Vector<Instruction> instructions = new Vector<Instruction>();

	/**
	 * The constructor.
	 */
	public ParallelInstruction() {
	}
	
	/**
	 * Adds an instruction that should be executed in parallel.
	 * @param i The instruction to add.
	 * @throws IncompatibleInstructionException If the instruction to be added is incompatible
	 * with any of the existing instructions that are already to be executed in parallel.
	 */
	public void addInstruction(Instruction i) throws IncompatibleInstructionException {
		Enumeration<Instruction> e = instructions.elements();
		while (e.hasMoreElements()) {
			Instruction currInst = e.nextElement();
			if (!currInst.isCompatibleWith(i))
				throw new IncompatibleInstructionException("Instruction " + i + " incompatible with instruction " + currInst);
		}
		instructions.add(i);
	}
	
	/**
	 * An accessor to the instructions that should be executed in parallel.
	 * 
	 * @return An enumeration of the instructions that should be executed in parallel.
	 */
	public Enumeration<Instruction> getInstructions() {
		return instructions.elements();
	}
	
	@Override
	public InstructionType getType() {
		return InstructionType.PARALLEL;
	}

	@Override
	public boolean isCompatibleWith(Instruction instr) {
		// Parallel instructions cannot run in parallel with any other instructions.
		return false;
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer("PARALLEL Execute:");
		Enumeration<Instruction> e = instructions.elements();
		while (e.hasMoreElements()) {
			Instruction currInst = e.nextElement();
			result.append("\n\t" + currInst.toString());
		}
		return result.toString();
	}
}
