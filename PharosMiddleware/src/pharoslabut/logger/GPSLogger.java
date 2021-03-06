package pharoslabut.logger;

import pharoslabut.exceptions.NoNewDataException;
import pharoslabut.sensors.*;

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
	public static final long GPS_LOGGER_REFRESH_PERIOD = 100;
	
	private GPSInterface gps = null;
	private GPSDataBuffer gpsdb;
	private boolean logging = false;
	private long startTime;
//	private FileLogger flogger;
	
	/**
	 * A constructor that creates a new PlayerClient to connect to the GPS device.
	 * 
	 * @param serverIP The IP address of the server
	 * @param serverPort The server's port number
	 * @param deviceIndex The index of the GPS device
	 */
	public GPSLogger(String serverIP, int serverPort, int deviceIndex) {
		
		PlayerClient client = null;
		
//		this.flogger = flogger;
		
		try {
			Logger.logDbg("Connecting to server " + serverIP + ":" + serverPort);
			client = new PlayerClient(serverIP, serverPort);
		} catch(PlayerException e) {
			Logger.logErr("Problem connecting to Player server: " + e.toString());
			System.exit (1);
		}
		
		while (gps == null) {
			try {
				Logger.logDbg("Subscribing to GPS service...");
				gps = client.requestInterfaceGPS(deviceIndex, PlayerConstants.PLAYER_OPEN_MODE);
				if (gps != null)
					Logger.logDbg("Subscribed to GPS service...");
				else {
					Logger.logErr("GPS service was null, waiting 1s then retrying...");
					synchronized(this) {
						try {
							wait(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			} catch(PlayerException pe) {
				Logger.logErr(pe.getMessage());
			}
		}
		
		// The runThreaded and change of data delivery mode can be done in reverse order.
		Logger.logDbg("Setting Player Client to run in continuous threaded mode...");
		client.runThreaded(-1, -1);
		
		Logger.logDbg("Changing Player server mode to PUSH...");
		client.requestDataDeliveryMode(playerclient3.structures.PlayerConstants.PLAYER_DATAMODE_PUSH);
		
		Logger.logDbg("Creating GPSDataBuffer...");
		gpsdb = new GPSDataBuffer(gps);
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
	 * @return true if the start was successful. false if it was already started.
	 */
	public boolean start() {
		if (!logging) {
			logging = true;
			
			Logger.log("Time (ms)\tDelta Time (ms)\tGPS Quality\tLatitude\tLongitude\tAltitude\tGPS Time (s)\tGPS Time(us)\tGPS Error Vertical\tGPS Error Horizontal");
			
			startTime = System.currentTimeMillis();
			
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
	
//	@Override
//	public void newGPSData(PlayerGpsData gpsData) {
//		long endTime = System.currentTimeMillis();
//		
//		String result = endTime + "\t" + (endTime - startTime) + "\t";
//		result += gpsData.getQuality()  + "\t" + (gpsData.getLatitude()/1e7) + "\t" 
//			+ (gpsData.getLongitude()/1e7) + "\t" + gpsData.getAltitude() + "\t"
//			+ gpsData.getTime_sec() + "\t" + gpsData.getTime_usec() + "\t"
//			+ gpsData.getErr_vert() + "\t" + gpsData.getErr_horz();
//		
//		log(result);
//	}
	
	PlayerGpsData oldData = null;
	/**
	 * This method sits in a loop polling for GPS data.
	 */
	public void run() {
		PlayerGpsData gpsData;

		Logger.logDbg("thread starting...");

		while(logging) {
			try {
				
				gpsData = gpsdb.getCurrLoc();
				
				if (oldData != null && !oldData.equals(gpsData)) {

					long endTime = System.currentTimeMillis();


					String result = endTime + "\t" + (endTime - startTime) + "\t";
					result += gpsData.getQuality()  + "\t" + (gpsData.getLatitude()/1e7) + "\t" 
					+ (gpsData.getLongitude()/1e7) + "\t" + gpsData.getAltitude() + "\t"
					+ gpsData.getTime_sec() + "\t" + gpsData.getTime_usec() + "\t"
					+ gpsData.getErr_vert() + "\t" + gpsData.getErr_horz();

					Logger.log(result);
				}
				
				oldData = gpsData;
				
			} catch (NoNewDataException e1) {
				Logger.logDbg("No new data...");
			}

			try {
				synchronized(this) {
					wait(GPS_LOGGER_REFRESH_PERIOD);
				}
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}

		Logger.logDbg("thread terminating...");
	}

//	/**
//	 * Log debug statements.  These statements are only logged if we are running in 
//	 * debug mode.
//	 * 
//	 * @param msg The debug statement.
//	 */
//	private void logDbg(String msg) {
//		String result = "GPSLogger: " + msg;
//		if (System.getProperty ("PharosMiddleware.debug") != null) {
//			System.out.println(result);
//			if (flogger != null)
//				flogger.log(result);
//		}
//	}
//	
//	/**
//	 * Log error statements.  These are always logged regardless of whether we are running
//	 * in debug mode.
//	 * 
//	 * @param msg The statement.
//	 */
//	private void logErr(String msg) {
//		String result = "GPSLogger: ERROR: " + msg;
//		System.err.println(result);
//		if (flogger != null)
//			flogger.log(result);
//	}
//	
//	/**
//	 * Log statements.  These are always logged regardless of whether we are running
//	 * in debug mode.
//	 * 
//	 * @param msg The statement.
//	 */
//	private void log(String msg) {
//		System.out.println(msg);
//		if (flogger != null)
//			flogger.log(msg);
//	}
	
	private static void usage() {
		System.err.println("Usage: pharoslabut.logger.GPSLogger <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
		System.err.println("\t-index <index>: the index of the GPS device (default 0)");
		System.err.println("\t-log <file name>: The name of the file into which the compass data is logged (default log.txt)");
		System.err.println("\t-time <period>: The amount of time in seconds to record data (default infinity)");
		System.err.println("\t-d: enable debug output");
	}
	
	public static void main(String[] args) {
		String serverIP = "localhost";
		int serverPort = 6665;
		int index = 0;
		String fileName = null;
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
			else if (args[i].equals("-log")) {
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
		System.out.println("Loge: " + fileName);
		System.out.println("Debug: " + (System.getProperty("PharosMiddleware.debug") != null));
		System.out.println("Log time: " + time + "s");
	
		if (fileName != null)
			Logger.setFileLogger(new FileLogger(fileName, false));
		
		GPSLogger gl = new GPSLogger(serverIP, serverPort, index);
		gl.start();
		
		if (time > 0) {
			try {
				synchronized(gl) {
					gl.wait(time*1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			gl.stop();
			System.exit(0);
		}
	}
}
