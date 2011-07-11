package pharoslabut.logger.analyzer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import pharoslabut.logger.FileLogger;
import pharoslabut.navigate.Location;
import pharoslabut.navigate.motionscript.MotionScript;

/**
 * Analyzes the motion divergence exhibit by one or more mobile nodes as they follow
 * a GPS waypoint-based motion script.
 * 
 * @author Chien-Liang Fok
 */
public class MotionDivergenceGPS {

	private String outputFileName = null;		// Where to save the results
	private MotionScript motionScript = null;	// The motion script used by all of experiments included in logFileNames
	private Vector<RobotExpData> robotExpData = new Vector<RobotExpData>();		// The log files
	private Vector<String> expLabels = new Vector<String>();	// The label for each log file
	
	/**
	 * The constructor.
	 * 
	 * @param specFile The name of the file containing the specifications on which log files to analyze.
	 */
	public MotionDivergenceGPS(String specFile) {
		readSpecFile(specFile); // if this fails, program will exit inside this call
		analyzeAbsoluteDivergence();
	}
		
//	class AnalysisResults {
//		Vector<ResultEdge> resultAbsoluteDivergence = new Vector<ResultEdge>();
//		Vector<ResultEdge> resultRelativeDivergence = new Vector<ResultEdge>();
//		Vector<ResultEdge> resultRelativeSpeedDivergence = new Vector<ResultEdge>();
//		
//		public AnalysisResults() {
//			
//		}
//	}
	
//	/**
//	 * Analyze a log file in three different ways: absolute, relative, and relative+speed.
//	 * 
//	 * @param prefix The prefix to be added to the beginning of the file names that contain
//	 * the final results.
//	 */
//	private AnalysisResults analyzeFile(RobotExpData expData, String prefix) {
//		System.out.println("Analyzing file: " + expData.getFileName());
//		
//		FileLogger flogger = new FileLogger(prefix + "-MotionDivergenceAbsolute.txt", false /* print time stamp */);
//		Vector<PathEdge> pathHistory = expData.getPathEdges();
//		AnalysisResults results = new AnalysisResults();
//		
//		for (int i=0; i < pathHistory.size(); i++) {
//			log(flogger, "Absolute analysis of path to way point " + (i+1) + " ...");
//			ResultEdge edgeResult = analyzeEdgeAbsoluteDivergence(expData, pathHistory.get(i));
//			log(flogger, edgeResult.toString());
//			results.resultAbsoluteDivergence.add(edgeResult);
//		}
//		
//		flogger = new FileLogger(prefix + "-MotionDivergenceRelative.txt", false /* print time stamp */);
//		for (int i=0; i < pathHistory.size(); i++) {
//			log(flogger, "Relative analysis of path to way point " + (i+1) + "...");
//			ResultEdge edgeResult = analyzeEdgeRelativeDivergence(expData, pathHistory.get(i));
//			log(flogger, edgeResult.toString());
//			results.resultRelativeDivergence.add(edgeResult);
//		}
//		
//		flogger = new FileLogger(prefix + "-MotionDivergenceRelativeSpeed.txt", false /* print time stamp */);
//		for (int i=0; i < pathHistory.size(); i++) {
//			log(flogger, "Relative+Speed analysis of path to way point " + (i+1) + "...");
//			ResultEdge edgeResult = analyzeEdgeRelativeDivergenceSpeed(expData, pathHistory.get(i));
//			log(flogger, edgeResult.toString());
//			results.resultRelativeSpeedDivergence.add(edgeResult);
//		}
//		return results;
//	}
	
