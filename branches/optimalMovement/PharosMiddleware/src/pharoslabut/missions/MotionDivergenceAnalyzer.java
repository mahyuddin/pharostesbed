package pharoslabut.missions;

import java.util.*;
//import java.io.*;
import pharoslabut.navigate.*;
import pharoslabut.logger.*;
import pharoslabut.logger.analyzer.*;
//import playerclient.structures.gps.PlayerGpsData;

/**
 * Reads the Pharos Middleware log file and calculates the robot's divergence using three methods:
 * absolute, relative, and relative+speed.
 * 
 * @author Chien-Liang Fok
 */
public class MotionDivergenceAnalyzer {
	
	/**
	 * The constructor.
	 */
	public MotionDivergenceAnalyzer() throws Exception {
		
		Vector<String> logFileNames = new Vector<String>();
		Vector<String> filePrefixes = new Vector<String>();
		
		/*
		 * Analyze the log files of robot Lonestar
		 */
		String robotName = "Lonestar";
		
		logFileNames.add("M9/M9-Results/M9-Exp9/M9-Exp9-Lonestar_20101203100206.log");
		filePrefixes.add("M9-Exp9-Lonestar");
		
		logFileNames.add("M9/M9-Results/M9-Exp10/M9-Exp10-Lonestar_20101203102922.log");
		filePrefixes.add("M9-Exp10-Lonestar");
		
		logFileNames.add("M9/M9-Results/M9-Exp11/M9-Exp11-Lonestar_20101203111154.log");
		filePrefixes.add("M9-Exp11-Lonestar");
		
		logFileNames.add("M9/M9-Results/M9-Exp12/M9-Exp12-Lonestar_20101203114724.log");
		filePrefixes.add("M9-Exp12-Lonestar");
		
		logFileNames.add("M11/M11-Results/M11-Exp1/M11-Exp1-Lonestar_20101206054444.log");
		filePrefixes.add("M11-Exp1-Lonestar");
		
		logFileNames.add("M11/M11-Results/M11-Exp3/M11-Exp3-Lonestar_20101206064809.log");
		filePrefixes.add("M11-Exp3-Lonestar");
		
		logFileNames.add("M11/M11-Results/M11-Exp4/M11-Exp4-Lonestar_20101206071859.log");
		filePrefixes.add("M11-Exp4-Lonestar");
		
		
//		String robotName = "Shiner";
//		
//		logFileNames.add("M9/M9-Results/M9-Exp1/M9-Exp1-Shiner_20101203123303.log");
//		filePrefixes.add("M9-Exp1-Shiner");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp2/M9-Exp2-Shiner_20101203130807.log");
//		filePrefixes.add("M9-Exp2-Shiner");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp3/M9-Exp3-Shiner_20101203132921.log");
//		filePrefixes.add("M9-Exp3-Shiner");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp5/M9-Exp5-Shiner_20101203135855.log");
//		filePrefixes.add("M9-Exp5-Shiner");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp6/M9-Exp6-Shiner_20101203142245.log");
//		filePrefixes.add("M9-Exp6-Shiner");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp7/M9-Exp7-Shiner_20101203143934.log");
//		filePrefixes.add("M9-Exp7-Shiner");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp9/M9-Exp9-Shiner_20101203160528.log");
//		filePrefixes.add("M9-Exp9-Shiner");
				
//		String robotName = "Wynkoop";
//		
//		logFileNames.add("M9/M9-Results/M9-Exp9/M9-Exp9-Wynkoop_20101203100336.log");
//		filePrefixes.add("M9-Exp9-Wynkoop");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp10/M9-Exp10-Wynkoop_20101203103051.log");
//		filePrefixes.add("M9-Exp10-Wynkoop");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp11/M9-Exp11-Wynkoop_20101203111323.log");
//		filePrefixes.add("M9-Exp11-Wynkoop");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp12/M9-Exp12-Wynkoop_20101203114853.log");
//		filePrefixes.add("M9-Exp12-Wynkoop");
//		
//		logFileNames.add("M11/M11-Results/M11-Exp3/M11-Exp3-Wynkoop_20101206064822.log");
//		filePrefixes.add("M11-Exp3-Wynkoop");
//		
//		logFileNames.add("M11/M11-Results/M11-Exp4/M11-Exp4-Wynkoop_20101206071912.log");
//		filePrefixes.add("M11-Exp4-Wynkoop");
//		
//		logFileNames.add("M11/M11-Results/M11-Exp5/M11-Exp5-Wynkoop_20101206075054.log");
//		filePrefixes.add("M11-Exp5-Wynkoop");
		
		
		/*
		 * Manny cannot be used because its data from mission 8 does not contain GPS buffer information,
		 * which this analyzer depends on to determine when a robot receives location coordinates.
		 */
//		String robotName = "Manny";
//		
//		logFileNames.add("M8/M8-Results/M8-Exp3/M8-Exp3-Manny_20101202122133.log");
//		filePrefixes.add("M8-Exp3-Manny");
//		
//		logFileNames.add("M8/M8-Results/M8-Exp4/M8-Exp4-Manny_20101202123755.log");
//		filePrefixes.add("M8-Exp4-Manny");
//		
//		logFileNames.add("M8/M8-Results/M8-Exp15/M8-Exp15-Manny_20101202172524.log");
//		filePrefixes.add("M8-Exp15-Manny");
//		
//		logFileNames.add("M8/M8-Results/M8-Exp16/M8-Exp16-Manny_20101202174416.log");
//		filePrefixes.add("M8-Exp16-Manny");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp9/M9-Exp9-Manny_20101203160216.log");
//		filePrefixes.add("M9-Exp9-Manny");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp10/M9-Exp10-Manny_20101203162932.log");
//		filePrefixes.add("M9-Exp10-Manny");
//		
//		logFileNames.add("M9/M9-Results/M9-Exp12/M9-Exp12-Manny_20101203174734.log");
//		filePrefixes.add("M9-Exp12-Manny");
		
		/*
		 * Read in all of the log files and analyze them.
		 */
		Vector<ExpData> expData = new Vector<ExpData>();
		Vector<AnalysisResults> resultsData = new Vector<AnalysisResults>();
		for (int i = 0; i < logFileNames.size(); i++) {
			ExpData ed = LogFileReader.readLogFile(logFileNames.get(i));
			expData.add(ed);
			resultsData.add(analyzeFile(ed, filePrefixes.get(i)));
		}
		
		FileLogger flogger = new FileLogger(robotName + "-AverageDivergence.txt", false /* print time stamp */);
		
		StringBuffer tableHeader = new StringBuffer();
		tableHeader.append("Pct Complete");
		for (int i=0; i < filePrefixes.size(); i++) {
			tableHeader.append("\t" + filePrefixes.get(i));
		}
		
		int numWayPoints = resultsData.get(0).resultAbsoluteDivergence.size();
		
		log(flogger, "Absolute Divergence: ");
		for (int wayPoint = 0; wayPoint < numWayPoints; wayPoint++) {
			log(flogger, "To way point " + (wayPoint+1) + ":");
			log(flogger, tableHeader.toString());
			
			for (int pctComplete = 0; pctComplete <= 100; pctComplete += 10) {
				StringBuffer sb = new StringBuffer();
				sb.append((pctComplete));
				for (int i=0; i < resultsData.size(); i++) {
					sb.append("\t" + resultsData.get(i).resultAbsoluteDivergence.get(wayPoint)
							.getInterpolatedResults(pctComplete));
				}
				log(flogger, sb.toString());
			}
		}
		
		
		log(flogger, "Relative Divergence: ");
		for (int wayPoint = 0; wayPoint < numWayPoints; wayPoint++) {
			log(flogger, "To way point " + (wayPoint+1) + ":");
			log(flogger, tableHeader.toString());
			
			for (int pctComplete = 0; pctComplete <= 100; pctComplete += 10) {
				StringBuffer sb = new StringBuffer();
				sb.append((pctComplete));
				for (int i=0; i < resultsData.size(); i++) {
					sb.append("\t" + resultsData.get(i).resultRelativeDivergence.get(wayPoint)
							.getInterpolatedResults(pctComplete));
				}
				log(flogger, sb.toString());
			}
		}
		
		log(flogger, "Relative Speed Divergence: ");
		for (int wayPoint = 0; wayPoint < numWayPoints; wayPoint++) {
			log(flogger, "To way point " + (wayPoint+1) + ":");
			log(flogger, tableHeader.toString());
			
			for (int pctComplete = 0; pctComplete <= 100; pctComplete += 10) {
				StringBuffer sb = new StringBuffer();
				sb.append((pctComplete));
				for (int i=0; i < resultsData.size(); i++) {
					sb.append("\t" + resultsData.get(i).resultRelativeSpeedDivergence.get(wayPoint)
							.getInterpolatedResults(pctComplete));
				}
				log(flogger, sb.toString());
			}
		}
	}
	

	
	class AnalysisResults {
		Vector<ResultEdge> resultAbsoluteDivergence = new Vector<ResultEdge>();
		Vector<ResultEdge> resultRelativeDivergence = new Vector<ResultEdge>();
		Vector<ResultEdge> resultRelativeSpeedDivergence = new Vector<ResultEdge>();
		
