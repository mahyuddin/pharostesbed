package pharoslabut.missions;

import java.io.*;
import java.util.*;

import pharoslabut.logger.*;
import pharoslabut.logger.analyzer.*;
import pharoslabut.logger.analyzer.tcpdump.*;
import pharoslabut.navigate.*;

/**
 * Processes the data from MM11Exp4.
 * 
 * @author Chien-Liang Fok
 */
public class MM11Exp4 {

	Location shinerLoc = new Location(30.4461433, -97.7647967);
	Vector<RobotLoc> wynkoopLocs = new Vector<RobotLoc>();
	FileLogger flogger;
	
	public MM11Exp4() throws Exception {
		
		// Read in the locations of Wynkoop
		File file = new File("MM11-Exp4-gps.log");
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
		
		//int lineno = 0;
		while (( line = br.readLine()) != null) {
			if (!line.contains("Time")) {
				String[] tokens = line.split("\\s+");
				long timestamp = Long.valueOf(tokens[2]);
				Location loc = new Location(Double.valueOf(tokens[5]), Double.valueOf(tokens[6]));
				wynkoopLocs.add(new RobotLoc(timestamp, loc));
				//lineno++;
			}
		}

		// Read in the RSSI values...
		TCPDumpReader tcpDump = new TCPDumpReader("MM11-Exp4.tcpdump.txt");
		
		// Start analysis at the first GPS reading of Wynkoop
		long startTime = wynkoopLocs.get(0).timestamp;
		
		FileLogger outLogger = new FileLogger("M11-Exp4-Results.txt");
		outLogger.log("Timestamp (us)\tDelta Time (s)\tDist (m)\tRSSI (dBm)");
		Enumeration<TCPDumpRecord> e = tcpDump.elements();
		while (e.hasMoreElements()) {
			TCPDumpRecord currRec = e.nextElement();
			
			// If the sender is the other robot...
			if (currRec.getSourceAddr() == Long.valueOf("1bb1009a4d", 16)) {
				
				// If we have GPS data...
				if (currRec.getTimeStamp() > startTime) {
					
					// Get the GPS location of Wynkoop at the packet's time stamp
					Location wynkoopLoc = getWynkoopLoc(currRec.getTimeStamp());
					
					// Calculate the distance between Wynkop and Shiner
					double dist = wynkoopLoc.distanceTo(shinerLoc);
					outLogger.log(currRec.getTimeStamp() + "\t" + (currRec.getTimeStamp() - startTime)/1000.0 + "\t"
							+ dist + "\t" + currRec.getRSSI());
				}
			}
		}
		
	}
	
	/**
	 * Gets Wynkoop's location at the specified time.
	 * 
	 * @param timestamp The specified time.
	 * @return Wynkoop's location at the specified time.
	 */
	private Location getWynkoopLoc(long timestamp) {
		// Get the indices of the locations before or after the desired timestamp
		int beforeIndx = 0; 
		int afterIndx = 0;
		
		boolean afterIndxFound = false;
		
		for (int i=0; i < wynkoopLocs.size(); i++) {
			RobotLoc currLocation = wynkoopLocs.get(i);
			if (currLocation.timestamp <= timestamp)
				beforeIndx = i;
			if (!afterIndxFound && currLocation.timestamp >= timestamp) {
				afterIndxFound = true;
				afterIndx = i;
			}
		}
		
		log("getWynkoopLoc: timestamp = " + timestamp + ", beforeIndx = " + beforeIndx + ", afterIndx = " + afterIndx, flogger);
		
		if (beforeIndx == afterIndx)
			return wynkoopLocs.get(beforeIndx).loc;
		else {
			RobotLoc bLoc = wynkoopLocs.get(beforeIndx);
			RobotLoc aLoc = wynkoopLocs.get(afterIndx);
			
			return RobotExpData.getInterpolatedLoc(
					bLoc.loc.latitude(), bLoc.loc.longitude(), bLoc.timestamp,
					aLoc.loc.latitude(), aLoc.loc.longitude(), aLoc.timestamp,
					timestamp, flogger);
		}
	}
	
	private class RobotLoc {
		long timestamp;
		Location loc;
		
		public RobotLoc(long timestamp, Location loc) {
			this.timestamp = timestamp;
			this.loc = loc;
		}
	}
	
	/**
	 * Sets the file logger for saving debug messages into a file.
	 * 
	 * @param flogger The file logger to use to save debug messages.
	 */
	public void setFileLogger(FileLogger flogger) {
		this.flogger = flogger;
	}
	
	private void log(String msg, FileLogger flogger) {
		System.out.println(msg);
		if (flogger != null)
			flogger.log(msg);
	}
	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(msg);
	}
	
	private static void usage() {
		System.setProperty ("PharosMiddleware.debug", "true");
		print("Usage: pharoslabut.missions.MM11Exp4 <options>\n");
		print("Where <options> include:");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		
		// Process the command line arguments...
		try {
			for (int i=0; i < args.length; i++) {
//				if (args[i].equals("-file"))
//					fileName = args[++i];
//				else 
				if (args[i].equals("-debug") || args[i].equals("-d"))
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
	
		try {
			new MM11Exp4();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
