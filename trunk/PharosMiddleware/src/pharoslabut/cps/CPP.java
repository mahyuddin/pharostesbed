package pharoslabut.cps;

/**
 * A cyber-physical property.
 * 
 * @author Chien-Liang Fok
 *
 */
public class CPP {
	
	String instruction; // the instruction
	double lValue; // logical value
	double pValue; // physical value
	
	public CPP(String instruction, double lValue, double pValue) {
		this.instruction = instruction;
		this.lValue = lValue;
		this.pValue = pValue;
	}
	
	public String getInstruction() {
		return instruction;
	}
	
	public double getLValue() {
		return lValue;
	}
	
	public double getPValue() {
		return pValue;
	}
	
	public String toString() {
		return "CPP: " + instruction + ", " + lValue + ", " + pValue;
	}

}
