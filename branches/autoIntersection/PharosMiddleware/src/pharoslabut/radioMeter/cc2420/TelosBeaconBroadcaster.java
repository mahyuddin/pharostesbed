package pharoslabut.radioMeter.cc2420;

import net.tinyos.message.*;
import net.tinyos.packet.*;
import net.tinyos.util.*;

//import java.util.*;
import java.io.*;

import pharoslabut.beacon.*;
//import pharoslabut.logger.*;
import pharoslabut.logger.FileLogger;

/**
 * This is responsible for broadcasting TelosB beacons.
 * 
 * @author Chien-Liang Fok
 */
public class TelosBeaconBroadcaster extends BeaconBroadcaster {
	public static short TX_PWR_MAX = 31;
	public static short TX_PWR_MIN = 1;
	
//	private String outputFile;
	private MoteIF moteIF;
//	private FileLogger flogger = null;
	
	/**
	 * This determines the sequence number of each beacon.  Each beacon
	 * has a unique seuqence number.
	 */
	private int seqno = 0;
	
	/**
	 * The ID of the mote that is attached to this device.  This ID is included
	 * in the beacon to identify the transmitter.
	 */
	private int moteID;
	
//	private int numBcasts;
//	private Timer timer = null;
	
//	private String motePort;
	private TelosBeaconReceiver receiver;
	
	/**
	 * The constructor.
	 * 
	 * @throws TelosBeaconException When unble to connect to the TelosB node.
	 */
	public TelosBeaconBroadcaster() throws TelosBeaconException {
		try {
			// Get the local node ID
			moteID = getMoteID();
			log("Mote ID = " + moteID);
		} catch(TelosBeaconException e) {
			log("Unable to get moteID, assuming it is zero");
			moteID = 0;
		}
		
		try {
			// Get the serial port on which the TelosB is connected.
			String motePort = detectMotePort();

			PhoenixSource phoenixLocal = null;
			
			if (motePort == null) {
				phoenixLocal = BuildSource.makePhoenix(PrintStreamMessenger.err);
			} else {
				phoenixLocal = BuildSource.makePhoenix(motePort, PrintStreamMessenger.err);
			}

			moteIF = new MoteIF(phoenixLocal);
			receiver = new TelosBeaconReceiver(moteIF);
		} catch(Exception e) {
			e.printStackTrace();
			throw new TelosBeaconException("Unable to connect to TelosB Mote!");
		}
	}
	
	public TelosBeaconReceiver getReceiver() {
		return receiver;
	}
	
//	public void setFileLogger(FileLogger flogger) {
//		this.flogger = flogger;
//		receiver.setFileLogger(flogger);
//	}
	
	/**
	 * Determines the ID of the Mote.  The ID is assumed to be the last octal in
	 * the wireless ad hoc network IP address.  The form of this IP is 
	 * 10.11.12.xx where "xx" is the mote ID.
	 * 
	 * @return The mote ID.
	 * @throws TelosBeaconException When the ID could not be determined.
	 */
	public static int getMoteID() throws TelosBeaconException {
		int addr = 0;
		String ipAddr = pharoslabut.beacon.WiFiBeaconBroadcaster.getPharosIP();
		if (ipAddr != null) {
			//System.out.println("ipAddr = " + ipAddr);
			String[] addrTokens = ipAddr.split("\\.");
			if (addrTokens.length == 4)
				addr = Integer.valueOf(addrTokens[3]);
			else {
				String eMsg = "Unable to determine mote ID (addrTokens.length = " 
					+ addrTokens.length + ").";
				System.err.println(eMsg);
				throw new TelosBeaconException(eMsg);
			}
//			for (int i=0; i < addrStr.length; i++) {
//        		System.out.println(i + ": " + addrStr[i]);
//        	}
		} else {
			String eMsg = "Unable to determine mote ID (ipAddr is null).";
			System.err.println(eMsg);
			throw new TelosBeaconException(eMsg);
		}
		return addr;
	}
	
