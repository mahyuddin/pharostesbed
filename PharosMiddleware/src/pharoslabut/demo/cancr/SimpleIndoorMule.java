package pharoslabut.demo.cancr;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;

import pharoslabut.RobotIPAssignments;
import pharoslabut.exceptions.PharosException;
import pharoslabut.logger.FileLogger;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.LineFollower;
import pharoslabut.navigate.LineFollowerError;
import pharoslabut.navigate.Location;
import playerclient3.PlayerClient;
import playerclient3.PlayerException;

/**
 * A simple implementation of an indoor mule.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.navigate.LineFollower
 */
public class SimpleIndoorMule implements pharoslabut.navigate.LineFollowerListener, Runnable {

	/**
	 * The minimum time that a node must remain at a waypoint.
	 */
	public static final long WAYPOINT_STAY_TIME = 10000;
	
	/**
	 * The port that the local contextinator is listening on.
	 */
	private static final int port = 6666;
	
	/**
	 * The location of the source.
	 */
	private static final Location srcLoc = new Location(2, 0);
	
	/**
	 * The location of the sink.
	 */
	private static final Location sinkLoc = new Location(0, 1);
	
	/**
	 * My starting location.
	 */
	private Location currLoc;
	
	/**
	 * My destination location.
	 */
	private Location destLoc;
	
	/**
	 * Whether the LineFollower is in an error state.
	 */
	private boolean inErrorState = false;
	
	/**
	 * Whether the node is at a waypoint.
	 */
	private boolean atWaypoint = false;
	
	private long errorBeginTime;
	
	private boolean useContextinator = true;
	
	/**
	 * The constructor.
	 * 
	 * @param expName The experiment name.
	 * @param playerIP The IP address of the player server.
	 * @param playerPort The port of the player server.
	 * @param contextinatorPort The port of the Contextinator.
	 * @param startLoc The starting location.
	 * @param useContextinator Whether to use the contextinator
	 */
	public SimpleIndoorMule(String expName, String playerIP, int playerPort, 
			int contextinatorPort, Location startLoc, boolean useContextinator) 
	throws PharosException {
		this.useContextinator = useContextinator;
		
		String fileName = expName + "-" + RobotIPAssignments.getName() + "-SimpleIndoorMule_" + FileLogger.getUniqueNameExtension() + ".log"; 
		FileLogger expFlogger = new FileLogger(fileName);
		Logger.setFileLogger(expFlogger);
		
		// Set the current and destination locations.  The current location remains constant until
		// the mule reaches the end of the line.
		currLoc = startLoc;
		if (currLoc.equals(srcLoc))
			destLoc = sinkLoc;
		else if (currLoc.equals(sinkLoc))
			destLoc = srcLoc;
		else {
			Logger.logErr("The current location of " + currLoc + " does not match the sink " + sinkLoc + " or source " + srcLoc + " locations.");
			System.exit(1);
		}
		
		Logger.log("Current Location: " + currLoc);
		Logger.log("Destination location: " + destLoc);
			
		PlayerClient client = null;
		
		// Connect to the player server.
		try {
			client = new PlayerClient(playerIP, playerPort);
		} catch (PlayerException e) { Logger.logErr("Could not connect to server."); System.exit(1); }
		Logger.log("Created robot client.");
		
		LineFollower lf = new LineFollower(client);
		
		Logger.logDbg("Changing Player server mode to PUSH...");
		client.requestDataDeliveryMode(playerclient3.structures.PlayerConstants.PLAYER_DATAMODE_PUSH);
		
		Logger.logDbg("Setting Player Client to run in continuous threaded mode...");
		client.runThreaded(-1, -1);	
		
		lf.addListener(this);
		lf.start();
		
		new Thread(this).start();
	}
	
	private void swapCurrDestLocs() {
		Logger.log("Swaping destination locations!");
		if (currLoc.equals(srcLoc))
			currLoc = sinkLoc;
		else
			currLoc = srcLoc;
		
		if (destLoc.equals(srcLoc)) 
			destLoc = sinkLoc;
		else
			destLoc = srcLoc;
	}
	
