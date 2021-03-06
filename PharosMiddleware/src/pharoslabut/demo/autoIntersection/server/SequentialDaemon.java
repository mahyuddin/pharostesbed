package pharoslabut.demo.autoIntersection.server;

import java.net.InetAddress;

import pharoslabut.demo.autoIntersection.clientDaemons.V2I.ExitingMsg;
import pharoslabut.demo.autoIntersection.clientDaemons.V2I.GrantAccessMsg;
import pharoslabut.demo.autoIntersection.clientDaemons.V2I.RequestAccessMsg;
import pharoslabut.demo.autoIntersection.intersectionSpecs.IntersectionSpecs;
import pharoslabut.io.Message;
import pharoslabut.io.MessageReceiver;
import pharoslabut.io.TCPNetworkInterface;
import pharoslabut.logger.Logger;

/**
 * Implements a sequential intersection management server.
 * It only allows one robot to be in the intersection at a time.
 * 
 * @author Chien-Liang Fok
 */
public class SequentialDaemon extends ServerDaemon implements MessageReceiver {

//	/**
//	 * The cycle time of this daemon in milliseconds.
//	 */
//	public static final long CYCLE_TIME = 100; // 10Hz
	
	/**
	 * This daemon's thread.
	 */
//	Thread daemonThread = null;
	
	/**
	 * The network interface.
	 */
	TCPNetworkInterface networkInterface;
	
	/**
	 * Whether this daemon is running.
	 */
	boolean running = false;
	
	/**
	 * A reference to the current vehicle in the intersection.
	 */
	Vehicle currVehicle = null;
	
	/**
	 * The time when the current vehicle was granted access to the intersection.
	 */
	long currVehicleTime;
	
	/**
	 * The constructor.
	 * 
	 * @param intersectionSpecs The intersection specifications.
	 * @param serverPort The port on which to listen.
	 */
	public SequentialDaemon(IntersectionSpecs intersectionSpecs, int serverPort) {
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
			
//			daemonThread = new Thread(this);
//			daemonThread.start();
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
		
		// Either there is no vehicle in the intersection, or this is a duplicate request.
		if (currVehicle == null || currVehicle.equals(requestingVehicle)) {
			
			currVehicle = requestingVehicle;
			currVehicleTime = System.currentTimeMillis();
			
			Logger.log("Granting vehicle " + vehicleIP + ":" + vehiclePort 
					+ " access to the intersection at time " + currVehicleTime);
			
			// Send the grant access message to the vehicle.
			GrantAccessMsg grantMsg = new GrantAccessMsg(vehicleIP, vehiclePort);
			networkInterface.sendMessage(vehicleIP, vehiclePort, grantMsg);
		} else {
			
			// Denying a vehicle consists of ignoring it.  The vehicle will not proceed
			// through the intersection if it did not receive a GrantAccessMsg.
			Logger.log("Denying vehicle " + msg.getIPString() + ":" + msg.getPort() + ", current vehicle = " + currVehicle.getIP() + ":" + currVehicle.getPort());
		}
	}


	/**
	 * Handles an exiting message.  This message is sent by a robot to this server
	 * whenever it exits the intersection.
	 * 
	 * @param msg The message.
	 */
	private synchronized void handleExitingMsg(ExitingMsg msg) {
		
		// Do a sanity check
		if(msg == null) {
			Logger.logErr("The message is null!");
			return;
		}
		
		Vehicle vehicle = new Vehicle(msg.getIP(), msg.getPort());
		
		if (currVehicle != null) {
			
			if (currVehicle.equals(vehicle)) {
				long latency = System.currentTimeMillis() - currVehicleTime;
				Logger.log("Vehicle " + currVehicle + " exited the intersection!  Time in intersection: " + latency);
				currVehicle = null;
			} else {
				Logger.logErr("Ignoring unexpected exit message from vehicle " 
						+ vehicle + ", expecting " + currVehicle);
			}
		} else
			Logger.logErr("Ignoring unexpected exit message from vehicle " + vehicle 
					+ " (thought intersectino was empty)");
	}
	
//	private void handleAutoIntDebugMsg(AutoIntDebugMsg msg) {
//		Logger.log("Received AutoIntDebugMsg: " + msg);
//		display.updateText(msg.getIE().getType().toString());
//	}
	
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
//    	else if (msg instanceof AutoIntDebugMsg) {
//    		handleAutoIntDebugMsg((AutoIntDebugMsg)msg);
//    	}
    	else
    		Logger.log("RECEIVER: Unknown message " + msg);
	}
	
//	public void run() {
//		while(running) {
//			
//			// Do something
//			
//			synchronized(this) {
//				try {
//					wait(CYCLE_TIME);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	}
	
	public String toString() {
		return getClass().getName();
	}
}
