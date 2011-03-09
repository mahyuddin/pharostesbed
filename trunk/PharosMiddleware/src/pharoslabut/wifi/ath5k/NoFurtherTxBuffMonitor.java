package pharoslabut.wifi.ath5k;

import java.io.*;
import java.util.*;

/**
 * This is supposed to run as a background daemon monitoring for ath5k driver
 * errors of type "no further tx buff available".  Whenever it detects the error,
 * it resets the wireless interface.
 * 
 * @author Chien-Liang Fok
 */
public class NoFurtherTxBuffMonitor implements Runnable {
	
	private PrintWriter pw = null;
	double lastTimeStamp = 0;
	private int checkInterval;
	public static final String LOG_FILE = "/var/log/ath5kmonitor.log";
	
	/**
	 * The constructor.
	 * 
	 * @param The interval in milliseconds between checking for the error.
	 */
	public NoFurtherTxBuffMonitor(int checkInterval) {
		this.checkInterval = checkInterval;
		try {
			FileWriter fw = new FileWriter(LOG_FILE, true /* append */);
			pw = new PrintWriter(fw);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new Thread(this).start();
	}
	
	/**
	 * Checks whether the noFutherTxBuff error occurred.  This checks whether the error occurred
	 * by monitoring the last line of /var/log/syslog.
	 * 
	 * @return true if error occurred.
	 */
	public boolean noFurtherTxBuffDetected() {
		try {
			Runtime rt = Runtime.getRuntime();
			String cmd = "tail -n 1 /var/log/syslog";
			
			Process pr = rt.exec(cmd);

			BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

			String nextLine = null;
			StringBuffer lineBuff = new StringBuffer();

			while((nextLine=input.readLine()) != null) {
				lineBuff.append(nextLine);
			}

			int exitVal = pr.waitFor();
			
			String line = lineBuff.toString();
			//log("line = \"" + line + "\"");
			String[] tokens = line.split("[\\s+|\\]]");
			//for (int i=0; i < tokens.length; i++) {
			//	log(i + ": " + tokens[i]);
			//}
			if (line.contains("no further txbuf available, dropping packet")) {
					double timestamp = Double.valueOf(tokens[7]);
					if (timestamp != lastTimeStamp) {
						lastTimeStamp = timestamp;
						return true;
					} else {
						//log("duplicate error, ignoring");
					}
			}
			
		} catch(Exception e) {
			String eMsg = "Unable to run command: " + e.toString();
			System.err.println(eMsg);
			log(eMsg);
			System.exit(1);
		}
		return false;
	}
	
	private void resetWirelessInterface() {
		log("Resetting wireless interface...");
		try {
			Runtime rt = Runtime.getRuntime();
			String cmd = "sudo ifdown wlan0";
			Process pr = rt.exec(cmd);
			int exitVal = pr.waitFor();
			log("exitVal of ifdown: " + exitVal);
			
			cmd = "sudo ifup wlan0";
			pr = rt.exec(cmd);
			exitVal = pr.waitFor();
			log("exitVal of ifup: " + exitVal);
			
		} catch(Exception e) {
			String eMsg = "Unable to run command: " + e.toString();
			System.err.println(eMsg);
			log(eMsg);
			System.exit(1);
		}
		log("Wireless interface restarted...");
	}
	
	/**
	 * Logs the error detection event.
	 */
	private void logErrorDetected() {
		if (pw != null) {
			Calendar cal = Calendar.getInstance();
			int sec = cal.get(Calendar.SECOND);
			int min = cal.get(Calendar.MINUTE);
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int day = cal.get(Calendar.DATE);
			int month = cal.get(Calendar.MONTH) + 1;
			int year = cal.get(Calendar.YEAR);
			
			String msg = "[" + System.currentTimeMillis() + "] No Further Tx Buf Error Detected at " + year + "-" 
				+ month + "-" + day + " " + hour + ":" + min + ":" + sec + ", resetting interface...";
			
			pw.println(msg);
			pw.flush();
		}
	}
	
	/**
	 * Sits in an infinite loop searching for the ath5k no tx buff error.
	 */
	public void run() {
		while (true) {
			
			boolean errorDetected = noFurtherTxBuffDetected();
			//log("error = " + errorDetected);
			if (errorDetected) {
				logErrorDetected();
				resetWirelessInterface();
			}
				
			synchronized(this) {
				try {
					this.wait(checkInterval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void log(String msg) {
		System.out.println(msg);
	}
	
	private static void print(String msg) {
		System.out.println(msg);
	}
	
	private static void usage() {
		print("Usage: pharoslabut.wifi.ath5k.NoFurtherTxBuffMonitor <options>\n");
		print("Where <options> include:");
		print("\t-checkInterval <check interval>: The period between checking for the error in milliseconds");
	}
	
	public static void main(String[] args) {
		int checkInterval = 500;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-checkInterval")) {
					checkInterval = Integer.valueOf(args[++i]);
				} else {
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			usage();
			System.exit(1);
		}
		new NoFurtherTxBuffMonitor(checkInterval);
	}
}