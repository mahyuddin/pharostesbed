package pharoslabut.radioMeter.cc2420;

import pharoslabut.logger.FileLogger;
import net.tinyos.message.*;

/**
 * Receives TelosB beacons and records the receptions.
 * 
 * @author Chien-Liang Fok
 */
public class TelosBeaconReceiver implements MessageListener{
//	private MoteIF moteIF;
	private FileLogger flogger;
	
	public TelosBeaconReceiver(MoteIF moteIF) {
//		this.moteIF = moteIF;
		moteIF.registerListener(new RadioSignalResultsMsg(), this);
	}
	
	public void messageReceived(int to, Message message) {
		if (message instanceof RadioSignalResultsMsg) {
			RadioSignalResultsMsg resultMsg = (RadioSignalResultsMsg)message;
			log("RADIO_CC2420_RECEIVE" 
				+ "\t" + resultMsg.get_idReceiver()
				+ "\t" + resultMsg.get_idSender()
				+ "\t" + resultMsg.get_seqno()
				+ "\t" + resultMsg.get_rssi()
				+ "\t" + resultMsg.get_lqi()
				+ "\t" + resultMsg.get_timestamp());
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
