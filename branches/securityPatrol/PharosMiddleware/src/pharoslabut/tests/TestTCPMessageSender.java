package pharoslabut.tests;

import java.net.InetAddress;
import java.net.UnknownHostException;

import pharoslabut.behavior.MultiRobotBehaveMsg;
import pharoslabut.exceptions.PharosException;
import pharoslabut.io.TCPMessageSender;
import pharoslabut.logger.FileLogger;

public class TestTCPMessageSender {

	/**
	 * A file logger for recording debug data.
	 */
	private FileLogger flogger = null;
	
	/**
	 * The constructor.
	 */
	public TestTCPMessageSender() {
		
		flogger = new FileLogger("TestTCPMessageSender", false);
		
		InetAddress address = null;
		try {
			address = InetAddress.getByName("192.168.1.101");
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			System.exit(1); // fatal exception
		}
		int port = 12345;
		
		TCPMessageSender sndr = TCPMessageSender.getSender();
		sndr.setFileLogger(flogger);
		
		int cntr = 0;
		while(true) {
			try {
				MultiRobotBehaveMsg msg = new MultiRobotBehaveMsg("blah", 0, cntr++);
				log("Sending " + msg);
				sndr.sendMessage(address, port, msg);
			} catch (PharosException e) {
				logErr("Error while sending message: " + e.getMessage());
				e.printStackTrace();
			}
			pause(1000);
		}
		
	}
	
	private void pause(long interval) {
		synchronized(this) {
			try {
				this.wait(interval);
			} catch (InterruptedException e1) {	}
		}
	}
	
    private void logErr(String msg) {
		String result = "TestTCPMessageSender: ERROR: " + msg;
		System.err.println(result);
		
		if (flogger != null)
			flogger.log(result);
	}
    
	private void log(String msg) {
		String result = "TestTCPMessageSender: " + msg;
		System.out.println(result);
		
		if (flogger != null)
			flogger.log(result);
	}
	
	public static void main(String[] args) {
		System.setProperty ("PharosMiddleware.debug", "true");
		new TestTCPMessageSender();
	}
}
