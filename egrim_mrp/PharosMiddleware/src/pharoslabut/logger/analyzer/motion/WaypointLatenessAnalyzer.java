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
import pharoslabut.logger.analyzer.PathEdge;
import pharoslabut.logger.analyzer.RobotExpData;
import pharoslabut.navigate.Location;


/**
 * Computes the temporal divergence of a Proteus robot as it follows
 * a GPS-based motion script.  The temporal divergence is the difference
 * between the time when a robot arrives at a waypoint and when it should
 * have ideally arrived.
 * 
 * @author Chien-Liang Fok
 */
public class WaypointLatenessAnalyzer {
	
	private static final WaypointLatenessAnalyzer analyzer = new WaypointLatenessAnalyzer();
	
	/**
	 * The constructor.
	 */
	private WaypointLatenessAnalyzer() {}
	
	
	/**
	 * An accessor to this singleton object.
	 * 
	 * @return An ActualStartDivergenceAnalyzer object.
	 */
	public static WaypointLatenessAnalyzer getAnalyzer() {
		return analyzer;
	}
	
	/**
	 * Save the results into files.
	 * 
	 * @param robotData The robot data obtained during an experiment.
	 * @param waypointArrivalDivs The waypoint arrival divergence measurements.
	 * @param absolute whether to report the divergence in absolute terms.
	 */
	public void saveResults(RobotExpData robotData, Vector<TemporalDivergence> waypointArrivalDivs, boolean absolute) {
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
		FileLogger flogger = new FileLogger(outputFilePrefix + "-temporalDivergence.txt");
		flogger.log("WayPoint Number\tActual Arrival Time\tIdeal Relative Arrival Time\tIdeal Arrival Time\tTemporal Divergence");
		Enumeration<TemporalDivergence> e2 = waypointArrivalDivs.elements();
		while (e2.hasMoreElements()) {
			TemporalDivergence td = e2.nextElement();
			flogger.log(td.getWaypointNumber()
					+ "\t" + td.getActualTOA()
					+ "\t" + td.getIdealRelTOA()
					+ "\t" + td.getIdealTOA()
					+ "\t" + td.getLateness(absolute)
					+ "\t" + td.getRelLateness(absolute));
		}
		
		robotData.printWayPointArrivalTable(flogger);
	}
	
	/**
	 * Calculates the waypoint arrival time divergence of the robot.  This is the difference between the time
	 * the robot is expected to be at a waypoint and the time it actually arrives at the waypoint.
	 * 
	 * @param robotData The robot data to analyze.
	 * @return The temporal divergence of the robot.
	 */
	public Vector<TemporalDivergence> getWaypointLateness(RobotExpData robotData) {
		System.setProperty ("PharosMiddleware.debug", "true");
		FileLogger flogger = new FileLogger("WaypointLateness.txt");
		Logger.setFileLogger(flogger);
		Logger.logDbg("Calculating how late a robot arrives at a waypoint...");
		
		Vector<PathEdge> pathEdges = robotData.getPathEdges();
		
		if (pathEdges.size() == 0) {
			Logger.logErr("No edges present!");
			System.exit(1);
		}
		
		long edgeStartTime = pathEdges.get(0).getStartTime(); // initialize this to be the start of the first edge
		Logger.logDbg("first edgeStartTime = " + edgeStartTime);
		
		Vector<TemporalDivergence> result = new Vector<TemporalDivergence>();
		
		// First calculate the expected time that the robot is supposed to reach
		// each waypoint.  Then determine the temporal divergence by comparing the expected time
		// with the actual time the robot reached the waypoint.
		for (int i=0; i < pathEdges.size(); i++) {
			PathEdge currEdge = pathEdges.get(i);
			Logger.logDbg("Processing edge " + currEdge.getSeqNo());
			
			Location idealStartLoc = currEdge.getIdealStartLoc();
			Location idealEndLoc = currEdge.getEndLocation();
			double idealSpeed = currEdge.getIdealSpeed(); // This is the speed that the robot is supposed to travel at
			
			double idealDist = idealStartLoc.distanceTo(idealEndLoc);
			double idealDuration = idealDist / idealSpeed * 1000;
			
			long idealTOA = (long)(edgeStartTime + idealDuration); // this is cumulative across the entire experiment
			long idealRelativeTOA = (long)(currEdge.getStartTime() + idealDuration); // this is relative to the previous waypoint
			long actualTOA = currEdge.getEndTime();
			
			TemporalDivergence td = new TemporalDivergence(i, idealTOA, idealRelativeTOA, actualTOA);	
			result.add(td);
			Logger.logDbg("Waypoint " + i 
					+ "\n\tideal start loc = " + idealStartLoc 
					+ "\n\tideal end loc: " + idealEndLoc 
					+ "\n\tideal speed: " + idealSpeed
					+ "\n\tideal TOA = " + idealTOA 
					+ "\n\tideal relative TOA = " + idealRelativeTOA 
					+ "\n\tactual TOA = " + actualTOA 
					+ "\n\tlateness = " + td.getLateness(false)
					+ "\n\trelative lateness = " + td.getRelLateness(false));
			
			/*
			 * The pause time is the difference between the current edge's end time
			 * and the next edge's start time.
			 */
			if (i < pathEdges.size() -1 ) {
				PathEdge nextEdge = pathEdges.get(i+1);
				edgeStartTime = idealTOA + nextEdge.getStartTime() - currEdge.getEndTime(); // include the pause time that the robot stops at each waypoint
			}
		}
		
		return result;
		
	}
	
