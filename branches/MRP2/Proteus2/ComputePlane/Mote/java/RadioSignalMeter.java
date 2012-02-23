import net.tinyos.message.*;
import net.tinyos.packet.*;
import net.tinyos.util.*;

import java.util.*;
import java.io.*;

public class RadioSignalMeter implements MessageListener {
	private String motePort, outputFile;
	private MoteIF moteIF;
  
	public RadioSignalMeter(String motePort, String outputFile) {
		this.motePort = motePort;
		this.outputFile = outputFile;
		
		PhoenixSource phoenixLocal = null;
		
		if (motePort == null) {
			phoenixLocal = BuildSource.makePhoenix(PrintStreamMessenger.err);
		} else {
			phoenixLocal = BuildSource.makePhoenix(motePort, PrintStreamMessenger.err);
		}
		
		moteIF = new MoteIF(phoenixLocal);
		moteIF.registerListener(new RadioSignalResultsMsg(), this);
	}
	
	public void messageReceived(int to, Message message) {
		Date date = Calendar.getInstance().getTime();
		
		if (message instanceof RadioSignalResultsMsg) {
			RadioSignalResultsMsg resultMsg = (RadioSignalResultsMsg)message;
			log(date + "\t" + date.getTime() 
				+ "\t" + "RADIO15.4_RESULT_MSG" 
				+ "\t" + resultMsg.get_idReceiver()
				+ "\t" + resultMsg.get_idSender()
				+ "\t" + resultMsg.get_seqno()
				+ "\t" + resultMsg.get_rssi()
				+ "\t" + resultMsg.get_lqi()
				+ "\t" + resultMsg.get_timestamp());
		}
		else {
			log(date + "\t" + date.getTime()
				+ "\t UNKNOWN_MSG"
				+ "\t" + message.toString());
		}
	}
	
	private void saveResult(String result) {
		if (outputFile != null) {
			try {
				FileWriter fw = new FileWriter(outputFile, true /* append */);
				PrintWriter pw = new PrintWriter(fw);
				pw.println(result);
				pw.close();
				fw.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void log(String msg) {
		System.out.println(msg);
		saveResult(msg);
	}
	private static void usage() {
		System.err.println("usage: RadioSignalMeter [-comm <motePort>] [-file <dest>]\n");
		System.err.println("For example, if your TelosB mote is attached to COM3,");
		System.err.println("use the following command:");
		System.err.println("  java RadioSignalMeter -comm serial@COM3:telosb");
		
	}
	
	public static void main(String[] args) {
		String motePort = null;
		String outputFile = null;
		for (int i=0; i < args.length; i++) {
			if (args[i].equals("-comm")) {
				motePort = args[++i];
				System.out.println("Using mote port: " + motePort);
			} 
			else if (args[i].equals("-file")) {
				outputFile = args[++i];
				System.out.println("Saving data to: " + outputFile);
			}
			else {
				usage();
				System.exit(1);
			}
		}
		
		RadioSignalMeter meter = new RadioSignalMeter(motePort, outputFile);
  }
}