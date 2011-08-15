package pharoslabut.logger.analyzer;

//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.IOException;
import java.awt.Color;
import java.util.Enumeration;
import java.util.Vector;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RefineryUtilities;

import pharoslabut.logger.*;
import pharoslabut.navigate.Location;
//import pharoslabut.navigate.motionscript.MotionScript;

/**
 * Analyzes the motion divergence exhibit by a Proteus robot as it follows
 * a GPS-based motion script.
 * 
 * @author Chien-Liang Fok
 */
public class MotionDivergenceAnalyzer {
	
	/**
	 * The period at which to calculate divergence in milliseconds.
	 */
	private long samplingInterval;
	
	/**
	 * The robot's experiment data.
	 */
	private RobotExpData robotData;
	
	/**
	 * The maximum heading error in radians before the robot is considered "oriented".  
	 * This is used to calculate the oriented start divergence, see: 
	 * http://pharos.ece.utexas.edu/wiki/index.php/How_to_Analyze_Motion_Divergence_when_a_Robot_Follows_GPS_Waypoints#Oriented_Start_Divergence
	 */
	private double orientedThreshold;
	
	// Calculate the various forms of divergence
	Vector<SpatialDivergence> absDivs;      // absolute divergence
	Vector<SpatialDivergence> osDivs;  // oriented start divergence
	Vector<TemporalDivergence> tempDivs;    // temporal divergence
	
	/**
	 * The constructor.
	 * 
	 * @param logFileName The robot's experiment log file to analyze.
	 * @param samplingInterval  The interval at which to calculate the divergence in milliseconds.
	 */
	public MotionDivergenceAnalyzer(String logFileName, long samplingInterval, double orientedThreshold, boolean saveData) {
		
		this.robotData = new RobotExpData(logFileName);
		this.samplingInterval = samplingInterval;
		this.orientedThreshold = orientedThreshold;
		
		// Calculate the various forms of divergence
		absDivs = calcAbsoluteDivergence();      // absolute divergence
		osDivs = calcOrientedStartDivergence();  // oriented start divergence
		tempDivs = calcTemporalDivergence();    // temporal divergence
		
		if (saveData)
			saveResults();
		
		showPlot();
	}
	
	/**
	 * Save the results into files.
	 */
	private void saveResults() {
		String logFileName = robotData.getFileName();
		
		// Determine the prefix of the output file
		String outputFilePrefix = null;
		
		if (logFileName.contains(".")) {
			outputFilePrefix = logFileName.substring(0, logFileName.lastIndexOf('.'));
		} else {
			outputFilePrefix = logFileName;
		}
		
		Logger.logDbg("saveResults: outputFilePrefix = " + outputFilePrefix);
		
		// Save the absolute divergence...
		FileLogger flogger = new FileLogger(outputFilePrefix + "-absDivergence.txt");
		flogger.log("Time (ms)\tDelta Time (ms)\tDelta Time (s)\tAbsolute Divergence");
		
		Enumeration<SpatialDivergence> e = absDivs.elements();
		while (e.hasMoreElements()) {
			SpatialDivergence sd = e.nextElement();
			flogger.log(sd.getTimeStamp() 
					+ "\t" + (sd.getTimeStamp() - robotData.getStartTime()) 
					+ "\t" + (sd.getTimeStamp() - robotData.getStartTime())/1000 
					+ "\t" + sd.getDivergence());
		}
		
		robotData.printWayPointArrivalTable(flogger); // save the waypoint arrival table
		
		// Save the oriented start divergence...
		flogger = new FileLogger(outputFilePrefix + "-orientedStartDivergence.txt");
		flogger.log("Time (ms)\tDelta Time (ms)\tDelta Time (s)\tOriented Start Divergence");
		e = osDivs.elements();
		while (e.hasMoreElements()) {
			SpatialDivergence sd = e.nextElement();
			flogger.log(sd.getTimeStamp() 
					+ "\t" + (sd.getTimeStamp() - robotData.getStartTime()) 
					+ "\t" + (sd.getTimeStamp() - robotData.getStartTime())/1000 
					+ "\t" + sd.getDivergence());
		}
		
		robotData.printWayPointArrivalTable(flogger); // save the waypoint arrival table
		
		// Save the temporal divergence
		flogger = new FileLogger(outputFilePrefix + "-temporalDivergence.txt");
		flogger.log("WayPoint Number\tActual Arrival Time\tIdeal Relative Arrival Time\tIdeal Arrival Time\tTemporal Divergence");
		Enumeration<TemporalDivergence> e2 = tempDivs.elements();
		while (e2.hasMoreElements()) {
			TemporalDivergence td = e2.nextElement();
			flogger.log(td.getWaypointNumber()
					+ "\t" + td.getActualTOA()
					+ "\t" + td.getIdealRelTOA()
					+ "\t" + td.getIdealTOA()
					+ "\t" + td.getDivergence());
		}
		
		robotData.printWayPointArrivalTable(flogger);
	}
	