	/**
	 * Calculates the absolute divergence of each robot as it follows the GPS-based motion script.
	 * Creates a table containing the absolute divergence of each robot, along fixed percentages
	 * towards each way point.
	 */
	private void analyzeAbsoluteDivergence() {
		
		// This data structure stores the results of all absolute divergence calculations.
		Vector<DivergenceExp> absoluteDivs = new Vector<DivergenceExp>();
		
		// For each log file, analyze the absolute divergence.
		Enumeration<RobotExpData> expEnum = robotExpData.elements();
		while (expEnum.hasMoreElements()) {
			RobotExpData expData = expEnum.nextElement();
			log("analyzeAbsoluteDivergence: Analyzing robot " + expData.getRobotName() + ", log: " + expData.getFileName());
			
			DivergenceExp divExp = new DivergenceExp(expData);
			absoluteDivs.add(divExp);
			
			// For each edge in the motion script...
			for (int edgeCount = 0; edgeCount < motionScript.numWayPoints(); edgeCount++) {
				PathEdge currEdge = expData.getPathEdge(edgeCount);
				
				// Get the starting location of the robot...
				Location startLoc = currEdge.getStartLoc();
				if (startLoc == null) {
					logErr("analyzeAbsoluteDivergence: Unknown start location in path edge:\n" + currEdge);
					System.exit(-1);
				}

				// Create the ideal path along which the robot should travel.
				// This is the line connecting the start location to the end location.
				Line perfectRoute = new Line(startLoc, currEdge.getEndLocation());

				// Some debug tests...
				//		System.out.println("Equation of Perfect Route: " + perfectRoute);
				//		System.out.println("Check getLongitude: got " + perfectRoute.getLongitude(startLoc.latitude()) + ", expected " + startLoc.longitude());
				//		System.out.println("Check getLatitude: got " + perfectRoute.getLatitude(startLoc.longitude()) + ", expected " + startLoc.latitude());

				// The destination way point is always the edge count + 1.  For example,
				// if this is edge 0, the destination way point is way point 1.
				DivergenceEdge divEdge = new DivergenceEdge(edgeCount+1, perfectRoute);
				divExp.addEdge(divEdge);
				
				// Divide the edge traversal into ten segments.  We will compute the absolute divergence at 10% intervals.
				long totalTime = currEdge.getEndTime() - currEdge.getStartTime();
				long deltaTime = totalTime / 10;
				log("analyzeAbsoluteDivergence: totalTime = " + totalTime + ", deltaTime = " + deltaTime);
				
				// For every 10% of the edge traversed...
				for (int pctComplete = 0; pctComplete <= 100; pctComplete += 10) {
					long currTime = currEdge.getStartTime() + (pctComplete / 10) * deltaTime;
					
					Location currLoc = expData.getLocation(currTime);
					Location idealLoc = perfectRoute.getClosestLocationTo(currLoc);
					double divergence = currLoc.distanceTo(idealLoc);

					log("analyzeAbsoluteDivergence: pctEdgeComplete=" + pctComplete + ", currTime=" + currTime + ", currLoc=" + currLoc + ", idealLoc=" + idealLoc + ", divergence=" + divergence);
					
					Divergence div = new Divergence(currTime, pctComplete, currLoc, idealLoc, divergence);
					divEdge.addDivergence(div);
				}
			}
		}
		
		saveResults(outputFileName + "-absoluteDivergence.txt", absoluteDivs);
	}
	
	/**
	 * Saves the results into a text file.
	 * 
	 * @param fileName The name of the file in which to save the results.
	 * @param divs A vector containing the divergences of the robots.
	 */
	private void saveResults(String fileName, Vector<DivergenceExp> divs) {
		FileLogger fileWriter = new FileLogger(fileName, false);
		
		// Create and save the table header
		String tableHeader = "Dest. Waypoint\tPct. Complete\tIndex";
		Enumeration<String> expLabelsEnum = expLabels.elements();
		while (expLabelsEnum.hasMoreElements()) {
			tableHeader += "\t" + expLabelsEnum.nextElement();
		}
		log(fileWriter, tableHeader);
		
		// For each way point...
		for (int wayPointIndx = 0; wayPointIndx < motionScript.numWayPoints(); wayPointIndx++) {
			
			// For each percentage completed...
			for (double pctComplete = 0; pctComplete < 100; pctComplete += 10) {
				
				StringBuffer sb = new StringBuffer();
				sb.append(wayPointIndx+1);
				sb.append("\t");
				sb.append(pctComplete);
				sb.append("\t");
				sb.append((wayPointIndx+1)*100 + pctComplete);
				
				
//				if (pctComplete == 0) {
//					sb.append(wayPointIndx+1);
//				} else
//					sb.append("\t")
				
				
				// For each experiment get and save the divergence...
				for (int expIndx = 0; expIndx < divs.size(); expIndx++) {
					double divergence = divs.get(expIndx).getDivergence(wayPointIndx, pctComplete);
					sb.append("\t");
					sb.append(divergence);
				}
				
				log(fileWriter, sb.toString());
			}
		}
		
	}
	