		public AnalysisResults() {
			
		}
	}
	
	/**
	 * Analyze a log file in three different ways: absolute, relative, and relative+speed.
	 * 
	 * @param prefix The prefix to be added to the beginning of the file names that contain
	 * the final results.
	 */
	private AnalysisResults analyzeFile(ExpData expData, String prefix) {
		System.out.println("Analyzing file: " + expData.getFileName());
		
		FileLogger flogger = new FileLogger(prefix + "-MotionDivergenceAbsolute.txt", false /* print time stamp */);
		Vector<PathEdge> pathHistory = expData.getPathHistory();
		AnalysisResults results = new AnalysisResults();
		
		for (int i=0; i < pathHistory.size(); i++) {
			log(flogger, "Absolute analysis of path to way point " + (i+1) + " ...");
			ResultEdge edgeResult = analyzeEdgeAbsoluteDivergence(pathHistory.get(i));
			log(flogger, edgeResult.toString());
			results.resultAbsoluteDivergence.add(edgeResult);
		}
		
		flogger = new FileLogger(prefix + "-MotionDivergenceRelative.txt", false /* print time stamp */);
		for (int i=0; i < pathHistory.size(); i++) {
			log(flogger, "Relative analysis of path to way point " + (i+1) + "...");
			ResultEdge edgeResult = analyzeEdgeRelativeDivergence(pathHistory.get(i));
			log(flogger, edgeResult.toString());
			results.resultRelativeDivergence.add(edgeResult);
		}
		
		flogger = new FileLogger(prefix + "-MotionDivergenceRelativeSpeed.txt", false /* print time stamp */);
		for (int i=0; i < pathHistory.size(); i++) {
			log(flogger, "Relative+Speed analysis of path to way point " + (i+1) + "...");
			ResultEdge edgeResult = analyzeEdgeRelativeDivergenceSpeed(pathHistory.get(i));
			log(flogger, edgeResult.toString());
			results.resultRelativeSpeedDivergence.add(edgeResult);
		}
		return results;
	}
	
