package pharoslabut.tests;

import pharoslabut.io.Message;
import pharoslabut.io.MessageReceiver;
import pharoslabut.io.TCPMessageReceiver;
import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;

public class TestTCPMessageReceiver implements MessageReceiver {

	/**
	 * A file logger for recording debug data.
	 */
//	private FileLogger flogger = null;
	
	/**
	 * The constructor.
	 */
	public TestTCPMessageReceiver(int port) {
		new TCPMessageReceiver(this, port);
	}
	
	@Override
	public void newMessage(Message msg) {
		Logger.log("newMessage: Message Received: " + msg);
	}
	
//    private void logErr(String msg) {
//		String result = "TestTCPMessageReceiver: ERROR: " + msg;
//		System.err.println(result);
//		
//		if (flogger != null)
//			flogger.log(result);
//	}
    
//	private void log(String msg) {
//		String result = "TestTCPMessageReceiver: " + msg;
//		System.out.println(result);
//		
//		if (flogger != null)
//			flogger.log(result);
//	}
	
	private static void usage() {
		System.err.println("Usage: " + TestTCPMessageReceiver.class.getName() + " <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-port <port number>: The Player Server's port number (default 12345)");
		System.err.println("\t-log <file name>: name of file in which to save results (default null)");
	}
	
	public static void main(String[] args) {
		int serverPort = 12345;
		
		System.setProperty ("PharosMiddleware.debug", "true");
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-port")) {
					serverPort = Integer.valueOf(args[++i]);
				}
				else if (args[i].equals("-log")) {
					Logger.setFileLogger(new FileLogger(args[++i], false));
				} else if (args[i].equals("-h")) {
					usage();
					System.exit(0);
				} else {
					System.err.println("Unknown option: " + args[i]);
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Server port: " + serverPort);
		
		new TestTCPMessageReceiver(serverPort);
	}
}
