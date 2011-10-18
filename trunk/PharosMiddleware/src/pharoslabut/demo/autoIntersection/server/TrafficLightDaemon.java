package pharoslabut.demo.autoIntersection.server;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import pharoslabut.demo.autoIntersection.clientDaemons.centralized.ExitingMsg;
import pharoslabut.demo.autoIntersection.clientDaemons.centralized.GrantAccessMsg;
import pharoslabut.demo.autoIntersection.clientDaemons.centralized.RequestAccessMsg;
import pharoslabut.demo.autoIntersection.intersectionSpecs.IntersectionSpecs;
import pharoslabut.demo.autoIntersection.intersectionSpecs.Road;
import pharoslabut.io.Message;
import pharoslabut.io.MessageReceiver;
import pharoslabut.io.TCPNetworkInterface;
import pharoslabut.logger.Logger;

/**
 * Implements a traffic-light-mimicking intersection management server.
 * This means the roads of the intersection are sequentially allowed passage
 * at pre-set intervals.
 * 
 * @author Chien-Liang Fok
 */
public class TrafficLightDaemon extends ServerDaemon implements MessageReceiver {

	/**
	 * The period between changing the road that is enabled in the intersection.
	 */
	//public static final long ROTATION_INTERVAL = 60000; // 1 minute rotation
	public static final long ROTATION_INTERVAL = 30000; // 30 second rotation
	
	/**
	 * The period within the rotation interval when the transition should begin
	 * to occur.  This is analogous to the "yellow light" in real traffic light 
	 * systems.
	 */
	//public static final long TRANSISTION_PERIOD = 15000; // 15 second transition period
	public static final long TRANSISTION_PERIOD = 10000; // 10 second transition period
	
	/**
	 * The network interface.
	 */
	private TCPNetworkInterface networkInterface;
	
	/**
	 * Whether this daemon is running.
	 */
	private boolean running = false;
	
	/**
	 * The current vehicles that are in the intersection.
	 */
	private Vector<VehicleState> currVehicles = new Vector<VehicleState>();
	
	/**
	 * The index of the road that is currently enabled.
	 */
	private int currEnabledRoad = 0;
	
	/**
	 * The timer used to trigger changes in the traffic light.
	 */
	private Timer timer;
	
	/**
	 * The time when the next rotation should occur.
	 */
	private long nextRotationTime;
	
	/**
	 * The constructor.
	 * 
	 * @param intersectionSpecs The intersection specifications.
	 * @param serverPort The port on which to listen.
	 */
	public TrafficLightDaemon(IntersectionSpecs intersectionSpecs, int serverPort) {
		super(intersectionSpecs, serverPort);
	}
	
	/**
	 * Starts this daemon running.
	 */
	@Override
	public synchronized void start() {
		if (!running) {
			Logger.log("Starting on port " + serverPort + "...");
			
			// Create the network interface and register this object as a listener for
			// incoming messages.
			networkInterface = new TCPNetworkInterface(serverPort); //UDPNetworkInterface(serverPort);
			networkInterface.registerMsgListener(this);
			
			// Randomly select which road to enable first
			currEnabledRoad = (int)(Math.random() * intersectionSpecs.numRoads());
			Logger.log("Enabling road " + intersectionSpecs.getRoad(currEnabledRoad));
			
			// Randomly determine at what point during the cycle time we are at.
			long timeTillSwitch = (long)(Math.random() * (ROTATION_INTERVAL - TRANSISTION_PERIOD)) 
				+ TRANSISTION_PERIOD;
			
			nextRotationTime = System.currentTimeMillis() + timeTillSwitch;
			Logger.log("Next rotation time = " + nextRotationTime 
					+ " (in " + (nextRotationTime - System.currentTimeMillis()) / 1000.0 + "s)");
			
			timer = new Timer("TrafficLightTimer");
			timer.schedule(new RotationTimerTask(), timeTillSwitch, ROTATION_INTERVAL);
			running = true;
		} else {
			Logger.logErr("Already started.");
		}
	}
	
	/**
	 * Stops this daemon.
	 */
	@Override
	public synchronized void stop() {
		running = false;
	}
	
