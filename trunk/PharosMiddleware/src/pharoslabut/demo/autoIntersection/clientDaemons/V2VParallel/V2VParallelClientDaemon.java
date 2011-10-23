package pharoslabut.demo.autoIntersection.clientDaemons.V2VParallel;

import java.net.InetAddress;
import java.net.UnknownHostException;

import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionDetector;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.LineFollower;
import pharoslabut.sensors.Position2DBuffer;

/**
 * Implements a V2V form of intersection management where each robot decides
 * independently whether it is safe to traverse the intersection.  In this daemon,
 * multiple robots may cross the intersection simultaneously.
 * 
 * @author Chien-Liang Fok
 */
public class V2VParallelClientDaemon 
	extends
	pharoslabut.demo.autoIntersection.clientDaemons.V2VSerial.V2VSerialClientDaemon
{

	public V2VParallelClientDaemon(LineFollower lineFollower,
			IntersectionDetector intersectionDetector, Position2DBuffer pos2DBuffer, String entryPointID,
			String exitPointID) 
	{
		super(lineFollower, intersectionDetector, pos2DBuffer, entryPointID, exitPointID);
		
	}
	
	@Override
	protected void createBeacon(String pharosIP) {
		Logger.log("Creating the beacon.");
		try {
			beacon = new V2VParallelBeacon(InetAddress.getByName(pharosIP), mCastPort,
					entryPointID, exitPointID);
		} catch (UnknownHostException e) {
			Logger.logErr("Unable to create the beacon: " + e.toString());
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	@Override
	protected void createNeighborList() {
		 nbrList = new NeighborList(entryPointID, exitPointID);
	}

}
