package pharoslabut.demo.mrpatrol2.context;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import pharoslabut.demo.mrpatrol2.config.ExpConfig;
import pharoslabut.demo.mrpatrol2.config.RobotExpSettings;
import pharoslabut.logger.Logger;

/**
 * Records what the local robot thinks is the state of its teammates.
 * 
 * @author Chien-Liang Fok
 */
public class WorldModel implements java.io.Serializable {

	private static final long serialVersionUID = 337716832663441881L;
	
	/**
	 * Maintains information about each teammate.
	 * We use a Hashtable rather than a HashMap since it may be accessed
	 * by multiple threads.
	 */
	private Hashtable<String, Teammate> teammates = new Hashtable<String, Teammate>();
	
	/**
	 * The constructor.
	 * 
	 * @param expConfig The experiment configuration.
	 */
	public WorldModel(ExpConfig expConfig) {
		String myName = expConfig.getMySettings().getName();
		
		Iterator<RobotExpSettings> i = expConfig.getRobotItr();
		while (i.hasNext()) {
			RobotExpSettings res = i.next();
			if (!res.getName().equals(myName)) {
				teammates.put(res.getName(), new Teammate(res.getName()));
			}
		}
	}
	
	/**
	 * Obtains a list of active teammates.
	 * 
	 * @param maxTimeDelta The max difference between the current time and the last time the teammate
	 * was update. 
	 * @return A list of active teammates.
	 */
	public Vector<Teammate> getActiveTeammates(long maxTimeDelta) {
		Vector<Teammate> result = new Vector<Teammate>();
		Enumeration<String> names = teammates.keys();
		while (names.hasMoreElements()) {
			Teammate teammate = teammates.get(names.nextElement());
			if (teammate.isActive(maxTimeDelta))
				result.add(teammate);
		}
		return result;
	}
	
	/**
	 * Updates a teammate's record.
	 * 
	 * @param name The name of the teammate.
	 * @param numWaypointsVisited The number of waypoints visited.
	 * @param lastUpdateTime The last update time, as specified by the teammate's system clock.
	 */
	public void updateTeammate(String name, int numWaypointsVisited, long lastUpdateTime) {
		Teammate teammate = teammates.get(name);
		if (teammate != null) {
			teammate.updateNumWaypointsVisited(numWaypointsVisited, lastUpdateTime);
		} else {
			Logger.logErr("Unable to get record of teammate " + name);
			System.exit(1);
		}
	}
	
	/**
	 * @return A string representation of this class.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(getClass().getCanonicalName() + ":");
		Enumeration<String> names = teammates.keys();
		while (names.hasMoreElements()) {
			Teammate teammate = teammates.get(names.nextElement());
			sb.append("\n\t" + teammate);
		}
		return sb.toString();
	}
}
