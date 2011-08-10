package pharoslabut.logger.analyzer;

import pharoslabut.logger.*;

import java.awt.Color;
import java.util.*;

import org.jfree.ui.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
//import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
//import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Calculates the difference between the robot's heading
 * and the ideal heading.  The ideal heading is the direction the robot 
 * must face to point to the next waypoint.
 * Displays the resulting error on a graph, and optionally saves it in a file.
 * 
 * @author Chien-Liang Fok
 */
public class GetHeadingError {
	private String logFileName;
	
	private RobotExpData robotData;
	
	private Vector<HeadingDivergenceState> divergenceData = new Vector<HeadingDivergenceState>();
	
	/**
	 * The constructor.
	 * 
	 * @param logFileName The experiment log file to analyze.
	 * @param samplingInterval  The interval at which to calculate the heading error in milliseconds.
	 * @param saveToFile whether to save the error calculations to a file.
	 */
	public GetHeadingError(String logFileName, long samplingInterval, boolean saveToFile) {
		this.logFileName = logFileName;
		robotData = new RobotExpData(logFileName);
		
		long startTime = robotData.getStartTime();
		
		// Calculate heading divergence every sampling interval
		for (long time = startTime; time < robotData.getStopTime(); time += samplingInterval) {
			
			// Only do the calculation if the robot has started to move.
			if (time >= robotData.getPathEdge(0).getStartTime())
				divergenceData.add(getHeadingError(time));
		}
		
		if (saveToFile)
			saveToFile();
		showPlot();
	}
	
	private void saveToFile() {
		String fileName;
		if (logFileName.contains("."))
			fileName = logFileName.substring(logFileName.lastIndexOf('.')) + "-headingError.txt";
		else
			fileName = logFileName + "-headingError.txt";
		
		FileLogger flogger = new FileLogger(fileName, false);
		long startTime = robotData.getStartTime();
		
		log("Time (ms)\tDelta Time (ms)\tActual Heading\tIdeal Heading\tHeading Error", flogger);
		Enumeration<HeadingDivergenceState> e = divergenceData.elements();
		while (e.hasMoreElements()) {
			HeadingDivergenceState currState = e.nextElement();
			log(currState.time 
					+ "\t" + (currState.time - startTime) 
					+ "\t" + currState.currHeading 
					+ "\t" + currState.idealHeading 
					+ "\t" + currState.headingError, flogger);
		}
		
		// Print when the robot starts traversing an edge...
		log("Start Times of Edge Traversal:", flogger);
		log("Time (ms)\tDelta Time (ms)\tDummy", flogger);
		Vector<PathEdge> pathEdges = robotData.getPathEdges();
		Enumeration<PathEdge> e2 = pathEdges.elements();
		while (e2.hasMoreElements()) {
			PathEdge currEdge = e2.nextElement();
			long relStartTime = (currEdge.getStartTime() - robotData.getStartTime())/1000;
			log(currEdge.getStartTime() + "\t" + relStartTime + "\t" + 0, flogger);
		}
	}
	
	/**
	 * Calculates the heading error of the robot at the specified time.
	 * 
	 * @param time The time to consider.
	 * @return The heading error of the robot.
	 */
	private HeadingDivergenceState getHeadingError(long time) {
		double currHeading = robotData.getHeading(time);
		double idealHeading = robotData.getIdealHeading(time);
		double headingError = pharoslabut.navigate.Navigate
			.headingError(currHeading, idealHeading);
		return new HeadingDivergenceState(time, currHeading, idealHeading, headingError);
	}
	
