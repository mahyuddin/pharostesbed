package pharoslabut.demo.simonsays.io;

import pharoslabut.demo.simonsays.SimonSaysClient;
import pharoslabut.demo.simonsays.SimonSaysServer;
import pharoslabut.io.Message;
import pharoslabut.sensors.CricketData;
import playerclient3.structures.PlayerPoint3d;

/**
 * This message is sent by the SimonSaysServer to the SimonSaysClient with
 * new data from the Cricket Mote
 * 
 * @author Kevin Boos
 * @see SimonSaysClient
 * @see SimonSaysServer
 */
public class CricketDataMsg implements Message {

	private static final long serialVersionUID = -7795567339491090483L;

	/**
	 * the data collected from this cricket beacon
	 */
	private CricketData cricketData;
	/**
	 * the coordinates of this cricket beacon
	 */
	private PlayerPoint3d point;
	
	/**
	 * The constructor.
	 * 
	 * @param cd the data values read from this cricket beacon
	 * @param pt the coordinates (x,y,z) of this cricket beacon
	 */
	public CricketDataMsg(CricketData cd, PlayerPoint3d pt) {
		this.cricketData = cd;
		this.point = pt;
	}
	
	
	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
	}

	
	public PlayerPoint3d getPoint() {
		return point;
	}

	public void setPoint(PlayerPoint3d point) {
		this.point = point;
	}
	
	public CricketData getCricketData() {
		return cricketData;
	}

	public void setCricketData(CricketData cricketData) {
		this.cricketData = cricketData;
	}

	public String toString() {
		return "Cricket Data: " + this.cricketData.toString();
	}
}
