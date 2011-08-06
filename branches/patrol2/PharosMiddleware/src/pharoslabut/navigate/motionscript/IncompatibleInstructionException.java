package pharoslabut.navigate.motionscript;

/**
 * This is thrown whenever the user wants to execute two instructions that cannot
 * be executed in parallel.
 * 
 * @author Chien-Liang Fok
 *
 */
public class IncompatibleInstructionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 463771813770250785L;

	
	public IncompatibleInstructionException(String msg) {
		super(msg);
	}
}
