package pharoslabut.demo.simonsays.io;

import java.util.ArrayList;

import pharoslabut.demo.simonsays.SimonSaysClient;
import pharoslabut.demo.simonsays.SimonSaysServer;
import pharoslabut.io.Message;
import pharoslabut.sensors.CricketData;

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

	private CricketData cricketData;
	
	/**
	 * The constructor.
	 * 
	 * @param dist the array of distances calculated by the Cricket mote,
	 * it has one element for each Cricket beacon that was seen
	 */
	public CricketDataMsg(CricketData cd) {
		this.cricketData = cd;
	}
	
	
	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
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
