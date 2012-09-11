package pharoslabut.radioMeter.cc2420;

import pharoslabut.RobotIPAssignments;
import pharoslabut.logger.FileLogger;
import pharoslabut.exceptions.*;
import net.tinyos.message.*;

/**
 * Receives TelosB beacons and records the receptions.
 * 
 * @author Chien-Liang Fok
 */
public class TelosBeaconReceiver implements MessageListener{
//	private MoteIF moteIF;
	private FileLogger flogger;
	
	int currCount;
	int targetCount;
	
	/**
	 * The ID of the mote that is attached to this device.  This ID is included
	 * in the beacon to identify the transmitter.
	 */
	private int moteID;
	
	public TelosBeaconReceiver(MoteIF moteIF) {
		try {
			moteID = RobotIPAssignments.getID(); // Get the local node ID
			log("Mote ID = " + moteID);
		} catch(PharosException e) {
			log("Unable to get moteID, assuming it is zero");
			moteID = 0;
		}
		
//		this.moteIF = moteIF;
		moteIF.registerListener(new RadioSignalResultsMsg(), this);
	}
	
	public void rcvBeacons(int targetCount) {
		log("Waiting until " + targetCount + " beacons are received...");
		this.currCount = 0;
		this.targetCount = targetCount;
		
		while (currCount < targetCount) {
			synchronized(this) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			log("Received " + currCount + " of " + targetCount + " beacons");
		}
	}
	
	public void messageReceived(int to, Message message) {
		if (message instanceof RadioSignalResultsMsg) {
			RadioSignalResultsMsg resultMsg = (RadioSignalResultsMsg)message;
			log("RADIO_CC2420_RECEIVE" 
				+ "\t" + moteID //resultMsg.get_idReceiver()
				+ "\t" + resultMsg.get_idSender()
				+ "\t" + resultMsg.get_seqno()
				+ "\t" + resultMsg.get_rssi()
				+ "\t" + resultMsg.get_lqi()
				+ "\t" + resultMsg.get_timestamp());
			
			// Update the number of beacons received.
			currCount++;
			synchronized(this) {
				notifyAll();
			}
		}
		else {
			log("UNKNOWN_MSG"
				+ "\t" + message.toString());
		}
	}
	
	public void setFileLogger(FileLogger flogger) {
		this.flogger = flogger;
		
	}
	
	private void log(String msg) {
		String result = "TelosBeaconReceiver: " + msg;
		if (flogger != null) 
			flogger.log(result);
		
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
	}
}
