package pharoslabut.missions;

import java.util.*;
import java.io.*;
import pharoslabut.logger.*;

/**
 * Reads the Pharos Middleware log file and analyzes the difference between the
 * current compass heading and the heading used to by 
 * pharoslabut.navigate.NavigateCompassGPS, which was selected through a median
 * filter in pharoslabut.navigate.CompassDataBuffer.
 * 
 * @author Chien-Liang Fok
 * @see pharoslabut.navigate.NavigateCompassGPS
 * @see pharoslabut.navigate.CompassDataBuffer
 */
public class M10ExpResultsHeadingAnalyzer {

	/**
	 * This contains the latest heading information from the compass.
	 */
	private Vector<HeadingState> latestHeading = new Vector<HeadingState>();
	
	/**
	 * This is the filtered heading that is used by pharoslabut.navigate.NavigateCompassGPS.
	 */
	private Vector<HeadingState> filteredHeading = new Vector<HeadingState>();
	
	
	/**
	 * The constructor.
	 */
	public M10ExpResultsHeadingAnalyzer() throws Exception {
		String m10Exp2logfile = "M10/M10-Results/M10-Exp2/M10-Exp2-Lonestar_20101204111012.log";
		
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
				line = br.readLine();
				
				tokens = line.split("[: ]");
				
				filteredHeading.add(new HeadingState(timeStamp, Double.valueOf(tokens[3])));
//				for (int i=0; i < tokens.length; i++) {
//					System.out.println(i + ": " + tokens[i]);
//				}
			}
			else if (line.contains("CompassDataBuffer: New heading:")) {
				String[] tokens = line.split("[:, ]");
//				for (int i=0; i < tokens.length; i++) {
//					System.out.println(i + ": " + tokens[i]);
//				}
				long timeStamp = Long.valueOf(tokens[0].substring(1, tokens[0].length()-1));
				HeadingState hs = new HeadingState(timeStamp, Double.valueOf(tokens[6]));
				latestHeading.add(hs);
				//System.out.println(hs.toString());
			}
		}
		
//		System.out.println("Filtered Headings:");
//		for (int i=0; i < filteredHeading.size(); i++) {
//			HeadingState hs = filteredHeading.get(i);
//			System.out.println(hs.toString());
//		}
		
		FileLogger flogger = new FileLogger("M10/M10-Results/M10-Exp2/M10-Exp2-DeltaHeading.log");
		flogger.log("Time\tFiltered Heading\tActual Heading\tDelta");
		// for each filtered heading, find the most recent actual heading, calculate the difference,
		// and print it to the screen
		for (int i=0; i < filteredHeading.size(); i++) {
			HeadingState filteredState = filteredHeading.get(i);
			HeadingState actualState = getLatestHeading(filteredState.getTimeStamp());
			String result = ((filteredState.getTimeStamp() - startTime)/1000.0) + "\t" + filteredState.getHeading() + "\t" + 
			actualState.getHeading() + "\t" + (filteredState.getHeading() - actualState.getHeading());
			flogger.log(result);
			System.out.println(result);
		}
	}
	
	/**
	 * Searches through the latestHeading vector and returns the HeadingState that is
	 * immediately before the specified timeStamp;
	 * 
	 * @param timeStamp
	 * @return
	 */
	private HeadingState getLatestHeading(long timeStamp) {
		HeadingState result = latestHeading.get(0);
		for (int i=1; i < latestHeading.size(); i++) {
			HeadingState curr = latestHeading.get(i);
			if (curr.getTimeStamp() > timeStamp)
				continue;
			else
				result = curr;
		}
		return result;
	}
	
	
	public static final void main(String[] args) {
		try {
			new M10ExpResultsHeadingAnalyzer();	
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	} 
}

