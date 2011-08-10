package pharoslabut.logger.analyzer;

import pharoslabut.logger.*;

import java.awt.Color;
import java.util.*;

import org.jfree.ui.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Calculates the divergence between the robot's heading
 * and the ideal heading.  The ideal heading is the direction the robot 
 * must face to point to the next waypoint.
 * 
 * @author Chien-Liang Fok
 */
public class GetHeadingError {
	/**
	 * The interval at which to calculate the heading error in milliseconds.
	 */
	public static final long HEADING_ERROR_CALCULATION_INTERVAL = 500;

	private String logFileName;
	
	RobotExpData robotData;
	
	private Vector<HeadingDivergenceState> divergenceData = new Vector<HeadingDivergenceState>();
	
	/**
	 * The constructor.
	 * 
	 * @param logFileName The experiment log file to analyze.
	 */
	public GetHeadingError(String logFileName) {
		this.logFileName = logFileName;
		robotData = new RobotExpData(logFileName);
		//FileLogger flogger = new FileLogger(outputFileName, false);
		
		long startTime = robotData.getStartTime();
		
		//log("Time (ms)\tDelta Time (ms)\tActual Heading\tIdeal Heading\tHeading Error", flogger);
		
		// Calculate heading divergence every 1s
		for (long time = startTime; time < robotData.getStopTime(); time += HEADING_ERROR_CALCULATION_INTERVAL) {
			double currHeading = robotData.getHeading(time);
			double idealHeading = robotData.getIdealHeading(time);
			double headingError = pharoslabut.navigate.Navigate
				.headingError(currHeading, idealHeading);
			
			divergenceData.add(new HeadingDivergenceState(time, currHeading, idealHeading, headingError));
			//log(time + "\t" + (time - startTime) + "\t" + currHeading + "\t" + idealHeading 
			//		+ "\t" + headingError, flogger);
		}
		
		showPlot();
		
	}
	
	/**
	 * Displays the data in a graph.
	 */
	private void showPlot() {
		// Create a data series containing the heading error data...
		XYSeries dataSeries = new XYSeries("Heading Divergence Measurements");
		Enumeration<HeadingDivergenceState> e = divergenceData.elements();
		while (e.hasMoreElements()) {
			HeadingDivergenceState currState = e.nextElement();
			dataSeries.add((currState.time - robotData.getStartTime())/1000, currState.headingError);
		}

		// Create a dataset out of the data series
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(dataSeries);
		
		// Create the chart
        JFreeChart chart = ChartFactory.createXYLineChart(
            "Heading Error Measurements",        // chart title
            "Time (s)",                          // x axis label
            "Heading Error (radians)",           // y axis label
            dataset,                             // the data
            PlotOrientation.VERTICAL,            // plot orientation (y axis is vertical)
            false,                               // include legend
            true,                                // tooltips
            false                                // urls
        );
        
        chart.setBackgroundPaint(Color.white);
        
        XYPlot plot = chart.getXYPlot();
//        plot.setBackgroundPaint(Color.lightGray);
//    //    plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
//        plot.setDomainGridlinePaint(Color.white);
//        plot.setRangeGridlinePaint(Color.white);
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShapesVisible(1, false);
        plot.setRenderer(renderer);
        
//        final NumberAxis domainAxis = (NumberAxis)plot.getDomainAxis();
//        domainAxis.setRange(new Range(0,140));

        
//        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
//     // change the auto tick unit selection to integer units only...
////        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
//        rangeAxis.setRange(new Range(-Math.PI, Math.PI));     
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(1000, 400));
       
       
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
		String result = "HeadingDivergence: " + msg;
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
		print("\t-debug or -d: Enable debug mode");
	}
	
	public static void main(String[] args) {
		String logFileName = null;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-h") || args[i].equals("-help")) {
					usage();
					System.exit(0);
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
			new GetHeadingError(logFileName);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
