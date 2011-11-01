package pharoslabut.radioMeter.cc2420;

import pharoslabut.logger.*;

public class RadioSignalMeterTester {

	public RadioSignalMeterTester() {
		
		Logger.setFileLogger(new FileLogger("RadioSignalMeterTester-" 
				+ FileLogger.getUniqueNameExtension() + ".log", false));;
		
		TelosBeaconBroadcaster tbb = null;
		try {
			tbb = new TelosBeaconBroadcaster();
//			tbb.setFileLogger(flogger);
			long minPeriod = 1000, maxPeriod = 4000;
			tbb.start(minPeriod, maxPeriod, TelosBeaconBroadcaster.TX_PWR_MAX);
		} catch (TelosBeaconException e) {
			String msg = "Failed to start CC2420 radio signal meter.";
			Logger.logErr(msg);
			System.err.println(msg);
			System.exit(1);
		}
		
		synchronized(this) {
			Logger.log("Waiting 10s...");
			try {
				wait(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		Logger.log("Shutting down beacons...");
		tbb.stop();
		
		synchronized(this) {
			Logger.log("Waiting another 10s before shutting down...");
			try {
				wait(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		
	}
	
	private static void print(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(msg);
	}
	
	private static void usage() {
		System.setProperty ("PharosMiddleware.debug", "true");
		print("Usage: " +  RadioSignalMeterTester.class.getName() + " <options>\n");
		print("Where <options> include:");
		print("\t-debug: enable debug mode");
		System.exit(0);
	}
	
	public static void main(String[] args) {
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-debug") || args[i].equals("-d"))
					System.setProperty ("PharosMiddleware.debug", "true");
				else
					usage();
			}
		} catch(Exception e) {
			e.printStackTrace();
			usage();
		}
		
		new RadioSignalMeterTester();
	}
}