	private void log(FileLogger flogger, String msg) {
		if (flogger != null)
			flogger.log(msg);
		System.out.println(msg);
	}
	
	/**
	 * Holds the divergence exhibited by a robot throughout an experiment.
	 * 
	 * @author Chien-Liang Fok
	 */
	private class DivergenceExp {
		
		private RobotExpData robotExpData;
		
		private Vector<DivergenceEdge> edges = new Vector<DivergenceEdge>();
		
		public DivergenceExp(RobotExpData robotExpData) {
			this.robotExpData = robotExpData;
		}
		
		public void addEdge(DivergenceEdge edge) {
			edges.add(edge);
		}
		
		/**
		 * Returns the divergence of the robot as it travels towards a certain waypoint.
		 * 
		 * @param wayPointIndx The destination way point.  The way point index is the way point number minus one.
		 * @param pctComplete The percentage of the edge traversal that is complete.
		 * @return The divergence of the robot.
		 */
		public double getDivergence(int wayPointIndx, double pctComplete) {
			DivergenceEdge de = edges.get(wayPointIndx);
			return de.getDivergence(pctComplete);
		}
		
		/**
		 * @return The raw experiment data recorded by the robot that is the source of the divergence data.
		 */
		public RobotExpData getRobotExpData() {
			return robotExpData;
		}
		
		public String toString() {
			StringBuffer sb = new StringBuffer("Divergence for experiment: ");
			sb.append(robotExpData.getFileName());
			sb.append("\n");
			
			for (int i=0; i < edges.size(); i++) {
				DivergenceEdge currEdge = edges.get(i);
				sb.append(currEdge.toString());
				sb.append("\n");
			}
			
			return sb.toString();
		}
	}
	
	/**
	 * Hold the divergence exhibited by a robot as it traversed a single edge of a motion script.
	 * 
	 * @author Chien-Liang Fok
	 *
	 */
	private class DivergenceEdge {
		
		/**
		 * The destination way point at the end of this edge.
		 */
		private int destWayPoint;
		
		/**
		 * The ideal path along which the robot should traverse.
		 */
		private Line idealEdge;
		
		/**
		 * The divergences from the ideal path that the robot actually traversed.
		 */
		private Vector<Divergence> divergences = new Vector<Divergence>();
		
		/**
		 * The constructor.
		 * 
		 * @param destWayPoint The destination waypoint.
		 */
		public DivergenceEdge(int destWayPoint, Line idealEdge) {
			this.destWayPoint = destWayPoint;
			this.idealEdge = idealEdge;
		}
		
		/**
		 * Adds a divergence measurement to this edge.
		 * 
		 * @param div The divergence measurement.
		 */
		public void addDivergence(Divergence div) {
			divergences.add(div);
		}
		
		/**
		 * Returns the divergence of the robot when it has traveled a certain 
		 * percentage of the edge.
		 * 
		 * @param pctComplete The percentage traversed.
		 * @return The divergence of the robot.
		 */
		public double getDivergence(double pctComplete) {
			Enumeration<Divergence> divEnum = divergences.elements();
			while (divEnum.hasMoreElements()) {
				Divergence div = divEnum.nextElement();
				if (div.getPctComplete() == pctComplete)
					return div.getDivergence();
			}
			logErr("DivergenceEdge: getDivergence: ERROR: Unable to get divergence of robot at " + pctComplete + "% completion");
			System.exit(1);
			return -1; // should never get here...
		}
		
		/**
		 * @return The destination way point.
		 */
		public int getDestWayPoint() {
			return destWayPoint;
		}
		
		/**
		 * @return The ideal edge.
		 */
		public Line getIdealEdge() {
			return idealEdge;
		}
		
