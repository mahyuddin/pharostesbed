package pharoslabut.missions;

import java.util.*;
import java.io.*;
import pharoslabut.logger.*;

/**
 * Reads the Pharos Middleware log file and analyzes the heading error.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.navigate.NavigateCompassGPS
 * @see pharoslabut.navigate.GPSDataBuffer
 */
public class M10Exp4HeadingErrorAnalyzer {

	/**
	 * This contains the heading errors as calculated by the Pharos Middleware
	 */
	private Vector<HeadingErrorState> headingErrorState = new Vector<HeadingErrorState>();
	
	/**
	 * This is the filtered heading that is used by pharoslabut.navigate.NavigateCompassGPS.
	 */
	private Vector<HeadingState> filteredHeading = new Vector<HeadingState>();
	
	/**
	 * This contains the location measurement from the GPS device.
	 */
	private Vector<MotionArbiterState> motionArbiterState = new Vector<MotionArbiterState>();
	
	/**
	 * The constructor.
	 */
	public M10Exp4HeadingErrorAnalyzer() throws Exception {
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
			else if (line.contains("Current State as of time")) {
				String[] tokens = line.split("[: ]");
				long timeStamp = Long.valueOf(line.split("[: ]")[9]);
				
				line = br.readLine();
				line = br.readLine();
				line = br.readLine();
				line = br.readLine();
				line = br.readLine();
				line = br.readLine();
				line = br.readLine(); // the current heading
				
				tokens = line.split("[: ]");
				filteredHeading.add(new HeadingState(timeStamp, Double.valueOf(tokens[3])));
				
				line = br.readLine(); // the heading error
				
				tokens = line.split("[: ]");
				
				headingErrorState.add(new HeadingErrorState(timeStamp, Double.valueOf(tokens[3])));
//				for (int i=0; i < tokens.length; i++) {
//					System.out.println(i + ": " + tokens[i]);
//				}
			}
			else if (line.contains("MotionArbiter: Sending the following motion command")) {
				String[] tokens = line.split("[:=, ]");
				long timeStamp = Long.valueOf(tokens[0].substring(1, tokens[0].length()-1));
				double speedCmd = Double.valueOf(tokens[10]);
				double steeringCmd = Double.valueOf(tokens[13]);
				motionArbiterState.add(new MotionArbiterState(timeStamp, speedCmd, steeringCmd));
				
//				for (int i=0; i < tokens.length; i++) {
//					System.out.println(i + ": " + tokens[i]);
//				}
			}
		}
		
		FileLogger flogger = new FileLogger("M10/M10-Results/M10-Exp4/M10-Exp4-HeadingErrorState.log");
		for (int i=0; i < headingErrorState.size(); i++) {
			HeadingErrorState hes = headingErrorState.get(i);
			String result = (hes.getTimeStamp() - startTime)/1000.0 + "\t" + hes.toString();
			System.out.println(result);
			flogger.log(result);
		}
		
		flogger = new FileLogger("M10/M10-Results/M10-Exp4/M10-Exp4-FilteredHeadingState.log");
		for (int i=0; i < filteredHeading.size(); i++) {
			HeadingState hs = filteredHeading.get(i);
			String result = (hs.getTimeStamp() - startTime)/1000.0 + "\t" + hs.toString();
			System.out.println(result);
			flogger.log(result);
		}
		
		flogger = new FileLogger("M10/M10-Results/M10-Exp4/M10-Exp4-MotionArbiterState.log");
		for (int i=0; i < motionArbiterState.size(); i++) {
			MotionArbiterState mas = motionArbiterState.get(i);
			String result = (mas.getTimeStamp() - startTime)/1000.0 + "\t" + mas.toString();
			System.out.println(result);
			flogger.log(result);
		}
	}
	
	
	public static final void main(String[] args) {
		try {
			new M10Exp4HeadingErrorAnalyzer();	
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	} 
}