	/**
	 * Calculates the absolute divergence of a robot.
	 * 
	 * @return The absolute divergence.
	 */
	private Vector<SpatialDivergence> calcAbsoluteDivergence() {
		
		long startTime = robotData.getStartTime();
		Vector<SpatialDivergence> result = new Vector<SpatialDivergence>(); 
		
		// For the duration of the experiment, at every sampling interval... 
		for (long time = startTime; time < robotData.getFinalWaypointArrivalTime(); time += samplingInterval) {
			
			// Calculate the absolute divergence by creating the perfect path
			// along which the robot should travel and finding the point on
			// this path that is closest to the robot's actual location.
			PathEdge edge = robotData.getRelevantPathEdge(time);
			
//			logDbg("Got path edge " + edge.getSeqNo(), flogger);
			
			Line perfectPath = new Line(edge.getStartLoc(), edge.getEndLocation());
			Location actualLoc = robotData.getLocation(time);
			Location idealLoc = perfectPath.getLocationClosestTo(actualLoc);
			
			result.add(new SpatialDivergence(time, actualLoc, idealLoc, perfectPath));
		}
		
		return result;
	}
	
	/**
	 * Calculates the oriented start divergence of a robot.
	 * 
	 * @param robotData The robot's experiment data.
	 * @param outputFileName The file in which to save results.
	 */
	private Vector<SpatialDivergence> calcOrientedStartDivergence() {
		
		long startTime = robotData.getStartTime();
		Vector<SpatialDivergence> result = new Vector<SpatialDivergence>();
		
		// For the duration of the experiment, at every sampling interval... 
		for (long time = startTime; time < robotData.getFinalWaypointArrivalTime(); time += samplingInterval) {
			// Get the relevant path edge.  
			PathEdge edge = robotData.getRelevantPathEdge(time);
			
			//logDbg("calcOrientedStartDivergence: Got path edge " + edge.getSeqNo(), flogger);
			
			// If the robot has oriented itself by the specified time, determine the
			// ideal path over which the robot should travel and its divergence from this path.
			LocationState orientedLoc = robotData.getOrientedLocation(edge, orientedThreshold); 
			Logger.logDbg("StartLoc=" + edge.getStartLoc() + " @ " + edge.getStartTime() + ", OrientedLoc=" + orientedLoc.getLoc()
					+ " @ " + orientedLoc.getTimestamp());
			
			if (orientedLoc.getTimestamp() < time) {
				
				Line perfectPath = new Line(orientedLoc.getLoc(), edge.getEndLocation());
				Location actualLoc = robotData.getLocation(time);
				Location idealLoc = perfectPath.getLocationClosestTo(actualLoc);
				
				result.add(new SpatialDivergence(time, actualLoc, idealLoc, perfectPath));
			} else
				Logger.logDbg("Robot was not oriented yet at time " + time);
		}
		
		return result;
	}
	
