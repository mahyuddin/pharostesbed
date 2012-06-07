package pharoslabut.tests;

import java.net.InetAddress;
import java.net.UnknownHostException;

import pharoslabut.exceptions.PharosException;
import pharoslabut.experiment.ExpType;
import pharoslabut.io.StartExpMsg;
import pharoslabut.io.TCPMessageSender;
import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;

public class TestTCPMessageSender {

	/**
	 * A file logger for recording debug data.
	 */
//	private FileLogger flogger = null;
	
	/**
	 * The constructor.
	 */
	public TestTCPMessageSender(String serverIP, int port) {
		
		InetAddress address = null;
		try {
			address = InetAddress.getByName(serverIP);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			System.exit(1); // fatal exception
		}
		
		TCPMessageSender sndr = TCPMessageSender.getSender();
		
		int cntr = 0;
		while(true) {
			try {
				StartExpMsg msg = new StartExpMsg("TestExp", ExpType.FOLLOW_GPS_MOTION_SCRIPT);
				Logger.log("Sending " + msg);
				sndr.sendMessage(address, port, msg);
			} catch (PharosException e) {
				Logger.logErr("Problem while sending message: " + e.getMessage());
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
	
//    private void logErr(String msg) {
//		String result = "TestTCPMessageSender: ERROR: " + msg;
//		System.err.println(result);
//		
//		if (flogger != null)
//			flogger.log(result);
//	}
//    
//	private void log(String msg) {
//		String result = "TestTCPMessageSender: " + msg;
//		System.out.println(result);
//		
//		if (flogger != null)
//			flogger.log(result);
//	}
	
	private static void usage() {
		System.err.println("Usage: " + TestTCPMessageSender.class.getName() + " <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
		System.err.println("\t-log <file name>: name of file in which to save results (default null)");
	}
	
	public static void main(String[] args) {
		String logFileName = "TestTCPMessageSender";
		String serverIP = "localhost";
		int serverPort = 12345;
		
		System.setProperty ("PharosMiddleware.debug", "true");
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-server")) {
					serverIP = args[++i];
				} 
				else if (args[i].equals("-port")) {
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
 
		System.out.println("Server IP: " + serverIP);
		System.out.println("Server port: " + serverPort);
		System.out.println("Log: " + logFileName);
		
		new TestTCPMessageSender(serverIP, serverPort);
	}
}
