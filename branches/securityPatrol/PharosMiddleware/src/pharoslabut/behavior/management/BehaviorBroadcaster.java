package pharoslabut.behavior.management;

import java.net.InetAddress;
import java.net.UnknownHostException;

import pharoslabut.behavior.MultiRobotBehaveMsg;
import pharoslabut.exceptions.PharosException;
import pharoslabut.io.TCPMessageSender;
import pharoslabut.logger.FileLogger;

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
	 * The FileLogger for logging debug messages.
	 */
	private FileLogger flogger = null;
	
	/**
	 * The constructor.
	 * 
	 * @param sender The component that sends the message.
	 * @param wm The world model from which the MultiRobotBehaveMsg should be created.
	 */
	public BehaviorBroadcaster(TCPMessageSender sender, WorldModel wm) {
		this(sender, wm, null);
	}
	
	/**
	 * The constructor.
	 * 
	 * @param sender The component that sends the message.
	 * @param wm The world model from which the MultiRobotBehaveMsg should be created. 
	 * @param flogger The file logger for logging debug messages.
	 */
	public BehaviorBroadcaster(TCPMessageSender sender, WorldModel wm, FileLogger flogger) {
		this.sender = sender;
		this.wm = wm;
		this.flogger = flogger;
		new Thread(this).start(); // This has its own thread.
	}
	
	/**
	 * Sets the file logger for saving debug messages.
	 * 
	 * @param flogger The file logger.
	 */
	public void setFileLogger(FileLogger flogger) {
		this.flogger = flogger;
	}
	
	/**
	 * Initializes the broadcast process.
	 * 
	 
	 */
	public synchronized void sendBehaviorToClients() {
		if (!doSend) {
			log("sendBehaviorToClients: Setting doSend to be true.");
			doSend = true;
		} else
			log("sendBehaviorToClients: doSend already true, ignoring request.");
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
//			logErr("doBroadcast: sender not set, aborting.");
//			System.exit(1);
//		}
//		if (wm == null) {
//			logErr("doBroadcast: wm not set, aborting.");
//			System.exit(1);
//		}
		
		log("doBroadcast: Sending behavior to teammates:"
				+ "\n\tBehavior name " + wm.getCurrentBehaviorName() 
				+ "\n\tBehavior ID: "+ wm.getCurrentBehaviorID() 
				+ "\n\tMy index "+ wm.getMyIndex()
				+ "\n\tMy port "+ wm.getMyPort()+"\n");
				
		MultiRobotBehaveMsg	msg = new MultiRobotBehaveMsg(wm.getCurrentBehaviorName(), wm.getCurrentBehaviorID(), wm.getMyIndex());
		log("doBroadcast: Sending message: " + msg);
		
		// For each team member...
		for (int i = 0; i < wm.getTeamSize(); i++) {
			
			// If the team member is not myself ...
			if (i != wm.getMyIndex()) {
				
				// Send the message to the team member...
				String ip = wm.getIp(i);
				int port = wm.getPort(i);
				log("doBroadcast: Attempting to send message to " + ip + ":" + port);
				
				InetAddress address = null;
				try {
					address = InetAddress.getByName(ip);
				} catch (UnknownHostException e) {
					logErr("doBroadcast: UnknownHostException when trying to get InetAddress for " + ip + ", error message: " + e.getMessage());
					e.printStackTrace();
					continue;
				}
				
				if (address != null) {
					try {
						log("doBroadcast: BEFORE Send: Sending " + wm.getCurrentBehaviorName() + " to Client " + i + " at " + address + ":" + port + "\n");		
						sender.sendMessage(address, port, msg);
						log("doBroadcast: AFTER Send: Sent " + wm.getCurrentBehaviorName() + " to Client " + i);
					} catch (PharosException e) {
						logErr("doBroadcast: PharosException when trying to send message to " + address + ":" + port + ", error message: " + e.getMessage());
						e.printStackTrace();
					}
				} else {
					logErr("doBroadcast: Unable to send message because address was null!");
				}
			}
		}
	}
	
	@Override
	public void run() {
		
		log("run: Starting broadcast loop...");
		
		while (!done) {
			if (doSend) {
				log("run: doSend true, broadcasting to teammates!");
				doSend = false;
				doBroadcast();  
			} else
				log("run: doSend false, not broadcasting during this cycle");
			
			// Pause for the specified period.
			synchronized(this) {
				try {
					log("run: pausing for " + MIN_BROADCAST_PERIOD + "ms.");
					wait(MIN_BROADCAST_PERIOD);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		log("run: Thread terminates.");
	}
		

	private void logErr(String msg) {
		String result = "BehaviorBroadcaster: ERROR: " + msg;

		System.err.println(result);

		// always log text to file if a FileLogger is present
		if (flogger != null)
			flogger.log(result);
	}

	private void log(String msg) {
		String result = "BehaviorBroadcaster: " + msg;

		// only print log text to string if in debug mode
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);

		// always log text to file if a FileLogger is present
		if (flogger != null)
			flogger.log(result);
	}

}
