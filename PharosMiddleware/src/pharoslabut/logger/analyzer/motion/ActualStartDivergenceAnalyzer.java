package pharoslabut.logger.analyzer.motion;

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
import pharoslabut.logger.analyzer.Line;
import pharoslabut.logger.analyzer.PathEdge;
import pharoslabut.logger.analyzer.RobotExpData;
import pharoslabut.navigate.Location;


/**
 * Computes the actual start motion divergence of a Proteus robot as it follows
 * a GPS-based motion script.
 * 
 * @author Chien-Liang Fok
 */
public class ActualStartDivergenceAnalyzer {
	
	private static final ActualStartDivergenceAnalyzer analyzer = new ActualStartDivergenceAnalyzer();
	
	/**
	 * The constructor.
	 */
	private ActualStartDivergenceAnalyzer() {}
	
	
	/**
	 * An accessor to this singleton object.
	 * 
	 * @return An ActualStartDivergenceAnalyzer object.
	 */
	public static ActualStartDivergenceAnalyzer getAnalyzer() {
		return analyzer;
	}
	
	/**
	 * Save the results into files.
	 * 
	 * @param robotData The robot data obtained during an experiment.
	 * @param actualStartDivs The absolute divergence measurements.
	 * @param absolute whether to report the divergence in absolute terms.
	 */
	public void saveResults(RobotExpData robotData, Vector<SpatialDivergence> actualStartDivs, boolean absolute) {
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
		FileLogger flogger = new FileLogger(outputFilePrefix + "-actualStartDivergence.txt");
		flogger.log("Time (ms)\tDelta Time (ms)\tDelta Time (s)\tActual Start Divergence");
		
		Enumeration<SpatialDivergence> e = actualStartDivs.elements();
		while (e.hasMoreElements()) {
			SpatialDivergence sd = e.nextElement();
			
			double divergence = 0;
			if (absolute)
				divergence = sd.getAbsoluteDivergence();
			else
				divergence = sd.getDivergence();
			
			flogger.log(sd.getTimeStamp() 
					+ "\t" + (sd.getTimeStamp() - robotData.getStartTime()) 
					+ "\t" + (sd.getTimeStamp() - robotData.getStartTime())/1000 
					+ "\t" + divergence);
		}
		
		robotData.printWayPointArrivalTable(flogger); // save the waypoint arrival table
	}
	
	/**
	 * Calculates and returns a vector containing the actual start divergence of a robot as
	 * it follows a GPS-based motion script.
	 * 
	 * @param robotData The robot data obtained during an experiment.
	 * @param samplingInterval The period at which to calculate the absolute divergence.
	 * @return The actual start divergence.
	 */
	public Vector<SpatialDivergence> getActualStartDivergence(RobotExpData robotData, long samplingInterval) {
		long startTime = robotData.getStartTime();
		Vector<SpatialDivergence> result = new Vector<SpatialDivergence>(); 
		
		// For the duration of the experiment, at every sampling interval... 
		for (long time = startTime; time < robotData.getFinalWaypointArrivalTime(); time += samplingInterval) {
			
			// Calculate the actual start divergence by creating the perfect path
			// along which the robot should travel and finding the point on
			// this path that is closest to the robot's actual location.
			PathEdge edge = robotData.getRelevantPathEdge(time);
			
			Line perfectPath = new Line(edge.getStartLoc(), edge.getEndLocation()); // The ideal path starts from where the robot actually started.
			Location actualLoc = robotData.getLocation(time);
			Location idealLoc = perfectPath.getLocationClosestTo(actualLoc);
			
			result.add(new SpatialDivergence(time, actualLoc, idealLoc, perfectPath));
		}
		
		return result;
	}
	
