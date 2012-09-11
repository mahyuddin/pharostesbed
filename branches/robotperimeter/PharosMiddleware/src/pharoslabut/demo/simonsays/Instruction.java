package pharoslabut.demo.simonsays;

/**
 * A command is a single instruction entered by the user.
 * 
 * @author Chien-Liang Fok
 */
public class Instruction {

	private InstructionType type;
	private boolean hasDoubleParam = false;
	private double doubleParam;
	private int line;

	public Instruction(InstructionType type, int line) {
		this.type = type;
		this.line = line;
	}
	
	public Instruction(InstructionType type, double param, int line) {
		this(type, line);
		this.doubleParam = param;
		hasDoubleParam = true;
	}
	
	public double getDoubleParam() {
		return doubleParam;
	}

	public InstructionType getType() {
		return type;
	}
	
	public int getLine() {
		return line;
	}
	
	public String toString() {
		if (hasDoubleParam)
			return "Instruction type=" + type + ", param=" + doubleParam + ", line=" + line;
		else
			return "Instruction type=" + type + ", line=" + line;
	}
}
