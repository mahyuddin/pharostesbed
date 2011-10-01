package pharoslabut.tests;

import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.sensors.CricketData;
import pharoslabut.sensors.CricketDataListener;
import pharoslabut.sensors.CricketInterface;

/**
 * Tests the connection to the Cricket Motes.
 * 
 * @author liangfok
 *
 */
public class TestCricketInterface implements CricketDataListener {

	private CricketInterface cricketInterface;
	
	/**
	 * The constructor.
	 * 
	 * @param serverIP The IP address of the robot.
	 * @param serverPort The port on which the robot is listening.
	 */
	public TestCricketInterface(String cricketPort) {
		Logger.log("Creading a Cricket Interface...");
		cricketInterface  = new CricketInterface(cricketPort);
		
		Logger.log("Registering self as Listener for Cricket data.");
		cricketInterface.registerCricketDataListener(this);
	}
	
	@Override
	public void newCricketData(CricketData cd) {
		Logger.log("New Cricket Data: " + cd);
		
	}

	private static void usage() {
		System.err.println("Usage: " + TestCricketInterface.class.getName() + " <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-port <port number>: The serial port to which the Cricket is attached (default /dev/ttyS1)");
		System.err.println("\t-log <file name>: name of file in which to save results (default null)");
		System.err.println("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String port = "/dev/ttyS1";
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-port")) {
					port = args[++i];
				}
				else if (args[i].equals("-log")) {
					Logger.setFileLogger(new FileLogger(args[++i], false)); 
				} else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
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
 
		new TestCricketInterface(port);
	}
}