	/**
	 * Displays the data in a graph.
	 */
	private void showPlot(RobotExpData robotData, long samplingInterval, 
			Vector<SpatialDivergence> actualStartDivs, boolean noheading) 
	{
		// Create a data series containing the actual start divergence.
		XYSeries asDivSeries = new XYSeries("Actual Start Divergence (" + samplingInterval + "ms interval)");
		
		Enumeration<SpatialDivergence> e = actualStartDivs.elements();
		while (e.hasMoreElements()) {
			SpatialDivergence currDiv = e.nextElement();
			double currTime = (currDiv.getTimeStamp() - robotData.getStartTime())/1000.0;
			
			double divergence = 0;
			if (noheading)
				divergence = currDiv.getAbsoluteDivergence();
			else
				divergence = currDiv.getDivergence();
			
			asDivSeries.add(currTime, divergence);
		}
		
		// Create two data series containing the times when the robot starts heading 
		// towards a waypoint, and the times when the robot arrives at a waypoint
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
		XYSeriesCollection asDivDataSet = new XYSeriesCollection();
		asDivDataSet.addSeries(asDivSeries);
		asDivDataSet.addSeries(beginEdgeSeries);
		asDivDataSet.addSeries(waypointArrivalSeries);
		
		// Create the chart
		JFreeChart asDivChart = ChartFactory.createXYLineChart(
				"Actual Start Divergence vs. Time",                   // chart title
				"Time (s)",                                           // x axis label
				"Divergence (m)",                                     // y axis label
				asDivDataSet,                                         // the actual start divergence data
				PlotOrientation.VERTICAL,                             // plot orientation (y axis is vertical)
				true,                                                 // include legend
				true,                                                 // tooltips
				false                                                 // urls
		);
       
        // Place the legend on top of the chart just below the title.
        LegendTitle headingLegend = asDivChart.getLegend();
        headingLegend.setPosition(RectangleEdge.TOP);
        
        asDivChart.setBackgroundPaint(Color.white);
        
        // Configure when to display lines an when to display the shapes that indicate data points
        XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer();
        renderer1.setSeriesLinesVisible(0, false); // display the actual start divergence as black triangles
        renderer1.setSeriesShapesVisible(0, true);
        renderer1.setSeriesPaint(0, Color.BLACK);
        renderer1.setSeriesShape(0, org.jfree.util.ShapeUtilities.createUpTriangle(2));
        renderer1.setSeriesLinesVisible(1, false); // display the begin edge traversal points as blue dots
        renderer1.setSeriesShapesVisible(1, true);
        renderer1.setSeriesPaint(1, Color.BLUE);
        renderer1.setSeriesShape(1, new java.awt.geom.Ellipse2D.Double(-3,-3,6,6));
        renderer1.setSeriesLinesVisible(2, false); // display the begin edge traversal points as green dots
        renderer1.setSeriesShapesVisible(2, true);
        renderer1.setSeriesPaint(2, Color.GREEN.darker());
        renderer1.setSeriesShape(2, new java.awt.geom.Ellipse2D.Double(-5,-5,10,10));
        
        final XYPlot headingPlot = asDivChart.getXYPlot();
        headingPlot.setRenderer(0, renderer1);
        
        // Place the charts in their own panels.
        ChartPanel headingChartPanel = new ChartPanel(asDivChart);
        headingChartPanel.setPreferredSize(new java.awt.Dimension(1200, 500));
       
        // Create a frame for the chart, then display it.
        ApplicationFrame appFrame1 = new ApplicationFrame("Divergence for " + robotData.getFileName());
        appFrame1.setContentPane(headingChartPanel);
        appFrame1.pack();
		RefineryUtilities.centerFrameOnScreen(appFrame1);
		appFrame1.setVisible(true);
	}
	
	private static void print(String msg) {
		System.out.println(msg);
	}
	
	private static void printErr(String msg) {
		System.err.println(msg);
	}
	
	private static void usage() {
		print("Description: Calculates the actual start divergence of a robot as it follows a GPS-based motion script.\n");
		print("Usage: " + ActualStartDivergenceAnalyzer.class.getName() + " <options>\n");
		print("Where <options> include:");
		print("\t-log <log file name>: The experiment log file generated by the robot. (required)");
		print("\t-interval <sampling interval>: The interval in milliseconds at which the divergence is calculated. (default 1000)");
		print("\t-save: Save the results into a file with the same name as the log file but ending with \"-absDiv.txt\"");
		print("\t-absolute: Report the divergence in absolute terms.");
		print("\t-d or -debug: Enable debug mode.");
	}
	
	public static void main(String[] args) {
		String logFileName = null;
		long samplingInterval = 1000;
		boolean saveResults = false;
		boolean absolute = false;
		
		try {
			for (int i=0; i < args.length; i++) {
				if (args[i].equals("-h") || args[i].equals("-help")) {
					usage();
					System.exit(0);
				} 
				else if (args[i].equals("-log")) {
					logFileName = args[++i];
				}
				else if (args[i].equals("-interval") || args[i].equals("-i")) {
					samplingInterval = Long.valueOf(args[++i]);
				}
				else if (args[i].equals("-save") || args[i].equals("-s")) {
					saveResults = true;
				}
				else if (args[i].equals("-absolute") || args[i].equals("-a")) {
					absolute = true;
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
		print("Divergence Calculation Interval: " + samplingInterval);
		print("Save results: " + saveResults);
		print("Report absolute value of divergence: " + absolute);
		print("Debug: " + (System.getProperty ("PharosMiddleware.debug") != null));

		RobotExpData robotData = new RobotExpData(logFileName);
		
		ActualStartDivergenceAnalyzer analyzer = ActualStartDivergenceAnalyzer.getAnalyzer();
		Vector<SpatialDivergence> absDivs = analyzer.getActualStartDivergence(robotData, samplingInterval);
		
		if (saveResults)
			analyzer.saveResults(robotData, absDivs, absolute);
		
		analyzer.showPlot(robotData, samplingInterval, absDivs, absolute);
	}
}
