package pharoslabut.logger.analyzer;

import java.util.Vector;

import pharoslabut.logger.FileLogger;
import java.util.*;

/**
 * Extracts the RSSI data from a robot's log file.
 * It currently only supports the RSSI generated by the TelosB mote.
 * 
 * @author Chien-Liang Fok
 */
public class GetRSSIData {

	public GetRSSIData(String robotExpDataFile) {
		RobotExpData robotData = new RobotExpData(robotExpDataFile);
		
		String destFile = robotExpDataFile.substring(0, robotExpDataFile.indexOf(".")) + ".rssi";
		FileLogger flogger = new FileLogger(destFile);
		
		Vector<TelosBRxRecord> rxHist = robotData.getTelosBRxHist();
		Enumeration<TelosBRxRecord> e = rxHist.elements();
		while (e.hasMoreElements()) {
			TelosBRxRecord rec = e.nextElement();
			log(rec.toString(), flogger);
		}
		
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
		print("Usage: pharoslabut.logger.analyzer.GetRSSIData <options>\n");
		print("Where <options> include:");
		print("\t-robotFile <name of file containing robot data>: (required)");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		String expDir = null;
		
		// Process the command line arguments...
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-robotFile"))
					expDir = args[++i];
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
		
		if (expDir == null) {
			usage();
			System.exit(1);
		}
	
		new GetRSSIData(expDir);
	}
}