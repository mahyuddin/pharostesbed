package pharoslabut.demo.mrpatrol2.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import pharoslabut.RobotIPAssignments;
import pharoslabut.demo.mrpatrol2.Waypoint;
import pharoslabut.exceptions.PharosException;
import pharoslabut.logger.Logger;
import pharoslabut.navigate.Location;

/**
 * Contains the configuration of a multi-robot patrol 2 (MRP2) experiment.
 * 
 * @author Chien-Liang Fok
 */
public class ExpConfig implements java.io.Serializable {
	
	private static final long serialVersionUID = -6844595389153177043L;

	/**
	 * The name of the experiment.
	 */
	private String expName = "Exp";

	/**
	 * The experiment type.
	 */
	private ExpType expType = ExpType.OUTDOOR;
	
	/**
	 * The type of coordination.
	 */
	private CoordinationType coordType = CoordinationType.NONE;
	
	/**
	 * The strength of coordination.
	 */
	private CoordinationStrength coordStrength = CoordinationStrength.LOOSE;
	
	/**
	 * The type of information exchanged while coordinating.
	 */
	private CoordinationInfo coordInfo = CoordinationInfo.LOCAL;
	
	/**
	 * The type of network to use.
	 */
	private NetworkType networkType = NetworkType.P2P;
	
	/**
	 * The number of times to patrol the route.
	 */
	private int numRounds = -1;
	
	/**
	 * The number of seconds to delay between receiving the start 
	 * experiment message and actually leaving the first waypoint.
	 */
	private long startDelay = 0;
	
	/**
	 * The IP address of the central coordinator.  This is only used in
	 * centralized experiments.
	 */
	private InetAddress coordinatorIP;
	
	/**
	 * The port of the central coordinator.  This is only used in centralized
	 * experiments.
	 */
	private int coordinatorPort;
	
	/**
	 * Details of each robot in the team.
	 */
	private Vector<RobotExpSettings> team = new Vector<RobotExpSettings>();
	
	/**
	 * Details of each waypoint in the patrol path.
	 */
	private Vector<Waypoint> waypoints = new Vector<Waypoint>();
	
	/**
	 * The ahead time used in fixed anticipated experiments.
	 */
	private long aheadTime;
	
	/**
	 * The constructor.
	 * 
	 * @param fileName The name of the file containing the experiment specifications.
	 */
	public ExpConfig(String fileName) {
		readFile(fileName);
	}
	
