package pharoslabut.demo.autoIntersection.server;

import java.net.InetAddress;
import java.util.Vector;

import pharoslabut.demo.autoIntersection.clientDaemons.V2I.RequestAccessMsg;
import pharoslabut.demo.autoIntersection.clientDaemons.V2IReservation.GrantAccessReservationMsg;
import pharoslabut.demo.autoIntersection.clientDaemons.V2IReservation.RequestReservationMsg;
import pharoslabut.demo.autoIntersection.intersectionSpecs.IntersectionSpecs;
import pharoslabut.logger.Logger;

/**
 * Implements a reservation-based intersection management server.
 * When a vehicle request access to the intersection, it computes the earliest time the
 * the robot can enter the intersection and reserves this time for the vehicle.
 * It informs the vehicle of this time and expects that the vehicle not enter the intersection
 * until the time is reached.
 * 
 * @author Chien-Liang Fok
 */
public class ReservationDaemon extends ParallelDaemon {
	
	/**
	 * The constructor.
	 * 
	 * @param intersectionSpecs The intersection specifications.
	 * @param serverPort The port on which to listen.
	 */
	public ReservationDaemon(IntersectionSpecs intersectionSpecs, int serverPort) {
		super(intersectionSpecs, serverPort);
	}

	/**
	 * Handles a request access message.  This message is sent by a vehicle to this server
	 * whenever it wants to cross the intersection.
	 * 
	 * @param msg The message.
	 */
	@Override
	protected synchronized void handleRequestAccessMsg(RequestAccessMsg msg) {
		
		// Do a sanity check
		if(msg == null) {
			Logger.logErr("The message is null!");
			return;
		}
		
		if (!(msg instanceof RequestReservationMsg)) {
			Logger.logErr("Receive a request that was not a " + RequestReservationMsg.class.getName());
			System.exit(1);
		}
		
		RequestReservationMsg request = (RequestReservationMsg)msg;
			
		InetAddress vehicleIP = request.getIP();
		int vehiclePort = request.getPort();
		
		Vehicle requestingVehicle = new Vehicle(vehicleIP, vehiclePort, 
				request.getEntryPoint(), request.getExitPoint());
		
		// add a 0.5s buffer to account for differences in the clocks of the server and the vehicles.
		// TODO Get rid of this buffer.
		long currTime = System.currentTimeMillis() - 500; 
		long grantTime = -1;
		
		
		// If this is a duplicate request, re-grant it access at the time that was originally granted.
		if (inIntersection(requestingVehicle)) {
			VehicleState vs = getVehicle(requestingVehicle);
			grantTime = vs.getGrantTime();
			Logger.log("Duplicate request, previous grant time was " + grantTime);
			
			//TODO: Check if grantTime is still valid.  For example, if it is already past, find a new time for the vehicle to enter.
		}
		
		// If this is not a duplicate request and there is no vehicle in the intersection...
		else if (currVehicles.size() == 0) {
			VehicleState vs = new VehicleState(requestingVehicle, request.getEntryPoint(), request.getExitPoint(), currTime);
			currVehicles.add(vs);
			grantTime = currTime;
			Logger.log("No vehicles are in the intersection, granting immediate access.");
		}
		
		// If this is not a duplicate request and vehicles exist in the intersection, determine if there are
		// any vehicles that conflict with the requesting vehicle.  Then compute
		// the earliest time the vehicle can enter the intersection.
		else {
			Vector<VehicleState> conflictingVehicles = getConflictingVehicles(requestingVehicle);
			
			long lastTimeOfConflict = -1;
			for (int i=0; i < conflictingVehicles.size(); i++) {
				VehicleState currVehicle = conflictingVehicles.get(i);
				long currGrantTime = currVehicle.getGrantTime();
				if (lastTimeOfConflict < currGrantTime)
					lastTimeOfConflict = currGrantTime;
			}
			
			if (lastTimeOfConflict == -1) {
				grantTime = currTime;
				Logger.log("Vehicles exist in intersection, but none conflict.  Thus granting immediate access.");
			} else {
				grantTime = lastTimeOfConflict + request.getTimeToCross();
				Logger.log("Vehicles exist in intersection, and there is a conflict.  Time of last conflict is " + lastTimeOfConflict + ", thus granting access at time " + grantTime);
			}
			
			VehicleState vs = new VehicleState(requestingVehicle, request.getEntryPoint(), request.getExitPoint(), currTime);
			currVehicles.add(vs);
			
		}

		assert(grantTime != -1); // for debugging purposes
		
		Logger.log("Granting vehicle " + vehicleIP + ":" + vehiclePort + " access to the intersection at time " + grantTime);

		// Send the grant access message to the vehicle.
		GrantAccessReservationMsg grantMsg = new GrantAccessReservationMsg(vehicleIP, vehiclePort, grantTime);
		networkInterface.sendMessage(vehicleIP, vehiclePort, grantMsg);
	}
}
