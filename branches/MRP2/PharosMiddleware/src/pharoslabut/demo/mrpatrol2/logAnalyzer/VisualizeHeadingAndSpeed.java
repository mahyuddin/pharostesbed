package pharoslabut.demo.mrpatrol2.logAnalyzer;

import pharoslabut.logger.*;
import pharoslabut.logger.analyzer.PathEdge;

import java.awt.Color;
import java.util.*;

import org.jfree.ui.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
//import org.jfree.chart.axis.NumberAxis;
//import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
//import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Plots the heading, speed, heading command, and speed command of the robot as it follows GPS way points.
 * 
 * @author Chien-Liang Fok
 */
public class VisualizeHeadingAndSpeed {
	
	private String logFileName;
	
	private RobotExpData robotData;
	
	private Vector<SpeedHeadingState> speedHeadingData = new Vector<SpeedHeadingState>();
	
	/**
	 * The constructor.
	 * 
	 * @param logFileName The experiment log file to analyze.
	 * @param samplingInterval  The interval at which to calculate the heading error in milliseconds.
	 * @param saveToFile whether to save the error calculations to a file.
	 */
	public VisualizeHeadingAndSpeed(String logFileName, long samplingInterval, boolean saveToFile) {
		this.logFileName = logFileName;
		robotData = new RobotExpData(logFileName);
		
		long startTime = robotData.getStartTime();
		
		// Calculate heading and speed of the robot every sampling interval.
		for (long time = startTime; time < robotData.getStopTime(); time += samplingInterval) {
			SpeedHeadingState dataPoint = new SpeedHeadingState(time, 
					robotData.getHeading(time), robotData.getSpeed(time),
					robotData.getHeadingCmd(time), robotData.getSpeedCmd(time));
			speedHeadingData.add(dataPoint);			
		}
		
		if (saveToFile)
			saveToFile();

		showPlot();
	}
	
	private void saveToFile() {
		String fileName;
		if (logFileName.contains("."))
			fileName = logFileName.substring(0, logFileName.lastIndexOf('.')) + "-headingSpeed.txt";
		else
			fileName = logFileName + "-headingSpeed.txt";
		
		log("Saving data to " + fileName, null);
		FileLogger flogger = new FileLogger(fileName, false);
		long startTime = robotData.getStartTime();
		
		flogger.log("Time (ms)\tDelta Time (ms)\tDelta Time (s)\tHeading (radians)\tHeading Command (radians)\tSpeed (m/s)\tSpeed Command (m/s)");
		Enumeration<SpeedHeadingState> e = speedHeadingData.elements();
		while (e.hasMoreElements()) {
			SpeedHeadingState currState = e.nextElement();
			flogger.log(currState.time 
					+ "\t" + (currState.time - startTime) 
					+ "\t" + (currState.time - startTime) / 1000.0 
					+ "\t" + currState.heading 
					+ "\t" + currState.headingCmd
					+ "\t" + currState.speed
					+ "\t" + currState.speedCmd);
		}
		
		// Print when the robot starts traversing an edge...
		flogger.log("Start Times of Edge Traversal:");
		flogger.log("Time (ms)\tDelta Time (ms)\tDummy");
		Vector<PathEdge> pathEdges = robotData.getPathEdges();
		Enumeration<PathEdge> e2 = pathEdges.elements();
		while (e2.hasMoreElements()) {
			PathEdge currEdge = e2.nextElement();
			long relStartTime = (currEdge.getStartTime() - robotData.getStartTime())/1000;
			flogger.log(currEdge.getStartTime() + "\t" + relStartTime + "\t" + 0);
		}
		
		// Print when the robot arrives at a waypoint...
		flogger.log("Waypoint arrival times:");
		flogger.log("Time (ms)\tDelta Time (ms)\tDummy");
		e2 = pathEdges.elements();
		while (e2.hasMoreElements()) {
			PathEdge currEdge = e2.nextElement();
			long relEndTime = (currEdge.getEndTime() - robotData.getStartTime())/1000;
			flogger.log(currEdge.getEndTime() + "\t" + relEndTime + "\t" + 0);
		}
	}
	
