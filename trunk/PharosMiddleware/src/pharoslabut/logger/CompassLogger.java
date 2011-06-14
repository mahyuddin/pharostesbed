package pharoslabut.logger;

import org.jfree.ui.RefineryUtilities;

import pharoslabut.sensors.*;
import playerclient3.PlayerClient;
import playerclient3.PlayerException;
import playerclient3.Position2DInterface;
import playerclient3.structures.PlayerConstants;
import playerclient3.structures.position2d.PlayerPosition2dData;


/**
 * Periodically accesses the compass sensor and logs the heading data to a file.
 * After creating this object, you need to call start(...) before it will begin
 * accessing and logging the compass data.
 * 
 * @see CompassLoggerEvent
 * @author Chien-Liang Fok
 */
public class CompassLogger implements Position2DListener, ProteusOpaqueListener {
	
	//private String serverIP = null;
	private Position2DInterface compass = null;
	private ProteusOpaqueInterface poi;
	private CompassLoggerGUI gui;
	private FileLogger flogger;
	private boolean logging = false;
	private long startTime;
	
	/**
	 * A constructor that creates a new PlayerClient to connect to the compass.
	 * 
	 * @param serverIP The IP address of the server
	 * @param serverPort The server's port number
	 * @param deviceIndex The index of the compass device
	 * @param flogger The name of the file in which to save the log.
	 * @param showGUI Whether to display the compass logger's GUI
	 * @param getStatusMsgs Whether to get status messages.
	 */
	public CompassLogger(String serverIP, int serverPort, int deviceIndex, FileLogger flogger, 
			boolean showGUI, boolean getStatusMsgs) 
	{
		PlayerClient client = null;
	
		this.flogger = flogger;
		
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
		
		log("Creating CompassDataBuffer...");
		CompassDataBuffer cdb = new CompassDataBuffer(compass);
		
		log("Registering self as listener to CompassDataBuffer events...");
		cdb.addPos2DListener(this);
		cdb.start();
		
		if (getStatusMsgs) {
			log("Connecting to ProteusOpaqueInterface...");
			poi = (ProteusOpaqueInterface)client.requestInterfaceOpaque(0, PlayerConstants.PLAYER_OPEN_MODE);
			
			log("Registering self as listener to Opaque data...");
			poi.addOpaqueListener(this);
		}
		
		if (showGUI)
			initGUI();
		
		log("Changing Player server mode to PUSH...");
		client.requestDataDeliveryMode(playerclient3.structures.PlayerConstants.PLAYER_DATAMODE_PUSH);
		
		log("Setting Player Client to run in continuous threaded mode...");
		client.runThreaded(-1, -1);
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
	public boolean start() {
		if (!logging) {
			logging = true;
			
			// Print a header to the table...
			log("Time (ms)\tDelta Time (ms)\tDelta Time (s)\tHeading (radians)");
			
			// Record the starting time...
			startTime = System.currentTimeMillis();
			
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
	
	/**
	 * This should be called whenever new compass data arrives.
	 */
	@Override
	public void newPlayerPosition2dData(PlayerPosition2dData data) {
		long endTime = System.currentTimeMillis();
		double deltaTimeMS = (endTime - startTime);
		double deltaTimeS = deltaTimeMS/1000;

		
		double heading = data.getPos().getPa();
		log(endTime + "\t" + deltaTimeMS + "\t" + deltaTimeS + "\t" + heading);

		if (gui != null)
			gui.addData(deltaTimeS, heading);
	}
	
	@Override
	public void newOpaqueData(ProteusOpaqueData opaqueData) {
		String s = new String(opaqueData.getData());
		log(s);
	}
	
	private void log(String msg) {
		String result = "CompassLogger: " + msg;
		System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
	
	private static void usage() {
		System.err.println("Usage: pharoslabut.logger.CompassLogger <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
		System.err.println("\t-index <index>: the index of the compass device (default 1)");
		System.err.println("\t-log <file name>: The name of the file into which the compass data is logged (default null)");
		System.err.println("\t-time <period>: The amount of time in seconds to record data (default infinity)");
		System.err.println("\t-nostatus: Do not subscribe to opaque interface to get status messages");
		System.err.println("\t-d: enable debug output");
	}
	
	public static void main(String[] args) {
		String serverIP = "localhost";
		int serverPort = 6665;
		int index = 1;
		String fileName = null;
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
			else if (args[i].equals("-log")) {
				fileName = args[++i];
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
		System.out.println("Debug: " + (System.getProperty("PharosMiddleware.debug") != null));
		System.out.println("Show GUI: " + showGUI);
		System.out.println("Log time: " + time + "s");
		System.out.println("Get status messages: " + getStatusMsgs);
		
		FileLogger flogger = null;
		if (fileName != null)
			flogger = new FileLogger(fileName, false);
		
		CompassLogger cl = new CompassLogger(serverIP, serverPort, index, flogger, showGUI, getStatusMsgs);
		cl.start();
		
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
