package pharoslabut.logger;

import playerclient3.GPSInterface;
import playerclient3.PlayerClient;
import playerclient3.PlayerException;
import playerclient3.structures.PlayerConstants;
import playerclient3.structures.gps.PlayerGpsData;

/**
 * Periodically accesses the GPS sensor and logs the location data to a file.
 * 
 * @author Chien-Liang Fok
 */
public class GPSLogger implements Runnable {
	
	private GPSInterface gps = null;
	private boolean logging;
	private long period;
	private FileLogger flogger;
	
	/**
	 * A constructor that creates a new PlayerClient to connect to the GPS device.
	 * 
	 * @param serverIP The IP address of the server
	 * @param serverPort The server's port number
	 * @param deviceIndex The index of the GPS device
	 * @param fileName The name of the log file.
	 */
	public GPSLogger(String serverIP, int serverPort, int deviceIndex, String fileName) {
		
		PlayerClient client = null;
		
		flogger = new FileLogger(fileName, false);
		
		try {
			log("Connecting to server " + serverIP + ":" + serverPort);
			client = new PlayerClient(serverIP, serverPort);
		} catch(PlayerException e) {
			log("Error connecting to Player: ");
			log("    [ " + e.toString() + " ]");
			System.exit (1);
		}
		
		while (gps == null) {
			try {
				log("Subscribing to GPS service...");
				gps = client.requestInterfaceGPS(deviceIndex, PlayerConstants.PLAYER_OPEN_MODE);
				if (gps != null)
					log("Subscribed to GPS service...");
				else {
					log("ERROR: GPS service was null...");
					synchronized(this) {
						try {
							wait(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			} catch(PlayerException pe) {
				System.err.println("Error: " + pe.getMessage());
			}
		}
	}
	
	/**
	 * A constructor that uses an existing PlayerClient to connect to the GPS device.
	 * 
	 * @param gps The GPS' proxy.
	 */
	public GPSLogger(GPSInterface gps) {
		this.gps = gps;
	}
	
	/**
	 * Starts the logging process.
	 * 
	 * @param period The period in which to access the GPS sensor.
	 * @return true if the start was successful. false if it was already started.
	 */
	public boolean start(long period) {
		if (!logging) {
			logging = true;
			this.period = period;
			new Thread(this).start();
			return true;
		} else
			return false;
	}
	
	/**
	 * Stops the logging process.
	 */
	public void stop() {
		logging = false;
	}
	
	public void run() {
		log("Time (ms)\tDelta Time (ms)\tGPS Quality\tLatitude\tLongitude\tAltitude\tGPS Time (s)\tGPS Time(us)\tGPS Error Vertical\tGPS Error Horizontal");
		
		long startTime = System.currentTimeMillis();

		while(logging) {
			if (gps.isDataReady()) {
				long endTime = System.currentTimeMillis();
				PlayerGpsData currLocGPS = gps.getData();
				
				String result = endTime + "\t" + (endTime - startTime) + "\t";
				result += currLocGPS.getQuality()  + "\t" + (currLocGPS.getLatitude()/1e7) + "\t" 
					+ (currLocGPS.getLongitude()/1e7) + "\t" + currLocGPS.getAltitude() + "\t"
					+ currLocGPS.getTime_sec() + "\t" + currLocGPS.getTime_usec() + "\t"
					+ currLocGPS.getErr_vert() + "\t" + currLocGPS.getErr_horz();	
				log(result);
			} else 
				log("No new GPS data...");
			
			try {
				synchronized(this) {
					wait(period);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void log(String msg) {
		String result = "GPSLogger: " + msg;
		System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
	
	private static void usage() {
		System.err.println("Usage: pharoslabut.GPSLogger <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
		System.err.println("\t-index <index>: the index of the GPS device (default 0)");
		System.err.println("\t-file <file name>: The name of the file into which the compass data is logged (default log.txt)");
		System.err.println("\t-period <period>: The period of sampling in milliseconds (default 1000)");
		System.err.println("\t-time <period>: The amount of time in seconds to record data (default infinity)");
		System.err.println("\t-d: enable debug output");
	}
	
	public static void main(String[] args) {
		String serverIP = "localhost";
		int serverPort = 6665;
		int index = 0;
		String fileName = "log.txt";
		long period = 1500; // period between sampling in milliseconds
		int time = 0;
		
		for (int i=0; i < args.length; i++) {
			if (args[i].equals("-server")) {
				serverIP = args[++i];
			} 
			else if (args[i].equals("-port")) {
				serverPort = Integer.valueOf(args[++i]);
			} 
			else if (args[i].equals("-index")) {
				index = Integer.valueOf(args[++i]);
			} 
			else if (args[i].equals("-file")) {
				fileName = args[++i];
			}
			else if (args[i].equals("-period")) {
				period = Long.valueOf(args[++i]);
			}
			else if (args[i].equals("-d") || args[i].equals("-debug")) {
				System.setProperty ("PharosMiddleware.debug", "true");
			}
			else if (args[i].equals("-time")) {
				time = Integer.valueOf(args[++i]);
			}
			else {
				usage();
				System.exit(1);
			}
		}
		
		System.out.println("Server IP: " + serverIP);
		System.out.println("Server port: " + serverPort);
		System.out.println("GPS sensor index: " + index);
		System.out.println("File name: " + fileName);
		System.out.println("Sampling period: " + period);
		System.out.println("Debug: " + (System.getProperty("PharosMiddleware.debug") != null));
		System.out.println("Log time: " + time + "s");
		
		GPSLogger cl = new GPSLogger(serverIP, serverPort, index, fileName);
		cl.start(period);
		
		if (time > 0) {
			try {
				synchronized(cl) {
					cl.wait(time*1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			cl.stop();
			System.exit(0);
		}
	}
}
