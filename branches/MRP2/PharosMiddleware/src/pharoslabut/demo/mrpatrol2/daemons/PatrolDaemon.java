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
	 * Whether the patrol daemon's threads should continue to run.
	 */
	protected boolean done = false;
	
	/**
	 * The TCP port on which the local MRPatrol2Server is listening.
	 */
	protected int serverPort;
	
	/**
	 * The multicast address for transmitting and receiving beacons.
	 */
	protected String mCastAddress;
	
	/**
	 * The multicast port for transmitting and receiving beacons.
	 */
	protected int mCastPort;
	
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
	 * @param serverPort The TCP port on which the local MRPatrol2Server is listening.
	 * @param mCastAddress The multicast address for transmitting and receiving beacons
	 * @param mCastPort The multicast port for transmitting and receiving beacons.
	 */
	public PatrolDaemon(ExpConfig expConfig, MotionArbiter.MotionType mobilityPlane, 
			int serverPort,
			String mCastAddress, int mCastPort) 
	{
		this.expConfig = expConfig;
		this.mobilityPlane = mobilityPlane;
		this.serverPort = serverPort;
		this.mCastPort = mCastPort;
		this.mCastAddress = mCastAddress;
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
	
	/**
	 * Stops the patrol daemon.
	 */
	public void stop() {
		done = true;
	}
	
	@Override
	public void run() {
		Logger.logDbg("Thread starting at time " + System.currentTimeMillis() + "...");
		
		while (!done) {
			int numDone = 0;
			
			// Create a vector for holding behaviors that can start.
			Vector<Behavior> startableBehaviors = new Vector<Behavior>();
			
			StringBuffer sb1 = new StringBuffer();
			
			// Go through each behavior find out which can start
			Enumeration<Behavior> e = behaviors.elements();
			while (e.hasMoreElements()) {
				Behavior b = e.nextElement();
				pharoslabut.demo.mrpatrol2.behaviors.Behavior.CanStart cs = b.canStart();
				if (cs.getCanStart()) {
					startableBehaviors.add(b);
				} else {
					sb1.append("\n\t" + b.getName() + " - " + cs.getReason());
				}
				
				if (b.isDone())
					numDone++;
			}
			
			StringBuffer sb2 = new StringBuffer("\nThe following " + startableBehaviors.size() + " behaviors can start:");
			e = startableBehaviors.elements();
			while (e.hasMoreElements()) {
				sb2.append("\n\t" + e.nextElement().getName());
			}
			
			Logger.logDbg("Refresh cycle results:" 
					+ sb2.toString() 
					+ "\nThe following " + (behaviors.size() - startableBehaviors.size()) 
					+ " behaviors cannot start:" + sb1.toString() 
					+ "\nDone: " + numDone + " of " + behaviors.size());
			
			// Start the startable behaviors.
			e = startableBehaviors.elements();
			while (e.hasMoreElements()) {
				e.nextElement().start();
			}
			
			if (numDone == behaviors.size()) {
				Logger.log("All behaviors done!");
				Logger.log("Experiment completed!");
				Logger.log("Program exiting.");
				System.exit(0);
			}
			
			synchronized(this) {
				try {
					this.wait(100);  // daemon thread cycles at 10Hz
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		
		Logger.logDbg("Thread terminating.");
	}
}