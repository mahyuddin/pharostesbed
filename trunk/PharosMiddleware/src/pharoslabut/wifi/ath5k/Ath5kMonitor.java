package pharoslabut.wifi.ath5k;

import java.io.*;
import java.util.*;

/**
 * This is supposed to run as a background daemon monitoring for ath5k driver
 * errors.  Errors detected include "no further tx buff available" and "unsupported jumbo".
 * Whenever it detects an error, it resets the wireless interface.
 * 
 * To create a jar file containing this program, execute the following command from the 
 * base package directory:
 * 
 * $ jar -cf Ath5kMonitor.jar pharoslabut/wifi/ath5k/*
 * 
 * @author Chien-Liang Fok
 * @see http://pharos.ece.utexas.edu/wiki/index.php/Fixing_the_ath5k_no_tx_buf_Error_-_03/06/2011
 */
public class Ath5kMonitor implements Runnable {
	
	private PrintWriter pw = null;
	double lastTimeStampNoTxBuff = 0;
	double lastTimeStampUnsupportedJumbo = 0;
	private int checkInterval;
	public static final String LOG_FILE = "/var/log/ath5kmonitor.log";
	
	/**
	 * The constructor.
	 * 
	 * @param The interval in milliseconds between checking for the error.
	 */
	public Ath5kMonitor(int checkInterval) {
		this.checkInterval = checkInterval;
		try {
			FileWriter fw = new FileWriter(LOG_FILE, true /* append */);
			pw = new PrintWriter(fw);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Unable to open log file, try running as super user.");
			System.exit(1);
		}
		log("Starting to monitor for Ath5k faults at " + getDateString() + "...");
		new Thread(this).start();
	}
	
	private double getTime(String line) {
		String[] tokens = line.split("[\\[|\\]]");
		double result = 0;
		boolean found = false;
		
		for (int i=0; i < tokens.length && !found; i++) {
			//System.out.println("Checking token " + tokens[i]);
			try {
				result = Double.valueOf(tokens[i]);
				found = true;
			} catch(Exception e) {
				// ignore token as it is not a double
			}
		}

		if (!found) {
			log("ERROR: Unable to determine timestmp of line \"" + line + "\"");
			result = -1;
		}
		return result;
	}
	
	/**
	 * Checks for whether an error as occurred.  This checks whether the error occurred
	 * by monitoring the last line of /var/log/syslog.
	 * 
	 * @return true if error occurred.
	 */
	public boolean errorDetected() {
		boolean errorFound = false;
		
		try {
			Runtime rt = Runtime.getRuntime();
			String cmd = "tail -n 20 /var/log/syslog";
			
			Process pr = rt.exec(cmd);

			BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

			String nextLine = null;
			Vector<String> lineBuff = new Vector<String>();

			while((nextLine=input.readLine()) != null) {
				lineBuff.add(nextLine);
			}

			//int exitVal = 
			pr.waitFor();
			
			// Search text backwards to find most recent error first...
			for (int i=lineBuff.size()-1; i > 0 && !errorFound; i--) {
				String currLine = lineBuff.get(i);
				//log("line = \"" + line + "\"");
				String[] tokens = currLine.split("[\\s+|\\]]");
				//for (int i=0; i < tokens.length; i++) {
				//	log(i + ": " + tokens[i]);
				//}
				if (currLine.contains("no further txbuf available, dropping packet")) {
					try {
						double timestamp = getTime(currLine);
						if (timestamp != -1 && timestamp > lastTimeStampNoTxBuff) {
							lastTimeStampNoTxBuff = timestamp;
							log("No Further Tx Buf Error Detected at " + getDateString() + ", resetting interface...");
							errorFound = true;
						}
					} catch(Exception e) {
						e.printStackTrace();
						log("ERROR: Unable to parse: " + currLine);
					}
				}
				else if (currLine.contains("unsupported jumbo")) {
					try {
						double timestamp = getTime(currLine);
						if (timestamp != -1 && timestamp > lastTimeStampUnsupportedJumbo) {
							lastTimeStampUnsupportedJumbo = timestamp;
							log("Unsupported Jumbo Error Detected at " + getDateString() + ", resetting interface...");
							errorFound = true;
						}
					} catch(Exception e) {
						e.printStackTrace();
						log("ERROR: Unable to parse: " + currLine);
					}
				}
			}
		} catch(Exception e) {
			String eMsg = "Unable to run command: " + e.toString();
			System.err.println(eMsg);
			log(eMsg);
			System.exit(1);
		}
		return errorFound;
	}
	
	private void resetWirelessInterface() {
		log("Resetting wireless interface...");
		try {
			Runtime rt = Runtime.getRuntime();
			String cmd = "sudo ifdown wlan0";
			Process pr = rt.exec(cmd);
			int exitVal = pr.waitFor();
			if (exitVal == 0)
				log("sudo ifdown wlan0 executed OK");
			else
				log("sudo ifdown wlan0 exited with error code " + exitVal);
			
			cmd = "sudo ifup wlan0";
			pr = rt.exec(cmd);
			exitVal = pr.waitFor();
			if (exitVal == 0)
				log("sudo ifup wlan0 executed OK");
			else
				log("sudo ifup wlan0 exited with error code " + exitVal);
		} catch(Exception e) {
			String eMsg = "Unable to run command: " + e.toString();
			System.err.println(eMsg);
			log(eMsg);
			System.exit(1);
		}
		log("Wireless interface restarted...");
	}
	
	private void log(String msg) {
		msg = "[" + System.currentTimeMillis() + "] " + msg;
		if (pw != null) {
			pw.println(msg);
			pw.flush();
		}
		System.out.println(msg);
	}
	
	private String getDateString() {
		Calendar cal = Calendar.getInstance();
		int sec = cal.get(Calendar.SECOND);
		int min = cal.get(Calendar.MINUTE);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int day = cal.get(Calendar.DATE);
		int month = cal.get(Calendar.MONTH) + 1;
		int year = cal.get(Calendar.YEAR);
		return year + "-" + month + "-" + day + " " + hour + ":" + min + ":" + sec;
	}
	
	/**
	 * Sits in an infinite loop searching for the ath5k no tx buff error.
	 */
	public void run() {
		while (true) {
			
			boolean errorDetected = errorDetected();
			//log("error = " + errorDetected);
			if (errorDetected) {
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
	
	private static void print(String msg) {
		System.out.println(msg);
	}
	
	private static void usage() {
		print("Usage: pharoslabut.wifi.ath5k.Ath5kMonitor <options>\n");
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
		new Ath5kMonitor(checkInterval);
	}
}
