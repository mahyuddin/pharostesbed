package pharoslabut.tests;

import pharoslabut.io.Message;
import pharoslabut.io.MessageReceiver;
import pharoslabut.io.TCPMessageReceiver;
import pharoslabut.logger.FileLogger;

public class TestTCPMessageReceiver implements MessageReceiver {

	/**
	 * A file logger for recording debug data.
	 */
	private FileLogger flogger = null;
	
	/**
	 * The constructor.
	 */
	public TestTCPMessageReceiver() {
		flogger = new FileLogger("TestTCPMessageReceiver", false);
		int port = 12345;
		
		new TCPMessageReceiver(this, port, flogger);
	}
	
	@Override
	public void newMessage(Message msg) {
		log("newMessage: Message Received: " + msg);
	}
	
    private void logErr(String msg) {
		String result = "TestTCPMessageReceiver: ERROR: " + msg;
		System.err.println(result);
		
		if (flogger != null)
			flogger.log(result);
	}
    
	private void log(String msg) {
		String result = "TestTCPMessageReceiver: " + msg;
		System.out.println(result);
		
		if (flogger != null)
			flogger.log(result);
	}
	
	public static void main(String[] args) {
		System.setProperty ("PharosMiddleware.debug", "true");
		new TestTCPMessageReceiver();
	}
}
