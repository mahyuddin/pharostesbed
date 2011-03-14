package pharoslabut.logger;

import playerclient.GPSInterface;
import playerclient.PlayerClient;
import playerclient.PlayerException;
import playerclient.structures.PlayerConstants;
import playerclient.structures.gps.PlayerGpsData;

/**
 * Periodically accesses the GPS sensor and logs the location data to a file.
 * 
 * @author Chien-Liang Fok
 */
public class GPSLoggerEvent implements DeviceLogger, playerclient.GPSListener {
	
	private GPSInterface gps = null;
	private boolean logging;
	private int period;
	private FileLogger flogger;
	private long startTime;
	
	/**
	 * A constructor that creates a new PlayerClient to connect to the GPS device.
	 * 
	 * @param serverIP The IP address of the server
	 * @param serverPort The server's port number
	 * @param deviceIndex The index of the GPS device
	 */
	public GPSLoggerEvent(String serverIP, int serverPort, int deviceIndex) {
		
		PlayerClient client = null;
		
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
	public GPSLoggerEvent(GPSInterface gps) {
		this.gps = gps;
	}
	
	@Override
	public boolean start(int period, String fileName) {
		if (!logging) {
			logging = true;
			flogger = new FileLogger(fileName);
			startTime = System.currentTimeMillis();
			String result = "Time (ms)\tDelta Time (ms)\tGPS Quality\tLatitude\tLongitude\tAltitude\tGPS Time (s)\tGPS Time(us)\tGPS Error Vertical\tGPS Error Horizontal";
			log(result);
			gps.addGPSListener(this);
			return true;
		} else
			return false;
	}
	
	@Override
	public void stop() {
		logging = false;
		gps.removeGPSListener(this);
	}
	
	@Override
	public int getPeriod() {
		return period;
	}
	
	@Override
	public void newGPSData(PlayerGpsData gpsData) {
		String result;
		long endTime = System.currentTimeMillis();
		result = endTime + "\t" + (endTime - startTime) + "\t";

		PlayerGpsData currLocGPS = gpsData;
		result += currLocGPS.getQuality()  + "\t" + (currLocGPS.getLatitude()/1e7) + "\t" 
		+ (currLocGPS.getLongitude()/1e7) + "\t" + currLocGPS.getAltitude() + "\t"
		+ currLocGPS.getTime_sec() + "\t" + currLocGPS.getTime_usec() + "\t"
		+ currLocGPS.getErr_vert() + "\t" + currLocGPS.getErr_horz();	
		log(result);
	}
	
	private void log(String msg) {
		String result = "CompassLogger: " + msg;
		System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
	
	private static void usage() {
		System.err.println("Usage: pharoslabut.GPSLoggerEvent <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
		System.err.println("\t-index <index>: the index of the GPS device (default 0)");
		System.err.println("\t-file <file name>: The name of the file into which the compass data is logged (default log.txt)");
		System.err.println("\t-time <period>: The amount of time in seconds to record data (default infinity)");
		System.err.println("\t-d: enable debug output");
	}
	
	public static void main(String[] args) {
		String serverIP = "localhost";
		int serverPort = 6665;
		int index = 0;
		String fileName = "log.txt";
		
		// This is not used in an event-based logger, 
		// but remains to adhere to the DeviceLogger interface.
		int period = 0; 
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
		
		GPSLoggerEvent cl = new GPSLoggerEvent(serverIP, serverPort, index);
		cl.start(period, fileName);
		
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
