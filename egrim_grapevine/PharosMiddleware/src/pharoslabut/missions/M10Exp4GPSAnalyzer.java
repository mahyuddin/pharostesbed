package pharoslabut.missions;

import java.util.*;
import java.io.*;
import pharoslabut.navigate.*;
import pharoslabut.logger.*;

/**
 * Reads the Pharos Middleware log file and analyzes the GPS data that is used to by 
 * pharoslabut.navigate.NavigateCompassGPS.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.navigate.NavigateCompassGPS
 * @see pharoslabut.sensors.GPSDataBuffer
 */
public class M10Exp4GPSAnalyzer {

	/**
	 * This contains the location measurement from the GPS device.
	 */
	private Vector<LocationState> locationState = new Vector<LocationState>();
	
	
	/**
	 * The constructor.
	 */
	public M10Exp4GPSAnalyzer() throws Exception {
		String m10Exp2logfile = "M10/M10-Results/M10-Exp4/M10-Exp4-Lonestar_20101204113118.log";
		
		long startTime = 0;
		
		File file = new File(m10Exp2logfile);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
		while (( line = br.readLine()) != null){
			if (line.contains("Starting experiment at time:")) {
				String[] tokens = line.split("[: ]");
//				for (int i=0; i < tokens.length; i++) {
//					System.out.println(i + ": " + tokens[i]);
//				}
				startTime = Long.valueOf(tokens[8]);
			}
			else if (line.contains("GPSDataBuffer")) {
				String[] tokens = line.split("[:= ]");
				long timeStamp = Long.valueOf(tokens[0].substring(1, tokens[0].length()-1));
				double lat = Long.valueOf(tokens[13])/(1e7);
				double lon = Long.valueOf(tokens[15])/(1e7);
				locationState.add(new LocationState(timeStamp-startTime, new Location(lat, lon)));
				
//				for (int i=0; i < tokens.length; i++) {
//					System.out.println(i + ": " + tokens[i]);
//				}
			}
		}
		
		
		FileLogger flogger = new FileLogger("M10/M10-Results/M10-Exp4/M10-Exp4-DeltaGPS.log");
		
		Location prevLoc = locationState.get(0).getLoc();
//		System.out.println("Location Measurements:");
		for (int i=0; i < locationState.size(); i++) {
			LocationState ls = locationState.get(i);
			String result = ls.toString() + "\t" + ls.getLoc().distanceTo(prevLoc);
			System.out.println(result);
			flogger.log(result);
			prevLoc = ls.getLoc();
		}
	}
	
	
	public static final void main(String[] args) {
		try {
			new M10Exp4GPSAnalyzer();	
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	} 
}