		/**
		 * @return The divergence measurements along this edge.
		 */
		public Vector<Divergence> getDivergences() {
			return divergences;
		}
		
		public String toString() {
			StringBuffer sb = new StringBuffer("Towards Way Point ");
			sb.append(getDestWayPoint());
			sb.append(", ideal edge: ");
			sb.append(getIdealEdge());
			sb.append("\n");
			
			for (int i=0; i < divergences.size(); i++) {
				Divergence div = divergences.get(i);
				sb.append(div.toString());
				sb.append("\n");
			}
			
			return sb.toString();
		}
	}
	
	/**
	 * Contains a single divergence measurement.
	 *
	 * @author Chien-Liang Fok
	 */
	private class Divergence {
		/**
		 * The time stamp of the divergence measurement.
		 */
		private long timeStamp;
		
		/**
		 * The percentage of the edge that the robot has completed traversing.
		 */
		private double pctComplete;
		
		/**
		 * The actual location of the robot.
		 */
		private Location currLoc;
		
		/**
		 * The ideal location that the robot should be at.  This depends on the 
		 * type of divergence being calculated.
		 */
		private Location idealLoc;
		
		/**
		 * The distance between the robot's current location and its ideal location.
		 */
		private double divergence;
		
		/**
		 * The constructor.
		 * 
		 * @param timeStamp The time stamp of the divergence measurement.
		 * @param pctComplete The percentage of the edge the robot has traversed, based on the path the robot actually traveled.
		 * @param currLoc The current location.
		 * @param idealLoc The ideal location.
		 * @param divergence The divergence between the robot's actual location and its ideal location.
		 */
		public Divergence(long timeStamp, double pctComplete, Location currLoc, Location idealLoc, double divergence) {
			this.timeStamp = timeStamp;
			this.pctComplete = pctComplete;
			this.currLoc = currLoc;
			this.idealLoc = idealLoc;
			this.divergence = divergence;
		}
		
		/**
		 * @return The time stamp.
		 */
		public long getTimeStamp() {
			return timeStamp;
		}
		
		/**
		 * @return The percentage of the edge completed.
		 */
		public double getPctComplete() {
			return pctComplete;
		}
		
		/**
		 * @return The actual location of the robot.
		 */
		public Location getCurrLoc() {
			return currLoc;
		}
		
		/**
		 * @return The ideal location that the robot should be at.  This depends on the 
		 * type of divergence being calculated.
		 */
		public Location getIdealLoc() {
			return idealLoc;
		}
		
		/**
		 * @return The divergence.
		 */
		public double getDivergence() {
			return divergence;
		}
		
		public String toString() {
			return timeStamp + "\t" + pctComplete + "\t" + currLoc + "\t" + idealLoc + "\t" + divergence;
		}
	}
	
