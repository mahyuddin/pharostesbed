package pharoslabut.demo.indoorMRPatrol;

import pharoslabut.io.Message;

import java.util.*;

/**
 * Contains the specifications for a multi-robot patrol experiment.
 * This is sent by a SimpleMRPatrolClient to a SimpleMRPatrolServer.
 * 
 * @author Chien-Liang Fok
 */
public class LoadExpSettingsMsg implements Message {

	private static final long serialVersionUID = 722133144971291375L;

	/**
	 * The number of markers along the patrol route.
	 */
	private int numMarkers;
	
	/**
	 * The distance between markers in meters.
	 */
	private double markerDist;
	
	/**
	 * A list of the robots participating in the patrol and
	 * their starting locations.
	 */
	private Vector<RobotExpSettings> team;
	
	/**
	 * The constructor.
	 * 
	 * @param numMarkers The number of markers along the patrol route.
	 * @param markerDist The distance between markers in meters.
	 * @param team A list of the robots participating in the patrol and
	 * their starting locations.
	 */
	public LoadExpSettingsMsg(int numMarkers, double markerDist, Vector<RobotExpSettings> team) 
	{
		this.numMarkers = numMarkers;
		this.markerDist = markerDist;
		this.team = team;
	}
	
	/**
	 * 
	 * @return The number of markers along the patrol route.
	 */
	public int getNumMarkers() {
		return numMarkers;
	}
	
	/**
	 * 
	 * @return The distance between markers in meters.
	 */
	public double getMarkerDist() {
		return markerDist;
	}
	
	/**
	 * 
	 * @return A list of the robots participating in the patrol and
	 * their starting locations.
	 */
	public Vector<RobotExpSettings> getTeam() {
		return team;
	}
	
	@Override
	public MsgType getType() {
		return MsgType.LOAD_SETTINGS;
	}
	
	public String toString() {
		return getClass().getName();
	}

}
