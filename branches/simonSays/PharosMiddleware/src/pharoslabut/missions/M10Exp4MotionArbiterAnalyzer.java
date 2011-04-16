package pharoslabut.missions;

import java.util.*;
import java.io.*;
import pharoslabut.logger.*;

/**
 * Reads the Pharos Middleware log file and analyzes the GPS data that is used to by 
 * pharoslabut.navigate.NavigateCompassGPS.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.navigate.NavigateCompassGPS
 * @see pharoslabut.navigate.GPSDataBuffer
 */
public class M10Exp4MotionArbiterAnalyzer {

	/**
	 * This contains the location measurement from the GPS device.
	 */
	private Vector<MotionArbiterState> motionArbiterState = new Vector<MotionArbiterState>();
	
	
	/**
	 * The constructor.
	 */
	public M10Exp4MotionArbiterAnalyzer() throws Exception {
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
			else if (line.contains("MotionArbiter: Sending the following motion command")) {
				String[] tokens = line.split("[:=, ]");
				long timeStamp = Long.valueOf(tokens[0].substring(1, tokens[0].length()-1));
				double speedCmd = Double.valueOf(tokens[10]);
				double steeringCmd = Double.valueOf(tokens[13]);
				motionArbiterState.add(new MotionArbiterState(timeStamp-startTime, speedCmd, steeringCmd));
				
//				for (int i=0; i < tokens.length; i++) {
//					System.out.println(i + ": " + tokens[i]);
//				}
			}
		}
		
		//MotionArbiterState prevState = motionArbiterState.get(0);
		
		FileLogger flogger = new FileLogger("M10/M10-Results/M10-Exp4/M10-Exp4-MotionArbiterState.log");
		for (int i=0; i < motionArbiterState.size(); i++) {
			MotionArbiterState mas = motionArbiterState.get(i);
			String result = mas.toString();
			System.out.println(result);
			flogger.log(result);
			
			//prevState = mas;
		}
	}
	
	
	public static final void main(String[] args) {
		try {
			new M10Exp4MotionArbiterAnalyzer();	
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	} 
}

