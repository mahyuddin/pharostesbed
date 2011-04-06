package pharoslabut.demo.simonsays;

import pharoslabut.io.Message;

public class Command {

	private Message msg;
	private int line;

	public Command(Message msg, int line) {
		this.msg = msg;
		this.line = line;
	}
	
	public Message getMessage() {
		return msg;
	}
	
	public int getLine() {
		return line;
	}
}
