package pharoslabut.logger.analyzer.tcpdump;

/**
 * Thrown when a line in a tcpdump log file cannot be processed.
 * 
 * @author Chien-Liang Fok
 */
public class InvalidFormatException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -848644017511897655L;

	public InvalidFormatException(String msg) {
		super(msg);
	}
}
