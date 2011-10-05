package pharoslabut.demo.simonsays;

import pharoslabut.logger.FileLogger;

public class BeaconDataCollector {

	static FileLogger flogger;
	static long startTime = 0;
	public static boolean running = false;
	
	public BeaconDataCollector(String fileName) {
		flogger = new FileLogger(fileName, false);
	}
	
	public void startTimer() {
		startTime = System.currentTimeMillis();
		running = true;
	}
	
	public void stopTimer() {
		running = false;
	}
	
	public void newBeaconData(long ts, double x, double y, double dist) {
		if (running) 
		{
			flogger.log((ts - startTime) + " , " + x + " , " + y + " , " + dist);
//			System.out.println("Cricket data: " + (ts - startTime) + " , " + x + " , " + y + " , " + dist);
		}
	}
		
}