	/**
	 * Displays the data in a graph.
	 */
	private void showPlot(RobotExpData robotData, Vector<TemporalDivergence> waypointArrivalDivs, boolean noheading) {
		// Create a data series containing the waypoint arrival divergence and relative waypoint arrival divergence.
		XYSeries waDivSeries = new XYSeries("Waypoint Arrival Lateness");
		XYSeries rwaDivSeries = new XYSeries("Waypoint Arrival Relative Lateness");
		
		Enumeration<TemporalDivergence> e3 = waypointArrivalDivs.elements();
		while (e3.hasMoreElements()) {
			TemporalDivergence currDiv = e3.nextElement();
			waDivSeries.add(currDiv.getWaypointNumber(), currDiv.getLateness(noheading) / 1000); // convert the divergence into seconds
			rwaDivSeries.add(currDiv.getWaypointNumber(), currDiv.getRelLateness(noheading) / 1000); // convert the divergence into seconds
		}
		
		// Create data sets out of the data series.
		XYSeriesCollection watDivDataSet = new XYSeriesCollection();
		watDivDataSet.addSeries(waDivSeries);
		watDivDataSet.addSeries(rwaDivSeries);
		
		// Create the chart
		JFreeChart waDivChart = ChartFactory.createXYLineChart(
				"Waypoint Arrival Lateness vs. Time",          // chart title
				"Waypoint Number",                                    // x axis label
				"Lateness (s)",                                     // y axis label
				watDivDataSet,                                        // the waypoint arrival time divergence data
				PlotOrientation.VERTICAL,                             // plot orientation (y axis is vertical)
				true,                                                 // include legend
				true,                                                 // tooltips
				false                                                 // urls
		);
       
        // Place the legend on top of the chart just below the title.
        LegendTitle headingLegend = waDivChart.getLegend();
        headingLegend.setPosition(RectangleEdge.TOP);
        
        waDivChart.setBackgroundPaint(Color.white);
        
        // Configure when to display lines an when to display the shapes that indicate data points
        XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer();
        renderer1.setSeriesLinesVisible(0, false); // display the waypoint arrival time Divergence as black triangles connected by lines
        renderer1.setSeriesShapesVisible(0, true);
        renderer1.setSeriesPaint(0, Color.BLACK);
        renderer1.setSeriesShape(0, org.jfree.util.ShapeUtilities.createUpTriangle(3));
        renderer1.setSeriesLinesVisible(1, false); // display the relative waypoint arrival time Divergence as red crosses connected by lines
        renderer1.setSeriesShapesVisible(1, true);
        renderer1.setSeriesPaint(1, Color.RED);
        renderer1.setSeriesShape(1, org.jfree.util.ShapeUtilities.createRegularCross(3,(float).3));
        
        final XYPlot headingPlot = waDivChart.getXYPlot();
        headingPlot.setRenderer(0, renderer1);
        
        // Place the charts in their own panels.
        ChartPanel headingChartPanel = new ChartPanel(waDivChart);
        headingChartPanel.setPreferredSize(new java.awt.Dimension(1200, 500));
       
        // Create a frame for the chart, then display it.
        ApplicationFrame appFrame1 = new ApplicationFrame("Lateness of " + robotData.getFileName());
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
		print("Description: Calculates the waypoint arrival time divergence of a robot as it follows a GPS-based motion script.\n");
		print("Usage: " + WaypointLatenessAnalyzer.class.getName() + " <options>\n");
		print("Where <options> include:");
		print("\t-log <log file name>: The experiment log file generated by the robot. (required)");
		print("\t-save: Save the results into a file with the same name as the log file but ending with \"-absDiv.txt\"");
		print("\t-absolute: Report the divergence in absolute terms.");
		print("\t-d or -debug: Enable debug mode.");
	}
	
	public static void main(String[] args) {
		String logFileName = null;
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
		print("Save results: " + saveResults);
		print("Report absolute value of divergence: " + absolute);
		print("Debug: " + (System.getProperty ("PharosMiddleware.debug") != null));

		RobotExpData robotData = new RobotExpData(logFileName);
		
		WaypointLatenessAnalyzer analyzer = WaypointLatenessAnalyzer.getAnalyzer();
		Vector<TemporalDivergence> latenessDivs = analyzer.getWaypointLateness(robotData);
		
		if (saveResults)
			analyzer.saveResults(robotData, latenessDivs, absolute);
		
		analyzer.showPlot(robotData, latenessDivs, absolute);
	}
}
