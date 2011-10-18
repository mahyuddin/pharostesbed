package pharoslabut.demo.autoIntersection.server;

import pharoslabut.demo.autoIntersection.intersectionSpecs.IntersectionSpecs;
import pharoslabut.demo.autoIntersection.intersectionSpecs.TwoLaneFourWayIntersectionSpecs;
import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;

/**
 * The intersection manager defines the strategy for AIM
 * 
 * @author Chien-Liang Fok
 * @author Michael Hanna
 */
public class AutoIntersectionServer {

	// TODO: Add batch and other types.
	public static enum ServerType {SEQUENTIAL, PARALLEL, TRAFFIC_LIGHT};
	
	/**
	 * The server type.
	 */
	private ServerType serverType;
	
	/**
	 * The server's port.
	 */
	private int serverPort;
	
	/**
	 * The intersection specifications.
	 */
	private IntersectionSpecs intersectionSpecs;
	
	/**
	 * This is the actual daemon performing the intersection management.
	 */
	private ServerDaemon daemon = null;
	
    /**
     * default constructor
     * sets nextAvailableETC to a dummy low value to make the intersection available at initialization
     */
    public AutoIntersectionServer(ServerType serverType, int serverPort, 
    		IntersectionSpecs intersectionSpecs)
    {
    	Logger.log("Constructor called.");
    	this.serverType = serverType;
    	this.serverPort = serverPort;
    	this.intersectionSpecs = intersectionSpecs;
    }
    
    /**
     * Starts the server.
     */
    public synchronized void start() {
    	if (daemon == null) {
    		switch(serverType) {
    		case SEQUENTIAL:
    			Logger.log("Starting sequentual daemon.");
    			daemon = new SequentialDaemon(intersectionSpecs, serverPort);
    			daemon.start();
    			break;
    		case PARALLEL:
    			Logger.log("Starting parallel daemon.");
    			daemon = new ParallelDaemon(intersectionSpecs, serverPort);
    			daemon.start();
    			break;
    		case TRAFFIC_LIGHT:
    			Logger.log("Starting traffic light daemon.");
    			daemon = new TrafficLightDaemon(intersectionSpecs, serverPort);
    			daemon.start();
    			break;
    		}
    	} else 
    		Logger.logErr("Already started, daemon = " + daemon);
    }
	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(msg);
	}
    
	private static void usage() {
		System.setProperty ("PharosMiddleware.debug", "true");
		print("Usage: " + AutoIntersectionServer.class.getName() + " <options>\n");
		print("Where <options> include:");
		print("\t-type <sequential|parallel|trafficlight>: The type of intersection management (default sequential)");
		print("\t-port <port number>: The port on which to listen (default 7898)");
		print("\t-log <log file name>: The name of the file in which to save debug output (default null)");
		print("\t-debug: enable debug mode");
	}
	
    /**
     * call the ServerIntersectionManager and start running the code
     * @param args the command line arguments
     * @throws InterruptedException
     */
    public static void main(String [] args) {
		int serverPort = 7898;
		String logFileName = null;
		ServerType serverType = ServerType.SEQUENTIAL;
		
		// TODO: Allow user to specify this through the command line.
		IntersectionSpecs intersectionSpecs = TwoLaneFourWayIntersectionSpecs.getSpecs();
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-port")) {
					serverPort = Integer.valueOf(args[++i]);
				} else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				} else if (args[i].equals("-log")) {
					logFileName = args[++i];
				} else if (args[i].equals("-type")) {
					String mp = args[++i].toLowerCase();
					if (mp.equals("sequential"))
						serverType = ServerType.SEQUENTIAL;
					else if (mp.equals("parallel"))
						serverType = ServerType.PARALLEL;
					else if (mp.equals("trafficlight"))
						serverType = ServerType.TRAFFIC_LIGHT;
					else {
						System.err.println("Unknown mobility plane " + mp);
						usage();
						System.exit(1);
					}
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
		
		if (logFileName != null)
			Logger.setFileLogger(new FileLogger(logFileName));
		
		AutoIntersectionServer server = new AutoIntersectionServer(serverType, serverPort, intersectionSpecs);
		server.start();
    }
}
