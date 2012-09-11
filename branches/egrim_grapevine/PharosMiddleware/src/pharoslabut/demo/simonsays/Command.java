package pharoslabut.demo.simonsays;

import pharoslabut.io.AckableMessage;

/**
 * A command is a single instruction entered by the user.
 * 
 * @author Chien-Liang Fok
 */
public class Command {

	private AckableMessage msg;
	private int line;

	public Command(AckableMessage msg, int line) {
		this.msg = msg;
		this.line = line;
	}
	
	public AckableMessage getMessage() {
		return msg;
	}
	
	public int getLine() {
		return line;
	}
}
