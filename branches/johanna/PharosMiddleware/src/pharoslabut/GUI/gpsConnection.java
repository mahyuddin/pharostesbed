package pharoslabut.GUI;

import java.io.PrintStream;
import java.util.ArrayList;
import pharoslabut.exceptions.NoNewDataException;
import pharoslabut.sensors.*;

import playerclient3.GPSInterface;
import playerclient3.PlayerClient;
import playerclient3.PlayerException;
import playerclient3.structures.PlayerConstants;
import playerclient3.structures.gps.PlayerGpsData;
import pharoslabut.logger.FileLogger;

/**
 *
 * Edited Version ~ Johanna Rivera Santos
 * Summer 2011
 */
public class gpsConnection implements Runnable{
    
    


/**
 * Periodically accesses the GPS sensor and logs the location data to a file.
 * 
 * @author Chien-Liang Fok
 */

	public static final long GPS_LOGGER_REFRESH_PERIOD = 100;
	
	private PlayerClient client = null;
	private String serverIP;
	private int serverPort;
	private int deviceIndex;
	private GPSInterface gps = null;
	private GPSDataBuffer gpsdb;
	private boolean logging = false;
	private long startTime;
	private FileLogger flogger;
        private PlayerGpsData gpsData;
        
        
        
        public gpsConnection(){
            this(null);
        }
	
	/**
	 * A constructor that creates a new PlayerClient to connect to the GPS device.
	 * 
	 * @param serverIP The IP address of the server
	 * @param serverPort The server's port number
	 * @param deviceIndex The index of the GPS device
	 * @param flogger The FileLogger to use to save debug messages.
	 */
	public gpsConnection(String serverIP, int serverPort, int deviceIndex, FileLogger flogger) {
		
		this.serverIP = serverIP;
		this.serverPort = serverPort;
		this.deviceIndex = deviceIndex;
		this.flogger = flogger;
		
		
	}
	
	/**
	 * A constructor that uses an existing PlayerClient to connect to the GPS device.
	 * 
	 * @param gps The GPS' proxy.
	 */
	public gpsConnection(GPSInterface gps) {
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
			
			log("Time (ms)\tDelta Time (ms)\tGPS Quality\tLatitude\tLongitude\tAltitude\tGPS Time (s)\tGPS Time(us)\tGPS Error Vertical\tGPS Error Horizontal");
			
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
		try {
			logDbg("Connecting to server " + serverIP + ":" + serverPort);
			client = new PlayerClient(serverIP, serverPort);
		} catch(PlayerException e) {
			logErr("Problem connecting to Player server: " + e.toString());
			System.exit (1);
		}
		
		while (gps == null) {
			try {
				logDbg("Subscribing to GPS service...");
				gps = client.requestInterfaceGPS(deviceIndex, PlayerConstants.PLAYER_OPEN_MODE);
				if (gps != null)
					logDbg("Subscribed to GPS service...");
				else {
					logErr("GPS service was null, waiting 1s then retrying...");
					synchronized(this) {
						try {
							wait(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			} catch(PlayerException pe) {
				logErr(pe.getMessage());
			}
		

		logDbg("run: thread starting...");

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

					log(result);
				}
				
				oldData = gpsData;
				
			} catch (NoNewDataException e1) {
				logDbg("run: No new data...");
			}

			try {
				synchronized(this) {
					wait(GPS_LOGGER_REFRESH_PERIOD);
				}
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}

		logDbg("run: thread terminating...");
	}
	}
        
        
        
      
        public double Latitude(){
            
            
            return gpsData.getLatitude();
        }
        
        
        public double Longitude(){
           
            
            return gpsData.getLongitude();
        }
        
        public double Altitude(){
            
            return gpsData.getAltitude();
        }
        
        
        public String getServerIP() {
			return serverIP;
		}

		public void setServerIP(String serverIP) {
			this.serverIP = serverIP;
		}

		public int getServerPort() {
			return serverPort;
		}

		public void setServerPort(int serverPort) {
			this.serverPort = serverPort;
		}

		public int getDeviceIndex() {
			return deviceIndex;
		}

		public void setDeviceIndex(int deviceIndex) {
			this.deviceIndex = deviceIndex;
		}

		public long getStartTime() {
			return startTime;
		}

		public void setStartTime(long startTime) {
			this.startTime = startTime;
		}

	/**
	 * Log debug statements.  These statements are only logged if we are running in 
	 * debug mode.
	 * 
	 * @param msg The debug statement.
	 */
	private void logDbg(String msg) {
		String result = "GPSLogger: " + msg;
		if (System.getProperty ("PharosMiddleware.debug") != null) {
			System.out.println(result);
			if (flogger != null)
				flogger.log(result);
		}
	}
	
	/**
	 * Log error statements.  These are always logged regardless of whether we are running
	 * in debug mode.
	 * 
	 * @param msg The statement.
	 */
	private void logErr(String msg) {
		String result = "GPSLogger: ERROR: " + msg;
		System.err.println(result);
		if (flogger != null)
			flogger.log(result);
	}
	
	/**
	 * Log statements.  These are always logged regardless of whether we are running
	 * in debug mode.
	 * 
	 * @param msg The statement.
	 */
	private void log(String msg) {
		System.out.println(msg);
		if (flogger != null)
			flogger.log(msg);
	}
	
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
	
        
        


}

    