	/**
	 * This is called whenever the line follower begins to work again.
	 */
	public void lineFollowerWorking() {
		inErrorState = false;
		atWaypoint = false;
	}
	
	@Override
	public void lineFollowerError(LineFollowerError errno) {
		long currTime = System.currentTimeMillis();
		
		if (inErrorState) {
			if (currTime - errorBeginTime > WAYPOINT_STAY_TIME) {
				if (!atWaypoint) {
					swapCurrDestLocs();
					atWaypoint = true;
				}
			}
		} else {
			inErrorState = true;
			errorBeginTime = currTime;
		}
		
	}

//	private void sendContext() {
//		String sendStr = currLoc.longitude() + "," + currLoc.latitude() + "," + destLoc.longitude() + "," + destLoc.latitude();
//		Logger.log("Sending the following context package: \"" + sendStr + "\"");
//		try {
//			Socket s = new Socket("localhost", port);
//			OutputStream os = s.getOutputStream();
//			BufferedOutputStream bos = new BufferedOutputStream(os);
//			bos.write(sendStr.getBytes(Charset.defaultCharset()));
//			bos.flush();
//			os.flush();
//			s.shutdownOutput();
//			s.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//			Logger.logErr("Problem connecting to contextinator: " + e.toString());
//		}
//	}

	@Override
	public void run() {
		while(true) {
//			sendContext();
			pharoslabut.util.ThreadControl.pause(this, 2000);
		}
	}
	
	private static void usage() {
		StringBuffer sb = new StringBuffer("Usage: " + SimpleIndoorMule.class.getName() + " <options>\n\n");
		sb.append("Where <options> include:\n");
		sb.append("\t-expName <experiment name>: The name of the experiment (required)\n");
		sb.append("\t-playerIP <ip address>: The IP address of the player server (default localhost)\n");
		sb.append("\t-playerPort <port number>: The Player Server's port number (default 6665)\n");
		sb.append("\t-contextPort <port number>: The Contexinator's port number (default 6666)\n");
		sb.append("\t-startPos <src|snk>: The starting location of this mule, either source or sink. (default src)\n");
		sb.append("\t-noContext: Disable the connection to the contextinator (useful for debugging)\n");
		sb.append("\t-debug: enable debug mode");
		System.out.println(sb.toString());
	}
	
	public static void main(String[] args) {
		String expName = null;
		String serverIP = "localhost";
		int serverPort = 6665;
		int contextinatorPort = 6666;
		Location startPos = srcLoc;
		boolean useContextinator = true;
		
		try {
			for (int i=0; i < args.length; i++) {
				
				if (args[i].equals("-expName")) {
					expName = args[++i];
				} else if (args[i].equals("-playerIP")) {
					serverIP = args[++i];
				} else if (args[i].equals("-playerPort")) {
					serverPort = Integer.valueOf(args[++i]);
				} else if (args[i].equals("-contextPort")) {
					serverPort = Integer.valueOf(args[++i]);
				} else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				} else if (args[i].equals("-startPos")) {
					String start = args[++i];
					if (start.equals("snk"))
						startPos = sinkLoc;
					else if (start.equals("src"))
						startPos = srcLoc;
					else {
						Logger.logErr("Invalid starting position \"" + start + "\"");
						usage();
						System.exit(1);
					}
				} else if (args[i].equals("-noContext")) {
					useContextinator = false;
				} else if (args[i].equals("-h")) {
					usage();
					System.exit(0);
				} else {
					Logger.logErr("Unknown argument " + args[i]);
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
 
		if (expName == null) {
			Logger.logErr("Experiment name undefined.");
			usage();
			System.exit(1);
		}
		Logger.log("ExpName: " + expName);
		Logger.log("Server IP: " + serverIP);
		Logger.log("Server port: " + serverPort);
		Logger.log("Source location: " + srcLoc);
		Logger.log("Sink location: " + sinkLoc);
		Logger.log("Starting Position: " + startPos);
		
		try {
			new SimpleIndoorMule(expName, serverIP, serverPort, contextinatorPort, startPos, useContextinator);
		} catch (PharosException e) {
			e.printStackTrace();
		}
	}
}