	/**
	 * Displays the data in a graph.
	 */
	private void showPlot() {
		// Create the various data series...
		XYSeries headingSeries = new XYSeries("Estimated Heading");
		XYSeries headingCmdSeries = new XYSeries("Steering Angle Command");
		XYSeries speedSeries = new XYSeries("Estimated Speed");
		XYSeries speedCmdSeries = new XYSeries("Desired Speed");
		
		Enumeration<SpeedHeadingState> e = speedHeadingData.elements();
		while (e.hasMoreElements()) {
			SpeedHeadingState currState = e.nextElement();
			double currTime = (currState.time - robotData.getStartTime())/1000.0;
			headingSeries.add(currTime, currState.heading);
			headingCmdSeries.add(currTime, currState.headingCmd);
			speedSeries.add(currTime, currState.speed);
			speedCmdSeries.add(currTime, currState.speedCmd);
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

		// Create two data sets, one containing heading and headingCmd, and the other
		// containing speed and speedCmd data.
		XYSeriesCollection headingDataSet = new XYSeriesCollection();
		headingDataSet.addSeries(headingSeries);
		headingDataSet.addSeries(headingCmdSeries);
		headingDataSet.addSeries(beginEdgeSeries);
		headingDataSet.addSeries(waypointArrivalSeries);
		
		XYSeriesCollection speedDataSet = new XYSeriesCollection();
		speedDataSet.addSeries(speedSeries);
		speedDataSet.addSeries(speedCmdSeries);
		speedDataSet.addSeries(beginEdgeSeries);
		speedDataSet.addSeries(waypointArrivalSeries);
		
		// Create the charts
		JFreeChart headingChart = ChartFactory.createXYLineChart(
				"Heading vs. Time",                                    // chart title
				"Time (s)",                                            // x axis label
				"Heading (radians)",                                   // y axis label
				headingDataSet,                                        // the heading data
				PlotOrientation.VERTICAL,                              // plot orientation (y axis is vertical)
				true,                                                  // include legend
				true,                                                  // tooltips
				false                                                  // urls
		);
        
        JFreeChart speedChart = ChartFactory.createXYLineChart(
        		"Speed vs. Time",                                      // chart title
        		"Time (s)",                                            // x axis label
        		"Speed (m/s)",                                         // y axis label
        		speedDataSet,                                          // the speed data
        		PlotOrientation.VERTICAL,                              // plot orientation (y axis is vertical)
        		true,                                                  // include legend
        		true,                                                  // tooltips
        		false                                                  // urls
        );
        
        // Place the legend on top of the chart just below the title.
        LegendTitle headingLegend = headingChart.getLegend();
        headingLegend.setPosition(RectangleEdge.TOP);
        LegendTitle speedLegend = speedChart.getLegend();
        speedLegend.setPosition(RectangleEdge.TOP);
        
        headingChart.setBackgroundPaint(Color.white);
        speedChart.setBackgroundPaint(Color.white);
        
        // Configure when to display lines an when to display the shapes that indicate data points
        XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer();
        renderer1.setSeriesLinesVisible(0, true); // display the heading as a line
        renderer1.setSeriesShapesVisible(0, false);
        renderer1.setSeriesPaint(0, Color.BLACK);
        renderer1.setSeriesLinesVisible(1, true); // display the headingCmd as a line
        renderer1.setSeriesShapesVisible(1, false);
        renderer1.setSeriesPaint(1, Color.RED);
        renderer1.setSeriesLinesVisible(2, false); // display the begin edge traversal points as blue dots
        renderer1.setSeriesShapesVisible(2, true);
        renderer1.setSeriesPaint(2, Color.BLUE);
        renderer1.setSeriesShape(2, new java.awt.geom.Ellipse2D.Double(-3,-3,6,6));
        renderer1.setSeriesLinesVisible(3, false); // display the begin edge traversal points as green dots
        renderer1.setSeriesShapesVisible(3, true);
        renderer1.setSeriesPaint(3, Color.GREEN.darker());
        renderer1.setSeriesShape(3, new java.awt.geom.Ellipse2D.Double(-5,-5,10,10));
        
        final XYPlot headingPlot = headingChart.getXYPlot();
        headingPlot.setRenderer(0, renderer1);
        
        XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer();
        renderer2.setSeriesLinesVisible(0, true); // display the speed as a line
        renderer2.setSeriesShapesVisible(0, false);
        renderer2.setSeriesPaint(0, Color.BLACK);
        renderer2.setSeriesLinesVisible(1, true); // display the speedCmd as a line
        renderer2.setSeriesShapesVisible(1, false);
        renderer2.setSeriesPaint(1, Color.RED);
        renderer2.setSeriesLinesVisible(2, false); // display the waypoints as points
        renderer2.setSeriesShapesVisible(2, true);
        renderer2.setSeriesPaint(2, Color.BLUE);
        renderer2.setSeriesShape(2, new java.awt.geom.Ellipse2D.Double(-5,-5,10,10));
        renderer2.setSeriesLinesVisible(3, false); // display the begin edge traversal points as green dots
        renderer2.setSeriesShapesVisible(3, true);
        renderer2.setSeriesPaint(3, Color.GREEN);
        renderer2.setSeriesShape(3, new java.awt.geom.Ellipse2D.Double(-5,-5,10,10));
        
        final XYPlot speedPlot = speedChart.getXYPlot();
        speedPlot.setRenderer(0, renderer2);
        
        // Place the charts in their own panels.
        ChartPanel headingChartPanel = new ChartPanel(headingChart);
        headingChartPanel.setPreferredSize(new java.awt.Dimension(1200, 500));
        ChartPanel speedChartPanel = new ChartPanel(speedChart);
        speedChartPanel.setPreferredSize(new java.awt.Dimension(1200, 500));
       
        // Place both chart panels within a single panel with two rows.
        javax.swing.JPanel chartsPanel = new javax.swing.JPanel(new java.awt.GridLayout(2,1));
        chartsPanel.add(headingChartPanel);
        chartsPanel.add(speedChartPanel);
       
        // Create a frame for the chart, then display it.
        ApplicationFrame appFrame = new ApplicationFrame("Heading and Speed for " + logFileName);
        appFrame.setContentPane(chartsPanel);
        appFrame.pack();
		RefineryUtilities.centerFrameOnScreen(appFrame);
		appFrame.setVisible(true);
	}
	
	private class SpeedHeadingState {
		long time;
		double heading, speed, headingCmd, speedCmd;
		
		public SpeedHeadingState(long time, double heading, double speed, 
				double headingCmd, double speedCmd) 
		{
			this.time = time;
			this.heading = heading;
			this.speed = speed;
			this.headingCmd = headingCmd;
			this.speedCmd = speedCmd;
		}
	}
	
	/**
	 * Logs a debug message.  This message is only printed if we're running
	 * in debug mode.
	 * 
	 * @param msg The message to log.
	 */
//	private void logDbg(String msg) {
//		String result = getClass().getName() + ": " + msg;
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
		System.out.println(msg);
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
		print("Usage: " + VisualizeHeadingAndSpeed.class.getName()  + " <options>\n");
		print("Where <options> include:");
		print("\t-log <log file name>: The log file generated during the experiment. (required)");
		print("\t-save: Save the movement and movement commands in a text file.  The name will be the same as the log file but ending with \"-headingSpeed.txt\" (optional)");
		print("\t-interval <sampling interval>: The interval at which to calculate the movement and movement commands in milliseconds. (optional, default 500)");
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
					saveToFile = true;
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
			new VisualizeHeadingAndSpeed(logFileName, samplingInterval, saveToFile);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}