	/**
	 * Calculates the position error relative to the perfect route, which is the
	 * straight line from the starting waypoint to the destination waypoint.  The
	 * distance is the segment of line that is perpendicular to the ideal line
	 * from the point of intersection with the ideal line to the current location
	 * of the robot.
	 * 
	 * @param edge The edge to analyze.
	 * @param flogger The FileLogger with which to log the results of the analysis.
	 */
	private ResultEdge analyzeEdgeAbsoluteDivergence(PathEdge edge) {
		Location startLoc = edge.getStartLoc();
		
		if (startLoc == null) {
			System.err.println("Unknown start location in path edge:\n" + edge);
			System.exit(-1);
		}
		
		// First create the line connecting the start location and end location.
		// This is the most ideal path the robot should travel.
		Line perfectRoute = new Line(startLoc, edge.getDest());
		
		/*
		 * Calculate
		 */
		long edgeEndTime = edge.getNormalizedEndTime() / 1000;
		
		// Some debug tests...
//		System.out.println("Equation of Perfect Route: " + perfectRoute);
//		System.out.println("Check getLongitude: got " + perfectRoute.getLongitude(startLoc.latitude()) + ", expected " + startLoc.longitude());
//		System.out.println("Check getLatitude: got " + perfectRoute.getLatitude(startLoc.longitude()) + ", expected " + startLoc.latitude());
	
		//log(flogger, "GPS Measurement\tTime(s)\tPcnt Complete\tDivergence (Absolute)");
	
		Vector<ResultDatumEdge> result = new Vector<ResultDatumEdge>();
		for (int i = 0; i < edge.numLocations(); i++) {
			GPSLocationState currGpsLoc = edge.getLocation(i);
			Location currLoc = new Location(currGpsLoc.getLoc());
			double distToOptimal = perfectRoute.shortestDistanceTo(currLoc);
			long time = (currGpsLoc.getTimeStamp() - edge.getAbsoluteStartTime())/1000;
			double pctComplete = (((double)time / (double)edgeEndTime) * 100.0);
			//System.out.println("time=" + time + ", edgeEndTime=" + edgeEndTime);
			result.add(new ResultDatumEdge(i+1, time, pctComplete, distToOptimal));
			
			//log(flogger, (i+1) + "\t" + time + "\t" + pctComplete + "\t" + distToOptimal);
		}
		return new ResultEdge(result);
	}
	
