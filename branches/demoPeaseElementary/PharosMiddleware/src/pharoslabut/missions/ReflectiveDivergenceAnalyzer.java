package pharoslabut.missions;

import java.util.Vector;

import pharoslabut.logger.analyzer.*;
import pharoslabut.logger.*;
import pharoslabut.navigate.Location;

/**
 * Reads log files of experimental runs.  Does a percent-completion-based comparison between every pair combination
 * of motion scripts.
 * 
 * @author Chien-Liang Fok
 *
 */
public class ReflectiveDivergenceAnalyzer {

	public ReflectiveDivergenceAnalyzer() throws Exception {
		Vector<String> logFileNames = new Vector<String>();
		Vector<String> captionName = new Vector<String>();
		
		/*
		 * Analyze the log files of robot Lonestar
		 */
//		String robotName = "Lonestar";
//		
//		logFileNames.add("M9/M9-Results/M9-Exp9/M9-Exp9-Lonestar_20101203100206.log");
//		captionName.add("M9-Exp9-Lonestar");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp10/M9-Exp10-Lonestar_20101203102922.log");
//		captionName.add("M9-Exp10-Lonestar");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp11/M9-Exp11-Lonestar_20101203111154.log");
//		captionName.add("M9-Exp11-Lonestar");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp12/M9-Exp12-Lonestar_20101203114724.log");
//		captionName.add("M9-Exp12-Lonestar");
//		
//		logFileNames.add("M11/M11-Results/M11-Exp1/M11-Exp1-Lonestar_20101206054444.log");
//		captionName.add("M11-Exp1-Lonestar");
//		
//		logFileNames.add("M11/M11-Results/M11-Exp3/M11-Exp3-Lonestar_20101206064809.log");		
//		captionName.add("M11-Exp3-Lonestar");
//		
//		logFileNames.add("M11/M11-Results/M11-Exp4/M11-Exp4-Lonestar_20101206071859.log");
//		captionName.add("M11-Exp4-Lonestar");
		
//		String robotName = "Shiner";
//		
//		logFileNames.add("M9/M9-Results/M9-Exp1/M9-Exp1-Shiner_20101203123303.log");
//		captionName.add("M9-Exp1-Shiner");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp2/M9-Exp2-Shiner_20101203130807.log");
//		captionName.add("M9-Exp2-Shiner");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp3/M9-Exp3-Shiner_20101203132921.log");
//		captionName.add("M9-Exp3-Shiner");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp5/M9-Exp5-Shiner_20101203135855.log");
//		captionName.add("M9-Exp5-Shiner");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp6/M9-Exp6-Shiner_20101203142245.log");
//		captionName.add("M9-Exp6-Shiner");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp7/M9-Exp7-Shiner_20101203143934.log");
//		captionName.add("M9-Exp7-Shiner");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp9/M9-Exp9-Shiner_20101203160528.log");
//		captionName.add("M9-Exp9-Shiner");
		
		String robotName = "Wynkoop";
		
		logFileNames.add("M9/M9-Results/M9-Exp9/M9-Exp9-Wynkoop_20101203100336.log");
		captionName.add("M9-Exp9-Wynkoop");
		
		logFileNames.add("M9/M9-Results/M9-Exp10/M9-Exp10-Wynkoop_20101203103051.log");
		captionName.add("M9-Exp10-Wynkoop");
		
		logFileNames.add("M9/M9-Results/M9-Exp11/M9-Exp11-Wynkoop_20101203111323.log");
		captionName.add("M9-Exp11-Wynkoop");
		
		logFileNames.add("M9/M9-Results/M9-Exp12/M9-Exp12-Wynkoop_20101203114853.log");
		captionName.add("M9-Exp12-Wynkoop");
		
		logFileNames.add("M11/M11-Results/M11-Exp3/M11-Exp3-Wynkoop_20101206064822.log");
		captionName.add("M11-Exp3-Wynkoop");
		
		logFileNames.add("M11/M11-Results/M11-Exp4/M11-Exp4-Wynkoop_20101206071912.log");
		captionName.add("M11-Exp4-Wynkoop");
		
		logFileNames.add("M11/M11-Results/M11-Exp5/M11-Exp5-Wynkoop_20101206075054.log");
		captionName.add("M11-Exp5-Wynkoop");
		
		Vector<RobotExpData> allExpData = new Vector<RobotExpData>();
		
		
		for (int i=0; i < logFileNames.size(); i++) {
			log("Processing " + logFileNames.get(i));
			RobotExpData ed = new RobotExpData(logFileNames.get(i));
			allExpData.add(ed);
		}
		
		if (allExpData.size() == 0) {
			System.err.println("Insufficent data for comparison.");
			System.exit(-1);
		}
		
		// First generate the heading of each table.
		StringBuffer tableHeading = new StringBuffer();
		tableHeading.append("Pct Complete");
		for (int i = 0; i < captionName.size()-1; i++) {
			String goldStandard = captionName.get(i);
			for (int j = i+1; j < captionName.size(); j++) {
				tableHeading.append("\t" + goldStandard + " vs. " + captionName.get(j));
			}
			
		}
		
		
		FileLogger flogger = new FileLogger("ReflectiveDivergenceAnalyzer-" + robotName + ".txt", false /* print time stamp */);
		
		// For each edge in the motion script...
		for (int edgeIndx=0; edgeIndx < allExpData.get(0).numEdges(); edgeIndx++) {
			System.out.println("Processing edge " + (edgeIndx+1) + "...");
			
			flogger.log("To way point " + (edgeIndx+1) + ":");
			flogger.log(tableHeading.toString());
			
			// For every 10% of edge traversed...
			for (int pctComplete = 0; pctComplete <= 100; pctComplete += 10) {
				StringBuffer sb = new StringBuffer();
				sb.append(pctComplete);
				
				for (int goldenIndx = 0; goldenIndx < allExpData.size()-1; goldenIndx++) {
					PathEdge goldenEdge = allExpData.get(goldenIndx).getEdge(edgeIndx);
					Location goldLoc = goldenEdge.getLocationPct(pctComplete);	
					for (int i = goldenIndx + 1; i < allExpData.size(); i++) {
						Location actualLoc = allExpData.get(i).getEdge(edgeIndx).getLocationPct(pctComplete);
						double dist = goldLoc.distanceTo(actualLoc);
						sb.append("\t" + dist);
					}
					
				}
				
				flogger.log(sb.toString());
			}
		}
	}
	
	private void log(String msg) {
		System.out.println(msg);
	}
	
	public static void main(String[] args) {
		try {
			new ReflectiveDivergenceAnalyzer();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
