package pharoslabut.exceptions;

/**
 * This is thrown when a component cannot carry out a requested task
 * because no new data is available.
 * 
 * @author Chien-Liang Fok
 *
 */
public class NoNewDataException extends Exception {

	private static final long serialVersionUID = -391157482277980727L;

	public NoNewDataException() {
		super();
	}
	
	public NoNewDataException(String msg) {
		super(msg);
	}
}