	/**
	 * Calculates the position error relative to the perfect route, which is the
	 * straight line from where it's previously known location was to the destination.
	 * Note that the perfect route is updated each time a new GPS datapoint arrives.
	 * 
	 * @param edge The edge to analyze.
	 */
	private ResultEdge analyzeEdgeRelativeDivergence(PathEdge edge) {
		Location startLoc = edge.getStartLoc();
		
		/*
		 * The initial perfect route is the straight line from the start position
		 * to the final destination.
		 */
		Line perfectRoute = new Line(startLoc, edge.getDest());
		
		long edgeEndTime = edge.getNormalizedEndTime() / 1000;
		
		//log(flogger, "GPS Measurement\tTime(s)\tPcnt Complete\tDivergence (Relative)");
		
		Vector<ResultDatumEdge> result = new Vector<ResultDatumEdge>();
		for (int i = 0; i < edge.numLocations(); i++) {
			GPSLocationState currGpsLoc = edge.getLocation(i);
			Location currLoc = new Location(currGpsLoc.getLoc());
			long time = (currGpsLoc.getTimeStamp() - edge.getAbsoluteStartTime())/1000;
			double pctComplete = (((double)time / (double)edgeEndTime) * 100.0);
			double distToOptimal = perfectRoute.shortestDistanceTo(currLoc);
			
			System.out.println("analyzeEdgeRelativeDivergence: Adding result: " + pctComplete + " " + distToOptimal);
			if (Double.isNaN(distToOptimal)) {
				System.err.println("invalid distToOptimal, perfect route: " + perfectRoute + ", currLoc: " + currLoc);
				System.exit(0);
			}
			
			result.add(new ResultDatumEdge(i+1 /* GPS Measurement Count */, time, pctComplete, distToOptimal));
			
			/*
			 * For the relative-divergence analysis, the perfect route is updated to be the
			 * straight line from the robot's previous location to the final destination.
			 * This is "fairer" since the robot is always trying to get to the final destination
			 * from is present location and not necessarily follow the straight line from the
			 * edge's start location to final location.
			 */
			perfectRoute = new Line(currLoc, edge.getDest());
			
			// Some debug output
//			System.out.println("currLoc: " + currLoc);
//			System.out.println("destLoc: " + edge.getDest());
//			System.out.println("Updating perfect route to be: " + perfectRoute);
		}
		return new ResultEdge(result);
	}
	
