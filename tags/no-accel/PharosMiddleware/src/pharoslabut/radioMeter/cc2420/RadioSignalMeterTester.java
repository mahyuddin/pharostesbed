package pharoslabut.radioMeter.cc2420;

import pharoslabut.logger.*;

public class RadioSignalMeterTester {

	public RadioSignalMeterTester() {
		
		FileLogger flogger = new FileLogger("RadioSignalMeterTester-" 
				+ FileLogger.getUniqueNameExtension() + ".log");
		
		TelosBeaconBroadcaster tbb = null;
		try {
			tbb = new TelosBeaconBroadcaster();
			tbb.setFileLogger(flogger);
			long minPeriod = 1000, maxPeriod = 4000;
			tbb.start(minPeriod, maxPeriod, TelosBeaconBroadcaster.TX_PWR_MAX);
		} catch (TelosBeaconException e) {
			String msg = "Failed to start CC2420 radio signal meter.";
			flogger.log(msg);
			System.err.println(msg);
			System.exit(1);
		}
		
		synchronized(this) {
			System.out.println("Waiting 10s...");
			try {
				wait(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Shutting down beacons...");
		tbb.stop();
		
		synchronized(this) {
			System.out.println("Waiting another 10s before shuttind down...");
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
		print("Usage: pharoslabut.radiometer.cc2420 <options>\n");
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
