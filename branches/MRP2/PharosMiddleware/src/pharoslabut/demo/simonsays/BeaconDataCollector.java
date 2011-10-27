package pharoslabut.demo.simonsays;

import java.util.ArrayList;

import pharoslabut.logger.FileLogger;

public class BeaconDataCollector {

	static FileLogger flogger;
	static long startTime = 0;
	public static boolean running = false;
	public static ArrayList<BeaconReading> readings = new ArrayList<BeaconReading>();
	
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
	
	public void newBeaconData(BeaconReading br) {
		System.out.println("Received new BeaconReading br.");
		readings.add(br);
	}
	
	public BeaconReading getLastBeaconReading() {
		if (readings.isEmpty())
			return null;
		else
			return readings.get(readings.size() - 1); // last element
	}
		
}
