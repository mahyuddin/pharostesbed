package pharoslabut.demo.autoIntersection.server;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.Vector;

import pharoslabut.demo.autoIntersection.clientDaemons.V2I.ExitingMsg;
import pharoslabut.demo.autoIntersection.clientDaemons.V2I.GrantAccessMsg;
import pharoslabut.demo.autoIntersection.clientDaemons.V2I.RequestAccessMsg;
import pharoslabut.demo.autoIntersection.intersectionSpecs.IntersectionSpecs;
import pharoslabut.io.Message;
import pharoslabut.io.MessageReceiver;
import pharoslabut.io.TCPNetworkInterface;
import pharoslabut.logger.Logger;

/**
 * Implements a parallel intersection management server.
 * This means multiple robots may traverse the intersection at a time without
 * colliding.  For example, two robots traveling in opposite directions along the 
 * same road can proceed simultaneously.
 * 
 * @author Chien-Liang Fok
 */
public class ParallelDaemon extends ServerDaemon implements MessageReceiver {
	
	/**
	 * This daemon's thread.
	 */
//	Thread daemonThread = null;
	
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
	 * The constructor.
	 * 
	 * @param intersectionSpecs The intersection specifications.
	 * @param serverPort The port on which to listen.
	 */
	public ParallelDaemon(IntersectionSpecs intersectionSpecs, int serverPort) {
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
	 * Returns the vehicle state of the specified vehicle.
	 * @param v The specified vehicle.
	 * @return The vehicle state.
	 */
	private VehicleState getVehicle(Vehicle v) {
		Iterator<VehicleState> i = currVehicles.iterator();
		while (i.hasNext()) {
			VehicleState vs = i.next();
			if (vs.getVehicle().equals(v))
				return vs;
		}
		return null;
	}
	
	/**
	 * Determines whether the specified vehicle is in the intersection.
	 * 
	 * @param v The vehicle to evaluate.
	 * @return Whether the vehicle is in the intersection.
	 */
	private boolean inIntersection(Vehicle v) {
		return getVehicle(v) != null;
	}
	
	/**
	 * Determines whether the specified vehicle conflicts with any of the existing
	 * vehicles that are already in the intersection.
	 * 
	 * @param v The vehicle to check
	 * @return true if a conflict exists.
	 */
	private boolean conflictsWithExisting(Vehicle v) {
		Logger.log("Checking if " + v + " intersects with any of the existing vehicles in the intersection.");
		Iterator<VehicleState> i = currVehicles.iterator();
		while (i.hasNext()) {
			VehicleState vs = i.next();
			Vehicle currVehicle = vs.getVehicle();
			if (intersectionSpecs.willIntersect(v.getEntryPointID(), v.getExitPointID(), currVehicle.getEntryPointID(), currVehicle.getExitPointID())) {
				Logger.log("Potential intersection between " + v + " and " + currVehicle);
				return true;
			}
		}
		
		Logger.log("No potential intersections!");
		return false;
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
		
		// Either there is no vehicle in the intersection, this is a duplicate request, or the requesting vehicle will not conflict
		// with any exiting vehicles.
		if (currVehicles.size() == 0 || inIntersection(requestingVehicle) || !conflictsWithExisting(requestingVehicle)) {
			
			long currVehicleTime = System.currentTimeMillis();
			if (!inIntersection(requestingVehicle)) {
				VehicleState vs = new VehicleState(requestingVehicle, msg.getEntryPoint(), msg.getExitPoint(), currVehicleTime);
				currVehicles.add(vs);
			}
			
			Logger.log("Granting vehicle " + vehicleIP + ":" + vehiclePort 
					+ " access to the intersection at time " + currVehicleTime);
			
			
			// Send the grant access message to the vehicle.
			GrantAccessMsg grantMsg = new GrantAccessMsg(vehicleIP, vehiclePort);
			networkInterface.sendMessage(vehicleIP, vehiclePort, grantMsg);
		} else {
			// Denying a vehicle consists of ignoring it.  The vehicle will not proceed
			// through the intersection if it did not receive a GrantAccessMsg.
			Logger.log("Denying vehicle " + msg.getIPString() + ":" + msg.getPort() + ", current vehicles: " + getVehicleList());
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
		
		if (currVehicles.size() > 0) {
			
			VehicleState vs = getVehicle(vehicle);
			if (vs != null) {
				long latency = System.currentTimeMillis() - vs.getTimeOfEntry();
				Logger.log("Vehicle " + vs.getVehicle() + " exited the intersection!  Time in intersection: " + latency);
				currVehicles.remove(vs);
			} else {
				Logger.logErr("Ignoring unexpected exit message from vehicle " 
						+ vehicle + ", expecting " + getVehicleList());
			}
		} else
			Logger.logErr("Ignoring unexpected exit message from vehicle " + vehicle 
					+ " (thought intersectino was empty)");
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
