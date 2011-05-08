package pharoslabut.logger;

import java.net.*;
import org.jfree.ui.RefineryUtilities;

import pharoslabut.CompassLoggerGUI;
import playerclient3.PlayerClient;
import playerclient3.PlayerException;
import playerclient3.Position2DInterface;
import playerclient3.structures.PlayerConstants;


/**
 * Periodically accesses the compass sensor and logs the heading data to a file.
 * After creating this object, you need to call start(...) before it will begin
 * accessing and logging the compass data.
 * 
 * @see CompassLoggerEvent
 * @author Chien-Liang Fok
 */
public class CompassLogger implements Runnable {
	
	//private String serverIP = null;
	private Position2DInterface compass = null;
	private CompassLoggerGUI gui;
	private FileLogger flogger;
	private long period;
	private boolean logging = false;
	
	/**
	 * A constructor that creates a new PlayerClient to connect to the compass.
	 * 
	 * @param serverIP The IP address of the server
	 * @param serverPort The server's port number
	 * @param deviceIndex The index of the compass device
	 * @param fileName The name of the file in which to save the log.
	 * @param showGUI Whether to display the compass logger's GUI
	 * @param getStatusMsgs Whether to get status messages.
	 */
	public CompassLogger(String serverIP, int serverPort, int deviceIndex, String fileName, 
			boolean showGUI, boolean getStatusMsgs) 
	{
		PlayerClient client = null;
		
		this.flogger = new FileLogger(fileName, false);
		
		try {
			log("Connecting to server " + serverIP + ":" + serverPort);
			client = new PlayerClient(serverIP, serverPort);
		} catch(PlayerException e) {
			log("Error connecting to Player: ");
			log("    [ " + e.toString() + " ]");
			System.exit (1);
		}
		
		while (compass == null) {
			try {
				log("Subscribing to compass service...");
				compass = client.requestInterfacePosition2D(deviceIndex, PlayerConstants.PLAYER_OPEN_MODE);
				if (compass != null)
					log("Subscribed to compass service...");
				else {
					log("ERROR: Compass service was null, pausing 1s then retrying...");
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
		
		if (getStatusMsgs) {
			//TODO use opaque interface to get status messages.
		}
		
		if (showGUI)
			initGUI();
	}
	
	/**
	 * A constructor that uses an existing Position2DInterface to connect to the compass.
	 * Does not display the CompassLogger's GUI or subscribe to status messages
	 * 
	 * @param compass The compass' proxy.
	 */
	public CompassLogger(Position2DInterface compass) {
		this(compass, false);
	}
	
	/**
	 * A constructor that uses an existing PlayerClient to connect to the compass.
	 * 
	 * @param compass The compass' proxy.
	 * @param showGUI Whether to display the compass logger's GUI
	 */
	public CompassLogger(Position2DInterface compass, boolean showGUI) {
		this.compass = compass;
		
		if (showGUI) 
			initGUI();
	}
	
	/**
	 * Sets the file logger.
	 * 
	 * @param flogger The file logger to use.
	 */
	public void setFileLogger(FileLogger flogger) {
		this.flogger = flogger;
	}
	
	private void initGUI() {
		gui = new CompassLoggerGUI("Proteus Compass Data");
		gui.pack();
		RefineryUtilities.centerFrameOnScreen(gui);
		gui.setVisible(true);
	}
	
	/**
	 * Starts the logging process.
	 * 
	 * @param period The period in which to access the compass.
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
		
	@Override
	public void run() {
		
		// Print a header to the table...
		log("Time (ms)\tDelta Time (ms)\tDelta Time (s)\tHeading (radians)");

		// Record the starting time...
		long startTime = System.currentTimeMillis();

		// Sit in a loop logging compass data...
		while(logging) {
			long endTime = System.currentTimeMillis();
			double deltaTimeMS = (endTime - startTime);
			double deltaTimeS = deltaTimeMS/1000;
			
			if (compass.isDataReady()) {
				double heading = compass.getYaw();
				log(endTime + "\t" + deltaTimeMS + "\t" + deltaTimeS + "\t" + heading);
				
				if (gui != null)
					gui.addData(deltaTimeS, heading);
			} else 
				log("No new compass data available...");

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
		String result = "CompassLogger: " + msg;
		System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
	
	private static void usage() {
		System.err.println("Usage: pharoslabut.CompassLogger <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
		System.err.println("\t-index <index>: the index of the compass device (default 1)");
		System.err.println("\t-file <file name>: The name of the file into which the compass data is logged (default log.txt)");
		System.err.println("\t-period <period>: The period of sampling in milliseconds (default 100)");
		System.err.println("\t-time <period>: The amount of time in seconds to record data (default infinity)");
		System.err.println("\t-nostatus: Do not subscribe to opaque interface to get status messages");
		System.err.println("\t-d: enable debug output");
	}
	
	public static void main(String[] args) {
		String serverIP = "localhost";
		int serverPort = 6665;
		int index = 1;
		String fileName = "log.txt";
		long period = 100; // period between sampling in milliseconds
		boolean showGUI = false;
		int time = 0;
		boolean getStatusMsgs = true;
		
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
			else if (args[i].equals("-debug") || args[i].equals("-d")) {
				System.setProperty ("PharosMiddleware.debug", "true");
			}
			else if (args[i].equals("-gui")) {
				showGUI = true;
			}
			else if (args[i].equals("-time")) {
				time = Integer.valueOf(args[++i]);
			}
			else if (args[i].equals("-nostatus")) {
				getStatusMsgs = false;
			}
			else {
				usage();
				System.exit(1);
			}
		}
		
		System.out.println("Server IP: " + serverIP);
		System.out.println("Server port: " + serverPort);
		System.out.println("Compass sensor index: " + index);
		System.out.println("File name: " + fileName);
		System.out.println("Sampling period: " + period);
		System.out.println("Debug: " + (System.getProperty("PharosMiddleware.debug") != null));
		System.out.println("Show GUI: " + showGUI);
		System.out.println("Log time: " + time + "s");
		System.out.println("Get status messages: " + getStatusMsgs);
		
		CompassLogger cl = new CompassLogger(serverIP, serverPort, index, fileName, showGUI, getStatusMsgs);
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
