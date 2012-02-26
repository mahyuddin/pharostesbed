package pharoslabut.demo.mrpatrol2.daemons;


import java.util.Enumeration;
import java.util.Vector;

import pharoslabut.io.Message;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.MotionArbiter;
import pharoslabut.sensors.Position2DBuffer;
import pharoslabut.sensors.Position2DListener;
import pharoslabut.sensors.ProteusOpaqueData;
import pharoslabut.sensors.ProteusOpaqueInterface;
import pharoslabut.sensors.ProteusOpaqueListener;
import pharoslabut.demo.mrpatrol2.config.ExpConfig;
import pharoslabut.demo.mrpatrol2.behaviors.Behavior;
import playerclient3.PlayerClient;
import playerclient3.PlayerException;
import playerclient3.Position2DInterface;
import playerclient3.structures.PlayerConstants;
import playerclient3.structures.position2d.PlayerPosition2dData;

/**
 * The top-level class of all PatrolDaemons used in the multi-robot patrol 2 (MRP2)
 * experiments.
 * 
 * @author Chien-Liang Fok
 */
public abstract class PatrolDaemon implements ProteusOpaqueListener, Position2DListener, Runnable {
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
//	protected volatile int numWaypointsVisited = 0;
	
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
	 * Provides access to the mobility plane.
	 */
	protected Position2DInterface motors;
	
	/**
	 * The behaviors of the patrol daemon.
	 */
	private Vector<Behavior> behaviors = new Vector<Behavior>();
	
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
		
		Logger.logDbg("Subscribing to motor interface...");
		motors = playerClient.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
		if (motors == null) {
			Logger.logErr("Motors is null");
			System.exit(1);
		}
		
		Logger.logDbg("Listening for Position2D events (odometer data)...");
		Position2DBuffer p2dBuff = new Position2DBuffer(motors);
		p2dBuff.addPos2DListener(this);
		p2dBuff.start();
		
	}
	
	@Override
	public void newPlayerPosition2dData(PlayerPosition2dData data) {
		Logger.log(data.toString());
	}
	
	/**
	 * Handles incoming messages.
	 * 
	 * @param msg The incoming message.
	 */
	public abstract void newMessage(Message msg);
	
	@Override
	public void newOpaqueData(ProteusOpaqueData opaqueData) {
		if (opaqueData.getDataCount() > 0) {
			String s = new String(opaqueData.getData());
			Logger.log("MCU Message: " + s);
		}
	}

//	/**
//	 * Checks whether the experiment is done.  The experiment is done when
//	 * the number of markers seen is equal to the number of markers per round
//	 * times the number of rounds in the experiment.
//	 * 
//	 * @return True if the experiment is done.
//	 */
//	protected boolean checkDone() {
//		int numWaypointsVisitedWhenDone = expConfig.getNumWaypoints() * expConfig.getNumRounds();
//		Logger.logDbg("Waypoints visited = " + numWaypointsVisited + ", total when done = " + numWaypointsVisitedWhenDone);
//		return numWaypointsVisited >= numWaypointsVisitedWhenDone;
//	}
	
	/**
	 * Adds a behavior to this daemon.
	 * 
	 * @param b The behavior to add.
	 */
	protected void addBehavior(Behavior b) {
		behaviors.add(b);
	}
	
	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		Logger.logDbg("Thread starting at time " + startTime + "...");
		
		while (true) {
			int numDone = 0;
			
			// Go through each behavior and start those that can start
			Enumeration<Behavior> e = behaviors.elements();
			while (e.hasMoreElements()) {
				Behavior b = e.nextElement();
				if (b.canStart()) {
					b.start();
				} else if (b.isDone())
					numDone++;
			}
			
			if (numDone == behaviors.size()) {
				Logger.log("All behaviors done!");
				Logger.log("Experiment completed!");
				Logger.log("Program exiting.");
				System.exit(0);
			}
			
			synchronized(this) {
				try {
					this.wait(100);  // have daemon thread cycle at 10Hz
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
}