	/**
	 * Reads in the file containing the experiment specifications.
	 * 
	 * @param fileName The name of the file containing the experiment specifications.
	 */
	private void readFile(String fileName) {
		BufferedReader input = null;
		try {
			input =  new BufferedReader(new FileReader(fileName));
		} catch (IOException ex){
			ex.printStackTrace();
			System.err.println("Unable to open " + fileName);
			System.exit(1);
		}
		
		try {
			String line = null;
			int lineno = 1;
			while (( line = input.readLine()) != null) {
				if (!line.equals("") && !line.startsWith("//")) {
					if (line.contains("EXP_NAME")) {
						String[] elem = line.split("[\\s]+");
						try {
							expName = elem[1];
//							Logger.log("Experiment name: " + expName);
						} catch(Exception e) {
							e.printStackTrace();
							Logger.logErr("Syntax error on line " + lineno + " of experiment script " + fileName + ":\n" + line);
							System.exit(1);
						}
					} else if (line.contains("EXP_TYPE")) {
						String[] elem = line.split("[\\s]+");
						try {
							String expTypeStr = elem[1].toUpperCase();
							if (expTypeStr.equals("INDOOR"))
								expType = ExpType.INDOOR;
							else if (expTypeStr.equals("OUTDOOR"))
								expType = ExpType.OUTDOOR;
							else {
								System.err.println("Unknown experiment type " + elem[1]);
								System.exit(1);	
							}
//							Logger.log("Experiment type: " + expType);
						} catch(Exception e) {
							e.printStackTrace();
							Logger.logErr("Syntax error on line " + lineno + " of experiment script " + fileName + ":\n" + line);
							System.exit(1);
						}
					} else if (line.contains("COORDINATION_TYPE")) {
						String[] elem = line.split("[\\s]+");
						try {
							String coordTypeStr = elem[1].toUpperCase();
							if (coordTypeStr.equals("NONE"))
								coordType = CoordinationType.NONE;
							else if (coordTypeStr.equals("PASSIVE"))
								coordType = CoordinationType.PASSIVE;
							else if (coordTypeStr.equals("ANTICIPATED_FIXED"))
								coordType = CoordinationType.ANTICIPATED_FIXED;
							else if (coordTypeStr.equals("ANTICIPATED_VARIABLE"))
								coordType = CoordinationType.ANTICIPATED_VARIABLE;
							else {
								Logger.logErr("Unknown coordination type " + elem[1]);
								System.exit(1);	
							}
//							Logger.log("Coordination type: " + coordType);
						} catch(Exception e) {
							e.printStackTrace();
							Logger.logErr("Syntax error on line " + lineno + " of experiment script " + fileName + ":\n" + line);
							System.exit(1);
						}
					} else if (line.contains("COORDINATION_STRENGTH")) {
						String[] elem = line.split("[\\s]+");
						try {
							String coordStrengthStr = elem[1].toUpperCase();
							if (coordStrengthStr.equals("LOOSE"))
								coordStrength = CoordinationStrength.LOOSE;
							else if (coordStrengthStr.equals("TIGHT"))
								coordStrength = CoordinationStrength.TIGHT;
							else {
								Logger.logErr("Unknown coordination strength " + elem[1]);
								System.exit(1);	
							}
//							Logger.log("Coordination strength: " + coordStrength);
						} catch(Exception e) {
							e.printStackTrace();
							Logger.logErr("Syntax error on line " + lineno + " of experiment script " + fileName + ":\n" + line);
							System.exit(1);
						}
					} else if (line.contains("COORDINATION_INFO")) {
						String[] elem = line.split("[\\s]+");
						try {
							String coordInfoStr = elem[1].toUpperCase();
							if (coordInfoStr.equals("LOCAL"))
								coordInfo = CoordinationInfo.LOCAL;
							else if (coordInfoStr.equals("GLOBAL"))
								coordInfo = CoordinationInfo.GLOBAL;
							else {
								Logger.logErr("Unknown coordination info " + elem[1]);
								System.exit(1);	
							}
//							Logger.log("Coordination info: " + coordInfo);
						} catch(Exception e) {
							e.printStackTrace();
							System.err.println("Syntax error on line " + lineno + " of experiment script " + fileName + ":\n" + line);
							System.exit(1);
						}
					} else if (line.contains("NETWORK_TYPE")) {
						String[] elem = line.split("[\\s]+");
						try {
							String coordInfoStr = elem[1].toUpperCase();
							if (coordInfoStr.equals("CENTRALIZED"))
								networkType = NetworkType.CENTRALIZED;
							else if (coordInfoStr.equals("P2P"))
								networkType = NetworkType.P2P;
							else {
								Logger.logErr("Unknown network type " + elem[1]);
								System.exit(1);	
							}
//							Logger.log("Network type: " + networkType);
						} catch(Exception e) {
							e.printStackTrace();
							System.err.println("Syntax error on line " + lineno + " of experiment script " + fileName + ":\n" + line);
							System.exit(1);
						}
					} else if (line.contains("START_DELAY")) {
						try {
							String[] elem = line.split("[\\s]+");
							startDelay = Long.valueOf(elem[1]);
						} catch(Exception e) {
							e.printStackTrace();
							Logger.logErr("Syntax error on line " + lineno + " of experiment script " + fileName + ":\n" + line);
							System.exit(1);
						}
//						Logger.log("Start delay: " + startDelay);
					} else if (line.contains("CENTRAL_COORDINATOR")) {
						String[] elem = line.split("[\\s]+");
						try {
							coordinatorIP = InetAddress.getByName(elem[1]);
							coordinatorPort = Integer.valueOf(elem[2]);
//							Logger.log("Coordinator: IP: " + coordinatorIP + ", port: " + coordinatorPort);
						} catch(Exception e) {
							e.printStackTrace();
							Logger.logErr("Syntax error on line " + lineno + " of experiment script " + fileName + ":\n" + line);
							System.exit(1);
						}
					} else if (line.contains("NUM_ROUNDS")) {
						try {
							String[] elem = line.split("[\\s]+");
							numRounds = Integer.valueOf(elem[1]);
						} catch(Exception e) {
							e.printStackTrace();
							Logger.logErr("Syntax error on line " + lineno + " of experiment script " + fileName + ":\n" + line);
							System.exit(1);
						}
					} else if (line.contains("AHEAD_TIME")) {
						try {
							String[] elem = line.split("[\\s]+");
							aheadTime = Long.valueOf(elem[1]);
						} catch(Exception e) {
							e.printStackTrace();
							Logger.logErr("Syntax error on line " + lineno + " of experiment script " + fileName + ":\n" + line);
							System.exit(1);
						}
					} else if (line.contains("ROBOT")) {
						try {
							String[] elem = line.split("[\\s]+");
							String name = elem[1];
							InetAddress ip = InetAddress.getByName(elem[2]);
							int port = Integer.valueOf(elem[3]);
							String firstWaypoint = elem[4];
							RobotExpSettings rs = new RobotExpSettings(name, ip, port, firstWaypoint);
							
							team.add(rs);
						} catch(Exception e) {
							e.printStackTrace();
							Logger.logErr("Syntax error on line " + lineno + " of experiment script " + fileName + ":\n" + line);
							System.exit(1);
						}
					} else if (line.contains("WAYPOINT")) {
						try {
							String[] elem = line.split("[\\s]+");
							String name = elem[1];
							double latitude = Double.valueOf(elem[2]);
							double longitude = Double.valueOf(elem[3]);
							double speed = Double.valueOf(elem[4]);
							Location loc = new Location(latitude, longitude);
							
							waypoints.add(new Waypoint(name, loc, speed));
						} catch(Exception e) {
							e.printStackTrace();
							Logger.logErr("Syntax error on line " + lineno + " of experiment script " + fileName + ":\n" + line);
							System.exit(1);
						}
					} else {
						Logger.logWarn("Ignoring line \"" + line + "\"");
					}
				}
				lineno++;
			}
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		// Perform some checks to ensure experiment configuration file is valid.
		if (numRounds == -1) {
			System.err.println("Must specify number of rounds!");
			System.exit(1);
		}
		
		if (networkType == NetworkType.CENTRALIZED && (coordinatorIP == null || coordinatorPort == -1)) {
			System.err.println("Must specify coordinator IP and port in centralized experiment!");
			System.exit(1);
		}
		
		if (waypoints.size() == 0) {
			System.err.println("Must specify at least one waypoint!");
			System.exit(1);
		}
		
		if (getNumRobots() == 0) {
			System.err.println("Must specify at least one robot!");
			System.exit(1);
		}
		
		// Ensure all robots start at valid waypoints
		for (int i=0; i < team.size(); i++) {
			RobotExpSettings robot = team.get(i);
			if (getWaypoint(robot.getFirstWaypoint()) == null) {
				System.err.println("Robot " + robot.getName() + " starts at invalid waypoint \"" + robot.getFirstWaypoint() + "\"");
			}
		}
		
		// ensure all robots start from different waypoints
		for (int i=0; i < team.size()-1; i++) {
			RobotExpSettings robot1 = team.get(i);
			for (int j=i+1; j < team.size(); j++) {
				RobotExpSettings robot2 = team.get(j);
				if (robot1.getFirstWaypoint().equals(robot2.getFirstWaypoint())) {
					System.err.println("Robots " + robot1.getName() + " and " + robot2.getName() + " start at the same waypoint!");
					System.exit(1);
				}
			}
		}
		
	}
	
	/**
	 * Obtains the location of a particular waypoint.
	 * 
	 * @param name The name of the waypoint.
	 * @return The location of the waypoint, or null if no such
	 * waypoint exists.
	 */
	public Location getWaypoint(String name) {
		Location result = null;
		Enumeration<Waypoint> e = waypoints.elements();
		while(result == null && e.hasMoreElements()) {
			Waypoint wp = e.nextElement();
			if (wp.getName().equals(name))
				result = wp.getLoc();
		}
		
		return result;
	}
	
	public String getExpName() {
		return expName;
	}
	
	public ExpType getExpType() {
		return expType;
	}
	
	public CoordinationType getCoordinationType() {
		return coordType;
	}
	
	public CoordinationStrength getCoordinationStrength() {
		return coordStrength;
	}
	
	public CoordinationInfo getCoordinationInfo() {
		return coordInfo;
	}
	
	public NetworkType getNetworkType() {
		return networkType;
	}
	
	/**
	 * 
	 * @return The start delay time in seconds.
	 */
	public long getStartDelay() {
		return startDelay;
	}
	
	public InetAddress getCoordinatorIP() {
		return coordinatorIP;
	}
	
	public int getCoordinatorPort() {
		return coordinatorPort;
	}
	
	public int getNumRounds() {
		return numRounds;
	}
	
	public long getAheadTime() {
		return aheadTime;
	}
	
	public int getNumWaypoints() {
		return waypoints.size();
	}
	
	public int getNumRobots() {
		return team.size();
	}
	
	public Iterator<RobotExpSettings> getRobotItr() {
		return team.iterator();
	}
	
	/**
	 * 
	 * @return The experiment settings for the local robot.
	 */
	public RobotExpSettings getMySettings() {
		
		InetAddress pharosIP = null;
		try {
			pharosIP = InetAddress.getByName(RobotIPAssignments.getAdHocIP());
		} catch (PharosException e1) {
			Logger.logErr("Unable to get ad hoc IP address: " + e1.getMessage());
			e1.printStackTrace();
			System.exit(1);
		} catch (UnknownHostException e) {
			Logger.logErr("Failed to get ad hoc IP address: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
		
		
		Enumeration<RobotExpSettings> e = team.elements();
		while (e.hasMoreElements()) {
			RobotExpSettings res = e.nextElement();
			if (res.getIP().equals(pharosIP))
				return res;
		}
		
		Logger.logErr("Unble to find RobotExpSettings for robot with IP " + pharosIP);
		System.exit(1);
		return null;
	}
	
	/**
	 * 
	 * @param name The name of the teammate.
	 * @return The experiment settings for the specified teammate.
	 */
	public RobotExpSettings getTeamateSettings(String name) {
		Enumeration<RobotExpSettings> e = team.elements();
		while (e.hasMoreElements()) {
			RobotExpSettings res = e.nextElement();
			if (res.getName().equals(name))
				return res;
		}
		
		Logger.logErr("Unble to find RobotExpSettings for robot of name " + name);
		System.exit(1);
		return null;
	}
	
	public Vector<RobotExpSettings> getTeam() {
		return team;
	}
	
	public Vector<Waypoint> getRoute() {
		return waypoints;
	}
	
	/**
	 * Returns the index of a particular waypoint in the patrol route.  The index
	 * is the order of the waypoints in the route.  The first waypoint has index 0.
	 * This method terminates the program if the waypoint does not exist.
	 * 
	 * @param waypointName The name of the waypoint.
	 * @return The index of the waypoint.
	 */
	public int getWaypointIndex(String waypointName) {
		for (int i=0; i < waypoints.size(); i++) {
			if (waypoints.get(i).getName().equals(waypointName))
				return i;
		}
		Logger.logErr("Unable to get index of waypoint with name \"" + waypointName + "\"");
		System.exit(1);
		return -1;
	}
	
	/**
	 * Returns the waypoint with a specific index. The first waypoint in the route has index 0.
	 *  This method terminates the program if a waypoint with the specified index does not exist.
	 * 
	 * @param index The index of the waypoint.  
	 * @return The waypoint at the specified index.
	 */
	public Waypoint getWaypoint(int index) {
		if (index >= 0 && index < waypoints.size())
			return waypoints.get(index);
		else {
			Logger.logErr("Unable to get waypoint with index " + index);
			System.exit(1);
			return null;
		}
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer(getClass().getName() + "\n");
		sb.append("\tExp name = " + getExpName() + "\n");
		sb.append("\tExp type = " + getExpType() + "\n");
		sb.append("\tCoordination type = " + getCoordinationType() + "\n");
		sb.append("\tCoordination strength = " + getCoordinationStrength() + "\n");
		sb.append("\tCoordination info = " + getCoordinationInfo() + "\n");
		sb.append("\tNetwork type = " + getNetworkType() + "\n");
		sb.append("\tStart delay = " + startDelay + "\n");
		sb.append("\tCentral Coordinator = " + getCoordinatorIP() + ":" + getCoordinatorPort() + "\n");
		sb.append("\tNum Rounds = " + getNumRounds() + "\n");
		sb.append("\tAhead Time = " + getAheadTime() + "\n");
		sb.append("\tWaypoints:\n");
		Iterator<Waypoint> wp = waypoints.iterator();
		while (wp.hasNext()) {
			sb.append("\t\t" + wp.next().toString() + "\n");
		}
		
		sb.append("\tRobots:\n");
		Iterator<RobotExpSettings> itr = team.iterator();
		while (itr.hasNext()) {
			sb.append("\t\t" + itr.next().toString() + "\n");
		}
		
		return sb.toString();
	}
}