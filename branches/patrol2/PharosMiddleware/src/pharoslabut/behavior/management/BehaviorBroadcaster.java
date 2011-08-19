package pharoslabut.behavior.management;

import java.net.InetAddress;
import java.net.UnknownHostException;

import pharoslabut.behavior.MultiRobotBehaveMsg;
import pharoslabut.exceptions.PharosException;
import pharoslabut.io.TCPMessageSender;
import pharoslabut.logger.Logger;

/**
 * Sends MultiRobotBehaveMsg messages to each team member.  Throttles the rate at 
 * which these broadcasts may occur to prevent flooding the network.  To keep
 * the broadcasts going, you must continue to call sendBehaviorToClients(...).
 * You may call this method as frequently as you want, but the broadcasts will
 * only occur every MIN_BROADCAST_PERIOD milliseconds.
 * 
 * @see pharoslabut.behavior.MultiRobotBehaveMsg
 * @author Chien-Liang Fok
 * @author Noa Agmon
 */
public class BehaviorBroadcaster implements Runnable {
	
	/**
	 * The minimum period between broadcasting MultiRobotBehaveMsg to each team member.
	 * Its units is milliseconds.
	 */
	public static final long MIN_BROADCAST_PERIOD = 1000;
	
	/**
	 * The world model of the sender.  This is needed to determine the status of the sender
	 * and the IPs of the team members.
	 */
	private WorldModel wm;
	
	/**
	 * This actually sends the message.
	 */
	private TCPMessageSender sender;
	
	/**
	 * Whether to broadcast the MultiRobotBehaveMsg to all team members.
	 */
	private boolean doSend = false;
	
	/**
	 * Whether this broadcaster should continue to run.
	 */
	private boolean done = false;
	
	/**
	 * The constructor.
	 * 
	 * @param sender The component that sends the message.
	 * @param wm The world model from which the MultiRobotBehaveMsg should be created. 
	 */
	public BehaviorBroadcaster(TCPMessageSender sender, WorldModel wm) {
		this.sender = sender;
		this.wm = wm;
		new Thread(this).start(); // This has its own thread.
	}
	
	/**
	 * Initializes the broadcast process.
	 * 
	 
	 */
	public synchronized void sendBehaviorToClients() {
		if (!doSend) {
			Logger.log("Setting doSend to be true.");
			doSend = true;
		} else
			Logger.log("doSend already true, ignoring request.");
	}
	
	/**
	 * Terminates this broadcaster.  Cannot be undone.
	 */
	public void stop() {
		this.done = true;
	}
	
	/**
	 * Performs the actual broadcast operation.
	 */
	private void doBroadcast() {
		// Check for fatal error conditions.
//		if (sender == null) {
//			logErr("sender not set, aborting.");
//			System.exit(1);
//		}
//		if (wm == null) {
//			logErr("wm not set, aborting.");
//			System.exit(1);
//		}
		
		Logger.log("Sending behavior to teammates:"
				+ "\n\tBehavior name " + wm.getCurrentBehaviorName() 
				+ "\n\tBehavior ID: "+ wm.getCurrentBehaviorID() 
				+ "\n\tMy index "+ wm.getMyIndex()
				+ "\n\tMy port "+ wm.getMyPort()+"\n");
				
		MultiRobotBehaveMsg	msg = new MultiRobotBehaveMsg(wm.getCurrentBehaviorName(), wm.getCurrentBehaviorID(), wm.getMyIndex());
		Logger.log("Sending message: " + msg);
		
		// For each team member...
		for (int i = 0; i < wm.getTeamSize(); i++) {
			
			// If the team member is not myself ...
			if (i != wm.getMyIndex()) {
				
				// Send the message to the team member...
				String ip = wm.getIp(i);
				int port = wm.getPort(i);
				Logger.log("Attempting to send message to " + ip + ":" + port);
				
				InetAddress address = null;
				try {
					address = InetAddress.getByName(ip);
				} catch (UnknownHostException e) {
					Logger.logErr("UnknownHostException when trying to get InetAddress for " + ip + ", error message: " + e.getMessage());
					e.printStackTrace();
					continue;
				}
				
				if (address != null) {
					try {
						Logger.log("BEFORE Send: Sending " + wm.getCurrentBehaviorName() + " to Client " + i + " at " + address + ":" + port + "\n");		
						sender.sendMessage(address, port, msg);
						Logger.log("AFTER Send: Sent " + wm.getCurrentBehaviorName() + " to Client " + i);
					} catch (PharosException e) {
						Logger.logErr("PharosException when trying to send message to " + address + ":" + port + ", error message: " + e.getMessage());
						e.printStackTrace();
					}
				} else {
					Logger.logErr("Unable to send message because address was null!");
				}
			}
		}
	}
	
	/**
	 * This implements the main loop of this broadcaster.
	 */
	public void run() {
		
		Logger.log("Starting broadcast loop...");
		
		while (!done) {
			if (doSend) {
				Logger.log("doSend true, broadcasting to teammates!");
				doSend = false;
				doBroadcast();  
			} else
				Logger.log("doSend false, not broadcasting during this cycle");
			
			// Pause for the specified period.
			synchronized(this) {
				try {
					Logger.log("pausing for " + MIN_BROADCAST_PERIOD + "ms.");
					wait(MIN_BROADCAST_PERIOD);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		Logger.log("Thread terminates.");
	}
}