	/**
	 * Calculates the temporal divergence of the robot.  This is the difference between the time
	 * the robot is expected to be at a waypoint and the time it actually arrives at the waypoint.
	 * 
	 * @return The temporal divergence of the robot.
	 */
	private Vector<TemporalDivergence> calcTemporalDivergence() {
		Logger.logDbg("Calculating the temporal divergence of the robot...");
		
		Vector<PathEdge> pathEdges = robotData.getPathEdges();
		
		if (pathEdges.size() == 0) {
			Logger.logErr("No edges present!");
			System.exit(1);
		}
		
		long edgeStartTime = pathEdges.get(0).getStartTime(); // initialize this to be the start of the first edge
		Logger.logDbg("first edgeStartTime = " + edgeStartTime);
		
		Vector<TemporalDivergence> result = new Vector<TemporalDivergence>();
		
		// First calculate the expected time that the robot is supposed to reach
		// each waypoint.  Then determine the temporal divergence by comparing it
		// with the actual time the robot reached the waypoint.
		for (int i=0; i < pathEdges.size(); i++) {
			PathEdge currEdge = pathEdges.get(i);
			
			Location idealStartLoc = currEdge.getIdealStartLoc();
			Location idealEndLoc = currEdge.getEndLocation();
			double idealSpeed = currEdge.getIdealSpeed();
			
			double idealDist = idealStartLoc.distanceTo(idealEndLoc);
			double idealDuration = idealDist / idealSpeed;
			
			long idealArrivalTime = (long)(edgeStartTime + idealDuration); // this is cumulative across the entire experiment
			long idealRelArrivalTime = (long)(currEdge.getStartTime() + idealDuration); // this is relative to the previous waypoint
			long actualArrivalTime = currEdge.getEndTime();
			
			TemporalDivergence td = new TemporalDivergence(i, idealArrivalTime, idealRelArrivalTime, actualArrivalTime);
			
			//Logger.logDbg("Waypoint " + i + ", ideal TOA = " + idealArrivalTime + ", actual TOA = " + actualArrivalTime + ", delta = " + td.getDivergence());
			
			result.add(td);
			
			edgeStartTime = idealArrivalTime + currEdge.getPauseTime(); // include the pause time that the robot stops at each waypoint
		}
		
		return result;
		
	}
	
	
	/**
	 * Displays the data in a graph.
	 */
	private void showPlot() {
		// Create the various data series...
		XYSeries absDivSeries = new XYSeries("Absolute Divergence");
		XYSeries osDivSeries = new XYSeries("Oriented Start Divergence, threshold=" + orientedThreshold);
		XYSeries tempDivSeries = new XYSeries("Temporal Divergence");
		XYSeries relTempDivSeries = new XYSeries("Relative Temporal Divergence");
		
		Enumeration<SpatialDivergence> e = absDivs.elements();
		while (e.hasMoreElements()) {
			SpatialDivergence currDiv = e.nextElement();
			double currTime = (currDiv.getTimeStamp() - robotData.getStartTime())/1000.0;
			absDivSeries.add(currTime, currDiv.getDivergence());
		}
		
		e = osDivs.elements();
		while (e.hasMoreElements()) {
			SpatialDivergence currDiv = e.nextElement();
			double currTime = (currDiv.getTimeStamp() - robotData.getStartTime())/1000.0;
			osDivSeries.add(currTime, currDiv.getDivergence());
		}
		
		Enumeration<TemporalDivergence> e3 = tempDivs.elements();
		while (e3.hasMoreElements()) {
			TemporalDivergence currDiv = e3.nextElement();
			tempDivSeries.add(currDiv.getWaypointNumber(), currDiv.getDivergence());
			relTempDivSeries.add(currDiv.getWaypointNumber(), currDiv.getRelDivergence());
		}
		
		// Create two data series one containing the times when the robot starts heading 
		// towards a waypoint, and another containing the times when the robot arrives at
		// a waypoint
		final XYSeries beginEdgeSeries = new XYSeries("Begin Edge Traveral");
		final XYSeries waypointArrivalSeries = new XYSeries("Waypoint Arrival");
		Vector<PathEdge> pathEdges = robotData.getPathEdges();
		Enumeration<PathEdge> e2 = pathEdges.elements();
		while (e2.hasMoreElements()) {
			PathEdge currEdge = e2.nextElement();
			double beginEdgeTime = (currEdge.getStartTime() - robotData.getStartTime())/1000.0;
			beginEdgeSeries.add(beginEdgeTime, 0);
			double wayPointArrivalTime = (currEdge.getEndTime() -  robotData.getStartTime())/1000.0;
			waypointArrivalSeries.add(wayPointArrivalTime, 0);
		}
		
		// Create data sets out of the data series.
		XYSeriesCollection spatialDivDataSet = new XYSeriesCollection();
		spatialDivDataSet.addSeries(absDivSeries);
		spatialDivDataSet.addSeries(osDivSeries);
		spatialDivDataSet.addSeries(beginEdgeSeries);
		spatialDivDataSet.addSeries(waypointArrivalSeries);
		
		XYSeriesCollection tempDivDataSet = new XYSeriesCollection();
		tempDivDataSet.addSeries(tempDivSeries);
		tempDivDataSet.addSeries(relTempDivSeries);
		
		// Create the charts
		JFreeChart absDivChart = ChartFactory.createXYLineChart(
				"Spatial Divergence vs. Time",                        // chart title
				"Time (s)",                                            // x axis label
				"Divergence (m)",                                      // y axis label
				spatialDivDataSet,                                         // the absolute divergence data
				PlotOrientation.VERTICAL,                              // plot orientation (y axis is vertical)
				true,                                                  // include legend
				true,                                                  // tooltips
				false                                                  // urls
		);
       
        JFreeChart tempDivChart = ChartFactory.createXYLineChart(
        		"Temporal Divergence vs. Time",                  // chart title
        		"Time (s)",                                            // x axis label
        		"Temporal Divergence (s)",                                         // y axis label
        		tempDivDataSet,                                          // the oriented start data
        		PlotOrientation.VERTICAL,                              // plot orientation (y axis is vertical)
        		true,                                                  // include legend
        		true,                                                  // tooltips
        		false                                                  // urls
        );
        
        // Place the legend on top of the chart just below the title.
        LegendTitle headingLegend = absDivChart.getLegend();
        headingLegend.setPosition(RectangleEdge.TOP);
        LegendTitle speedLegend = tempDivChart.getLegend();
        speedLegend.setPosition(RectangleEdge.TOP);
        
        absDivChart.setBackgroundPaint(Color.white);
        tempDivChart.setBackgroundPaint(Color.white);
        
        // Configure when to display lines an when to display the shapes that indicate data points
        XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer();
        renderer1.setSeriesLinesVisible(0, false); // display the heading as a line
        renderer1.setSeriesShapesVisible(0, true);
        renderer1.setSeriesPaint(0, Color.BLACK);
        renderer1.setSeriesShape(0, org.jfree.util.ShapeUtilities.createUpTriangle(2));
        renderer1.setSeriesLinesVisible(1, false); // display the headingCmd as a line
        renderer1.setSeriesShapesVisible(1, true);
        renderer1.setSeriesPaint(1, Color.RED);
        renderer1.setSeriesShape(1, new java.awt.Rectangle(-2,-2, 4, 4));
        renderer1.setSeriesLinesVisible(2, false); // display the begin edge traversal points as blue dots
        renderer1.setSeriesShapesVisible(2, true);
        renderer1.setSeriesPaint(2, Color.BLUE);
        renderer1.setSeriesShape(2, new java.awt.geom.Ellipse2D.Double(-3,-3,6,6));
        renderer1.setSeriesLinesVisible(3, false); // display the begin edge traversal points as green dots
        renderer1.setSeriesShapesVisible(3, true);
        renderer1.setSeriesPaint(3, Color.GREEN.darker());
        renderer1.setSeriesShape(3, new java.awt.geom.Ellipse2D.Double(-5,-5,10,10));
        
        final XYPlot headingPlot = absDivChart.getXYPlot();
        headingPlot.setRenderer(0, renderer1);
        
//        XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer();
//        renderer2.setSeriesLinesVisible(0, true); // display the speed as a line
//        renderer2.setSeriesShapesVisible(0, false);
//        renderer2.setSeriesPaint(0, Color.BLACK);
//        renderer2.setSeriesLinesVisible(1, true); // display the speedCmd as a line
//        renderer2.setSeriesShapesVisible(1, false);
//        renderer2.setSeriesPaint(1, Color.RED);
//        renderer2.setSeriesLinesVisible(2, false); // display the waypoints as points
//        renderer2.setSeriesShapesVisible(2, true);
//        renderer2.setSeriesPaint(2, Color.BLUE);
//        renderer2.setSeriesShape(2, new java.awt.geom.Ellipse2D.Double(-5,-5,10,10));
//        renderer2.setSeriesLinesVisible(3, false); // display the begin edge traversal points as green dots
//        renderer2.setSeriesShapesVisible(3, true);
//        renderer2.setSeriesPaint(3, Color.GREEN);
//        renderer2.setSeriesShape(3, new java.awt.geom.Ellipse2D.Double(-5,-5,10,10));
        
//        final XYPlot speedPlot = osDivChart.getXYPlot();
//        speedPlot.setRenderer(0, renderer2);
        
        // Place the charts in their own panels.
        ChartPanel headingChartPanel = new ChartPanel(absDivChart);
        headingChartPanel.setPreferredSize(new java.awt.Dimension(1200, 500));
        ChartPanel temporalChartPanel = new ChartPanel(tempDivChart);
        temporalChartPanel.setPreferredSize(new java.awt.Dimension(1200, 500));
       
        // Place both chart panels within a single panel with two rows.
//        javax.swing.JPanel chartsPanel = new javax.swing.JPanel(new java.awt.GridLayout(2,1));
//        chartsPanel.add(headingChartPanel);
//        chartsPanel.add(speedChartPanel);
       
        // Create a frame for the chart, then display it.
        ApplicationFrame appFrame1 = new ApplicationFrame("Divergence for " + robotData.getFileName());
//        appFrame.setContentPane(chartsPanel);
        appFrame1.setContentPane(headingChartPanel);
        appFrame1.pack();
		RefineryUtilities.centerFrameOnScreen(appFrame1);
		appFrame1.setVisible(true);
		
		ApplicationFrame appFrame2 = new ApplicationFrame("Divergence for " + robotData.getFileName());
		appFrame2.setContentPane(temporalChartPanel);
		appFrame2.pack();
		RefineryUtilities.centerFrameOnScreen(appFrame2);
		appFrame2.setVisible(true);
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
	 * Calculates the absolute, relative, and relative-speed divergence of each robot as it follows 
	 * the GPS-based motion script.  Creates tables containing the divergences of each robot
	 * along fixed percentages towards each way point.
	 */
//	private void calculateDivergence() {
//		
//		// Stores the absolute divergence calculations.
//		Vector<DivergenceExp> absoluteDivs = new Vector<DivergenceExp>();
//		
//		// Stores the relative divergence calculations.
//		Vector<DivergenceExp> relativeDivs = new Vector<DivergenceExp>();
//		
//		// Stores the relative-speed divergence calculations.
//		Vector<DivergenceExp> relativeSpeedDivs = new Vector<DivergenceExp>();
//		
//		// For each log file analyze the absolute divergence.
//		Enumeration<RobotExpData> expEnum = robotExpData.elements();
//		while (expEnum.hasMoreElements()) {
//			RobotExpData expData = expEnum.nextElement();
//			log("calculateAbsoluteDivergence: Analyzing robot " + expData.getRobotName() + ", log: " + expData.getFileName());
//			
//			DivergenceExp absDivExp = new DivergenceExp(expData);
//			absoluteDivs.add(absDivExp);
//			DivergenceExp relDivExp = new DivergenceExp(expData);
//			relativeDivs.add(relDivExp);
//			DivergenceExp relSpeedDivExp = new DivergenceExp(expData);
//			relativeSpeedDivs.add(relSpeedDivExp);
//			
//			// For each edge in the motion script...
//			for (int edgeCount = 0; edgeCount < motionScript.numWayPoints(); edgeCount++) {
//				PathEdge currEdge = expData.getPathEdge(edgeCount);
//				
//				// Get the starting location of the robot...
//				Location startLoc = currEdge.getStartLoc();
//				if (startLoc == null) {
//					logErr("calculateAbsoluteDivergence: Unknown start location in path edge:\n" + currEdge);
//					System.exit(-1);
//				}
//
//				// Create the ideal path along which the robot should travel.
//				// This is the line connecting the start location to the end location.
//				Line absolutePerfectRoute = new Line(startLoc, currEdge.getEndLocation());
//
//				// Initially, the relative perfect route is the same as the absolute perfect route.
//				Line relativePerfectRoute = absolutePerfectRoute;
//				
//				// Some debug tests...
//				//		System.out.println("Equation of Perfect Route: " + perfectRoute);
//				//		System.out.println("Check getLongitude: got " + perfectRoute.getLongitude(startLoc.latitude()) + ", expected " + startLoc.longitude());
//				//		System.out.println("Check getLatitude: got " + perfectRoute.getLatitude(startLoc.longitude()) + ", expected " + startLoc.latitude());
//
//				// The destination way point is always the edge count + 1.  For example,
//				// if this is edge 0, the destination way point is way point 1.
//				DivergenceEdge absoluteDivEdge = new DivergenceEdge(edgeCount+1);
//				absDivExp.addEdge(absoluteDivEdge);
//				DivergenceEdge relativeDivEdge = new DivergenceEdge(edgeCount+1);
//				relDivExp.addEdge(relativeDivEdge);
//				DivergenceEdge relativeSpeedDivEdge = new DivergenceEdge(edgeCount+1);
//				relSpeedDivExp.addEdge(relativeSpeedDivEdge);
//				
//				// Divide the edge traversal into ten segments.  We will compute the absolute divergence at 10% intervals.
//				long totalTime = currEdge.getEndTime() - currEdge.getStartTime();
//				long deltaTime = totalTime / 10;
//				log("calculateAbsoluteDivergence: totalTime = " + totalTime + ", deltaTime = " + deltaTime);
//				
//				// For every 10% of the edge traversed...
//				for (int pctComplete = 0; pctComplete <= 100; pctComplete += 10) {
//					long currTime = currEdge.getStartTime() + (pctComplete / 10) * deltaTime;
//					
//					Location currLoc = expData.getLocation(currTime);
//					
//					// Calculate the absolute divergence...
//					Location absoluteIdealLoc = absolutePerfectRoute.getLocationClosestTo(currLoc);
//					double absoluteDivergence = currLoc.distanceTo(absoluteIdealLoc);
//					
//					// Calculate the relative divergence...
//					Location relativeIdealLoc = relativePerfectRoute.getLocationClosestTo(currLoc);
//					double relativeDivergence = currLoc.distanceTo(relativeIdealLoc);
//
//					// Calculate the relative-speed divergence...
//					/*
//					 * Calculate the distance between the previous location of the robot to the current location.
//					 * This will be used to determine where the robot would be if it had traveled
//					 * the exact same distance but along the perfect path towards the destination.
//					 */
//					double deltaDist = relativePerfectRoute.getStartLoc().distanceTo(currLoc);
//					Location relativeSpeedIdealLoc = relativePerfectRoute.getLocationRelativeSpeed(deltaDist);
//					double relativeSpeedDivergence = currLoc.distanceTo(relativeIdealLoc);
//					
//					log("calculateDivergence: pctEdgeComplete=" + pctComplete + ", currTime=" + currTime + ", currLoc=" + currLoc 
//							+ ", absoluteIdealLoc=" + absoluteIdealLoc + ", absoluteDivergence=" + absoluteDivergence
//							+ ", relativeIdealLoc=" + relativeIdealLoc + ", relativeDivergence=" + relativeDivergence
//							+ ", relativeSpeedIdealLoc=" + relativeSpeedIdealLoc + ", relativeSpeedDivergence=" + relativeSpeedDivergence);
//					
//					Divergence absoluteDiv = new Divergence(currTime, pctComplete, currLoc, absoluteIdealLoc, absoluteDivergence, absolutePerfectRoute);
//					absoluteDivEdge.addDivergence(absoluteDiv);
//					Divergence relativeDiv = new Divergence(currTime, pctComplete, currLoc, relativeIdealLoc, relativeDivergence, relativePerfectRoute);
//					relativeDivEdge.addDivergence(relativeDiv);
//					Divergence relativeSpeedDiv = new Divergence(currTime, pctComplete, currLoc, relativeSpeedIdealLoc, relativeSpeedDivergence, relativePerfectRoute);
//					relativeSpeedDivEdge.addDivergence(relativeSpeedDiv);
//					
//					/*
//					 * For relative-divergence, the perfect route is updated to be the
//					 * straight line from the robot's previous location to the final destination.
//					 * This is "fairer" since the robot is always trying to get to the final destination
//					 * from is present location and not necessarily follow the straight line from the
//					 * edge's start location to final location.
//					 */
//					relativePerfectRoute = new Line(currLoc, currEdge.getEndLocation());
//					
//					// Some debug output
////					System.out.println("currLoc: " + currLoc);
////					System.out.println("destLoc: " + currEdge.getEndLocation());
////					System.out.println("Updating perfect route to be: " + perfectRoute);
//				}
//			}
//		}
//		
//		saveResults(outputFileName + "-absoluteDivergence.txt", absoluteDivs);
//		saveResults(outputFileName + "-relativeDivergence.txt", relativeDivs);
//		saveResults(outputFileName + "-relativeSpeedDivergence.txt", relativeSpeedDivs);
//	}
	
	/**
	 * Saves the results into a text file.
	 * 
	 * @param fileName The name of the file in which to save the results.
	 * @param divs A vector containing the divergences of the robots.
	 */
//	private void saveResults(String fileName, Vector<DivergenceExp> divs) {
//		FileLogger fileWriter = new FileLogger(fileName, false);
//		
//		// Create and save the table header
//		StringBuffer tableHeader = new StringBuffer("Dest. Waypoint\tPct. Complete\tIndex");
//		Enumeration<String> expLabelsEnum = expLabels.elements();
//		while (expLabelsEnum.hasMoreElements()) {
//			tableHeader.append("\t");
//			tableHeader.append(expLabelsEnum.nextElement());
//		}
//		tableHeader.append("\t");
//		tableHeader.append("Average");
//		tableHeader.append("\t");
//		tableHeader.append("Std. Dev.");
//		tableHeader.append("\t");
//		tableHeader.append("95% Conf.");
//		log(fileWriter, tableHeader.toString());
//		
//		// For each way point...
//		for (int wayPointIndx = 0; wayPointIndx < motionScript.numWayPoints(); wayPointIndx++) {
//			
//			// For each percentage completed...
//			for (double pctComplete = 0; pctComplete < 100; pctComplete += 10) {
//				
//				StringBuffer sb = new StringBuffer();
//				sb.append(wayPointIndx+1);
//				sb.append("\t");
//				sb.append(pctComplete);
//				sb.append("\t");
//				sb.append(wayPointIndx + (pctComplete/100));
//				
//				
////				if (pctComplete == 0) {
////					sb.append(wayPointIndx+1);
////				} else
////					sb.append("\t")
//				
//				Vector<Double> allData = new Vector<Double>();
//				
//				// For each experiment get and save the divergence...
//				for (int expIndx = 0; expIndx < divs.size(); expIndx++) {
//					double divergence = divs.get(expIndx).getDivergence(wayPointIndx, pctComplete);
//					sb.append("\t");
//					sb.append(divergence);
//					allData.add(divergence);
//				}
//				
//				// Generate some statistics about the divergence at pctComplete across all experiments.
//				double avg = pharoslabut.util.Stats.getAvg(allData);
//				double stddev = pharoslabut.util.Stats.getSampleStdDev(allData);
//				double conf95 = pharoslabut.util.Stats.getConf95(stddev, allData.size());
//				
//				sb.append("\t");
//				sb.append(avg);
//				sb.append("\t");
//				sb.append(stddev);
//				sb.append("\t");
//				sb.append(conf95);
//				
//				log(fileWriter, sb.toString());
//			}
//		}
//		
//	}
	
//	private void logDbg(String msg) {
//		
//		// Add the name of the calling method to the beginning of the message.
//		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
//		String result = getClass().getName() + ": " + stackTraceElements[2].getMethodName() + ": " + msg;
//		
//		//stackTraceElements[2].getClass().getName();
//		
//		if (System.getProperty ("PharosMiddleware.debug") != null)
//			System.out.println(result);
//	}
//	
//	private void logDbg(String msg, FileLogger flogger) {
//		String result = getClass().getName() + ": " + msg; 
//		if (System.getProperty ("PharosMiddleware.debug") != null)
//			System.out.println(result);
//		if (flogger != null) 
//			flogger.log(result);
//	}
//	
//	private void log(String msg, FileLogger flogger) {
//		System.out.println(msg);
//		if (flogger != null)
//			flogger.log(msg);
//	}
	
//	/**
//	 * Holds the divergence exhibited by a robot throughout an experiment.
//	 * 
//	 * @author Chien-Liang Fok
//	 */
//	private class DivergenceExp {
//		
//		private RobotExpData robotExpData;
//		
//		private Vector<DivergenceEdge> edges = new Vector<DivergenceEdge>();
//		
//		public DivergenceExp(RobotExpData robotExpData) {
//			this.robotExpData = robotExpData;
//		}
//		
//		public void addEdge(DivergenceEdge edge) {
//			edges.add(edge);
//		}
//		
//		/**
//		 * Returns the divergence of the robot as it travels towards a certain waypoint.
//		 * 
//		 * @param wayPointIndx The destination way point.  The way point index is the way point number minus one.
//		 * @param pctComplete The percentage of the edge traversal that is complete.
//		 * @return The divergence of the robot.
//		 */
//		public double getDivergence(int wayPointIndx, double pctComplete) {
//			DivergenceEdge de = edges.get(wayPointIndx);
//			return de.getDivergence(pctComplete);
//		}
//		
//		/**
//		 * @return The raw experiment data recorded by the robot that is the source of the divergence data.
//		 */
//		public RobotExpData getRobotExpData() {
//			return robotExpData;
//		}
//		
//		public String toString() {
//			StringBuffer sb = new StringBuffer("Divergence for experiment: ");
//			sb.append(robotExpData.getFileName());
//			sb.append("\n");
//			
//			for (int i=0; i < edges.size(); i++) {
//				DivergenceEdge currEdge = edges.get(i);
//				sb.append(currEdge.toString());
//				sb.append("\n");
//			}
//			
//			return sb.toString();
//		}
//	}
	
//	/**
//	 * Hold the divergence exhibited by a robot as it traversed a single edge of a motion script.
//	 * 
//	 * @author Chien-Liang Fok
//	 *
//	 */
//	private class DivergenceEdge {
//		
//		/**
//		 * The destination way point at the end of this edge.
//		 */
//		private int destWayPoint;
//		
//		
//		/**
//		 * The divergences from the ideal path that the robot actually traversed.
//		 */
//		private Vector<SpatialDivergence> divergences = new Vector<SpatialDivergence>();
//		
//		/**
//		 * The constructor.
//		 * 
//		 * @param destWayPoint The destination way point.
//		 */
//		public DivergenceEdge(int destWayPoint) {
//			this.destWayPoint = destWayPoint;
//		}
//		
//		/**
//		 * Adds a divergence measurement to this edge.
//		 * 
//		 * @param div The divergence measurement.
//		 */
//		public void addDivergence(SpatialDivergence div) {
//			divergences.add(div);
//		}
//		
//		/**
//		 * Returns the divergence of the robot when it has traveled a certain 
//		 * percentage of the edge.
//		 * 
//		 * @param pctComplete The percentage traversed.
//		 * @return The divergence of the robot.
//		 */
//		public double getDivergence(double pctComplete) {
//			Enumeration<SpatialDivergence> divEnum = divergences.elements();
//			while (divEnum.hasMoreElements()) {
//				SpatialDivergence div = divEnum.nextElement();
//				if (div.getPctComplete() == pctComplete)
//					return div.getDivergence();
//			}
//			logErr("DivergenceEdge: getDivergence: ERROR: Unable to get divergence of robot at " + pctComplete + "% completion");
//			System.exit(1);
//			return -1; // should never get here...
//		}
//		
//		/**
//		 * @return The destination way point.
//		 */
//		public int getDestWayPoint() {
//			return destWayPoint;
//		}
//		
//		/**
//		 * @return The divergence measurements along this edge.
//		 */
//		public Vector<SpatialDivergence> getDivergences() {
//			return divergences;
//		}
//		
//		public String toString() {
//			StringBuffer sb = new StringBuffer("Towards Way Point ");
//			sb.append(getDestWayPoint());
//			sb.append("\n");
//			
//			for (int i=0; i < divergences.size(); i++) {
//				SpatialDivergence div = divergences.get(i);
//				sb.append(div.toString());
//				sb.append("\n");
//			}
//			
//			return sb.toString();
//		}
//	}
	
	/**
	 * A spatial divergence measurement.
	 *
	 * @author Chien-Liang Fok
	 */
	private class SpatialDivergence {
		/**
		 * The time stamp of the divergence measurement.
		 */
		private long timeStamp;
		
		/**
		 * The ideal path along which the robot should traverse.
		 */
		private Line idealPath;
		
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
		 * The constructor.
		 * 
		 * @param timeStamp The time stamp of the divergence measurement.
		 * @param currLoc The current location.
		 * @param idealLoc The ideal location.
		 * @param idealPath The ideal path along which the robot should travel.
		 */
		public SpatialDivergence(long timeStamp, Location currLoc, Location idealLoc, Line idealPath) {
			this.timeStamp = timeStamp;
			this.currLoc = currLoc;
			this.idealLoc = idealLoc;
			this.idealPath = idealPath;
		}
		
		/**
		 * @return The time stamp.
		 */
		public long getTimeStamp() {
			return timeStamp;
		}
		
		/**
		 * @return The ideal edge.
		 */
		public Line getIdealPath() {
			return idealPath;
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
			return currLoc.distanceTo(idealLoc);
		}
		
		public String toString() {
			return timeStamp + "\t" + "\t" + currLoc + "\t" + idealLoc + "\t" + getDivergence();
		}
	}
	
	/**
	 * A temporal divergence measurement.
	 *
	 * @author Chien-Liang Fok
	 */
	private class TemporalDivergence {
		
		/**
		 * The waypoint sequence number, starting from zero.
		 */
		private int waypointNumber;
		
		/**
		 * The ideal time at which the robot should have arrived at the waypoint.
		 * This is based on when the robot first started to move.
		 */
		private long idealTOA;
		
		/**
		 * The ideal time that the robot should have reached the waypoint relative to
		 * when it left the previous waypoint.
		 */
		private long idealRelTOA;
		
		/**
		 * The actual time when the robot reached the waypoint.
		 */
		private long actualTOA;
		
		/**
		 * The constructor.
		 * 
		 * @param waypointNumber The waypoint sequence number, starting from zero.
		 * @param idealTOA The ideal time at which the robot should have arrived at the waypoint.
		 * This is based on when the robot first started to move.
		 * @param idealRelTOA The ideal time that the robot should have reached the waypoint relative to
		 * when it left the previous waypoint.
		 * @param actualTOA The actual time when the robot reached the waypoint.
		 */
		public TemporalDivergence(int waypointNumber, long idealTOA, long idealRelTOA, long actualTOA) {
			this.waypointNumber = waypointNumber;
			this.idealTOA = idealTOA;
			this.idealRelTOA = idealRelTOA;
			this.actualTOA = actualTOA;
		}
		
		public int getWaypointNumber() {
			return waypointNumber;
		}
		
		public long getIdealTOA() {
			return idealTOA;
		}
		
		public long getIdealRelTOA() {
			return idealRelTOA;
		}
		
		public long getActualTOA() {
			return actualTOA;
		}
		
		/**
		 * 
		 * @return The temporal divergence in milliseconds.
		 */
		public long getDivergence() {
			return actualTOA - idealTOA;
		}
		
		/**
		 * 
		 * @return The relative divergence in milliseconds.
		 */
		public long getRelDivergence() {
			return actualTOA - idealRelTOA;
		}
		
		public String toString() {
			return "TemporalDivergence: waypointNumber=" + waypointNumber + "idealTOA=" + idealTOA + ", idealRelTOA=" + idealRelTOA 
			+ ", actualTOA=" + actualTOA + ", temporal divergence=" + getDivergence();
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
	
	
//	/**
//	 * Reads the spec file and initializes variables outputFileName, motionScript, logFileNames, and logFileLabels.
//	 * 
//	 * @param specFile The specification file.
//	 */
//	private void readSpecFile(String specFile) {
//		// Open the specification file
//		BufferedReader input = null;
//		try {
//			input =  new BufferedReader(new FileReader(specFile));
//		} catch (IOException ex){
//			ex.printStackTrace();
//			logErr("Unable to open " + specFile);
//			System.exit(1);
//		}
//		
//		try {
//			String line = null;
//			int lineno = 1;
//			while (( line = input.readLine()) != null) {
//				if (!line.equals("")) {
//					if (line.contains("OUTPUT_FILE")) {
//						String[] elem = line.split("[\\s]+");
//						
//						if (elem.length > 1) {
//							outputFileName = elem[1];
//						} else {
//							System.err.println("ERROR: Syntax error on line " + lineno + " of file " + specFile + ": outfile file not specified.");
//							System.exit(1);
//						}
//					}
//					else if (line.contains("LOG_FILE")) {
//						String[] elem = line.split("[\\s]+");
//					
//						robotExpData.add(new RobotExpData(elem[1]));
//						
//						if (elem.length > 2)
//							expLabels.add(elem[2]);  // user specified the caption name
//						else {
//							// use the mission, experiment, and robot names as the label
//							String label = LogFileNameParser.extractMissionName(elem[1]) 
//								+ "-" + LogFileNameParser.extractExpName(elem[1])
//								+ "-" + LogFileNameParser.extractRobotName(elem[1]); 
//							expLabels.add(label);
//						}
//					}
//					else if (line.contains("MOTION_SCRIPT")) {
//						String[] elem = line.split("[\\s]+");
//						
//						if (elem.length > 1)
//							motionScript = new MotionScript(elem[1]);
//						else {
//							System.err.println("ERROR on line " + lineno + " of file " + specFile + ": Motion script not specified.");
//							System.exit(1);
//						}
//					}
//				}
//				lineno++;
//			}
//			input.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		// Perform some sanity checks to ensure we have all the information we need to perform the 
//		// motion divergence analysis.
//		if (outputFileName == null) {
//			logErr("ERROR: output file name not specified.");
//			System.exit(1);
//		}
//		
//		if (motionScript == null) {
//			logErr("ERROR: motion script not specified.");
//			System.exit(1);
//		}
//		
//		if (robotExpData.size() == 0) {
//			logErr("ERROR: No log files specified.");
//			System.exit(1);
//		}
//	}
	
//	private void log(String msg) {
//		if (System.getProperty ("PharosMiddleware.debug") != null)
//			System.out.println(msg);
//	}
//	
//	private void logErr(String msg) {
//		// Add the name of the calling method to the beginning of the message.
//		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
//		String callingMethodName = stackTraceElements[2].getMethodName();
//		
//		String result = getClass().getName() + ": " + callingMethodName + ": ERROR: " + msg;
//		System.err.println(result);
//	}
	
	private static void print(String msg) {
		System.out.println(msg);
	}
	
	private static void printErr(String msg) {
		System.err.println(msg);
	}
	
	private static void usage() {
		print("Usage: " + MotionDivergenceAnalyzer.class.getName() + " <options>\n");
		print("Where <options> include:");
		print("\t-log <log file name>: The experiment log file generated by the robot. (required)");
		print("\t-interval <sampling interval>: The interval in milliseconds at which the divergence is calculated. (default 1000)");
		print("\t-orientedThreshold <oriented threshold>: The maximum heading error in radians before a robot is considered oriented. (default 0.25)");
		print("\t-saveReults: Save the results into a file.");
		print("\t-d or -debug: Enable debug mode.");
	}
	
	public static void main(String[] args) {
		String logFileName = null;
		double orientedThreshold = 0.25;
		long samplingInterval = 1000;
		boolean saveResults = false;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-h") || args[i].equals("-help")) {
					usage();
					System.exit(0);
				} 
				else if (args[i].equals("-log")) {
					logFileName = args[++i];
				}
				else if (args[i].equals("-interval")) {
					samplingInterval = Long.valueOf(args[++i]);
				}
				else if (args[i].equals("-orientedThreshold")) {
					orientedThreshold = Double.valueOf(args[++i]);
				}
				else if (args[i].equals("-saveResults")) {
					saveResults = true;
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
		
		if (logFileName == null) {
			printErr("Must specify log file.");
			usage();
			System.exit(1);
		}
		
		print("Log: " + logFileName);
		print("Sampling Interval: " + samplingInterval);
		print("Oriented Threshold: " + orientedThreshold);
		print("Save results: " + saveResults);
		print("Debug: " + (System.getProperty ("PharosMiddleware.debug") != null));
		
		new MotionDivergenceAnalyzer(logFileName, samplingInterval, orientedThreshold, saveResults);
	}
}
