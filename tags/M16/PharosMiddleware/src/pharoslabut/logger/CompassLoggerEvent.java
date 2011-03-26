package pharoslabut.logger;

//import java.net.*;
import org.jfree.ui.RefineryUtilities;

import pharoslabut.CompassLoggerGUI;
import playerclient.*;
import playerclient.structures.PlayerConstants;
import playerclient.structures.opaque.PlayerOpaqueData;
import playerclient.structures.position2d.PlayerPosition2dData;


/**
 * Subscribes to the compass sensor and logs the heading data to a file.
 * It uses an event-based model to efficiently gather compass data.
 * 
 * @author Chien-Liang Fok
 */
public class CompassLoggerEvent implements DeviceLogger, Position2DListener, OpaqueListener {
	
	//private String serverIP = null;
	private Position2DInterface compass = null;
	private CompassLoggerGUI gui;
	private FileLogger flogger;
	private int period;
	private boolean logging = false;
	private long startTime;
	
	/**
	 * A constructor that creates a new PlayerClient to connect to the compass.
	 * 
	 * @param serverIP The IP address of the server
	 * @param serverPort The server's port number
	 * @param deviceIndex The index of the compass device
	 * @param showGUI Whether to display the compass logger's GUI
	 * @param getStatusMsgs Whether to subscribe to the opaque interface to receive status messages
	 */
	public CompassLoggerEvent(String serverIP, int serverPort, int deviceIndex, boolean showGUI, boolean getStatusMsgs) {
		PlayerClient client = null;
		//this.serverIP = serverIP;
		
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
				//log("Subscribing to compass service...");
				compass = client.requestInterfacePosition2D(deviceIndex, PlayerConstants.PLAYER_OPEN_MODE);
				if (compass != null)
					log("Subscribed to compass service...");
				else {
					log("ERROR: Compass service was null...");
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
			log("Subscribing to opaque interface...");
			OpaqueInterface oi = client.requestInterfaceOpaque(0, PlayerConstants.PLAYER_OPEN_MODE);
			if (oi != null) {
				log("Subscribed to opaque interface.  Will log MCU messages.");
				oi.addOpaqueListener(this);
			} else {
				log("ERROR: Unable to subscribe to opaque interface.  MCU messages will not be received.");
			}
		}
		
		if (showGUI)
			initGUI();
	}
	
	/**
	 * A constructor that uses an existing Position2DInterface to connect to the compass.
	 * Does not display the CompassLogger's GUI.
	 * 
	 * @param compass The compass' proxy.
	 */
	public CompassLoggerEvent(Position2DInterface compass) {
		this(compass, false);
	}
	
	/**
	 * A constructor that uses an existing PlayerClient to connect to the compass.
	 * 
	 * @param compass The compass' proxy.
	 * @param showGUI Whether to display the compass logger's GUI
	 */
	public CompassLoggerEvent(Position2DInterface compass, boolean showGUI) {
		this.compass = compass;
		if (showGUI) 
			initGUI();
	}
	
	private void initGUI() {
		gui = new CompassLoggerGUI("Proteus Compass Data");
		gui.pack();
		RefineryUtilities.centerFrameOnScreen(gui);
		gui.setVisible(true);
	}
	
	/**
	 * Note that the period parameter is ignored.
	 */
	@Override
	public boolean start(int period, String fileName) {
		if (!logging) {
			logging = true;
			flogger = new FileLogger(fileName);
			
			String result = "Time (ms)\tDelta Time (ms)\tDelta Time (s)\tHeading (radians)";
			log(result);
			
			startTime = System.currentTimeMillis();
			compass.addPos2DListener(this);
			return true;
		} else
			return false;
	}
	
	@Override
	public void stop() {
		logging = false;
		compass.removePos2DListener(this);
	}
	
	@Override
	public int getPeriod() {
		return period;
	}
	
	@Override
	public void newPlayerPosition2dData(PlayerPosition2dData data) {
		long endTime = System.currentTimeMillis();
		double deltaTimeMS = (endTime - startTime);
		double deltaTimeS = deltaTimeMS/1000;
		double heading = data.getPos().getPa();
		String result = endTime + "\t" + deltaTimeMS + "\t" + deltaTimeS + "\t" + heading;
		log(result);
		if (gui != null)
			gui.addData(deltaTimeS, heading);
	}
	
	@Override
	public void newOpaqueData(PlayerOpaqueData opaqueData) {
		if (opaqueData.getDataCount() > 0) {
			String s = new String(opaqueData.getData());
			log("MCU Message: " + s);
		}
	}
	
	private void log(String msg) {
		String result = "CompassLoggerEvent: " + msg;
		System.out.println(result);
		if (flogger != null)
			flogger.log(result);
	}
	
	private static void usage() {
		System.err.println("Usage: pharoslabut.CompassLoggerEvent <options>\n");
		System.err.println("Where <options> include:");
		System.err.println("\t-server <ip address>: The IP address of the Player Server (default localhost)");
		System.err.println("\t-port <port number>: The Player Server's port number (default 6665)");
		System.err.println("\t-index <index>: the index of the compass device (default 1)");
		System.err.println("\t-file <file name>: The name of the file into which the compass data is logged (default log.txt)");
		System.err.println("\t-period <period>: The period of sampling in milliseconds (default 100)");
		System.err.println("\t-time <period>: The amount of time in seconds to record data (default infinity)");
		System.err.println("\t-gui: Display the GUI");
		System.err.println("\t-d: enable debug output");
		System.err.println("\t-getStatusMsgs: whether to subscribe to the interface that provides MCU status messages (default false)");
	}
	
	public static void main(String[] args) {
		String serverIP = "localhost";
		int serverPort = 6665;
		int index = 1;
		String fileName = "log.txt";
		int period = 100; // period between sampling in milliseconds
		boolean showGUI = false;
		int time = 0;
		boolean getStatusMsgs = false;
		
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
				period = Integer.valueOf(args[++i]);
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
			else if (args[i].equals("-getStatusMsgs")) {
				getStatusMsgs = true;
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
		System.out.println("GetStatusMsgs: " + getStatusMsgs);
		
		CompassLoggerEvent cl = new CompassLoggerEvent(serverIP, serverPort, index, showGUI, getStatusMsgs);
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
