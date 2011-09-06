package pharoslabut.demo.autoIntersection;

import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.LineFollower;
import pharoslabut.tests.TestLineFollower;

/**
 * Evaluates the intersection detector.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.demo.autoIntersection.IntersectionDetector
 */
public class TestIntersectionDetector implements IntersectionEventListener {

	public TestIntersectionDetector(String serverIP, int serverPort) {
		LineFollower lf = new LineFollower(serverIP, serverPort);
		IntersectionDetector id = new IntersectionDetector();
		lf.addBlobDataConsumer(id);
		lf.start();
	}
	
	@Override
	public void newIntersectionEvent(IntersectionEvent ie) {
		Logger.log("**** INTERSECTION EVENT: " + ie);
		
	}
	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(msg);
	}
	
	private static void usage() {
		System.setProperty ("PharosMiddleware.debug", "true");
		print("Usage: " + TestIntersectionDetector.class.getName() + " <options>\n");
		print("Where <options> include:");
		print("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		print("\t-port <port number>: The Player Server's port number (default 6665)");
		print("\t-log <log file name>: The name of the file in which to save debug output (default null)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String serverIP = "localhost";
		int serverPort = 6665;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-server")) {
					serverIP = args[++i];
				} else if (args[i].equals("-port")) {
					serverPort = Integer.valueOf(args[++i]);
				} else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				} else if (args[i].equals("-log")) {
					Logger.setFileLogger(new FileLogger(args[++i]));
				} else if (args[i].equals("-h")) {
					usage();
					System.exit(0);
				} else {
					print("Unknown argument " + args[i]);
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
 
		print("Server IP: " + serverIP);
		print("Server port: " + serverPort);
		
		new TestIntersectionDetector(serverIP, serverPort);
	}
}