	/**
	 * Calculates the position error relative to if the robot had traveled along the perfect route, 
	 * at the speed in which it was traveling.  The perfect route is the
	 * straight line from where it's previously known location was to the destination.
	 * The perfect route is updated each time a new GPS data point arrives, and the
	 * speed is calculated as the average speed between GPS data points.
	 * 
	 * @param edge The edge to analyze.
	 */
	private ResultEdge analyzeEdgeRelativeDivergenceSpeed(PathEdge edge) {
		Location startLoc = edge.getStartLoc();
		//long prevTime = edge.getAbsoluteStartTime();
		long edgeEndTime = edge.getNormalizedEndTime() / 1000;
		
		/*
		 * The initial perfect route is the straight line from the start position
		 * to the final destination.
		 */
		Line perfectRoute = new Line(startLoc, edge.getDest());
		
		//log(flogger, "GPS Measurement\tTime(s)\tPcnt Complete\tDivergence (Relative)");
		Vector<ResultDatumEdge> result = new Vector<ResultDatumEdge>();
		for (int i = 0; i < edge.numLocations(); i++) {
			GPSLocationState currGpsLoc = edge.getLocation(i);
			Location currLoc = new Location(currGpsLoc.getLoc());
			
			long time = (currGpsLoc.getTimeStamp() - edge.getAbsoluteStartTime())/1000;
			
			double pctComplete = (((double)time / (double)edgeEndTime) * 100.0);
			
			/*
			 * Calculate the distance between the previous GPS coordinate to the current one.
			 * This will be used to determine where the robot would be if it had traveled
			 * the exact same distance but along the perfect path towards the destination.
			 */
			double deltaDist = perfectRoute.getStartLoc().distanceTo(currLoc);
			
			double distToOptimal = perfectRoute.shortestDistanceTo(currLoc, deltaDist);
			
			result.add(new ResultDatumEdge(i+1, time, pctComplete, distToOptimal));
			//log(flogger, (i+1) + "\t" + time + "\t" + pctComplete + "\t" + distToOptimal);
			
			/*
			 * For the relative-divergence analysis, the perfect route is updated to be the
			 * straight line from the robot's previous location to the final destination.
			 * This is "fairer" since the robot is always trying to get to the final destination
			 * from is present location and not necessarily follow the straight line from the
			 * edge's start location to final location.
			 */
			perfectRoute = new Line(currLoc, edge.getDest());
			//prevTime = currGpsLoc.getTimeStamp();
			
			// Some debug output
//			System.out.println("currLoc: " + currLoc);
//			System.out.println("destLoc: " + edge.getDest());
//			System.out.println("Updating perfect route to be: " + perfectRoute);
		}
		return new ResultEdge(result);
	}
	
//	private void printMotionPath(ExpData expData, String logFileName) {
//		FileLogger flogger = new FileLogger("test.txt", false /* print time stamp */);
//		log(flogger, "Analysis of " + logFileName);
//		log(flogger, "Number of edges: " + expData.getPathHistory().size());
//		
//		for (int i = 0; i < expData.getPathHistory().size(); i++) {
//			log(flogger, "\nPath Edge " + (i+1) + ":");
//			log(flogger, expData.getPathHistory().get(i).toString());
//		}
//	}
	
	private void log(FileLogger flogger, String message) {
		System.out.println(message);
		flogger.log(message);
	}
	
	class ResultEdge {
		
		/**
		 * The divergence when the actual GPS data is received.
		 */
		Vector<ResultDatumEdge> actualResults;
		
		/**
		 * The divergence at specific percentage traversal completions.
		 * The index * 10 is the percent complete, e.g., index = 1 means
		 * 10% complete.
		 */
		Vector<Double> interpolatedResults = new Vector<Double>();
		
