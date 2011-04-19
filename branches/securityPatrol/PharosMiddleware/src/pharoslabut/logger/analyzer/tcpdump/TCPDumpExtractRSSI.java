package pharoslabut.logger.analyzer.tcpdump;

import java.util.Enumeration;

import pharoslabut.logger.FileLogger;

public class TCPDumpExtractRSSI {
//	
//	public TCPDumpExtractRSSI() {
//		
//	}
//	
//	private void log(String msg, FileLogger flogger) {
//		System.out.println(msg);
//		if (flogger != null)
//			flogger.log(msg);
//	}
	
	private static void logBeacon(TCPDumpRecord currRec, long startTime, FileLogger flogger) {
		flogger.log(currRec.getTimeStamp() + "\t" + (currRec.getTimeStamp() - startTime)/1000.0 + "\t" + Long.toHexString(currRec.getSourceAddr())
				+ "\t" + currRec.getNetworkName() + "\t" + currRec.getRSSI());
	}
	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(msg);
	}
	
	private static void usage() {
		System.setProperty ("PharosMiddleware.debug", "true");
		print("Usage: pharoslabut.logger.analyzer.tcpdump.TCPDumpExtractRSSI <options>\n");
		print("Where <options> include:");
		print("\t-file <name of file containing TCP Dump log in plain-text format>: (required)");
		print("\t-sa <MAC address of sender>: Only extract RSSI of beacons sent by this sender (default null)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String fileName = null;
		Long sa = null;
		
		// Process the command line arguments...
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-file"))
					fileName = args[++i];
				else if (args[i].equals("-sa"))
					sa = Long.valueOf(args[++i], 16);
				else if (args[i].equals("-debug") || args[i].equals("-d"))
					System.setProperty ("PharosMiddleware.debug", "true");
				else {
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			usage();
			System.exit(1);
		}
		
		if (fileName == null) {
			usage();
			System.exit(1);
		}
		
		// Read in the TCPDump file...
		TCPDumpReader reader = new TCPDumpReader(fileName);
		
		// Determine the output file name and create a FileLogger for saving the results...
		String outFileName = fileName.substring(0, fileName.indexOf("."));
		if (sa == null) {
			outFileName += "-rssi-all.txt";
		} else {
			outFileName += "-rssi-" + Long.toHexString(sa) + ".txt";
		}
		FileLogger flogger = new FileLogger(outFileName, false);
		
		// Determine the number of relevant beacons in the log file...
		int sampleSize = 0;
		long startTime = -1;
		
		Enumeration<TCPDumpRecord> e = reader.elements();
		while (e.hasMoreElements()) {
			TCPDumpRecord currRec = e.nextElement();
			
			// If we are only interested in beacons sent by a specific robot...
			if (sa != null) {
				// If the beacon was sent by said robot...
				if (currRec.getSourceAddr() == sa) {
					sampleSize++;
					if (startTime == -1) startTime = currRec.getTimeStamp();
				}
			} else {
				sampleSize++;
				if (startTime == -1) startTime = currRec.getTimeStamp();
			}
		}
		
		System.out.println("Saving results to: " + outFileName + ", sample size = " + sampleSize);
		
		flogger.log("File: " + fileName);
		flogger.log("Sample Size: " + sampleSize);
		flogger.log("Timestamp (us)\tDelta Time (s)\tSource MAC Address\tNetwork Name\tRSSI (dBm)");
		
		e = reader.elements();
		while (e.hasMoreElements()) {
			TCPDumpRecord currRec = e.nextElement();
			
			// If we are only interested in beacons sent by a specific robot...
			if (sa != null) {
				// If the beacon was sent by said robot...
				if (currRec.getSourceAddr() == sa) {
					logBeacon(currRec, startTime, flogger);
				}	
			} else {
				logBeacon(currRec, startTime, flogger);
			}
		}
	}
}