	/**
	 * Defines the task that is executed when the traffic light timer fires.
	 * 
	 * @author Chien-Liang Fok
	 */
	private class RotationTimerTask extends TimerTask {
		
		public RotationTimerTask() {
			
		}
		
		public void run() {
			currEnabledRoad = (currEnabledRoad + 1) % intersectionSpecs.numRoads();
			Logger.log("ROTATING INTERSECTION: Enabling road " + intersectionSpecs.getRoad(currEnabledRoad));
			
			nextRotationTime = System.currentTimeMillis() + ROTATION_INTERVAL;
			Logger.log("Next rotation time = " + nextRotationTime 
					+ " (in " + (nextRotationTime - System.currentTimeMillis()) / 1000.0 + "s)");
		}
	}
	
	/**
	 * 
	 * @return A string containing a list of vehicles.
	 */
	private String getVehicleList() {
		StringBuffer sb = new StringBuffer();
		Iterator<VehicleState> i = currVehicles.iterator();
		while (i.hasNext()) {
			VehicleState vs = i.next();
			sb.append(vs.toString());
			if (i.hasNext())
				sb.append(", ");
		}
		return sb.toString();
	}
	
	/**
	 * Handles a request access message.  This message is sent by a robot to this server
	 * whenever it wants to cross the intersection.
	 * 
	 * @param msg The message.
	 */
	private synchronized void handleRequestAccessMsg(RequestAccessMsg msg) {
		
		// Do a sanity check
		if(msg == null) {
			Logger.logErr("The message is null!");
			return;
		}
		
		InetAddress vehicleIP = msg.getIP();
		int vehiclePort = msg.getPort();
		
		Vehicle requestingVehicle = new Vehicle(vehicleIP, vehiclePort, 
				msg.getEntryPoint(), msg.getExitPoint());
		
		// If we are within the TRANSITION_PERIOD of the time of next rotation, deny the request.
		long currTime = System.currentTimeMillis();
		if (nextRotationTime - currTime < TRANSISTION_PERIOD) {
			Logger.log("Denying vehicle " + msg.getIPString() + ":" + msg.getPort() 
					+ " because in transition period.  CurrTime = " + currTime 
					+ ", NextRotationTime = " + nextRotationTime + ", diff = " + (nextRotationTime - currTime));
		} else {
			
			// Check if the vehicle is on the road that is enabled
			Road currRoad = intersectionSpecs.getRoad(currEnabledRoad);
			if (currRoad.contains(requestingVehicle)) {
				Logger.log("Granting vehicle " + vehicleIP + ":" + vehiclePort 
						+ " access to the intersection at time " + currTime);
				// Send the grant access message to the vehicle.
				GrantAccessMsg grantMsg = new GrantAccessMsg(vehicleIP, vehiclePort);
				networkInterface.sendMessage(vehicleIP, vehiclePort, grantMsg);
			} else {
				Logger.log("Denying vehicle " + msg.getIPString() + ":" + msg.getPort() 
						+ " because it is not travelling on the road that is currently enabled.");
			}
			
		}
	}


	/**
	 * Handles an exiting message.  This message is sent by a robot to this server
	 * whenever it exits the intersection.  It is not used in the traffic light 
	 * management scheme.
	 * 
	 * @param msg The message.
	 */
	private synchronized void handleExitingMsg(ExitingMsg msg) {
		
		// Do a sanity check
		if(msg == null) {
			Logger.logErr("The message is null!");
			return;
		}
		
		Logger.log("Vehicle " + msg.getIP() + ":" + msg.getPort() + " exited the intersection.");
	}
	
    /**
     * Handles incoming messages.
     */
	@Override
	public void newMessage(Message msg) {
		Logger.log("RECEIVED_MESSAGE: " + msg);
		if (msg instanceof RequestAccessMsg)
    		handleRequestAccessMsg((RequestAccessMsg)msg);
    	else if (msg instanceof ExitingMsg)
    		handleExitingMsg((ExitingMsg)msg);
    	else
    		Logger.log("RECEIVER: Unknown message " + msg);
	}
	
	public String toString() {
		return getClass().getName() + ", vehicles = " + getVehicleList();
	}
}