		/**
		 * The constructor.
		 * 
		 * @param actualResults
		 */
		public ResultEdge(Vector<ResultDatumEdge> actualResults) {
			this.actualResults = actualResults;
			
			// Interpolates the divergence at fixed percentage completions (E.g., 0%, 10%, 20%, etc.).
			for (int i = 0; i <= 100; i += 10) {
				interpolatedResults.add(interpolateDivergence(i));
			}
		}
		
		public Vector<ResultDatumEdge> getActualResults() {
			return actualResults;
		}
		
		public Vector<Double> getInterpolatedResults() {
			return interpolatedResults;
		}
		
		public double getInterpolatedResults(int pctComplete) {
			return interpolatedResults.get(pctComplete/10);
		}
		
		public int numActualDataPoints() {
			return actualResults.size();
		}
		
		private double interpolateDivergence(double pcntDone) {
			int beforeIndx = 0; // the index of the ResultDatumEdge that is immediately before pcntDone
			int afterIndx = 0; // the index of the ResultDatumEdge that is immediately after pctntDone
			boolean afterIndxFound = false;
			
			for (int i=0; i < actualResults.size(); i++) {
				ResultDatumEdge currDataPoint = actualResults.get(i);
				if (currDataPoint.getPctComplete() <= pcntDone)
					beforeIndx = i;
				if (!afterIndxFound && currDataPoint.getPctComplete() >= pcntDone) {
					afterIndxFound = true;
					afterIndx = i;
				}
			}
			
			if (beforeIndx == afterIndx)
				return actualResults.get(beforeIndx).getDivergence();
			else {
				// Now we need to interpolate, create a line that passes through the
				// points before and after pcntDone, then derive what the divergence
				// would be at the pctnt done.
				Location bLoc = new Location(actualResults.get(beforeIndx).getDivergence(), 
						actualResults.get(beforeIndx).getPctComplete());
				Location aLoc = new Location(actualResults.get(afterIndx).getDivergence(),
						actualResults.get(afterIndx).getPctComplete());
				Line l = new Line(bLoc, aLoc);
				return l.getLatitude(pcntDone);
			}
		}
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("Actual Data:\n");
			sb.append("GPS Measurement\tTime(s)\tPcnt Complete\tDivergence\n");
			for (int i=0; i < actualResults.size(); i++) {
				sb.append(actualResults.get(i).toString() + "\n");
			}
			sb.append("Interpolated Data:\n");
			sb.append("Pcnt Complete\tDivergence\n");
			for (int i=0; i < interpolatedResults.size(); i++) {
				sb.append((i*10) + "\t" + interpolatedResults.get(i) + "\n");
			}
			return sb.toString();
		}
	}
	
	/**
	 * Contains a single result of an analyzed edge.
	 *
	 */
	class ResultDatumEdge {
		/**
		 * A running count of received GPS data.
		 */
		int gpsDataNumber;
		
		/**
		 * The time since the robot began traversing this edge.
		 */
		long time;
		
		/**
		 * The percentage of the edge that the robot has completed traversing.
		 */
		double pctComplete;
		
		/**
		 * How far away from the ideal location the robot is at.
		 */
		double divergence;
		
		/**
		 * The constructor.
		 * 
		 * @param gpsDataNumber
		 * @param time
		 * @param pctComplete
		 * @param divergence
		 */
		public ResultDatumEdge(int gpsDataNumber, long time, double pctComplete, double divergence) {
			this.gpsDataNumber = gpsDataNumber;
			this.time = time;
			this.pctComplete = pctComplete;
			this.divergence = divergence;
		}
		
		public double getPctComplete() {
			return pctComplete;
		}
		
		public double getDivergence() {
			return divergence;
		}
		
		public String toString() {
			return gpsDataNumber + "\t" + time + "\t" + pctComplete + "\t" + divergence;
		}
	}
	
	
	public static final void main(String[] args) {
		try {
			new MotionDivergenceAnalyzer();	
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	} 
}

