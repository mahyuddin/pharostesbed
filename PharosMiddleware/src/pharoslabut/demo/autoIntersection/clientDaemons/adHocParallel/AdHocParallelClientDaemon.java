package pharoslabut.demo.autoIntersection.clientDaemons.adHocParallel;

import java.net.InetAddress;
import java.net.UnknownHostException;

import pharoslabut.demo.autoIntersection.intersectionDetector.IntersectionDetector;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.LineFollower;

/**
 * Implements an ad hoc form of intersection management where each robot decides
 * independently whether it is safe to traverse the intersection.  In this daemon,
 * multiple robots may cross the intersection simultaneously.
 * 
 * @author Chien-Liang Fok
 */
public class AdHocParallelClientDaemon 
	extends
	pharoslabut.demo.autoIntersection.clientDaemons.adHocSerial.AdHocSerialClientDaemon
{

	public AdHocParallelClientDaemon(LineFollower lineFollower,
			IntersectionDetector intersectionDetector, String entryPointID,
			String exitPointID) 
	{
		super(lineFollower, intersectionDetector, entryPointID, exitPointID);
		
	}
	
	@Override
	protected void createBeacon(String pharosIP) {
		Logger.log("Creating the beacon.");
		try {
			beacon = new AdHocParallelBeacon(InetAddress.getByName(pharosIP), mCastPort,
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