	/**
	 * Determines which port connects to the TelosB mote.  It does this by analyzing
	 * output of program "motelist" and finding an entry for the TelosB mote.
	 * 
	 * @return The port on which the TelosB mote is attached.
	 * @throws TelosBeaconException When no TelosB mote is found.
	 */
	public static String detectMotePort() throws TelosBeaconException {
		String result = null;
		String moteLine = null;
        try {
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec("motelist");

            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

            String line=null;

            while((line=input.readLine()) != null && moteLine == null) {
                if (line.contains("Telos")) 
                	moteLine = line;
            }

            pr.waitFor();
            //int exitVal = pr.waitFor();
            //System.out.println("Exited with error code "+exitVal);
        } catch(Exception e) {
        	String eMsg = "Unable to run motelist: " + e.toString();
            System.err.println(eMsg);
            throw new TelosBeaconException(eMsg);
        }
        
        if (moteLine != null) {
        	String[] tokens = moteLine.split("[\\s]+");
        	//for (int i=0; i < tokens.length; i++) {
        	//	System.out.println(i + ": " + tokens[i]);
        	//}
        	if (tokens.length >= 2)
        		result = "serial@" + tokens[1] + ":telosb";
        	else {
        		String eMsg = "Unable to determine mote port (tokens.length = " 
					+ tokens.length + ").";
        		System.err.println(eMsg);
        		throw new TelosBeaconException(eMsg);
        	}
        }
        
		return result;
	}
	
	/**
	 * Starts the broadcasting of beacons at a specified rate.
	 * 
	 * @param minPeriod The minimum number of milliseconds between broadcasts.
	 * @param maxPeriod The maximum number of milliseconds between broadcasts.
	 */
//	public void startBroadcast(long minPeriod, long maxPeriod) {
//		stopBroadcast();	
//		timer = new Timer();
//		timer.scheduleAtFixedRate(new SendBeaconTimerTask(), 0 /* delay */, period);
//	}
	
	/**
	 * Stops the broadcasting of beacons.
	 */
//	public void stopBroadcast() {
//		if (timer != null) {
//			timer.cancel();
//			timer = null;
//		}
//	}
	
	/**
	 * This is called by the super class whenever a TelosB beacon should be
	 * broadcasted.
	 */
	@Override
	protected void sendBeacon() {
		SendBeaconMsg sbm = new SendBeaconMsg();
		log("SEND_TELSOB_BCAST\t" + moteID + "\t" + seqno + "\t" + (short)txPower);
		
		try {
			sbm.set_sndrID(moteID);
			sbm.set_seqno(seqno++);
			sbm.set_txPwr((short)txPower);
			moteIF.send(moteID, sbm);
		} catch(IOException e) {
			log("Error sending message: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
//	private void saveResult(String result) {
//		if (outputFile != null) {
//			try {
//				FileWriter fw = new FileWriter(outputFile, true /* append */);
//				PrintWriter pw = new PrintWriter(fw);
//				pw.println(result);
//				pw.close();
//				fw.close();
//			} catch(IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}
	@Override
	public void setFileLogger(FileLogger flogger)  {
		this.flogger = flogger;
		receiver.setFileLogger(flogger);
	}
	
	protected void log(String msg) {
		String result = "TelosBeaconBroadcaster: " + msg;
		if (flogger != null) 
			flogger.log(result);
		
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
	}
	
//	private class SendBeaconTimerTask extends TimerTask {
//		public SendBeaconTimerTask() {
//		}
//		
//		@Override
//		public void run() {
//			sendBroadcast();
//		}
//	}
	
//	public static final void main(String[] args) {
//		try {
//			System.out.println("Mote port = " + TelosRadioSignalMeter.getMotePort());
//			System.out.println("Mote ID = " + TelosRadioSignalMeter.getMoteID());
//		} catch (RadioSignalMeterException e) {
//			e.printStackTrace();
//		}
//	}
	
//	private static void usage() {
//		System.err.println("usage: RadioSignalMeter [-comm <motePort>] [-file <dest>]\n");
//		System.err.println("For example, if your TelosB mote is attached to COM3,");
//		System.err.println("use the following command:");
//		System.err.println("  java RadioSignalMeter -comm serial@COM3:telosb");
//		
//	}
//	
//	public static void main(String[] args) {
//		String motePort = null;
//		String outputFile = null;
//		for (int i=0; i < args.length; i++) {
//			if (args[i].equals("-comm")) {
//				motePort = args[++i];
//				System.out.println("Using mote port: " + motePort);
//			} 
//			else if (args[i].equals("-file")) {
//				outputFile = args[++i];
//				System.out.println("Saving data to: " + outputFile);
//			}
//			else {
//				usage();
//				System.exit(1);
//			}
//		}
//		
//		RadioSignalMeter meter = new RadioSignalMeter(motePort, outputFile);
//  }
}