	/**
	 * Displays the data in a graph.
	 */
	private void showPlot() {
		// Create a data series containing the heading error data...
		XYSeries headingErrorSeries = new XYSeries("Heading Error");
		Enumeration<HeadingDivergenceState> e = divergenceData.elements();
		while (e.hasMoreElements()) {
			HeadingDivergenceState currState = e.nextElement();
			headingErrorSeries.add((currState.time - robotData.getStartTime())/1000, currState.headingError);
		}
		
		// Create a data series containing the times when the robot starts heading towards a waypoint
		XYSeries waypointSeries = new XYSeries("Begin Edge Traveral");
		Vector<PathEdge> pathEdges = robotData.getPathEdges();
		Enumeration<PathEdge> e2 = pathEdges.elements();
		while (e2.hasMoreElements()) {
			PathEdge currEdge = e2.nextElement();
			long waypointTime = (currEdge.getStartTime() - robotData.getStartTime())/1000;
//			HeadingDivergenceState headingErrorState = getHeadingError(currEdge.getEndTime());
			
			waypointSeries.add(waypointTime, 0); //headingErrorState.headingError);
//			log(waypointArrivalTime + ", " + headingErrorState.headingError, null);
		}

		// Create a dataset out of the data series
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(headingErrorSeries);
		dataset.addSeries(waypointSeries);
		
		// Create the chart
        JFreeChart chart = ChartFactory.createXYLineChart(
            "Heading Error vs. Time",            // chart title
            "Time (s)",                          // x axis label
            "Heading Error (radians)",           // y axis label
            dataset,                             // the data
            PlotOrientation.VERTICAL,            // plot orientation (y axis is vertical)
            true,                                // include legend
            true,                                // tooltips
            false                                // urls
        );
        
        chart.setBackgroundPaint(Color.white);
        
        XYPlot plot = chart.getXYPlot();
//        plot.setBackgroundPaint(Color.lightGray);
//    //    plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
//        plot.setDomainGridlinePaint(Color.white);
//        plot.setRangeGridlinePaint(Color.white);
        
        // Display the points and not the lines connecting the points
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, true); // display the heading errors as a line
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesLinesVisible(1, false); // display the waypoints as a point
        renderer.setSeriesShapesVisible(1, true);
        plot.setRenderer(renderer);
        
        
//        final NumberAxis domainAxis = (NumberAxis)plot.getDomainAxis();
//        domainAxis.setRange(new Range(0,140));

        
//        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
//     // change the auto tick unit selection to integer units only...
////        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
//        rangeAxis.setRange(new Range(-Math.PI, Math.PI));     
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(1000, 600));
       
       
        // Create a frame for the chart, then display it.
        ApplicationFrame appFrame = new ApplicationFrame("Heading Error for " + logFileName);
        appFrame.setContentPane(chartPanel);
        appFrame.pack();
		RefineryUtilities.centerFrameOnScreen(appFrame);
		appFrame.setVisible(true);
	}
	
	private class HeadingDivergenceState {
		long time;
		double currHeading, idealHeading, headingError;
		
		public HeadingDivergenceState(long time, double currHeading, double idealHeading, double headingError) {
			this.time = time;
			this.currHeading = currHeading;
			this.idealHeading = idealHeading;
			this.headingError = headingError;
		}
	}
	
	/**
	 * Logs a debug message.  This message is only printed if we're running
	 * in debug mode.
	 * 
	 * @param msg The message to log.
	 */
//	private void logDbg(String msg) {
//		if (System.getProperty ("PharosMiddleware.debug") != null)
//			log(msg);
//	}
	
	/**
	 * Logs a message.  This message is always printed regardless of
	 * whether we are running in debug mode.
	 * 
	 * @param msg The message to log.
	 */
//	private void log(String msg) {
//		log(msg, null);
//	}
	
	private void log(String msg, FileLogger flogger) {
		String result = getClass().getName() + ": " + msg;
		System.out.println(result);
		if (flogger != null)
			flogger.log(msg);
	}
	
//	private void logErr(String msg) {
//		logErr(msg, null);
//	}
	
//	private void logErr(String msg, FileLogger flogger) {
//		String result = "HeadingDivergence: ERROR: " + msg;
//		System.err.println(result);
//		if (flogger != null)
//			flogger.log(msg);
//	}
	
	private static void print(String msg) {
		System.out.println(msg);
	}
	
	private static void printErr(String msg) {
		System.err.println(msg);
	}
	
	private static void usage() {
		print("Usage: " + GetHeadingError.class.getName()  + " <options>\n");
		print("Where <options> include:");
		print("\t-log <log file name>: The log file generated during the experiment. (required)");
		print("\t-save: Save the heading errors in a text file.  The name will be the same as the log file but ending with \"-headingError.txt\" (optional)");
		print("\t-interval <sampling interval>: The interval at which to calculate the heading error in milliseconds. (optional, default 500)");
		print("\t-debug or -d: Enable debug mode");
	}
	
	public static void main(String[] args) {
		String logFileName = null;
		long samplingInterval = 500;
		boolean saveToFile = false;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-h") || args[i].equals("-help")) {
					usage();
					System.exit(0);
				}
				else if (args[i].equals("-save")) {
					saveToFile = false;
				}
				else if (args[i].equals("-interval")) {
					samplingInterval = Long.valueOf(args[++i]);
				}
				else if (args[i].equals("-log")) {
					logFileName = args[++i];
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
		
		print("Log file: " + logFileName);
		print("Debug: " + (System.getProperty ("PharosMiddleware.debug") != null));
		
		try {
			new GetHeadingError(logFileName, samplingInterval, saveToFile);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
