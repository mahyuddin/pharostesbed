package pharoslabut.demo.mrpatrol2.daemons;


import pharoslabut.io.Message;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.MotionArbiter;
import pharoslabut.sensors.ProteusOpaqueData;
import pharoslabut.sensors.ProteusOpaqueInterface;
import pharoslabut.sensors.ProteusOpaqueListener;
import pharoslabut.demo.mrpatrol2.config.ExpConfig;
import playerclient3.PlayerClient;
import playerclient3.PlayerException;
import playerclient3.structures.PlayerConstants;

/**
 * The top-level class of all PatrolDaemons used in the multi-robot patrol 2 (MRP2)
 * experiments.
 * 
 * @author Chien-Liang Fok
 */
public abstract class PatrolDaemon implements ProteusOpaqueListener {
	/**
	 * The experiment configuration.
	 */
	protected ExpConfig expConfig;
	
	/**
	 * The mobility plane used.
	 */
	protected MotionArbiter.MotionType mobilityPlane;
	
	/**
	 * The number of waypoints visited.
	 */
	protected volatile int numWaypointsVisited = 0;
	
	/**
	 * Whether the experiment is done.
	 */
	protected boolean done = false;
	
	/**
	 * The minimum beacon period.
	 */
	protected long minPeriod = 1000;
	
	/**
	 * The maximum beacon period.
	 */
	protected long maxPeriod = 1500;
	
	/**
	 * The beacon transmission power.
	 */
	protected short txPower = (short)31;
	
	/**
	 * The player client.
	 */
	protected PlayerClient playerClient;
	
	/**
	 * The constructor.
	 * 
	 * @param expConfig The experiment settings.
	 * @param mobilityPlane The mobility plane used.
	 */
	public PatrolDaemon(ExpConfig expConfig, MotionArbiter.MotionType mobilityPlane) {
		this.expConfig = expConfig;
		this.mobilityPlane = mobilityPlane;
	}
	
	/**
	 * Initializes the player client.
	 * 
	 * @param playerServerIP The IP address of the player server.
	 * @param playerServerPort The TCP port on which the player server listens.
	 */
	protected void initPlayerClient(String playerServerIP, int playerServerPort) {
		// Connect to the player server.
		Logger.log("Creating player client...");
		try {
			playerClient = new PlayerClient(playerServerIP, playerServerPort);
		} catch(PlayerException e) {
			Logger.logErr("Unable to connecting to Player: ");
			Logger.logErr("    [ " + e.toString() + " ]");
			System.exit(1);
		}
		
		
		Logger.log("Subscribing to opaque interface...");
		ProteusOpaqueInterface oi = (ProteusOpaqueInterface)playerClient.requestInterfaceOpaque(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (oi == null) {
			Logger.logErr("Opaque interface is null");
			System.exit(1);
		} else {
			oi.addOpaqueListener(this);
		}
	}
	
	/**
	 * Handles incoming messages.
	 * 
	 * @param msg The incoming message.
	 */
	public void newMessage(Message msg) {
		
	}
	
	@Override
	public void newOpaqueData(ProteusOpaqueData opaqueData) {
		if (opaqueData.getDataCount() > 0) {
			String s = new String(opaqueData.getData());
			Logger.log("MCU Message: " + s);
		}
	}
	
//	/**
//	 * This is called whenever a marker is detected.
//	 */
//	@Override
//	public void markerEvent(int numMarkers, double distance) {
//		Logger.log("MARKER DETECTED: Total = " + numMarkers + ", At marker " + (startingMarkerID + numMarkers) % numMarkersPerRound);
//		synchronized(this) {
//			this.numMarkersSeen = numMarkers;
//			this.numMarkersSeenUpdated = true;
//			this.notifyAll();
//		}
//	}

	/**
	 * Checks whether the experiment is done.  The experiment is done when
	 * the number of markers seen is equal to the number of markers per round
	 * times the number of rounds in the experiment.
	 * 
	 * @return True if the experiment is done.
	 */
	protected boolean checkDone() {
		int numWaypointsVisitedWhenDone = expConfig.getNumWaypoints() * expConfig.getNumRounds();
		Logger.logDbg("Waypoints visited = " + numWaypointsVisited + ", total when done = " + numWaypointsVisitedWhenDone);
		return numWaypointsVisited >= numWaypointsVisitedWhenDone;
	}
}