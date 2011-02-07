package pharoslabut.radioMeter.cc2420;

import net.tinyos.message.*;
import net.tinyos.packet.*;
import net.tinyos.util.*;

import java.util.*;
import java.io.*;

import pharoslabut.logger.*;

public class RadioSignalMeter implements MessageListener {
//	private String outputFile;
	private MoteIF moteIF;
	private FileLogger flogger = null;
	private int seqno = 0;
	private int moteID;
	
	private int numBcasts;
	private Timer timer = null;
	
	private String motePort;
	
	public RadioSignalMeter() throws RadioSignalMeterException {
		try {
			// Get the local node ID
			moteID = getMoteID();
			log("Mote ID = " + moteID);
		} catch(RadioSignalMeterException e) {
			log("Unable to get moteID, assuming it is zero");
			moteID = 0;
		}
		
		try {
			// Get the serial port on which the TelosB is connected.
			motePort = getMotePort();

			PhoenixSource phoenixLocal = null;



			if (motePort == null) {
				phoenixLocal = BuildSource.makePhoenix(PrintStreamMessenger.err);
			} else {
				phoenixLocal = BuildSource.makePhoenix(motePort, PrintStreamMessenger.err);
			}

			moteIF = new MoteIF(phoenixLocal);
			moteIF.registerListener(new RadioSignalResultsMsg(), this);
		} catch(Exception e) {
			e.printStackTrace();
			throw new RadioSignalMeterException("Unable to connect to TelosB Mote!");
		}
	}
	
	public void setFileLogger(FileLogger flogger) {
		this.flogger = flogger;
	}
	
	/**
	 * Determines the ID of the mote.  The ID is assumed to be equal to
	 * the last octal in the wireless ad hoc network IP address.  The
	 * form of this IP is 10.11.12.xx where "xx" is the mote ID.
	 * 
	 * @return The mote ID.
	 */
	public static int getMoteID() throws RadioSignalMeterException {
		int addr = 0;
		String ipAddr = pharoslabut.beacon.BeaconBroadcaster.getPharosIP();
		if (ipAddr != null) {
			//System.out.println("ipAddr = " + ipAddr);
			String[] addrTokens = ipAddr.split("\\.");
			if (addrTokens.length == 4)
				addr = Integer.valueOf(addrTokens[3]);
			else {
				String eMsg = "Unable to determine mote ID (addrTokens.length = " 
					+ addrTokens.length + ").";
				System.err.println(eMsg);
				throw new RadioSignalMeterException(eMsg);
			}
//			for (int i=0; i < addrStr.length; i++) {
//        		System.out.println(i + ": " + addrStr[i]);
//        	}
		} else {
			String eMsg = "Unable to determine mote ID (ipAddr is null).";
			System.err.println(eMsg);
			throw new RadioSignalMeterException(eMsg);
		}
		return addr;
	}
	
	/**
	 * Determines which port the TelosB is attached to.  It does this by analyzing
	 * output of program "motelist" and finding an entry for the TelosB mote.
	 * 
	 * @return The port on which the TelosB mote is attached.
	 */
	public static String getMotePort() throws RadioSignalMeterException {
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
            throw new RadioSignalMeterException(eMsg);
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
        		throw new RadioSignalMeterException(eMsg);
        	}
        }
        
		return result;
	}
	
	/**
	 * Starts the broadcasting of beacons at a specified rate.
	 * 
	 * @param period The broadcast period in milliseconds.
	 * @param numBcasts The number of broadcasts to emit.
	 */
	public void startBroadcast(long period, int numBcasts) {
		stopBroadcast();
		this.numBcasts = numBcasts;
		
		if (numBcasts > 0) {
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(new SendBeaconTimerTask(), 0 /* delay */, period);
		}
	}
	
	/**
	 * Stops the broadcasting of beacons.
	 */
	public void stopBroadcast() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}
	
	private void sendBroadcast() {
		SendBeaconMsg sbm = new SendBeaconMsg();
		log("SEND_BCAST\t" + moteID + "\t" + seqno);
		sbm.set_seqno(seqno++);
		try {
			moteIF.send(moteID, sbm);
		} catch(IOException e) {
			log("Error sending message: " + e.getMessage());
		}
		if (--numBcasts == 0) {
			timer.cancel();
			timer = null;
		}
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
	
	private void log(String msg) {
		String result = "RadioSignalMeter: " + msg;
		if (flogger != null) 
			flogger.log(result);
		
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(result);
	}
	
	private class SendBeaconTimerTask extends TimerTask {
		public SendBeaconTimerTask() {
		}
		
		@Override
		public void run() {
			sendBroadcast();
		}
	}
	
	public static final void main(String[] args) {
		try {
			System.out.println("Mote port = " + RadioSignalMeter.getMotePort());
			System.out.println("Mote ID = " + RadioSignalMeter.getMoteID());
		} catch (RadioSignalMeterException e) {
			e.printStackTrace();
		}
	}
	
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