	/**
	 * Calculates the position error relative to the perfect route, which is the
	 * straight line from where it's previously known location was to the destination.
	 * Note that the perfect route is updated each time a new GPS datapoint arrives.
	 * 
	 * @param expData The experiment data
	 * @param edge The edge to analyze.
	 */
//	private ResultEdge analyzeEdgeRelativeDivergence(RobotExpData expData, PathEdge edge) {
//		Location startLoc = edge.getStartLoc();
//		
//		/*
//		 * The initial perfect route is the straight line from the start position
//		 * to the final destination.
//		 */
//		Line perfectRoute = new Line(startLoc, edge.getEndLocation());
//		
////		long edgeEndTime =  / 1000;
//		
//		//log(flogger, "GPS Measurement\tTime(s)\tPcnt Complete\tDivergence (Relative)");
//		
//		Vector<ResultDatumEdge> result = new Vector<ResultDatumEdge>();
//		
//		int i = 0;
//		
//		// for each second in the edge...
//		for (long currTime = edge.getStartTime(); currTime < edge.getEndTime(); currTime += 1000) {
////		for (int i = 0; i < edge.getNumLocations(); i++) {
////			GPSLocationState currGpsLoc = edge.getLocation(i);
////			Location currLoc = new Location(currGpsLoc.getLoc());
//			
//			// Get the location of the robot at that time...
//			Location currLoc = expData.getLocation(currTime);
//			
//			long time = (currTime - edge.getStartTime());
//			double pctComplete = (((double)time / (double)edge.getDuration()) * 100.0);
//			double distToOptimal = perfectRoute.shortestDistanceTo(currLoc);
//			
//			System.out.println("analyzeEdgeRelativeDivergence: Adding result: " + pctComplete + " " + distToOptimal);
//			if (Double.isNaN(distToOptimal)) {
//				System.err.println("invalid distToOptimal, perfect route: " + perfectRoute + ", currLoc: " + currLoc);
//				System.exit(0);
//			}
//			
//			result.add(new ResultDatumEdge(++i, time, pctComplete, distToOptimal));
//			
//			/*
//			 * For the relative-divergence analysis, the perfect route is updated to be the
//			 * straight line from the robot's previous location to the final destination.
//			 * This is "fairer" since the robot is always trying to get to the final destination
//			 * from is present location and not necessarily follow the straight line from the
//			 * edge's start location to final location.
//			 */
//			perfectRoute = new Line(currLoc, edge.getEndLocation());
//			
//			// Some debug output
////			System.out.println("currLoc: " + currLoc);
////			System.out.println("destLoc: " + edge.getDest());
////			System.out.println("Updating perfect route to be: " + perfectRoute);
//		}
//		return new ResultEdge(result);
//	}
	                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
	/**
	 * Calculates the position error relative to if the robot had traveled along the perfect route, 
	 * at the speed in which it was traveling.  The perfect route is the
	 * straight line from where it's previously known location was to the destination.
	 * The perfect route is updated each time a new GPS data point arrives, and the
	 * speed is calculated as the average speed between GPS data points.
	 * 
	 * @param expData The experiment data
	 * @param edge The edge to analyze.
	 */
//	private ResultEdge analyzeEdgeRelativeDivergenceSpeed(RobotExpData expData, PathEdge edge) {
//		Location startLoc = edge.getStartLoc();
//		//long prevTime = edge.getAbsoluteStartTime();
////		long edgeEndTime = edge.getDuration() / 1000;
//		
//		/*
//		 * The initial perfect route is the straight line from the start position
//		 * to the final destination.
//		 */
//		Line perfectRoute = new Line(startLoc, edge.getEndLocation());
//		
//		//log(flogger, "GPS Measurement\tTime(s)\tPcnt Complete\tDivergence (Relative)");
//		Vector<ResultDatumEdge> result = new Vector<ResultDatumEdge>();
//		
//		int i = 0;
//		
//		// for each second in the edge...
//		for (long currTime = edge.getStartTime(); currTime < edge.getEndTime(); currTime += 1000) {
//			
////		for (int i = 0; i < edge.getNumLocations(); i++) {
////			GPSLocationState currGpsLoc = edge.getLocation(i);
////			Location currLoc = new Location(currGpsLoc.getLoc());
//			
//			// Get the location of the robot at that time...
//			Location currLoc = expData.getLocation(currTime);
//			
//			long time = currTime - edge.getStartTime();
//			
//			double pctComplete = (((double)time / (double)edge.getDuration()) * 100.0);
//			
//			/*
//			 * Calculate the distance between the previous GPS coordinate to the current one.
//			 * This will be used to determine where the robot would be if it had traveled
//			 * the exact same distance but along the perfect path towards the destination.
//			 */
//			double deltaDist = perfectRoute.getStartLoc().distanceTo(currLoc);
//			
//			double distToOptimal = perfectRoute.shortestDistanceTo(currLoc, deltaDist);
//			
//			result.add(new ResultDatumEdge(++i, time, pctComplete, distToOptimal));
//			//log(flogger, (i+1) + "\t" + time + "\t" + pctComplete + "\t" + distToOptimal);
//			
//			/*
//			 * For the relative-divergence analysis, the perfect route is updated to be the
//			 * straight line from the robot's previous location to the final destination.
//			 * This is "fairer" since the robot is always trying to get to the final destination
//			 * from is present location and not necessarily follow the straight line from the
//			 * edge's start location to final location.
//			 */
//			perfectRoute = new Line(currLoc, edge.getEndLocation());
//			//prevTime = currGpsLoc.getTimeStamp();
//			
//			// Some debug output
////			System.out.println("currLoc: " + currLoc);
////			System.out.println("destLoc: " + edge.getDest());
////			System.out.println("Updating perfect route to be: " + perfectRoute);
//		}
//		return new ResultEdge(result);
//	}
//	
//	class ResultEdge {
//		
//		/**
//		 * The divergence when the actual GPS data is received.
//		 */
//		Vector<ResultDatumEdge> actualResults;
//		
//		/**
//		 * The divergence at specific percentage traversal completions.
//		 * The index * 10 is the percent complete, e.g., index = 1 means
//		 * 10% complete.
//		 */
//		Vector<Double> interpolatedResults = new Vector<Double>();
//		
//		/**
//		 * The constructor.
//		 * 
//		 * @param actualResults
//		 */
//		public ResultEdge(Vector<ResultDatumEdge> actualResults) {
//			this.actualResults = actualResults;
//			
//			// Interpolates the divergence at fixed percentage completions (E.g., 0%, 10%, 20%, etc.).
//			for (int i = 0; i <= 100; i += 10) {
//				interpolatedResults.add(interpolateDivergence(i));
//			}
//		}
//		
//		public Vector<ResultDatumEdge> getActualResults() {
//			return actualResults;
//		}
//		
//		public Vector<Double> getInterpolatedResults() {
//			return interpolatedResults;
//		}
//		
//		public double getInterpolatedResults(int pctComplete) {
//			return interpolatedResults.get(pctComplete/10);
//		}
//		
//		public int numActualDataPoints() {
//			return actualResults.size();
//		}
//		
//		private double interpolateDivergence(double pcntDone) {
//			int beforeIndx = 0; // the index of the ResultDatumEdge that is immediately before pcntDone
//			int afterIndx = 0; // the index of the ResultDatumEdge that is immediately after pctntDone
//			boolean afterIndxFound = false;
//			
//			for (int i=0; i < actualResults.size(); i++) {
//				ResultDatumEdge currDataPoint = actualResults.get(i);
//				if (currDataPoint.getPctComplete() <= pcntDone)
//					beforeIndx = i;
//				if (!afterIndxFound && currDataPoint.getPctComplete() >= pcntDone) {
//					afterIndxFound = true;
//					afterIndx = i;
//				}
//			}
//			
//			if (beforeIndx == afterIndx)
//				return actualResults.get(beforeIndx).getDivergence();
//			else {
//				// Now we need to interpolate, create a line that passes through the
//				// points before and after pcntDone, then derive what the divergence
//				// would be at the pctnt done.
//				Location bLoc = new Location(actualResults.get(beforeIndx).getDivergence(), 
//						actualResults.get(beforeIndx).getPctComplete());
//				Location aLoc = new Location(actualResults.get(afterIndx).getDivergence(),
//						actualResults.get(afterIndx).getPctComplete());
//				Line l = new Line(bLoc, aLoc);
//				return l.getLatitude(pcntDone);
//			}
//		}
//		
//		public String toString() {
//			StringBuffer sb = new StringBuffer();
//			sb.append("Actual Data:\n");
//			sb.append("GPS Measurement\tTime(s)\tPcnt Complete\tDivergence\n");
//			for (int i=0; i < actualResults.size(); i++) {
//				sb.append(actualResults.get(i).toString() + "\n");
//			}
//			sb.append("Interpolated Data:\n");
//			sb.append("Pcnt Complete\tDivergence\n");
//			for (int i=0; i < interpolatedResults.size(); i++) {
//				sb.append((i*10) + "\t" + interpolatedResults.get(i) + "\n");
//			}
//			return sb.toString();
//		}
//	}
	
	
	/**
	 * Reads the spec file and initializes variables outputFileName, motionScript, logFileNames, and logFileLabels.
	 * 
	 * @param specFile The specification file.
	 */
	private void readSpecFile(String specFile) {
		// Open the specification file
		BufferedReader input = null;
		try {
			input =  new BufferedReader(new FileReader(specFile));
		} catch (IOException ex){
			ex.printStackTrace();
			logErr("Unable to open " + specFile);
			System.exit(1);
		}
		
		try {
			String line = null;
			int lineno = 1;
			while (( line = input.readLine()) != null) {
				if (!line.equals("")) {
					if (line.contains("OUTPUT_FILE")) {
						String[] elem = line.split("[\\s]+");
						
						if (elem.length > 1) {
							outputFileName = elem[1];
						} else {
							System.err.println("ERROR: Syntax error on line " + lineno + " of file " + specFile + ": outfile file not specified.");
							System.exit(1);
						}
					}
					else if (line.contains("LOG_FILE")) {
						String[] elem = line.split("[\\s]+");
					
						robotExpData.add(new RobotExpData(elem[1]));
						
						if (elem.length > 2)
							expLabels.add(elem[2]);  // user specified the caption name
						else {
							// use the mission, experiment, and robot names as the label
							String label = LogFileNameParser.extractMissionName(elem[1]) 
								+ "-" + LogFileNameParser.extractExpName(elem[1])
								+ "-" + LogFileNameParser.extractRobotName(elem[1]); 
							expLabels.add(label);
						}
					}
					else if (line.contains("MOTION_SCRIPT")) {
						String[] elem = line.split("[\\s]+");
						
						if (elem.length > 1)
							motionScript = new MotionScript(elem[1]);
						else {
							System.err.println("ERROR on line " + lineno + " of file " + specFile + ": Motion script not specified.");
							System.exit(1);
						}
					}
				}
				lineno++;
			}
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Perform some sanity checks to ensure we have all the information we need to perform the 
		// motion divergence analysis.
		if (outputFileName == null) {
			logErr("ERROR: output file name not specified.");
			System.exit(1);
		}
		
		if (motionScript == null) {
			logErr("ERROR: motion script not specified.");
			System.exit(1);
		}
		
		if (robotExpData.size() == 0) {
			logErr("ERROR: No log files specified.");
			System.exit(1);
		}
	}
	
	private void log(String msg) {
		if (System.getProperty ("PharosMiddleware.debug") != null)
			System.out.println(msg);
	}
	
	private void logErr(String msg) {
		System.err.println(msg);
	}
	
	private static void print(String msg) {
		System.out.println(msg);
	}
	
	private static void printErr(String msg) {
		System.err.println(msg);
	}
	
	private static void usage() {
		print("Usage: pharoslabut.logger.analyzer.MotionDivergenceGPS <options>\n");
		print("Where <options> include:");
		print("\t-spec <spec file name>: The specification file. (required)");
		print("\t\tSyntax of specification file:");
		print("\t\t\tMOTION_SCRIPT <motion script>");
		print("\t\t\tOUTPUT_FILE <name of output file>");
		print("\t\t\tLOG_FILE <name of log file> <label (optional)>");
		print("\t\t\t...");
		print("\t\tFor each LOG_FILE listed in the specification file, the divergence will be analyzed. " 
				+ "Reflective divergence is determined based on all of these log files.");
		print("\t\tFor more details, see: http://pharos.ece.utexas.edu/wiki/index.php/How_to_Analyze_the_Path_Divergence_when_Following_GPS_Waypoints");
	}
	
	public static void main(String[] args) {
		String specFileName = null;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-h")) {
					usage();
					System.exit(0);
				}
				
				if (args[i].equals("-spec")) {
					specFileName = args[++i];
				}
				else if (args[i].equals("-debug") || args[i].equals("-d")) {
					System.setProperty ("PharosMiddleware.debug", "true");
				}
				else {
					printErr("Unknown option: " + args[i]);
					usage();
					System.exit(1);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			usage();
			System.exit(1);
		}
		
		if (specFileName == null) {
			printErr("Must specify specification file or log file.");
			usage();
			System.exit(1);
		}
		
		print("SpecFile: " + specFileName);
		print("Debug: " + (System.getProperty ("PharosMiddleware.debug") != null));
		
		try {
			new MotionDivergenceGPS(specFileName);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
