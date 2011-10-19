package pharoslabut.demo.simonsays.io;

import pharoslabut.demo.simonsays.BeaconReading;
import pharoslabut.demo.simonsays.SimonSaysClient;
import pharoslabut.demo.simonsays.SimonSaysServer;
import pharoslabut.io.Message;
import playerclient3.structures.PlayerPoint3d;

/**
 * This message is sent by the SimonSaysServer to the SimonSaysClient with
 * a new BeaconReading 
 * 
 * @author Kevin Boos
 * @see SimonSaysClient
 * @see SimonSaysServer
 */
public class BeaconReadingMsg implements Message {

	public BeaconReading getBeaconReading() {
		return beaconReading;
	}

	private static final long serialVersionUID = -7795567339491090483L;

	/**
	 * the data collected from this cricket beacon
	 */
	private BeaconReading beaconReading;
	
	/**
	 * The constructor.
	 * 
	 * @param br the BeaconReading
	 * @param pt the coordinates (x,y) of this cricket beacon
	 */
	public BeaconReadingMsg(BeaconReading br) {
		this.beaconReading = br;
	}
	
	
	@Override
	public MsgType getType() {
		return MsgType.CUSTOM;
	}


	public String toString() {
		return "Beacon Reading: " + this.beaconReading.toString();
	}
}
