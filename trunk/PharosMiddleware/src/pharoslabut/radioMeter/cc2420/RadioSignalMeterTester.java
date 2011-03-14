package pharoslabut.radioMeter.cc2420;

import pharoslabut.logger.*;

public class RadioSignalMeterTester {

	public RadioSignalMeterTester() {
		
		FileLogger flogger = new FileLogger("RadioSignalMeterTester-" 
				+ FileLogger.getUniqueNameExtension() + ".log");
		
		TelosBeaconBroadcaster rsm;
		try {
			rsm = new TelosBeaconBroadcaster();
			rsm.setFileLogger(flogger);
			long minPeriod = 1000, maxPeriod = 4000;
			rsm.start(minPeriod, maxPeriod);
		} catch (TelosBeaconException e) {
			String msg = "Failed to start CC2420 radio signal meter.";
			flogger.log(msg);
			System.err.println(msg);
		}
	}
	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(msg);
	}
	
	private static void usage() {
		print("Usage: pharoslabut.radiometer.cc2420 <options>\n");
		print("Where <options> include:");
		print("\t-debug: enable debug mode");
	}
	
	public static void main(String[] args) {
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				}

				else {
					System.setProperty ("PharosMiddleware.debug", "true");
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			usage();
			System.exit(1);
		}
		
		new RadioSignalMeterTester();
	}
}
